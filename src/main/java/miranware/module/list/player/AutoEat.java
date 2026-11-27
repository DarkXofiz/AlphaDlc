package miranware.module.list.player;

import com.google.common.eventbus.Subscribe;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.ItemStack;
import miranware.event.list.EventPlayerUpdate;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.SliderSetting;

@ModuleInformation(
   moduleName = "Auto Eat",
   moduleDesc = "Automatically eats when hunger is below the threshold",
   moduleCategory = ModuleCategory.PLAYER
)
public class AutoEat extends Module {
   private final SliderSetting hungerThreshold = new SliderSetting("Hunger Threshold", 14.0, 1.0, 20.0, 1.0);
   private boolean isEating = false;
   private int previousSlot = -1;

   @Subscribe
   private void onUpdate(EventPlayerUpdate e) {
      if (this.mc.player != null && this.mc.world != null) {
         int currentHunger = this.mc.player.getHungerManager().getFoodLevel();
         if ((double)currentHunger <= this.hungerThreshold.getValue()) {
            int foodSlot = this.findFoodSlot();
            if (foodSlot != -1) {
               if (!this.isEating) {
                  this.previousSlot = this.mc.player.getInventory().selectedSlot;
                  this.mc.player.getInventory().selectedSlot = foodSlot;
                  this.isEating = true;
               }

               this.mc.options.useKey.setPressed(true);
            }
         } else if (this.isEating) {
            this.mc.options.useKey.setPressed(false);
            if (this.previousSlot != -1) {
               this.mc.player.getInventory().selectedSlot = this.previousSlot;
               this.previousSlot = -1;
            }

            this.isEating = false;
         }
      }
   }

   private int findFoodSlot() {
      for (int i = 0; i < 9; i++) {
         ItemStack stack = this.mc.player.getInventory().getStack(i);
         if (!stack.isEmpty()) {
            FoodComponent food = (FoodComponent)stack.get(DataComponentTypes.FOOD);
            if (food != null) {
               return i;
            }
         }
      }

      return -1;
   }

   @Override
   public void onDisable() {
      super.onDisable();
      if (this.isEating) {
         this.mc.options.useKey.setPressed(false);
         if (this.previousSlot != -1 && this.mc.player != null) {
            this.mc.player.getInventory().selectedSlot = this.previousSlot;
         }
      }

      this.isEating = false;
      this.previousSlot = -1;
   }
}
