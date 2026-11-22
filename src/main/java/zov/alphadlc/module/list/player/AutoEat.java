package zov.alphadlc.module.list.player;

import com.google.common.eventbus.Subscribe;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.ItemStack;
import zov.alphadlc.event.list.EventPlayerUpdate;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.SliderSetting;

@ModuleInformation(
   moduleName = "Auto Eat",
   moduleDesc = "\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438 \u0435\u0441\u0442 \u043a\u043e\u0433\u0434\u0430 \u0433\u043e\u043b\u043e\u0434 \u043d\u0438\u0436\u0435 \u043f\u043e\u0440\u043e\u0433\u0430",
   moduleCategory = ModuleCategory.PLAYER
)
public class AutoEat extends Module {
   private final SliderSetting hungerThreshold = new SliderSetting("\u041f\u043e\u0440\u043e\u0433 \u0433\u043e\u043b\u043e\u0434\u0430", 14.0, 1.0, 20.0, 1.0);
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
