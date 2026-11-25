package miranware.util.math;

import lombok.Generated;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext.ShapeType;
import miranware.util.IMinecraft;
import miranware.util.player.combat.RaytraceUtil;
import miranware.util.render.math.MathUtil;
import miranware.util.rotation.Rotation;

public final class BestPoint implements IMinecraft {
   private static Vec3d rotationPoint = Vec3d.ZERO;
   private static Vec3d rotationMotion = Vec3d.ZERO;

   public static Vec3d getRotationPoint() {
      return rotationPoint;
   }

   public static Vec3d getNearestPoint(Entity entity) {
      Box box = entity.getBoundingBox();
      double step = 0.1;
      Vec3d bestVec = null;
      double closestDistance = Double.MAX_VALUE;

      for (double x = box.minX; x <= box.maxX; x += step) {
         for (double y = box.minY; y <= box.maxY; y += step) {
            for (double z = box.minZ; z <= box.maxZ; z += step) {
               Vec3d sample = new Vec3d(x, y, z);
               double dist = mc.player.getEyePos().distanceTo(sample);
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
      Box box = target.getBoundingBox();
      double width = box.maxX - box.minX;
      double height = box.maxY - box.minY;
      double depth = box.maxZ - box.minZ;
      double baseX = box.minX + width / 2.0;
      double baseY = box.minY + height * 0.7;
      double baseZ = box.minZ + depth / 2.0;
      double time = (double)System.currentTimeMillis() / 50.0;
      int id = target.getId();
      double offsetX = Math.sin(time + (double)id) * width * 0.45;
      double offsetY = Math.cos(time * 0.8 + (double)id) * height * 0.1;
      double offsetZ = Math.cos(time * 1.2 + (double)id) * depth * 0.45;
      return new Vec3d(baseX + offsetX, baseY + offsetY, baseZ + offsetZ);
   }

   public static Vec3d getPoint2(Entity target) {
      Box box = target.getBoundingBox();
      double width = box.maxX - box.minX;
      double height = box.maxY - box.minY;
      double depth = box.maxZ - box.minZ;
      double baseX = box.minX + width / 2.0;
      double baseY = box.minY + height * 0.65;
      double baseZ = box.minZ + depth / 2.0;
      double time = (double)System.currentTimeMillis() / 65.0;
      int id = target.getId();
      double offsetX = Math.sin(time + (double)id) * width * 0.7;
      double offsetY = Math.cos(time * 0.8 + (double)id) * height * 0.4;
      double offsetZ = Math.cos(time * 1.2 + (double)id) * depth * 0.7;
      return new Vec3d(baseX + offsetX, baseY + offsetY, baseZ + offsetZ);
   }

   public static Vec3d getNearestVisiblePoint(Entity target, Vec3d preferredPoint, double range) {
      if (preferredPoint != null && mc.player != null && mc.world != null) {
         if (isPointVisible(target, preferredPoint, range)) {
            return preferredPoint;
         } else {
            Box box = target.getBoundingBox();
            double step = 0.12;
            Vec3d bestPoint = null;
            double bestDistance = Double.MAX_VALUE;

            for (double x = box.minX; x <= box.maxX; x += step) {
               for (double y = box.minY; y <= box.maxY; y += step) {
                  for (double z = box.minZ; z <= box.maxZ; z += step) {
                     Vec3d sample = new Vec3d(x, y, z);
                     if (isPointVisible(target, sample, range)) {
                        double distanceToCurrent = sample.squaredDistanceTo(preferredPoint);
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
      Vec3d eyePos = mc.player.getEyePos();
      double distance = eyePos.distanceTo(point);
      if (distance > range) {
         return false;
      } else {
         Vec3d direction = point.subtract(eyePos).normalize();
         if (!RaytraceUtil.rayTrace(direction, distance + 0.2, target.getBoundingBox())) {
            return false;
         } else {
            BlockHitResult blockHit = RaytraceUtil.raycast(eyePos, point, ShapeType.COLLIDER, mc.player);
            return blockHit.getType() == Type.MISS || eyePos.squaredDistanceTo(blockHit.getPos()) >= eyePos.squaredDistanceTo(point) - 1.0E-4;
         }
      }
   }

   public static Vec3d getMultipoint(Entity target, double distance) {
      float minMotionXZ = 0.005F;
      float maxMotionXZ = 0.015F;
      float minMotionY = 0.0015F;
      float maxMotionY = 0.015F;
      double lenghtX = target.getBoundingBox().getLengthX();
      double lenghtY = target.getBoundingBox().getLengthY();
      double lenghtZ = target.getBoundingBox().getLengthZ();
      if (rotationMotion.equals(Vec3d.ZERO)) {
         rotationMotion = new Vec3d((double)MathUtil.random(-0.02F, 0.02F), (double)MathUtil.random(-0.02F, 0.02F), (double)MathUtil.random(-0.02F, 0.02F));
      }

      if (rotationPoint.equals(Vec3d.ZERO)) {
         rotationPoint = new Vec3d(0.0, lenghtY * 0.5, 0.0);
      }

      rotationPoint = rotationPoint.add(rotationMotion);
      double safeX = (lenghtX - 0.1) / 2.0;
      double safeZ = (lenghtZ - 0.1) / 2.0;
      if (rotationPoint.x >= safeX) {
         rotationMotion = new Vec3d((double)(-MathUtil.random(minMotionXZ, maxMotionXZ)), rotationMotion.getY(), rotationMotion.getZ());
      } else if (rotationPoint.x <= -safeX) {
         rotationMotion = new Vec3d((double)MathUtil.random(minMotionXZ, maxMotionXZ), rotationMotion.getY(), rotationMotion.getZ());
      }

      if (rotationPoint.y >= lenghtY * 0.75) {
         rotationMotion = new Vec3d(rotationMotion.getX(), (double)(-MathUtil.random(minMotionY, maxMotionY)), rotationMotion.getZ());
      } else if (rotationPoint.y <= lenghtY * 0.3) {
         rotationMotion = new Vec3d(rotationMotion.getX(), (double)MathUtil.random(minMotionY, maxMotionY), rotationMotion.getZ());
      }

      if (rotationPoint.z >= safeZ) {
         rotationMotion = new Vec3d(rotationMotion.getX(), rotationMotion.getY(), (double)(-MathUtil.random(minMotionXZ, maxMotionXZ)));
      } else if (rotationPoint.z <= -safeZ) {
         rotationMotion = new Vec3d(rotationMotion.getX(), rotationMotion.getY(), (double)MathUtil.random(minMotionXZ, maxMotionXZ));
      }

      rotationPoint.add((double)MathUtil.random(-0.05F, 0.05F), 0.0, (double)MathUtil.random(-0.05F, 0.05F));
      if (!RaytraceUtil.rayTrace(mc.player.getRotationVector(), distance, target.getBoundingBox())) {
         float halfBox = (float)(lenghtX / 2.0) * 0.8F;

         for (float x1 = -halfBox; x1 <= halfBox; x1 += 0.1F) {
            for (float z1 = -halfBox; z1 <= halfBox; z1 += 0.1F) {
               for (float y1 = (float)(lenghtY * 0.9); (double)y1 >= lenghtY * 0.3; y1 -= 0.1F) {
                  Vec3d v1 = new Vec3d(target.getX() + (double)x1, target.getY() + (double)y1, target.getZ() + (double)z1);
                  Rotation rotation = RotationUtil.fromVec3d(v1);
                  if (RaytraceUtil.rayTrace(rotation.toVector(), distance, target.getBoundingBox())) {
                     rotationPoint = new Vec3d((double)x1, (double)y1, (double)z1);
                     return target.getPos().add(rotationPoint);
                  }
               }
            }
         }
      }

      return target.getPos().add(rotationPoint);
   }

   @Generated
   private BestPoint() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
