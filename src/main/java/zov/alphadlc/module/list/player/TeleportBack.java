package zov.alphadlc.module.list.player;

import com.google.common.eventbus.Subscribe;
import zov.alphadlc.event.list.EventTick;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.util.math.StopWatch;
import zov.alphadlc.util.player.other.SlownessManager;

@ModuleInformation(
   moduleName = "Teleport Back",
   moduleDesc = "\u0422\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0430\u0446\u0438\u044f \u043d\u0430 \u043f\u0440\u0435\u0434\u044b\u0434\u0443\u0449\u0443\u044e \u043f\u043e\u0437\u0438\u0446\u0438\u044e",
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
