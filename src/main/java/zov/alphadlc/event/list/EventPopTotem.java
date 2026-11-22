package zov.alphadlc.event.list;

import lombok.Generated;
import net.minecraft.entity.player.PlayerEntity;
import zov.alphadlc.event.Event;

public class EventPopTotem extends Event {
   private final PlayerEntity player;

   @Generated
   public PlayerEntity getPlayer() {
      return this.player;
   }

   @Generated
   public EventPopTotem(PlayerEntity player) {
      this.player = player;
   }
}
