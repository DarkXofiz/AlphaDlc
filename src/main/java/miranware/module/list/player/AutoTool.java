package miranware.module.list.player;

import com.google.common.eventbus.Subscribe;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import miranware.event.list.EventPlayerUpdate;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.ModeSetting;
import miranware.util.player.other.InventoryUtil;

@ModuleInformation(
   moduleName = "Auto Tool",
   moduleDesc = "Selects the best tool for mining blocks",
   moduleCategory = ModuleCategory.PLAYER
)
public class AutoTool extends Module {
   private final ModeSetting mode = new ModeSetting("Mode", "Vanilla", "Vanilla", "Grim");
   private int itemIndex = -1;
   private int oldSlot = -1;
   private boolean status;

   @Subscribe
   private void onTick(EventPlayerUpdate ignored) {
      if (this.mc.player == null || this.mc.player.isCreative()) {
         this.itemIndex = -1;
      } else if (!this.mc.player.isUsingItem()) {
         if (this.isMousePressed()) {
            if (this.oldSlot == -1) {
               this.itemIndex = this.findBestToolSlot();
               if (this.itemIndex != -1) {
                  if (this.itemIndex >= 0 && this.itemIndex <= 8) {
                     this.oldSlot = this.mc.player.getInventory().selectedSlot;
                     this.mc.player.getInventory().selectedSlot = this.itemIndex;
                     this.mc.interactionManager.syncSelectedSlot();
                     this.status = true;
                  } else {
                     String var2 = this.mode.getValue();
                     switch (var2) {
                        case "Vanilla":
                           this.swap();
                           break;
                        case "Grim":
                           InventoryUtil.swapWithBypassGrim(this::swap);
                           break;
                        case "ReallyWorld":
                           if (this.mc.player.isOnGround()) {
                              InventoryUtil.swapWithBypassPolar(this::swap);
                           } else {
                              InventoryUtil.swapWithBypassGrim(this::swap);
                           }
                     }

                     this.oldSlot = this.itemIndex;
                  }
               }
            }
         } else if (this.oldSlot != -1) {
            if (this.oldSlot >= 0 && this.oldSlot <= 8) {
               this.mc.player.getInventory().selectedSlot = this.oldSlot;
               this.mc.interactionManager.syncSelectedSlot();
               this.oldSlot = -1;
               this.status = false;
            } else {
               String var4 = this.mode.getValue();
               switch (var4) {
                  case "Vanilla":
                     this.swapBack();
                     break;
                  case "Grim":
                     InventoryUtil.swapWithBypassGrim(this::swapBack);
                     break;
                  case "ReallyWorld":
                     if (this.mc.player.isOnGround()) {
                        InventoryUtil.swapWithBypassPolar(this::swapBack);
                     } else {
                        InventoryUtil.swapWithBypassGrim(this::swapBack);
                     }
               }
            }
         }
      }
   }

   private void swap() {
      if (this.itemIndex != -1) {
         this.status = true;
         this.mc.interactionManager.clickSlot(0, this.itemIndex, this.mc.player.getInventory().selectedSlot, SlotActionType.SWAP, this.mc.player);
      }
   }

   private void swapBack() {
      if (this.oldSlot != -1 && this.status) {
         this.mc.interactionManager.clickSlot(0, this.oldSlot, this.mc.player.getInventory().selectedSlot, SlotActionType.SWAP, this.mc.player);
         this.oldSlot = -1;
         this.status = false;
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.status = false;
      this.itemIndex = -1;
      this.oldSlot = -1;
   }

   private int findBestToolSlot() {
      if (this.mc.crosshairTarget instanceof BlockHitResult blockHitResult) {
         BlockPos pos = blockHitResult.getBlockPos();
         BlockState state = this.mc.world.getBlockState(pos);
         int bestSlot = -1;
         float bestSpeed = 1.0F;

         for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = this.mc.player.getInventory().getStack(slot);
            float speed = stack.getMiningSpeedMultiplier(state);
            if (speed > bestSpeed) {
               bestSpeed = speed;
               bestSlot = slot;
            }
         }

         return bestSlot;
      } else {
         return -1;
      }
   }

   private boolean isMousePressed() {
      return this.mc.crosshairTarget != null && this.mc.options.attackKey.isPressed();
   }
}
