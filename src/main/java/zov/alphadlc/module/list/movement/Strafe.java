package zov.alphadlc.module.list.movement;

import com.google.common.eventbus.Subscribe;
import net.minecraft.util.math.MathHelper;
import zov.alphadlc.event.list.EventTick;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.ModeSetting;
import zov.alphadlc.module.settings.SliderSetting;
import zov.alphadlc.util.player.move.MoveUtil;
import zov.alphadlc.util.rotation.Rotation;
import zov.alphadlc.util.rotation.RotationComponent;

@ModuleInformation(
   moduleName = "Strafe",
   moduleDesc = "\u0413\u043e\u0440\u0438\u0437\u043e\u043d\u0442\u0430\u043b\u044c\u043d\u044b\u0435 \u0441\u0442\u0440\u0435\u0439\u0444\u044b \u0441 \u043c\u0430\u0442\u0440\u0438\u0446\u0435\u0439 \u0441\u043a\u043e\u0440\u043e\u0441\u0442\u0438",
   moduleCategory = ModuleCategory.MOVEMENT
)
public class Strafe extends Module {
   private final ModeSetting mode = new ModeSetting(
      "\u0420\u0435\u0436\u0438\u043c", "\u041c\u0430\u0442\u0440\u0438\u043a\u0441", "\u041c\u0430\u0442\u0440\u0438\u043a\u0441", "\u0413\u0440\u0438\u043c"
   );
   private final SliderSetting speed = new SliderSetting("\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c", 0.42, 0.0, 1.0, 0.01);

   @Subscribe
   public void onTick(EventTick event) {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
         boolean moving = MoveUtil.hasPlayerMovement();
         if (this.mode.is("\u041c\u0430\u0442\u0440\u0438\u043a\u0441")) {
            MoveUtil.setMotion(moving ? this.speed.getValue() * 1.5 : 0.0);
         } else if (this.mode.is("\u0413\u0440\u0438\u043c")) {
            if (!moving) {
               return;
            }

            MoveUtil.setMotion(this.speed.getValue() * 1.5);
            float forward = this.mc.field_1724.field_3913.field_3905;
            float strafe = this.mc.field_1724.field_3913.field_3907;
            float moveYaw = (float)Math.toDegrees(RotationComponent.direction(this.mc.field_1724.method_36454(), forward, strafe));
            moveYaw = MathHelper.method_15393(moveYaw);
            RotationComponent.update(new Rotation(moveYaw, this.mc.field_1724.method_36455()), 360.0F, 360.0F, 2, 0);
         }
      }
   }
}
