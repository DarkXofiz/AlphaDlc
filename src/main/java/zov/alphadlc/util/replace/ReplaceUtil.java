package zov.alphadlc.util.replace;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.text.PlainTextContent.Literal;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.ColorHelper;

public class ReplaceUtil {
   public static Text replace(Text input, String target, String replacement) {
      if (input != null && target != null && replacement != null) {
         MutableText result = Text.method_43473().method_10862(input.method_10866());
         appendReplaced(result, input, target, replacement);
         return result;
      } else {
         return input;
      }
   }

   private static void appendReplaced(MutableText result, Text current, String target, String replacement) {
      TextContent content = current.method_10851();
      Style style = current.method_10866();
      if (content instanceof Literal literal) {
         Pattern pattern = Pattern.compile(Pattern.quote(target), 2);
         String replaced = pattern.matcher(literal.comp_737()).replaceAll(replacement);
         result.method_10852(Text.method_43470(replaced).method_10862(style));
      } else if (!(content instanceof Literal)) {
         String str = content.toString();
         if (!str.isEmpty()) {
            result.method_10852(MutableText.method_43477(content).method_10862(style));
         }
      }

      for (Text sibling : current.method_10855()) {
         appendReplaced(result, sibling, target, replacement);
      }
   }

   public static Text replaceWithFormatted(Text input, String target, Formatting color, String label) {
      if (input != null && target != null) {
         MutableText result = Text.method_43473().method_10862(input.method_10866());
         appendReplacedFormatted(result, input, target, color, label);
         return result;
      } else {
         return input;
      }
   }

   private static void appendReplacedFormatted(MutableText result, Text current, String target, Formatting color, String label) {
      TextContent content = current.method_10851();
      Style style = current.method_10866();
      if (content instanceof Literal literal) {
         String s = literal.comp_737();
         String[] parts = s.split(Pattern.quote(target), -1);

         for (int i = 0; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
               result.method_10852(Text.method_43470(parts[i]).method_10862(style));
            }

            if (i < parts.length - 1) {
               result.method_10852(buildGradientLabel(label, color));
            }
         }
      } else if (content != null && !(content instanceof Literal)) {
         String str = content.toString();
         if (!str.isEmpty()) {
            result.method_10852(MutableText.method_43477(content).method_10862(style));
         }
      }

