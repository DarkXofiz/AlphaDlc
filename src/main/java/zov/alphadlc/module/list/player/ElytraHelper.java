package zov.alphadlc.module.list.player;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Formatting;
import zov.alphadlc.event.list.EventKeyInput;
import zov.alphadlc.event.list.EventPlayerUpdate;
import zov.alphadlc.event.list.EventTick;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.BindSetting;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.module.settings.ModeSetting;
import zov.alphadlc.util.chat.ChatUtil;
import zov.alphadlc.util.packet.NetworkUtils;
import zov.alphadlc.util.player.other.InventoryUtil;

@ModuleInformation(
   moduleName = "Elytra Helper",
   moduleDesc = "\u041f\u043e\u043c\u043e\u0449\u043d\u0438\u043a \u0434\u043b\u044f \u044d\u043b\u0438\u0442\u0440",
   moduleCategory = ModuleCategory.PLAYER
)
public class ElytraHelper extends Module {
   private final ModeSetting mode = new ModeSetting("\u041c\u043e\u0434", "Vanilla", "Vanilla", "Grim", "Polar");
   private final BindSetting swapKey = new BindSetting("\u041a\u043d\u043e\u043f\u043a\u0430 \u0441\u0432\u0430\u043f\u0430", -1);
   private final BindSetting fireworkKey = new BindSetting("\u041a\u043d\u043e\u043f\u043a\u0430 \u0444\u0435\u0435\u0440\u0432\u0435\u0440\u043a\u0430", -1);
   private final ModeSetting throwFireworkMode = new ModeSetting(
      "\u041c\u043e\u0434 \u043f\u0443\u0441\u043a\u0430 \u0444\u0435\u0435\u0440\u0430",
      "\u041e\u0431\u044b\u0447\u043d\u044b\u0439",
      "\u041e\u0431\u044b\u0447\u043d\u044b\u0439",
      "\u041b\u0435\u0433\u0438\u0442\u043d\u044b\u0439"
   );
   private final BooleanSetting autoTakeoff = new BooleanSetting("\u0410\u0432\u0442\u043e\u0432\u0437\u043b\u0451\u0442", true);
   private boolean fireworkUsed;
   private boolean swapped;
   private int lastElytraSlot = -1;

   @Subscribe
   private void onKey(EventKeyInput e2) {
      if (e2.getAction() != 0) {
         if (e2.getKey() == this.swapKey.getValue()) {
            this.swapped = true;
         }

         if (e2.getKey() == this.fireworkKey.getValue() && this.mc.field_1724.method_6128()) {
            this.fireworkUsed = true;
         }
      }
   }

   @Subscribe
   private void onPlayerUpdate(EventPlayerUpdate e2) {
      if (this.swapped) {
         this.swapped = false;
         this.swap(this.mode, this.mc.field_1724.method_6118(EquipmentSlot.field_6174).method_7909() == Items.field_8833);
      }
   }

   @Subscribe
   private void onTick(EventTick e2) {
      if (this.mc.field_1724 != null) {
         if (this.autoTakeoff.getValue()) {
            ItemStack chest = this.mc.field_1724.method_6118(EquipmentSlot.field_6174);
            if (chest.method_7909() == Items.field_8833
               && !this.mc.field_1724.method_5771()
               && !this.mc.field_1724.method_5799()
               && this.mc.field_1724.method_24828()
               && !this.mc.field_1724.method_5765()
               && !this.mc.field_1724.method_6128()
               && !this.mc.field_1724.method_7325()
               && !this.mc.field_1690.field_1903.method_1434()) {
               this.mc.field_1724.method_6043();
            }

            if (chest.method_7909() == Items.field_8833
               && !this.mc.field_1724.method_5771()
               && !this.mc.field_1724.method_5799()
               && !this.mc.field_1724.method_24828()
               && !this.mc.field_1724.method_5765()
               && !this.mc.field_1724.method_6128()
               && !this.mc.field_1724.method_7325()) {
               NetworkUtils.sendSilentPacket(new ClientCommandC2SPacket(this.mc.field_1724, Mode.field_12982));
               this.mc.field_1724.method_23669();
            }
         }

         if (this.fireworkUsed) {
            String var4 = this.throwFireworkMode.getValue();
            switch (var4) {
               case "\u041e\u0431\u044b\u0447\u043d\u044b\u0439":
                  InventoryUtil.swapAndUseHvH(Items.field_8639);
                  break;
               case "\u041b\u0435\u0433\u0438\u0442\u043d\u044b\u0439":
                  InventoryUtil.swapAndUseLegit(Items.field_8639);
            }

            this.fireworkUsed = false;
         }
      }
   }

