package zov.alphadlc.module.list.movement;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.LivingEntity;
import zov.alphadlc.event.list.EventPlayerUpdate;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.list.combat.KillAura;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.module.settings.SliderSetting;
import zov.alphadlc.util.base.Instance;

@ModuleInformation(
   moduleName = "Elytra Motion",
   moduleDesc = "\u0423\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u0435 \u0434\u0432\u0438\u0436\u0435\u043d\u0438\u0435\u043c \u044d\u043b\u0438\u0442\u0440",
   moduleCategory = ModuleCategory.MOVEMENT
)
public class ElytraMotion extends Module {
   private boolean waitTarget;
   private final SliderSetting distance = new SliderSetting("\u0414\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f", 3.0, 1.0, 6.0, 0.1F);
   private final BooleanSetting bypass = new BooleanSetting("\u041e\u0431\u0445\u043e\u0434", false);

   @Subscribe
   private void onPlayerTick(EventPlayerUpdate e2) {
      if (this.mc.player != null) {
         LivingEntity target = Instance.get(KillAura.class).getTarget();
         if (target == null) {
            if (!this.waitTarget) {
               this.mc.player.setNoGravity(false);
               this.waitTarget = true;
            }
         } else {
            this.waitTarget = false;
            float dist = (float)this.mc.player.getEyePos().distanceTo(target.getBoundingBox().getCenter());
            if (this.mc.player.isGliding() && dist < this.distance.getFloatValue()) {
               if (this.bypass.getValue()) {
                  float yaw = this.mc.player.getYaw();
                  double rad = Math.toRadians((double)yaw);
                  double forward = 0.01;
                  double down = -1.0E-4;
                  double moveX = -Math.sin(rad) * forward;
                  double moveZ = Math.cos(rad) * forward;
                  this.mc.player.setVelocity(moveX, down, moveZ);
               } else {
                  this.mc.player.setVelocity(0.0, 0.0, 0.0);
               }

               this.mc.player.setNoGravity(true);
            } else {
               this.mc.player.setNoGravity(false);
            }
         }
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();
      if (this.mc.player != null) {
         this.waitTarget = false;
         this.mc.player.setNoGravity(false);
      }
   }
}
