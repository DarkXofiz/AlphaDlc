package zov.alphadlc.module.list.movement;

import com.google.common.eventbus.Subscribe;
import java.util.List;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import zov.alphadlc.event.list.EventTick;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.util.math.RotationUtil;
import zov.alphadlc.util.player.other.InventoryUtil;
import zov.alphadlc.util.player.other.SlownessManager;
import zov.alphadlc.util.player.other.WorldUtils;
import zov.alphadlc.util.rotation.Rotation;
import zov.alphadlc.util.rotation.RotationComponent;

@ModuleInformation(
   moduleName = "High Jump",
   moduleDesc = "\u0423\u0432\u0435\u043b\u0438\u0447\u0438\u0432\u0430\u0435\u0442 \u0432\u044b\u0441\u043e\u0442\u0443 \u043f\u0440\u044b\u0436\u043a\u0430",
   moduleCategory = ModuleCategory.MOVEMENT
)
public class HighJump extends Module {
   private boolean hasPlaced;
   private boolean hasOpened;
   private int pickedSlot;
   private BlockPos shulkerPos;

   @Subscribe
   public void onUpdate(EventTick ignored) {
      if (this.mc.field_1724 != null) {
         int slot = InventoryUtil.searchItem(List.of(Items.field_8545, Items.field_8268, Items.field_8584, Items.field_8213, Items.field_8350));
         if (this.hasOpened) {
            this.pushPlayer();
         } else if (slot != -1) {
            BlockPos nearestBlock = WorldUtils.findNearestPlaceableBlock();
            if (nearestBlock != null) {
               RotationComponent.update(new Rotation(RotationUtil.calculate(nearestBlock.method_46558())), 360.0F, 360.0F, 360.0F, 360.0F, 0, 10, false);
               SlownessManager.addTask(
                  new SlownessManager.SlowTask(
                     0L,
                     () -> {
                        if (!this.hasPlaced) {
                           if (this.mc.field_1765 instanceof BlockHitResult hitResult) {
                              this.shulkerPos = nearestBlock;
                              if (slot >= 0 && slot <= 8) {
                                 this.pickedSlot = slot;
                                 this.mc.field_1724.method_31548().field_7545 = slot;
                              } else {
                                 InventoryUtil.swapWithBypassGrim(
                                    () -> {
                                       this.pickedSlot = slot;
                                       this.mc
                                          .field_1761
                                          .method_2906(0, slot, this.mc.field_1724.method_31548().field_7545, SlotActionType.field_7791, this.mc.field_1724);
                                    }
                                 );
                              }

                              this.mc.field_1761.method_2896(this.mc.field_1724, Hand.field_5808, hitResult);
                              if (this.pickedSlot >= 0 && this.pickedSlot <= 8) {
                                 this.mc.field_1724.method_31548().field_7545 = this.pickedSlot;
                              } else {
                                 InventoryUtil.swapWithBypassGrim(
                                    () -> this.mc
                                          .field_1761
                                          .method_2906(
                                             0, this.pickedSlot, this.mc.field_1724.method_31548().field_7545, SlotActionType.field_7791, this.mc.field_1724
                                          )
                                 );
                              }

                              this.hasPlaced = true;
                           }

                           if (this.hasOpened) {
                              return;
                           }

                           RotationComponent.update(
                              new Rotation(RotationUtil.calculate(this.shulkerPos.method_46558())), 360.0F, 360.0F, 360.0F, 360.0F, 0, 1424821, false
                           );
                           SlownessManager.addTask(new SlownessManager.SlowTask(50L, () -> {
                              if (this.shulkerPos != null) {
                                 if (this.mc.field_1765 instanceof BlockHitResult hitResultx) {
                                    this.mc.field_1761.method_2896(this.mc.field_1724, Hand.field_5808, hitResultx);
                                 }

                                 this.hasOpened = true;
                              }
                           }));
                        }
                     }
                  )
               );
            }
         }
      }
   }

   private void pushPlayer() {
      for (BlockPos pos : BlockPos.method_10097(
         this.mc.field_1724.method_24515().method_10069(-6, -6, -6), this.mc.field_1724.method_24515().method_10069(6, 6, 6)
      )) {
         BlockEntity be = this.mc.field_1687.method_8321(pos);
         if (be instanceof ShulkerBoxBlockEntity) {
            ShulkerBoxBlockEntity shulker = (ShulkerBoxBlockEntity)be;
            double dx = this.mc.field_1724.method_23317() - ((double)pos.method_10263() + 0.5);
            double dz = this.mc.field_1724.method_23321() - ((double)pos.method_10260() + 0.5);
            double dy = this.mc.field_1724.method_23318() - ((double)pos.method_10264() + 0.5);
            double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
            if (horizontalDistance > 1.5) {
               this.setEnabled(false);
            } else if (!(Math.abs(dy) > (this.mc.field_1724.method_18798().field_1351 > 1.0 ? 30.0 : 2.0)) && this.mc.field_1724.method_24828()) {
               float progress = shulker.method_11312(1.0F);
               if (progress > 0.0F && progress <= 1.0F) {
                  Vec3d vel = this.mc.field_1724.method_18798();
                  this.mc.field_1724.method_18800(vel.field_1352, 2.33F, vel.field_1350);
                  this.mc.field_1724.field_6007 = true;
                  this.setEnabled(false);
                  break;
               }
            }
         }
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.hasOpened = false;
      this.hasPlaced = false;
   }
}
