package zov.alphadlc.module.list.movement;

import com.google.common.eventbus.Subscribe;
import zov.alphadlc.event.list.EventTick;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;

@ModuleInformation(
   moduleName = "No Jump Delay",
   moduleDesc = "\u0423\u0431\u0438\u0440\u0430\u0435\u0442 \u0437\u0430\u0434\u0435\u0440\u0436\u043a\u0443 \u043c\u0435\u0436\u0434\u0443 \u043f\u0440\u044b\u0436\u043a\u0430\u043c\u0438",
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
