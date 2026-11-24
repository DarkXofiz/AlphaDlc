package miranware.module.list.movement;

import com.google.common.eventbus.Subscribe;
import miranware.event.list.EventTick;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;

@ModuleInformation(
   moduleName = "No Jump Delay",
   moduleDesc = "Removes the delay between jumps",
   moduleCategory = ModuleCategory.MISC
)
public class NoJumpDelay extends Module {
   @Subscribe
   private void onUpdate(EventTick e) {
      if (this.mc.player != null && this.mc.world != null) {
         this.mc.player.jumpingCooldown = 0;
      }
   }
}
