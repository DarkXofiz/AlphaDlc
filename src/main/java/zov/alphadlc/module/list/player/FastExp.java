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
      if (this.mc.field_1724.method_6047().method_7909() == Items.field_8287 || this.mc.field_1724.method_6079().method_7909() == Items.field_8287) {
         this.mc.field_1752 = 0;
      }
   }
}
