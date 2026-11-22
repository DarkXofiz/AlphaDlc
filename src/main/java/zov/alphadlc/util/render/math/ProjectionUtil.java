package zov.alphadlc.util.render.math;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ProjectionUtil {
   private static final MinecraftClient mc = MinecraftClient.method_1551();

   public static Vector2f project(double x, double y, double z) {
      return project(x, y, z, mc.method_61966().method_60637(true));
   }

   public static Vector2f project(double x, double y, double z, float tickDelta) {
      Camera camera = mc.method_1561().field_4686;
      Vec3d cameraPos = camera.method_19326();
      Quaternionf cameraRotation = mc.method_1561().method_24197();
      cameraRotation = cameraRotation.conjugate(new Quaternionf());
      Vector3f result3f = new Vector3f((float)(x - cameraPos.field_1352), (float)(y - cameraPos.field_1351), (float)(z - cameraPos.field_1350));
      result3f.rotate(cameraRotation);
      if ((Boolean)mc.field_1690.method_42448().method_41753() && mc.method_1560() instanceof AbstractClientPlayerEntity playerentity) {
         calculateViewBobbing(playerentity, result3f, tickDelta);
      }

      double fov = (double)mc.field_1773.method_3196(camera, tickDelta, true);
      return calculateScreenPosition(result3f, fov);
   }

   public static Vector2f project(Vec3d vec3d) {
      return project(vec3d, mc.method_61966().method_60637(true));
   }

   public static Vector2f project(Vec3d vec3d, float tickDelta) {
      return project(vec3d.field_1352, vec3d.field_1351, vec3d.field_1350, tickDelta);
   }

   private static void calculateViewBobbing(AbstractClientPlayerEntity playerEntity, Vector3f result3f, float tickDelta) {
      float distanceDelta = playerEntity.field_53039 - playerEntity.field_53038;
      float distance = -(playerEntity.field_53039 + distanceDelta * tickDelta);
      float stride = MathHelper.method_16439(tickDelta, playerEntity.field_7505, playerEntity.field_7483);
      float phase = distance * (float) Math.PI;
      float xRotation = Math.abs(MathHelper.method_15362(phase - 0.2F) * stride) * 5.0F;
      float zRotation = MathHelper.method_15374(phase) * stride * 3.0F;
      result3f.rotate(new Quaternionf().rotationX((float)Math.toRadians((double)xRotation)));
      result3f.rotate(new Quaternionf().rotationZ((float)Math.toRadians((double)zRotation)));
      result3f.add(MathHelper.method_15374(phase) * stride * 0.5F, -Math.abs(MathHelper.method_15362(phase) * stride), 0.0F);
   }

   private static Vector2f calculateScreenPosition(Vector3f result3f, double fov) {
      Window window = mc.method_22683();
      float width = (float)window.method_4486() / 2.0F;
      float height = (float)window.method_4502() / 2.0F;
      float x = result3f.x;
      float y = result3f.y;
      float z = result3f.z;
      float scaleFactor = height / (z * (float)Math.tan(Math.toRadians(fov / 2.0)));
      if (z < 0.0F) {
         float screenX = -x * scaleFactor + width;
         float screenY = y * scaleFactor + height;
         return new Vector2f(screenX, screenY);
      } else {
         return new Vector2f(Float.MAX_VALUE, Float.MAX_VALUE);
      }
   }
}
