package miranware.module.list.movement;

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
import miranware.event.list.EventTick;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.util.math.RotationUtil;
import miranware.util.player.other.InventoryUtil;
import miranware.util.player.other.SlownessManager;
import miranware.util.player.other.WorldUtils;
import miranware.util.rotation.Rotation;
import miranware.util.rotation.RotationComponent;

@ModuleInformation(
   moduleName = "High Jump",
   moduleDesc = "Increases jump height",
   moduleCategory = ModuleCategory.MOVEMENT
)
public class HighJump extends Module {
   private boolean hasPlaced;
   private boolean hasOpened;
   private int pickedSlot;
   private BlockPos shulkerPos;

   @Subscribe
   public void onUpdate(EventTick ignored) {
      if (this.mc.player != null) {
         int slot = InventoryUtil.searchItem(List.of(Items.SHULKER_BOX, Items.BLACK_SHULKER_BOX, Items.BROWN_SHULKER_BOX, Items.CYAN_SHULKER_BOX, Items.BLUE_SHULKER_BOX));
         if (this.hasOpened) {
            this.pushPlayer();
         } else if (slot != -1) {
            BlockPos nearestBlock = WorldUtils.findNearestPlaceableBlock();
            if (nearestBlock != null) {
               RotationComponent.update(new Rotation(RotationUtil.calculate(nearestBlock.toCenterPos())), 360.0F, 360.0F, 360.0F, 360.0F, 0, 10, false);
               SlownessManager.addTask(
                  new SlownessManager.SlowTask(
                     0L,
                     () -> {
                        if (!this.hasPlaced) {
                           if (this.mc.crosshairTarget instanceof BlockHitResult hitResult) {
                              this.shulkerPos = nearestBlock;
                              if (slot >= 0 && slot <= 8) {
                                 this.pickedSlot = slot;
                                 this.mc.player.getInventory().selectedSlot = slot;
                              } else {
                                 InventoryUtil.swapWithBypassGrim(
                                    () -> {
                                       this.pickedSlot = slot;
                                       this.mc
                                          .interactionManager
                                          .clickSlot(0, slot, this.mc.player.getInventory().selectedSlot, SlotActionType.SWAP, this.mc.player);
                                    }
                                 );
                              }

                              this.mc.interactionManager.interactBlock(this.mc.player, Hand.MAIN_HAND, hitResult);
                              if (this.pickedSlot >= 0 && this.pickedSlot <= 8) {
                                 this.mc.player.getInventory().selectedSlot = this.pickedSlot;
                              } else {
                                 InventoryUtil.swapWithBypassGrim(
                                    () -> this.mc
                                          .interactionManager
                                          .clickSlot(
                                             0, this.pickedSlot, this.mc.player.getInventory().selectedSlot, SlotActionType.SWAP, this.mc.player
                                          )
                                 );
                              }

                              this.hasPlaced = true;
                           }

                           if (this.hasOpened) {
                              return;
                           }

                           RotationComponent.update(
                              new Rotation(RotationUtil.calculate(this.shulkerPos.toCenterPos())), 360.0F, 360.0F, 360.0F, 360.0F, 0, 1424821, false
                           );
                           SlownessManager.addTask(new SlownessManager.SlowTask(50L, () -> {
                              if (this.shulkerPos != null) {
                                 if (this.mc.crosshairTarget instanceof BlockHitResult hitResultx) {
                                    this.mc.interactionManager.interactBlock(this.mc.player, Hand.MAIN_HAND, hitResultx);
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
      for (BlockPos pos : BlockPos.iterate(
         this.mc.player.getBlockPos().add(-6, -6, -6), this.mc.player.getBlockPos().add(6, 6, 6)
      )) {
         BlockEntity be = this.mc.world.getBlockEntity(pos);
         if (be instanceof ShulkerBoxBlockEntity) {
            ShulkerBoxBlockEntity shulker = (ShulkerBoxBlockEntity)be;
            double dx = this.mc.player.getX() - ((double)pos.getX() + 0.5);
            double dz = this.mc.player.getZ() - ((double)pos.getZ() + 0.5);
            double dy = this.mc.player.getY() - ((double)pos.getY() + 0.5);
            double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
            if (horizontalDistance > 1.5) {
               this.setEnabled(false);
            } else if (!(Math.abs(dy) > (this.mc.player.getVelocity().y > 1.0 ? 30.0 : 2.0)) && this.mc.player.isOnGround()) {
               float progress = shulker.getAnimationProgress(1.0F);
               if (progress > 0.0F && progress <= 1.0F) {
                  Vec3d vel = this.mc.player.getVelocity();
                  this.mc.player.setVelocity(vel.x, 2.33F, vel.z);
                  this.mc.player.velocityDirty = true;
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
