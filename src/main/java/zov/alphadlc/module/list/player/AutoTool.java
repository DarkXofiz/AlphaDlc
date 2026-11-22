package zov.alphadlc.module.list.player;

import com.google.common.eventbus.Subscribe;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import zov.alphadlc.event.list.EventPlayerUpdate;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.ModeSetting;
import zov.alphadlc.util.player.other.InventoryUtil;

@ModuleInformation(
   moduleName = "Auto Tool",
   moduleDesc = "\u0412\u044b\u0431\u0438\u0440\u0430\u0435\u0442 \u043b\u0443\u0447\u0448\u0438\u0439 \u0438\u043d\u0441\u0442\u0440\u0443\u043c\u0435\u043d\u0442 \u0434\u043b\u044f \u0434\u043e\u0431\u044b\u0447\u0438 \u0431\u043b\u043e\u043a\u043e\u0432",
   moduleCategory = ModuleCategory.PLAYER
)
public class AutoTool extends Module {
   private final ModeSetting mode = new ModeSetting("\u041c\u043e\u0434", "Vanilla", "Vanilla", "Grim");
   private int itemIndex = -1;
   private int oldSlot = -1;
   private boolean status;

   @Subscribe
   private void onTick(EventPlayerUpdate ignored) {
      if (this.mc.field_1724 == null || this.mc.field_1724.method_7337()) {
         this.itemIndex = -1;
      } else if (!this.mc.field_1724.method_6115()) {
         if (this.isMousePressed()) {
            if (this.oldSlot == -1) {
               this.itemIndex = this.findBestToolSlot();
               if (this.itemIndex != -1) {
                  if (this.itemIndex >= 0 && this.itemIndex <= 8) {
                     this.oldSlot = this.mc.field_1724.method_31548().field_7545;
                     this.mc.field_1724.method_31548().field_7545 = this.itemIndex;
                     this.mc.field_1761.method_2911();
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
                           if (this.mc.field_1724.method_24828()) {
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
               this.mc.field_1724.method_31548().field_7545 = this.oldSlot;
               this.mc.field_1761.method_2911();
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
                     if (this.mc.field_1724.method_24828()) {
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
         this.mc.field_1761.method_2906(0, this.itemIndex, this.mc.field_1724.method_31548().field_7545, SlotActionType.field_7791, this.mc.field_1724);
      }
   }

   private void swapBack() {
      if (this.oldSlot != -1 && this.status) {
         this.mc.field_1761.method_2906(0, this.oldSlot, this.mc.field_1724.method_31548().field_7545, SlotActionType.field_7791, this.mc.field_1724);
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
      if (this.mc.field_1765 instanceof BlockHitResult blockHitResult) {
         BlockPos pos = blockHitResult.method_17777();
         BlockState state = this.mc.field_1687.method_8320(pos);
         int bestSlot = -1;
         float bestSpeed = 1.0F;

         for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = this.mc.field_1724.method_31548().method_5438(slot);
            float speed = stack.method_7924(state);
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
      return this.mc.field_1765 != null && this.mc.field_1690.field_1886.method_1434();
   }
}
