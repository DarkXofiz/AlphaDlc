package miranware.module.list.misc;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.decoration.EndCrystalEntity;
import miranware.event.list.EventAttack;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;

@ModuleInformation(
   moduleName = "Crystal Optimizer",
   moduleDesc = "Crystal render optimization",
   moduleCategory = ModuleCategory.MISC
)
public class CrystalOptimizer extends Module {
   @Subscribe
   private void onAttack(EventAttack e) {
      if (e.getEntity() instanceof EndCrystalEntity entity) {
         entity.remove(RemovalReason.DISCARDED);
      }
   }
}
