package zov.alphadlc.util.player.combat;

import lombok.Generated;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import zov.alphadlc.util.IMinecraft;

public final class PredictUtils implements IMinecraft {
   public static Vec3d predict(LivingEntity target, double ticksAhead) {
      if (target.method_6128()) {
         Vec3d vel = target.method_18798();
         double speed = vel.method_1033();
         return speed < 0.01 ? target.method_19538() : target.method_19538().method_1019(vel.method_1021(ticksAhead * 1.25));
      } else if (Math.hypot(target.field_6014 - target.method_23317(), target.field_5969 - target.method_23321()) * 20.0 <= 1.0
         && target.field_6036 - target.method_23318() <= 1.0) {
         return target.method_19538();
      } else {
         Vec3d forward = Vec3d.method_1030(
               target.method_36455() + (target.method_36455() - target.field_6004), target.method_36454() + (target.method_36454() - target.field_5982)
            )
            .method_1021(
               new Vec3d(target.method_23317() - target.field_6014, target.method_23318() - target.field_6036, target.method_23321() - target.field_5969)
                     .method_1033()
                  * ticksAhead
            );
         Vec3d vec3d = target.method_5631(
            target.method_36455() + (target.method_36455() - target.field_6004), target.method_36454() + (target.method_36454() - target.field_5982)
         );
         float f2 = target.method_36455() * (float) (Math.PI / 180.0);
         double d2 = Math.sqrt(vec3d.field_1352 * vec3d.field_1352 + vec3d.field_1350 * vec3d.field_1350);
         double e2 = forward.method_37267();
         boolean bl = target.method_18798().field_1351 <= 0.0;
         double g2 = bl && target.method_6059(StatusEffects.field_5906) ? Math.min(target.method_56989(), 0.01) : target.method_56989();
         double h2 = MathHelper.method_33723(Math.cos((double)f2));
         forward = forward.method_1031(0.0, g2 * (-1.0 + h2 * 0.75), 0.0);
         if (forward.field_1351 < 0.0 && d2 > 0.0) {
            double i2 = forward.field_1351 * -0.1 * h2;
            forward = forward.method_1031(vec3d.field_1352 * i2 / d2, i2, vec3d.field_1350 * i2 / d2);
         }

         if (f2 < 0.0F && d2 > 0.0) {
            double i2 = e2 * (double)(-MathHelper.method_15374(f2)) * 0.04;
            forward = forward.method_1031(-vec3d.field_1352 * i2 / d2, i2 * 2.2F, -vec3d.field_1350 * i2 / d2);
         }

         if (d2 > 0.0) {
            forward = forward.method_1031((vec3d.field_1352 / d2 * e2 - forward.field_1352) * 0.1, 0.0, (vec3d.field_1350 / d2 * e2 - forward.field_1350) * 0.1);
         }

         return target.method_19538().method_1019(forward);
      }
   }

   @Generated
   private PredictUtils() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
