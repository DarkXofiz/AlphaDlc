package miranware.util.player.move;

import lombok.Generated;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.OnGroundOnly;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import miranware.util.IMinecraft;
import miranware.util.packet.NetworkUtils;

public final class VerticalTeleport implements IMinecraft {
   public static void teleport(double offset) {
      if (mc.player != null && mc.getNetworkHandler() != null) {
         double x = mc.player.getX();
         double y = mc.player.getY();
         double z = mc.player.getZ();

         for (int i = 0; i < 3; i++) {
            NetworkUtils.sendSilentPacket(new OnGroundOnly(mc.player.isOnGround(), mc.player.horizontalCollision));
         }

         NetworkUtils.sendSilentPacket(new PositionAndOnGround(x, y + offset, z, false, mc.player.horizontalCollision));
         mc.player.setPosition(x, y + offset, z);
      }
   }

   @Generated
   private VerticalTeleport() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
