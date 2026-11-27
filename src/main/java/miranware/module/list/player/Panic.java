package miranware.module.list.player;

import lombok.Generated;
import net.minecraft.util.Formatting;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.BindSetting;

@ModuleInformation(
   moduleName = "Unhook",
   moduleDesc = "Quickly disables all modules",
   moduleCategory = ModuleCategory.MISC
)
public class Panic extends Module {
   private final BindSetting backKey = new BindSetting("Return Button", -1);
   private boolean hide;

   @Override
   public void onEnable() {
      super.onEnable();
      if (this.mc.player != null) {
         if (this.backKey.getValue() == -1) {
            this.logDirect(
               Formatting.RED
                  + "Bind the return button!"
            );
            this.setEnabled(false);
         } else {
            this.setHide(true);
            this.mc.player.closeScreen();
         }
      }
   }

   @Generated
   public BindSetting getBackKey() {
      return this.backKey;
   }

   @Generated
   public boolean isHide() {
      return this.hide;
   }

   @Generated
   public void setHide(boolean hide) {
      this.hide = hide;
   }
}
