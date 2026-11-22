package zov.alphadlc.module.list.movement;

import com.google.common.eventbus.Subscribe;
import zov.alphadlc.event.list.EventTick;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.SliderSetting;
import zov.alphadlc.util.math.TimerUtil;

@ModuleInformation(
   moduleName = "Timer",
   moduleDesc = "\u0418\u0437\u043c\u0435\u043d\u044f\u0435\u0442 \u0441\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0438\u0433\u0440\u044b",
   moduleCategory = ModuleCategory.MOVEMENT
)
public class Timer extends Module {
   private final SliderSetting speed = new SliderSetting("\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c", 2.0, 0.1F, 10.0, 0.1F);

   @Subscribe
   private void onTick(EventTick e) {
      TimerUtil.setTimer((float)this.speed.getValue());
   }

   @Override
   public void onDisable() {
      TimerUtil.setTimer(1.0F);
      super.onDisable();
   }
}
