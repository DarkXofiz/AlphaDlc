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
   private static final MinecraftClient mc = MinecraftClient.method_1551();
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
      return new ShaderProgramKey(ResourceProvider.getShaderIdentifier(name), VertexFormats.field_1575, Defines.field_53930);
   }

   public static GlowEspRenderer getInstance() {
      if (instance == null) {
         instance = new GlowEspRenderer();
      }

      return instance;
   }

   public void renderMask(GlowEsp module, MatrixStack worldMatrices, float tickDelta) {
      if (mc.field_1687 != null && mc.field_1724 != null) {
         this.ensureBuffers();
         if (this.maskBuffer != null) {
            this.maskBuffer.method_1236(0.0F, 0.0F, 0.0F, 0.0F);
            this.maskBuffer.method_1230();
            this.maskBuffer.method_1235(false);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);

            for (PlayerEntity player : mc.field_1687.method_18456()) {
               if (this.shouldRender(player)) {
                  this.renderSilhouette(worldMatrices, player, tickDelta);
               }
            }

            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            mc.method_1522().method_1235(true);
            this.hasMask = true;
         }
      }
   }

   public void compositeIfReady(GlowEsp module) {
      if (this.hasMask) {
         this.hasMask = false;
         int iter = (int)module.glowRadius.getFloatValue();
         this.ensureBloomBuffers(iter);
         ShaderProgram downShader = mc.method_62887().method_62947(KEY_KAWASE_DOWN);
         ShaderProgram upShader = mc.method_62887().method_62947(KEY_KAWASE_UP);
         if (downShader != null && upShader != null) {
            int cur = this.maskBuffer.method_30277();

            for (int i = 0; i < iter; i++) {
               Framebuffer b = this.bloomBuffers.get(i);
               b.method_1236(0.0F, 0.0F, 0.0F, 0.0F);
               b.method_1230();
               b.method_1235(true);
               RenderSystem.setShader(KEY_KAWASE_DOWN);
               RenderSystem.setShaderTexture(0, cur);
               this.kawaseUniforms(downShader, b.field_1482, b.field_1481, 1.0F + (float)i);
               this.drawQuad();
               cur = b.method_30277();
            }

            for (int i = iter - 1; i >= 1; i--) {
               Framebuffer b = this.bloomBuffers.get(i - 1);
               b.method_1235(true);
               RenderSystem.setShader(KEY_KAWASE_UP);
               RenderSystem.setShaderTexture(0, cur);
               this.kawaseUniforms(upShader, b.field_1482, b.field_1481, 1.0F + (float)i);
               this.setUniform(upShader, "color", 1.0F, 1.0F, 1.0F);
               this.drawQuad();
               cur = b.method_30277();
            }

            mc.method_1522().method_1235(true);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(770, 1);
            RenderSystem.disableDepthTest();
            ShaderProgram glowShader = mc.method_62887().method_62947(KEY_FILL_GLOW);
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
               RenderSystem.setShaderTexture(1, this.maskBuffer.method_30277());
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
            mc.method_1522().method_1235(true);
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
      ShaderProgram outlineShader = mc.method_62887().method_62947(KEY_FILL_OUTLINE);
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
         int fw = Math.max(1, mc.method_22683().method_4489());
         int fh = Math.max(1, mc.method_22683().method_4506());
         mc.method_1522().method_1235(false);
         RenderSystem.enableBlend();
         RenderSystem.blendFuncSeparate(770, 771, 0, 1);
         RenderSystem.disableDepthTest();
         RenderSystem.setShader(KEY_FILL_OUTLINE);
         RenderSystem.setShaderTexture(0, this.maskBuffer.method_30277());
         if (isAuto && !this.bloomBuffers.isEmpty()) {
            RenderSystem.setShaderTexture(1, this.bloomBuffers.get(0).method_30277());
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
         mc.method_1522().method_1235(true);
      }
   }

   private boolean shouldRender(PlayerEntity player) {
      if (player != null && player.method_5805()) {
         if (player == mc.field_1724) {
            GlowEsp module = GlowEsp.getInstance();
            if (module == null || !module.renderSelf.getValue()) {
               return false;
            }

            if (mc.field_1690.method_31044().method_31034()) {
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
         if (mc.method_1561().method_3953(player) instanceof PlayerEntityRenderer renderer) {
            PlayerEntityRenderState state = renderer.method_62608();
            renderer.method_62604(clientPlayer, state, tickDelta);
            PlayerEntityModel model = (PlayerEntityModel)renderer.method_4038();
            model.method_62110(state);
            matrices.method_22903();
            Vec3d cam = mc.field_1773.method_19418().method_19326();
            Vec3d pos = player.method_30950(tickDelta);
            matrices.method_22904(pos.field_1352 - cam.field_1352, pos.field_1351 - cam.field_1351, pos.field_1350 - cam.field_1350);
            if (state.field_53463 != null) {
               float off = state.field_53331 - 0.1F;
               matrices.method_46416((float)(-state.field_53463.method_10148()) * off, 0.0F, (float)(-state.field_53463.method_10165()) * off);
            }

            float scale = state.field_53453;
            matrices.method_22905(scale, scale, scale);
            LivingEntityRendererAccessor accessor = (LivingEntityRendererAccessor)renderer;
            accessor.alphadlc$setupTransforms(state, matrices, state.field_53446, scale);
            accessor.alphadlc$scale(state, matrices);
            matrices.method_22905(-1.0F, -1.0F, 1.0F);
            matrices.method_46416(0.0F, -1.501F, 0.0F);
            RenderSystem.setShaderTexture(0, clientPlayer.method_52814().comp_1626());
            RenderSystem.setShader(ShaderProgramKeys.field_53880);
            ModelPart root = model.method_63512();
            BufferBuilder buf = Tessellator.method_1348().method_60827(DrawMode.field_27382, VertexFormats.field_1575);
            boolean slim = clientPlayer.method_52814().comp_1629() == Model.field_41122;
            this.fillPartTextured(matrices, buf, root, model.field_3398, -4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 8.0F, 8.0F, 16.0F, 16.0F);
            this.fillPartTextured(matrices, buf, root, model.field_3391, -4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 20.0F, 20.0F, 28.0F, 32.0F);
            if (slim) {
               this.fillPartTextured(matrices, buf, root, model.field_3401, -2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 44.0F, 16.0F, 47.0F, 28.0F);
               this.fillPartTextured(matrices, buf, root, model.field_27433, -1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 36.0F, 52.0F, 39.0F, 64.0F);
            } else {
               this.fillPartTextured(matrices, buf, root, model.field_3401, -3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 44.0F, 16.0F, 48.0F, 28.0F);
               this.fillPartTextured(matrices, buf, root, model.field_27433, -1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 36.0F, 52.0F, 40.0F, 64.0F);
            }

            this.fillPartTextured(matrices, buf, root, model.field_3392, -2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 4.0F, 16.0F, 8.0F, 28.0F);
            this.fillPartTextured(matrices, buf, root, model.field_3397, -2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 20.0F, 48.0F, 24.0F, 60.0F);
            BufferRenderer.method_43433(buf.method_60800());
            matrices.method_22909();
         }
      }
   }

   private int getPlayerColor(PlayerEntity player) {
      int hash = player.method_5667().hashCode();
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
      stack.method_22903();
      root.method_22703(stack);
      part.method_22703(stack);
      float s = 0.0625F;
      float x0 = ox * s;
      float y0 = oy * s;
      float z0 = oz * s;
      float x1 = (ox + w) * s;
      float y1 = (oy + h) * s;
      float z1 = (oz + d) * s;
      Matrix4f m = stack.method_23760().method_23761();
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
      stack.method_22909();
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
      b.method_22918(m, x1, y1, z1).method_22913(u1, v1).method_1336(255, 255, 255, 255);
      b.method_22918(m, x2, y2, z2).method_22913(u1, v2).method_1336(255, 255, 255, 255);
      b.method_22918(m, x3, y3, z3).method_22913(u2, v2).method_1336(255, 255, 255, 255);
      b.method_22918(m, x4, y4, z4).method_22913(u2, v1).method_1336(255, 255, 255, 255);
   }

   private void fillPart(
      MatrixStack stack, BufferBuilder buf, ModelPart root, ModelPart part, float ox, float oy, float oz, float w, float h, float d, float expand, int color
   ) {
      stack.method_22903();
      root.method_22703(stack);
      part.method_22703(stack);
      float s = 0.0625F;
      float e = expand * s;
      float x0 = ox * s - e;
      float y0 = oy * s - e;
      float z0 = oz * s - e;
      float x1 = (ox + w) * s + e;
      float y1 = (oy + h) * s + e;
      float z1 = (oz + d) * s + e;
      Matrix4f m = stack.method_23760().method_23761();
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
      stack.method_22909();
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
      b.method_22918(m, x1, y1, z1).method_1336(r, g, bl, a);
      b.method_22918(m, x2, y2, z2).method_1336(r, g, bl, a);
      b.method_22918(m, x3, y3, z3).method_1336(r, g, bl, a);
      b.method_22918(m, x4, y4, z4).method_1336(r, g, bl, a);
   }

   private void ensureBuffers() {
      int w = mc.method_22683().method_4489();
      int h = mc.method_22683().method_4506();
      if (w != this.width || h != this.height || this.maskBuffer == null) {
         if (this.maskBuffer != null) {
            this.maskBuffer.method_1238();
         }

         this.maskBuffer = new SimpleFramebuffer(w, h, true);
         this.setLinearFilter(this.maskBuffer);
         this.width = w;
         this.height = h;
         this.bloomBuffers.forEach(Framebuffer::method_1238);
         this.bloomBuffers.clear();
      }
   }

   private void ensureBloomBuffers(int n) {
      int fw = mc.method_22683().method_4489();
      int fh = mc.method_22683().method_4506();
      if (this.bloomBuffers.size() != n) {
         this.bloomBuffers.forEach(Framebuffer::method_1238);
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
            if (b.field_1482 != w || b.field_1481 != h) {
               b.method_1238();
               Framebuffer var10 = new SimpleFramebuffer(w, h, false);
               this.setLinearFilter(var10);
               this.bloomBuffers.set(i, var10);
            }
         }
      }
   }

   private void drawQuad() {
      float sw = (float)Math.max(mc.method_22683().method_4486(), 1);
      float sh = (float)Math.max(mc.method_22683().method_4502(), 1);
      BufferBuilder b = Tessellator.method_1348().method_60827(DrawMode.field_27382, VertexFormats.field_1575);
      b.method_22912(0.0F, 0.0F, 0.0F).method_22913(0.0F, 1.0F).method_22915(1.0F, 1.0F, 1.0F, 1.0F);
      b.method_22912(0.0F, sh, 0.0F).method_22913(0.0F, 0.0F).method_22915(1.0F, 1.0F, 1.0F, 1.0F);
      b.method_22912(sw, sh, 0.0F).method_22913(1.0F, 0.0F).method_22915(1.0F, 1.0F, 1.0F, 1.0F);
      b.method_22912(sw, 0.0F, 0.0F).method_22913(1.0F, 1.0F).method_22915(1.0F, 1.0F, 1.0F, 1.0F);
      BufferRenderer.method_43433(b.method_60800());
   }

   private void kawaseUniforms(ShaderProgram s, int w, int h, float off) {
      this.setUniform(s, "uSize", (float)Math.max(1, w), (float)Math.max(1, h));
      this.setUniform(s, "uOffset", off, off);
      this.setUniform(s, "uHalfPixel", 0.5F / (float)Math.max(1, w), 0.5F / (float)Math.max(1, h));
   }

   private void setLinearFilter(Framebuffer fb) {
      RenderSystem.bindTexture(fb.method_30277());
      GL11.glTexParameteri(3553, 10241, 9729);
      GL11.glTexParameteri(3553, 10240, 9729);
      RenderSystem.bindTexture(0);
   }

   private void setUniform(ShaderProgram s, String n, float v) {
      GlUniform u = s.method_34582(n);
      if (u != null) {
         u.method_1251(v);
      }
   }

   private void setUniform(ShaderProgram s, String n, float x, float y) {
      GlUniform u = s.method_34582(n);
      if (u != null) {
         u.method_1255(x, y);
      }
   }

   private void setUniform(ShaderProgram s, String n, float x, float y, float z) {
      GlUniform u = s.method_34582(n);
      if (u != null) {
         u.method_1249(x, y, z);
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
