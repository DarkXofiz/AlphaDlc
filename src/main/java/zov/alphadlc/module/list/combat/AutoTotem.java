package zov.alphadlc.module.list.combat;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import zov.alphadlc.event.list.EventPlayerUpdate;
import zov.alphadlc.event.list.EventPopTotem;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.module.settings.SliderSetting;
import zov.alphadlc.util.math.StopWatch;
import zov.alphadlc.util.player.other.InventoryUtil;
import zov.alphadlc.util.text.ValueUnit;

@ModuleInformation(
   moduleName = "Auto Totem",
   moduleDesc = "\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438 \u0431\u0435\u0440\u0435\u0442 \u0442\u043e\u0442\u0435\u043c \u0432 \u0440\u0443\u043a\u0443",
   moduleCategory = ModuleCategory.COMBAT
)
public class AutoTotem extends Module {
   private final SliderSetting health = new SliderSetting(
      "\u0417\u0434\u043e\u0440\u043e\u0432\u044c\u0435", ValueUnit.abbreviation("\u0425\u041f"), 4.0, 1.0, 20.0, 0.1F
   );
   private final SliderSetting healthOnElytra = new SliderSetting(
      "\u0417\u0434\u043e\u0440\u043e\u0432\u044c\u0435 \u043d\u0430 \u044d\u043b\u0438\u0442\u0440\u0435",
      ValueUnit.abbreviation("\u0425\u041f"),
      11.0,
      1.0,
      20.0,
      0.1F
   );
   private final BooleanSetting crystalsCheck = new BooleanSetting(
      "\u0420\u0430\u0431\u043e\u0442\u0430\u0442\u044c \u043d\u0430 \u043a\u0440\u0438\u0441\u0442\u0430\u043b\u044b", false
   );
   private final BooleanSetting slowness = new BooleanSetting("\u0417\u0430\u043c\u0435\u0434\u043b\u0435\u043d\u0438\u0435", false);
   private final SliderSetting slownessDuration = new SliderSetting(
         "\u0414\u043b\u0438\u0442\u0435\u043b\u044c\u043d\u043e\u0441\u0442\u044c \u0437\u0430\u043c\u0435\u0434\u043b\u0435\u043d\u0438\u044f",
         100.0,
         0.0,
         160.0,
         1.0
      )
      .setVisible(this.slowness::getValue);
   private int swapBackItem = -1;
   private float cooldownTicks;
   private final StopWatch totemStopWatch = new StopWatch();

   @Subscribe
   private void onUpdate(EventPlayerUpdate e) {
      if (this.mc.field_1724 != null) {
         if (this.cooldownTicks > 0.0F) {
            this.cooldownTicks--;
         }

         this.updateSwap();
      }
   }

   @Subscribe
   private void onPopTotem(EventPopTotem e) {
      if (this.mc.field_1724 != null && e.getPlayer() == this.mc.field_1724) {
         this.cooldownTicks = 5.0F;
      }
   }

   private boolean condition() {
      if (!this.mc.field_1724.method_7337() && !this.mc.field_1724.method_7325()) {
         boolean crystalsCheck = false;

         for (Entity entity : this.mc.field_1687.method_18112()) {
            if (entity instanceof EndCrystalEntity
               && this.mc.field_1724.method_23318() >= entity.method_23318()
               && this.mc.field_1724.method_33571().method_1022(entity.method_5829().method_1005()) <= 8.0) {
               crystalsCheck = true;
            }
         }

         return (double)(this.mc.field_1724.method_6032() + this.mc.field_1724.method_6067()) <= this.health.getValue()
            || (double)(this.mc.field_1724.method_6032() + this.mc.field_1724.method_6067()) <= this.healthOnElytra.getValue()
               && this.mc.field_1724.method_6118(EquipmentSlot.field_6174).method_7909() == Items.field_8833
            || this.crystalsCheck.getValue() && crystalsCheck;
      } else {
         return false;
      }
   }

   private void updateSwap() {
      ItemStack offHand = this.mc.field_1724.method_6079();
      int totemSlot = InventoryUtil.searchItem(Items.field_8288);
      if (this.condition() && this.totemStopWatch.isReached(300L) && totemSlot != -1 && offHand.method_7909() != Items.field_8288) {
         if (this.swapBackItem == -1 && !offHand.method_7960()) {
            this.swapBackItem = totemSlot;
         }

         if (totemSlot >= 0 && totemSlot <= 8) {
            this.swapTotem(() -> this.mc.field_1761.method_2906(0, 45, totemSlot, SlotActionType.field_7791, this.mc.field_1724));
         } else if (totemSlot >= 8 && totemSlot <= 45) {
            this.swapTotem(() -> this.mc.field_1761.method_2906(0, totemSlot, 40, SlotActionType.field_7791, this.mc.field_1724));
         }

         this.totemStopWatch.reset();
      }

      if (!this.condition() && this.swapBackItem != -1 && this.cooldownTicks == 0.0F) {
         int swapBackSlot = this.swapBackItem;
         if (swapBackSlot >= 0 && swapBackSlot <= 8) {
            this.swapTotem(() -> this.mc.field_1761.method_2906(0, 45, swapBackSlot, SlotActionType.field_7791, this.mc.field_1724));
         } else if (swapBackSlot >= 8 && swapBackSlot <= 45) {
            this.swapTotem(() -> this.mc.field_1761.method_2906(0, swapBackSlot, 40, SlotActionType.field_7791, this.mc.field_1724));
         }

         this.swapBackItem = -1;
      }
   }

   private void swapTotem(Runnable action) {
      if (this.slowness.getValue()) {
         InventoryUtil.swapWithBypassPolar(action, (long)this.slownessDuration.getValue());
      } else {
         InventoryUtil.swapWithBypassGrim(action);
      }
   }
}
