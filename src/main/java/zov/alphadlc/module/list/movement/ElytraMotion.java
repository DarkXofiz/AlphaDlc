package zov.alphadlc.module.list.movement;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.LivingEntity;
import zov.alphadlc.event.list.EventPlayerUpdate;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.list.combat.KillAura;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.module.settings.SliderSetting;
import zov.alphadlc.util.base.Instance;

@ModuleInformation(
   moduleName = "Elytra Motion",
   moduleDesc = "\u0423\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u0435 \u0434\u0432\u0438\u0436\u0435\u043d\u0438\u0435\u043c \u044d\u043b\u0438\u0442\u0440",
   moduleCategory = ModuleCategory.MOVEMENT
)
public class ElytraMotion extends Module {
   private boolean waitTarget;
   private final SliderSetting distance = new SliderSetting("\u0414\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f", 3.0, 1.0, 6.0, 0.1F);
   private final BooleanSetting bypass = new BooleanSetting("\u041e\u0431\u0445\u043e\u0434", false);

   @Subscribe
   private void onPlayerTick(EventPlayerUpdate e2) {
      if (this.mc.field_1724 != null) {
         LivingEntity target = Instance.get(KillAura.class).getTarget();
         if (target == null) {
            if (!this.waitTarget) {
               this.mc.field_1724.method_5875(false);
               this.waitTarget = true;
            }
         } else {
            this.waitTarget = false;
            float dist = (float)this.mc.field_1724.method_33571().method_1022(target.method_5829().method_1005());
            if (this.mc.field_1724.method_6128() && dist < this.distance.getFloatValue()) {
               if (this.bypass.getValue()) {
                  float yaw = this.mc.field_1724.method_36454();
                  double rad = Math.toRadians((double)yaw);
                  double forward = 0.01;
                  double down = -1.0E-4;
                  double moveX = -Math.sin(rad) * forward;
                  double moveZ = Math.cos(rad) * forward;
                  this.mc.field_1724.method_18800(moveX, down, moveZ);
               } else {
                  this.mc.field_1724.method_18800(0.0, 0.0, 0.0);
               }

               this.mc.field_1724.method_5875(true);
            } else {
               this.mc.field_1724.method_5875(false);
            }
         }
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();
      if (this.mc.field_1724 != null) {
         this.waitTarget = false;
         this.mc.field_1724.method_5875(false);
      }
   }
}
