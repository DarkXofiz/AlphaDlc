package zov.alphadlc.event.list;

import lombok.Generated;
import zov.alphadlc.event.Event;

public class EventChangeSprint extends Event {
   private boolean sprinting;

   @Generated
   public boolean isSprinting() {
      return this.sprinting;
   }

   @Generated
   public void setSprinting(boolean sprinting) {
      this.sprinting = sprinting;
   }

   @Generated
   public EventChangeSprint(boolean sprinting) {
      this.sprinting = sprinting;
   }
}
