package zov.alphadlc.util.math;

import lombok.Generated;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext.ShapeType;
import zov.alphadlc.util.IMinecraft;
import zov.alphadlc.util.player.combat.RaytraceUtil;
import zov.alphadlc.util.render.math.MathUtil;
import zov.alphadlc.util.rotation.Rotation;

public final class BestPoint implements IMinecraft {
   private static Vec3d rotationPoint = Vec3d.field_1353;
   private static Vec3d rotationMotion = Vec3d.field_1353;

   public static Vec3d getRotationPoint() {
      return rotationPoint;
   }

   public static Vec3d getNearestPoint(Entity entity) {
      Box box = entity.method_5829();
      double step = 0.1;
      Vec3d bestVec = null;
      double closestDistance = Double.MAX_VALUE;

      for (double x = box.field_1323; x <= box.field_1320; x += step) {
         for (double y = box.field_1322; y <= box.field_1325; y += step) {
            for (double z = box.field_1321; z <= box.field_1324; z += step) {
               Vec3d sample = new Vec3d(x, y, z);
               double dist = mc.field_1724.method_33571().method_1022(sample);
               if (dist < closestDistance) {
                  closestDistance = dist;
                  bestVec = sample;
               }
            }
         }
      }

      return bestVec;
   }

   public static Vec3d getPoint(Entity target) {
      Box box = target.method_5829();
      double width = box.field_1320 - box.field_1323;
      double height = box.field_1325 - box.field_1322;
      double depth = box.field_1324 - box.field_1321;
      double baseX = box.field_1323 + width / 2.0;
      double baseY = box.field_1322 + height * 0.7;
      double baseZ = box.field_1321 + depth / 2.0;
      double time = (double)System.currentTimeMillis() / 50.0;
      int id = target.method_5628();
      double offsetX = Math.sin(time + (double)id) * width * 0.45;
      double offsetY = Math.cos(time * 0.8 + (double)id) * height * 0.1;
      double offsetZ = Math.cos(time * 1.2 + (double)id) * depth * 0.45;
      return new Vec3d(baseX + offsetX, baseY + offsetY, baseZ + offsetZ);
   }

   public static Vec3d getPoint2(Entity target) {
      Box box = target.method_5829();
      double width = box.field_1320 - box.field_1323;
      double height = box.field_1325 - box.field_1322;
      double depth = box.field_1324 - box.field_1321;
      double baseX = box.field_1323 + width / 2.0;
      double baseY = box.field_1322 + height * 0.65;
      double baseZ = box.field_1321 + depth / 2.0;
      double time = (double)System.currentTimeMillis() / 65.0;
      int id = target.method_5628();
      double offsetX = Math.sin(time + (double)id) * width * 0.7;
      double offsetY = Math.cos(time * 0.8 + (double)id) * height * 0.4;
      double offsetZ = Math.cos(time * 1.2 + (double)id) * depth * 0.7;
      return new Vec3d(baseX + offsetX, baseY + offsetY, baseZ + offsetZ);
   }

   public static Vec3d getNearestVisiblePoint(Entity target, Vec3d preferredPoint, double range) {
      if (preferredPoint != null && mc.field_1724 != null && mc.field_1687 != null) {
         if (isPointVisible(target, preferredPoint, range)) {
            return preferredPoint;
         } else {
            Box box = target.method_5829();
            double step = 0.12;
            Vec3d bestPoint = null;
            double bestDistance = Double.MAX_VALUE;

            for (double x = box.field_1323; x <= box.field_1320; x += step) {
               for (double y = box.field_1322; y <= box.field_1325; y += step) {
                  for (double z = box.field_1321; z <= box.field_1324; z += step) {
                     Vec3d sample = new Vec3d(x, y, z);
                     if (isPointVisible(target, sample, range)) {
                        double distanceToCurrent = sample.method_1025(preferredPoint);
                        if (distanceToCurrent < bestDistance) {
                           bestDistance = distanceToCurrent;
                           bestPoint = sample;
                        }
                     }
                  }
               }
            }

            return bestPoint != null ? bestPoint : preferredPoint;
         }
      } else {
         return preferredPoint;
      }
   }

