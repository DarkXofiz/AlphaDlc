package miranware.module.list.movement;

import com.google.common.eventbus.Subscribe;
import miranware.event.list.EventTick;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.SliderSetting;
import miranware.util.math.TimerUtil;

@ModuleInformation(
   moduleName = "Timer",
   moduleDesc = "Changes the game speed",
   moduleCategory = ModuleCategory.MOVEMENT
)
public class Timer extends Module {
   private final SliderSetting speed = new SliderSetting("Speed", 2.0, 0.1F, 10.0, 0.1F);

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
