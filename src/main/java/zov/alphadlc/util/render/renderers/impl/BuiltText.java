package zov.alphadlc.util.render.renderers.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import org.joml.Matrix4f;
import zov.alphadlc.util.render.msdf.MsdfFont;
import zov.alphadlc.util.render.providers.ColorProvider;
import zov.alphadlc.util.render.providers.ResourceProvider;
import zov.alphadlc.util.render.renderers.IRenderer;

public record BuiltText(
   MsdfFont font, String text, float size, float thickness, int color, float smoothness, float spacing, int outlineColor, float outlineThickness
) implements IRenderer {
   private static final ShaderProgramKey MSDF_FONT_SHADER_KEY = new ShaderProgramKey(
      ResourceProvider.getShaderIdentifier("msdf_font"), VertexFormats.field_1575, Defines.field_53930
   );

   @Override
   public void render(Matrix4f matrix, float x, float y, float z) {
      try {
         if (this.text.isEmpty()) {
            return;
         }

         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableCull();
         RenderSystem.setShaderTexture(0, this.font.getTextureId());
         boolean outlineEnabled = this.outlineThickness > 0.0F;
         ShaderProgram shader = RenderSystem.setShader(MSDF_FONT_SHADER_KEY);
         shader.method_34582("Range").method_1251(this.font.getAtlas().range());
         shader.method_34582("Thickness").method_1251(this.thickness);
         shader.method_34582("Smoothness").method_1251(this.smoothness);
         shader.method_34582("Outline").method_35649(outlineEnabled ? 1 : 0);
         if (outlineEnabled) {
            shader.method_34582("OutlineThickness").method_1251(this.outlineThickness);
            float[] outlineComponents = ColorProvider.normalize(this.outlineColor);
            shader.method_34582("OutlineColor").method_35657(outlineComponents[0], outlineComponents[1], outlineComponents[2], outlineComponents[3]);
         }

         BufferBuilder builder = Tessellator.method_1348().method_60827(DrawMode.field_27382, VertexFormats.field_1575);
         this.font
            .applyGlyphs(
               matrix,
               builder,
               this.text,
               this.size,
               (this.thickness + this.outlineThickness * 0.5F) * 0.5F * this.size,
               this.spacing,
               x,
               y + this.font.getMetrics().baselineHeight() * this.size,
               z,
               this.color
            );
         BufferRenderer.method_43433(builder.method_60800());
         RenderSystem.setShaderTexture(0, 0);
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
      } catch (Exception var8) {
      }
   }
}
