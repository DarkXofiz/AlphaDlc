package miranware.module.list.misc;

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
import miranware.MiranWare;
import miranware.event.list.EventKeyInput;
import miranware.event.list.EventPlayerUpdate;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.list.movement.Sprint;
import miranware.module.settings.BindSetting;
import miranware.util.base.Instance;
import miranware.util.packet.NetworkUtils;

@ModuleInformation(
   moduleName = "Sunrise Helper",
   moduleDesc = "Helper for Sunrise",
   moduleCategory = ModuleCategory.MISC
)
public class SunriseHelper extends Module {
   private final BindSetting bindTrap = new BindSetting("Trap", -1);
   private final BindSetting bindFire = new BindSetting("Fire Wave", -1);
   private boolean throwTrap;
   private boolean throwFire;

   @Subscribe
   public void onKey(EventKeyInput e) {
      if (this.mc.currentScreen == null && e.getAction() != 0 && e.getAction() != 2) {
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
      if (this.mc.player != null && this.mc.world != null) {
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
      int previousSlot = this.mc.player.getInventory().selectedSlot;
      String mainHandName = this.mc.player.getMainHandStack().getName().getString().toLowerCase();
      if (mainHandName.contains(itemName)) {
         this.mc.interactionManager.interactItem(this.mc.player, Hand.MAIN_HAND);
      } else {
         String offHandName = this.mc.player.getOffHandStack().getName().getString().toLowerCase();
         if (offHandName.contains(itemName)) {
            this.mc.interactionManager.interactItem(this.mc.player, Hand.OFF_HAND);
         } else if (slotHotbar != -1) {
            this.mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slotHotbar));
            this.mc.interactionManager.interactItem(this.mc.player, Hand.MAIN_HAND);
            this.mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
         } else {
            if (slotInv != -1) {
               int slotCorrectable = -1;

               for (int slotNone = 0; slotNone < 8; slotNone++) {
                  ItemStack stack = this.mc.player.getInventory().getStack(slotNone);
                  if (stack.isEmpty()) {
                     slotCorrectable = slotNone;
                  }

                  if (stack.getUseAction() == UseAction.NONE) {
                     slotCorrectable = slotNone;
                  }
               }

               this.mc.getNetworkHandler().sendPacket(new PlayerInputC2SPacket(new PlayerInput(false, false, false, false, false, false, false)));
               if (MiranWare.getInstance().getServerManager().isServerSprinting()) {
                  this.mc.player.setSprinting(false);
                  this.mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(this.mc.player, Mode.STOP_SPRINTING));
                  if (!Instance.get(Sprint.class).isEnabled()) {
                     this.mc.options.sprintKey.setPressed(false);
                  }
               }

               if (slotCorrectable == -1) {
                  this.mc.interactionManager.clickSlot(0, slotInv, 8, SlotActionType.SWAP, this.mc.player);
                  NetworkUtils.sendSilentPacket(new CloseHandledScreenC2SPacket(0));
                  this.mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(8));
                  this.mc.interactionManager.interactItem(this.mc.player, Hand.MAIN_HAND);
                  this.mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
               } else {
                  this.mc.interactionManager.clickSlot(0, slotInv, slotCorrectable, SlotActionType.SWAP, this.mc.player);
                  NetworkUtils.sendSilentPacket(new CloseHandledScreenC2SPacket(0));
                  this.mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slotCorrectable));
                  this.mc.interactionManager.interactItem(this.mc.player, Hand.MAIN_HAND);
                  this.mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
                  this.mc.interactionManager.clickSlot(0, slotInv, slotCorrectable, SlotActionType.SWAP, this.mc.player);
                  NetworkUtils.sendSilentPacket(new CloseHandledScreenC2SPacket(0));
               }

               this.mc.getNetworkHandler().sendPacket(new PlayerInputC2SPacket(this.mc.player.input.playerInput));
            }
         }
      }
   }

   private int findItemSlotHotbar(String itemName) {
      for (int i = 0; i < 9; i++) {
         ItemStack stack = this.mc.player.getInventory().getStack(i);
         if (!stack.isEmpty()) {
            String name = stack.getName().getString().toLowerCase();
            if (name.contains(itemName)) {
               return i;
            }
         }
      }

      return -1;
   }

   private int findItemSlotInventory(String itemName) {
      for (int i = 9; i < 36; i++) {
         ItemStack stack = this.mc.player.getInventory().getStack(i);
         if (!stack.isEmpty()) {
            String name = stack.getName().getString().toLowerCase();
            if (name.contains(itemName)) {
               return i;
            }
         }
      }

      return -1;
   }
}
