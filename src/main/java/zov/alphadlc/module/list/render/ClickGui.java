package zov.alphadlc.module.list.render;

import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.SliderSetting;
import zov.alphadlc.ui.ClickGuiFrame;

@ModuleInformation(
   moduleName = "Click Gui",
   moduleDesc = "\u0413\u0440\u0430\u0444\u0438\u0447\u0435\u0441\u043a\u043e\u0435 \u043c\u0435\u043d\u044e \u043d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438 \u043c\u043e\u0434\u0443\u043b\u0435\u0439",
   moduleCategory = ModuleCategory.RENDER,
   moduleKeybind = 344
)
public class ClickGui extends Module {
   public final SliderSetting size = new SliderSetting("\u0420\u0430\u0437\u043c\u0435\u0440", 1.0, 0.7, 1.4, 0.05);
   private ClickGuiFrame clickGuiFrame;

   @Override
   public void onEnable() {
      if (this.clickGuiFrame == null) {
         this.clickGuiFrame = new ClickGuiFrame();
      }

      this.mc.setScreen(this.clickGuiFrame);
      this.clickGuiFrame.playOpenAnimation();
      this.toggle();
   }
}
