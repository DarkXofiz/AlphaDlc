package miranware.module.list.player;

import com.google.common.eventbus.Subscribe;
import net.minecraft.item.Items;
import miranware.event.list.EventKeyInput;
import miranware.event.list.EventPlayerSync;
import miranware.event.list.EventPlayerUpdate;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.BindSetting;
import miranware.module.settings.ModeSetting;
import miranware.util.player.other.InventoryUtil;

@ModuleInformation(
   moduleName = "Click Pearl",
   moduleDesc = "Quickly throws a pearl with a bind",
   moduleCategory = ModuleCategory.PLAYER
)
public class ClickPearl extends Module {
   private final ModeSetting mode = new ModeSetting(
      "Mode",
      "Normal",
      "Normal",
      "Legit"
   );
   private final BindSetting key = new BindSetting("Throw Key", -98);
   private boolean pearlUsed;
   private int ticksExisted;

   @Subscribe
   private void onKey(EventKeyInput e) {
      if (e.getAction() != 0) {
         if (e.getKey() == this.key.getValue()) {
            if (this.mode.is("Normal")) {
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
