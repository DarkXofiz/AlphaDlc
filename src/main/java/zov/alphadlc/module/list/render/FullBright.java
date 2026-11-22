package zov.alphadlc.module.list.render;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import zov.alphadlc.event.list.EventTick;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;

@ModuleInformation(
   moduleName = "Full Bright",
   moduleDesc = "\u041f\u043e\u043b\u043d\u0430\u044f \u044f\u0440\u043a\u043e\u0441\u0442\u044c \u0431\u0435\u0437 \u0442\u044c\u043c\u044b",
   moduleCategory = ModuleCategory.RENDER
)
public class FullBright extends Module {
   @Subscribe
   private void onUpdate(EventTick e) {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
         if (!this.mc.field_1724.method_6059(StatusEffects.field_5925)) {
            this.mc.field_1724.method_6092(new StatusEffectInstance(StatusEffects.field_5925, -1, 3));
         }
      }
   }

   @Override
   public void onDisable() {
      if (this.mc.field_1724 != null) {
         this.mc.field_1724.method_6016(StatusEffects.field_5925);
         super.onDisable();
      }
   }
}
