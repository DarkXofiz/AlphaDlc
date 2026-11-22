package zov.alphadlc.module.list.combat;

import com.google.common.eventbus.Subscribe;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Hand;
import zov.alphadlc.event.list.EventTick;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.BooleanSetting;

@ModuleInformation(
   moduleName = "Trigger Bot",
   moduleDesc = "\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0430\u044f \u0430\u0442\u0430\u043a\u0430 \u043f\u0440\u0438 \u043d\u0430\u0432\u0435\u0434\u0435\u043d\u0438\u0438",
   moduleCategory = ModuleCategory.COMBAT
)
public class TriggerBot extends Module {
   public final BooleanSetting pauseEating = new BooleanSetting(
      "\u041e\u0441\u0442\u0430\u043d\u043e\u0432\u043a\u0430 \u043f\u0440\u0438 \u0435\u0434\u0435", true
   );
   public final BooleanSetting onlyCriticals = new BooleanSetting("\u0422\u043e\u043b\u044c\u043a\u043e \u043a\u0440\u0438\u0442\u044b", true);
   public final BooleanSetting spaceOnly = new BooleanSetting("\u0423\u043c\u043d\u044b\u0435 \u043a\u0440\u0438\u0442\u044b", false);
   private int delay;

   @Subscribe
   public void onEvent(EventTick e2) {
      if (this.mc.field_1724 != null) {
         if (!this.mc.field_1724.method_6115() || !this.pauseEating.getValue()) {
            if (this.delay > 0) {
               this.delay--;
            } else if (this.autoCrit()) {
               Entity ent = this.mc.field_1692;
               if (ent != null) {
                  this.mc.field_1761.method_2918(this.mc.field_1724, ent);
                  this.mc.field_1724.method_6104(Hand.field_5808);
                  this.delay = 10;
               }
            }
         }
      }
   }

   @Override
   public void onDisable() {
      this.delay = 0;
      super.onDisable();
   }

   private boolean autoCrit() {
      boolean reasonForSkipCrit = !this.onlyCriticals.getValue()
         || this.mc.field_1724.method_31549().field_7479
         || this.mc.field_1724.method_6059(StatusEffects.field_5902)
         || this.mc.field_1724.method_6059(StatusEffects.field_5919)
         || this.mc.field_1687.method_8320(this.mc.field_1724.method_24515()).method_26204() == Blocks.field_9983;
      float f2 = this.mc.field_1724.method_7261(0.5F);
      float f3 = this.mc.field_1724.method_24828() ? 1.0F : 0.9F;
      if (f2 < f3) {
         return false;
      } else {
         boolean mergeWithSpeed = this.mc.field_1724.method_24828();
         if (!this.mc.field_1690.field_1903.method_1434() && mergeWithSpeed && this.spaceOnly.getValue()) {
            return true;
         } else if (this.mc.field_1724.method_5771()) {
            return true;
         } else {
            return reasonForSkipCrit ? true : !this.mc.field_1724.method_24828() && this.mc.field_1724.field_6017 > 0.0F;
         }
      }
   }
}
