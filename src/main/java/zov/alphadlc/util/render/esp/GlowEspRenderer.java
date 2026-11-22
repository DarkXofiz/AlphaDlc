package zov.alphadlc.util.render.esp;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.SkinTextures.Model;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import zov.alphadlc.mixin.LivingEntityRendererAccessor;
import zov.alphadlc.module.list.render.GlowEsp;
import zov.alphadlc.util.render.providers.ColorProvider;
import zov.alphadlc.util.render.providers.ResourceProvider;

public class GlowEspRenderer {
   private static GlowEspRenderer instance;
   private static final MinecraftClient mc = MinecraftClient.getInstance();
   private static final ShaderProgramKey KEY_KAWASE_DOWN = key("glass_hands_kawase_down");
   private static final ShaderProgramKey KEY_KAWASE_UP = key("glass_hands_kawase_up");
   private static final ShaderProgramKey KEY_FILL_GLOW = key("glass_hands_fill_glow");
   private static final ShaderProgramKey KEY_FILL_OUTLINE = key("glass_hands_fill_outline");
   private Framebuffer maskBuffer;
   private final List<Framebuffer> bloomBuffers = new ArrayList<>();
   private int width = -1;
   private int height = -1;
   private boolean hasMask = false;

   private static ShaderProgramKey key(String name) {
      return new ShaderProgramKey(ResourceProvider.getShaderIdentifier(name), VertexFormats.POSITION_TEXTURE_COLOR, Defines.EMPTY);
   }

   public static GlowEspRenderer getInstance() {
      if (instance == null) {
         instance = new GlowEspRenderer();
      }

      return instance;
   }

