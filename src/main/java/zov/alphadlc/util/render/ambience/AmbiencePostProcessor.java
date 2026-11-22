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
   private static final MinecraftClient MC = MinecraftClient.getInstance();
   private static final ShaderProgramKey SATURATION_KEY = new ShaderProgramKey(
      ResourceProvider.getShaderIdentifier("saturation"), VertexFormats.POSITION_COLOR, Defines.EMPTY
   );
   private static Framebuffer sourceBuffer;
   private static int width = -1;
   private static int height = -1;

   private AmbiencePostProcessor() {
   }

   public static void apply(Ambience module) {
      if (module != null && module.isEnabled() && module.worldSaturation.getValue()) {
         if (MC.world != null && MC.getFramebuffer() != null) {
            int w = Math.max(1, MC.getWindow().getFramebufferWidth());
            int h = Math.max(1, MC.getWindow().getFramebufferHeight());
            ensureBuffer(w, h);
            if (sourceBuffer != null) {
               int readFbo = GL11.glGetInteger(36010);
               int drawFbo = GL11.glGetInteger(36006);
               int sourceFbo = readFbo != 0 ? readFbo : drawFbo;
               GL30.glBindFramebuffer(36008, sourceFbo);
               GL30.glBindFramebuffer(36009, sourceBuffer.fbo);
               GL30.glBlitFramebuffer(0, 0, w, h, 0, 0, w, h, 16384, 9728);
               GL30.glBindFramebuffer(36008, readFbo);
               GL30.glBindFramebuffer(36009, drawFbo);

               try {
                  GL30.glBindFramebuffer(36160, drawFbo);
                  RenderSystem.viewport(0, 0, w, h);
                  RenderSystem.disableDepthTest();
                  RenderSystem.depthMask(false);
                  RenderSystem.disableBlend();
                  RenderSystem.setShaderTexture(0, sourceBuffer.getColorAttachment());
                  ShaderProgram shader = RenderSystem.setShader(SATURATION_KEY);
                  if (shader != null) {
                     if (shader.getUniform("u_Saturation") != null) {
                        shader.getUniform("u_Saturation").set(module.saturationValue.getFloatValue());
                     }

                     if (shader.getUniform("u_Resolution") != null) {
                        shader.getUniform("u_Resolution")
                           .set((float)Math.max(1, MC.getWindow().getScaledWidth()), (float)Math.max(1, MC.getWindow().getScaledHeight()));
                     }

                     BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
                     buffer.vertex(-1.0F, 1.0F, 0.0F).color(-1);
                     buffer.vertex(-1.0F, -1.0F, 0.0F).color(-1);
                     buffer.vertex(1.0F, -1.0F, 0.0F).color(-1);
                     buffer.vertex(1.0F, 1.0F, 0.0F).color(-1);
                     BufferRenderer.drawWithGlobalProgram(buffer.end());
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
            sourceBuffer.delete();
         }

         sourceBuffer = new SimpleFramebuffer(w, h, false);
         sourceBuffer.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
         width = w;
         height = h;
      }
   }
}
