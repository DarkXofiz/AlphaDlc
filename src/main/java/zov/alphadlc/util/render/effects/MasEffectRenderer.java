package zov.alphadlc.util.render.effects;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import zov.alphadlc.util.render.providers.ResourceProvider;

public final class MasEffectRenderer {
   private static final Map<String, Identifier[]> CACHE = new HashMap<>();

   private MasEffectRenderer() {
   }

   public static void render(String effect, MatrixStack matrices, Camera camera, Vec3d position, long ageMs, long durationMs, float size, int color) {
      Identifier[] frames = frames(effect);
      if (frames.length != 0 && ageMs >= 0L && ageMs < durationMs) {
         float progress = Math.min(0.999F, (float)ageMs / (float)durationMs);
         int frame = Math.min(frames.length - 1, (int)(progress * (float)frames.length));
         float fade = Math.min(1.0F, Math.min(progress * 5.0F, (1.0F - progress) * 5.0F));
         int alpha = Math.max(0, Math.min(255, (int)((float)(color >>> 24 & 0xFF) * fade)));
         int rgba = alpha << 24 | color & 16777215;
         Vec3d cameraPos = camera.method_19326();
         matrices.method_22903();
         matrices.method_22904(
            position.field_1352 - cameraPos.field_1352, position.field_1351 - cameraPos.field_1351, position.field_1350 - cameraPos.field_1350
         );
         matrices.method_22907(camera.method_23767());
         Matrix4f matrix = matrices.method_23760().method_23761();
         RenderSystem.setShader(ShaderProgramKeys.field_53880);
         RenderSystem.setShaderTexture(0, frames[frame]);
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
         RenderSystem.disableCull();
         RenderSystem.depthMask(false);
         RenderSystem.enableDepthTest();
         BufferBuilder buffer = Tessellator.method_1348().method_60827(DrawMode.field_27382, VertexFormats.field_1575);
         buffer.method_22918(matrix, -size, size, 0.0F).method_22913(0.0F, 1.0F).method_39415(rgba);
         buffer.method_22918(matrix, size, size, 0.0F).method_22913(1.0F, 1.0F).method_39415(rgba);
         buffer.method_22918(matrix, size, -size, 0.0F).method_22913(1.0F, 0.0F).method_39415(rgba);
         buffer.method_22918(matrix, -size, -size, 0.0F).method_22913(0.0F, 0.0F).method_39415(rgba);
         BufferRenderer.method_43433(buffer.method_60800());
         RenderSystem.depthMask(true);
         RenderSystem.enableCull();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableBlend();
         RenderSystem.setShaderTexture(0, 0);
         matrices.method_22909();
      }
   }

   public static Identifier[] frames(String effect) {
      return CACHE.computeIfAbsent(effect, MasEffectRenderer::loadFrames);
   }

   private static Identifier[] loadFrames(String effect) {
      try {
         JsonObject json = ResourceProvider.toJson(Identifier.method_60655("mre", "maseffects/" + effect + ".json"));
         JsonArray textures = json.getAsJsonArray("textures");
         Identifier[] frames = new Identifier[textures.size()];
         int i = 0;

         for (JsonElement element : textures) {
            String raw = element.getAsString();
            String path = raw.contains(":") ? raw.substring(raw.indexOf(58) + 1) : raw;
            frames[i++] = Identifier.method_60655("mre", "maseffects/" + path + ".png");
         }

         return frames;
      } catch (RuntimeException var9) {
         return new Identifier[0];
      }
   }
}
