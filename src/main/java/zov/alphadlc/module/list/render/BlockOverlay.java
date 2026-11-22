package zov.alphadlc.module.list.render;

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
import zov.alphadlc.event.list.EventWorldRender;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.module.settings.ColorSetting;
import zov.alphadlc.module.settings.ModeSetting;
import zov.alphadlc.module.settings.SliderSetting;
import zov.alphadlc.util.render.providers.ResourceProvider;

@ModuleInformation(
   moduleName = "Block Overlay",
   moduleDesc = "\u041a\u0430\u0441\u0442\u043e\u043c\u043d\u0430\u044f \u043f\u043e\u0434\u0441\u0432\u0435\u0442\u043a\u0430 \u0431\u043b\u043e\u043a\u0430",
   moduleCategory = ModuleCategory.RENDER
)
public class BlockOverlay extends Module {
   private static final ShaderProgramKey ENERGY_SHADER = shaderKey("block_overlay");
   private static final ShaderProgramKey WAVE_SHADER = shaderKey("block_overlay_wave");
   private static final ShaderProgramKey CRYSTAL_SHADER = shaderKey("block_overlay_crystal");
   private static final ShaderProgramKey GALAXY_SHADER = shaderKey("block_overlay_galaxy");
   private static final ShaderProgramKey LIGHTNING_SHADER = shaderKey("block_overlay_lightning");
   public final ModeSetting shaderMode = new ModeSetting("\u0428\u0435\u0439\u0434\u0435\u0440", "Energy", "Energy", "Wave", "Crystal", "Galaxy", "Lightning");
   public final ColorSetting overlayColor = new ColorSetting("\u0426\u0432\u0435\u0442", -9673774);
   public final SliderSetting fillOpacity = new SliderSetting(
      "\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c \u0437\u0430\u043b\u0438\u0432\u043a\u0438", 0.32, 0.0, 1.0, 0.05
   );
   public final BooleanSetting outline = new BooleanSetting("\u041e\u0431\u0432\u043e\u0434\u043a\u0430", true);
   public final SliderSetting outlineWidth = new SliderSetting(
         "\u0422\u043e\u043b\u0449\u0438\u043d\u0430 \u043e\u0431\u0432\u043e\u0434\u043a\u0438", 2.0, 0.5, 5.0, 0.1
      )
      .setVisible(() -> this.outline.getValue());
   public final SliderSetting outlineOpacity = new SliderSetting(
         "\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c \u043e\u0431\u0432\u043e\u0434\u043a\u0438", 1.0, 0.0, 1.0, 0.05
      )
      .setVisible(() -> this.outline.getValue());
   public final SliderSetting smoothness = new SliderSetting("\u041f\u043b\u0430\u0432\u043d\u043e\u0441\u0442\u044c", 6.0, 1.0, 10.0, 1.0);
   private Box currentBox;
   private long lastFrameTime;
   private Object lastWorld;
   private float visibility;

   private static ShaderProgramKey shaderKey(String name) {
      return new ShaderProgramKey(ResourceProvider.getShaderIdentifier(name), VertexFormats.field_1575, Defines.field_53930);
   }

