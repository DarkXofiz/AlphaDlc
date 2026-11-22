package zov.alphadlc.module.list.player;

import com.google.common.eventbus.Subscribe;
import net.minecraft.item.Items;
import zov.alphadlc.event.list.EventPlayerUpdate;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;

@ModuleInformation(
   moduleName = "Fast Exp",
   moduleDesc = "\u0423\u0441\u043a\u043e\u0440\u0435\u043d\u043d\u043e\u0435 \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435 \u043e\u043f\u044b\u0442\u0430",
   moduleCategory = ModuleCategory.PLAYER
)
public class FastExp extends Module {
   @Subscribe
   private void onPlayerUpdate(EventPlayerUpdate e) {
      if (this.mc.player.getMainHandStack().getItem() == Items.EXPERIENCE_BOTTLE || this.mc.player.getOffHandStack().getItem() == Items.EXPERIENCE_BOTTLE) {
         this.mc.itemUseCooldown = 0;
      }
   }
}
