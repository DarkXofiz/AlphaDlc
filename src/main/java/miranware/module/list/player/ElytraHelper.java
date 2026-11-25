package miranware.module.list.player;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Formatting;
import miranware.event.list.EventKeyInput;
import miranware.event.list.EventPlayerUpdate;
import miranware.event.list.EventTick;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.BindSetting;
import miranware.module.settings.BooleanSetting;
import miranware.module.settings.ModeSetting;
import miranware.util.chat.ChatUtil;
import miranware.util.packet.NetworkUtils;
import miranware.util.player.other.InventoryUtil;

@ModuleInformation(
   moduleName = "Elytra Helper",
   moduleDesc = "Elytra helper",
   moduleCategory = ModuleCategory.PLAYER
)
public class ElytraHelper extends Module {
   private final ModeSetting mode = new ModeSetting("Mode", "Vanilla", "Vanilla", "Grim", "Polar");
   private final BindSetting swapKey = new BindSetting("Swap Button", -1);
   private final BindSetting fireworkKey = new BindSetting("Firework Button", -1);
   private final ModeSetting throwFireworkMode = new ModeSetting(
      "Firework Launch Mode",
      "Normal",
      "Normal",
      "Legit"
   );
   private final BooleanSetting autoTakeoff = new BooleanSetting("Auto Takeoff", true);
   private boolean fireworkUsed;
   private boolean swapped;
   private int lastElytraSlot = -1;

   @Subscribe
   private void onKey(EventKeyInput e2) {
      if (e2.getAction() != 0) {
         if (e2.getKey() == this.swapKey.getValue()) {
            this.swapped = true;
         }

         if (e2.getKey() == this.fireworkKey.getValue() && this.mc.player.isGliding()) {
            this.fireworkUsed = true;
         }
      }
   }

   @Subscribe
   private void onPlayerUpdate(EventPlayerUpdate e2) {
      if (this.swapped) {
         this.swapped = false;
         this.swap(this.mode, this.mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA);
      }
   }

