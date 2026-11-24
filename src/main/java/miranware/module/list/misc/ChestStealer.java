package miranware.module.list.misc;

import com.google.common.eventbus.Subscribe;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import miranware.event.list.EventTick;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.BooleanSetting;
import miranware.module.settings.SliderSetting;
import miranware.util.math.RotationUtil;
import miranware.util.rotation.Rotation;
import miranware.util.rotation.RotationComponent;

@ModuleInformation(
   moduleName = "ChestStealer",
   moduleDesc = "Takes items from containers",
   moduleCategory = ModuleCategory.MISC
)
public class ChestStealer extends Module {
   private final SliderSetting delay = new SliderSetting("Delay", 100.0, 0.0, 1000.0, 10.0);
   private final BooleanSetting autoOpen = new BooleanSetting("Auto Open", false);
   private long lastClick;

   @Subscribe
   private void onTick(EventTick ignored) {
      if (this.mc.player != null && this.mc.world != null && this.mc.interactionManager != null) {
         ScreenHandler handler = this.mc.player.currentScreenHandler;
         if (!(handler instanceof GenericContainerScreenHandler) && !(handler instanceof ShulkerBoxScreenHandler)) {
            if (this.autoOpen.getValue() && this.mc.currentScreen == null) {
               this.tryOpenChest();
            }
         } else {
            this.stealFrom(handler);
         }
      }
   }

   private void stealFrom(ScreenHandler handler) {
      if (!((double)(System.currentTimeMillis() - this.lastClick) < this.delay.getValue())) {
         for (Slot slot : handler.slots) {
            if (slot.hasStack() && slot.inventory != this.mc.player.getInventory()) {
               this.mc.interactionManager.clickSlot(handler.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, this.mc.player);
               this.lastClick = System.currentTimeMillis();
               return;
            }
         }
      }
   }

   private void tryOpenChest() {
      BlockPos chest = this.findNearestChest(5);
      if (chest != null) {
         Vec3d center = Vec3d.ofCenter(chest);
         Vec2f angle = RotationUtil.calculate(center);
         RotationComponent.update(new Rotation(angle.x, angle.y), 60.0F, 60.0F, 2, 2);
         float tickDelta = this.mc.getRenderTickCounter().getTickDelta(true);
         if (this.mc.player.raycast(5.0, tickDelta, false) instanceof BlockHitResult blockHit && blockHit.getType() == Type.BLOCK) {
            BlockState state = this.mc.world.getBlockState(blockHit.getBlockPos());
            if (state.isOf(Blocks.CHEST)
               || state.isOf(Blocks.TRAPPED_CHEST)
               || state.isOf(Blocks.ENDER_CHEST)
               || state.getBlock() instanceof ShulkerBoxBlock) {
               this.mc.interactionManager.interactBlock(this.mc.player, Hand.MAIN_HAND, blockHit);
               this.mc.player.swingHand(Hand.MAIN_HAND);
            }
         }
      }
   }

   private BlockPos findNearestChest(int range) {
      BlockPos playerPos = this.mc.player.getBlockPos();
      BlockPos best = null;
      double bestDist = Double.MAX_VALUE;

      for (BlockPos pos : BlockPos.iterate(playerPos.add(-range, -range, -range), playerPos.add(range, range, range))) {
         BlockState state = this.mc.world.getBlockState(pos);
         boolean container = state.isOf(Blocks.CHEST)
            || state.isOf(Blocks.TRAPPED_CHEST)
            || state.isOf(Blocks.ENDER_CHEST)
            || state.getBlock() instanceof ShulkerBoxBlock;
         if (container) {
            double dist = this.mc.player.squaredDistanceTo(Vec3d.ofCenter(pos));
            if (dist < bestDist && dist <= 25.0) {
               bestDist = dist;
               best = pos.toImmutable();
            }
         }
      }

      return best;
   }
}
