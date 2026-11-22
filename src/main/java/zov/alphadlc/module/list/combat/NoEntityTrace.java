package zov.alphadlc.module.list.combat;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.LivingEntity;
import zov.alphadlc.event.list.EventEntityHitBox;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.BooleanSetting;

@ModuleInformation(
   moduleName = "NoEntityTrace",
   moduleDesc = "\u0423\u0432\u0435\u043b\u0438\u0447\u0435\u043d\u0438\u0435 \u0445\u0438\u0442\u0431\u043e\u043a\u0441\u0430 \u0441\u0443\u0449\u043d\u043e\u0441\u0442\u0435\u0439",
   moduleCategory = ModuleCategory.COMBAT
)
public class NoEntityTrace extends Module {
   private final BooleanSetting invisible = new BooleanSetting("\u041d\u0435\u0432\u0438\u0434\u0438\u043c\u043a\u0438", true);

   @Subscribe
   private void onEntityHitBox(EventEntityHitBox e2) {
      if (e2.getEntity() instanceof LivingEntity entity2) {
         if (!entity2.method_5767() || this.invisible.getValue()) {
            e2.setSize(-0.75F);
         }
      }
   }
}