   @Subscribe
   public void onWorldRender(EventWorldRender event) {
      if (this.mc.field_1687 != null && this.mc.field_1724 != null) {
         long now = System.nanoTime();
         float dt = this.lastFrameTime == 0L ? 0.016666668F : Math.min(0.1F, (float)(now - this.lastFrameTime) / 1.0E9F);
         this.lastFrameTime = now;
         Box targetBox = this.getTargetBox();
         if (this.lastWorld != this.mc.field_1687) {
            this.lastWorld = this.mc.field_1687;
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
            Vec3d camPos = this.mc.field_1773.method_19418().method_19326();
            matrices.method_22903();
            double minX = this.currentBox.field_1323 - camPos.field_1352;
            double minY = this.currentBox.field_1322 - camPos.field_1351;
            double minZ = this.currentBox.field_1321 - camPos.field_1350;
            double maxX = this.currentBox.field_1320 - camPos.field_1352;
            double maxY = this.currentBox.field_1325 - camPos.field_1351;
            double maxZ = this.currentBox.field_1324 - camPos.field_1350;
            this.drawFilled(matrices, minX, minY, minZ, maxX, maxY, maxZ, color, this.visibility);
            if (this.outline.getValue()) {
               this.drawOutline(matrices, minX, minY, minZ, maxX, maxY, maxZ, color, this.visibility);
            }

            matrices.method_22909();
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
      if (this.mc.field_1765 != null && this.mc.field_1765.method_17783() == Type.field_1332) {
         BlockPos pos = ((BlockHitResult)this.mc.field_1765).method_17777();
         BlockState state = this.mc.field_1687.method_8320(pos);
         if (state.method_26215()) {
            return null;
         } else {
            VoxelShape shape = state.method_26218(this.mc.field_1687, pos);
            return shape.method_1110() ? null : shape.method_1107().method_996(pos);
         }
      } else {
         return null;
      }
   }

   private Box lerpBox(Box from, Box to, float delta) {
      return new Box(
         from.field_1323 + (to.field_1323 - from.field_1323) * (double)delta,
         from.field_1322 + (to.field_1322 - from.field_1322) * (double)delta,
         from.field_1321 + (to.field_1321 - from.field_1321) * (double)delta,
         from.field_1320 + (to.field_1320 - from.field_1320) * (double)delta,
         from.field_1325 + (to.field_1325 - from.field_1325) * (double)delta,
         from.field_1324 + (to.field_1324 - from.field_1324) * (double)delta
      );
   }

   private void drawOutline(MatrixStack matrices, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, int color, float visibility) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableDepthTest();
      RenderSystem.setShader(ShaderProgramKeys.field_53876);
      RenderSystem.lineWidth(this.outlineWidth.getFloatValue());
      Matrix4f matrix = matrices.method_23760().method_23761();
      Tessellator tessellator = Tessellator.method_1348();
      BufferBuilder buffer = tessellator.method_60827(DrawMode.field_29344, VertexFormats.field_1576);
      float r = (float)(color >> 16 & 0xFF) / 255.0F;
      float g = (float)(color >> 8 & 0xFF) / 255.0F;
      float b = (float)(color & 0xFF) / 255.0F;
      float outlineAlpha = this.outlineOpacity.getFloatValue() * Math.max(0.0F, Math.min(1.0F, visibility));
      buffer.method_22918(matrix, (float)minX, (float)minY, (float)minZ).method_22915(r, g, b, outlineAlpha);
      buffer.method_22918(matrix, (float)maxX, (float)minY, (float)minZ).method_22915(r, g, b, outlineAlpha);
      buffer.method_22918(matrix, (float)maxX, (float)minY, (float)minZ).method_22915(r, g, b, outlineAlpha);
      buffer.method_22918(matrix, (float)maxX, (float)minY, (float)maxZ).method_22915(r, g, b, outlineAlpha);
      buffer.method_22918(matrix, (float)maxX, (float)minY, (float)maxZ).method_22915(r, g, b, outlineAlpha);
      buffer.method_22918(matrix, (float)minX, (float)minY, (float)maxZ).method_22915(r, g, b, outlineAlpha);
      buffer.method_22918(matrix, (float)minX, (float)minY, (float)maxZ).method_22915(r, g, b, outlineAlpha);
      buffer.method_22918(matrix, (float)minX, (float)minY, (float)minZ).method_22915(r, g, b, outlineAlpha);
      buffer.method_22918(matrix, (float)minX, (float)maxY, (float)minZ).method_22915(r, g, b, outlineAlpha);
      buffer.method_22918(matrix, (float)maxX, (float)maxY, (float)minZ).method_22915(r, g, b, outlineAlpha);
      buffer.method_22918(matrix, (float)maxX, (float)maxY, (float)minZ).method_22915(r, g, b, outlineAlpha);
      buffer.method_22918(matrix, (float)maxX, (float)maxY, (float)maxZ).method_22915(r, g, b, outlineAlpha);
      buffer.method_22918(matrix, (float)maxX, (float)maxY, (float)maxZ).method_22915(r, g, b, outlineAlpha);
      buffer.method_22918(matrix, (float)minX, (float)maxY, (float)maxZ).method_22915(r, g, b, outlineAlpha);
      buffer.method_22918(matrix, (float)minX, (float)maxY, (float)maxZ).method_22915(r, g, b, outlineAlpha);
      buffer.method_22918(matrix, (float)minX, (float)maxY, (float)minZ).method_22915(r, g, b, outlineAlpha);
      buffer.method_22918(matrix, (float)minX, (float)minY, (float)minZ).method_22915(r, g, b, outlineAlpha);
      buffer.method_22918(matrix, (float)minX, (float)maxY, (float)minZ).method_22915(r, g, b, outlineAlpha);
      buffer.method_22918(matrix, (float)maxX, (float)minY, (float)minZ).method_22915(r, g, b, outlineAlpha);
      buffer.method_22918(matrix, (float)maxX, (float)maxY, (float)minZ).method_22915(r, g, b, outlineAlpha);
      buffer.method_22918(matrix, (float)maxX, (float)minY, (float)maxZ).method_22915(r, g, b, outlineAlpha);
      buffer.method_22918(matrix, (float)maxX, (float)maxY, (float)maxZ).method_22915(r, g, b, outlineAlpha);
      buffer.method_22918(matrix, (float)minX, (float)minY, (float)maxZ).method_22915(r, g, b, outlineAlpha);
      buffer.method_22918(matrix, (float)minX, (float)maxY, (float)maxZ).method_22915(r, g, b, outlineAlpha);
      BufferRenderer.method_43433(buffer.method_60800());
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
         RenderSystem.setShader(ShaderProgramKeys.field_53880);
      } else if (shader.method_34582("Time") != null) {
         shader.method_34582("Time").method_1251((float)(System.currentTimeMillis() % 100000L) / 1000.0F);
      }

      Matrix4f matrix = matrices.method_23760().method_23761();
      Tessellator tessellator = Tessellator.method_1348();
      BufferBuilder buffer = tessellator.method_60827(DrawMode.field_27382, VertexFormats.field_1575);
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
      BufferRenderer.method_43433(buffer.method_60800());
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
      buffer.method_22918(matrix, (float)x1, (float)y1, (float)z1).method_22913(0.0F, 0.0F).method_22915(r, g, b, a);
      buffer.method_22918(matrix, (float)x2, (float)y2, (float)z2).method_22913(1.0F, 0.0F).method_22915(r, g, b, a);
      buffer.method_22918(matrix, (float)x3, (float)y3, (float)z3).method_22913(1.0F, 1.0F).method_22915(r, g, b, a);
      buffer.method_22918(matrix, (float)x4, (float)y4, (float)z4).method_22913(0.0F, 1.0F).method_22915(r, g, b, a);
   }
}
