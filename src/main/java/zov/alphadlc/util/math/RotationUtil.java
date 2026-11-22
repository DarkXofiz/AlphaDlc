package zov.alphadlc.util.math;

import lombok.Generated;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import zov.alphadlc.util.IMinecraft;
import zov.alphadlc.util.rotation.Rotation;

public final class RotationUtil implements IMinecraft {
   public static Vec2f calculate(Vec3d fromVec, Vec3d toVec) {
      double TO_DEGREES = 180.0 / Math.PI;
      Vec3d diff = toVec.method_1020(fromVec);
      double distance = Math.hypot(diff.field_1352, diff.field_1350);
      float yaw = (float)(MathHelper.method_15349(diff.field_1350, diff.field_1352) * (180.0 / Math.PI)) - 90.0F;
      float pitch = (float)(-(MathHelper.method_15349(diff.field_1351, distance) * (180.0 / Math.PI)));
      return new Vec2f(yaw, pitch);
   }

   public static Vec2f calculate(Entity entity) {
      return calculate(entity.method_19538().method_1031(0.0, (double)entity.method_18381(entity.method_18376()), 0.0));
   }

   public static Vec2f calculate(Vec3d toVec) {
      return calculate(mc.field_1724.method_19538().method_1031(0.0, (double)mc.field_1724.method_18381(mc.field_1724.method_18376()), 0.0), toVec);
   }

   public static float getAngleDifference(float dir, float yaw) {
      float f = Math.abs(yaw - dir) % 360.0F;
      return f > 180.0F ? 360.0F - f : f;
   }

   public static Vec3d getEyesPos(Entity entity) {
      return entity.method_19538().method_1031(0.0, (double)entity.method_18381(entity.method_18376()), 0.0);
   }

   public static float[] calculateAngle(Vec3d to) {
      return calculateAngle(getEyesPos(mc.field_1724), to);
   }

   public static float[] calculateAngle(Vec3d from, Vec3d to) {
      double difX = to.field_1352 - from.field_1352;
      double difY = (to.field_1351 - from.field_1351) * -1.0;
      double difZ = to.field_1350 - from.field_1350;
      double dist = (double)MathHelper.method_15355((float)(difX * difX + difZ * difZ));
      float yD = (float)MathHelper.method_15338(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0);
      float pD = (float)MathHelper.method_15350(MathHelper.method_15338(Math.toDegrees(Math.atan2(difY, dist))), -90.0, 90.0);
      return new float[]{yD, pD};
   }

   public static Vector3f getDirectionVector(float yaw, float pitch) {
      float yawRadians = (float)Math.toRadians((double)yaw);
      float pitchRadians = (float)Math.toRadians((double)pitch);
      float x = -MathHelper.method_15362(pitchRadians) * MathHelper.method_15374(yawRadians);
      float y = -MathHelper.method_15374(pitchRadians);
      float z = MathHelper.method_15362(pitchRadians) * MathHelper.method_15362(yawRadians);
      return new Vector3f(x, y, z);
   }

   public static float calculateFov(float cameraYaw, float cameraPitch, float targetYaw, float targetPitch) {
      Vector3f cameraDirection = getDirectionVector(cameraYaw, cameraPitch);
      Vector3f targetDirection = getDirectionVector(targetYaw, targetPitch);
      float dotProduct = cameraDirection.dot(targetDirection);
      dotProduct = MathHelper.method_15363(dotProduct, -1.0F, 1.0F);
      float angleRadians = (float)Math.acos((double)dotProduct);
      return (float)Math.toDegrees((double)angleRadians);
   }

   public static Rotation fromVec3d(Vec3d vector) {
      return new Rotation(
         (float)MathHelper.method_15338(Math.toDegrees(Math.atan2(vector.field_1350, vector.field_1352)) - 90.0),
         (float)MathHelper.method_15338(Math.toDegrees(-Math.atan2(vector.field_1351, Math.hypot(vector.field_1352, vector.field_1350))))
      );
   }

   @Generated
   private RotationUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
