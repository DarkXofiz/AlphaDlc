package zov.alphadlc.module.list.combat;

import com.google.common.eventbus.Subscribe;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import zov.alphadlc.event.list.EventPacket;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;

@ModuleInformation(
   moduleName = "Velocity",
   moduleDesc = "\u0423\u0431\u0438\u0440\u0430\u0435\u0442 \u043e\u0442\u0431\u0440\u0430\u0441\u044b\u0432\u0430\u043d\u0438\u0435 \u043e\u0442 \u0443\u0434\u0430\u0440\u043e\u0432",
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
