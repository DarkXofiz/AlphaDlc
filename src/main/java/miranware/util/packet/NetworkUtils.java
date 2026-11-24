package miranware.util.packet;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.minecraft.network.packet.Packet;
import miranware.util.IMinecraft;

public final class NetworkUtils implements IMinecraft {
   private static final List<Packet<?>> silentPackets = new ArrayList<>();

   public static void sendSilentPacket(Packet<?> packet) {
      silentPackets.add(packet);
      sendPacket(packet);
   }

   public static void sendPacket(Packet<?> packet) {
      mc.getNetworkHandler().sendPacket(packet);
   }

   @Generated
   private NetworkUtils() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   @Generated
   public static List<Packet<?>> getSilentPackets() {
      return silentPackets;
   }
}
