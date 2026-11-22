package zov.alphadlc.util.rotation;

import lombok.Generated;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import zov.alphadlc.util.IMinecraft;
import zov.alphadlc.util.math.RotationUtil;

public class Rotation implements IMinecraft {
   private float yaw;
   private float pitch;

   public Rotation(Entity entity) {
      this.yaw = entity.method_36454();
      this.pitch = entity.method_36455();
   }

   public Rotation(Vec2f vec) {
      this.yaw = vec.field_1343;
      this.pitch = vec.field_1342;
   }

   public Rotation(Vec3d vec) {
      this.yaw = RotationUtil.calculate(vec).field_1343;
      this.pitch = RotationUtil.calculate(vec).field_1342;
   }

   public float getDelta(Rotation target) {
      float yawDelta = MathHelper.method_15393(target.getYaw() - this.yaw);
      float pitchDelta = target.getPitch() - this.pitch;
      return (float)Math.hypot((double)Math.abs(yawDelta), (double)Math.abs(pitchDelta));
   }

   public double getDeltaDouble(Rotation target) {
      double yawDelta = (double)MathHelper.method_15393(target.getYaw() - this.yaw);
      double pitchDelta = (double)MathHelper.method_15393(target.getPitch() - this.pitch);
      return Math.hypot(yawDelta, pitchDelta);
   }

   public static Vector2f camera() {
      return new Vector2f(cameraYaw(), cameraPitch());
   }

   public static float cameraYaw() {
      return MathHelper.method_15393(mc.field_1773.method_19418().method_19330() + (float)(mc.field_1773.method_19418().method_19333() ? 180 : 0));
   }

   public static float cameraPitch() {
      return (float)(mc.field_1773.method_19418().method_19333() ? -1 : 1) * mc.field_1773.method_19418().method_19329();
   }

   public static Rotation from(PlayerEntity player, Entity target) {
      Vec3d playerPos = player.method_5836(0.0F);
      Vec3d targetPos = target.method_19538().method_1031(0.0, (double)target.method_17682() * 0.5, 0.0);
      double dx = targetPos.field_1352 - playerPos.field_1352;
      double dy = targetPos.field_1351 - playerPos.field_1351;
      double dz = targetPos.field_1350 - playerPos.field_1350;
      double distanceXZ = Math.sqrt(dx * dx + dz * dz);
      float yaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
      float pitch = (float)(-Math.toDegrees(Math.atan2(dy, distanceXZ)));
      return new Rotation(yaw, pitch);
   }

   public final Vec3d toVector() {
      float f = this.pitch * (float) (Math.PI / 180.0);
      float g = -this.yaw * (float) (Math.PI / 180.0);
      float h = MathHelper.method_15362(g);
      float i = MathHelper.method_15374(g);
      float j = MathHelper.method_15362(f);
      float k = MathHelper.method_15374(f);
      return new Vec3d((double)(i * j), (double)(-k), (double)(h * j));
   }

   @Generated
   public float getYaw() {
      return this.yaw;
   }

   @Generated
   public float getPitch() {
      return this.pitch;
   }

   @Generated
   public void setYaw(float yaw) {
      this.yaw = yaw;
   }

   @Generated
   public void setPitch(float pitch) {
      this.pitch = pitch;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof Rotation other)) {
         return false;
      } else if (!other.canEqual(this)) {
         return false;
      } else {
         return Float.compare(this.getYaw(), other.getYaw()) != 0 ? false : Float.compare(this.getPitch(), other.getPitch()) == 0;
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof Rotation;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      result = result * 59 + Float.floatToIntBits(this.getYaw());
      return result * 59 + Float.floatToIntBits(this.getPitch());
   }

   @Generated
   @Override
   public String toString() {
      return "Rotation(yaw=" + this.getYaw() + ", pitch=" + this.getPitch() + ")";
   }

   @Generated
   public Rotation() {
   }

   @Generated
   public Rotation(float yaw, float pitch) {
      this.yaw = yaw;
      this.pitch = pitch;
   }
}
