package zov.alphadlc.module.list.player;

import com.google.common.eventbus.Subscribe;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket.Status;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import zov.alphadlc.event.list.EventPacket;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.util.packet.NetworkUtils;

@ModuleInformation(
   moduleName = "RP Spoofer",
   moduleDesc = "\u0418\u0437\u043c\u0435\u043d\u044f\u0435\u0442 \u043e\u0442\u043f\u0440\u0430\u0432\u043a\u0443 \u043f\u0430\u043a\u0435\u0442\u043e\u0432 \u0440\u0435\u0441\u0443\u0440\u0441\u043f\u0430\u043a\u043e\u0432",
   moduleCategory = ModuleCategory.MISC
)
public class RPSpoofer extends Module {
   @Subscribe
   private void onPacket(EventPacket e) {
      if (e.getPacket() instanceof ResourcePackSendS2CPacket) {
         NetworkUtils.sendPacket(new ResourcePackStatusC2SPacket(this.mc.field_1724.method_5667(), Status.field_13016));
         NetworkUtils.sendPacket(new ResourcePackStatusC2SPacket(this.mc.field_1724.method_5667(), Status.field_47704));
         NetworkUtils.sendPacket(new ResourcePackStatusC2SPacket(this.mc.field_1724.method_5667(), Status.field_13017));
         e.cancelEvent();
      }
   }
}
