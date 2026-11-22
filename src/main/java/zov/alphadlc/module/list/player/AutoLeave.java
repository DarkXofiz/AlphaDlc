package zov.alphadlc.module.list.player;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import zov.alphadlc.event.list.EventTick;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.ModeSetting;
import zov.alphadlc.module.settings.SliderSetting;
import zov.alphadlc.util.friend.FriendRepository;

@ModuleInformation(
   moduleName = "Auto Leave",
   moduleDesc = "\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438\u0439 \u0432\u044b\u0445\u043e\u0434 \u0441 \u0441\u0435\u0440\u0432\u0435\u0440\u0430",
   moduleCategory = ModuleCategory.PLAYER
)
public class AutoLeave extends Module {
   private final SliderSetting distance = new SliderSetting("\u0414\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f", 8.0, 1.0, 64.0, 1.0);
   private final ModeSetting mode = new ModeSetting("\u0414\u0435\u0439\u0441\u0442\u0432\u0438\u0435", "Hub", "Hub", "Spawn", "Disconnect");

   @Subscribe
   private void onTick(EventTick event) {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null && this.mc.method_1562() != null) {
         double triggerDistance = this.distance.getValue();

         for (PlayerEntity player : this.mc.field_1687.method_18456()) {
            if (player != this.mc.field_1724
               && !player.method_7325()
               && !FriendRepository.isFriend(player.method_5820())
               && !((double)this.mc.field_1724.method_5739(player) > triggerDistance)) {
               if (this.mode.is("Hub")) {
                  this.mc.method_1562().method_45730("hub");
               } else if (this.mode.is("Spawn")) {
                  this.mc.method_1562().method_45730("spawn");
               } else {
                  this.mc.method_1562().method_48296().method_10747(Text.method_43470("AutoLeave"));
               }

               this.setEnabled(false);
               return;
            }
         }
      }
   }
}
