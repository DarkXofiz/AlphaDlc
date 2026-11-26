package miranware.module.list.combat;

import com.google.common.eventbus.Subscribe;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import miranware.event.list.EventKeyInput;
import miranware.event.list.EventPlayerUpdate;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.ModuleSettingDefinitions;
import miranware.module.settings.BindSetting;
import miranware.module.settings.ModeSetting;
import miranware.util.player.other.InventoryUtil;

@ModuleInformation(
   moduleName = "Auto Swap",
   moduleDesc = "Quickly swaps items with a bind",
   moduleCategory = ModuleCategory.COMBAT
)
public class AutoSwap extends Module {
   private final ModeSetting mode = this.modeCreate();
   private final BindSetting swapKey = new BindSetting("Swap Key", -1);
   private final ModeSetting firstItem = ModuleSettingDefinitions.autoSwapFrom();
   private final ModeSetting secondItem = ModuleSettingDefinitions.autoSwapTo();
   private boolean swapped;

   @Subscribe
   private void onPlayerUpdate(EventPlayerUpdate e) {
      if (this.mc.player != null) {
         if (this.swapped) {
            this.swapped = false;
            boolean sameItem = this.firstItem.getValue().equals(this.secondItem.getValue());
            int slotFirstItem = this.findItemByName(this.firstItem.getValue(), sameItem);
            int slotSecondItem = this.findItemByName(this.secondItem.getValue(), sameItem);
            if (slotFirstItem == 40 && slotSecondItem == 40) {
               slotSecondItem = InventoryUtil.searchItemStack(item -> AutoSwapRecognition.isSphere(item) && item != this.mc.player.getOffHandStack());
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
                        this.mc.interactionManager.clickSlot(0, 45, slotFirstItem, SlotActionType.SWAP, this.mc.player);
                        break;
                     case "Grim":
                        InventoryUtil.swapWithBypassGrim(
                           () -> this.mc.interactionManager.clickSlot(0, 45, slotFirstItem, SlotActionType.SWAP, this.mc.player)
                        );
                        break;
                     case "Polar":
                        InventoryUtil.swapWithBypassPolar(
                           () -> this.mc.interactionManager.clickSlot(0, 45, slotFirstItem, SlotActionType.SWAP, this.mc.player)
                        );
                  }
               } else {
                  String var9 = this.mode.getValue();
                  switch (var9) {
                     case "Vanilla":
                        this.mc.interactionManager.clickSlot(0, slotFirstItem, 40, SlotActionType.SWAP, this.mc.player);
                        break;
                     case "Grim":
                        InventoryUtil.swapWithBypassGrim(
                           () -> this.mc.interactionManager.clickSlot(0, slotFirstItem, 40, SlotActionType.SWAP, this.mc.player)
                        );
                        break;
                     case "Polar":
                        InventoryUtil.swapWithBypassPolar(
                           () -> this.mc.interactionManager.clickSlot(0, slotFirstItem, 40, SlotActionType.SWAP, this.mc.player)
                        );
                  }
               }
            } else if (slotSecondItem >= 0 && slotSecondItem <= 8) {
               int finalSlotSecondItem = slotSecondItem;
               String var11 = this.mode.getValue();
               switch (var11) {
                  case "Vanilla":
                     this.mc.interactionManager.clickSlot(0, 45, slotSecondItem, SlotActionType.SWAP, this.mc.player);
                     break;
                  case "Grim":
                     InventoryUtil.swapWithBypassGrim(
                        () -> this.mc.interactionManager.clickSlot(0, 45, finalSlotSecondItem, SlotActionType.SWAP, this.mc.player)
                     );
                     break;
                  case "Polar":
                     InventoryUtil.swapWithBypassPolar(
                        () -> this.mc.interactionManager.clickSlot(0, 45, finalSlotSecondItem, SlotActionType.SWAP, this.mc.player)
                     );
               }
            } else if (slotSecondItem != -1) {
               int finalSlotSecondItem = slotSecondItem;
               String var6 = this.mode.getValue();
               switch (var6) {
                  case "Vanilla":
                     this.mc.interactionManager.clickSlot(0, slotSecondItem, 40, SlotActionType.SWAP, this.mc.player);
                     break;
                  case "Grim":
                     InventoryUtil.swapWithBypassGrim(
                        () -> this.mc.interactionManager.clickSlot(0, finalSlotSecondItem, 40, SlotActionType.SWAP, this.mc.player)
                     );
                     break;
                  case "Polar":
                     InventoryUtil.swapWithBypassPolar(
                        () -> this.mc.interactionManager.clickSlot(0, finalSlotSecondItem, 40, SlotActionType.SWAP, this.mc.player)
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
         case "Gapple":
            if (!ignoreOffhand && this.mc.player.getOffHandStack().getItem() == Items.GOLDEN_APPLE) {
               return 40;
            }

            return InventoryUtil.searchItem(Items.GOLDEN_APPLE);
         case "Shield":
            if (!ignoreOffhand && this.mc.player.getOffHandStack().getItem() == Items.SHIELD) {
               return 40;
            }

            return InventoryUtil.searchItem(Items.SHIELD);
         case "Talisman":
            if (!ignoreOffhand
               && this.mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING
               && this.mc.player.getOffHandStack().get(DataComponentTypes.ATTRIBUTE_MODIFIERS) != null
               && !((AttributeModifiersComponent)this.mc.player.getOffHandStack().get(DataComponentTypes.ATTRIBUTE_MODIFIERS)).modifiers().isEmpty()) {
               return 40;
            }

            return InventoryUtil.searchItemStack(
               item -> item.getItem() == Items.TOTEM_OF_UNDYING
                     && item.get(DataComponentTypes.ATTRIBUTE_MODIFIERS) != null
                     && !((AttributeModifiersComponent)item.get(DataComponentTypes.ATTRIBUTE_MODIFIERS)).modifiers().isEmpty()
            );
         case "Sphere":
            if (!ignoreOffhand && AutoSwapRecognition.isSphere(this.mc.player.getOffHandStack())) {
               return 40;
            }

            return InventoryUtil.searchItemStack(AutoSwapRecognition::isSphere);
         default:
            return -1;
      }
   }
}