   @Subscribe
   private void onTick(EventTick e2) {
      if (this.mc.player != null) {
         if (this.autoTakeoff.getValue()) {
            ItemStack chest = this.mc.player.getEquippedStack(EquipmentSlot.CHEST);
            if (chest.getItem() == Items.ELYTRA
               && !this.mc.player.isInLava()
               && !this.mc.player.isTouchingWater()
               && this.mc.player.isOnGround()
               && !this.mc.player.hasVehicle()
               && !this.mc.player.isGliding()
               && !this.mc.player.isSpectator()
               && !this.mc.options.jumpKey.isPressed()) {
               this.mc.player.jump();
            }

            if (chest.getItem() == Items.ELYTRA
               && !this.mc.player.isInLava()
               && !this.mc.player.isTouchingWater()
               && !this.mc.player.isOnGround()
               && !this.mc.player.hasVehicle()
               && !this.mc.player.isGliding()
               && !this.mc.player.isSpectator()) {
               NetworkUtils.sendSilentPacket(new ClientCommandC2SPacket(this.mc.player, Mode.START_FALL_FLYING));
               this.mc.player.startGliding();
            }
         }

         if (this.fireworkUsed) {
            String var4 = this.throwFireworkMode.getValue();
            switch (var4) {
               case "Normal":
                  InventoryUtil.swapAndUseHvH(Items.FIREWORK_ROCKET);
                  break;
               case "Legit":
                  InventoryUtil.swapAndUseLegit(Items.FIREWORK_ROCKET);
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
            "Elytra"
               + Formatting.GRAY
               + " not found in inventory"
         );
      } else {
         if (slot >= 0 && slot <= 8) {
            this.lastElytraSlot = slot;
            this.mc.interactionManager.clickSlot(0, 6, slot, SlotActionType.SWAP, this.mc.player);
         } else if (slot >= 9 && slot <= 35) {
            this.lastElytraSlot = 8;
            this.mc.interactionManager.clickSlot(0, slot, 8, SlotActionType.SWAP, this.mc.player);
            this.mc.interactionManager.clickSlot(0, 6, 8, SlotActionType.SWAP, this.mc.player);
            this.mc.interactionManager.clickSlot(0, slot, 8, SlotActionType.SWAP, this.mc.player);
         }
      }
   }

   private void dequipElytraVanilla() {
      int chestplateSlot = InventoryUtil.findBestChestplateSlot();
      if (chestplateSlot == -1) {
         ChatUtil.send(
            "Chestplate"
               + Formatting.GRAY
               + " not found in inventory"
         );
      } else {
         if (this.lastElytraSlot >= 0 && this.lastElytraSlot <= 8) {
            if (chestplateSlot == this.lastElytraSlot) {
               this.mc.interactionManager.clickSlot(0, 6, this.lastElytraSlot, SlotActionType.SWAP, this.mc.player);
            } else {
               this.mc.interactionManager.clickSlot(0, chestplateSlot, this.lastElytraSlot, SlotActionType.SWAP, this.mc.player);
               this.mc.interactionManager.clickSlot(0, 6, this.lastElytraSlot, SlotActionType.SWAP, this.mc.player);
            }
         } else if (chestplateSlot >= 0 && chestplateSlot <= 8) {
            this.mc.interactionManager.clickSlot(0, 6, chestplateSlot, SlotActionType.SWAP, this.mc.player);
         } else if (chestplateSlot >= 9 && chestplateSlot <= 35) {
            this.mc.interactionManager.clickSlot(0, chestplateSlot, 8, SlotActionType.SWAP, this.mc.player);
            this.mc.interactionManager.clickSlot(0, 6, 8, SlotActionType.SWAP, this.mc.player);
            this.mc.interactionManager.clickSlot(0, chestplateSlot, 8, SlotActionType.SWAP, this.mc.player);
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
            "Elytra"
               + Formatting.GRAY
               + " not found in inventory"
         );
      } else {
         if (slot >= 0 && slot <= 8) {
            this.lastElytraSlot = slot;
            InventoryUtil.swapWithBypassGrim(() -> this.mc.interactionManager.clickSlot(0, 6, slot, SlotActionType.SWAP, this.mc.player));
         } else if (slot >= 9 && slot <= 35) {
            this.lastElytraSlot = 8;
            InventoryUtil.swapWithBypassGrim(() -> {
               this.mc.interactionManager.clickSlot(0, slot, 8, SlotActionType.SWAP, this.mc.player);
               this.mc.interactionManager.clickSlot(0, 6, 8, SlotActionType.SWAP, this.mc.player);
               this.mc.interactionManager.clickSlot(0, slot, 8, SlotActionType.SWAP, this.mc.player);
            });
         }
      }
   }

   private void dequipElytraGrim() {
      int chestplateSlot = InventoryUtil.findBestChestplateSlot();
      if (chestplateSlot == -1) {
         ChatUtil.send(
            "Chestplate"
               + Formatting.GRAY
               + " not found in inventory"
         );
      } else {
         if (this.lastElytraSlot >= 0 && this.lastElytraSlot <= 8) {
            if (chestplateSlot == this.lastElytraSlot) {
               InventoryUtil.swapWithBypassGrim(() -> this.mc.interactionManager.clickSlot(0, 6, this.lastElytraSlot, SlotActionType.SWAP, this.mc.player));
            } else {
               InventoryUtil.swapWithBypassGrim(() -> {
                  this.mc.interactionManager.clickSlot(0, chestplateSlot, this.lastElytraSlot, SlotActionType.SWAP, this.mc.player);
                  this.mc.interactionManager.clickSlot(0, 6, this.lastElytraSlot, SlotActionType.SWAP, this.mc.player);
               });
            }
         } else if (chestplateSlot >= 0 && chestplateSlot <= 8) {
            InventoryUtil.swapWithBypassGrim(() -> this.mc.interactionManager.clickSlot(0, 6, chestplateSlot, SlotActionType.SWAP, this.mc.player));
         } else if (chestplateSlot >= 9 && chestplateSlot <= 35) {
            InventoryUtil.swapWithBypassGrim(() -> {
               this.mc.interactionManager.clickSlot(0, chestplateSlot, 8, SlotActionType.SWAP, this.mc.player);
               this.mc.interactionManager.clickSlot(0, 6, 8, SlotActionType.SWAP, this.mc.player);
               this.mc.interactionManager.clickSlot(0, chestplateSlot, 8, SlotActionType.SWAP, this.mc.player);
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
            "Elytra"
               + Formatting.GRAY
               + " not found in inventory"
         );
      } else {
         if (slot >= 0 && slot <= 8) {
            this.lastElytraSlot = slot;
            InventoryUtil.swapWithBypassPolar(() -> this.mc.interactionManager.clickSlot(0, 6, slot, SlotActionType.SWAP, this.mc.player));
         } else if (slot >= 9 && slot <= 35) {
            this.lastElytraSlot = 8;
            InventoryUtil.swapWithBypassPolar(() -> {
               this.mc.interactionManager.clickSlot(0, slot, 8, SlotActionType.SWAP, this.mc.player);
               this.mc.interactionManager.clickSlot(0, 6, 8, SlotActionType.SWAP, this.mc.player);
               this.mc.interactionManager.clickSlot(0, slot, 8, SlotActionType.SWAP, this.mc.player);
            });
         }
      }
   }

   private void dequipElytraPolar() {
      int chestplateSlot = InventoryUtil.findBestChestplateSlot();
      if (chestplateSlot == -1) {
         ChatUtil.send(
            "Chestplate"
               + Formatting.GRAY
               + " not found in inventory"
         );
      } else {
         if (this.lastElytraSlot >= 0 && this.lastElytraSlot <= 8) {
            if (chestplateSlot == this.lastElytraSlot) {
               InventoryUtil.swapWithBypassPolar(() -> this.mc.interactionManager.clickSlot(0, 6, this.lastElytraSlot, SlotActionType.SWAP, this.mc.player));
            } else {
               InventoryUtil.swapWithBypassPolar(() -> {
                  this.mc.interactionManager.clickSlot(0, chestplateSlot, this.lastElytraSlot, SlotActionType.SWAP, this.mc.player);
                  this.mc.interactionManager.clickSlot(0, 6, this.lastElytraSlot, SlotActionType.SWAP, this.mc.player);
               });
            }
         } else if (chestplateSlot >= 0 && chestplateSlot <= 8) {
            InventoryUtil.swapWithBypassPolar(() -> this.mc.interactionManager.clickSlot(0, 6, chestplateSlot, SlotActionType.SWAP, this.mc.player));
         } else if (chestplateSlot >= 9 && chestplateSlot <= 35) {
            InventoryUtil.swapWithBypassPolar(() -> {
               this.mc.interactionManager.clickSlot(0, chestplateSlot, 8, SlotActionType.SWAP, this.mc.player);
               this.mc.interactionManager.clickSlot(0, 6, 8, SlotActionType.SWAP, this.mc.player);
               this.mc.interactionManager.clickSlot(0, chestplateSlot, 8, SlotActionType.SWAP, this.mc.player);
            });
         }
      }
   }
}
