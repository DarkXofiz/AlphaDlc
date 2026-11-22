package zov.alphadlc.module.list.movement;

import com.google.common.eventbus.Subscribe;
import zov.alphadlc.event.list.EventTick;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.SliderSetting;
import zov.alphadlc.util.player.move.MoveUtil;

@ModuleInformation(
   moduleName = "Dragon Fly",
   moduleDesc = "\u0423\u0441\u043a\u043e\u0440\u044f\u0435\u0442 \u0443\u0436\u0435 \u0430\u043a\u0442\u0438\u0432\u043d\u044b\u0439 \u043f\u043e\u043b\u0451\u0442",
   moduleCategory = ModuleCategory.MOVEMENT
)
public class DragonFly extends Module {
   private final SliderSetting xzSpeed = new SliderSetting("\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u043f\u043e X/Z", 1.0, 0.0, 2.0, 0.1);
   private final SliderSetting ySpeed = new SliderSetting("\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u043f\u043e Y", 1.0, 0.0, 2.0, 0.1);

   @Subscribe
   public void onTick(EventTick event) {
      if (this.mc.player != null && this.mc.world != null && this.mc.player.getAbilities().flying) {
         MoveUtil.setMotion(this.xzSpeed.getValue());
         if (this.mc.options.jumpKey.isPressed()) {
            this.mc.player.setVelocity(this.mc.player.getVelocity().x, this.ySpeed.getValue(), this.mc.player.getVelocity().z);
         }

         if (this.mc.options.sneakKey.isPressed()) {
            this.mc
               .player
               .setVelocity(this.mc.player.getVelocity().x, -this.ySpeed.getValue(), this.mc.player.getVelocity().z);
         }
      }
   }
}
