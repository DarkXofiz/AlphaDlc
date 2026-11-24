package miranware.module.list.movement;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.LivingEntity;
import miranware.MiranWare;
import miranware.event.list.EventKeyInput;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.list.combat.KillAura;
import miranware.module.settings.BindSetting;
import miranware.module.settings.BooleanSetting;
import miranware.module.settings.SliderSetting;
import miranware.util.base.Instance;
import miranware.util.math.BestPoint;
import miranware.util.text.ValueUnit;

@ModuleInformation(
   moduleName = "ElytraTarget",
   moduleCategory = ModuleCategory.MOVEMENT
)
public class ElytraTarget extends Module {
   public final BooleanSetting predictate = new BooleanSetting("Overtake", true);
   public final BindSetting predictateKey = new BindSetting("Overtake Bind", -1)
      .setVisible(() -> true);
   public final SliderSetting predictValue = new SliderSetting("Value", 3.0, 1.0, 6.0, 0.1F)
      .setVisible(() -> this.predictate.getValue());
   public final BooleanSetting elytraSlowdown = new BooleanSetting("Slow Down", true);
   public final BindSetting elytraSlowdownKey = new BindSetting("Slowdown Bind", -1)
      .setVisible(() -> true);
   public final SliderSetting slowdownRadius = new SliderSetting(
         "Radius",
         ValueUnit.countable("block", "blocks", "blocks"),
         1.9F,
         1.0,
         6.0,
         0.1F
      )
      .setVisible(() -> this.elytraSlowdown.getValue());
   public final SliderSetting minSpeed = new SliderSetting("Speed", 0.2F, 0.1F, 1.0, 0.05F)
      .setVisible(() -> this.elytraSlowdown.getValue());
   public final BooleanSetting hitAfterOvertake = new BooleanSetting("Sloth bypa$", false);
   public final BindSetting hitAfterOvertakeKey = new BindSetting("Sloth Bypass Bind", -1).setVisible(() -> true);

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
      ElytraTarget target = MiranWare.getInstance().getModuleStorage().get(ElytraTarget.class);
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
