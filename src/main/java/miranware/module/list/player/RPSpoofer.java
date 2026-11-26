package miranware.module.list.player;

import com.google.common.eventbus.Subscribe;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket.Status;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import miranware.event.list.EventPacket;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.util.packet.NetworkUtils;

@ModuleInformation(
   moduleName = "RP Spoofer",
   moduleDesc = "Changes how resource pack packets are sent",
   moduleCategory = ModuleCategory.MISC
)
public class RPSpoofer extends Module {
   @Subscribe
   private void onPacket(EventPacket e) {
      if (e.getPacket() instanceof ResourcePackSendS2CPacket) {
         NetworkUtils.sendPacket(new ResourcePackStatusC2SPacket(this.mc.player.getUuid(), Status.ACCEPTED));
         NetworkUtils.sendPacket(new ResourcePackStatusC2SPacket(this.mc.player.getUuid(), Status.DOWNLOADED));
         NetworkUtils.sendPacket(new ResourcePackStatusC2SPacket(this.mc.player.getUuid(), Status.SUCCESSFULLY_LOADED));
         e.cancelEvent();
      }
   }
}
