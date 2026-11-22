package zov.alphadlc.module.list.movement;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.LivingEntity;
import zov.alphadlc.AlphaDLC;
import zov.alphadlc.event.list.EventKeyInput;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.list.combat.KillAura;
import zov.alphadlc.module.settings.BindSetting;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.module.settings.SliderSetting;
import zov.alphadlc.util.base.Instance;
import zov.alphadlc.util.math.BestPoint;
import zov.alphadlc.util.text.ValueUnit;

@ModuleInformation(
   moduleName = "ElytraTarget",
   moduleCategory = ModuleCategory.MOVEMENT
)
public class ElytraTarget extends Module {
   public final BooleanSetting predictate = new BooleanSetting("\u041f\u0435\u0440\u0435\u0433\u043e\u043d", true);
   public final BindSetting predictateKey = new BindSetting("\u0411\u0438\u043d\u0434 \u041f\u0435\u0440\u0435\u0433\u043e\u043d\u0430", -1)
      .setVisible(() -> true);
   public final SliderSetting predictValue = new SliderSetting("\u0417\u043d\u0430\u0447\u0435\u043d\u0438\u0435", 3.0, 1.0, 6.0, 0.1F)
      .setVisible(() -> this.predictate.getValue());
   public final BooleanSetting elytraSlowdown = new BooleanSetting("\u0417\u0430\u043c\u0435\u0434\u043b\u044f\u0442\u044c", true);
   public final BindSetting elytraSlowdownKey = new BindSetting("\u0411\u0438\u043d\u0434 \u0417\u0430\u043c\u0435\u0434\u043b\u0435\u043d\u0438\u044f", -1)
      .setVisible(() -> true);
   public final SliderSetting slowdownRadius = new SliderSetting(
         "\u0420\u0430\u0434\u0438\u0443\u0441",
         ValueUnit.countable("\u0431\u043b\u043e\u043a", "\u0431\u043b\u043e\u043a\u0430", "\u0431\u043b\u043e\u043a\u043e\u0432"),
         1.9F,
         1.0,
         6.0,
         0.1F
      )
      .setVisible(() -> this.elytraSlowdown.getValue());
   public final SliderSetting minSpeed = new SliderSetting("\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c", 0.2F, 0.1F, 1.0, 0.05F)
      .setVisible(() -> this.elytraSlowdown.getValue());
   public final BooleanSetting hitAfterOvertake = new BooleanSetting("Sloth bypa$", false);
   public final BindSetting hitAfterOvertakeKey = new BindSetting("\u0411\u0438\u043d\u0434 Sloth bypa$", -1).setVisible(() -> true);

   @Subscribe
   private void onKey(EventKeyInput e) {
      if (this.mc.currentScreen == null) {
         if (e.getAction() == 1) {
            if (e.getKey() == this.predictateKey.getValue()) {
               this.predictate.toggle();
            }

            if (e.getKey() == this.elytraSlowdownKey.getValue()) {
               this.elytraSlowdown.toggle();
            }

            if (e.getKey() == this.hitAfterOvertakeKey.getValue()) {
               this.hitAfterOvertake.toggle();
            }
         }
      }
   }

   public boolean canAttack(LivingEntity target) {
      if (target != null && this.mc.player != null) {
         KillAura attackAura = Instance.get(KillAura.class);
         float attackDist = attackAura != null ? attackAura.distance.getFloatValue() : 4.0F;
         double distNearest = this.mc.player.getEyePos().distanceTo(BestPoint.getNearestPoint(target));
         return distNearest <= (double)attackDist;
      } else {
         return false;
      }
   }

   private static ElytraTarget getElytraTarget() {
      ElytraTarget target = AlphaDLC.getInstance().getModuleStorage().get(ElytraTarget.class);
      return target != null && target.isEnabled() ? target : null;
   }

   public static class ElytraSlowdownWrapper {
      public boolean getValue() {
         ElytraTarget target = ElytraTarget.getElytraTarget();
         return target != null && target.elytraSlowdown.getValue();
      }
   }

   public static class HitAfterOvertakeWrapper {
      public boolean getValue() {
         ElytraTarget target = ElytraTarget.getElytraTarget();
         return target != null && target.hitAfterOvertake.getValue();
      }
   }

   public static class MinSpeedWrapper {
      public float getFloatValue() {
         ElytraTarget target = ElytraTarget.getElytraTarget();
         return target != null ? target.minSpeed.getFloatValue() : 0.3F;
      }
   }

   public static class PredictValueWrapper {
      public double getValue() {
         ElytraTarget target = ElytraTarget.getElytraTarget();
         return target != null ? target.predictValue.getValue() : 3.0;
      }

      public float getFloatValue() {
         return (float)this.getValue();
      }
   }

   public static class PredictateWrapper {
      public boolean getValue() {
         ElytraTarget target = ElytraTarget.getElytraTarget();
         return target != null && target.predictate.getValue();
      }
   }

   public static class ShowPredictPointWrapper {
      public boolean getValue() {
         ElytraTarget target = ElytraTarget.getElytraTarget();
         return target != null;
      }
   }

   public static class SlowdownRadiusWrapper {
      public double getValue() {
         ElytraTarget target = ElytraTarget.getElytraTarget();
         return target != null ? target.slowdownRadius.getValue() : 3.0;
      }
   }
}
