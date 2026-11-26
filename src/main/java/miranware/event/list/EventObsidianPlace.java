package miranware.event.list;

import lombok.Generated;
import net.minecraft.util.math.BlockPos;
import miranware.event.Event;

public class EventObsidianPlace extends Event {
   private final BlockPos blockPos;

   @Generated
   public BlockPos getBlockPos() {
      return this.blockPos;
   }

   @Generated
   public EventObsidianPlace(BlockPos blockPos) {
      this.blockPos = blockPos;
   }
}
