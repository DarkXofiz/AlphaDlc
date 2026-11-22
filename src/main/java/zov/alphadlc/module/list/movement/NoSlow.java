package zov.alphadlc.module.list.movement;

import com.google.common.eventbus.Subscribe;
import zov.alphadlc.AlphaDLC;
import zov.alphadlc.event.list.EventNoSlow;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.list.combat.KillAura;
import zov.alphadlc.module.settings.ModeSetting;
import zov.alphadlc.util.player.simulate.SimulatedPlayer;

@ModuleInformation(
   moduleName = "No Slow",
   moduleDesc = "\u0423\u0431\u0438\u0440\u0430\u0435\u0442 \u0437\u0430\u043c\u0435\u0434\u043b\u0435\u043d\u0438\u0435 \u043f\u0440\u0438 \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0438",
   moduleCategory = ModuleCategory.MOVEMENT
)
public class NoSlow extends Module {
   private final ModeSetting mode = new ModeSetting("\u041c\u043e\u0434", "Vanilla", "Vanilla", "Grim");

   @Subscribe
   private void onNoSlow(EventNoSlow e) {
      String var2 = this.mode.getValue();
      switch (var2) {
         case "Vanilla":
            if (AlphaDLC.getInstance().getModuleStorage().get(KillAura.class).getTarget() == null
               || !(SimulatedPlayer.simulateLocalPlayer(1).fallDistance > 0.0F)) {
               this.mc.field_1724.method_5728(true);
            }

            e.cancelEvent();
            break;
         case "Grim":
            this.mc
               .field_1724
               .method_5728(
                  this.mc.field_1724.method_6048() > 4
                     && (
                        AlphaDLC.getInstance().getModuleStorage().get(KillAura.class).getTarget() == null
                           || !(SimulatedPlayer.simulateLocalPlayer(1).fallDistance > 0.0F)
                     )
                     && AlphaDLC.getInstance().getServerManager().getSprintingChangeTicks() > 0.0F
               );
            if (this.mc.field_1724.method_6048() % 2 == 0) {
               e.cancelEvent();
            }
      }
   }
}
