package miranware.module.list.render;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.SliderSetting;

@ModuleInformation(
   moduleName = "View Model",
   moduleDesc = "Hand and item position settings",
   moduleCategory = ModuleCategory.RENDER
)
public class ViewModel extends Module {
   private final SliderSetting offsetLeftX = new SliderSetting("Left Hand X", 0.0, -2.0, 2.0, 0.1F);
   private final SliderSetting offsetLeftY = new SliderSetting("Left Hand Y", 0.0, -2.0, 2.0, 0.1F);
   private final SliderSetting offsetLeftZ = new SliderSetting("Left Hand Z", 0.0, -2.0, 2.0, 0.1F);
   private final SliderSetting offsetRightX = new SliderSetting(
      "Right Hand X", 0.0, -2.0, 2.0, 0.1F
   );
   private final SliderSetting offsetRightY = new SliderSetting(
      "Right Hand Y", 0.0, -2.0, 2.0, 0.1F
   );
   private final SliderSetting offsetRightZ = new SliderSetting(
      "Right Hand Z", 0.0, -2.0, 2.0, 0.1F
   );

   public void applyHandPosition(MatrixStack matrices, Arm arm) {
      if (this.isEnabled()) {
         if (arm == Arm.LEFT) {
            matrices.translate(this.offsetLeftX.getValue(), this.offsetLeftY.getValue(), this.offsetLeftZ.getValue());
         } else {
            matrices.translate(this.offsetRightX.getValue(), this.offsetRightY.getValue(), this.offsetRightZ.getValue());
         }
      } else {
         matrices.translate(0.0F, 0.0F, 0.0F);
      }
   }
}
