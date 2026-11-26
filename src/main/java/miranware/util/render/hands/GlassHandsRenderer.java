package miranware.util.render.hands;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
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
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import miranware.module.list.render.GlassHands;
import miranware.util.render.providers.ColorProvider;
import miranware.util.render.providers.ResourceProvider;

public class GlassHandsRenderer {
   private static final float EPSILON = 0.001F;
   private static GlassHandsRenderer instance;
   private static final MinecraftClient mc = MinecraftClient.getInstance();
   private static final ShaderProgramKey KEY_MASK_DIFF = key("glass_hands_mask_diff");
   private static final ShaderProgramKey KEY_GLOW = key("glass_hands_glow");
   private static final ShaderProgramKey KEY_OVERLAY = key("glass_hands_overlay");
   private static final ShaderProgramKey KEY_KAWASE_DOWN = key("glass_hands_kawase_down");
   private static final ShaderProgramKey KEY_KAWASE_UP = key("glass_hands_kawase_up");
   private static final ShaderProgramKey KEY_PRETTY = key("glass_hands_pretty");
   private static final ShaderProgramKey KEY_BLUR_HANDS = key("glass_hands_blur");
   private static final ShaderProgramKey KEY_TRAIL = key("glass_hands_trail");
   private static final ShaderProgramKey KEY_TRAIL_MOTION = key("glass_hands_trail_motion");
   private static final ShaderProgramKey KEY_TRAIL_ACCUMULATE = key("glass_hands_trail_accumulate");
   private static final ShaderProgramKey KEY_TRAIL_COMPOSITE = key("glass_hands_trail_composite");
   private static final ShaderProgramKey KEY_GLASS = key("glass_hands_glass");
   private static final ShaderProgramKey KEY_GLASS_GLOW = key("glass_hands_glass_glow");
   private static final ShaderProgramKey KEY_PLASMA = key("glass_hands_plasma");
   private static final ShaderProgramKey KEY_FILL = key("glass_hands_fill");
   private static final ShaderProgramKey KEY_FILL_GLOW = key("glass_hands_fill_glow");
   private static final ShaderProgramKey KEY_FILL_OUTLINE = key("glass_hands_fill_outline");
   private static final ShaderProgramKey KEY_FILL_TRAIL_FADE = key("glass_hands_fill_trail_fade");
   private static final ShaderProgramKey KEY_FILL_TRAIL_COLOR = key("glass_hands_fill_trail_color");
   private static final ShaderProgramKey KEY_WAVE = key("glass_hands_wave");
   private Framebuffer beforeBuffer;
   private Framebuffer afterBuffer;
   private Framebuffer maskBuffer;
   private Framebuffer trailBuffer;
   private Framebuffer fillTrailRead;
   private Framebuffer fillTrailWrite;
   private Framebuffer colorMask;
   private final List<Framebuffer> bloomBuffers = new ArrayList<>();
   private final List<Framebuffer> glassBloomBuffers = new ArrayList<>();
   private final List<Framebuffer> fillBloomBuffers = new ArrayList<>();
   private int width = -1;
   private int height = -1;
   private boolean hasBeforeCapture = false;
   private boolean pendingComposite = false;
   private int configuredBeforeDepthTex = -1;
   private int configuredAfterDepthTex = -1;
   private float prevYaw = 0.0F;
   private float prevPitch = 0.0F;
   private float smoothDeltaYaw = 0.0F;
   private float smoothDeltaPitch = 0.0F;
   private float smoothTime = 0.0F;
   private float slashPulse = 0.0F;
   private float slashDirection = 1.0F;
   private long fillLastTrailTime = 0L;
   private float fillSmoothDt = 0.016666668F;
   private float fillSmoothTrailRise;
   private float fillSmoothTrailSway;
   private float fillSmoothBurst;
   private long fillLastSwingMs = -10000L;
   private boolean fillWasSwinging = false;
   private int fillLastBloomTex = -1;
   private Framebuffer waveColorBuf;
   private Framebuffer waveTrailRead;
   private Framebuffer waveTrailWrite;
   private long waveLastTrailTime = 0L;
   private float waveSmoothDt = 0.016666668F;
   private float waveSmoothTrailRise;
   private float waveSmoothTrailSway;
   private float waveSmoothBurst;
   private long waveLastSwingMs = -10000L;
   private boolean waveWasSwinging = false;
   private String previousMode = "";

   private static ShaderProgramKey key(String name) {
      return new ShaderProgramKey(ResourceProvider.getShaderIdentifier(name), VertexFormats.POSITION_TEXTURE_COLOR, Defines.EMPTY);
   }

   public static GlassHandsRenderer getInstance() {
      if (instance == null) {
         instance = new GlassHandsRenderer();
      }

      return instance;
   }

   public void captureBeforeHands(GlassHands module) {
      if (!this.isEffectEnabled(module)) {
         this.invalidateState();
      } else {
         this.ensureBuffers();
         if (this.beforeBuffer != null) {
            int currentDrawFbo = GL11.glGetInteger(36006);
            this.copyMainFramebuffer(this.beforeBuffer);
            this.hasBeforeCapture = true;
         }
      }
   }

   public void captureAfterHands(GlassHands module) {
      if (!this.isEffectEnabled(module)) {
         this.invalidateState();
      } else {
         this.ensureBuffers();
         if (this.beforeBuffer != null && this.afterBuffer != null && this.maskBuffer != null) {
            if (this.hasBeforeCapture) {
               this.copyMainFramebuffer(this.afterBuffer);
               this.pendingComposite = true;
            }
         }
      }
   }

