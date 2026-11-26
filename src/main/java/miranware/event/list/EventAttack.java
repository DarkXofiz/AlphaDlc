package miranware.event.list;

import lombok.Generated;
import net.minecraft.entity.Entity;
import miranware.event.Event;

public class EventAttack extends Event {
   private final Entity entity;

   @Generated
   public Entity getEntity() {
      return this.entity;
   }

   @Generated
   public EventAttack(Entity entity) {
      this.entity = entity;
   }
}
