package zov.alphadlc.module.list.player;

import com.google.common.eventbus.Subscribe;
import zov.alphadlc.event.list.EventTick;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;

@ModuleInformation(
   moduleName = "Death Coords",
   moduleDesc = "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0435\u0442 \u043a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u044b \u0441\u043c\u0435\u0440\u0442\u0438",
   moduleCategory = ModuleCategory.PLAYER
)
public class DeathCoords extends Module {
   private boolean send;

   @Subscribe
   private void onUpdate(EventTick e) {
      if (this.mc.field_1724 != null) {
         if (this.mc.field_1724.method_29504()) {
            if (!this.send) {
               this.logDirect(
                  String.format(
                        "\u0412\u044b \u0443\u043c\u0435\u0440\u043b\u0438 \u043d\u0430 XYZ: %.0f %.0f %.0f",
                        this.mc.field_1724.method_23317(),
                        this.mc.field_1724.method_23318(),
                        this.mc.field_1724.method_23321()
                     )
                     .replace(",", ".")
               );
               this.send = true;
            }
         } else {
            this.send = false;
         }
      }
   }
}
