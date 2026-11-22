package zov.alphadlc.module.list.combat;

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
import zov.alphadlc.event.list.EventPlayerSync;
import zov.alphadlc.event.list.EventPlayerUpdate;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.module.settings.ModeListSetting;
import zov.alphadlc.util.math.StopWatch;

@ModuleInformation(
   moduleName = "Auto Potion",
   moduleDesc = "\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438 \u043a\u0438\u0434\u0430\u0435\u0442 \u0432\u044b\u0431\u0440\u0430\u043d\u043d\u044b\u0435 \u0431\u0430\u0444\u044b",
   moduleCategory = ModuleCategory.COMBAT
)
public class AutoPotion extends Module {
   private final ModeListSetting throwSettings = new ModeListSetting(
      "\u041a\u0438\u0434\u0430\u0442\u044c",
      new BooleanSetting("\u0417\u0435\u043b\u044c\u0435 \u0441\u0438\u043b\u044b", true),
      new BooleanSetting("\u0417\u0435\u043b\u044c\u0435 \u0441\u043a\u043e\u0440\u043e\u0441\u0442\u0438", true),
      new BooleanSetting("\u0417\u0435\u043b\u044c\u0435 \u043e\u0433\u043d\u0435\u0441\u0442\u043e\u0439\u043a\u043e\u0441\u0442\u0438", true)
   );
   private final BooleanSetting autoDisable = new BooleanSetting(
      "\u0412\u044b\u043a\u043b\u044e\u0447\u0430\u0442\u044c \u043f\u043e\u0441\u043b\u0435 \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u044f",
      false
   );
   private final StopWatch timer = new StopWatch();
   private boolean throwing = false;
   private static AutoPotion staticInstance;

   @Subscribe
   public void onSync(EventPlayerSync e) {
      if (this.mc.field_1724 != null) {
         this.throwing = this.canThrow();
      }
   }

   @Subscribe
   public void onUpdate(EventPlayerUpdate e) {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
         if (this.throwing) {
            if (this.timer.isReached(500L)) {
               for (AutoPotion.PotionType type : AutoPotion.PotionType.values()) {
                  if (type.isSettingEnabled() && !this.mc.field_1724.method_6059(type.getEffect())) {
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
      int previousSlot = this.mc.field_1724.method_31548().field_7545;
      if (slot < 9) {
         this.mc.method_1562().method_52787(new UpdateSelectedSlotC2SPacket(slot));
         this.sendDownwardUse();
         this.mc.method_1562().method_52787(new UpdateSelectedSlotC2SPacket(previousSlot));
      } else {
         this.mc.field_1761.method_2906(0, slot, previousSlot, SlotActionType.field_7791, this.mc.field_1724);
         this.mc.method_1562().method_52787(new UpdateSelectedSlotC2SPacket(previousSlot));
         this.sendDownwardUse();
         this.mc.field_1761.method_2906(0, slot, previousSlot, SlotActionType.field_7791, this.mc.field_1724);
      }
   }

   private void sendDownwardUse() {
      AutoPotionThrowPlan.ServerRotation rotation = AutoPotionThrowPlan.forYaw(this.mc.field_1724.method_36454());
      this.mc
         .method_1562()
         .method_52787(new LookAndOnGround(rotation.yaw(), rotation.pitch(), this.mc.field_1724.method_24828(), this.mc.field_1724.field_5976));
      this.mc
         .field_1761
         .method_41931(this.mc.field_1687, sequence -> new PlayerInteractItemC2SPacket(Hand.field_5808, sequence, rotation.yaw(), rotation.pitch()));
   }

   private int findPotionSlot(RegistryEntry<StatusEffect> effect) {
      for (int i = 0; i < 45; i++) {
         ItemStack stack = this.mc.field_1724.method_31548().method_5438(i);
         if (stack.method_7909() == Items.field_8436) {
            PotionContentsComponent contents = (PotionContentsComponent)stack.method_57824(DataComponentTypes.field_49651);
            if (contents != null) {
               for (StatusEffectInstance effectInstance : contents.method_57397()) {
                  if (effectInstance.method_5579().equals(effect)) {
                     return i;
                  }
               }
            }
         }
      }

      return -1;
   }

   private boolean canThrow() {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
         boolean onGround = this.mc.field_1724.method_24828()
            || this.mc
               .field_1687
               .method_8320(
                  BlockPos.method_49637(this.mc.field_1724.method_23317(), this.mc.field_1724.method_23318() - 0.3, this.mc.field_1724.method_23321())
               )
               .method_51367();
         if (!onGround) {
            return false;
         } else if (this.mc.field_1724.method_6101()) {
            return false;
         } else if (this.mc.field_1724.method_5765()) {
            return false;
         } else if (this.mc.field_1724.method_31549().field_7479) {
            return false;
         } else if (!this.mc.field_1724.method_5799() && !this.mc.field_1724.method_5771()) {
            for (AutoPotion.PotionType type : AutoPotion.PotionType.values()) {
               if (type.isSettingEnabled() && !this.mc.field_1724.method_6059(type.getEffect()) && this.findPotionSlot(type.getEffect()) != -1) {
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
      STRENGTH(StatusEffects.field_5910, "\u0417\u0435\u043b\u044c\u0435 \u0441\u0438\u043b\u044b"),
      SPEED(StatusEffects.field_5904, "\u0417\u0435\u043b\u044c\u0435 \u0441\u043a\u043e\u0440\u043e\u0441\u0442\u0438"),
      FIRE_RESISTANCE(StatusEffects.field_5918, "\u0417\u0435\u043b\u044c\u0435 \u043e\u0433\u043d\u0435\u0441\u0442\u043e\u0439\u043a\u043e\u0441\u0442\u0438");

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
