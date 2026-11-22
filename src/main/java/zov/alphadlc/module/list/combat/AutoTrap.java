package zov.alphadlc.module.list.combat;

import com.google.common.eventbus.Subscribe;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult.Success;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import zov.alphadlc.event.list.EventPlayerSyncEnd;
import zov.alphadlc.event.list.EventPlayerUpdate;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.module.settings.SliderSetting;
import zov.alphadlc.util.friend.FriendRepository;
import zov.alphadlc.util.math.RotationUtil;
import zov.alphadlc.util.math.StopWatch;
import zov.alphadlc.util.player.other.InventoryUtil;
import zov.alphadlc.util.rotation.Rotation;
import zov.alphadlc.util.rotation.RotationComponent;

@ModuleInformation(
   moduleName = "AutoTrap",
   moduleDesc = "\u0421\u0442\u0430\u0432\u0438\u0442 \u0434\u0432\u043e\u0439\u043d\u0443\u044e \u043f\u0430\u0443\u0442\u0438\u043d\u0443 \u0432 \u043d\u043e\u0433\u0438 \u0438 \u0440\u043e\u0441\u0442 \u0446\u0435\u043b\u0438",
   moduleCategory = ModuleCategory.COMBAT
)
public class AutoTrap extends Module {
   private final SliderSetting range = new SliderSetting("\u0420\u0430\u0434\u0438\u0443\u0441", 3.2F, 2.0, 4.5, 0.1F);
   private final SliderSetting delay = new SliderSetting("\u0417\u0430\u0434\u0435\u0440\u0436\u043a\u0430 (\u0442\u0438\u043a\u0438)", 7.0, 4.0, 12.0, 1.0);
   private final BooleanSetting pauseInGui = new BooleanSetting("\u041f\u0430\u0443\u0437\u0430 \u0432 GUI", true);
   private final BooleanSetting predict = new BooleanSetting("\u041f\u0440\u0435\u0434\u0438\u043a\u0442", true);
   private final SliderSetting predictTicks = new SliderSetting(
         "\u041f\u0440\u0435\u0434\u0438\u043a\u0442 \u0442\u0438\u043a\u043e\u0432", 3.0, 1.0, 10.0, 1.0
      )
      .setVisible(this.predict::getValue);
   private final StopWatch timer = new StopWatch();

   @Subscribe
   private void onUpdate(EventPlayerUpdate ignored) {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
         if (!this.pauseInGui.getValue() || this.mc.field_1755 == null) {
            if (this.timer.isReached((long)(this.delay.getValue() * 50.0))) {
               PlayerEntity target = this.findTarget();
               if (target != null) {
                  BlockPos placePos = this.getTrapPosition(target);
                  if (placePos != null) {
                     BlockHitResult hit = this.buildHit(placePos);
                     if (hit != null) {
                        Vec2f rot = RotationUtil.calculate(this.mc.field_1724.method_33571(), hit.method_17784());
                        RotationComponent.update(new Rotation(rot), 65.0F, 65.0F, 180.0F, 2, 6);
                     }
                  }
               }
            }
         }
      }
   }

   @Subscribe
   private void onSyncEnd(EventPlayerSyncEnd ignored) {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
         if (!this.pauseInGui.getValue() || this.mc.field_1755 == null) {
            if (this.timer.isReached((long)(this.delay.getValue() * 50.0))) {
               PlayerEntity target = this.findTarget();
               if (target != null) {
                  int slot = InventoryUtil.searchItemHotbar(Items.field_8786);
                  if (slot != -1) {
                     BlockPos trapPos = this.getTrapPosition(target);
                     if (trapPos != null) {
                        BlockPos headPos = trapPos.method_10084();
                        boolean placedAny = false;
                        if (this.mc.field_1687.method_8320(trapPos).method_26215()) {
                           BlockHitResult hit = this.buildHit(trapPos);
                           if (hit != null && this.placeWeb(hit, slot)) {
                              placedAny = true;
                           }
                        }

                        if (this.mc.field_1687.method_8320(headPos).method_26215()) {
                           BlockHitResult hit = this.buildHit(headPos);
                           if (hit != null && this.placeWeb(hit, slot)) {
                              placedAny = true;
                           }
                        }

                        if (placedAny) {
                           this.timer.reset();
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private boolean placeWeb(BlockHitResult hit, int slot) {
      int previous = this.mc.field_1724.method_31548().field_7545;
      this.mc.field_1724.method_31548().field_7545 = slot;
      this.mc.field_1761.method_2911();
      ActionResult result = this.mc.field_1761.method_2896(this.mc.field_1724, Hand.field_5808, hit);
      if (result instanceof Success) {
         this.mc.field_1724.method_6104(Hand.field_5808);
      }

      this.mc.field_1724.method_31548().field_7545 = previous;
      this.mc.field_1761.method_2911();
      return result instanceof Success;
   }

   private PlayerEntity findTarget() {
      PlayerEntity best = null;
      double bestDistance = Double.MAX_VALUE;

      for (PlayerEntity player : this.mc.field_1687.method_18456()) {
         if (player != this.mc.field_1724 && player.method_5805() && !FriendRepository.isFriend(player.method_5820())) {
            double distance = (double)this.mc.field_1724.method_5739(player);
            if (!(distance > this.range.getValue()) && distance < bestDistance) {
               bestDistance = distance;
               best = player;
            }
         }
      }

      return best;
   }

   private BlockPos getTrapPosition(PlayerEntity target) {
      if (!this.predict.getValue()) {
         return target.method_24515();
      } else {
         Vec3d velocity = new Vec3d(
            target.method_23317() - target.field_6014, target.method_23318() - target.field_6036, target.method_23321() - target.field_5969
         );
         if (velocity.method_1027() < 0.001) {
            return target.method_24515();
         } else {
            Vec3d predictedPos = target.method_19538().method_1019(velocity.method_1021(this.predictTicks.getValue()));
            return BlockPos.method_49638(predictedPos);
         }
      }
   }

   private BlockHitResult buildHit(BlockPos placePos) {
      if (!this.mc.field_1687.method_8320(placePos).method_26215()) {
         return null;
      } else {
         for (Direction dir : Direction.values()) {
            BlockPos support = placePos.method_10093(dir);
            BlockState state = this.mc.field_1687.method_8320(support);
            if (!state.method_26215() && !state.method_27852(Blocks.field_10343)) {
               Direction face = dir.method_10153();
               Vec3d center = new Vec3d(
                  (double)support.method_10263() + 0.5 + (double)face.method_10148() * 0.5,
                  (double)support.method_10264() + 0.5 + (double)face.method_10164() * 0.5,
                  (double)support.method_10260() + 0.5 + (double)face.method_10165() * 0.5
               );
               return new BlockHitResult(center, face, support, false);
            }
         }

         return null;
      }
   }
}
