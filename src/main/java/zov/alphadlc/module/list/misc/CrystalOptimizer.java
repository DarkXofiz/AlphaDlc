package zov.alphadlc.module.list.misc;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.decoration.EndCrystalEntity;
import zov.alphadlc.event.list.EventAttack;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;

@ModuleInformation(
   moduleName = "Crystal Optimizer",
   moduleDesc = "\u041e\u043f\u0442\u0438\u043c\u0438\u0437\u0430\u0446\u0438\u044f \u0440\u0435\u043d\u0434\u0435\u0440\u0430 \u043a\u0440\u0438\u0441\u0442\u0430\u043b\u043b\u043e\u0432",
   moduleCategory = ModuleCategory.MISC
)
public class CrystalOptimizer extends Module {
   @Subscribe
   private void onAttack(EventAttack e) {
      if (e.getEntity() instanceof EndCrystalEntity entity) {
         entity.method_5650(RemovalReason.field_26999);
      }
   }
}
