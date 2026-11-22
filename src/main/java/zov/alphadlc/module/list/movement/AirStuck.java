package zov.alphadlc.module.list.movement;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import zov.alphadlc.event.list.EventPacket;
import zov.alphadlc.event.list.EventTick;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.list.player.ElytraHelper;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.module.settings.ModeSetting;
import zov.alphadlc.module.settings.SliderSetting;
import zov.alphadlc.util.base.Instance;

@ModuleInformation(
   moduleName = "Air Stuck",
   moduleDesc = "\u0417\u0430\u0432\u0438\u0441\u0430\u043d\u0438\u0435 \u0432 \u0432\u043e\u0437\u0434\u0443\u0445\u0435",
   moduleCategory = ModuleCategory.MOVEMENT
)
public class AirStuck extends Module {
   private final ModeSetting mode = new ModeSetting("\u041c\u043e\u0434", "Vanilla", "Vanilla", "Grim", "Polar");
   private final BooleanSetting autoSwapChest = new BooleanSetting(
      "\u0421\u0432\u0430\u043f \u043d\u0430 \u043d\u0430\u0433\u0440\u0443\u0434\u043d\u0438\u043a", true
   );
   private final BooleanSetting backElytra = new BooleanSetting("\u0412\u0435\u0440\u043d\u0443\u0442\u044c \u043f\u0440\u0438 \u0432\u044b\u043a\u043b", true)
      .setVisible(this.autoSwapChest::getValue);
   private final BooleanSetting fallCheck = new BooleanSetting(
      "\u041f\u0440\u043e\u0432\u0435\u0440\u043a\u0430 \u043d\u0430 \u043f\u0430\u0434\u0435\u043d\u0438\u0435", true
   );
   public final BooleanSetting useKillAuraDistance = new BooleanSetting(
      "\u0421\u0432\u043e\u044f \u0434\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f", false
   );
   public final SliderSetting killAuraDistance = new SliderSetting("\u0414\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f", 6.0, 2.0, 8.0, 0.1)
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
