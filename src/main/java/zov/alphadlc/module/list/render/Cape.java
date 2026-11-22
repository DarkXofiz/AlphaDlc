package zov.alphadlc.module.list.render;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import zov.alphadlc.AlphaDLC;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.module.settings.ModeListSetting;
import zov.alphadlc.util.friend.FriendRepository;

@ModuleInformation(
   moduleName = "Cape",
   moduleDesc = "\u0421\u0432\u043e\u0439 \u043f\u043b\u0430\u0449 \u0434\u043b\u044f \u0438\u0433\u0440\u043e\u043a\u0430",
   moduleCategory = ModuleCategory.RENDER
)
public class Cape extends Module {
   private final ModeListSetting targets = new ModeListSetting(
      "\u041d\u0430 \u043a\u043e\u0433\u043e \u0440\u0430\u0431\u043e\u0442\u0430\u0442\u044c",
      new BooleanSetting("\u0421\u0435\u0431\u044f", true),
      new BooleanSetting("\u0414\u0440\u0443\u0437\u044c\u044f", true)
   );

   public static Cape getInstance() {
      return AlphaDLC.getInstance().getModuleStorage().get(Cape.class);
   }

   public boolean shouldRenderCape(AbstractClientPlayerEntity player) {
      if (this.isEnabled() && player != null) {
         boolean isSelf = player == this.mc.field_1724;
         boolean isFriend = FriendRepository.isFriend(player.method_5820());
         boolean selfEnabled = this.targets.isEnabled("\u0421\u0435\u0431\u044f");
         boolean friendsEnabled = this.targets.isEnabled("\u0414\u0440\u0443\u0437\u044c\u044f");
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
