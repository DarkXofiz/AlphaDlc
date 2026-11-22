package zov.alphadlc.module.list.player;

import com.google.common.eventbus.Subscribe;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import zov.alphadlc.event.list.EventPacket;
import zov.alphadlc.mixin.SlotAccessor;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.module.settings.ModeListSetting;
import zov.alphadlc.util.chat.ChatUtil;

@ModuleInformation(
   moduleName = "LockSlot",
   moduleDesc = "\u0411\u043b\u043e\u043a\u0438\u0440\u043e\u0432\u043a\u0430 \u0432\u044b\u0431\u0440\u043e\u0441\u0430 \u0438\u0437 \u0441\u043b\u043e\u0442\u043e\u0432",
   moduleCategory = ModuleCategory.PLAYER
)
public class LockSlot extends Module {
   private final ModeListSetting slots = new ModeListSetting(
      "\u0421\u043b\u043e\u0442\u044b",
      new BooleanSetting("1", false),
      new BooleanSetting("2", false),
      new BooleanSetting("3", false),
      new BooleanSetting("4", false),
      new BooleanSetting("5", false),
      new BooleanSetting("6", false),
      new BooleanSetting("7", false),
      new BooleanSetting("8", false),
      new BooleanSetting("9", false)
   );

   @Subscribe
   private void onPacket(EventPacket e2) {
      if (this.mc.player != null && e2.getType() == EventPacket.Type.SEND) {
         if (!(this.mc.currentScreen instanceof HandledScreen)) {
            if (e2.getPacket() instanceof PlayerActionC2SPacket packet3) {
               if (packet3.getAction() == Action.DROP_ITEM || packet3.getAction() == Action.DROP_ALL_ITEMS) {
                  if (this.isCurrentSlotLockedForDrop()) {
                     e2.cancelEvent();
                     this.sendLockedMessage(this.mc.player.getInventory().selectedSlot);
                  }
               }
            } else {
               Packet<?> var6 = e2.getPacket();
               int hotbarSlot;
               ClickSlotC2SPacket packet;
               if (var6 instanceof ClickSlotC2SPacket
                  && (packet = (ClickSlotC2SPacket)var6).getActionType() == SlotActionType.THROW
                  && (hotbarSlot = this.getHotbarSlotFromClick(packet.getSlot())) >= 0
                  && this.isHotbarSlotLocked(hotbarSlot)) {
                  e2.cancelEvent();
                  this.sendLockedMessage(hotbarSlot);
               }
            }
         }
      }
   }

   public boolean isCurrentSlotLockedForDrop() {
      if (!this.isEnabled() || this.mc.player == null || this.mc.player.getMainHandStack().isEmpty()) {
         return false;
      } else {
         return this.mc.currentScreen instanceof HandledScreen ? false : this.isHotbarSlotLocked(this.mc.player.getInventory().selectedSlot);
      }
   }

   private boolean isHotbarSlotLocked(int slot) {
      return slot >= 0 && slot < this.slots.getSettings().size() ? this.slots.getSettings().get(slot).getValue() : false;
   }

   private int getHotbarSlotFromClick(int slotId) {
      if (this.mc.player != null && slotId >= 0 && slotId < this.mc.player.currentScreenHandler.slots.size()) {
         Slot slot = this.mc.player.currentScreenHandler.getSlot(slotId);
         SlotAccessor accessor = (SlotAccessor)slot;
         int inventoryIndex = accessor.getIndex();
         return accessor.getInventory() == this.mc.player.getInventory() && inventoryIndex >= 0 && inventoryIndex <= 9 ? inventoryIndex : -1;
      } else {
         return -1;
      }
   }

   private void sendLockedMessage(int slot) {
      ChatUtil.send(
         "\u0412\u044b\u0431\u0440\u043e\u0441 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u0430 \u0438\u0437 \u0441\u043b\u043e\u0442\u0430 "
            + (slot + 1)
            + " \u0437\u0430\u0431\u043b\u043e\u043a\u0438\u0440\u043e\u0432\u0430\u043d"
      );
   }
}
