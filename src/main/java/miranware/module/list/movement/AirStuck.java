package miranware.module.list.movement;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import miranware.event.list.EventPacket;
import miranware.event.list.EventTick;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.list.player.ElytraHelper;
import miranware.module.settings.BooleanSetting;
import miranware.module.settings.ModeSetting;
import miranware.module.settings.SliderSetting;
import miranware.util.base.Instance;

@ModuleInformation(
   moduleName = "Air Stuck",
   moduleDesc = "Hang in the air",
   moduleCategory = ModuleCategory.MOVEMENT
)
public class AirStuck extends Module {
   private final ModeSetting mode = new ModeSetting("Mode", "Vanilla", "Vanilla", "Grim", "Polar");
   private final BooleanSetting autoSwapChest = new BooleanSetting(
      "Swap To Chestplate", true
   );
   private final BooleanSetting backElytra = new BooleanSetting("Return On Disable", true)
      .setVisible(this.autoSwapChest::getValue);
   private final BooleanSetting fallCheck = new BooleanSetting(
      "Fall Check", true
   );
   public final BooleanSetting useKillAuraDistance = new BooleanSetting(
      "Custom Distance", false
   );
   public final SliderSetting killAuraDistance = new SliderSetting("Distance", 6.0, 2.0, 8.0, 0.1)
      .setVisible(this.useKillAuraDistance::getValue);
   private Vec3d savedVelocity = Vec3d.ZERO;
   private boolean isElytra;
   private boolean hasFailed = false;
   private int pauseTicks = 0;

   @Subscribe
   private void onPacket(EventPacket e2) {
      if (this.mc.player != null && !this.hasFailed) {
         if (e2.getPacket() instanceof PlayerMoveC2SPacket) {
            if (this.pauseTicks > 0) {
               return;
            }

            e2.cancelEvent();
         }
      }
   }

   @Subscribe
   private void onTick(EventTick e2) {
      if (this.mc.player != null && !this.hasFailed) {
         if (this.pauseTicks > 0) {
            this.pauseTicks--;
         } else {
            this.mc.player.setVelocity(0.0, 0.0, 0.0);
            this.mc.player.setNoGravity(true);
         }
      }
   }

   @Override
   public void onEnable() {
      super.onEnable();
      if (this.mc.player != null && this.mc.world != null) {
         this.hasFailed = false;
         if (this.mc.player.fallDistance == 0.0F && this.fallCheck.getValue()) {
            this.setEnabled(false);
         } else {
            this.mc.player.setNoGravity(true);
            this.savedVelocity = this.mc.player.getVelocity();
            boolean wearingElytra = this.mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA;
            if (wearingElytra && this.autoSwapChest.getValue()) {
               this.isElytra = true;
               Instance.get(ElytraHelper.class).swap(this.mode, true);
            } else {
               this.isElytra = false;
            }

            if (this.mc.player.isOnGround() && this.mc.player.fallDistance == 0.0F) {
               this.setEnabled(false);
            }
         }
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();
      if (this.mc.player != null) {
         if (this.savedVelocity != null && !this.hasFailed) {
            this.mc.player.setVelocity(this.savedVelocity);
         }

         this.mc.player.setNoGravity(false);
         if (this.isElytra) {
            boolean wearingChestPlate = this.mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() instanceof ArmorItem;
            if (wearingChestPlate && this.backElytra.getValue()) {
               Instance.get(ElytraHelper.class).swap(this.mode, false);
            }

            this.isElytra = false;
         }

         this.hasFailed = false;
         this.savedVelocity = Vec3d.ZERO;
      }
   }

   public void forceDisable() {
      if (this.isEnabled()) {
         this.hasFailed = true;
         this.setEnabled(false);
      }
   }

   public void pauseForAttack() {
      this.pauseTicks = 1;
   }
}
