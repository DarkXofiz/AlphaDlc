package miranware.event.list;

import lombok.Generated;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import miranware.event.Event;

public class EventRightClickBlock extends Event {
   private final Hand hand;
   private final BlockHitResult hitResult;

   @Generated
   public Hand getHand() {
      return this.hand;
   }

   @Generated
   public BlockHitResult getHitResult() {
      return this.hitResult;
   }

   @Generated
   public EventRightClickBlock(Hand hand, BlockHitResult hitResult) {
      this.hand = hand;
      this.hitResult = hitResult;
   }
}
