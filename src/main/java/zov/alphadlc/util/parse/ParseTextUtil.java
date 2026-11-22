package zov.alphadlc.util.parse;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.text.KeybindTextContent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.text.PlainTextContent.Literal;
import zov.alphadlc.util.IMinecraft;
import zov.alphadlc.util.render.msdf.MsdfFont;

public class ParseTextUtil implements IMinecraft {
   public static List<MsdfFont.ColoredGlyph> parseTextToColoredGlyphs(Text text) {
      List<MsdfFont.ColoredGlyph> result = new ArrayList<>();
      parseTextRecursive(text, -1, result);
      return result;
   }

   private static void parseTextRecursive(Text text, int currentColor, List<MsdfFont.ColoredGlyph> result) {
      Style style = text.method_10866();
      int color = style.method_10973() != null ? style.method_10973().method_27716() | 0xFF000000 : currentColor;
      TextContent content = text.method_10851();
      String raw = "";
      if (content instanceof Literal literal) {
         raw = literal.comp_737();
      } else if (content instanceof TranslatableTextContent translatable) {
         raw = translatable.method_11022();
      } else if (content instanceof KeybindTextContent keybind) {
         raw = keybind.method_10901();
      }

      for (int i = 0; i < raw.length(); i++) {
         char c = raw.charAt(i);
         if (c == 167 && i + 1 < raw.length()) {
            i++;
         } else {
            result.add(new MsdfFont.ColoredGlyph(c, color));
         }
      }

      for (Text sibling : text.method_10855()) {
         parseTextRecursive(sibling, color, result);
      }
   }
}