   public void swap(ModeSetting mode, boolean chestplate) {
      String var3 = mode.getValue();
      switch (var3) {
         case "Vanilla":
            this.vanillaSwap(chestplate);
            break;
         case "Grim":
            this.grimSwap(chestplate);
            break;
         case "Polar":
            this.polarSwap(chestplate);
      }
   }

   private void vanillaSwap(boolean chestplate) {
      if (chestplate) {
         this.dequipElytraVanilla();
      } else {
         this.equipElytraVanilla();
      }
   }

   private void equipElytraVanilla() {
      int slot = InventoryUtil.findBestElytraSlot();
      if (slot == -1) {
         ChatUtil.send(
            "\u042d\u043b\u0438\u0442\u0440\u0430"
               + Formatting.field_1080
               + " \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u0430 \u0432 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u0435"
         );
      } else {
         if (slot >= 0 && slot <= 8) {
            this.lastElytraSlot = slot;
            this.mc.field_1761.method_2906(0, 6, slot, SlotActionType.field_7791, this.mc.field_1724);
         } else if (slot >= 9 && slot <= 35) {
            this.lastElytraSlot = 8;
            this.mc.field_1761.method_2906(0, slot, 8, SlotActionType.field_7791, this.mc.field_1724);
            this.mc.field_1761.method_2906(0, 6, 8, SlotActionType.field_7791, this.mc.field_1724);
            this.mc.field_1761.method_2906(0, slot, 8, SlotActionType.field_7791, this.mc.field_1724);
         }
      }
   }

   private void dequipElytraVanilla() {
      int chestplateSlot = InventoryUtil.findBestChestplateSlot();
      if (chestplateSlot == -1) {
         ChatUtil.send(
            "\u041d\u0430\u0433\u0440\u0443\u0434\u043d\u0438\u043a"
               + Formatting.field_1080
               + " \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d \u0432 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u0435"
         );
      } else {
         if (this.lastElytraSlot >= 0 && this.lastElytraSlot <= 8) {
            if (chestplateSlot == this.lastElytraSlot) {
               this.mc.field_1761.method_2906(0, 6, this.lastElytraSlot, SlotActionType.field_7791, this.mc.field_1724);
            } else {
               this.mc.field_1761.method_2906(0, chestplateSlot, this.lastElytraSlot, SlotActionType.field_7791, this.mc.field_1724);
               this.mc.field_1761.method_2906(0, 6, this.lastElytraSlot, SlotActionType.field_7791, this.mc.field_1724);
            }
         } else if (chestplateSlot >= 0 && chestplateSlot <= 8) {
            this.mc.field_1761.method_2906(0, 6, chestplateSlot, SlotActionType.field_7791, this.mc.field_1724);
         } else if (chestplateSlot >= 9 && chestplateSlot <= 35) {
            this.mc.field_1761.method_2906(0, chestplateSlot, 8, SlotActionType.field_7791, this.mc.field_1724);
            this.mc.field_1761.method_2906(0, 6, 8, SlotActionType.field_7791, this.mc.field_1724);
            this.mc.field_1761.method_2906(0, chestplateSlot, 8, SlotActionType.field_7791, this.mc.field_1724);
         }
      }
   }

   private void grimSwap(boolean chestplate) {
      if (chestplate) {
         this.dequipElytraGrim();
      } else {
         this.equipElytraGrim();
      }
   }

   private void equipElytraGrim() {
      int slot = InventoryUtil.findBestElytraSlot();
      if (slot == -1) {
         ChatUtil.send(
            "\u042d\u043b\u0438\u0442\u0440\u0430"
               + Formatting.field_1080
               + " \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u0430 \u0432 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u0435"
         );
      } else {
         if (slot >= 0 && slot <= 8) {
            this.lastElytraSlot = slot;
            InventoryUtil.swapWithBypassGrim(() -> this.mc.field_1761.method_2906(0, 6, slot, SlotActionType.field_7791, this.mc.field_1724));
         } else if (slot >= 9 && slot <= 35) {
            this.lastElytraSlot = 8;
            InventoryUtil.swapWithBypassGrim(() -> {
               this.mc.field_1761.method_2906(0, slot, 8, SlotActionType.field_7791, this.mc.field_1724);
               this.mc.field_1761.method_2906(0, 6, 8, SlotActionType.field_7791, this.mc.field_1724);
               this.mc.field_1761.method_2906(0, slot, 8, SlotActionType.field_7791, this.mc.field_1724);
            });
         }
      }
   }

