package miranware.module.list.movement;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.LivingEntity;
import miranware.event.list.EventPlayerUpdate;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.list.combat.KillAura;
import miranware.module.settings.BooleanSetting;
import miranware.module.settings.SliderSetting;
import miranware.util.base.Instance;

@ModuleInformation(
   moduleName = "Elytra Motion",
   moduleDesc = "Elytra movement control",
   moduleCategory = ModuleCategory.MOVEMENT
)
public class ElytraMotion extends Module {
   private boolean waitTarget;
   private final SliderSetting distance = new SliderSetting("Distance", 3.0, 1.0, 6.0, 0.1F);
   private final BooleanSetting bypass = new BooleanSetting("Bypass", false);

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