   public void renderMask(GlowEsp module, MatrixStack worldMatrices, float tickDelta) {
      if (mc.world != null && mc.player != null) {
         this.ensureBuffers();
         if (this.maskBuffer != null) {
            this.maskBuffer.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            this.maskBuffer.clear();
            this.maskBuffer.beginWrite(false);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);

            for (PlayerEntity player : mc.world.getPlayers()) {
               if (this.shouldRender(player)) {
                  this.renderSilhouette(worldMatrices, player, tickDelta);
               }
            }

            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            mc.getFramebuffer().beginWrite(true);
            this.hasMask = true;
         }
      }
   }

   public void compositeIfReady(GlowEsp module) {
      if (this.hasMask) {
         this.hasMask = false;
         int iter = (int)module.glowRadius.getFloatValue();
         this.ensureBloomBuffers(iter);
         ShaderProgram downShader = mc.getShaderLoader().getOrCreateProgram(KEY_KAWASE_DOWN);
         ShaderProgram upShader = mc.getShaderLoader().getOrCreateProgram(KEY_KAWASE_UP);
         if (downShader != null && upShader != null) {
            int cur = this.maskBuffer.getColorAttachment();

            for (int i = 0; i < iter; i++) {
               Framebuffer b = this.bloomBuffers.get(i);
               b.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
               b.clear();
               b.beginWrite(true);
               RenderSystem.setShader(KEY_KAWASE_DOWN);
               RenderSystem.setShaderTexture(0, cur);
               this.kawaseUniforms(downShader, b.textureWidth, b.textureHeight, 1.0F + (float)i);
               this.drawQuad();
               cur = b.getColorAttachment();
            }

            for (int i = iter - 1; i >= 1; i--) {
               Framebuffer b = this.bloomBuffers.get(i - 1);
               b.beginWrite(true);
               RenderSystem.setShader(KEY_KAWASE_UP);
               RenderSystem.setShaderTexture(0, cur);
               this.kawaseUniforms(upShader, b.textureWidth, b.textureHeight, 1.0F + (float)i);
               this.setUniform(upShader, "color", 1.0F, 1.0F, 1.0F);
               this.drawQuad();
               cur = b.getColorAttachment();
            }

            mc.getFramebuffer().beginWrite(true);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(770, 1);
            RenderSystem.disableDepthTest();
            ShaderProgram glowShader = mc.getShaderLoader().getOrCreateProgram(KEY_FILL_GLOW);
            if (glowShader != null) {
               boolean isAuto = module.autoColor.getValue();
               int gc1;
               int gc2;
               if (!isAuto && module.rainbow.getValue()) {
                  float speed = module.rainbowSpeed.getFloatValue();
                  float hue = (float)(System.currentTimeMillis() % (long)(10000.0 / (double)speed)) / (10000.0F / speed);
                  gc1 = Color.HSBtoRGB(hue, 1.0F, 1.0F) & 16777215;
                  gc2 = gc1;
               } else {
                  gc1 = module.glowColor1.getValue();
                  gc2 = module.glowColor2.getValue();
               }

               RenderSystem.setShader(KEY_FILL_GLOW);
               RenderSystem.setShaderTexture(0, cur);
               RenderSystem.setShaderTexture(1, this.maskBuffer.getColorAttachment());
               this.setUniform(glowShader, "glowColor1", this.rf(gc1), this.gf(gc1), this.bf(gc1));
               this.setUniform(glowShader, "glowColor2", this.rf(gc2), this.gf(gc2), this.bf(gc2));
               this.setUniform(glowShader, "exposure", module.glowExposure.getFloatValue());
               this.setUniform(glowShader, "autoColor", isAuto ? 1.0F : 0.0F);
               this.setUniform(glowShader, "saturation", module.saturation.getFloatValue());
               this.drawQuad();
            }

            RenderSystem.enableDepthTest();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            mc.getFramebuffer().beginWrite(true);
            if (module.outlineEnabled.getValue()) {
               this.renderOutline(module);
            }
         }
      }
   }

   public void invalidate() {
      this.hasMask = false;
   }

   private void renderOutline(GlowEsp module) {
      ShaderProgram outlineShader = mc.getShaderLoader().getOrCreateProgram(KEY_FILL_OUTLINE);
      if (outlineShader != null) {
         boolean isAuto = module.autoColor.getValue();
         boolean isRainbow = !isAuto && module.rainbow.getValue();
         int oc;
         if (isRainbow) {
            float speed = module.rainbowSpeed.getFloatValue();
            float hue = (float)(System.currentTimeMillis() % (long)(10000.0 / (double)speed)) / (10000.0F / speed);
            oc = Color.HSBtoRGB(hue, 1.0F, 1.0F) | 0xFF000000;
         } else {
            oc = module.outlineColor.getValue();
         }

         Color col = new Color(oc, true);
         float a = (float)col.getAlpha() / 255.0F;
         int fw = Math.max(1, mc.getWindow().getFramebufferWidth());
         int fh = Math.max(1, mc.getWindow().getFramebufferHeight());
         mc.getFramebuffer().beginWrite(false);
         RenderSystem.enableBlend();
         RenderSystem.blendFuncSeparate(770, 771, 0, 1);
         RenderSystem.disableDepthTest();
         RenderSystem.setShader(KEY_FILL_OUTLINE);
         RenderSystem.setShaderTexture(0, this.maskBuffer.getColorAttachment());
         if (isAuto && !this.bloomBuffers.isEmpty()) {
            RenderSystem.setShaderTexture(1, this.bloomBuffers.get(0).getColorAttachment());
         }

         this.setUniform(outlineShader, "colorMode", isAuto ? 2.0F : 0.0F);
         this.setUniform(outlineShader, "width", module.outlineWidth.getFloatValue());
         this.setUniform(outlineShader, "texelSize", 1.0F / (float)fw, 1.0F / (float)fh);
         this.setUniform(outlineShader, "alpha", !isAuto && !isRainbow ? a : 1.0F);
         this.setUniform(outlineShader, "saturation", module.saturation.getFloatValue());
         this.setUniform(outlineShader, "solidColor", (float)col.getRed() / 255.0F, (float)col.getGreen() / 255.0F, (float)col.getBlue() / 255.0F);
         this.drawQuad();
         RenderSystem.defaultBlendFunc();
         RenderSystem.enableDepthTest();
         RenderSystem.disableBlend();
         mc.getFramebuffer().beginWrite(true);
      }
   }

   private boolean shouldRender(PlayerEntity player) {
      if (player != null && player.isAlive()) {
         if (player == mc.player) {
            GlowEsp module = GlowEsp.getInstance();
            if (module == null || !module.renderSelf.getValue()) {
               return false;
            }

            if (mc.options.getPerspective().isFirstPerson()) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private void renderSilhouette(MatrixStack matrices, PlayerEntity player, float tickDelta) {
      if (player instanceof AbstractClientPlayerEntity clientPlayer) {
         if (mc.getEntityRenderDispatcher().getRenderer(player) instanceof PlayerEntityRenderer renderer) {
            PlayerEntityRenderState state = renderer.createRenderState();
            renderer.updateRenderState(clientPlayer, state, tickDelta);
            PlayerEntityModel model = (PlayerEntityModel)renderer.getModel();
            model.setAngles(state);
            matrices.push();
            Vec3d cam = mc.gameRenderer.getCamera().getPos();
            Vec3d pos = player.getLerpedPos(tickDelta);
            matrices.translate(pos.x - cam.x, pos.y - cam.y, pos.z - cam.z);
            if (state.sleepingDirection != null) {
               float off = state.standingEyeHeight - 0.1F;
               matrices.translate((float)(-state.sleepingDirection.getOffsetX()) * off, 0.0F, (float)(-state.sleepingDirection.getOffsetZ()) * off);
            }

            float scale = state.baseScale;
            matrices.scale(scale, scale, scale);
            LivingEntityRendererAccessor accessor = (LivingEntityRendererAccessor)renderer;
            accessor.alphadlc$setupTransforms(state, matrices, state.bodyYaw, scale);
            accessor.alphadlc$scale(state, matrices);
            matrices.scale(-1.0F, -1.0F, 1.0F);
            matrices.translate(0.0F, -1.501F, 0.0F);
            RenderSystem.setShaderTexture(0, clientPlayer.getSkinTextures().comp_1626());
            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
            ModelPart root = model.getRootPart();
            BufferBuilder buf = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            boolean slim = clientPlayer.getSkinTextures().comp_1629() == Model.SLIM;
            this.fillPartTextured(matrices, buf, root, model.head, -4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 8.0F, 8.0F, 16.0F, 16.0F);
            this.fillPartTextured(matrices, buf, root, model.body, -4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 20.0F, 20.0F, 28.0F, 32.0F);
            if (slim) {
               this.fillPartTextured(matrices, buf, root, model.rightArm, -2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 44.0F, 16.0F, 47.0F, 28.0F);
               this.fillPartTextured(matrices, buf, root, model.leftArm, -1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 36.0F, 52.0F, 39.0F, 64.0F);
            } else {
               this.fillPartTextured(matrices, buf, root, model.rightArm, -3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 44.0F, 16.0F, 48.0F, 28.0F);
               this.fillPartTextured(matrices, buf, root, model.leftArm, -1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 36.0F, 52.0F, 40.0F, 64.0F);
            }

            this.fillPartTextured(matrices, buf, root, model.rightLeg, -2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 4.0F, 16.0F, 8.0F, 28.0F);
            this.fillPartTextured(matrices, buf, root, model.leftLeg, -2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 20.0F, 48.0F, 24.0F, 60.0F);
            BufferRenderer.drawWithGlobalProgram(buf.end());
            matrices.pop();
         }
      }
   }

   private int getPlayerColor(PlayerEntity player) {
      int hash = player.getUuid().hashCode();
      float hue = (float)(hash & 65535) / 65535.0F;
      Color color = Color.getHSBColor(hue, 0.7F, 0.9F);
      return ColorProvider.rgba(color.getRed(), color.getGreen(), color.getBlue(), 255.0F);
   }

   private void fillPartTextured(
      MatrixStack stack,
      BufferBuilder buf,
      ModelPart root,
      ModelPart part,
      float ox,
      float oy,
      float oz,
      float w,
      float h,
      float d,
      float u1,
      float v1,
      float u2,
      float v2
   ) {
      stack.push();
      root.rotate(stack);
      part.rotate(stack);
      float s = 0.0625F;
      float x0 = ox * s;
      float y0 = oy * s;
      float z0 = oz * s;
      float x1 = (ox + w) * s;
      float y1 = (oy + h) * s;
      float z1 = (oz + d) * s;
      Matrix4f m = stack.peek().getPositionMatrix();
      float tu1 = u1 / 64.0F;
      float tv1 = v1 / 64.0F;
      float tu2 = u2 / 64.0F;
      float tv2 = v2 / 64.0F;
      this.quadTex(buf, m, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0, tu1, tv1, tu2, tv2);
      this.quadTex(buf, m, x0, y0, z1, x0, y0, z0, x1, y0, z0, x1, y0, z1, tu1, tv1, tu2, tv2);
      this.quadTex(buf, m, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0, tu1, tv1, tu2, tv2);
      this.quadTex(buf, m, x1, y0, z1, x1, y1, z1, x0, y1, z1, x0, y0, z1, tu1, tv1, tu2, tv2);
      this.quadTex(buf, m, x0, y0, z1, x0, y1, z1, x0, y1, z0, x0, y0, z0, tu1, tv1, tu2, tv2);
      this.quadTex(buf, m, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1, tu1, tv1, tu2, tv2);
      stack.pop();
   }

   private void quadTex(
      BufferBuilder b,
      Matrix4f m,
      float x1,
      float y1,
      float z1,
      float x2,
      float y2,
      float z2,
      float x3,
      float y3,
      float z3,
      float x4,
      float y4,
      float z4,
      float u1,
      float v1,
      float u2,
      float v2
   ) {
      b.vertex(m, x1, y1, z1).texture(u1, v1).color(255, 255, 255, 255);
      b.vertex(m, x2, y2, z2).texture(u1, v2).color(255, 255, 255, 255);
      b.vertex(m, x3, y3, z3).texture(u2, v2).color(255, 255, 255, 255);
      b.vertex(m, x4, y4, z4).texture(u2, v1).color(255, 255, 255, 255);
   }

   private void fillPart(
      MatrixStack stack, BufferBuilder buf, ModelPart root, ModelPart part, float ox, float oy, float oz, float w, float h, float d, float expand, int color
   ) {
      stack.push();
      root.rotate(stack);
      part.rotate(stack);
      float s = 0.0625F;
      float e = expand * s;
      float x0 = ox * s - e;
      float y0 = oy * s - e;
      float z0 = oz * s - e;
      float x1 = (ox + w) * s + e;
      float y1 = (oy + h) * s + e;
      float z1 = (oz + d) * s + e;
      Matrix4f m = stack.peek().getPositionMatrix();
      int r = ColorProvider.red(color);
      int g = ColorProvider.green(color);
      int b = ColorProvider.blue(color);
      int a = ColorProvider.alpha(color);
      this.quad(buf, m, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0, r, g, b, a);
      this.quad(buf, m, x0, y0, z1, x0, y0, z0, x1, y0, z0, x1, y0, z1, r, g, b, a);
      this.quad(buf, m, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0, r, g, b, a);
      this.quad(buf, m, x1, y0, z1, x1, y1, z1, x0, y1, z1, x0, y0, z1, r, g, b, a);
      this.quad(buf, m, x0, y0, z1, x0, y1, z1, x0, y1, z0, x0, y0, z0, r, g, b, a);
      this.quad(buf, m, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1, r, g, b, a);
      stack.pop();
   }

   private void quad(
      BufferBuilder b,
      Matrix4f m,
      float x1,
      float y1,
      float z1,
      float x2,
      float y2,
      float z2,
      float x3,
      float y3,
      float z3,
      float x4,
      float y4,
      float z4,
      int r,
      int g,
      int bl,
      int a
   ) {
      b.vertex(m, x1, y1, z1).color(r, g, bl, a);
      b.vertex(m, x2, y2, z2).color(r, g, bl, a);
      b.vertex(m, x3, y3, z3).color(r, g, bl, a);
      b.vertex(m, x4, y4, z4).color(r, g, bl, a);
   }

   private void ensureBuffers() {
      int w = mc.getWindow().getFramebufferWidth();
      int h = mc.getWindow().getFramebufferHeight();
      if (w != this.width || h != this.height || this.maskBuffer == null) {
         if (this.maskBuffer != null) {
            this.maskBuffer.delete();
         }

         this.maskBuffer = new SimpleFramebuffer(w, h, true);
         this.setLinearFilter(this.maskBuffer);
         this.width = w;
         this.height = h;
         this.bloomBuffers.forEach(Framebuffer::delete);
         this.bloomBuffers.clear();
      }
   }

   private void ensureBloomBuffers(int n) {
      int fw = mc.getWindow().getFramebufferWidth();
      int fh = mc.getWindow().getFramebufferHeight();
      if (this.bloomBuffers.size() != n) {
         this.bloomBuffers.forEach(Framebuffer::delete);
         this.bloomBuffers.clear();

         for (int i = 0; i < n; i++) {
            Framebuffer f = new SimpleFramebuffer(Math.max(2, fw >> i + 1), Math.max(2, fh >> i + 1), false);
            this.setLinearFilter(f);
            this.bloomBuffers.add(f);
         }
      } else {
         for (int i = 0; i < n; i++) {
            int w = Math.max(2, fw >> i + 1);
            int h = Math.max(2, fh >> i + 1);
            Framebuffer b = this.bloomBuffers.get(i);
            if (b.textureWidth != w || b.textureHeight != h) {
               b.delete();
               Framebuffer var10 = new SimpleFramebuffer(w, h, false);
               this.setLinearFilter(var10);
               this.bloomBuffers.set(i, var10);
            }
         }
      }
   }

   private void drawQuad() {
      float sw = (float)Math.max(mc.getWindow().getScaledWidth(), 1);
      float sh = (float)Math.max(mc.getWindow().getScaledHeight(), 1);
      BufferBuilder b = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      b.vertex(0.0F, 0.0F, 0.0F).texture(0.0F, 1.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
      b.vertex(0.0F, sh, 0.0F).texture(0.0F, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
      b.vertex(sw, sh, 0.0F).texture(1.0F, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
      b.vertex(sw, 0.0F, 0.0F).texture(1.0F, 1.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
      BufferRenderer.drawWithGlobalProgram(b.end());
   }

   private void kawaseUniforms(ShaderProgram s, int w, int h, float off) {
      this.setUniform(s, "uSize", (float)Math.max(1, w), (float)Math.max(1, h));
      this.setUniform(s, "uOffset", off, off);
      this.setUniform(s, "uHalfPixel", 0.5F / (float)Math.max(1, w), 0.5F / (float)Math.max(1, h));
   }

   private void setLinearFilter(Framebuffer fb) {
      RenderSystem.bindTexture(fb.getColorAttachment());
      GL11.glTexParameteri(3553, 10241, 9729);
      GL11.glTexParameteri(3553, 10240, 9729);
      RenderSystem.bindTexture(0);
   }

   private void setUniform(ShaderProgram s, String n, float v) {
      GlUniform u = s.getUniform(n);
      if (u != null) {
         u.set(v);
      }
   }

   private void setUniform(ShaderProgram s, String n, float x, float y) {
      GlUniform u = s.getUniform(n);
      if (u != null) {
         u.set(x, y);
      }
   }

   private void setUniform(ShaderProgram s, String n, float x, float y, float z) {
      GlUniform u = s.getUniform(n);
      if (u != null) {
         u.set(x, y, z);
      }
   }

   private float rf(int c) {
      return (float)(c >> 16 & 0xFF) / 255.0F;
   }

   private float gf(int c) {
      return (float)(c >> 8 & 0xFF) / 255.0F;
   }

   private float bf(int c) {
      return (float)(c & 0xFF) / 255.0F;
   }
}
