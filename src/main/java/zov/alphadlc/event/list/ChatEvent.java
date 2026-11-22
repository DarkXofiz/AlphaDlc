package zov.alphadlc.event.list;

import lombok.Generated;
import zov.alphadlc.event.Event;

public class ChatEvent extends Event {
   private final String message;

   @Generated
   public String getMessage() {
      return this.message;
   }

   @Generated
   public ChatEvent(String message) {
      this.message = message;
   }
}
