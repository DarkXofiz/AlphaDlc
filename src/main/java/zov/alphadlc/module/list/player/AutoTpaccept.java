package zov.alphadlc.module.list.player;

import com.google.common.eventbus.Subscribe;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import zov.alphadlc.event.list.EventPacket;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.ModuleSettingDefinitions;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.util.friend.FriendRepository;

@ModuleInformation(
   moduleName = "Auto Tpaccept",
   moduleDesc = "\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438 \u043f\u0440\u0438\u043d\u0438\u043c\u0430\u0435\u0442 \u0442\u043f \u0437\u0430\u043f\u0440\u043e\u0441\u044b",
   moduleCategory = ModuleCategory.PLAYER
)
public class AutoTpaccept extends Module {
   private final BooleanSetting friendsOnly = ModuleSettingDefinitions.autoTpOnlyFriends();

   @Subscribe
   public void onPacket(EventPacket e) {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
         if (e.getPacket() instanceof GameMessageS2CPacket p
            && TpRequestRecognizer.shouldAccept(p.comp_763().getString(), this.friendsOnly.getValue(), FriendRepository::isFriend)) {
            this.mc.method_1562().method_45730("tpaccept");
         }
      }
   }
}
