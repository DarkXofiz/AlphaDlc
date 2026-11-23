package miranware.module.list.misc;

import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.BooleanSetting;
import miranware.util.friend.Friend;
import miranware.util.friend.FriendRepository;

@ModuleInformation(
   moduleName = "Streamer Mode",
   moduleDesc = "Hides player nicknames",
   moduleCategory = ModuleCategory.MISC
)
public class NameProtect extends Module {
   public final BooleanSetting hideFriends = new BooleanSetting("Hide Friends", false);

   public String getCustomName() {
      return this.isEnabled() ? "miranware" : this.mc.player.getNameForScoreboard();
   }

   public String getCustomName(String originalName) {
      if (this.isEnabled() && this.mc.player != null) {
         String me = this.mc.player.getNameForScoreboard();
         if (originalName.contains(me)) {
            return originalName.replace(me, "miranware");
         } else {
            if (this.hideFriends.getValue()) {
               for (Friend friend : FriendRepository.getFriends()) {
                  if (originalName.contains(friend.name())) {
                     return originalName.replace(friend.name(), "miranware");
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
