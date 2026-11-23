package miranware.module.list.player;

import com.google.common.eventbus.Subscribe;
import net.minecraft.util.hit.HitResult.Type;
import miranware.event.list.EventTick;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.BooleanSetting;
import miranware.module.settings.SliderSetting;
import miranware.util.bot.BotSessionManager;

@ModuleInformation(
   moduleName = "Tape Mouse",
   moduleDesc = "Automatic clicks for background bots",
   moduleCategory = ModuleCategory.PLAYER
)
public class TapeMouse extends Module {
   private final SliderSetting cps = new SliderSetting("CPS", 1.0, 0.0, 2.0, 0.05F);
   private final SliderSetting delay = new SliderSetting("Extra Delay", 0.0, 0.0, 1000.0, 10.0);
   private final BooleanSetting rightClick = new BooleanSetting("Right Button", false);
   private final BooleanSetting onlyEntity = new BooleanSetting("Entity Only", false);
   private final BooleanSetting onlyBots = new BooleanSetting("Bots Only", true);
   private long lastClick;

   @Subscribe
   private void onTick(EventTick eventTick) {
      long cpsDelay = this.cps.getValue() > 0.0 ? (long)(1000.0 / this.cps.getValue()) : 1000L;
      long effectiveDelay = Math.max(cpsDelay, (long)this.delay.getValue());
      long now = System.currentTimeMillis();
      if (now - this.lastClick >= effectiveDelay) {
         if (!this.onlyBots.getValue()) {
            this.clickForLocalPlayer();
         }

         if (!BotSessionManager.getConnections().isEmpty()) {
            BotSessionManager.pulseBots(this.rightClick.getValue());
         }

         this.lastClick = now;
      }
   }

   private void clickForLocalPlayer() {
      if (this.mc.player != null && this.mc.currentScreen == null) {
         if (this.rightClick.getValue()) {
            this.mc.doItemUse();
         } else if (!this.onlyEntity.getValue() || this.mc.crosshairTarget != null && this.mc.crosshairTarget.getType() == Type.ENTITY) {
            this.mc.doAttack();
         }
      }
   }
}
