package miranware.module.list.combat;

import com.google.common.eventbus.Subscribe;
import lombok.Generated;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import miranware.event.list.EventPlayerSync;
import miranware.event.list.EventPlayerUpdate;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.BooleanSetting;
import miranware.module.settings.ModeListSetting;
import miranware.util.math.StopWatch;

@ModuleInformation(
   moduleName = "Auto Potion",
   moduleDesc = "Automatically throws the selected buffs",
   moduleCategory = ModuleCategory.COMBAT
)
public class AutoPotion extends Module {
   private final ModeListSetting throwSettings = new ModeListSetting(
      "Throw",
      new BooleanSetting("Strength Potion", true),
      new BooleanSetting("Speed Potion", true),
      new BooleanSetting("Fire Resistance Potion", true)
   );
   private final BooleanSetting autoDisable = new BooleanSetting(
      "Disable After Use",
      false
   );
   private final StopWatch timer = new StopWatch();
   private boolean throwing = false;
   private static AutoPotion staticInstance;

   @Subscribe
   public void onSync(EventPlayerSync e) {
      if (this.mc.player != null) {
         this.throwing = this.canThrow();
      }
   }

   @Subscribe
   public void onUpdate(EventPlayerUpdate e) {
      if (this.mc.player != null && this.mc.world != null) {
         if (this.throwing) {
            if (this.timer.isReached(500L)) {
               for (AutoPotion.PotionType type : AutoPotion.PotionType.values()) {
                  if (type.isSettingEnabled() && !this.mc.player.hasStatusEffect(type.getEffect())) {
                     int slot = this.findPotionSlot(type.getEffect());
                     if (slot != -1) {
                        this.throwPotion(slot);
                        this.timer.reset();
                        if (this.autoDisable.getValue()) {
                           this.setEnabled(false);
                        }

                        return;
                     }
                  }
               }
            }
         }
      }
   }

   private void throwPotion(int slot) {
      int previousSlot = this.mc.player.getInventory().selectedSlot;
      if (slot < 9) {
         this.mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
         this.sendDownwardUse();
         this.mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
      } else {
         this.mc.interactionManager.clickSlot(0, slot, previousSlot, SlotActionType.SWAP, this.mc.player);
         this.mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
         this.sendDownwardUse();
         this.mc.interactionManager.clickSlot(0, slot, previousSlot, SlotActionType.SWAP, this.mc.player);
      }
   }

   private void sendDownwardUse() {
      AutoPotionThrowPlan.ServerRotation rotation = AutoPotionThrowPlan.forYaw(this.mc.player.getYaw());
      this.mc
         .getNetworkHandler()
         .sendPacket(new LookAndOnGround(rotation.yaw(), rotation.pitch(), this.mc.player.isOnGround(), this.mc.player.horizontalCollision));
      this.mc
         .interactionManager
         .sendSequencedPacket(this.mc.world, sequence -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, sequence, rotation.yaw(), rotation.pitch()));
   }

   private int findPotionSlot(RegistryEntry<StatusEffect> effect) {
      for (int i = 0; i < 45; i++) {
         ItemStack stack = this.mc.player.getInventory().getStack(i);
         if (stack.getItem() == Items.SPLASH_POTION) {
            PotionContentsComponent contents = (PotionContentsComponent)stack.get(DataComponentTypes.POTION_CONTENTS);
            if (contents != null) {
               for (StatusEffectInstance effectInstance : contents.getEffects()) {
                  if (effectInstance.getEffectType().equals(effect)) {
                     return i;
                  }
               }
            }
         }
      }

      return -1;
   }

   private boolean canThrow() {
      if (this.mc.player != null && this.mc.world != null) {
         boolean onGround = this.mc.player.isOnGround()
            || this.mc
               .world
               .getBlockState(
                  BlockPos.ofFloored(this.mc.player.getX(), this.mc.player.getY() - 0.3, this.mc.player.getZ())
               )
               .isSolid();
         if (!onGround) {
            return false;
         } else if (this.mc.player.isClimbing()) {
            return false;
         } else if (this.mc.player.hasVehicle()) {
            return false;
         } else if (this.mc.player.getAbilities().flying) {
            return false;
         } else if (!this.mc.player.isTouchingWater() && !this.mc.player.isInLava()) {
            for (AutoPotion.PotionType type : AutoPotion.PotionType.values()) {
               if (type.isSettingEnabled() && !this.mc.player.hasStatusEffect(type.getEffect()) && this.findPotionSlot(type.getEffect()) != -1) {
                  return true;
               }
            }

            return false;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public void onDisable() {
      this.throwing = false;
      super.onDisable();
   }

   public AutoPotion() {
      staticInstance = this;
   }

   public static AutoPotion getStaticInstance() {
      return staticInstance;
   }

   private static enum PotionType {
      STRENGTH(StatusEffects.STRENGTH, "Strength Potion"),
      SPEED(StatusEffects.SPEED, "Speed Potion"),
      FIRE_RESISTANCE(StatusEffects.FIRE_RESISTANCE, "Fire Resistance Potion");

      private final RegistryEntry<StatusEffect> effect;
      private final String settingName;

      public boolean isSettingEnabled() {
         return AutoPotion.getStaticInstance().throwSettings.isEnabled(this.settingName);
      }

      @Generated
      public RegistryEntry<StatusEffect> getEffect() {
         return this.effect;
      }

      @Generated
      public String getSettingName() {
         return this.settingName;
      }

      @Generated
      private PotionType(final RegistryEntry<StatusEffect> effect, final String settingName) {
         this.effect = effect;
         this.settingName = settingName;
      }
   }
}
