package miranware.module.list.player;

import com.google.common.eventbus.Subscribe;
import miranware.event.list.EventKeyInput;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.BindSetting;

@ModuleInformation(
   moduleName = "Cords Dropper",
   moduleDesc = "Sends coordinates to chat",
   moduleCategory = ModuleCategory.PLAYER
)
public class CordsDropper extends Module {
   private final BindSetting bind = new BindSetting("Key", -1);

   @Subscribe
   private void onKey(EventKeyInput e) {
      if (e.getAction() != 0) {
         if (e.getKey() == this.bind.getValue() && this.mc.player != null) {
            String message = String.format("! %.0f %.0f !!!", this.mc.player.getX(), this.mc.player.getZ());
            this.mc.player.networkHandler.sendChatMessage(message);
         }
      }
   }
}
