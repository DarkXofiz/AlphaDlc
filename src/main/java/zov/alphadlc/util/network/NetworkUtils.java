package zov.alphadlc.util.network;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;

public final class NetworkUtils {
   private static boolean sendingSilent;

   private NetworkUtils() {
   }

   public static void sendSilentPacket(Packet<?> packet) {
      MinecraftClient minecraft = MinecraftClient.method_1551();
      if (minecraft.field_1724 != null && minecraft.method_1562() != null) {
         try {
            sendingSilent = true;
            minecraft.method_1562().method_52787(packet);
         } finally {
            sendingSilent = false;
         }
      }
   }

   public static void sendPacket(Packet<?> packet) {
      MinecraftClient minecraft = MinecraftClient.method_1551();
      if (minecraft.field_1724 != null && minecraft.method_1562() != null) {
         minecraft.method_1562().method_52787(packet);
      }
   }

   public static boolean isSendingSilent() {
      return sendingSilent;
   }
}
