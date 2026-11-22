package zov.alphadlc.module.list.combat;

import com.google.common.eventbus.Subscribe;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import zov.alphadlc.event.list.EventKeyInput;
import zov.alphadlc.event.list.EventPlayerUpdate;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.ModuleSettingDefinitions;
import zov.alphadlc.module.settings.BindSetting;
import zov.alphadlc.module.settings.ModeSetting;
import zov.alphadlc.util.player.other.InventoryUtil;

@ModuleInformation(
   moduleName = "Auto Swap",
   moduleDesc = "\u0411\u044b\u0441\u0442\u0440\u044b\u0439 \u0441\u0432\u043e\u043f \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u043e\u0432 \u043f\u043e \u0431\u0438\u043d\u0434y",
   moduleCategory = ModuleCategory.COMBAT
)
public class AutoSwap extends Module {
   private final ModeSetting mode = this.modeCreate();
   private final BindSetting swapKey = new BindSetting("\u041a\u043b\u0430\u0432\u0438\u0448\u0430 \u0441\u0432\u0430\u043f\u0430", -1);
   private final ModeSetting firstItem = ModuleSettingDefinitions.autoSwapFrom();
   private final ModeSetting secondItem = ModuleSettingDefinitions.autoSwapTo();
   private boolean swapped;

   @Subscribe
   private void onPlayerUpdate(EventPlayerUpdate e) {
      if (this.mc.field_1724 != null) {
         if (this.swapped) {
            this.swapped = false;
            boolean sameItem = this.firstItem.getValue().equals(this.secondItem.getValue());
            int slotFirstItem = this.findItemByName(this.firstItem.getValue(), sameItem);
            int slotSecondItem = this.findItemByName(this.secondItem.getValue(), sameItem);
            if (slotFirstItem == 40 && slotSecondItem == 40) {
               slotSecondItem = InventoryUtil.searchItemStack(item -> AutoSwapRecognition.isSphere(item) && item != this.mc.field_1724.method_6079());
            }

            if (slotFirstItem == -1 && slotSecondItem == -1) {
               return;
            }

            if (slotFirstItem != 40 && (slotFirstItem != -1 || slotSecondItem == 40)) {
               if (slotFirstItem == -1) {
                  return;
               }

               if (slotFirstItem >= 0 && slotFirstItem <= 8) {
                  String var10 = this.mode.getValue();
                  switch (var10) {
                     case "Vanilla":
                        this.mc.field_1761.method_2906(0, 45, slotFirstItem, SlotActionType.field_7791, this.mc.field_1724);
                        break;
                     case "Grim":
                        InventoryUtil.swapWithBypassGrim(
                           () -> this.mc.field_1761.method_2906(0, 45, slotFirstItem, SlotActionType.field_7791, this.mc.field_1724)
                        );
                        break;
                     case "Polar":
                        InventoryUtil.swapWithBypassPolar(
                           () -> this.mc.field_1761.method_2906(0, 45, slotFirstItem, SlotActionType.field_7791, this.mc.field_1724)
                        );
                  }
               } else {
                  String var9 = this.mode.getValue();
                  switch (var9) {
                     case "Vanilla":
                        this.mc.field_1761.method_2906(0, slotFirstItem, 40, SlotActionType.field_7791, this.mc.field_1724);
                        break;
                     case "Grim":
                        InventoryUtil.swapWithBypassGrim(
                           () -> this.mc.field_1761.method_2906(0, slotFirstItem, 40, SlotActionType.field_7791, this.mc.field_1724)
                        );
                        break;
                     case "Polar":
                        InventoryUtil.swapWithBypassPolar(
                           () -> this.mc.field_1761.method_2906(0, slotFirstItem, 40, SlotActionType.field_7791, this.mc.field_1724)
                        );
                  }
               }
            } else if (slotSecondItem >= 0 && slotSecondItem <= 8) {
               int finalSlotSecondItem = slotSecondItem;
               String var11 = this.mode.getValue();
               switch (var11) {
                  case "Vanilla":
                     this.mc.field_1761.method_2906(0, 45, slotSecondItem, SlotActionType.field_7791, this.mc.field_1724);
                     break;
                  case "Grim":
                     InventoryUtil.swapWithBypassGrim(
                        () -> this.mc.field_1761.method_2906(0, 45, finalSlotSecondItem, SlotActionType.field_7791, this.mc.field_1724)
                     );
                     break;
                  case "Polar":
                     InventoryUtil.swapWithBypassPolar(
                        () -> this.mc.field_1761.method_2906(0, 45, finalSlotSecondItem, SlotActionType.field_7791, this.mc.field_1724)
                     );
               }
            } else if (slotSecondItem != -1) {
               int finalSlotSecondItem = slotSecondItem;
               String var6 = this.mode.getValue();
               switch (var6) {
                  case "Vanilla":
                     this.mc.field_1761.method_2906(0, slotSecondItem, 40, SlotActionType.field_7791, this.mc.field_1724);
                     break;
                  case "Grim":
                     InventoryUtil.swapWithBypassGrim(
                        () -> this.mc.field_1761.method_2906(0, finalSlotSecondItem, 40, SlotActionType.field_7791, this.mc.field_1724)
                     );
                     break;
                  case "Polar":
                     InventoryUtil.swapWithBypassPolar(
                        () -> this.mc.field_1761.method_2906(0, finalSlotSecondItem, 40, SlotActionType.field_7791, this.mc.field_1724)
                     );
               }
            }
         }
      }
   }

   @Subscribe
   private void onKey(EventKeyInput e) {
      if (e.getAction() != 0) {
         if (e.getKey() == this.swapKey.getValue()) {
            this.swapped = true;
         }
      }
   }

   private int findItemByName(String name, boolean ignoreOffhand) {
      switch (name) {
         case "\u0413\u0435\u043f\u043b":
            if (!ignoreOffhand && this.mc.field_1724.method_6079().method_7909() == Items.field_8463) {
               return 40;
            }

            return InventoryUtil.searchItem(Items.field_8463);
         case "\u0429\u0438\u0442":
            if (!ignoreOffhand && this.mc.field_1724.method_6079().method_7909() == Items.field_8255) {
               return 40;
            }

            return InventoryUtil.searchItem(Items.field_8255);
         case "\u0422\u0430\u043b\u0438\u0441\u043c\u0430\u043d":
            if (!ignoreOffhand
               && this.mc.field_1724.method_6079().method_7909() == Items.field_8288
               && this.mc.field_1724.method_6079().method_57824(DataComponentTypes.field_49636) != null
               && !((AttributeModifiersComponent)this.mc.field_1724.method_6079().method_57824(DataComponentTypes.field_49636)).comp_2393().isEmpty()) {
               return 40;
            }

            return InventoryUtil.searchItemStack(
               item -> item.method_7909() == Items.field_8288
                     && item.method_57824(DataComponentTypes.field_49636) != null
                     && !((AttributeModifiersComponent)item.method_57824(DataComponentTypes.field_49636)).comp_2393().isEmpty()
            );
         case "\u0428\u0430\u0440":
            if (!ignoreOffhand && AutoSwapRecognition.isSphere(this.mc.field_1724.method_6079())) {
               return 40;
            }

            return InventoryUtil.searchItemStack(AutoSwapRecognition::isSphere);
         default:
            return -1;
      }
   }
}