   private void dequipElytraGrim() {
      int chestplateSlot = InventoryUtil.findBestChestplateSlot();
      if (chestplateSlot == -1) {
         ChatUtil.send(
            "\u041d\u0430\u0433\u0440\u0443\u0434\u043d\u0438\u043a"
               + Formatting.field_1080
               + " \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d \u0432 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u0435"
         );
      } else {
         if (this.lastElytraSlot >= 0 && this.lastElytraSlot <= 8) {
            if (chestplateSlot == this.lastElytraSlot) {
               InventoryUtil.swapWithBypassGrim(() -> this.mc.field_1761.method_2906(0, 6, this.lastElytraSlot, SlotActionType.field_7791, this.mc.field_1724));
            } else {
               InventoryUtil.swapWithBypassGrim(() -> {
                  this.mc.field_1761.method_2906(0, chestplateSlot, this.lastElytraSlot, SlotActionType.field_7791, this.mc.field_1724);
                  this.mc.field_1761.method_2906(0, 6, this.lastElytraSlot, SlotActionType.field_7791, this.mc.field_1724);
               });
            }
         } else if (chestplateSlot >= 0 && chestplateSlot <= 8) {
            InventoryUtil.swapWithBypassGrim(() -> this.mc.field_1761.method_2906(0, 6, chestplateSlot, SlotActionType.field_7791, this.mc.field_1724));
         } else if (chestplateSlot >= 9 && chestplateSlot <= 35) {
            InventoryUtil.swapWithBypassGrim(() -> {
               this.mc.field_1761.method_2906(0, chestplateSlot, 8, SlotActionType.field_7791, this.mc.field_1724);
               this.mc.field_1761.method_2906(0, 6, 8, SlotActionType.field_7791, this.mc.field_1724);
               this.mc.field_1761.method_2906(0, chestplateSlot, 8, SlotActionType.field_7791, this.mc.field_1724);
            });
         }
      }
   }

   private void polarSwap(boolean chestplate) {
      if (chestplate) {
         this.dequipElytraPolar();
      } else {
         this.equipElytraPolar();
      }
   }

   private void equipElytraPolar() {
      int slot = InventoryUtil.findBestElytraSlot();
      if (slot == -1) {
         ChatUtil.send(
            "\u042d\u043b\u0438\u0442\u0440\u0430"
               + Formatting.field_1080
               + " \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u0430 \u0432 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u0435"
         );
      } else {
         if (slot >= 0 && slot <= 8) {
            this.lastElytraSlot = slot;
            InventoryUtil.swapWithBypassPolar(() -> this.mc.field_1761.method_2906(0, 6, slot, SlotActionType.field_7791, this.mc.field_1724));
         } else if (slot >= 9 && slot <= 35) {
            this.lastElytraSlot = 8;
            InventoryUtil.swapWithBypassPolar(() -> {
               this.mc.field_1761.method_2906(0, slot, 8, SlotActionType.field_7791, this.mc.field_1724);
               this.mc.field_1761.method_2906(0, 6, 8, SlotActionType.field_7791, this.mc.field_1724);
               this.mc.field_1761.method_2906(0, slot, 8, SlotActionType.field_7791, this.mc.field_1724);
            });
         }
      }
   }

   private void dequipElytraPolar() {
      int chestplateSlot = InventoryUtil.findBestChestplateSlot();
      if (chestplateSlot == -1) {
         ChatUtil.send(
            "\u041d\u0430\u0433\u0440\u0443\u0434\u043d\u0438\u043a"
               + Formatting.field_1080
               + " \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d \u0432 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u0435"
         );
      } else {
         if (this.lastElytraSlot >= 0 && this.lastElytraSlot <= 8) {
            if (chestplateSlot == this.lastElytraSlot) {
               InventoryUtil.swapWithBypassPolar(() -> this.mc.field_1761.method_2906(0, 6, this.lastElytraSlot, SlotActionType.field_7791, this.mc.field_1724));
            } else {
               InventoryUtil.swapWithBypassPolar(() -> {
                  this.mc.field_1761.method_2906(0, chestplateSlot, this.lastElytraSlot, SlotActionType.field_7791, this.mc.field_1724);
                  this.mc.field_1761.method_2906(0, 6, this.lastElytraSlot, SlotActionType.field_7791, this.mc.field_1724);
               });
            }
         } else if (chestplateSlot >= 0 && chestplateSlot <= 8) {
            InventoryUtil.swapWithBypassPolar(() -> this.mc.field_1761.method_2906(0, 6, chestplateSlot, SlotActionType.field_7791, this.mc.field_1724));
         } else if (chestplateSlot >= 9 && chestplateSlot <= 35) {
            InventoryUtil.swapWithBypassPolar(() -> {
               this.mc.field_1761.method_2906(0, chestplateSlot, 8, SlotActionType.field_7791, this.mc.field_1724);
               this.mc.field_1761.method_2906(0, 6, 8, SlotActionType.field_7791, this.mc.field_1724);
               this.mc.field_1761.method_2906(0, chestplateSlot, 8, SlotActionType.field_7791, this.mc.field_1724);
            });
         }
      }
   }
}