      for (Text sibling : current.method_10855()) {
         appendReplacedFormatted(result, sibling, target, color, label);
      }
   }

   private static MutableText buildGradientLabel(String label, Formatting color) {
      int targetRgb = formattingToRgb(color);
      int tr = targetRgb >> 16 & 0xFF;
      int tg = targetRgb >> 8 & 0xFF;
      int tb = targetRgb & 0xFF;
      int er = (int)((double)tr * 0.6);
      int eg = (int)((double)tg * 0.6);
      int eb = (int)((double)tb * 0.6);
      MutableText result = Text.method_43473();
      int len = label.length();

      for (int i = 0; i < len; i++) {
         float t = len == 1 ? 1.0F : (float)i / (float)(len - 1);
         int r = tr + (int)((float)(er - tr) * t);
         int g = tg + (int)((float)(eg - tg) * t);
         int b = tb + (int)((float)(eb - tb) * t);
         int rgb = r << 16 | g << 8 | b;
         result.method_10852(Text.method_43470(String.valueOf(label.charAt(i))).method_10862(Style.field_24360.method_36139(ColorHelper.method_61334(rgb))));
      }

      return result;
   }

   private static int formattingToRgb(Formatting formatting) {
      return switch (formatting) {
         case field_1074 -> 0;
         case field_1058 -> 170;
         case field_1077 -> 43520;
         case field_1062 -> 43690;
         case field_1079 -> 11141120;
         case field_1064 -> 11141290;
         case field_1065 -> 16755200;
         case field_1080 -> 11184810;
         case field_1063 -> 5592405;
         case field_1078 -> 5592575;
         case field_1060 -> 5635925;
         case field_1075 -> 5636095;
         case field_1061 -> 16733525;
         case field_1076 -> 16733695;
         case field_1054 -> 16777045;
         case field_1068 -> 16777215;
         default -> 16777215;
      };
   }

   public static Text replaceLiteral(Text input, String target, String replacement) {
      if (input == null) {
         return null;
      } else {
         String full = input.getString();
         if (!full.toLowerCase().contains(target.toLowerCase())) {
            return input;
         } else {
            full = full.replaceAll("(?i)" + Pattern.quote(target), replacement);
            MutableText out = Text.method_43473();
            List<ReplaceUtil.StyledChar> styledChars = flatten(input);
            int index = 0;

            for (int i = 0; i < full.length(); i++) {
               Style style = index < styledChars.size() ? styledChars.get(index).style : Style.field_24360;
               out.method_10852(Text.method_43470(String.valueOf(full.charAt(i))).method_10862(style));
               index++;
            }

            return out;
         }
      }
   }

   private static List<ReplaceUtil.StyledChar> flatten(Text text) {
      List<ReplaceUtil.StyledChar> list = new ArrayList<>();
      collect(text, list);
      return list;
   }

   private static void collect(Text text, List<ReplaceUtil.StyledChar> list) {
      Style style = text.method_10866();
      if (text.method_10851() instanceof Literal literal) {
         String s = literal.comp_737();

         for (int i = 0; i < s.length(); i++) {
            list.add(new ReplaceUtil.StyledChar(s.charAt(i), style));
         }
      }

      for (Text sibling : text.method_10855()) {
         collect(sibling, list);
      }
   }

   public static Text trimText(Text text, int length) {
      List<ReplaceUtil.StyledChar> chars = flatten(text);
      MutableText result = Text.method_43473();

      for (int i = 0; i < Math.min(length, chars.size()); i++) {
         result.method_10852(Text.method_43470(String.valueOf(chars.get(i).c())).method_10862(chars.get(i).style()));
      }

      return result;
   }

   public static String replaceSymbols(String string) {
      return string.replaceAll("\ua517", Formatting.field_1078 + "MODER")
         .replaceAll("\ua525", Formatting.field_1078 + "ST.MODER")
         .replaceAll("\ua521", Formatting.field_1076 + "MODER+")
         .replaceAll("\ua500", Formatting.field_1080 + "\u0418\u0413\u0420\u041e\u041a")
         .replaceAll("\ua509", Formatting.field_1054 + "HELPER")
         .replaceAll("\u25c6", "@")
         .replaceAll("\u2503", "|")
         .replaceAll("\ua533", Formatting.field_1075 + "ML.ADMIN")
         .replaceAll("\ua505", Formatting.field_1061 + "Y" + Formatting.field_1068 + "T")
         .replaceAll("\ua502", Formatting.field_1065 + "GOD")
         .replaceAll("\ua538", Formatting.field_1065 + "GOD")
         .replaceAll("\ua560", Formatting.field_1054 + "D.HELPER")
         .replaceAll("\ua544", Formatting.field_1061 + "VAMPIRE")
         .replaceAll("\ua516", Formatting.field_1075 + "OVERLORD")
         .replaceAll("\ua548", Formatting.field_1060 + "COBRA")
         .replaceAll("\ua528", Formatting.field_1076 + "DRAGON")
         .replaceAll("\ua524", Formatting.field_1061 + "IMPERATOR")
         .replaceAll("\ua520", Formatting.field_1065 + "MAGISTER")
         .replaceAll("\ua504", Formatting.field_1078 + "HERO")
         .replaceAll("\ua512", Formatting.field_1060 + "AVENGER")
         .replaceAll("\ua552", Formatting.field_1068 + "RABBIT")
         .replaceAll("\ua508", Formatting.field_1054 + "TITAN")
         .replaceAll("\ua540", Formatting.field_1077 + "HYDRA")
         .replaceAll("\ua536", Formatting.field_1065 + "TIGER")
         .replaceAll("\ua532", Formatting.field_1064 + "BULL")
         .replaceAll("\ua556", Formatting.field_1074 + "BUNNY")
         .replaceAll("\ua557\ua558", Formatting.field_1054 + "SPONSOR")
         .replaceAll("\ua549", Formatting.field_1065 + "PEGAS")
         .replaceAll("\ua546", Formatting.field_1065 + "PEGAS")
         .replaceAll("\ua553", Formatting.field_1063 + "GHOST")
         .replaceAll("\ua506", Formatting.field_1080 + "D.MODER")
         .replaceAll("\ua530", Formatting.field_1080 + "D.ML.ADMIN")
         .replaceAll("\ua510", Formatting.field_1058 + "D.GL.MODER")
         .replaceAll("\ua514", Formatting.field_1080 + "D.GL.MODER")
         .replaceAll("\ua522", Formatting.field_1080 + "D.ST.MODER")
         .replaceAll("\ua561", Formatting.field_1065 + "ST.HELPER")
         .replaceAll("\ua545", Formatting.field_1064 + "MEDIA+")
         .replaceAll("\ua513", Formatting.field_1078 + "ML.MODER")
         .replaceAll("\ua529", Formatting.field_1064 + "GL.MODER")
         .replaceAll("\ua557", Formatting.field_1079 + "D.ADMIN")
         .replaceAll("\ua518", Formatting.field_1078 + "D.ST.MODER")
         .replaceAll("\ua526", Formatting.field_1078 + "D.ML.ADMIN")
         .replaceAll("\ua501", Formatting.field_1064 + "MEDIA")
         .replaceAll("\ud83d\udd25", "@")
         .replaceAll("\u1d00", "A")
         .replaceAll("\u0299", "B")
         .replaceAll("\u1d04", "C")
         .replaceAll("\u1d05", "D")
         .replaceAll("\u1d07", "E")
         .replaceAll("\u0493", "F")
         .replaceAll("\u0262", "G")
         .replaceAll("\u029c", "H")
         .replaceAll("\u026a", "I")
         .replaceAll("\u1d0a", "J")
         .replaceAll("\u1d0b", "K")
         .replaceAll("\u029f", "L")
         .replaceAll("\u1d0d", "M")
         .replaceAll("\u0274", "N")
         .replaceAll("\ua731", "S")
         .replaceAll("\u1d0f", "O")
         .replaceAll("\u1d18", "P")
         .replaceAll("\u01eb", "Q")
         .replaceAll("\u0280", "R")
         .replaceAll("\u1d1b", "T")
         .replaceAll("\u1d1c", "U")
         .replaceAll("\u1d20", "V")
         .replaceAll("\u1d21", "W")
         .replaceAll("\ua730", "F")
         .replaceAll("\u028f", "Y")
         .replaceAll("\u1d22", "Z");
   }

   public static Text replaceSymbols(Text text) {
      if (text.getString().contains("\ua517")) {
         text = replaceWithFormatted(text, "\ua517", Formatting.field_1078, "MODER");
      }

      if (text.getString().contains("\ua525")) {
         text = replaceWithFormatted(text, "\ua525", Formatting.field_1078, "ST.MODER");
      }

      if (text.getString().contains("\ua521")) {
         text = replaceWithFormatted(text, "\ua521", Formatting.field_1076, "MODER+");
      }

      if (text.getString().contains("\ua500")) {
         text = replaceWithFormatted(text, "\ua500", Formatting.field_1080, "\u0418\u0413\u0420\u041e\u041a");
      }

      if (text.getString().contains("\ua509")) {
         text = replaceWithFormatted(text, "\ua509", Formatting.field_1054, "HELPER");
      }

      if (text.getString().contains("\u25c6")) {
         text = replace(text, "\u25c6", "@");
      }

      if (text.getString().contains("\u2503")) {
         text = replace(text, "\u2503", "|");
      }

      if (text.getString().contains("\ua533")) {
         text = replaceWithFormatted(text, "\ua533", Formatting.field_1075, "ML.ADMIN");
      }

      if (text.getString().contains("\ua505")) {
         text = replaceWithFormatted(text, "\ua505", Formatting.field_1061, "YT");
      }

      if (text.getString().contains("\ua502")) {
         text = replaceWithFormatted(text, "\ua502", Formatting.field_1065, "GOD");
      }

      if (text.getString().contains("\ua538")) {
         text = replaceWithFormatted(text, "\ua538", Formatting.field_1065, "GOD");
      }

      if (text.getString().contains("\ua560")) {
         text = replaceWithFormatted(text, "\ua560", Formatting.field_1054, "D.HELPER");
      }

      if (text.getString().contains("\ua544")) {
         text = replaceWithFormatted(text, "\ua544", Formatting.field_1061, "VAMPIRE");
      }

      if (text.getString().contains("\ua516")) {
         text = replaceWithFormatted(text, "\ua516", Formatting.field_1075, "OVERLORD");
      }

      if (text.getString().contains("\ua548")) {
         text = replaceWithFormatted(text, "\ua548", Formatting.field_1060, "COBRA");
      }

      if (text.getString().contains("\ua528")) {
         text = replaceWithFormatted(text, "\ua528", Formatting.field_1076, "DRAGON");
      }

      if (text.getString().contains("\ua524")) {
         text = replaceWithFormatted(text, "\ua524", Formatting.field_1061, "IMPERATOR");
      }

      if (text.getString().contains("\ua520")) {
         text = replaceWithFormatted(text, "\ua520", Formatting.field_1065, "MAGISTER");
      }

      if (text.getString().contains("\ua504")) {
         text = replaceWithFormatted(text, "\ua504", Formatting.field_1078, "HERO");
      }

      if (text.getString().contains("\ua512")) {
         text = replaceWithFormatted(text, "\ua512", Formatting.field_1060, "AVENGER");
      }

      if (text.getString().contains("\ua552")) {
         text = replaceWithFormatted(text, "\ua552", Formatting.field_1068, "RABBIT");
      }

      if (text.getString().contains("\ua508")) {
         text = replaceWithFormatted(text, "\ua508", Formatting.field_1054, "TITAN");
      }

      if (text.getString().contains("\ua540")) {
         text = replaceWithFormatted(text, "\ua540", Formatting.field_1077, "HYDRA");
      }

      if (text.getString().contains("\ua536")) {
         text = replaceWithFormatted(text, "\ua536", Formatting.field_1065, "TIGER");
      }

      if (text.getString().contains("\ua532")) {
         text = replaceWithFormatted(text, "\ua532", Formatting.field_1064, "BULL");
      }

      if (text.getString().contains("\ua556")) {
         text = replaceWithFormatted(text, "\ua556", Formatting.field_1080, "BUNNY");
      }

      if (text.getString().contains("\ua557\ua558")) {
         text = replaceWithFormatted(text, "\ua557\ua558", Formatting.field_1054, "SPONSOR");
      }

      if (text.getString().contains("\ua549")) {
         text = replaceWithFormatted(text, "\ua549", Formatting.field_1065, "PEGAS");
      }

      if (text.getString().contains("\ua546")) {
         text = replaceWithFormatted(text, "\ua546", Formatting.field_1065, "PEGAS");
      }

      if (text.getString().contains("\ua553")) {
         text = replaceWithFormatted(text, "\ua553", Formatting.field_1063, "GHOST");
      }

      if (text.getString().contains("\ua506")) {
         text = replaceWithFormatted(text, "\ua506", Formatting.field_1080, "D.MODER");
      }

      if (text.getString().contains("\ua530")) {
         text = replaceWithFormatted(text, "\ua530", Formatting.field_1080, "D.ML.ADMIN");
      }

      if (text.getString().contains("\ua510")) {
         text = replaceWithFormatted(text, "\ua510", Formatting.field_1058, "D.GL.MODER");
      }

      if (text.getString().contains("\ua514")) {
         text = replaceWithFormatted(text, "\ua514", Formatting.field_1080, "D.GL.MODER");
      }

      if (text.getString().contains("\ua522")) {
         text = replaceWithFormatted(text, "\ua522", Formatting.field_1080, "D.ST.MODER");
      }

      if (text.getString().contains("\ua561")) {
         text = replaceWithFormatted(text, "\ua561", Formatting.field_1065, "ST.HELPER");
      }

      if (text.getString().contains("\ua545")) {
         text = replaceWithFormatted(text, "\ua545", Formatting.field_1064, "MEDIA+");
      }

      if (text.getString().contains("\ua513")) {
         text = replaceWithFormatted(text, "\ua513", Formatting.field_1078, "ML.MODER");
      }

      if (text.getString().contains("\ua529")) {
         text = replaceWithFormatted(text, "\ua529", Formatting.field_1064, "GL.MODER");
      }

      if (text.getString().contains("\ua557")) {
         text = replaceWithFormatted(text, "\ua557", Formatting.field_1079, "D.ADMIN");
      }

      if (text.getString().contains("\ua518")) {
         text = replaceWithFormatted(text, "\ua518", Formatting.field_1078, "D.ST.MODER");
      }

      if (text.getString().contains("\ua526")) {
         text = replaceWithFormatted(text, "\ua526", Formatting.field_1078, "D.ML.ADMIN");
      }

      if (text.getString().contains("\ua501")) {
         text = replaceWithFormatted(text, "\ua501", Formatting.field_1064, "MEDIA");
      }

      if (text.getString().contains("\ud83d\udd25")) {
         text = replace(text, "\ud83d\udd25", "@");
      }

      if (text.getString().contains("\u1d00")) {
         text = replace(text, "\u1d00", "A");
      }

      if (text.getString().contains("\u0299")) {
         text = replace(text, "\u0299", "B");
      }

      if (text.getString().contains("\u1d04")) {
         text = replace(text, "\u1d04", "C");
      }

      if (text.getString().contains("\u1d05")) {
         text = replace(text, "\u1d05", "D");
      }

      if (text.getString().contains("\u1d07")) {
         text = replace(text, "\u1d07", "E");
      }

      if (text.getString().contains("\u0493")) {
         text = replace(text, "\u0493", "F");
      }

      if (text.getString().contains("\u0262")) {
         text = replace(text, "\u0262", "G");
      }

      if (text.getString().contains("\u029c")) {
         text = replace(text, "\u029c", "H");
      }

      if (text.getString().contains("\u026a")) {
         text = replace(text, "\u026a", "I");
      }

      if (text.getString().contains("\u1d0a")) {
         text = replace(text, "\u1d0a", "J");
      }

      if (text.getString().contains("\u1d0b")) {
         text = replace(text, "\u1d0b", "K");
      }

      if (text.getString().contains("\u029f")) {
         text = replace(text, "\u029f", "L");
      }

      if (text.getString().contains("\u1d0d")) {
         text = replace(text, "\u1d0d", "M");
      }

      if (text.getString().contains("\u0274")) {
         text = replace(text, "\u0274", "N");
      }

      if (text.getString().contains("\ua731")) {
         text = replace(text, "\ua731", "S");
      }

      if (text.getString().contains("\u1d0f")) {
         text = replace(text, "\u1d0f", "O");
      }

      if (text.getString().contains("\u1d18")) {
         text = replace(text, "\u1d18", "P");
      }

      if (text.getString().contains("\u01eb")) {
         text = replace(text, "\u01eb", "Q");
      }

      if (text.getString().contains("\u0280")) {
         text = replace(text, "\u0280", "R");
      }

      if (text.getString().contains("\u1d1b")) {
         text = replace(text, "\u1d1b", "T");
      }

      if (text.getString().contains("\u1d1c")) {
         text = replace(text, "\u1d1c", "U");
      }

      if (text.getString().contains("\u1d20")) {
         text = replace(text, "\u1d20", "V");
      }

      if (text.getString().contains("\u1d21")) {
         text = replace(text, "\u1d21", "W");
      }

      if (text.getString().contains("\ua730")) {
         text = replace(text, "\ua730", "F");
      }

      if (text.getString().contains("\u028f")) {
         text = replace(text, "\u028f", "Y");
      }

      if (text.getString().contains("\u1d22")) {
         text = replace(text, "\u1d22", "Z");
      }

      return text;
   }

   public static String toQwerty(String text) {
      return text.replace("\u0439", "q")
         .replace("\u0446", "w")
         .replace("\u0443", "e")
         .replace("\u043a", "r")
         .replace("\u0435", "t")
         .replace("\u043d", "y")
         .replace("\u0433", "u")
         .replace("\u0448", "i")
         .replace("\u0449", "o")
         .replace("\u0437", "p")
         .replace("\u0445", "[")
         .replace("\u044a", "]")
         .replace("\u0444", "a")
         .replace("\u044b", "s")
         .replace("\u0432", "d")
         .replace("\u0430", "f")
         .replace("\u043f", "g")
         .replace("\u0440", "h")
         .replace("\u043e", "j")
         .replace("\u043b", "k")
         .replace("\u0434", "l")
         .replace("\u0436", ";")
         .replace("\u044d", "'")
         .replace("\u044f", "z")
         .replace("\u0447", "x")
         .replace("\u0441", "c")
         .replace("\u043c", "v")
         .replace("\u0438", "b")
         .replace("\u0442", "n")
         .replace("\u044c", "m")
         .replace("\u0431", ",")
         .replace("\u044e", ".")
         .replace("\u0451", "`")
         .replace("\u0419", "Q")
         .replace("\u0426", "W")
         .replace("\u0423", "E")
         .replace("\u041a", "R")
         .replace("\u0415", "T")
         .replace("\u041d", "Y")
         .replace("\u0413", "U")
         .replace("\u0428", "I")
         .replace("\u0429", "O")
         .replace("\u0417", "P")
         .replace("\u0425", "{")
         .replace("\u042a", "}")
         .replace("\u0424", "A")
         .replace("\u042b", "S")
         .replace("\u0412", "D")
         .replace("\u0410", "F")
         .replace("\u041f", "G")
         .replace("\u0420", "H")
         .replace("\u041e", "J")
         .replace("\u041b", "K")
         .replace("\u0414", "L")
         .replace("\u0416", ":")
         .replace("\u042d", "\"")
         .replace("\u042f", "Z")
         .replace("\u0427", "X")
         .replace("\u0421", "C")
         .replace("\u041c", "V")
         .replace("\u0418", "B")
         .replace("\u0422", "N")
         .replace("\u042c", "M")
         .replace("\u0411", "<")
         .replace("\u042e", ">")
         .replace("\u0401", "~");
   }

   public static String getDonatorRankSymbol(String rank) {
      String var1 = rank.toLowerCase();

      return switch (var1) {
         case "admin" -> "\ua533";
         case "moder" -> "\ua517";
         case "yt" -> "\ua505";
         case "pegas" -> "\ua549";
         case "helper" -> "\ua509";
         case "media" -> "\ua501";
         case "god" -> "\ua502";
         default -> "\ua500";
      };
   }

   private static record StyledChar(char c, Style style) {
   }
}
