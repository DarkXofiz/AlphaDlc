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
      if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
         if (!this.mc.field_1724.method_5805() && !this.dead && this.stopWatch.isReached(500L)) {
            this.mc.field_1724.field_3944.method_45730("sethome gavno");
            SlownessManager.addTimeTask(new SlownessManager.TimeTask(100L, () -> {
               if (this.mc.field_1724 != null) {
                  this.mc.field_1724.method_7331();
                  this.mc.field_1724.field_3944.method_45730("home gavno");
                  this.dead = false;
               }
            }, true));
            this.stopWatch.reset();
         }
      }
   }
}
