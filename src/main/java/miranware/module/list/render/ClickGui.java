package miranware.module.list.render;

import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.SliderSetting;
import miranware.ui.ClickGuiFrame;

@ModuleInformation(
   moduleName = "Click Gui",
   moduleDesc = "Graphical module settings menu",
   moduleCategory = ModuleCategory.RENDER,
   moduleKeybind = 344
)
public class ClickGui extends Module {
   public final SliderSetting size = new SliderSetting("Size", 1.0, 0.7, 1.4, 0.05);
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
