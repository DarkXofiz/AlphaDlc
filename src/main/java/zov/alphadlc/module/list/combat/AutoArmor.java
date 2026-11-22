package zov.alphadlc.module.list.combat;

import com.google.common.eventbus.Subscribe;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.screen.slot.SlotActionType;
import zov.alphadlc.event.list.EventPlayerUpdate;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.ModeSetting;
import zov.alphadlc.util.math.StopWatch;
import zov.alphadlc.util.player.other.InventoryUtil;

@ModuleInformation(
   moduleName = "Auto Armor",
   moduleDesc = "\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438 \u044d\u043a\u0438\u043f\u0438\u0440\u0443\u0435\u0442 \u0431\u0440\u043e\u043d\u044e",
   moduleCategory = ModuleCategory.COMBAT
)
public class AutoArmor extends Module {
   private final ModeSetting mode = new ModeSetting("\u041c\u043e\u0434", "Vanilla", "Vanilla", "Grim");
   private final StopWatch equipArmorCooldownHelmet = new StopWatch();
   private final StopWatch equipArmorCooldownChestplate = new StopWatch();
   private final StopWatch equipArmorCooldownLeggings = new StopWatch();
   private final StopWatch equipArmorCooldownBoots = new StopWatch();

   @Subscribe
   private void onUpdate(EventPlayerUpdate ignored) {
      if (this.mc.field_1724 != null) {
         if (this.mc.field_1755 == null || this.mc.field_1755 instanceof InventoryScreen) {
            if (this.mc.field_1724.method_6118(EquipmentSlot.field_6169).method_7960()) {
               this.swapArmor(EquipmentSlot.field_6169);
            }

            if (this.mc.field_1724.method_6118(EquipmentSlot.field_6174).method_7960()) {
               this.swapArmor(EquipmentSlot.field_6174);
            }

            if (this.mc.field_1724.method_6118(EquipmentSlot.field_6172).method_7960()) {
               this.swapArmor(EquipmentSlot.field_6172);
            }

            if (this.mc.field_1724.method_6118(EquipmentSlot.field_6166).method_7960()) {
               this.swapArmor(EquipmentSlot.field_6166);
            }
         }
      }
   }

   private void swapArmor(EquipmentSlot equipmentSlot) {
      if ((equipmentSlot != EquipmentSlot.field_6169 || this.equipArmorCooldownHelmet.isReached(50L))
         && (equipmentSlot != EquipmentSlot.field_6174 || this.equipArmorCooldownChestplate.isReached(50L))
         && (equipmentSlot != EquipmentSlot.field_6172 || this.equipArmorCooldownLeggings.isReached(50L))
         && (equipmentSlot != EquipmentSlot.field_6166 || this.equipArmorCooldownBoots.isReached(50L))) {
         int slot = InventoryUtil.getBestArmorSlot(equipmentSlot);
         if (slot != -1) {
            if (slot < 9) {
               slot += 36;
            }

            int finalSlot = slot;
            String var4 = this.mode.getValue();
            switch (var4) {
               case "Vanilla":
                  this.mc.field_1761.method_2906(0, slot, 0, SlotActionType.field_7794, this.mc.field_1724);
                  break;
               case "Grim":
                  InventoryUtil.swapWithBypassGrim(() -> this.mc.field_1761.method_2906(0, finalSlot, 0, SlotActionType.field_7794, this.mc.field_1724));
            }

            if (equipmentSlot == EquipmentSlot.field_6169) {
               this.equipArmorCooldownHelmet.reset();
            }

            if (equipmentSlot == EquipmentSlot.field_6174) {
               this.equipArmorCooldownChestplate.reset();
            }

            if (equipmentSlot == EquipmentSlot.field_6172) {
               this.equipArmorCooldownLeggings.reset();
            }

            if (equipmentSlot == EquipmentSlot.field_6166) {
               this.equipArmorCooldownBoots.reset();
            }
         }
      }
   }
}
