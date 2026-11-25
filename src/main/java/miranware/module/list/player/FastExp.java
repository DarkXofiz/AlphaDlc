package miranware.module.list.player;

import com.google.common.eventbus.Subscribe;
import net.minecraft.item.Items;
import miranware.event.list.EventPlayerUpdate;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;

@ModuleInformation(
   moduleName = "Fast Exp",
   moduleDesc = "Fast experience bottle use",
   moduleCategory = ModuleCategory.PLAYER
)
public class FastExp extends Module {
   @Subscribe
   private void onPlayerUpdate(EventPlayerUpdate e) {
      if (this.mc.player.getMainHandStack().getItem() == Items.EXPERIENCE_BOTTLE || this.mc.player.getOffHandStack().getItem() == Items.EXPERIENCE_BOTTLE) {
         this.mc.itemUseCooldown = 0;
      }
   }
}
