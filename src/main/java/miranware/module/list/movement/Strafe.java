package miranware.module.list.movement;

import com.google.common.eventbus.Subscribe;
import net.minecraft.util.math.MathHelper;
import miranware.event.list.EventTick;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.ModeSetting;
import miranware.module.settings.SliderSetting;
import miranware.util.player.move.MoveUtil;
import miranware.util.rotation.Rotation;
import miranware.util.rotation.RotationComponent;

@ModuleInformation(
   moduleName = "Strafe",
   moduleDesc = "Horizontal strafes with a speed matrix",
   moduleCategory = ModuleCategory.MOVEMENT
)
public class Strafe extends Module {
   private final ModeSetting mode = new ModeSetting(
      "Mode", "Matrix", "Matrix", "Grim"
   );
   private final SliderSetting speed = new SliderSetting("Speed", 0.42, 0.0, 1.0, 0.01);

   @Subscribe
   public void onTick(EventTick event) {
      if (this.mc.player != null && this.mc.world != null) {
         boolean moving = MoveUtil.hasPlayerMovement();
         if (this.mode.is("Matrix")) {
            MoveUtil.setMotion(moving ? this.speed.getValue() * 1.5 : 0.0);
         } else if (this.mode.is("Grim")) {
            if (!moving) {
               return;
            }

            MoveUtil.setMotion(this.speed.getValue() * 1.5);
            float forward = this.mc.player.input.movementForward;
            float strafe = this.mc.player.input.movementSideways;
            float moveYaw = (float)Math.toDegrees(RotationComponent.direction(this.mc.player.getYaw(), forward, strafe));
            moveYaw = MathHelper.wrapDegrees(moveYaw);
            RotationComponent.update(new Rotation(moveYaw, this.mc.player.getPitch()), 360.0F, 360.0F, 2, 0);
         }
      }
   }
}
