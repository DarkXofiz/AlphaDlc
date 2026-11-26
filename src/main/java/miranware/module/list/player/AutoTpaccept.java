package miranware.module.list.player;

import com.google.common.eventbus.Subscribe;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import miranware.event.list.EventPacket;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.ModuleSettingDefinitions;
import miranware.module.settings.BooleanSetting;
import miranware.util.friend.FriendRepository;

@ModuleInformation(
   moduleName = "Auto Tpaccept",
   moduleDesc = "Automatically accepts tp requests",
   moduleCategory = ModuleCategory.PLAYER
)
public class AutoTpaccept extends Module {
   private final BooleanSetting friendsOnly = ModuleSettingDefinitions.autoTpOnlyFriends();

   @Subscribe
   public void onPacket(EventPacket e) {
      if (this.mc.player != null && this.mc.world != null) {
         if (e.getPacket() instanceof GameMessageS2CPacket p
            && TpRequestRecognizer.shouldAccept(p.content().getString(), this.friendsOnly.getValue(), FriendRepository::isFriend)) {
            this.mc.getNetworkHandler().sendChatCommand("tpaccept");
         }
      }
   }
}
