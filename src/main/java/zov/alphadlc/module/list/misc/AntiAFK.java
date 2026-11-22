package zov.alphadlc.module.list.misc;

import com.google.common.eventbus.Subscribe;
import net.minecraft.util.Hand;
import zov.alphadlc.event.list.EventPlayerUpdate;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.util.math.StopWatch;

@ModuleInformation(
   moduleName = "Anti AFK",
   moduleDesc = "\u041f\u0440\u0435\u0434\u043e\u0442\u0432\u0440\u0430\u0449\u0430\u0435\u0442 \u043a\u0438\u043a \u0437\u0430 AFK",
   moduleCategory = ModuleCategory.MISC
)
public class AntiAFK extends Module {
   private final StopWatch stopWatch = new StopWatch();

   @Subscribe
   private void onUpdate(EventPlayerUpdate e) {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
         if (this.stopWatch.isReached(10000L)) {
            this.mc.field_1724.method_6104(Hand.field_5808);
            this.mc.field_1724.method_6043();
            this.stopWatch.reset();
         }
      }
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.stopWatch.reset();
   }
}
