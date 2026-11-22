package zov.alphadlc.module.list.misc;

import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.util.friend.Friend;
import zov.alphadlc.util.friend.FriendRepository;

@ModuleInformation(
   moduleName = "Streamer Mode",
   moduleDesc = "\u0421\u043a\u0440\u044b\u0432\u0430\u0435\u0442 \u043d\u0438\u043a\u043d\u0435\u0439\u043c\u044b \u0438\u0433\u0440\u043e\u043a\u043e\u0432",
   moduleCategory = ModuleCategory.MISC
)
public class NameProtect extends Module {
   public final BooleanSetting hideFriends = new BooleanSetting("\u0421\u043a\u0440\u044b\u0442\u044c \u0434\u0440\u0443\u0437\u0435\u0439", false);

   public String getCustomName() {
      return this.isEnabled() ? "alphadlc" : this.mc.player.getNameForScoreboard();
   }

   public String getCustomName(String originalName) {
      if (this.isEnabled() && this.mc.player != null) {
         String me = this.mc.player.getNameForScoreboard();
         if (originalName.contains(me)) {
            return originalName.replace(me, "alphadlc");
         } else {
            if (this.hideFriends.getValue()) {
               for (Friend friend : FriendRepository.getFriends()) {
                  if (originalName.contains(friend.name())) {
                     return originalName.replace(friend.name(), "alphadlc");
                  }
               }
            }

            return originalName;
         }
      } else {
         return originalName;
      }
   }
}
