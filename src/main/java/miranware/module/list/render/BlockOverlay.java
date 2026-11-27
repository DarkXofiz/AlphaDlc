package miranware.module.list.render;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import org.joml.Matrix4f;
import miranware.event.list.EventWorldRender;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.BooleanSetting;
import miranware.module.settings.ColorSetting;
import miranware.module.settings.ModeSetting;
import miranware.module.settings.SliderSetting;
import miranware.util.render.providers.ResourceProvider;

@ModuleInformation(
   moduleName = "Block Overlay",
   moduleDesc = "Custom block highlight",
   moduleCategory = ModuleCategory.RENDER
)
public class BlockOverlay extends Module {
   private static final ShaderProgramKey ENERGY_SHADER = shaderKey("block_overlay");
   private static final ShaderProgramKey WAVE_SHADER = shaderKey("block_overlay_wave");
   private static final ShaderProgramKey CRYSTAL_SHADER = shaderKey("block_overlay_crystal");
   private static final ShaderProgramKey GALAXY_SHADER = shaderKey("block_overlay_galaxy");
   private static final ShaderProgramKey LIGHTNING_SHADER = shaderKey("block_overlay_lightning");
   public final ModeSetting shaderMode = new ModeSetting("Shader", "Energy", "Energy", "Wave", "Crystal", "Galaxy", "Lightning");
   public final ColorSetting overlayColor = new ColorSetting("Color", -9673774);
   public final SliderSetting fillOpacity = new SliderSetting(
      "Fill Opacity", 0.32, 0.0, 1.0, 0.05
   );
   public final BooleanSetting outline = new BooleanSetting("Outline", true);
   public final SliderSetting outlineWidth = new SliderSetting(
         "Outline Thickness", 2.0, 0.5, 5.0, 0.1
      )
      .setVisible(() -> this.outline.getValue());
   public final SliderSetting outlineOpacity = new SliderSetting(
         "Outline Opacity", 1.0, 0.0, 1.0, 0.05
      )
      .setVisible(() -> this.outline.getValue());
   public final SliderSetting smoothness = new SliderSetting("Smoothness", 6.0, 1.0, 10.0, 1.0);
   private Box currentBox;
   private long lastFrameTime;
   private Object lastWorld;
   private float visibility;

   private static ShaderProgramKey shaderKey(String name) {
      return new ShaderProgramKey(ResourceProvider.getShaderIdentifier(name), VertexFormats.POSITION_TEXTURE_COLOR, Defines.EMPTY);
   }

   @Subscribe
   public void onWorldRender(EventWorldRender event) {
      if (this.mc.world != null && this.mc.player != null) {
         long now = System.nanoTime();
         float dt = this.lastFrameTime == 0L ? 0.016666668F : Math.min(0.1F, (float)(now - this.lastFrameTime) / 1.0E9F);
         this.lastFrameTime = now;
         Box targetBox = this.getTargetBox();
         if (this.lastWorld != this.mc.world) {
            this.lastWorld = this.mc.world;
            this.currentBox = targetBox;
            this.visibility = 0.0F;
         }

         float smoothnessValue = this.smoothness.getFloatValue();
         if (smoothnessValue == 1.0F) {
            this.currentBox = targetBox;
            this.visibility = targetBox == null ? 0.0F : 1.0F;
            if (this.currentBox == null) {
               return;
            }
         } else {
            float smoothnessProgress = Math.max(0.0F, Math.min(1.0F, (smoothnessValue - 2.0F) / 8.0F));
            float responseRate = 18.0F + -15.0F * smoothnessProgress;
            float easing = 1.0F - (float)Math.exp((double)(-responseRate * dt));
            if (targetBox != null) {
               if (this.currentBox == null) {
                  this.currentBox = targetBox;
               } else {
                  this.currentBox = this.lerpBox(this.currentBox, targetBox, easing);
               }

               this.visibility = this.visibility + (1.0F - this.visibility) * easing;
            } else {
               this.visibility = this.visibility + (0.0F - this.visibility) * easing;
               if (this.visibility < 0.01F) {
                  this.currentBox = null;
                  this.visibility = 0.0F;
                  return;
               }
            }
         }

         if (this.currentBox != null) {
            int color = this.overlayColor.getValue();
            MatrixStack matrices = event.getMatrixStack();
            Vec3d camPos = this.mc.gameRenderer.getCamera().getPos();
            matrices.push();
            double minX = this.currentBox.minX - camPos.x;
            double minY = this.currentBox.minY - camPos.y;
            double minZ = this.currentBox.minZ - camPos.z;
            double maxX = this.currentBox.maxX - camPos.x;
            double maxY = this.currentBox.maxY - camPos.y;
            double maxZ = this.currentBox.maxZ - camPos.z;
            this.drawFilled(matrices, minX, minY, minZ, maxX, maxY, maxZ, color, this.visibility);
            if (this.outline.getValue()) {
               this.drawOutline(matrices, minX, minY, minZ, maxX, maxY, maxZ, color, this.visibility);
            }

            matrices.pop();
         }
      }
   }

