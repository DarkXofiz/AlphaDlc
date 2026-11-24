package miranware.event.list;

import lombok.Generated;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import miranware.event.Event;

public class EventPreHUD extends Event {
   private final DrawContext drawContext;
   private final RenderTickCounter renderTickCounter;

   @Generated
   public DrawContext getDrawContext() {
      return this.drawContext;
   }

   @Generated
   public RenderTickCounter getRenderTickCounter() {
      return this.renderTickCounter;
   }

   @Generated
   public EventPreHUD(DrawContext drawContext, RenderTickCounter renderTickCounter) {
      this.drawContext = drawContext;
      this.renderTickCounter = renderTickCounter;
   }
}
