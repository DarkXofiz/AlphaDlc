package zov.alphadlc.event.list;

import lombok.Generated;
import net.minecraft.entity.Entity;
import zov.alphadlc.event.Event;

public class EventEntitySpawn extends Event {
   private final Entity entity;

   @Generated
   public Entity getEntity() {
      return this.entity;
   }

   @Generated
   public EventEntitySpawn(Entity entity) {
      this.entity = entity;
   }
}
