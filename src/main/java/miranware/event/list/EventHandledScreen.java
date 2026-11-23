package miranware.event.list;

import lombok.Generated;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.screen.slot.Slot;
import miranware.event.Event;

public class EventHandledScreen extends Event {
   private final Slot slotHover;
   private final DrawContext drawContext;
   private final int mouseX;
   private final int mouseY;

   @Generated
   public Slot getSlotHover() {
      return this.slotHover;
   }

   @Generated
   public DrawContext getDrawContext() {
      return this.drawContext;
   }

   @Generated
   public int getMouseX() {
      return this.mouseX;
   }

   @Generated
   public int getMouseY() {
      return this.mouseY;
   }

   @Generated
   public EventHandledScreen(Slot slotHover, DrawContext drawContext, int mouseX, int mouseY) {
      this.slotHover = slotHover;
      this.drawContext = drawContext;
      this.mouseX = mouseX;
      this.mouseY = mouseY;
   }
}
