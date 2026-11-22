package zov.alphadlc.util.render.renderers.impl;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import zov.alphadlc.util.render.providers.ResourceProvider;

public final class HudShine {
   private static final MinecraftClient mc = MinecraftClient.method_1551();
   private static final float SWEEP_MS = 2200.0F;
   private static final float PAUSE_MS = 1100.0F;
   private static final ShaderProgramKey SHINE_SHADER = new ShaderProgramKey(
      ResourceProvider.getShaderIdentifier("hud_shine"), VertexFormats.field_1592, Defines.field_53930
   );

   private HudShine() {
   }

   public static void render(MatrixStack matrices, float x, float y, float w, float h, float round) {
      render(matrices, x, y, w, h, round, 0.5F, 1.0F, 0.28F, 1.6F);
   }

   public static void render(MatrixStack matrices, float x, float y, float w, float h, float round, float sizeScale, float speedScale, float brightness) {
      render(matrices, x, y, w, h, round, sizeScale, speedScale, brightness, 1.6F);
   }

   public static void render(
      MatrixStack matrices, float x, float y, float w, float h, float round, float sizeScale, float speedScale, float brightness, float edgeK
   ) {
      if (!(w <= 2.0F) && !(h <= 2.0F)) {
         double cycle = 3300.0;
         double now = (double)System.currentTimeMillis() * (double)speedScale;
         double t = now % cycle;
         if (!(t > 2200.0)) {
            Matrix4f matrix = matrices.method_23760().method_23761();
            float scale = (float)mc.method_22683().method_4495();
            Vector3f pos = matrix.transformPosition(x, y, 0.0F, new Vector3f()).mul(scale);
            Vector3f sz = matrix.getScale(new Vector3f()).mul(scale);
            float scaledWidth = w * sz.x;
            float scaledHeight = h * sz.y;
            float locationX = pos.x;
            float locationY = (float)mc.method_22683().method_4506() - scaledHeight - pos.y;
            float radius = Math.min(round, Math.min(w, h) * 0.5F) * sz.y;
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
            RenderSystem.disableDepthTest();
            RenderSystem.disableCull();
            ShaderProgram shader = RenderSystem.setShader(SHINE_SHADER);
            shader.method_34582("Size").method_1255(scaledWidth, scaledHeight);
            shader.method_34582("Location").method_1255(locationX, locationY);
            shader.method_34582("Radius").method_35657(radius, radius, radius, radius);
            shader.method_34582("BeamWidth").method_1251(0.2F * sizeScale);
            shader.method_34582("Brightness").method_1251(brightness);
            if (shader.method_34582("EdgeK") != null) {
               shader.method_34582("EdgeK").method_1251(edgeK);
            }

            if (shader.method_34582("Time") != null) {
               shader.method_34582("Time").method_1251((float)(now % (4.0 * cycle) / 1000.0));
            }

            float m = 8.0F;
            BufferBuilder buffer = Tessellator.method_1348().method_60827(DrawMode.field_27382, VertexFormats.field_1592);
            buffer.method_22918(matrix, x - m, y - m, 0.0F);
            buffer.method_22918(matrix, x - m, y + h + m, 0.0F);
            buffer.method_22918(matrix, x + w + m, y + h + m, 0.0F);
            buffer.method_22918(matrix, x + w + m, y - m, 0.0F);
            BufferRenderer.method_43433(buffer.method_60800());
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.defaultBlendFunc();
         }
      }
   }
}
