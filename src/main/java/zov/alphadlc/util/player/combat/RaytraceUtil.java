package zov.alphadlc.util.player.combat;

import java.util.Objects;
import java.util.function.Predicate;
import lombok.Generated;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import zov.alphadlc.util.IMinecraft;
import zov.alphadlc.util.rotation.Rotation;

public final class RaytraceUtil implements IMinecraft {
   public static BlockHitResult raycast(double range, Rotation angle, boolean includeFluids) {
      return raycast(Objects.requireNonNull(mc.field_1724).method_5836(1.0F), range, angle, includeFluids);
   }

   public static BlockHitResult raycast(Vec3d vec, double range, Rotation angle, boolean includeFluids) {
      Entity entity = mc.field_1719;
      if (entity == null) {
         return null;
      } else {
         Vec3d rotationVec = angle.toVector();
         Vec3d end = vec.method_1031(rotationVec.field_1352 * range, rotationVec.field_1351 * range, rotationVec.field_1350 * range);
         World world = mc.field_1687;
         if (world == null) {
            return null;
         } else {
            FluidHandling fluidHandling = includeFluids ? FluidHandling.field_1347 : FluidHandling.field_1348;
            RaycastContext context = new RaycastContext(vec, end, ShapeType.field_17559, fluidHandling, entity);
            return world.method_17742(context);
         }
      }
   }

   public static BlockHitResult raycast(Vec3d start, Vec3d end, ShapeType shapeType) {
      return raycast(start, end, shapeType, mc.field_1724);
   }

   public static BlockHitResult raycast(Vec3d start, Vec3d end, ShapeType shapeType, Entity entity) {
      return mc.field_1687.method_17742(new RaycastContext(start, end, shapeType, FluidHandling.field_1348, entity));
   }

   public static EntityHitResult raytraceEntity(double range, Rotation angle, Predicate<Entity> filter) {
      Entity entity = mc.field_1719;
      if (entity == null) {
         return null;
      } else {
         Vec3d cameraVec = entity.method_5836(1.0F);
         Vec3d rotationVec = angle.toVector();
         Vec3d vec3d3 = cameraVec.method_1031(rotationVec.field_1352 * range, rotationVec.field_1351 * range, rotationVec.field_1350 * range);
         Box box = entity.method_5829().method_18804(rotationVec.method_1021(range)).method_1009(1.0, 1.0, 1.0);
         return ProjectileUtil.method_18075(entity, cameraVec, vec3d3, box, e -> !e.method_7325() && filter.test(e), range * range);
      }
   }

   public static boolean rayTrace(Vec3d clientVec, double range, Box box) {
      Vec3d cameraVec = Objects.requireNonNull(mc.field_1724).method_33571();
      return box.method_1006(cameraVec) || box.method_992(cameraVec, cameraVec.method_1019(clientVec.method_1021(range))).isPresent();
   }

   @Generated
   private RaytraceUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
