package zov.alphadlc.module.list.misc;

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
import zov.alphadlc.event.list.EventTick;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.module.settings.SliderSetting;
import zov.alphadlc.util.math.RotationUtil;
import zov.alphadlc.util.rotation.Rotation;
import zov.alphadlc.util.rotation.RotationComponent;

@ModuleInformation(
   moduleName = "ChestStealer",
   moduleDesc = "\u0417\u0430\u0431\u0438\u0440\u0430\u0435\u0442 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u044b \u0438\u0437 \u043a\u043e\u043d\u0442\u0435\u0439\u043d\u0435\u0440\u043e\u0432",
   moduleCategory = ModuleCategory.MISC
)
public class ChestStealer extends Module {
   private final SliderSetting delay = new SliderSetting("\u0417\u0430\u0434\u0435\u0440\u0436\u043a\u0430", 100.0, 0.0, 1000.0, 10.0);
   private final BooleanSetting autoOpen = new BooleanSetting("\u0410\u0432\u0442\u043e-\u043e\u0442\u043a\u0440\u044b\u0442\u0438\u0435", false);
   private long lastClick;

   @Subscribe
   private void onTick(EventTick ignored) {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null && this.mc.field_1761 != null) {
         ScreenHandler handler = this.mc.field_1724.field_7512;
         if (!(handler instanceof GenericContainerScreenHandler) && !(handler instanceof ShulkerBoxScreenHandler)) {
            if (this.autoOpen.getValue() && this.mc.field_1755 == null) {
               this.tryOpenChest();
            }
         } else {
            this.stealFrom(handler);
         }
      }
   }

   private void stealFrom(ScreenHandler handler) {
      if (!((double)(System.currentTimeMillis() - this.lastClick) < this.delay.getValue())) {
         for (Slot slot : handler.field_7761) {
            if (slot.method_7681() && slot.field_7871 != this.mc.field_1724.method_31548()) {
               this.mc.field_1761.method_2906(handler.field_7763, slot.field_7874, 0, SlotActionType.field_7794, this.mc.field_1724);
               this.lastClick = System.currentTimeMillis();
               return;
            }
         }
      }
   }

   private void tryOpenChest() {
      BlockPos chest = this.findNearestChest(5);
      if (chest != null) {
         Vec3d center = Vec3d.method_24953(chest);
         Vec2f angle = RotationUtil.calculate(center);
         RotationComponent.update(new Rotation(angle.field_1343, angle.field_1342), 60.0F, 60.0F, 2, 2);
         float tickDelta = this.mc.method_61966().method_60637(true);
         if (this.mc.field_1724.method_5745(5.0, tickDelta, false) instanceof BlockHitResult blockHit && blockHit.method_17783() == Type.field_1332) {
            BlockState state = this.mc.field_1687.method_8320(blockHit.method_17777());
            if (state.method_27852(Blocks.field_10034)
               || state.method_27852(Blocks.field_10380)
               || state.method_27852(Blocks.field_10443)
               || state.method_26204() instanceof ShulkerBoxBlock) {
               this.mc.field_1761.method_2896(this.mc.field_1724, Hand.field_5808, blockHit);
               this.mc.field_1724.method_6104(Hand.field_5808);
            }
         }
      }
   }

   private BlockPos findNearestChest(int range) {
      BlockPos playerPos = this.mc.field_1724.method_24515();
      BlockPos best = null;
      double bestDist = Double.MAX_VALUE;

      for (BlockPos pos : BlockPos.method_10097(playerPos.method_10069(-range, -range, -range), playerPos.method_10069(range, range, range))) {
         BlockState state = this.mc.field_1687.method_8320(pos);
         boolean container = state.method_27852(Blocks.field_10034)
            || state.method_27852(Blocks.field_10380)
            || state.method_27852(Blocks.field_10443)
            || state.method_26204() instanceof ShulkerBoxBlock;
         if (container) {
            double dist = this.mc.field_1724.method_5707(Vec3d.method_24953(pos));
            if (dist < bestDist && dist <= 25.0) {
               bestDist = dist;
               best = pos.method_10062();
            }
         }
      }

      return best;
   }
}
