package miranware.util.chat;

import java.util.Arrays;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import miranware.util.IMinecraft;

public class ChatUtil implements IMinecraft {
   public static void send(Object message) {
      if (mc.player != null) {
         mc.player.sendMessage(Text.of("miranware Dlc " + Formatting.DARK_GRAY + "-> " + Formatting.RESET + message.toString()), false);
      }
   }

   public static void send(Object... messages) {
      if (mc.player != null) {
         mc.player
            .sendMessage(
               Text.of("miranware Dlc " + Formatting.DARK_GRAY + "-> " + Formatting.RESET + String.join(",", Arrays.toString(messages))), false
            );
      }
   }
}
