package zov.alphadlc.util.chat;

import java.util.Arrays;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import zov.alphadlc.util.IMinecraft;

public class ChatUtil implements IMinecraft {
   public static void send(Object message) {
      if (mc.field_1724 != null) {
         mc.field_1724.method_7353(Text.method_30163("alphadlc Dlc " + Formatting.field_1063 + "-> " + Formatting.field_1070 + message.toString()), false);
      }
   }

   public static void send(Object... messages) {
      if (mc.field_1724 != null) {
         mc.field_1724
            .method_7353(
               Text.method_30163("alphadlc Dlc " + Formatting.field_1063 + "-> " + Formatting.field_1070 + String.join(",", Arrays.toString(messages))), false
            );
      }
   }
}