   public void renderOverlayIfPending(GlassHands module) {
      if (this.pendingComposite) {
         this.ensureBuffers();
         if (this.beforeBuffer != null && this.afterBuffer != null && this.maskBuffer != null) {
            if (!this.isEffectEnabled(module)) {
               this.invalidateState();
            } else {
               ShaderProgram maskShader = mc.getShaderLoader().getOrCreateProgram(KEY_MASK_DIFF);
               if (maskShader == null) {
                  this.invalidateState();
               } else {
                  this.maskBuffer.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
                  this.maskBuffer.clear();
                  this.maskBuffer.beginWrite(false);
                  RenderSystem.disableDepthTest();
                  RenderSystem.disableBlend();
                  RenderSystem.setShader(KEY_MASK_DIFF);
                  RenderSystem.setShaderTexture(0, this.beforeBuffer.getColorAttachment());
                  RenderSystem.setShaderTexture(1, this.afterBuffer.getColorAttachment());
                  int beforeDepth = this.beforeBuffer.getDepthAttachment();
                  int afterDepth = this.afterBuffer.getDepthAttachment();
                  if (beforeDepth != 0 && beforeDepth != this.configuredBeforeDepthTex) {
                     this.configureDepthTexture(beforeDepth);
                     this.configuredBeforeDepthTex = beforeDepth;
                  }

                  if (afterDepth != 0 && afterDepth != this.configuredAfterDepthTex) {
                     this.configureDepthTexture(afterDepth);
                     this.configuredAfterDepthTex = afterDepth;
                  }

                  RenderSystem.setShaderTexture(2, beforeDepth);
                  RenderSystem.setShaderTexture(3, afterDepth);
                  this.drawFullscreenQuad();
                  RenderSystem.enableDepthTest();
                  float glowValue = module.glow.getFloatValue();
                  float fillValue = module.fill.getFloatValue();
                  float alphaValue = module.alpha.getFloatValue();
                  float outlineValue = module.outline.getFloatValue();
                  int color1 = ColorProvider.getThemeColor();
                  int color2 = ColorProvider.getThemeColorTwo();
                  String currentMode = module.mode.getValue();
                  if (!currentMode.equals(this.previousMode)) {
                     this.clearTrailBuffer(this.waveTrailRead);
                     this.clearTrailBuffer(this.waveTrailWrite);
                     this.clearTrailBuffer(this.fillTrailRead);
                     this.clearTrailBuffer(this.fillTrailWrite);
                     this.waveLastTrailTime = 0L;
                     this.fillLastTrailTime = 0L;
                     this.previousMode = currentMode;
                  }

                  if (module.mode.is("Blur")) {
                     this.renderBlurMode(module);
                     this.invalidateState();
                  } else if (module.mode.is("Pretty")) {
                     this.renderPrettyMode(module, color1, color2, glowValue, fillValue, alphaValue, outlineValue);
                     this.invalidateState();
                  } else if (module.mode.is("Outline")) {
                     this.renderOutlineMode(module, color1, color2);
                     this.invalidateState();
                  } else if (module.mode.is("Trail")) {
                     this.renderTrailMode(module, color1, color2);
                     this.invalidateState();
                  } else if (module.mode.is("Trail")) {
                     this.renderTrailMotionMode(module, color1, color2);
                     this.invalidateState();
                  } else if (module.mode.is("Glass")) {
                     this.renderGlassMode(module, color1, color2);
                     this.invalidateState();
                  } else if (module.mode.is("Plasma")) {
                     this.renderPlasmaMode(module, color1, color2);
                     this.invalidateState();
                  } else if (module.mode.is("Fill")) {
                     this.renderFillMode(module, color1, color2);
                     this.invalidateState();
                  } else if (module.mode.is("Wave")) {
                     this.renderWaveMode(module, color1, color2);
                     this.invalidateState();
                  } else {
                     boolean hasGlow = glowValue > 0.001F;
                     boolean hasFill = fillValue > 0.001F && alphaValue > 0.001F;
                     int blurredMaskTex = 0;
                     if (hasGlow) {
                        int iterations = Math.max(3, Math.min(8, 4 + Math.round(outlineValue * 0.7F)));
                        blurredMaskTex = this.runKawaseBloom(iterations);
                     }

                     mc.getFramebuffer().beginWrite(true);
                     RenderSystem.enableBlend();
                     RenderSystem.colorMask(true, true, true, false);
                     RenderSystem.disableDepthTest();
                     if (hasGlow) {
                        ShaderProgram glowShader = mc.getShaderLoader().getOrCreateProgram(KEY_GLOW);
                        if (glowShader != null) {
                           RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE, SrcFactor.ZERO, DstFactor.ONE);
                           RenderSystem.setShader(KEY_GLOW);
                           RenderSystem.setShaderTexture(0, blurredMaskTex);
                           RenderSystem.setShaderTexture(1, this.maskBuffer.getColorAttachment());
                           int effectiveColor1 = color1;
                           int effectiveColor2 = color2;
                           if (module.mode.is("Glow") && module.glowItemColor.getValue()) {
                              int itemColor = this.getHeldItemColor();
                              effectiveColor1 = itemColor;
                              effectiveColor2 = itemColor;
                           }

                           this.setUniform(glowShader, "color", this.rf(effectiveColor1), this.gf(effectiveColor1), this.bf(effectiveColor1));
                           this.setUniform(glowShader, "color2", this.rf(effectiveColor2), this.gf(effectiveColor2), this.bf(effectiveColor2));
                           this.setUniform(glowShader, "exposure", 1.0F + glowValue * 1.8F);
                           this.drawFullscreenQuad();
                        }
                     }

                     if (hasFill) {
                        ShaderProgram overlayShader = mc.getShaderLoader().getOrCreateProgram(KEY_OVERLAY);
                        if (overlayShader != null) {
                           RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA, SrcFactor.ZERO, DstFactor.ONE);
                           RenderSystem.setShader(KEY_OVERLAY);
                           RenderSystem.setShaderTexture(0, this.maskBuffer.getColorAttachment());
                           int effectiveColor = color1;
                           if (module.mode.is("Glow") && module.glowItemColor.getValue()) {
                              effectiveColor = this.getHeldItemColor();
                           }

                           this.setUniform(overlayShader, "color", this.rf(effectiveColor), this.gf(effectiveColor), this.bf(effectiveColor));
                           this.setUniform(overlayShader, "fill", fillValue);
                           this.setUniform(overlayShader, "alpha", alphaValue);
                           this.drawFullscreenQuad();
                        }
                     }

                     this.restoreCompositeState();
                     this.invalidateState();
                  }
               }
            }
         }
      }
   }

   public void invalidateState() {
      this.hasBeforeCapture = false;
      this.pendingComposite = false;
      this.configuredBeforeDepthTex = -1;
      this.configuredAfterDepthTex = -1;
      this.fillLastBloomTex = -1;
   }

   private void renderBlurMode(GlassHands module) {
      int iterations = Math.max(1, Math.min(8, (int)module.blurStrength.getFloatValue()));
      int blurredBgTex = this.runKawaseBloomFrom(this.beforeBuffer.getColorAttachment(), iterations);
      int color1 = module.blurRainbow.getValue() ? this.rainbowColors(module.blurRainbowSpeed.getFloatValue())[0] : ColorProvider.getThemeColor();
      float tint = module.blurTint.getFloatValue();
      mc.getFramebuffer().beginWrite(false);
      RenderSystem.disableDepthTest();
      RenderSystem.enableBlend();
      ShaderProgram sh = mc.getShaderLoader().getOrCreateProgram(KEY_BLUR_HANDS);
      if (sh != null) {
         RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA, SrcFactor.ZERO, DstFactor.ONE);
         RenderSystem.setShader(KEY_BLUR_HANDS);
         RenderSystem.setShaderTexture(0, this.beforeBuffer.getColorAttachment());
         RenderSystem.setShaderTexture(1, this.maskBuffer.getColorAttachment());
         this.setUniform(sh, "tintColor", this.rf(color1), this.gf(color1), this.bf(color1));
         this.setUniform(sh, "tintStrength", 0.0F);
         this.drawFullscreenQuad();
         RenderSystem.setShader(KEY_BLUR_HANDS);
         RenderSystem.setShaderTexture(0, blurredBgTex);
         RenderSystem.setShaderTexture(1, this.maskBuffer.getColorAttachment());
         this.setUniform(sh, "tintColor", this.rf(color1), this.gf(color1), this.bf(color1));
         this.setUniform(sh, "tintStrength", tint);
         this.drawFullscreenQuad();
      }

      this.restoreCompositeState();
   }

   private void renderOutlineMode(GlassHands module, int color1, int color2) {
      mc.getFramebuffer().beginWrite(true);
      RenderSystem.enableBlend();
      RenderSystem.colorMask(true, true, true, false);
      RenderSystem.disableDepthTest();
      ShaderProgram glowShader = mc.getShaderLoader().getOrCreateProgram(KEY_GLOW);
      if (glowShader != null) {
         RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE, SrcFactor.ZERO, DstFactor.ONE);
         RenderSystem.setShader(KEY_GLOW);
         RenderSystem.setShaderTexture(0, this.maskBuffer.getColorAttachment());
         RenderSystem.setShaderTexture(1, this.maskBuffer.getColorAttachment());
         this.setUniform(glowShader, "color", this.rf(color1), this.gf(color1), this.bf(color1));
         this.setUniform(glowShader, "color2", this.rf(color2), this.gf(color2), this.bf(color2));
         this.setUniform(glowShader, "exposure", 1.0F);
         this.drawFullscreenQuad();
      }

      this.restoreCompositeState();
   }

   private void renderTrailMode(GlassHands module, int color1, int color2) {
      ShaderProgram shader = mc.getShaderLoader().getOrCreateProgram(KEY_TRAIL);
      if (shader != null) {
         int iterations = Math.max(3, Math.min(10, (int)module.trailBlur.getFloatValue()));
         int blurredMaskTex = this.runKawaseBloom(iterations);
         mc.getFramebuffer().beginWrite(false);
         RenderSystem.disableDepthTest();
         RenderSystem.enableBlend();
         RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE, SrcFactor.ZERO, DstFactor.ONE);
         RenderSystem.setShader(KEY_TRAIL);
         RenderSystem.setShaderTexture(0, this.maskBuffer.getColorAttachment());
         RenderSystem.setShaderTexture(1, blurredMaskTex);
         int fw = Math.max(1, mc.getWindow().getFramebufferWidth());
         int fh = Math.max(1, mc.getWindow().getFramebufferHeight());
         this.setUniform(shader, "texelSize", 1.0F / (float)fw, 1.0F / (float)fh);
         this.setUniform(shader, "color", this.rf(color1), this.gf(color1), this.bf(color1));
         this.setUniform(shader, "color2", this.rf(color2), this.gf(color2), this.bf(color2));
         this.setUniform(shader, "trailIntensity", module.trailIntensity.getFloatValue());
         this.setUniform(shader, "glowSize", module.trailGlowSize.getFloatValue());
         this.setUniform(shader, "time", (float)(System.currentTimeMillis() % 100000L) / 1000.0F);
         this.setUniform(shader, "rainbowEnabled", module.rainbow.getValue() ? 1.0F : 0.0F);
         this.setUniform(shader, "rainbowSpeed", module.rainbowSpeed.getFloatValue());
         this.drawFullscreenQuad();
         this.restoreCompositeState();
      }
   }

   public void pulseAttack(float direction) {
      this.slashPulse = 1.0F;
      this.slashDirection = direction >= 0.0F ? 1.0F : -1.0F;
   }

   private int getHeldItemColor() {
      if (mc.player == null) {
         return ColorProvider.getThemeColor();
      } else {
         ItemStack stack = mc.player.getMainHandStack();
         if (stack.isEmpty()) {
            stack = mc.player.getOffHandStack();
         }

         if (stack.isEmpty()) {
            return ColorProvider.getThemeColor();
         } else {
            Item item = stack.getItem();
            String path = item.toString().toLowerCase();
            if (path.contains("diamond")) {
               return 5627360;
            } else if (path.contains("netherite")) {
               return 5982823;
            } else if (path.contains("golden") || path.contains("gold")) {
               return 16766042;
            } else if (path.contains("iron")) {
               return 14212840;
            } else if (path.contains("stone")) {
               return 9079434;
            } else if (path.contains("wood")) {
               return 12157509;
            } else if (path.contains("emerald")) {
               return 3526767;
            } else if (path.contains("redstone")) {
               return 14826299;
            } else if (path.contains("lapis")) {
               return 3233492;
            } else if (path.contains("copper")) {
               return 13728325;
            } else if (path.contains("amethyst")) {
               return 11562239;
            } else if (path.contains("ender") || path.contains("prismarine")) {
               return 4579520;
            } else {
               return path.contains("blaze") ? 16747050 : ColorProvider.getThemeColor();
            }
         }
      }
   }

   private void renderTrailMotionMode(GlassHands module, int color1, int color2) {
      ShaderProgram accumShader = mc.getShaderLoader().getOrCreateProgram(KEY_TRAIL_ACCUMULATE);
      if (accumShader != null && this.trailBuffer != null) {
         this.smoothTime += 0.016F;
         float currentYaw = mc.player != null ? mc.player.getYaw() : 0.0F;
         float currentPitch = mc.player != null ? mc.player.getPitch() : 0.0F;
         float deltaYaw = currentYaw - this.prevYaw;
         float deltaPitch = currentPitch - this.prevPitch;
         this.prevYaw = currentYaw;
         this.prevPitch = currentPitch;

         while (deltaYaw > 180.0F) {
            deltaYaw -= 360.0F;
         }

         while (deltaYaw < -180.0F) {
            deltaYaw += 360.0F;
         }

         this.smoothDeltaYaw = this.smoothDeltaYaw * 0.85F + deltaYaw * 0.15F;
         this.smoothDeltaPitch = this.smoothDeltaPitch * 0.85F + deltaPitch * 0.15F;
         this.slashPulse = Math.max(0.0F, this.slashPulse - 0.045F);
         int fw = Math.max(1, mc.getWindow().getFramebufferWidth());
         int fh = Math.max(1, mc.getWindow().getFramebufferHeight());
         float cameraInfluence = module.trailCamera.getFloatValue();
         float camX = this.smoothDeltaYaw * -3.5E-4F * cameraInfluence;
         float camY = this.smoothDeltaPitch * 3.5E-4F * cameraInfluence;
         Framebuffer tempBuffer = new SimpleFramebuffer(fw, fh, false);
         tempBuffer.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
         tempBuffer.clear();
         tempBuffer.beginWrite(false);
         RenderSystem.disableDepthTest();
         RenderSystem.disableBlend();
         RenderSystem.setShader(KEY_TRAIL_ACCUMULATE);
         RenderSystem.setShaderTexture(0, this.trailBuffer.getColorAttachment());
         RenderSystem.setShaderTexture(1, this.afterBuffer.getColorAttachment());
         RenderSystem.setShaderTexture(2, this.maskBuffer.getColorAttachment());
         this.setUniform(accumShader, "resolution", (float)fw, (float)fh, this.smoothTime, module.trailIntensityM.getFloatValue());
         this.setUniform(accumShader, "glowColor", this.rf(color1), this.gf(color1), this.bf(color1), 1.0F);
         this.setUniform(
            accumShader,
            "settings",
            module.trailItemColor.getValue() ? 1.0F : 0.0F,
            module.trailSpeed.getFloatValue(),
            module.trailLength.getFloatValue(),
            module.trailSoftness.getFloatValue()
         );
         this.setUniform(
            accumShader, "settings2", module.trailBlurRadius.getFloatValue(), module.trailSmoke.getFloatValue(), module.trailAttack.getFloatValue(), 0.0F
         );
         this.setUniform(accumShader, "reserved", this.slashPulse, this.slashDirection, camX, camY);
         this.drawFullscreenQuad();
         GL30.glBindFramebuffer(36008, tempBuffer.fbo);
         GL30.glBindFramebuffer(36009, this.trailBuffer.fbo);
         GL30.glBlitFramebuffer(0, 0, fw, fh, 0, 0, fw, fh, 16384, 9728);
         tempBuffer.delete();
         mc.getFramebuffer().beginWrite(false);
         RenderSystem.disableDepthTest();
         RenderSystem.enableBlend();
         RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA, SrcFactor.ZERO, DstFactor.ONE);
         RenderSystem.setShaderTexture(0, this.trailBuffer.getColorAttachment());
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX);
         this.drawFullscreenQuad();
         this.restoreCompositeState();
      }
   }

   private int runKawaseBloomFrom(int sourceTex, int iterations) {
      this.ensureBloomBuffers(iterations);
      if (this.bloomBuffers.isEmpty()) {
         return sourceTex;
      } else {
         ShaderProgram downShader = mc.getShaderLoader().getOrCreateProgram(KEY_KAWASE_DOWN);
         ShaderProgram upShader = mc.getShaderLoader().getOrCreateProgram(KEY_KAWASE_UP);
         if (downShader != null && upShader != null) {
            int currentTex = sourceTex;

            for (int i = 0; i < iterations; i++) {
               Framebuffer dst = this.bloomBuffers.get(i);
               dst.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
               dst.clear();
               dst.beginWrite(true);
               RenderSystem.setShader(KEY_KAWASE_DOWN);
               RenderSystem.setShaderTexture(0, currentTex);
               this.setKawaseUniforms(downShader, dst.textureWidth, dst.textureHeight, 1.0F + (float)i);
               this.drawFullscreenQuad();
               currentTex = dst.getColorAttachment();
            }

            for (int i = iterations - 1; i >= 1; i--) {
               Framebuffer dst = this.bloomBuffers.get(i - 1);
               dst.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
               dst.clear();
               dst.beginWrite(true);
               RenderSystem.setShader(KEY_KAWASE_UP);
               RenderSystem.setShaderTexture(0, currentTex);
               this.setKawaseUniforms(upShader, dst.textureWidth, dst.textureHeight, 1.0F + (float)i);
               this.setUniform(upShader, "color", 1.0F, 1.0F, 1.0F);
               this.drawFullscreenQuad();
               currentTex = dst.getColorAttachment();
            }

            mc.getFramebuffer().beginWrite(true);
            return currentTex;
         } else {
            return sourceTex;
         }
      }
   }

   private void renderPrettyMode(GlassHands module, int color1, int color2, float glowValue, float fillValue, float alphaValue, float outlineValue) {
      ShaderProgram shader = mc.getShaderLoader().getOrCreateProgram(KEY_PRETTY);
      if (shader != null) {
         mc.getFramebuffer().beginWrite(false);
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableDepthTest();
         RenderSystem.setShader(KEY_PRETTY);
         RenderSystem.setShaderTexture(0, this.maskBuffer.getColorAttachment());
         int fw = Math.max(1, mc.getWindow().getFramebufferWidth());
         int fh = Math.max(1, mc.getWindow().getFramebufferHeight());
         this.setUniform(shader, "texelSize", 1.0F / (float)fw, 1.0F / (float)fh);
         this.setUniform(shader, "color", this.rf(color1), this.gf(color1), this.bf(color1));
         this.setUniform(shader, "color2", this.rf(color2), this.gf(color2), this.bf(color2));
         this.setUniform(shader, "time", (float)(System.currentTimeMillis() % 100000L) / 1000.0F);
         this.setUniform(shader, "speed", module.waveSpeed.getFloatValue());
         this.setUniform(shader, "scale", module.waveScale.getFloatValue());
         this.setUniform(shader, "outline", outlineValue);
         this.setUniform(shader, "glow", glowValue);
         this.setUniform(shader, "fill", fillValue);
         this.setUniform(shader, "alpha", alphaValue);
         this.drawFullscreenQuad();
         RenderSystem.enableDepthTest();
         RenderSystem.disableBlend();
         this.restoreCompositeState();
      }
   }

   private int runKawaseBloom(int iterations) {
      return this.runKawaseBloomFrom(this.maskBuffer.getColorAttachment(), iterations);
   }

   private void copyMainFramebuffer(Framebuffer target) {
      int currentFbo = GL11.glGetInteger(36006);
      int sourceFbo = currentFbo != 0 ? currentFbo : mc.getFramebuffer().fbo;
      GL30.glBindFramebuffer(36008, sourceFbo);
      GL30.glBindFramebuffer(36009, target.fbo);
      GL30.glBlitFramebuffer(0, 0, this.width, this.height, 0, 0, this.width, this.height, 16640, 9728);
      GL30.glBindFramebuffer(36160, currentFbo);
   }

   private void configureDepthTexture(int depthTex) {
      RenderSystem.bindTexture(depthTex);
      GL11.glTexParameteri(3553, 34892, 0);
      GL11.glTexParameteri(3553, 10241, 9728);
      GL11.glTexParameteri(3553, 10240, 9728);
      RenderSystem.bindTexture(0);
   }

   private void ensureBuffers() {
      int w = mc.getWindow().getFramebufferWidth();
      int h = mc.getWindow().getFramebufferHeight();
      if (w != this.width || h != this.height || this.beforeBuffer == null || this.afterBuffer == null || this.maskBuffer == null || this.trailBuffer == null) {
         this.deleteBuffers();
         this.beforeBuffer = new SimpleFramebuffer(w, h, true);
         this.afterBuffer = new SimpleFramebuffer(w, h, true);
         this.maskBuffer = new SimpleFramebuffer(w, h, true);
         this.trailBuffer = new SimpleFramebuffer(w, h, false);
         this.width = w;
         this.height = h;
         this.configuredBeforeDepthTex = -1;
         this.configuredAfterDepthTex = -1;
      }
   }

   private void deleteBuffers() {
      if (this.beforeBuffer != null) {
         this.beforeBuffer.delete();
         this.beforeBuffer = null;
      }

      if (this.afterBuffer != null) {
         this.afterBuffer.delete();
         this.afterBuffer = null;
      }

      if (this.maskBuffer != null) {
         this.maskBuffer.delete();
         this.maskBuffer = null;
      }

      if (this.trailBuffer != null) {
         this.trailBuffer.delete();
         this.trailBuffer = null;
      }

      if (this.fillTrailRead != null) {
         this.fillTrailRead.delete();
         this.fillTrailRead = null;
      }

      if (this.fillTrailWrite != null) {
         this.fillTrailWrite.delete();
         this.fillTrailWrite = null;
      }

      if (this.colorMask != null) {
         this.colorMask.delete();
         this.colorMask = null;
      }

      if (this.waveColorBuf != null) {
         this.waveColorBuf.delete();
         this.waveColorBuf = null;
      }

      if (this.waveTrailRead != null) {
         this.waveTrailRead.delete();
         this.waveTrailRead = null;
      }

      if (this.waveTrailWrite != null) {
         this.waveTrailWrite.delete();
         this.waveTrailWrite = null;
      }

      for (Framebuffer fb : this.bloomBuffers) {
         fb.delete();
      }

      this.bloomBuffers.clear();

      for (Framebuffer fb : this.fillBloomBuffers) {
         fb.delete();
      }

      this.fillBloomBuffers.clear();
   }

   private void ensureBloomBuffers(int iterations) {
      while (this.bloomBuffers.size() > iterations) {
         int last = this.bloomBuffers.size() - 1;
         this.bloomBuffers.get(last).delete();
         this.bloomBuffers.remove(last);
      }

      for (int i = 0; i < iterations; i++) {
         int w = Math.max(2, this.width >> i + 1);
         int h = Math.max(2, this.height >> i + 1);
         if (i >= this.bloomBuffers.size()) {
            Framebuffer fb = new SimpleFramebuffer(w, h, false);
            this.setLinearFiltering(fb);
            this.bloomBuffers.add(fb);
         } else {
            Framebuffer fb = this.bloomBuffers.get(i);
            if (fb.textureWidth != w || fb.textureHeight != h) {
               fb.delete();
               Framebuffer var8 = new SimpleFramebuffer(w, h, false);
               this.setLinearFiltering(var8);
               this.bloomBuffers.set(i, var8);
            }
         }
      }
   }

   private void setLinearFiltering(Framebuffer fb) {
      RenderSystem.bindTexture(fb.getColorAttachment());
      GL11.glTexParameteri(3553, 10241, 9729);
      GL11.glTexParameteri(3553, 10240, 9729);
      RenderSystem.bindTexture(0);
   }

   private void clearTrailBuffer(Framebuffer buf) {
      if (buf != null) {
         buf.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
         buf.clear();
      }
   }

   private void restoreCompositeState() {
      RenderSystem.colorMask(true, true, true, true);
      RenderSystem.depthMask(true);
      RenderSystem.enableDepthTest();
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.setShaderTexture(0, 0);
      RenderSystem.setShaderTexture(1, 0);
      RenderSystem.setShaderTexture(2, 0);
      RenderSystem.setShaderTexture(3, 0);
      mc.getFramebuffer().beginWrite(true);
   }

   private void renderGlassMode(GlassHands module, int color1, int color2) {
      int iterations = 3;
      int blurredBgTex = this.runKawaseBloomFrom(this.beforeBuffer.getColorAttachment(), iterations);
      ShaderProgram glassShader = mc.getShaderLoader().getOrCreateProgram(KEY_GLASS);
      if (glassShader != null) {
         mc.getFramebuffer().beginWrite(false);
         RenderSystem.disableDepthTest();
         RenderSystem.enableBlend();
         RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA, SrcFactor.ZERO, DstFactor.ONE);
         RenderSystem.setShader(KEY_GLASS);
         RenderSystem.setShaderTexture(0, blurredBgTex);
         RenderSystem.setShaderTexture(1, this.afterBuffer.getColorAttachment());
         RenderSystem.setShaderTexture(2, this.maskBuffer.getColorAttachment());
         this.setUniform(glassShader, "mixFactor", module.glassMixFactor.getFloatValue());
         this.drawFullscreenQuad();
         RenderSystem.defaultBlendFunc();
         if (module.glassGlowEnabled.getValue() && module.glassOuterGlow.getValue()) {
            this.renderGlassOuterGlow(module, color1, color2);
         }

         this.restoreCompositeState();
      }
   }

   private void renderGlassOuterGlow(GlassHands module, int color1, int color2) {
      int iterations = Math.max(1, Math.min(6, (int)module.glassGlowRadius.getFloatValue()));
      int glowTex = this.runKawaseBloom(iterations);
      int c1;
      int c2;
      if (module.glassRainbow.getValue()) {
         int[] rb = this.rainbowColors(module.glassRainbowSpeed.getFloatValue());
         c1 = rb[0];
         c2 = rb[1];
      } else {
         c1 = module.glassGlowColor1.getValue();
         c2 = module.glassGlowColor2.getValue();
      }

      float[] col1 = new float[]{this.rf(c1), this.gf(c1), this.bf(c1)};
      float[] col2 = new float[]{this.rf(c2), this.gf(c2), this.bf(c2)};
      ShaderProgram glowShader = mc.getShaderLoader().getOrCreateProgram(KEY_GLASS_GLOW);
      if (glowShader != null) {
         mc.getFramebuffer().beginWrite(false);
         RenderSystem.enableBlend();
         RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE, SrcFactor.ZERO, DstFactor.ONE);
         RenderSystem.setShader(KEY_GLASS_GLOW);
         RenderSystem.setShaderTexture(0, glowTex);
         RenderSystem.setShaderTexture(1, this.maskBuffer.getColorAttachment());
         this.setUniform(glowShader, "glowColor1", col1[0], col1[1], col1[2]);
         this.setUniform(glowShader, "glowColor2", col2[0], col2[1], col2[2]);
         this.setUniform(glowShader, "exposure", module.glassGlowExposure.getFloatValue());
         this.drawFullscreenQuad();
         RenderSystem.defaultBlendFunc();
      }
   }

   private void renderPlasmaMode(GlassHands module, int color1, int color2) {
      ShaderProgram shader = mc.getShaderLoader().getOrCreateProgram(KEY_PLASMA);
      if (shader != null) {
         mc.getFramebuffer().beginWrite(false);
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableDepthTest();
         RenderSystem.setShader(KEY_PLASMA);
         RenderSystem.setShaderTexture(0, this.maskBuffer.getColorAttachment());
         float time = (float)(System.currentTimeMillis() % 100000L) / 1000.0F;
         this.setUniform(shader, "iTime", time * module.plasmaSpeed.getFloatValue());
         this.setUniform(shader, "uColor", this.rf(color1), this.gf(color1), this.bf(color1));
         this.setUniform(shader, "plasmaScale", module.plasmaScale.getFloatValue());
         this.setUniform(shader, "uShowStars", module.plasmaStars.getValue() ? 1.0F : 0.0F);
         this.drawFullscreenQuad();
         RenderSystem.enableDepthTest();
         RenderSystem.disableBlend();
         this.restoreCompositeState();
      }
   }

   private void renderWaveMode(GlassHands module, int color1, int color2) {
      ShaderProgram shader = mc.getShaderLoader().getOrCreateProgram(KEY_WAVE);
      if (shader != null) {
         if (module.waveRainbow.getValue() && !module.waveAutoColor.getValue()) {
            int[] rb = this.rainbowColors(module.waveRainbowSpeed.getFloatValue());
            color1 = rb[0];
            color2 = rb[0];
         }

         this.waveColorBuf = this.ensureBuffer(this.waveColorBuf, false, 1);
         this.waveColorBuf.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
         this.waveColorBuf.clear();
         this.waveColorBuf.beginWrite(false);
         RenderSystem.disableBlend();
         RenderSystem.disableDepthTest();
         RenderSystem.setShader(KEY_WAVE);
         RenderSystem.setShaderTexture(0, this.maskBuffer.getColorAttachment());
         float t = (float)(System.currentTimeMillis() % 100000L) / 1000.0F;
         this.setUniform(shader, "time", t);
         this.setUniform(shader, "color1", this.rf(color1), this.gf(color1), this.bf(color1));
         this.setUniform(shader, "color2", this.rf(color2), this.gf(color2), this.bf(color2));
         this.setUniform(shader, "waveSpeedX", module.waveSpeedX.getFloatValue());
         this.setUniform(shader, "waveSpeedY", module.waveSpeedY.getFloatValue());
         this.setUniform(shader, "waveScale", module.waveScaleM.getFloatValue());
         this.setUniform(shader, "waveDensity", module.waveDensity.getFloatValue());
         this.setUniform(shader, "waveGlow", module.waveGlow.getFloatValue());
         this.setUniform(shader, "fillAlpha", 1.0F);
         this.setUniform(shader, "modelVisibility", module.waveModelVisibility.getFloatValue());
         RenderSystem.setShaderTexture(1, this.afterBuffer.getColorAttachment());
         this.drawFullscreenQuad();
         if (module.waveGlowEnabled.getValue() && module.waveOuterGlow.getValue()) {
            this.doWaveBloomChain(module, color1, color2);
         }

         mc.getFramebuffer().beginWrite(false);
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(1, 771);
         RenderSystem.disableDepthTest();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, module.waveFillAlpha.getFloatValue());
         RenderSystem.setShaderTexture(0, this.waveColorBuf.getColorAttachment());
         RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX);
         this.drawFullscreenQuad();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         this.restoreCompositeState();
      }
   }

   private void doWaveBloomChain(GlassHands module, int color1, int color2) {
      int iter = (int)module.waveGlowRadius.getFloatValue();
      int bloomSource = module.waveAutoColor.getValue() ? this.waveColorBuf.getColorAttachment() : this.maskBuffer.getColorAttachment();
      this.ensureFillBloomBuffers(iter);
      ShaderProgram downShader = mc.getShaderLoader().getOrCreateProgram(KEY_KAWASE_DOWN);
      ShaderProgram upShader = mc.getShaderLoader().getOrCreateProgram(KEY_KAWASE_UP);
      if (downShader != null && upShader != null) {
         int cur = bloomSource;

         for (int i = 0; i < iter; i++) {
            Framebuffer b = this.fillBloomBuffers.get(i);
            b.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            b.clear();
            b.beginWrite(true);
            RenderSystem.setShader(KEY_KAWASE_DOWN);
            RenderSystem.setShaderTexture(0, cur);
            this.setKawaseUniforms(downShader, b.textureWidth, b.textureHeight, 1.0F + (float)i);
            this.drawFullscreenQuad();
            cur = b.getColorAttachment();
         }

         for (int i = iter - 1; i >= 1; i--) {
            Framebuffer b = this.fillBloomBuffers.get(i - 1);
            b.beginWrite(true);
            RenderSystem.setShader(KEY_KAWASE_UP);
            RenderSystem.setShaderTexture(0, cur);
            this.setKawaseUniforms(upShader, b.textureWidth, b.textureHeight, 1.0F + (float)i);
            this.setUniform(upShader, "color", 1.0F, 1.0F, 1.0F);
            this.drawFullscreenQuad();
            cur = b.getColorAttachment();
         }

         this.fillLastBloomTex = cur;
         int g1;
         int g2;
         if (module.waveRainbow.getValue() && !module.waveAutoColor.getValue()) {
            g1 = color1;
            g2 = color1;
         } else {
            g1 = module.waveGlowColor1.getValue();
            g2 = module.waveGlowColor2.getValue();
         }

         float[] c1 = new float[]{this.rf(g1), this.gf(g1), this.bf(g1)};
         float[] c2 = new float[]{this.rf(g2), this.gf(g2), this.bf(g2)};
         if (module.waveOuterGlow.getValue()) {
            if (module.waveTrailEnabled.getValue()) {
               this.renderWaveTrail(module, cur, c1, c2);
            } else {
               mc.getFramebuffer().beginWrite(true);
               RenderSystem.enableBlend();
               RenderSystem.blendFunc(770, 1);
               ShaderProgram glowShader = mc.getShaderLoader().getOrCreateProgram(KEY_FILL_GLOW);
               if (glowShader != null) {
                  RenderSystem.setShader(KEY_FILL_GLOW);
                  RenderSystem.setShaderTexture(0, cur);
                  RenderSystem.setShaderTexture(1, this.maskBuffer.getColorAttachment());
                  this.setUniform(glowShader, "glowColor1", c1[0], c1[1], c1[2]);
                  this.setUniform(glowShader, "glowColor2", c2[0], c2[1], c2[2]);
                  this.setUniform(glowShader, "exposure", module.waveGlowExposure.getFloatValue());
                  this.setUniform(glowShader, "autoColor", module.waveAutoColor.getValue() ? 1.0F : 0.0F);
                  this.setUniform(glowShader, "saturation", module.waveSaturation.getFloatValue());
                  this.drawFullscreenQuad();
               }

               RenderSystem.defaultBlendFunc();
            }
         }
      }
   }

   private void renderWaveTrail(GlassHands module, int bloomTex, float[] c1, float[] c2) {
      this.waveTrailRead = this.ensureBuffer(this.waveTrailRead, false, 2);
      this.waveTrailWrite = this.ensureBuffer(this.waveTrailWrite, false, 2);
      long now = System.currentTimeMillis();
      float rawDt = this.waveLastTrailTime > 0L ? (float)(now - this.waveLastTrailTime) / 1000.0F : 0.016666668F;
      this.waveLastTrailTime = now;
      if (rawDt <= 0.0F || rawDt > 0.05F) {
         rawDt = this.waveSmoothDt;
      }

      rawDt = Math.max(0.0069444445F, Math.min(0.016666668F, rawDt));
      this.waveSmoothDt = this.waveSmoothDt + (rawDt - this.waveSmoothDt) * 0.08F;
      float dt = this.waveSmoothDt;
      float t = (float)(now % 100000L) / 1000.0F;
      float swayFreq = 3.6F;
      float upDelta = module.waveTrailRise.getFloatValue() * dt;
      float amp = module.waveTrailSway.getFloatValue();
      float swayDelta = (float)(Math.sin((double)(t * swayFreq)) - Math.sin((double)((t - dt) * swayFreq))) * amp;
      float trailSmooth = 1.0F - (float)Math.exp((double)(-dt * 8.0F));
      this.waveSmoothTrailRise = this.waveSmoothTrailRise + (upDelta - this.waveSmoothTrailRise) * trailSmooth;
      this.waveSmoothTrailSway = this.waveSmoothTrailSway + (swayDelta - this.waveSmoothTrailSway) * trailSmooth;
      boolean swinging = mc.player != null && mc.player.handSwingProgress > 0.0F;
      if (module.waveTrailBurst.getValue() && swinging && !this.waveWasSwinging) {
         this.waveLastSwingMs = now;
      }

      this.waveWasSwinging = swinging;
      float burstAge = (float)(now - this.waveLastSwingMs) / 1000.0F;
      float burst = Math.max(0.0F, 1.0F - burstAge / 0.45F);
      this.waveSmoothBurst = this.waveSmoothBurst + (burst - this.waveSmoothBurst) * (1.0F - (float)Math.exp((double)(-dt * 6.0F)));
      float fadeAdd = this.waveSmoothBurst * module.waveTrailBurstPower.getFloatValue() * 0.012F;
      this.waveTrailWrite.beginWrite(false);
      GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
      GL11.glClear(16384);
      RenderSystem.disableBlend();
      ShaderProgram fadeShader = mc.getShaderLoader().getOrCreateProgram(KEY_FILL_TRAIL_FADE);
      if (fadeShader != null) {
         RenderSystem.setShader(KEY_FILL_TRAIL_FADE);
         RenderSystem.setShaderTexture(0, this.waveTrailRead.getColorAttachment());
         this.setUniform(fadeShader, "offset", this.waveSmoothTrailSway, this.waveSmoothTrailRise);
         this.setUniform(fadeShader, "fade", module.waveTrailFade.getFloatValue() + fadeAdd);
         this.setUniform(fadeShader, "t", t);
         this.setUniform(fadeShader, "dt", dt);
         this.setUniform(fadeShader, "turb", module.waveTrailTurb.getFloatValue());
         this.setUniform(fadeShader, "flickAmp", module.waveTrailFlicker.getFloatValue());
         this.setUniform(fadeShader, "texSize", (float)this.waveTrailRead.textureWidth, (float)this.waveTrailRead.textureHeight);
         this.drawFullscreenQuad();
      }

      RenderSystem.enableBlend();
      RenderSystem.blendFunc(1, 771);
      ShaderProgram colorShader = mc.getShaderLoader().getOrCreateProgram(KEY_FILL_TRAIL_COLOR);
      if (colorShader != null) {
         RenderSystem.setShader(KEY_FILL_TRAIL_COLOR);
         RenderSystem.setShaderTexture(0, bloomTex);
         this.setUniform(colorShader, "glowColor1", c1[0], c1[1], c1[2]);
         this.setUniform(colorShader, "glowColor2", c2[0], c2[1], c2[2]);
         this.setUniform(colorShader, "exposure", module.waveGlowExposure.getFloatValue());
         this.setUniform(colorShader, "autoColor", module.waveAutoColor.getValue() ? 1.0F : 0.0F);
         this.setUniform(colorShader, "saturation", module.waveSaturation.getFloatValue());
         this.drawFullscreenQuad();
      }

      if (module.waveTrailModel.getValue()) {
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(770, 771);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, module.waveTrailModelAlpha.getFloatValue());
         RenderSystem.setShaderTexture(0, this.colorMask != null ? this.colorMask.getColorAttachment() : this.waveColorBuf.getColorAttachment());
         RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX);
         this.drawFullscreenQuad();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }

      mc.getFramebuffer().beginWrite(true);
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(1, 771);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.setShaderTexture(0, this.waveTrailWrite.getColorAttachment());
      RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX);
      this.drawFullscreenQuad();
      RenderSystem.defaultBlendFunc();
      Framebuffer tmp = this.waveTrailRead;
      this.waveTrailRead = this.waveTrailWrite;
      this.waveTrailWrite = tmp;
   }

   private void renderFillMode(GlassHands module, int color1, int color2) {
      if (module.fillGlowEnabled.getValue() || module.fillOutlineEnabled.getValue()) {
         this.renderFillEffects(module, color1, color2);
      }

      if (module.fillGlassMode.getValue()) {
         this.renderFillGlassMode(module);
      } else {
         this.renderFillColorMode(module);
      }

      this.restoreCompositeState();
   }

   private void renderFillColorMode(GlassHands module) {
      ShaderProgram shader = mc.getShaderLoader().getOrCreateProgram(KEY_FILL);
      if (shader != null) {
         mc.getFramebuffer().beginWrite(false);
         RenderSystem.enableBlend();
         RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA, SrcFactor.ZERO, DstFactor.ONE);
         RenderSystem.disableDepthTest();
         RenderSystem.setShader(KEY_FILL);
         RenderSystem.setShaderTexture(0, this.afterBuffer.getColorAttachment());
         RenderSystem.setShaderTexture(1, this.maskBuffer.getColorAttachment());
         int fc = module.fillRainbow.getValue() ? this.rainbowColors(module.fillRainbowSpeed.getFloatValue())[0] : module.fillColor.getValue();
         this.setUniform(shader, "fillColor", this.rf(fc), this.gf(fc), this.bf(fc));
         this.setUniform(shader, "fillAlpha", module.fillAlpha.getFloatValue());
         this.setUniform(shader, "keepShading", module.fillKeepShading.getValue() ? 1.0F : 0.0F);
         this.setUniform(shader, "shadingStrength", module.fillShadingStrength.getFloatValue());
         this.drawFullscreenQuad();
         RenderSystem.enableDepthTest();
         RenderSystem.defaultBlendFunc();
      }
   }

   private void renderFillGlassMode(GlassHands module) {
      int iterations = 3;
      int blurredBgTex = this.runKawaseBloomFrom(this.beforeBuffer.getColorAttachment(), iterations);
      ShaderProgram glassShader = mc.getShaderLoader().getOrCreateProgram(KEY_GLASS);
      if (glassShader != null) {
         mc.getFramebuffer().beginWrite(false);
         RenderSystem.disableDepthTest();
         RenderSystem.enableBlend();
         RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA, SrcFactor.ZERO, DstFactor.ONE);
         RenderSystem.setShader(KEY_GLASS);
         RenderSystem.setShaderTexture(0, blurredBgTex);
         RenderSystem.setShaderTexture(1, this.afterBuffer.getColorAttachment());
         RenderSystem.setShaderTexture(2, this.maskBuffer.getColorAttachment());
         this.setUniform(glassShader, "mixFactor", module.fillGlassMix.getFloatValue());
         this.drawFullscreenQuad();
         RenderSystem.enableDepthTest();
         RenderSystem.defaultBlendFunc();
      }
   }

   private void renderFillEffects(GlassHands module, int color1, int color2) {
      boolean doBloom = module.fillGlowEnabled.getValue() && module.fillOuterGlow.getValue();
      if (doBloom) {
         this.doFillBloomChain(module);
      } else if (module.fillOutlineEnabled.getValue() && module.fillAutoColor.getValue()) {
         this.doSmallBlurForFillAuto(module);
      }

      if (module.fillOutlineEnabled.getValue()) {
         this.renderFillOutline(module);
      }
   }

   private void doFillBloomChain(GlassHands module) {
      int iter = (int)module.fillGlowRadius.getFloatValue();
      this.colorMask = this.ensureBuffer(this.colorMask, false, 1);
      this.colorMask.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
      this.colorMask.clear();
      this.colorMask.beginWrite(false);
      RenderSystem.disableBlend();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.setShaderTexture(0, this.afterBuffer.getColorAttachment());
      RenderSystem.setShaderTexture(1, this.maskBuffer.getColorAttachment());
      ShaderProgram colorMaskShader = mc.getShaderLoader()
         .getOrCreateProgram(new ShaderProgramKey(ResourceProvider.getShaderIdentifier("glass_hands_color_mask"), VertexFormats.POSITION_TEXTURE_COLOR, Defines.EMPTY));
      if (colorMaskShader != null) {
         RenderSystem.setShader(
            new ShaderProgramKey(ResourceProvider.getShaderIdentifier("glass_hands_color_mask"), VertexFormats.POSITION_TEXTURE_COLOR, Defines.EMPTY)
         );
         this.drawFullscreenQuad();
      }

      int cur = this.colorMask.getColorAttachment();
      this.ensureFillBloomBuffers(iter);
      ShaderProgram downShader = mc.getShaderLoader().getOrCreateProgram(KEY_KAWASE_DOWN);
      ShaderProgram upShader = mc.getShaderLoader().getOrCreateProgram(KEY_KAWASE_UP);
      if (downShader != null && upShader != null) {
         for (int i = 0; i < iter; i++) {
            Framebuffer b = this.fillBloomBuffers.get(i);
            b.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            b.clear();
            b.beginWrite(true);
            RenderSystem.setShader(KEY_KAWASE_DOWN);
            RenderSystem.setShaderTexture(0, cur);
            this.setKawaseUniforms(downShader, b.textureWidth, b.textureHeight, 1.0F + (float)i);
            this.drawFullscreenQuad();
            cur = b.getColorAttachment();
         }

         for (int i = iter - 1; i >= 1; i--) {
            Framebuffer b = this.fillBloomBuffers.get(i - 1);
            b.beginWrite(true);
            RenderSystem.setShader(KEY_KAWASE_UP);
            RenderSystem.setShaderTexture(0, cur);
            this.setKawaseUniforms(upShader, b.textureWidth, b.textureHeight, 1.0F + (float)i);
            this.setUniform(upShader, "color", 1.0F, 1.0F, 1.0F);
            this.drawFullscreenQuad();
            cur = b.getColorAttachment();
         }

         this.fillLastBloomTex = cur;
         int g1;
         int g2;
         if (module.fillRainbow.getValue()) {
            int[] rb = this.rainbowColors(module.fillRainbowSpeed.getFloatValue());
            g1 = rb[0];
            g2 = rb[1];
         } else {
            g1 = module.fillGlowColor1.getValue();
            g2 = module.fillGlowColor2.getValue();
         }

         float[] c1 = new float[]{this.rf(g1), this.gf(g1), this.bf(g1)};
         float[] c2 = new float[]{this.rf(g2), this.gf(g2), this.bf(g2)};
         if (module.fillTrailEnabled.getValue()) {
            this.renderFillTrail(module, cur, c1, c2);
         } else {
            mc.getFramebuffer().beginWrite(true);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(770, 1);
            ShaderProgram glowShader = mc.getShaderLoader().getOrCreateProgram(KEY_FILL_GLOW);
            if (glowShader != null) {
               RenderSystem.setShader(KEY_FILL_GLOW);
               RenderSystem.setShaderTexture(0, cur);
               RenderSystem.setShaderTexture(1, this.maskBuffer.getColorAttachment());
               this.setUniform(glowShader, "glowColor1", c1[0], c1[1], c1[2]);
               this.setUniform(glowShader, "glowColor2", c2[0], c2[1], c2[2]);
               this.setUniform(glowShader, "exposure", module.fillGlowExposure.getFloatValue());
               this.setUniform(glowShader, "autoColor", module.fillAutoColor.getValue() ? 1.0F : 0.0F);
               this.setUniform(glowShader, "saturation", module.fillSaturation.getFloatValue());
               this.drawFullscreenQuad();
            }

            RenderSystem.defaultBlendFunc();
         }
      }
   }

   private void doSmallBlurForFillAuto(GlassHands module) {
      int iter = 3;
      this.colorMask = this.ensureBuffer(this.colorMask, false, 1);
      this.colorMask.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
      this.colorMask.clear();
      this.colorMask.beginWrite(false);
      RenderSystem.disableBlend();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.setShaderTexture(0, this.afterBuffer.getColorAttachment());
      RenderSystem.setShaderTexture(1, this.maskBuffer.getColorAttachment());
      ShaderProgram colorMaskShader = mc.getShaderLoader()
         .getOrCreateProgram(new ShaderProgramKey(ResourceProvider.getShaderIdentifier("glass_hands_color_mask"), VertexFormats.POSITION_TEXTURE_COLOR, Defines.EMPTY));
      if (colorMaskShader != null) {
         RenderSystem.setShader(
            new ShaderProgramKey(ResourceProvider.getShaderIdentifier("glass_hands_color_mask"), VertexFormats.POSITION_TEXTURE_COLOR, Defines.EMPTY)
         );
         this.drawFullscreenQuad();
      }

      this.ensureFillBloomBuffers(iter);
      int cur = this.colorMask.getColorAttachment();
      ShaderProgram downShader = mc.getShaderLoader().getOrCreateProgram(KEY_KAWASE_DOWN);
      ShaderProgram upShader = mc.getShaderLoader().getOrCreateProgram(KEY_KAWASE_UP);
      if (downShader != null && upShader != null) {
         for (int i = 0; i < iter; i++) {
            Framebuffer b = this.fillBloomBuffers.get(i);
            b.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            b.clear();
            b.beginWrite(true);
            RenderSystem.setShader(KEY_KAWASE_DOWN);
            RenderSystem.setShaderTexture(0, cur);
            this.setKawaseUniforms(downShader, b.textureWidth, b.textureHeight, 1.0F + (float)i);
            this.drawFullscreenQuad();
            cur = b.getColorAttachment();
         }

         for (int i = iter - 1; i >= 1; i--) {
            Framebuffer b = this.fillBloomBuffers.get(i - 1);
            b.beginWrite(true);
            RenderSystem.setShader(KEY_KAWASE_UP);
            RenderSystem.setShaderTexture(0, cur);
            this.setKawaseUniforms(upShader, b.textureWidth, b.textureHeight, 1.0F + (float)i);
            this.setUniform(upShader, "color", 1.0F, 1.0F, 1.0F);
            this.drawFullscreenQuad();
            cur = b.getColorAttachment();
         }

         this.fillLastBloomTex = cur;
         mc.getFramebuffer().beginWrite(true);
      }
   }

   private void renderFillTrail(GlassHands module, int bloomTex, float[] c1, float[] c2) {
      this.fillTrailRead = this.ensureBuffer(this.fillTrailRead, false, 2);
      this.fillTrailWrite = this.ensureBuffer(this.fillTrailWrite, false, 2);
      long now = System.currentTimeMillis();
      float rawDt = this.fillLastTrailTime > 0L ? (float)(now - this.fillLastTrailTime) / 1000.0F : 0.016666668F;
      this.fillLastTrailTime = now;
      if (rawDt <= 0.0F || rawDt > 0.05F) {
         rawDt = this.fillSmoothDt;
      }

      rawDt = Math.max(0.0069444445F, Math.min(0.016666668F, rawDt));
      this.fillSmoothDt = this.fillSmoothDt + (rawDt - this.fillSmoothDt) * 0.08F;
      float dt = this.fillSmoothDt;
      float t = (float)(now % 100000L) / 1000.0F;
      float swayFreq = 3.6F;
      float upDelta = module.fillTrailRise.getFloatValue() * dt;
      float amp = module.fillTrailSway.getFloatValue();
      float swayDelta = (float)(Math.sin((double)(t * swayFreq)) - Math.sin((double)((t - dt) * swayFreq))) * amp;
      float trailSmooth = 1.0F - (float)Math.exp((double)(-dt * 8.0F));
      this.fillSmoothTrailRise = this.fillSmoothTrailRise + (upDelta - this.fillSmoothTrailRise) * trailSmooth;
      this.fillSmoothTrailSway = this.fillSmoothTrailSway + (swayDelta - this.fillSmoothTrailSway) * trailSmooth;
      boolean swinging = mc.player != null && mc.player.handSwingProgress > 0.0F;
      if (module.fillTrailBurst.getValue() && swinging && !this.fillWasSwinging) {
         this.fillLastSwingMs = now;
      }

      this.fillWasSwinging = swinging;
      float burstAge = (float)(now - this.fillLastSwingMs) / 1000.0F;
      float burst = Math.max(0.0F, 1.0F - burstAge / 0.45F);
      this.fillSmoothBurst = this.fillSmoothBurst + (burst - this.fillSmoothBurst) * (1.0F - (float)Math.exp((double)(-dt * 6.0F)));
      float fadeAdd = this.fillSmoothBurst * module.fillTrailBurstPower.getFloatValue() * 0.012F;
      this.fillTrailWrite.beginWrite(false);
      GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
      GL11.glClear(16384);
      RenderSystem.disableBlend();
      ShaderProgram trailFadeShader = mc.getShaderLoader().getOrCreateProgram(KEY_FILL_TRAIL_FADE);
      if (trailFadeShader != null) {
         RenderSystem.setShader(KEY_FILL_TRAIL_FADE);
         RenderSystem.setShaderTexture(0, this.fillTrailRead.getColorAttachment());
         this.setUniform(trailFadeShader, "offset", this.fillSmoothTrailSway, this.fillSmoothTrailRise);
         this.setUniform(trailFadeShader, "fade", module.fillTrailFade.getFloatValue() + fadeAdd);
         this.setUniform(trailFadeShader, "t", t);
         this.setUniform(trailFadeShader, "dt", dt);
         this.setUniform(trailFadeShader, "turb", module.fillTrailTurb.getFloatValue());
         this.setUniform(trailFadeShader, "flickAmp", module.fillTrailFlicker.getFloatValue());
         this.setUniform(trailFadeShader, "texSize", (float)this.fillTrailRead.textureWidth, (float)this.fillTrailRead.textureHeight);
         this.drawFullscreenQuad();
      }

      RenderSystem.enableBlend();
      RenderSystem.blendFunc(1, 771);
      ShaderProgram trailColorShader = mc.getShaderLoader().getOrCreateProgram(KEY_FILL_TRAIL_COLOR);
      if (trailColorShader != null) {
         RenderSystem.setShader(KEY_FILL_TRAIL_COLOR);
         RenderSystem.setShaderTexture(0, bloomTex);
         this.setUniform(trailColorShader, "glowColor1", c1[0], c1[1], c1[2]);
         this.setUniform(trailColorShader, "glowColor2", c2[0], c2[1], c2[2]);
         this.setUniform(trailColorShader, "exposure", module.fillGlowExposure.getFloatValue());
         this.setUniform(trailColorShader, "autoColor", module.fillAutoColor.getValue() ? 1.0F : 0.0F);
         this.setUniform(trailColorShader, "saturation", module.fillSaturation.getFloatValue());
         this.drawFullscreenQuad();
      }

      if (module.fillTrailModel.getValue()) {
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(770, 771);
         float modelAlpha = module.fillTrailModelAlpha.getFloatValue();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, modelAlpha);
         if (this.colorMask != null) {
            RenderSystem.setShaderTexture(0, this.colorMask.getColorAttachment());
         } else {
            RenderSystem.setShaderTexture(0, this.afterBuffer.getColorAttachment());
         }

         RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX);
         this.drawFullscreenQuad();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }

      mc.getFramebuffer().beginWrite(true);
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(1, 771);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.setShaderTexture(0, this.fillTrailWrite.getColorAttachment());
      RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX);
      this.drawFullscreenQuad();
      RenderSystem.defaultBlendFunc();
      Framebuffer tmp = this.fillTrailRead;
      this.fillTrailRead = this.fillTrailWrite;
      this.fillTrailWrite = tmp;
   }

   private void renderFillOutline(GlassHands module) {
      boolean auto = module.fillAutoColor.getValue() && this.fillLastBloomTex != -1;
      int colorMode = auto ? 2 : 0;
      int oc = module.fillOutlineColor.getValue();
      Color col = new Color(oc, true);
      float a = (float)col.getAlpha() / 255.0F;
      ShaderProgram outlineShader = mc.getShaderLoader().getOrCreateProgram(KEY_FILL_OUTLINE);
      if (outlineShader != null) {
         mc.getFramebuffer().beginWrite(false);
         RenderSystem.enableBlend();
         RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA, SrcFactor.ZERO, DstFactor.ONE);
         RenderSystem.setShader(KEY_FILL_OUTLINE);
         RenderSystem.setShaderTexture(0, this.maskBuffer.getColorAttachment());
         if (auto) {
            RenderSystem.setShaderTexture(1, this.fillLastBloomTex);
         }

         int fw = Math.max(1, mc.getWindow().getFramebufferWidth());
         int fh = Math.max(1, mc.getWindow().getFramebufferHeight());
         this.setUniform(outlineShader, "colorMode", (float)colorMode);
         this.setUniform(outlineShader, "width", module.fillOutlineWidth.getFloatValue());
         this.setUniform(outlineShader, "texelSize", 1.0F / (float)fw, 1.0F / (float)fh);
         this.setUniform(outlineShader, "alpha", a);
         this.setUniform(outlineShader, "saturation", module.fillSaturation.getFloatValue());
         this.setUniform(outlineShader, "solidColor", (float)col.getRed() / 255.0F, (float)col.getGreen() / 255.0F, (float)col.getBlue() / 255.0F);
         this.drawFullscreenQuad();
         RenderSystem.defaultBlendFunc();
      }
   }

   private static float clamp01(float v) {
      return v < 0.0F ? 0.0F : Math.min(v, 1.0F);
   }

   private int[] rainbowColors(float speed) {
      float hue = (float)(System.currentTimeMillis() % (long)(10000.0 / (double)speed)) / (10000.0F / speed);
      int c1 = Color.HSBtoRGB(hue, 1.0F, 1.0F) & 16777215;
      int c2 = Color.HSBtoRGB((hue + 0.5F) % 1.0F, 1.0F, 1.0F) & 16777215;
      return new int[]{c1, c2};
   }

   private Framebuffer ensureBuffer(Framebuffer buf, boolean depth, int divisor) {
      int fw = mc.getWindow().getFramebufferWidth();
      int fh = mc.getWindow().getFramebufferHeight();
      int w = Math.max(2, fw / divisor);
      int h = Math.max(2, fh / divisor);
      if (buf == null) {
         buf = new SimpleFramebuffer(w, h, depth);
         this.setLinearFiltering(buf);
      } else if (buf.textureWidth != w || buf.textureHeight != h) {
         buf.delete();
         buf = new SimpleFramebuffer(w, h, depth);
         this.setLinearFiltering(buf);
      }

      return buf;
   }

   private void ensureFillBloomBuffers(int n) {
      int fw = mc.getWindow().getFramebufferWidth();
      int fh = mc.getWindow().getFramebufferHeight();
      if (this.fillBloomBuffers.size() < n) {
         this.fillBloomBuffers.forEach(Framebuffer::delete);
         this.fillBloomBuffers.clear();

         for (int i = 0; i < n; i++) {
            Framebuffer f = new SimpleFramebuffer(Math.max(2, fw >> i + 1), Math.max(2, fh >> i + 1), false);
            this.setLinearFiltering(f);
            this.fillBloomBuffers.add(f);
         }
      }

      for (int i = 0; i < n; i++) {
         int w = Math.max(2, fw >> i + 1);
         int h = Math.max(2, fh >> i + 1);
         Framebuffer b = this.fillBloomBuffers.get(i);
         if (b.textureWidth != w || b.textureHeight != h) {
            b.delete();
            Framebuffer var10 = new SimpleFramebuffer(w, h, false);
            this.setLinearFiltering(var10);
            this.fillBloomBuffers.set(i, var10);
         }
      }
   }

   private boolean isEffectEnabled(GlassHands module) {
      if (module == null || !module.isEnabled()) {
         return false;
      } else if (module.mode.is("Blur")) {
         return true;
      } else if (module.mode.is("Outline")) {
         return true;
      } else if (module.mode.is("Trail")) {
         return true;
      } else if (module.mode.is("Trail")) {
         return true;
      } else if (module.mode.is("Glass")) {
         return true;
      } else if (module.mode.is("Plasma")) {
         return true;
      } else if (module.mode.is("Fill")) {
         return true;
      } else if (module.mode.is("Wave")) {
         return true;
      } else {
         boolean hasGlow = module.glow.getFloatValue() > 0.001F;
         boolean hasFill = module.fill.getFloatValue() > 0.001F && module.alpha.getFloatValue() > 0.001F;
         return hasGlow || hasFill;
      }
   }

   private void drawFullscreenQuad() {
      float sw = (float)Math.max(mc.getWindow().getScaledWidth(), 1);
      float sh = (float)Math.max(mc.getWindow().getScaledHeight(), 1);
      BufferBuilder b = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      b.vertex(0.0F, 0.0F, 0.0F).texture(0.0F, 1.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
      b.vertex(0.0F, sh, 0.0F).texture(0.0F, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
      b.vertex(sw, sh, 0.0F).texture(1.0F, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
      b.vertex(sw, 0.0F, 0.0F).texture(1.0F, 1.0F).color(1.0F, 1.0F, 1.0F, 1.0F);
      BufferRenderer.drawWithGlobalProgram(b.end());
   }

   private void setUniform(ShaderProgram s, String name, float v) {
      GlUniform u = s.getUniform(name);
      if (u != null) {
         u.set(v);
      }
   }

   private void setUniform(ShaderProgram s, String name, float x, float y) {
      GlUniform u = s.getUniform(name);
      if (u != null) {
         u.set(x, y);
      }
   }

   private void setUniform(ShaderProgram s, String name, float x, float y, float z) {
      GlUniform u = s.getUniform(name);
      if (u != null) {
         u.set(x, y, z);
      }
   }

   private void setUniform(ShaderProgram s, String name, float x, float y, float z, float w) {
      GlUniform u = s.getUniform(name);
      if (u != null) {
         u.set(x, y, z, w);
      }
   }

   private void setKawaseUniforms(ShaderProgram s, int w, int h, float offset) {
      this.setUniform(s, "uSize", (float)Math.max(1, w), (float)Math.max(1, h));
      this.setUniform(s, "uOffset", offset, offset);
      this.setUniform(s, "uHalfPixel", 0.5F / (float)Math.max(1, w), 0.5F / (float)Math.max(1, h));
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
