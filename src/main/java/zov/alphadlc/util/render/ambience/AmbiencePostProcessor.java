package zov.alphadlc.util.render.ambience;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import zov.alphadlc.module.list.render.Ambience;
import zov.alphadlc.util.render.providers.ResourceProvider;

public final class AmbiencePostProcessor {
   private static final MinecraftClient MC = MinecraftClient.method_1551();
   private static final ShaderProgramKey SATURATION_KEY = new ShaderProgramKey(
      ResourceProvider.getShaderIdentifier("saturation"), VertexFormats.field_1576, Defines.field_53930
   );
   private static Framebuffer sourceBuffer;
   private static int width = -1;
   private static int height = -1;

   private AmbiencePostProcessor() {
   }

   public static void apply(Ambience module) {
      if (module != null && module.isEnabled() && module.worldSaturation.getValue()) {
         if (MC.field_1687 != null && MC.method_1522() != null) {
            int w = Math.max(1, MC.method_22683().method_4489());
            int h = Math.max(1, MC.method_22683().method_4506());
            ensureBuffer(w, h);
            if (sourceBuffer != null) {
               int readFbo = GL11.glGetInteger(36010);
               int drawFbo = GL11.glGetInteger(36006);
               int sourceFbo = readFbo != 0 ? readFbo : drawFbo;
               GL30.glBindFramebuffer(36008, sourceFbo);
               GL30.glBindFramebuffer(36009, sourceBuffer.field_1476);
               GL30.glBlitFramebuffer(0, 0, w, h, 0, 0, w, h, 16384, 9728);
               GL30.glBindFramebuffer(36008, readFbo);
               GL30.glBindFramebuffer(36009, drawFbo);

               try {
                  GL30.glBindFramebuffer(36160, drawFbo);
                  RenderSystem.viewport(0, 0, w, h);
                  RenderSystem.disableDepthTest();
                  RenderSystem.depthMask(false);
                  RenderSystem.disableBlend();
                  RenderSystem.setShaderTexture(0, sourceBuffer.method_30277());
                  ShaderProgram shader = RenderSystem.setShader(SATURATION_KEY);
                  if (shader != null) {
                     if (shader.method_34582("u_Saturation") != null) {
                        shader.method_34582("u_Saturation").method_1251(module.saturationValue.getFloatValue());
                     }

                     if (shader.method_34582("u_Resolution") != null) {
                        shader.method_34582("u_Resolution")
                           .method_1255((float)Math.max(1, MC.method_22683().method_4486()), (float)Math.max(1, MC.method_22683().method_4502()));
                     }

                     BufferBuilder buffer = Tessellator.method_1348().method_60827(DrawMode.field_27382, VertexFormats.field_1576);
                     buffer.method_22912(-1.0F, 1.0F, 0.0F).method_39415(-1);
                     buffer.method_22912(-1.0F, -1.0F, 0.0F).method_39415(-1);
                     buffer.method_22912(1.0F, -1.0F, 0.0F).method_39415(-1);
                     buffer.method_22912(1.0F, 1.0F, 0.0F).method_39415(-1);
                     BufferRenderer.method_43433(buffer.method_60800());
                     return;
                  }
               } finally {
                  RenderSystem.setShaderTexture(0, 0);
                  RenderSystem.depthMask(true);
                  RenderSystem.enableDepthTest();
                  GL30.glBindFramebuffer(36008, readFbo);
                  GL30.glBindFramebuffer(36009, drawFbo);
                  RenderSystem.viewport(0, 0, w, h);
               }
            }
         }
      }
   }

   private static void ensureBuffer(int w, int h) {
      if (sourceBuffer == null || width != w || height != h) {
         if (sourceBuffer != null) {
            sourceBuffer.method_1238();
         }

         sourceBuffer = new SimpleFramebuffer(w, h, false);
         sourceBuffer.method_1236(0.0F, 0.0F, 0.0F, 0.0F);
         width = w;
         height = h;
      }
   }
}
