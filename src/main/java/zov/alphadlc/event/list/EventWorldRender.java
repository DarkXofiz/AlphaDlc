package zov.alphadlc.event.list;

import lombok.Generated;
import net.minecraft.client.util.math.MatrixStack;
import zov.alphadlc.event.Event;

public class EventWorldRender extends Event {
   private final MatrixStack matrixStack;
   private final float tickDelta;

   @Generated
   public MatrixStack getMatrixStack() {
      return this.matrixStack;
   }

   @Generated
   public float getTickDelta() {
      return this.tickDelta;
   }

   @Generated
   public EventWorldRender(MatrixStack matrixStack, float tickDelta) {
      this.matrixStack = matrixStack;
      this.tickDelta = tickDelta;
   }
}
