package zov.alphadlc.util;

import java.util.Arrays;
import java.util.stream.Stream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public interface QuickLogger {
   static Text getPrefix() {
      MutableText onetap = Text.method_43470("alphadlc DLC");
      onetap.method_10862(onetap.method_10866().method_10977(Formatting.field_1068));
      MutableText prefix = Text.method_43470("");
      prefix.method_10862(onetap.method_10866().method_10977(Formatting.field_1063));
      prefix.method_10852(onetap);
      prefix.method_27693(" -> ");
      return prefix;
   }

   default void logDirect(String message, Object... args) {
      this.logDirect(message.formatted(args));
   }

   default void logDirect(Text... components) {
      MutableText component = Text.method_43470("");
      component.method_10852(getPrefix());
      component.method_10852(Text.method_43470(" "));
      Arrays.asList(components).forEach(component::method_10852);
      if (MinecraftClient.method_1551().field_1724 != null) {
         MinecraftClient.method_1551().field_1705.method_1743().method_1812(component);
      }
   }

   default void logDirect(String message, Formatting color) {
      Stream.of(message.split("\n")).forEach(line -> {
         MutableText component = Text.method_43470(line.replace("\t", "    "));
         component.method_10862(component.method_10866().method_10977(color));
         this.logDirect(component);
      });
   }

   default void logDirect(String message) {
      this.logDirect(message, Formatting.field_1080);
   }
}
