package miranware.module.list.misc;

import com.google.common.eventbus.Subscribe;
import net.minecraft.util.Hand;
import miranware.event.list.EventPlayerUpdate;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.util.math.StopWatch;

@ModuleInformation(
   moduleName = "Anti AFK",
   moduleDesc = "Prevents AFK kicks",
   moduleCategory = ModuleCategory.MISC
)
public class AntiAFK extends Module {
   private final StopWatch stopWatch = new StopWatch();

   @Subscribe
   private void onUpdate(EventPlayerUpdate e) {
      if (this.mc.player != null && this.mc.world != null) {
         if (this.stopWatch.isReached(10000L)) {
            this.mc.player.swingHand(Hand.MAIN_HAND);
            this.mc.player.jump();
            this.stopWatch.reset();
         }
      }
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.stopWatch.reset();
   }
}
