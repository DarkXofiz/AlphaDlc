package miranware.module.list.player;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import miranware.event.list.EventTick;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.ModeSetting;
import miranware.module.settings.SliderSetting;
import miranware.util.friend.FriendRepository;

@ModuleInformation(
   moduleName = "Auto Leave",
   moduleDesc = "Automatically leaves the server",
   moduleCategory = ModuleCategory.PLAYER
)
public class AutoLeave extends Module {
   private final SliderSetting distance = new SliderSetting("Distance", 8.0, 1.0, 64.0, 1.0);
   private final ModeSetting mode = new ModeSetting("Action", "Hub", "Hub", "Spawn", "Disconnect");

   @Subscribe
   private void onTick(EventTick event) {
      if (this.mc.player != null && this.mc.world != null && this.mc.getNetworkHandler() != null) {
         double triggerDistance = this.distance.getValue();

         for (PlayerEntity player : this.mc.world.getPlayers()) {
            if (player != this.mc.player
               && !player.isSpectator()
               && !FriendRepository.isFriend(player.getNameForScoreboard())
               && !((double)this.mc.player.distanceTo(player) > triggerDistance)) {
               if (this.mode.is("Hub")) {
                  this.mc.getNetworkHandler().sendChatCommand("hub");
               } else if (this.mode.is("Spawn")) {
                  this.mc.getNetworkHandler().sendChatCommand("spawn");
               } else {
                  this.mc.getNetworkHandler().getConnection().disconnect(Text.literal("AutoLeave"));
               }

               this.setEnabled(false);
               return;
            }
         }
      }
   }
}
