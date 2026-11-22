package zov.alphadlc.util.render.msdf;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import lombok.Generated;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import zov.alphadlc.util.render.providers.ResourceProvider;

public final class MsdfRenderer {
   public static final ShaderProgramKey MSDF_FONT_SHADER_KEY = new ShaderProgramKey(
      ResourceProvider.getShaderIdentifier("msdf_font"), VertexFormats.field_1575, Defines.field_53930
   );

   public static void renderText(MsdfFont font, String text, float size, int color, Matrix4f matrix, float x, float y, float z) {
      renderText(font, text, size, color, matrix, x, y, z, false, 0.0F, 1.0F, 0.0F);
   }

   public static void renderText(
      MsdfFont font,
      String text,
      float size,
      int color,
      Matrix4f matrix,
      float x,
      float y,
      float z,
      boolean enableFadeout,
      float fadeoutStart,
      float fadeoutEnd,
      float maxWidth
   ) {
      float thickness = 0.05F;
      float smoothness = 0.5F;
      float spacing = 0.0F;
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      RenderSystem.setShaderTexture(0, font.getTextureId());
      ShaderProgram shader = RenderSystem.setShader(MSDF_FONT_SHADER_KEY);
      shader.method_34582("Range").method_1251(font.getAtlas().range());
      shader.method_34582("Thickness").method_1251(thickness);
      shader.method_34582("Smoothness").method_1251(0.5F);
      shader.method_34582("EnableFadeout").method_35649(enableFadeout ? 1 : 0);
      shader.method_34582("FadeoutStart").method_1251(fadeoutStart);
      shader.method_34582("FadeoutEnd").method_1251(fadeoutEnd);
      shader.method_34582("MaxWidth").method_1251(maxWidth);
      shader.method_34582("TextPosX").method_1251(x);
      BufferBuilder builder = Tessellator.method_1348().method_60827(DrawMode.field_27382, VertexFormats.field_1575);
      font.applyGlyphs(matrix, builder, text, size, thickness * 0.5F * size, spacing, x - 0.75F, y + size * 0.7F, z, color);
      BuiltBuffer builtBuffer = builder.method_60794();
      if (builtBuffer != null) {
         BufferRenderer.method_43433(builtBuffer);
      }

      RenderSystem.setShaderTexture(0, 0);
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
   }

   public static void renderText(
      MsdfFont font,
      String text,
      float size,
      int color,
      Matrix4f matrix,
      float x,
      float y,
      float z,
      boolean enableFadeout,
      float fadeoutStart,
      float fadeoutEnd
   ) {
      float maxWidth = font.getWidth(text, size) * 2.0F;
      renderText(font, text, size, color, matrix, x, y, z, enableFadeout, fadeoutStart, fadeoutEnd, maxWidth);
   }

   public static void renderText(MsdfFont font, Text text, float size, Matrix4f matrix, float x, float y, float z) {
      renderText(font, text, size, matrix, x, y, z, false, 0.0F, 1.0F, 0.0F);
   }

   public static void renderText(MsdfFont font, Text text, float size, Matrix4f matrix, float x, float y, float z, int alpha) {
      renderText(font, text, size, matrix, x, y, z, false, 0.0F, 1.0F, 0.0F, alpha);
   }

   public static void renderText(
      MsdfFont font,
      Text text,
      float size,
      Matrix4f matrix,
      float x,
      float y,
      float z,
      boolean enableFadeout,
      float fadeoutStart,
      float fadeoutEnd,
      float maxWidth
   ) {
      float thickness = 0.05F;
      float smoothness = 0.5F;
      float spacing = 0.0F;
      List<FormattedTextProcessor.TextSegment> segments = FormattedTextProcessor.processText(text, -1);
      float currentX = x;
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      RenderSystem.setShaderTexture(0, font.getTextureId());
      ShaderProgram shader = RenderSystem.setShader(MSDF_FONT_SHADER_KEY);
      shader.method_34582("Range").method_1251(font.getAtlas().range());
      shader.method_34582("Thickness").method_1251(thickness);
      shader.method_34582("Smoothness").method_1251(0.5F);
      shader.method_34582("EnableFadeout").method_35649(enableFadeout ? 1 : 0);
      shader.method_34582("FadeoutStart").method_1251(fadeoutStart);
      shader.method_34582("FadeoutEnd").method_1251(fadeoutEnd);
      shader.method_34582("MaxWidth").method_1251(maxWidth);
      shader.method_34582("TextPosX").method_1251(x);
      BufferBuilder builder = Tessellator.method_1348().method_60827(DrawMode.field_27382, VertexFormats.field_1575);

      for (FormattedTextProcessor.TextSegment segment : segments) {
         font.applyGlyphs(matrix, builder, segment.text(), size, thickness * 0.5F * size, spacing - 0.3F, currentX - 0.75F, y + size * 0.7F, z, segment.color());
         currentX += font.getWidth(segment.text(), size);
      }

      BuiltBuffer builtBuffer = builder.method_60794();
      if (builtBuffer != null) {
         BufferRenderer.method_43433(builtBuffer);
      }

      RenderSystem.setShaderTexture(0, 0);
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
   }

   public static void renderText(
      MsdfFont font,
      Text text,
      float size,
      Matrix4f matrix,
      float x,
      float y,
      float z,
      boolean enableFadeout,
      float fadeoutStart,
      float fadeoutEnd,
      float maxWidth,
      int alpha
   ) {
      float thickness = 0.05F;
      float smoothness = 0.5F;
      float spacing = 0.0F;
      List<FormattedTextProcessor.TextSegment> segments = FormattedTextProcessor.processText(text, -1);
      float currentX = x;
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      RenderSystem.setShaderTexture(0, font.getTextureId());
      ShaderProgram shader = RenderSystem.setShader(MSDF_FONT_SHADER_KEY);
      shader.method_34582("Range").method_1251(font.getAtlas().range());
      shader.method_34582("Thickness").method_1251(thickness);
      shader.method_34582("Smoothness").method_1251(0.5F);
      shader.method_34582("EnableFadeout").method_35649(enableFadeout ? 1 : 0);
      shader.method_34582("FadeoutStart").method_1251(fadeoutStart);
      shader.method_34582("FadeoutEnd").method_1251(fadeoutEnd);
      shader.method_34582("MaxWidth").method_1251(maxWidth);
      shader.method_34582("TextPosX").method_1251(x);
      BufferBuilder builder = Tessellator.method_1348().method_60827(DrawMode.field_27382, VertexFormats.field_1575);

      for (FormattedTextProcessor.TextSegment segment : segments) {
         int color = segment.color();
         if (alpha != 255) {
            color = color & 16777215 | (alpha & 0xFF) << 24;
         }

         font.applyGlyphs(matrix, builder, segment.text(), size, thickness * 0.5F * size, spacing - 0.3F, currentX - 0.75F, y + size * 0.7F, z, color);
         currentX += font.getWidth(segment.text(), size);
      }

      BuiltBuffer builtBuffer = builder.method_60794();
      if (builtBuffer != null) {
         BufferRenderer.method_43433(builtBuffer);
      }

      RenderSystem.setShaderTexture(0, 0);
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
   }

   public static void renderText(
      MsdfFont font, Text text, float size, Matrix4f matrix, float x, float y, float z, boolean enableFadeout, float fadeoutStart, float fadeoutEnd
   ) {
      float maxWidth = font.getWidth(text, size) * 2.0F;
      renderText(font, text, size, matrix, x, y, z, enableFadeout, fadeoutStart, fadeoutEnd, maxWidth);
   }

   @Generated
   private MsdfRenderer() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
