package miranware.module.list.render;

import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.SliderSetting;

@ModuleInformation(
   moduleName = "SeeInvisible",
   moduleDesc = "Makes invisible players translucent",
   moduleCategory = ModuleCategory.RENDER
)
public class SeeInvisible extends Module {
   private final SliderSetting alpha = new SliderSetting("Opacity", 0.5, 0.1, 1.0, 0.05);

   public float getAlpha() {
      return this.alpha.getFloatValue();
   }

   public boolean isOpaque() {
      return this.alpha.getFloatValue() >= 0.9F;
   }
}
