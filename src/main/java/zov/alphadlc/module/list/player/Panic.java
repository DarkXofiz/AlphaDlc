package zov.alphadlc.module.list.player;

import lombok.Generated;
import net.minecraft.util.Formatting;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.BindSetting;

@ModuleInformation(
   moduleName = "Unhook",
   moduleDesc = "\u0411\u044b\u0441\u0442\u0440\u043e\u0435 \u043e\u0442\u043a\u043b\u044e\u0447\u0435\u043d\u0438\u0435 \u0432\u0441\u0435\u0445 \u043c\u043e\u0434\u0443\u043b\u0435\u0439",
   moduleCategory = ModuleCategory.MISC
)
public class Panic extends Module {
   private final BindSetting backKey = new BindSetting("\u041a\u043d\u043e\u043f\u043a\u0430 \u0432\u043e\u0437\u0432\u0440\u0430\u0442\u0430", -1);
   private boolean hide;

   @Override
   public void onEnable() {
      super.onEnable();
      if (this.mc.field_1724 != null) {
         if (this.backKey.getValue() == -1) {
            this.logDirect(
               Formatting.field_1061
                  + "\u0417\u0430\u0431\u0438\u043d\u0434\u0438\u0442\u0435 \u043a\u043d\u043e\u043f\u043a\u0443 \u0432\u043e\u0437\u0432\u0440\u0430\u0442\u0430!"
            );
            this.setEnabled(false);
         } else {
            this.setHide(true);
            this.mc.field_1724.method_3137();
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
