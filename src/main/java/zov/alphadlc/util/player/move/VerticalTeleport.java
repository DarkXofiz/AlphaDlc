package zov.alphadlc.util.player.move;

import lombok.Generated;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.OnGroundOnly;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import zov.alphadlc.util.IMinecraft;
import zov.alphadlc.util.packet.NetworkUtils;

public final class VerticalTeleport implements IMinecraft {
   public static void teleport(double offset) {
      if (mc.field_1724 != null && mc.method_1562() != null) {
         double x = mc.field_1724.method_23317();
         double y = mc.field_1724.method_23318();
         double z = mc.field_1724.method_23321();

         for (int i = 0; i < 3; i++) {
            NetworkUtils.sendSilentPacket(new OnGroundOnly(mc.field_1724.method_24828(), mc.field_1724.field_5976));
         }

         NetworkUtils.sendSilentPacket(new PositionAndOnGround(x, y + offset, z, false, mc.field_1724.field_5976));
         mc.field_1724.method_5814(x, y + offset, z);
      }
   }

   @Generated
   private VerticalTeleport() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