   @Override
   public void onDisable() {
      this.currentBox = null;
      this.lastFrameTime = 0L;
      this.lastWorld = null;
      this.visibility = 0.0F;
      super.onDisable();
   }

   private Box getTargetBox() {
      if (this.mc.crosshairTarget != null && this.mc.crosshairTarget.getType() == Type.BLOCK) {
         BlockPos pos = ((BlockHitResult)this.mc.crosshairTarget).getBlockPos();
         BlockState state = this.mc.world.getBlockState(pos);
         if (state.isAir()) {
            return null;
         } else {
            VoxelShape shape = state.getOutlineShape(this.mc.world, pos);
            return shape.isEmpty() ? null : shape.getBoundingBox().offset(pos);
         }
      } else {
         return null;
      }
   }

   private Box lerpBox(Box from, Box to, float delta) {
      return new Box(
         from.minX + (to.minX - from.minX) * (double)delta,
         from.minY + (to.minY - from.minY) * (double)delta,
         from.minZ + (to.minZ - from.minZ) * (double)delta,
         from.maxX + (to.maxX - from.maxX) * (double)delta,
         from.maxY + (to.maxY - from.maxY) * (double)delta,
         from.maxZ + (to.maxZ - from.maxZ) * (double)delta
      );
   }

   private void drawOutline(MatrixStack matrices, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, int color, float visibility) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableDepthTest();
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      RenderSystem.lineWidth(this.outlineWidth.getFloatValue());
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder buffer = tessellator.begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
      float r = (float)(color >> 16 & 0xFF) / 255.0F;
      float g = (float)(color >> 8 & 0xFF) / 255.0F;
      float b = (float)(color & 0xFF) / 255.0F;
      float outlineAlpha = this.outlineOpacity.getFloatValue() * Math.max(0.0F, Math.min(1.0F, visibility));
      buffer.vertex(matrix, (float)minX, (float)minY, (float)minZ).color(r, g, b, outlineAlpha);
      buffer.vertex(matrix, (float)maxX, (float)minY, (float)minZ).color(r, g, b, outlineAlpha);
      buffer.vertex(matrix, (float)maxX, (float)minY, (float)minZ).color(r, g, b, outlineAlpha);
      buffer.vertex(matrix, (float)maxX, (float)minY, (float)maxZ).color(r, g, b, outlineAlpha);
      buffer.vertex(matrix, (float)maxX, (float)minY, (float)maxZ).color(r, g, b, outlineAlpha);
      buffer.vertex(matrix, (float)minX, (float)minY, (float)maxZ).color(r, g, b, outlineAlpha);
      buffer.vertex(matrix, (float)minX, (float)minY, (float)maxZ).color(r, g, b, outlineAlpha);
      buffer.vertex(matrix, (float)minX, (float)minY, (float)minZ).color(r, g, b, outlineAlpha);
      buffer.vertex(matrix, (float)minX, (float)maxY, (float)minZ).color(r, g, b, outlineAlpha);
      buffer.vertex(matrix, (float)maxX, (float)maxY, (float)minZ).color(r, g, b, outlineAlpha);
      buffer.vertex(matrix, (float)maxX, (float)maxY, (float)minZ).color(r, g, b, outlineAlpha);
      buffer.vertex(matrix, (float)maxX, (float)maxY, (float)maxZ).color(r, g, b, outlineAlpha);
      buffer.vertex(matrix, (float)maxX, (float)maxY, (float)maxZ).color(r, g, b, outlineAlpha);
      buffer.vertex(matrix, (float)minX, (float)maxY, (float)maxZ).color(r, g, b, outlineAlpha);
      buffer.vertex(matrix, (float)minX, (float)maxY, (float)maxZ).color(r, g, b, outlineAlpha);
      buffer.vertex(matrix, (float)minX, (float)maxY, (float)minZ).color(r, g, b, outlineAlpha);
      buffer.vertex(matrix, (float)minX, (float)minY, (float)minZ).color(r, g, b, outlineAlpha);
      buffer.vertex(matrix, (float)minX, (float)maxY, (float)minZ).color(r, g, b, outlineAlpha);
      buffer.vertex(matrix, (float)maxX, (float)minY, (float)minZ).color(r, g, b, outlineAlpha);
      buffer.vertex(matrix, (float)maxX, (float)maxY, (float)minZ).color(r, g, b, outlineAlpha);
      buffer.vertex(matrix, (float)maxX, (float)minY, (float)maxZ).color(r, g, b, outlineAlpha);
      buffer.vertex(matrix, (float)maxX, (float)maxY, (float)maxZ).color(r, g, b, outlineAlpha);
      buffer.vertex(matrix, (float)minX, (float)minY, (float)maxZ).color(r, g, b, outlineAlpha);
      buffer.vertex(matrix, (float)minX, (float)maxY, (float)maxZ).color(r, g, b, outlineAlpha);
      BufferRenderer.drawWithGlobalProgram(buffer.end());
      RenderSystem.enableDepthTest();
      RenderSystem.disableBlend();
      RenderSystem.lineWidth(1.0F);
   }

   private void drawFilled(MatrixStack matrices, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, int color, float visibility) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableDepthTest();
      RenderSystem.disableCull();
      ShaderProgram shader = RenderSystem.setShader(this.getOverlayShader());
      if (shader == null) {
         RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
      } else if (shader.getUniform("Time") != null) {
         shader.getUniform("Time").set((float)(System.currentTimeMillis() % 100000L) / 1000.0F);
      }

      Matrix4f matrix = matrices.peek().getPositionMatrix();
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder buffer = tessellator.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      float r = (float)(color >> 16 & 0xFF) / 255.0F;
      float g = (float)(color >> 8 & 0xFF) / 255.0F;
      float b = (float)(color & 0xFF) / 255.0F;
      float a = this.fillOpacity.getFloatValue() * Math.max(0.0F, Math.min(1.0F, visibility));
      this.putFace(buffer, matrix, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ, r, g, b, a);
      this.putFace(buffer, matrix, minX, maxY, minZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, r, g, b, a);
      this.putFace(buffer, matrix, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, maxX, minY, minZ, r, g, b, a);
      this.putFace(buffer, matrix, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, a);
      this.putFace(buffer, matrix, minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, r, g, b, a);
      this.putFace(buffer, matrix, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, maxX, minY, maxZ, r, g, b, a);
      BufferRenderer.drawWithGlobalProgram(buffer.end());
      RenderSystem.enableCull();
      RenderSystem.enableDepthTest();
      RenderSystem.disableBlend();
   }

   private ShaderProgramKey getOverlayShader() {
      String var1 = this.shaderMode.getValue();

      return switch (var1) {
         case "Wave" -> WAVE_SHADER;
         case "Crystal" -> CRYSTAL_SHADER;
         case "Galaxy" -> GALAXY_SHADER;
         case "Lightning" -> LIGHTNING_SHADER;
         default -> ENERGY_SHADER;
      };
   }

   private void putFace(
      BufferBuilder buffer,
      Matrix4f matrix,
      double x1,
      double y1,
      double z1,
      double x2,
      double y2,
      double z2,
      double x3,
      double y3,
      double z3,
      double x4,
      double y4,
      double z4,
      float r,
      float g,
      float b,
      float a
   ) {
      buffer.vertex(matrix, (float)x1, (float)y1, (float)z1).texture(0.0F, 0.0F).color(r, g, b, a);
      buffer.vertex(matrix, (float)x2, (float)y2, (float)z2).texture(1.0F, 0.0F).color(r, g, b, a);
      buffer.vertex(matrix, (float)x3, (float)y3, (float)z3).texture(1.0F, 1.0F).color(r, g, b, a);
      buffer.vertex(matrix, (float)x4, (float)y4, (float)z4).texture(0.0F, 1.0F).color(r, g, b, a);
   }
}
