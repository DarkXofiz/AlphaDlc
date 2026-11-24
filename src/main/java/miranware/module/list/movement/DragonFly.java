package miranware.module.list.movement;

import com.google.common.eventbus.Subscribe;
import miranware.event.list.EventTick;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.SliderSetting;
import miranware.util.player.move.MoveUtil;

@ModuleInformation(
   moduleName = "Dragon Fly",
   moduleDesc = "Speeds up an already active flight",
   moduleCategory = ModuleCategory.MOVEMENT
)
public class DragonFly extends Module {
   private final SliderSetting xzSpeed = new SliderSetting("X/Z Speed", 1.0, 0.0, 2.0, 0.1);
   private final SliderSetting ySpeed = new SliderSetting("Y Speed", 1.0, 0.0, 2.0, 0.1);

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
