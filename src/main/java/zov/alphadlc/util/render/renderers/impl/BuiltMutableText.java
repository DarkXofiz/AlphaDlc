package zov.alphadlc.util.render.renderers.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.text.KeybindTextContent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.text.PlainTextContent.Literal;
import net.minecraft.util.Formatting;
import org.joml.Matrix4f;
import zov.alphadlc.util.render.msdf.MsdfFont;
import zov.alphadlc.util.render.providers.ColorProvider;
import zov.alphadlc.util.render.providers.ResourceProvider;
import zov.alphadlc.util.render.renderers.IRenderer;

public record BuiltMutableText(
   MsdfFont font, Text text, float size, float thickness, int color, float smoothness, float spacing, int outlineColor, float outlineThickness, int alpha
) implements IRenderer {
   private static final ShaderProgramKey MSDF_FONT_SHADER_KEY = new ShaderProgramKey(
      ResourceProvider.getShaderIdentifier("msdf_font"), VertexFormats.field_1575, Defines.field_53930
   );

   public List<MsdfFont.ColoredGlyph> parseTextToColoredGlyphs(Text text) {
      List<MsdfFont.ColoredGlyph> result = new ArrayList<>();
      this.parseTextRecursivePreservingColors(text, -1, result);
      return result;
   }

   private void parseTextRecursivePreservingColors(Text text, int currentColor, List<MsdfFont.ColoredGlyph> result) {
      Style style = text.method_10866();
      int color = style.method_10973() != null
         ? ColorProvider.setAlpha(style.method_10973().method_27716(), this.alpha) | ColorProvider.setAlpha(-16777216, this.alpha)
         : ColorProvider.setAlpha(currentColor, this.alpha);
      String content = extractContent(text);
      int length = content.length();
      int i = 0;

      while (i < length) {
         char c = content.charAt(i);
         if (c == 167 && i + 1 < length) {
            char code = Character.toLowerCase(content.charAt(i + 1));
            Formatting formatting = Formatting.method_544(code);
            if (formatting != null && formatting.method_543()) {
               color = ColorProvider.setAlpha(formatting.method_532(), this.alpha) | ColorProvider.setAlpha(-16777216, this.alpha);
            } else if (formatting == Formatting.field_1070) {
               color = ColorProvider.setAlpha(currentColor, this.alpha);
            }

            i += 2;
         } else {
            result.add(new MsdfFont.ColoredGlyph(c, color));
            i++;
         }
      }

      for (Text sibling : text.method_10855()) {
         this.parseTextRecursivePreservingColors(sibling, color, result);
      }
   }

   private static String extractContent(Text text) {
      TextContent content = text.method_10851();
      if (content instanceof Literal literal) {
         return literal.comp_737();
      } else if (content instanceof TranslatableTextContent translatable) {
         return translatable.method_11022();
      } else {
         return content instanceof KeybindTextContent keybind ? keybind.method_10901() : "";
      }
   }

   @Override
   public void render(Matrix4f matrix, float x, float y, float z) {
      List<MsdfFont.ColoredGlyph> glyphs = this.parseTextToColoredGlyphs(this.text);
      if (!glyphs.isEmpty()) {
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
               this.parseTextToColoredGlyphs(this.text),
               this.size,
               (this.thickness + this.outlineThickness * 0.5F) * 0.5F * this.size,
               this.spacing,
               x,
               y + this.font.getMetrics().baselineHeight() * this.size,
               z
            );
         if (builder.field_1556) {
            BufferRenderer.method_43433(builder.method_60800());
         }

         RenderSystem.setShaderTexture(0, 0);
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
      }
   }
}
