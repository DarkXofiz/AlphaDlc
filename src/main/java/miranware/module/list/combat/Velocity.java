package miranware.module.list.combat;

import com.google.common.eventbus.Subscribe;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import miranware.event.list.EventPacket;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;

@ModuleInformation(
   moduleName = "Velocity",
   moduleDesc = "Removes knockback from hits",
   moduleCategory = ModuleCategory.COMBAT
)
public class Velocity extends Module {
   @Subscribe
   private void onPacket(EventPacket e) {
      if (e.getPacket() instanceof EntityVelocityUpdateS2CPacket packet) {
         if (packet.getEntityId() != this.mc.player.getId()) {
            return;
         }

         e.cancelEvent();
      }
   }
}
