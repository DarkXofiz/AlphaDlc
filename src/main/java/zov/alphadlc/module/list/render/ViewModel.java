package zov.alphadlc.module.list.render;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.SliderSetting;

@ModuleInformation(
   moduleName = "View Model",
   moduleDesc = "\u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0430 \u043f\u043e\u043b\u043e\u0436\u0435\u043d\u0438\u044f \u0440\u0443\u043a \u0438 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u043e\u0432",
   moduleCategory = ModuleCategory.RENDER
)
public class ViewModel extends Module {
   private final SliderSetting offsetLeftX = new SliderSetting("\u041b\u0435\u0432\u0430\u044f \u0440\u0443\u043a\u0430 \u043f\u043e X", 0.0, -2.0, 2.0, 0.1F);
   private final SliderSetting offsetLeftY = new SliderSetting("\u041b\u0435\u0432\u0430\u044f \u0440\u0443\u043a\u0430 \u043f\u043e Y", 0.0, -2.0, 2.0, 0.1F);
   private final SliderSetting offsetLeftZ = new SliderSetting("\u041b\u0435\u0432\u0430\u044f \u0440\u0443\u043a\u0430 \u043f\u043e Z", 0.0, -2.0, 2.0, 0.1F);
   private final SliderSetting offsetRightX = new SliderSetting(
      "\u041f\u0440\u0430\u0432\u0430\u044f \u0440\u0443\u043a\u0430 \u043f\u043e X", 0.0, -2.0, 2.0, 0.1F
   );
   private final SliderSetting offsetRightY = new SliderSetting(
      "\u041f\u0440\u0430\u0432\u0430\u044f \u0440\u0443\u043a\u0430 \u043f\u043e Y", 0.0, -2.0, 2.0, 0.1F
   );
   private final SliderSetting offsetRightZ = new SliderSetting(
      "\u041f\u0440\u0430\u0432\u0430\u044f \u0440\u0443\u043a\u0430 \u043f\u043e Z", 0.0, -2.0, 2.0, 0.1F
   );

   public void applyHandPosition(MatrixStack matrices, Arm arm) {
      if (this.isEnabled()) {
         if (arm == Arm.field_6182) {
            matrices.method_22904(this.offsetLeftX.getValue(), this.offsetLeftY.getValue(), this.offsetLeftZ.getValue());
         } else {
            matrices.method_22904(this.offsetRightX.getValue(), this.offsetRightY.getValue(), this.offsetRightZ.getValue());
         }
      } else {
         matrices.method_46416(0.0F, 0.0F, 0.0F);
      }
   }
}
