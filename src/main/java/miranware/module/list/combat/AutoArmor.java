package miranware.module.list.combat;

import com.google.common.eventbus.Subscribe;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.screen.slot.SlotActionType;
import miranware.event.list.EventPlayerUpdate;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.ModeSetting;
import miranware.util.math.StopWatch;
import miranware.util.player.other.InventoryUtil;

@ModuleInformation(
   moduleName = "Auto Armor",
   moduleDesc = "Automatically equips armor",
   moduleCategory = ModuleCategory.COMBAT
)
public class AutoArmor extends Module {
   private final ModeSetting mode = new ModeSetting("Mode", "Vanilla", "Vanilla", "Grim");
   private final StopWatch equipArmorCooldownHelmet = new StopWatch();
   private final StopWatch equipArmorCooldownChestplate = new StopWatch();
   private final StopWatch equipArmorCooldownLeggings = new StopWatch();
   private final StopWatch equipArmorCooldownBoots = new StopWatch();

   @Subscribe
   private void onUpdate(EventPlayerUpdate ignored) {
      if (this.mc.player != null) {
         if (this.mc.currentScreen == null || this.mc.currentScreen instanceof InventoryScreen) {
            if (this.mc.player.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) {
               this.swapArmor(EquipmentSlot.HEAD);
            }

            if (this.mc.player.getEquippedStack(EquipmentSlot.CHEST).isEmpty()) {
               this.swapArmor(EquipmentSlot.CHEST);
            }

            if (this.mc.player.getEquippedStack(EquipmentSlot.LEGS).isEmpty()) {
               this.swapArmor(EquipmentSlot.LEGS);
            }

            if (this.mc.player.getEquippedStack(EquipmentSlot.FEET).isEmpty()) {
               this.swapArmor(EquipmentSlot.FEET);
            }
         }
      }
   }

   private void swapArmor(EquipmentSlot equipmentSlot) {
      if ((equipmentSlot != EquipmentSlot.HEAD || this.equipArmorCooldownHelmet.isReached(50L))
         && (equipmentSlot != EquipmentSlot.CHEST || this.equipArmorCooldownChestplate.isReached(50L))
         && (equipmentSlot != EquipmentSlot.LEGS || this.equipArmorCooldownLeggings.isReached(50L))
         && (equipmentSlot != EquipmentSlot.FEET || this.equipArmorCooldownBoots.isReached(50L))) {
         int slot = InventoryUtil.getBestArmorSlot(equipmentSlot);
         if (slot != -1) {
            if (slot < 9) {
               slot += 36;
            }

            int finalSlot = slot;
            String var4 = this.mode.getValue();
            switch (var4) {
               case "Vanilla":
                  this.mc.interactionManager.clickSlot(0, slot, 0, SlotActionType.QUICK_MOVE, this.mc.player);
                  break;
               case "Grim":
                  InventoryUtil.swapWithBypassGrim(() -> this.mc.interactionManager.clickSlot(0, finalSlot, 0, SlotActionType.QUICK_MOVE, this.mc.player));
            }

            if (equipmentSlot == EquipmentSlot.HEAD) {
               this.equipArmorCooldownHelmet.reset();
            }

            if (equipmentSlot == EquipmentSlot.CHEST) {
               this.equipArmorCooldownChestplate.reset();
            }

            if (equipmentSlot == EquipmentSlot.LEGS) {
               this.equipArmorCooldownLeggings.reset();
            }

            if (equipmentSlot == EquipmentSlot.FEET) {
               this.equipArmorCooldownBoots.reset();
            }
         }
      }
   }
}
