package zov.alphadlc.module.list.player;

import com.google.common.eventbus.Subscribe;
import net.minecraft.util.hit.HitResult.Type;
import zov.alphadlc.event.list.EventTick;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.module.settings.SliderSetting;
import zov.alphadlc.util.bot.BotSessionManager;

@ModuleInformation(
   moduleName = "Tape Mouse",
   moduleDesc = "\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438\u0435 \u043a\u043b\u0438\u043a\u0438 \u0434\u043b\u044f \u0444\u043e\u043d\u043e\u0432\u044b\u0445 \u0431\u043e\u0442\u043e\u0432",
   moduleCategory = ModuleCategory.PLAYER
)
public class TapeMouse extends Module {
   private final SliderSetting cps = new SliderSetting("CPS", 1.0, 0.0, 2.0, 0.05F);
   private final SliderSetting delay = new SliderSetting("\u0414\u043e\u043f. \u0437\u0430\u0434\u0435\u0440\u0436\u043a\u0430", 0.0, 0.0, 1000.0, 10.0);
   private final BooleanSetting rightClick = new BooleanSetting("\u041f\u0440\u0430\u0432\u0430\u044f \u043a\u043d\u043e\u043f\u043a\u0430", false);
   private final BooleanSetting onlyEntity = new BooleanSetting("\u0422\u043e\u043b\u044c\u043a\u043e \u043f\u043e entity", false);
   private final BooleanSetting onlyBots = new BooleanSetting("\u0422\u043e\u043b\u044c\u043a\u043e \u0431\u043e\u0442\u044b", true);
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
