package miranware.module.list.player;

import com.google.common.eventbus.Subscribe;
import miranware.event.list.EventTick;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;

@ModuleInformation(
   moduleName = "Death Coords",
   moduleDesc = "Shows death coordinates",
   moduleCategory = ModuleCategory.PLAYER
)
public class DeathCoords extends Module {
   private boolean send;

   @Subscribe
   private void onUpdate(EventTick e) {
      if (this.mc.player != null) {
         if (this.mc.player.isDead()) {
            if (!this.send) {
               this.logDirect(
                  String.format(
                        "You died at XYZ: %.0f %.0f %.0f",
                        this.mc.player.getX(),
                        this.mc.player.getY(),
                        this.mc.player.getZ()
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
