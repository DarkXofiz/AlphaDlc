package miranware.module.list.movement;

import com.google.common.eventbus.Subscribe;
import miranware.MiranWare;
import miranware.event.list.EventNoSlow;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.list.combat.KillAura;
import miranware.module.settings.ModeSetting;
import miranware.util.player.simulate.SimulatedPlayer;

@ModuleInformation(
   moduleName = "No Slow",
   moduleDesc = "Removes slowdown while using items",
   moduleCategory = ModuleCategory.MOVEMENT
)
public class NoSlow extends Module {
   private final ModeSetting mode = new ModeSetting("Mode", "Vanilla", "Vanilla", "Grim");

   @Subscribe
   private void onNoSlow(EventNoSlow e) {
      String var2 = this.mode.getValue();
      switch (var2) {
         case "Vanilla":
            if (MiranWare.getInstance().getModuleStorage().get(KillAura.class).getTarget() == null
               || !(SimulatedPlayer.simulateLocalPlayer(1).fallDistance > 0.0F)) {
               this.mc.player.setSprinting(true);
            }

            e.cancelEvent();
            break;
         case "Grim":
            this.mc
               .player
               .setSprinting(
                  this.mc.player.getItemUseTime() > 4
                     && (
                        MiranWare.getInstance().getModuleStorage().get(KillAura.class).getTarget() == null
                           || !(SimulatedPlayer.simulateLocalPlayer(1).fallDistance > 0.0F)
                     )
                     && MiranWare.getInstance().getServerManager().getSprintingChangeTicks() > 0.0F
               );
            if (this.mc.player.getItemUseTime() % 2 == 0) {
               e.cancelEvent();
            }
      }
   }
}
