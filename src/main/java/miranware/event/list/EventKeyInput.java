package miranware.event.list;

import lombok.Generated;
import miranware.event.Event;

public class EventKeyInput extends Event {
   private final int key;
   private final int action;

   @Generated
   public int getKey() {
      return this.key;
   }

   @Generated
   public int getAction() {
      return this.action;
   }

   @Generated
   public EventKeyInput(int key, int action) {
      this.key = key;
      this.action = action;
   }
}
