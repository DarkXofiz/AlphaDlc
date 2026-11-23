package miranware.module.list.player;

import com.google.common.eventbus.Subscribe;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import miranware.event.list.EventPacket;
import miranware.mixin.SlotAccessor;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.BooleanSetting;
import miranware.module.settings.ModeListSetting;
import miranware.util.chat.ChatUtil;

@ModuleInformation(
   moduleName = "LockSlot",
   moduleDesc = "Prevents dropping items from slots",
   moduleCategory = ModuleCategory.PLAYER
)
public class LockSlot extends Module {
   private final ModeListSetting slots = new ModeListSetting(
      "Slots",
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
         "Dropping the item from slot "
            + (slot + 1)
            + " is blocked"
      );
   }
}
