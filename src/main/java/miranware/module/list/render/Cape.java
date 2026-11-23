package miranware.module.list.render;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import miranware.MiranWare;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.BooleanSetting;
import miranware.module.settings.ModeListSetting;
import miranware.util.friend.FriendRepository;

@ModuleInformation(
   moduleName = "Cape",
   moduleDesc = "Custom cape for the player",
   moduleCategory = ModuleCategory.RENDER
)
public class Cape extends Module {
   private final ModeListSetting targets = new ModeListSetting(
      "Apply To",
      new BooleanSetting("Self", true),
      new BooleanSetting("Friends", true)
   );

   public static Cape getInstance() {
      return MiranWare.getInstance().getModuleStorage().get(Cape.class);
   }

   public boolean shouldRenderCape(AbstractClientPlayerEntity player) {
      if (this.isEnabled() && player != null) {
         boolean isSelf = player == this.mc.player;
         boolean isFriend = FriendRepository.isFriend(player.getNameForScoreboard());
         boolean selfEnabled = this.targets.isEnabled("Self");
         boolean friendsEnabled = this.targets.isEnabled("Friends");
         if (isSelf) {
            return selfEnabled;
         } else {
            return isFriend ? friendsEnabled : false;
         }
      } else {
         return false;
      }
   }
}
