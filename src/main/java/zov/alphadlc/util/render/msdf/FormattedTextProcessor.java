package zov.alphadlc.util.render.msdf;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

public class FormattedTextProcessor {
   public static List<FormattedTextProcessor.TextSegment> processText(Text text, int defaultColor) {
      List<FormattedTextProcessor.TextSegment> segments = new ArrayList<>();
      text.method_27658((style, string) -> {
         if (!string.isEmpty()) {
            int color = extractColor(style, defaultColor);
            boolean bold = style.method_10984();
            boolean italic = style.method_10966();
            boolean underlined = style.method_10965();
            boolean strikethrough = style.method_10986();
            segments.add(new FormattedTextProcessor.TextSegment(string, color, bold, italic, underlined, strikethrough));
         }

         return Optional.empty();
      }, Style.field_24360);
      return segments;
   }

   private static int extractColor(Style style, int defaultColor) {
      TextColor textColor = style.method_10973();
      return textColor != null ? textColor.method_27716() | 0xFF000000 : defaultColor;
   }

   public static record TextSegment(String text, int color, boolean bold, boolean italic, boolean underlined, boolean strikethrough) {
   }
}
