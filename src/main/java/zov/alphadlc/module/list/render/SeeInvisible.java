package zov.alphadlc.module.list.render;

import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.SliderSetting;

@ModuleInformation(
   moduleName = "SeeInvisible",
   moduleDesc = "\u0414\u0435\u043b\u0430\u0435\u0442 \u043d\u0435\u0432\u0438\u0434\u0438\u043c\u044b\u0445 \u0438\u0433\u0440\u043e\u043a\u043e\u0432 \u043f\u043e\u043b\u0443\u043f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u044b\u043c\u0438",
   moduleCategory = ModuleCategory.RENDER
)
public class SeeInvisible extends Module {
   private final SliderSetting alpha = new SliderSetting("\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c", 0.5, 0.1, 1.0, 0.05);

   public float getAlpha() {
      return this.alpha.getFloatValue();
   }

   public boolean isOpaque() {
      return this.alpha.getFloatValue() >= 0.9F;
   }
}
