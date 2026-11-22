package zov.alphadlc.module.list.player;

import com.google.common.eventbus.Subscribe;
import net.minecraft.item.Items;
import zov.alphadlc.event.list.EventKeyInput;
import zov.alphadlc.event.list.EventPlayerSync;
import zov.alphadlc.event.list.EventPlayerUpdate;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.BindSetting;
import zov.alphadlc.module.settings.ModeSetting;
import zov.alphadlc.util.player.other.InventoryUtil;

@ModuleInformation(
   moduleName = "Click Pearl",
   moduleDesc = "\u0411\u044b\u0441\u0442\u0440\u044b\u0439 \u0431\u0440\u043e\u0441\u043e\u043a \u043f\u0435\u0440\u043b\u044b \u043f\u043e \u0431\u0438\u043d\u0434y",
   moduleCategory = ModuleCategory.PLAYER
)
public class ClickPearl extends Module {
   private final ModeSetting mode = new ModeSetting(
      "\u041c\u043e\u0434",
      "\u041e\u0431\u044b\u0447\u043d\u044b\u0439",
      "\u041e\u0431\u044b\u0447\u043d\u044b\u0439",
      "\u041b\u0435\u0433\u0438\u0442\u043d\u044b\u0439"
   );
   private final BindSetting key = new BindSetting("\u041a\u043b\u0430\u0432\u0438\u0448\u0430 \u0431\u0440\u043e\u0441\u043a\u0430", -98);
   private boolean pearlUsed;
   private int ticksExisted;

   @Subscribe
   private void onKey(EventKeyInput e) {
      if (e.getAction() != 0) {
         if (e.getKey() == this.key.getValue()) {
            if (this.mode.is("\u041e\u0431\u044b\u0447\u043d\u044b\u0439")) {
               InventoryUtil.swapAndUseHvH(Items.ENDER_PEARL);
            } else {
               this.pearlUsed = true;
            }
         }
      }
   }

   @Subscribe
   private void onPlayerTick(EventPlayerUpdate ignored) {
      if (this.mc.player != null) {
         if (!this.pearlUsed && this.ticksExisted > 0) {
            this.ticksExisted--;
         }

         if (this.pearlUsed || this.ticksExisted > 0) {
            this.mc.player.setSprinting(false);
         }
      }
   }

   @Subscribe
   private void onPlayerSync(EventPlayerSync ignored) {
      if (this.mc.player != null && this.pearlUsed) {
         int slotHotbar = InventoryUtil.searchItem(Items.ENDER_PEARL, 0, 9);
         if (slotHotbar != -1) {
            InventoryUtil.swapAndUseLegit(Items.ENDER_PEARL);
         } else {
            if (this.ticksExisted == 0) {
               this.ticksExisted++;
               return;
            }

            InventoryUtil.swapAndUseLegit(Items.ENDER_PEARL);
            this.ticksExisted = 2;
         }

         this.pearlUsed = false;
      }
   }
}
