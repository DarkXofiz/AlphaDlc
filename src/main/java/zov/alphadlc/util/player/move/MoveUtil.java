package zov.alphadlc.util.player.move;

import java.util.Objects;
import lombok.Generated;
import net.minecraft.entity.Entity;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import zov.alphadlc.util.IMinecraft;

public final class MoveUtil implements IMinecraft {
   public static boolean hasPlayerMovement() {
      return mc.player.input.movementForward != 0.0F || mc.player.input.movementSideways != 0.0F;
   }

   public static boolean isMoving() {
      return hasPlayerMovement();
   }

   public static double getDirection() {
      return direction(mc.player.getYaw(), mc.player.input.movementForward, mc.player.input.movementSideways);
   }

   public static double[] calculateDirection(double distance) {
      float forward = mc.player.input.movementForward;
      float sideways = mc.player.input.movementSideways;
      float yaw = mc.player.getYaw();
      if (forward != 0.0F) {
         if (sideways > 0.0F) {
            yaw += forward > 0.0F ? -45.0F : 45.0F;
         } else if (sideways < 0.0F) {
            yaw += forward > 0.0F ? 45.0F : -45.0F;
         }

         sideways = 0.0F;
         forward = forward > 0.0F ? 1.0F : -1.0F;
      }

      double sinYaw = Math.sin(Math.toRadians((double)(yaw + 90.0F)));
      double cosYaw = Math.cos(Math.toRadians((double)(yaw + 90.0F)));
      double xMovement = (double)forward * distance * cosYaw + (double)sideways * distance * sinYaw;
      double zMovement = (double)forward * distance * sinYaw - (double)sideways * distance * cosYaw;
      return new double[]{xMovement, zMovement};
   }

   public static double getSpeedSqrt(Entity entity) {
      double dx = entity.getX() - entity.prevX;
      double dy = entity.getY() - entity.prevY;
      double dz = entity.getZ() - entity.prevZ;
      return Math.sqrt(dx * dx + dz * dz + dy * dy);
   }

   public static void setMotion(double velocity) {
      double[] direction = calculateDirection(velocity);
      Objects.requireNonNull(mc.player).setVelocity(direction[0], mc.player.getVelocity().getY(), direction[1]);
   }

   public static void setMotion(double velocity, double y) {
      double[] direction = calculateDirection(velocity);
      Objects.requireNonNull(mc.player).setVelocity(direction[0], y, direction[1]);
   }

   public static double getDegreesRelativeToView(Vec3d positionRelativeToPlayer, float yaw) {
      float optimalYaw = (float)Math.atan2(-positionRelativeToPlayer.x, positionRelativeToPlayer.z);
      double currentYaw = Math.toRadians((double)MathHelper.wrapDegrees(yaw));
      return Math.toDegrees(MathHelper.wrapDegrees((double)optimalYaw - currentYaw));
   }

   public static PlayerInput getDirectionalInputForDegrees(PlayerInput input, double dgs, float deadAngle) {
      boolean forwards = input.comp_3159();
      boolean backwards = input.comp_3160();
      boolean left = input.comp_3161();
      boolean right = input.comp_3162();
      if (dgs >= (double)(-90.0F + deadAngle) && dgs <= (double)(90.0F - deadAngle)) {
         forwards = true;
      } else if (dgs < (double)(-90.0F - deadAngle) || dgs > (double)(90.0F + deadAngle)) {
         backwards = true;
      }

      if (dgs >= (double)(0.0F + deadAngle) && dgs <= (double)(180.0F - deadAngle)) {
         right = true;
      } else if (dgs >= (double)(-180.0F + deadAngle) && dgs <= (double)(0.0F - deadAngle)) {
         left = true;
      }

      return new PlayerInput(forwards, backwards, left, right, input.comp_3163(), input.comp_3164(), input.comp_3165());
   }

   public static double direction(float rotationYaw, float moveForward, float moveStrafing) {
      if (moveForward < 0.0F) {
         rotationYaw += 180.0F;
      }

      float forward = 1.0F;
      if (moveForward < 0.0F) {
         forward = -0.5F;
      }

      if (moveForward > 0.0F) {
         forward = 0.5F;
      }

      if (moveStrafing > 0.0F) {
         rotationYaw -= 90.0F * forward;
      }

      if (moveStrafing < 0.0F) {
         rotationYaw += 90.0F * forward;
      }

      return Math.toRadians((double)rotationYaw);
   }

   public static PlayerInput getDirectionalInputForDegrees(PlayerInput input, double dgs) {
      return getDirectionalInputForDegrees(input, dgs, 20.0F);
   }

   @Generated
   private MoveUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
