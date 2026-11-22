package zov.alphadlc.module.list.player;

import com.google.common.eventbus.Subscribe;
import zov.alphadlc.event.list.EventKeyInput;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.BindSetting;

@ModuleInformation(
   moduleName = "Cords Dropper",
   moduleDesc = "\u0421\u043a\u0438\u0434\u044b\u0432\u0430\u0435\u0442 \u043a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u044b \u0432 \u0447\u0430\u0442",
   moduleCategory = ModuleCategory.PLAYER
)
public class CordsDropper extends Module {
   private final BindSetting bind = new BindSetting("Key", -1);

   @Subscribe
   private void onKey(EventKeyInput e) {
      if (e.getAction() != 0) {
         if (e.getKey() == this.bind.getValue() && this.mc.field_1724 != null) {
            String message = String.format("! %.0f %.0f !!!", this.mc.field_1724.method_23317(), this.mc.field_1724.method_23321());
            this.mc.field_1724.field_3944.method_45729(message);
         }
      }
   }
}