   private static boolean isPointVisible(Entity target, Vec3d point, double range) {
      Vec3d eyePos = mc.field_1724.method_33571();
      double distance = eyePos.method_1022(point);
      if (distance > range) {
         return false;
      } else {
         Vec3d direction = point.method_1020(eyePos).method_1029();
         if (!RaytraceUtil.rayTrace(direction, distance + 0.2, target.method_5829())) {
            return false;
         } else {
            BlockHitResult blockHit = RaytraceUtil.raycast(eyePos, point, ShapeType.field_17558, mc.field_1724);
            return blockHit.method_17783() == Type.field_1333 || eyePos.method_1025(blockHit.method_17784()) >= eyePos.method_1025(point) - 1.0E-4;
         }
      }
   }

   public static Vec3d getMultipoint(Entity target, double distance) {
      float minMotionXZ = 0.005F;
      float maxMotionXZ = 0.015F;
      float minMotionY = 0.0015F;
      float maxMotionY = 0.015F;
      double lenghtX = target.method_5829().method_17939();
      double lenghtY = target.method_5829().method_17940();
      double lenghtZ = target.method_5829().method_17941();
      if (rotationMotion.equals(Vec3d.field_1353)) {
         rotationMotion = new Vec3d((double)MathUtil.random(-0.02F, 0.02F), (double)MathUtil.random(-0.02F, 0.02F), (double)MathUtil.random(-0.02F, 0.02F));
      }

      if (rotationPoint.equals(Vec3d.field_1353)) {
         rotationPoint = new Vec3d(0.0, lenghtY * 0.5, 0.0);
      }

      rotationPoint = rotationPoint.method_1019(rotationMotion);
      double safeX = (lenghtX - 0.1) / 2.0;
      double safeZ = (lenghtZ - 0.1) / 2.0;
      if (rotationPoint.field_1352 >= safeX) {
         rotationMotion = new Vec3d((double)(-MathUtil.random(minMotionXZ, maxMotionXZ)), rotationMotion.method_10214(), rotationMotion.method_10215());
      } else if (rotationPoint.field_1352 <= -safeX) {
         rotationMotion = new Vec3d((double)MathUtil.random(minMotionXZ, maxMotionXZ), rotationMotion.method_10214(), rotationMotion.method_10215());
      }

      if (rotationPoint.field_1351 >= lenghtY * 0.75) {
         rotationMotion = new Vec3d(rotationMotion.method_10216(), (double)(-MathUtil.random(minMotionY, maxMotionY)), rotationMotion.method_10215());
      } else if (rotationPoint.field_1351 <= lenghtY * 0.3) {
         rotationMotion = new Vec3d(rotationMotion.method_10216(), (double)MathUtil.random(minMotionY, maxMotionY), rotationMotion.method_10215());
      }

      if (rotationPoint.field_1350 >= safeZ) {
         rotationMotion = new Vec3d(rotationMotion.method_10216(), rotationMotion.method_10214(), (double)(-MathUtil.random(minMotionXZ, maxMotionXZ)));
      } else if (rotationPoint.field_1350 <= -safeZ) {
         rotationMotion = new Vec3d(rotationMotion.method_10216(), rotationMotion.method_10214(), (double)MathUtil.random(minMotionXZ, maxMotionXZ));
      }

      rotationPoint.method_1031((double)MathUtil.random(-0.05F, 0.05F), 0.0, (double)MathUtil.random(-0.05F, 0.05F));
      if (!RaytraceUtil.rayTrace(mc.field_1724.method_5720(), distance, target.method_5829())) {
         float halfBox = (float)(lenghtX / 2.0) * 0.8F;

         for (float x1 = -halfBox; x1 <= halfBox; x1 += 0.1F) {
            for (float z1 = -halfBox; z1 <= halfBox; z1 += 0.1F) {
               for (float y1 = (float)(lenghtY * 0.9); (double)y1 >= lenghtY * 0.3; y1 -= 0.1F) {
                  Vec3d v1 = new Vec3d(target.method_23317() + (double)x1, target.method_23318() + (double)y1, target.method_23321() + (double)z1);
                  Rotation rotation = RotationUtil.fromVec3d(v1);
                  if (RaytraceUtil.rayTrace(rotation.toVector(), distance, target.method_5829())) {
                     rotationPoint = new Vec3d((double)x1, (double)y1, (double)z1);
                     return target.method_19538().method_1019(rotationPoint);
                  }
               }
            }
         }
      }

      return target.method_19538().method_1019(rotationPoint);
   }

   @Generated
   private BestPoint() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
