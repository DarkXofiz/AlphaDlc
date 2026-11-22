package zov.alphadlc.module.list.render;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.effect.StatusEffects;
import zov.alphadlc.event.list.EventTick;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.module.settings.ModeListSetting;

@ModuleInformation(
   moduleName = "Removals",
   moduleDesc = "\u0423\u0431\u0438\u0440\u0430\u0435\u0442 \u043b\u0438\u0448\u043d\u0438\u0435 \u044d\u043b\u0435\u043c\u0435\u043d\u0442\u044b \u0438\u043d\u0442\u0435\u0440\u0444\u0435\u0439\u0441\u0430",
   moduleCategory = ModuleCategory.RENDER
)
public class NoRender extends Module {
   public final ModeListSetting elements = new ModeListSetting(
      "\u0423\u0431\u0440\u0430\u0442\u044c \u044d\u043b\u0435\u043c\u0435\u043d\u0442\u044b",
      new BooleanSetting("\u041e\u0433\u043e\u043d\u044c", true),
      new BooleanSetting("\u0420\u0430\u0437\u043c\u044b\u0442\u0438\u0435 \u0432 \u0432\u043e\u0434\u0435", true),
      new BooleanSetting("\u0417\u0440\u0435\u043d\u0438\u0435 \u0432 \u0431\u043b\u043e\u043a\u0430\u0445", true),
      new BooleanSetting("\u041a\u0430\u043c\u0435\u0440\u0430\u043a\u043b\u0438\u043f", true),
      new BooleanSetting("\u0422\u0440\u044f\u0441\u043a\u0430 \u043a\u0430\u043c\u0435\u0440\u044b", true),
      new BooleanSetting("\u0422\u043e\u0442\u0435\u043c", true),
      new BooleanSetting("\u0423\u0434\u043e\u0447\u043a\u0430", true),
      new BooleanSetting("\u0414\u043e\u0436\u0434\u044c", true),
      new BooleanSetting("\u041f\u043b\u043e\u0445\u0438\u0435 \u044d\u0444\u0444\u0435\u043a\u0442\u044b", true)
   );

   @Subscribe
   public void onUpdate(EventTick e) {
      if (this.mc.field_1724 != null) {
         if (this.elements.isEnabled("\u0422\u0440\u044f\u0441\u043a\u0430 \u043a\u0430\u043c\u0435\u0440\u044b")) {
            this.mc.field_1690.method_48974().method_41748(0.0);
         } else {
            this.mc.field_1690.method_48974().method_41748(0.5);
         }

         if (this.elements.isEnabled("\u041f\u043b\u043e\u0445\u0438\u0435 \u044d\u0444\u0444\u0435\u043a\u0442\u044b")) {
            this.mc.field_1724.method_6016(StatusEffects.field_5919);
            this.mc.field_1724.method_6016(StatusEffects.field_5916);
            this.mc.field_1724.method_6016(StatusEffects.field_5901);
            this.mc.field_1724.method_6016(StatusEffects.field_5911);
            this.mc.field_1724.method_6016(StatusEffects.field_5909);
            this.mc.field_1724.method_6016(StatusEffects.field_5920);
            this.mc.field_1724.method_6016(StatusEffects.field_5899);
            this.mc.field_1724.method_6016(StatusEffects.field_5903);
            this.mc.field_1724.method_6016(StatusEffects.field_5902);
            this.mc.field_1724.method_6016(StatusEffects.field_5908);
            this.mc.field_1724.method_6016(StatusEffects.field_38092);
         }
      }
   }

   @Override
   public void onDisable() {
      this.mc.field_1690.method_48974().method_41748(0.5);
      super.onDisable();
   }
}
