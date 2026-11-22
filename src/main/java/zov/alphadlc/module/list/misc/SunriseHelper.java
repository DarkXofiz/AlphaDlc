package zov.alphadlc.module.list.misc;

import com.google.common.eventbus.Subscribe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.PlayerInput;
import zov.alphadlc.AlphaDLC;
import zov.alphadlc.event.list.EventKeyInput;
import zov.alphadlc.event.list.EventPlayerUpdate;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.list.movement.Sprint;
import zov.alphadlc.module.settings.BindSetting;
import zov.alphadlc.util.base.Instance;
import zov.alphadlc.util.packet.NetworkUtils;

@ModuleInformation(
   moduleName = "Sunrise Helper",
   moduleDesc = "\u041f\u043e\u043c\u043e\u0449\u043d\u0438\u043a \u0434\u043b\u044f Sunrise",
   moduleCategory = ModuleCategory.MISC
)
public class SunriseHelper extends Module {
   private final BindSetting bindTrap = new BindSetting("\u0422\u0440\u0430\u043f\u043a\u0430", -1);
   private final BindSetting bindFire = new BindSetting("\u0412\u043e\u043b\u043d\u0430 \u043e\u0433\u043d\u044f", -1);
   private boolean throwTrap;
   private boolean throwFire;

   @Subscribe
   public void onKey(EventKeyInput e) {
      if (this.mc.field_1755 == null && e.getAction() != 0 && e.getAction() != 2) {
         if (e.getKey() == this.bindTrap.getValue()) {
            this.throwTrap = true;
         }

         if (e.getKey() == this.bindFire.getValue()) {
            this.throwFire = true;
         }
      }
   }

   @Subscribe
   public void onUpdate(EventPlayerUpdate e) {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
         if (this.throwTrap) {
            this.useItem("\u0442\u0440\u0430\u043f\u043a\u0430");
            this.throwTrap = false;
         }

         if (this.throwFire) {
            this.useItem("\u0432\u043e\u043b\u043d\u0430 \u043e\u0433\u043d\u044f");
            this.throwFire = false;
         }
      }
   }

   private void useItem(String itemName) {
      int slotHotbar = this.findItemSlotHotbar(itemName);
      int slotInv = this.findItemSlotInventory(itemName);
      int previousSlot = this.mc.field_1724.method_31548().field_7545;
      String mainHandName = this.mc.field_1724.method_6047().method_7964().getString().toLowerCase();
      if (mainHandName.contains(itemName)) {
         this.mc.field_1761.method_2919(this.mc.field_1724, Hand.field_5808);
      } else {
         String offHandName = this.mc.field_1724.method_6079().method_7964().getString().toLowerCase();
         if (offHandName.contains(itemName)) {
            this.mc.field_1761.method_2919(this.mc.field_1724, Hand.field_5810);
         } else if (slotHotbar != -1) {
            this.mc.method_1562().method_52787(new UpdateSelectedSlotC2SPacket(slotHotbar));
            this.mc.field_1761.method_2919(this.mc.field_1724, Hand.field_5808);
            this.mc.method_1562().method_52787(new UpdateSelectedSlotC2SPacket(previousSlot));
         } else {
            if (slotInv != -1) {
               int slotCorrectable = -1;

               for (int slotNone = 0; slotNone < 8; slotNone++) {
                  ItemStack stack = this.mc.field_1724.method_31548().method_5438(slotNone);
                  if (stack.method_7960()) {
                     slotCorrectable = slotNone;
                  }

                  if (stack.method_7976() == UseAction.field_8952) {
                     slotCorrectable = slotNone;
                  }
               }

               this.mc.method_1562().method_52787(new PlayerInputC2SPacket(new PlayerInput(false, false, false, false, false, false, false)));
               if (AlphaDLC.getInstance().getServerManager().isServerSprinting()) {
                  this.mc.field_1724.method_5728(false);
                  this.mc.method_1562().method_52787(new ClientCommandC2SPacket(this.mc.field_1724, Mode.field_12985));
                  if (!Instance.get(Sprint.class).isEnabled()) {
                     this.mc.field_1690.field_1867.method_23481(false);
                  }
               }

               if (slotCorrectable == -1) {
                  this.mc.field_1761.method_2906(0, slotInv, 8, SlotActionType.field_7791, this.mc.field_1724);
                  NetworkUtils.sendSilentPacket(new CloseHandledScreenC2SPacket(0));
                  this.mc.method_1562().method_52787(new UpdateSelectedSlotC2SPacket(8));
                  this.mc.field_1761.method_2919(this.mc.field_1724, Hand.field_5808);
                  this.mc.method_1562().method_52787(new UpdateSelectedSlotC2SPacket(previousSlot));
               } else {
                  this.mc.field_1761.method_2906(0, slotInv, slotCorrectable, SlotActionType.field_7791, this.mc.field_1724);
                  NetworkUtils.sendSilentPacket(new CloseHandledScreenC2SPacket(0));
                  this.mc.method_1562().method_52787(new UpdateSelectedSlotC2SPacket(slotCorrectable));
                  this.mc.field_1761.method_2919(this.mc.field_1724, Hand.field_5808);
                  this.mc.method_1562().method_52787(new UpdateSelectedSlotC2SPacket(previousSlot));
                  this.mc.field_1761.method_2906(0, slotInv, slotCorrectable, SlotActionType.field_7791, this.mc.field_1724);
                  NetworkUtils.sendSilentPacket(new CloseHandledScreenC2SPacket(0));
               }

               this.mc.method_1562().method_52787(new PlayerInputC2SPacket(this.mc.field_1724.field_3913.field_54155));
            }
         }
      }
   }

   private int findItemSlotHotbar(String itemName) {
      for (int i = 0; i < 9; i++) {
         ItemStack stack = this.mc.field_1724.method_31548().method_5438(i);
         if (!stack.method_7960()) {
            String name = stack.method_7964().getString().toLowerCase();
            if (name.contains(itemName)) {
               return i;
            }
         }
      }

      return -1;
   }

   private int findItemSlotInventory(String itemName) {
      for (int i = 9; i < 36; i++) {
         ItemStack stack = this.mc.field_1724.method_31548().method_5438(i);
         if (!stack.method_7960()) {
            String name = stack.method_7964().getString().toLowerCase();
            if (name.contains(itemName)) {
               return i;
            }
         }
      }

      return -1;
   }
}
