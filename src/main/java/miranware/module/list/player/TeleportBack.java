package miranware.module.list.player;

import com.google.common.eventbus.Subscribe;
import miranware.event.list.EventTick;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.util.math.StopWatch;
import miranware.util.player.other.SlownessManager;

@ModuleInformation(
   moduleName = "Teleport Back",
   moduleDesc = "Teleports to the previous position",
   moduleCategory = ModuleCategory.PLAYER
)
public class TeleportBack extends Module {
   private final StopWatch stopWatch = new StopWatch();
   private boolean dead;

   @Subscribe
   private void onUpdate(EventTick e) {
      if (this.mc.player != null && this.mc.world != null) {
         if (!this.mc.player.isAlive() && !this.dead && this.stopWatch.isReached(500L)) {
            this.mc.player.networkHandler.sendChatCommand("sethome gavno");
            SlownessManager.addTimeTask(new SlownessManager.TimeTask(100L, () -> {
               if (this.mc.player != null) {
                  this.mc.player.requestRespawn();
                  this.mc.player.networkHandler.sendChatCommand("home gavno");
                  this.dead = false;
               }
            }, true));
            this.stopWatch.reset();
         }
      }
   }
}
