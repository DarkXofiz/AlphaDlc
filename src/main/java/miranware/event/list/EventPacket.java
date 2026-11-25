package miranware.event.list;

import lombok.Generated;
import net.minecraft.network.packet.Packet;
import miranware.event.Event;

public class EventPacket extends Event {
   private final Packet<?> packet;
   private EventPacket.Type type;

   private boolean isSent() {
      return this.type == EventPacket.Type.SEND;
   }

   private boolean isReceive() {
      return this.type == EventPacket.Type.RECEIVE;
   }

   @Generated
   public Packet<?> getPacket() {
      return this.packet;
   }

   @Generated
   public EventPacket.Type getType() {
      return this.type;
   }

   @Generated
   public EventPacket(Packet<?> packet, EventPacket.Type type) {
      this.packet = packet;
      this.type = type;
   }

   public static enum Type {
      SEND,
      RECEIVE;
   }
}
