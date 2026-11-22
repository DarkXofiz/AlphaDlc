package zov.alphadlc.module.list.combat;

import com.google.common.eventbus.Subscribe;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import zov.alphadlc.event.list.EventTick;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.module.settings.SliderSetting;
import zov.alphadlc.util.friend.FriendRepository;

@ModuleInformation(
   moduleName = "CrystalAura",
   moduleDesc = "\u0410\u0432\u0442\u043e\u0430\u0442\u0430\u043a\u0430 \u044d\u043d\u0434\u0435\u0440 \u043a\u0440\u0438\u0441\u0442\u0430\u043b\u043b\u0430\u043c\u0438",
   moduleCategory = ModuleCategory.COMBAT
)
public class CrystalAura extends Module {
   private final BooleanSetting autoPlace = new BooleanSetting("\u0420\u0430\u0437\u043c\u0435\u0449\u0435\u043d\u0438\u0435", true);
   private final BooleanSetting autoBreak = new BooleanSetting("\u0412\u0437\u0440\u044b\u0432", true);
   private final BooleanSetting noPlayer = new BooleanSetting("\u041d\u0435 \u0432\u0437\u0440\u044b\u0432\u0430\u0442\u044c \u0441\u0435\u0431\u044f", true);
   private final SliderSetting range = new SliderSetting("Range", 4.0, 1.0, 6.0, 0.1F);
   private final SliderSetting placeDelay = new SliderSetting("Place Delay", 0.0, 0.0, 20.0, 1.0);
   private final SliderSetting breakDelay = new SliderSetting("Break Delay", 0.0, 0.0, 20.0, 1.0);
   private float rotationYaw;
   private float rotationPitch;
   private boolean rotating = false;
   private EndCrystalEntity targetCrystal = null;
   private BlockPos targetPos = null;
   private int breakTimer = 0;
   private int placeTimer = 0;
   private BlockPos lastPlacedPos = null;
   private long lastPlaceTime = 0L;

   @Subscribe
   public void onTick(EventTick event) {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
         PlayerEntity target = this.findTarget();
         if (target == null) {
            this.rotating = false;
         } else {
            if (this.autoBreak.getValue()) {
               this.targetCrystal = this.findBestCrystal(target);
               if (this.targetCrystal != null) {
                  this.calcGrimRotations(this.targetCrystal.method_5829().method_1005());
                  if (this.rotating && this.mc.field_1724 != null) {
                     this.mc.field_1724.method_36456(this.rotationYaw);
                     this.mc.field_1724.method_36457(this.rotationPitch);
                  }

                  if ((double)this.breakTimer >= this.breakDelay.getValue()) {
                     this.attackCrystal(this.targetCrystal);
                     this.breakTimer = 0;
                  } else {
                     this.breakTimer++;
                  }
               }
            }

            if (this.autoPlace.getValue()) {
               this.targetPos = this.findBestPlacePos(target);
               if (this.targetPos != null && this.targetCrystal == null) {
                  if (this.lastPlacedPos != null && this.lastPlacedPos.equals(this.targetPos) && System.currentTimeMillis() - this.lastPlaceTime < 50L) {
                     return;
                  }

                  if (!this.hasCrystalInHand()) {
                     this.switchToCrystal();
                  }

                  if (this.hasCrystalInHand()) {
                     Vec3d placeVec = this.targetPos.method_46558().method_1031(0.0, 1.0, 0.0);
                     this.calcGrimRotations(placeVec);
                     if (this.rotating && this.mc.field_1724 != null) {
                        this.mc.field_1724.method_36456(this.rotationYaw);
                        this.mc.field_1724.method_36457(this.rotationPitch);
                     }

                     if ((double)this.placeTimer >= this.placeDelay.getValue()) {
                        this.placeCrystal(this.targetPos);
                        this.lastPlacedPos = this.targetPos;
                        this.lastPlaceTime = System.currentTimeMillis();
                        this.placeTimer = 0;
                     } else {
                        this.placeTimer++;
                     }
                  }
               }
            }
         }
      }
   }

   private PlayerEntity findTarget() {
      if (this.mc.field_1687 != null && this.mc.field_1724 != null) {
         PlayerEntity closest = null;
         double closestDist = Double.MAX_VALUE;

         for (PlayerEntity player : this.mc.field_1687.method_18456()) {
            if (player != this.mc.field_1724
               && !player.method_29504()
               && !(player.method_6032() <= 0.0F)
               && !FriendRepository.getFriends().contains(player.method_5477().getString())) {
               double dist = (double)this.mc.field_1724.method_5739(player);
               if (!(dist > this.range.getValue()) && dist < closestDist) {
                  closestDist = dist;
                  closest = player;
               }
            }
         }

         return closest;
      } else {
         return null;
      }
   }

   private EndCrystalEntity findBestCrystal(PlayerEntity target) {
      if (this.mc.field_1687 != null && this.mc.field_1724 != null) {
         EndCrystalEntity bestCrystal = null;
         double closestDistance = Double.MAX_VALUE;

         for (Entity entity : this.mc.field_1687.method_18112()) {
            if (entity instanceof EndCrystalEntity) {
               EndCrystalEntity crystal = (EndCrystalEntity)entity;
               if (crystal.method_5805()) {
                  double distance = (double)this.mc.field_1724.method_5739(crystal);
                  if (!(distance > this.range.getValue())) {
                     if (target != null) {
                        double targetY = target.method_23318();
                        double crystalY = crystal.method_23318();
                        if (targetY < crystalY - 1.0) {
                           continue;
                        }
                     }

                     float crystalYCheck = (float)crystal.method_23318();
                     float playerY = (float)this.mc.field_1724.method_23318() + 0.5F;
                     if ((!this.noPlayer.getValue() || !(crystalYCheck < playerY)) && distance < closestDistance) {
                        closestDistance = distance;
                        bestCrystal = crystal;
                     }
                  }
               }
            }
         }

         return bestCrystal;
      } else {
         return null;
      }
   }

   private BlockPos findBestPlacePos(PlayerEntity target) {
      if (this.mc.field_1687 != null && this.mc.field_1724 != null && target != null) {
         BlockPos playerPos = this.mc.field_1724.method_24515();
         BlockPos bestPos = null;
         double closestToTarget = Double.MAX_VALUE;
         int r = (int)Math.ceil(this.range.getValue());

         for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
               for (int z = -r; z <= r; z++) {
                  BlockPos pos = playerPos.method_10069(x, y, z);
                  if (this.canPlaceCrystal(pos)) {
                     double distToPlayer = this.mc.field_1724.method_19538().method_1022(pos.method_46558());
                     if (!(distToPlayer > this.range.getValue())) {
                        double targetY = target.method_23318();
                        double blockY = (double)pos.method_10264();
                        if (!(targetY < blockY - 0.2)) {
                           double distToTarget = target.method_19538().method_1022(pos.method_46558());
                           if (distToTarget < closestToTarget) {
                              closestToTarget = distToTarget;
                              bestPos = pos;
                           }
                        }
                     }
                  }
               }
            }
         }

         return bestPos;
      } else {
         return null;
      }
   }

   private boolean canPlaceCrystal(BlockPos pos) {
      if (this.mc.field_1687 == null) {
         return false;
      } else if (!this.mc.field_1687.method_8320(pos).method_27852(Blocks.field_10540) && !this.mc.field_1687.method_8320(pos).method_27852(Blocks.field_9987)) {
         return false;
      } else if (!this.mc.field_1687.method_8320(pos.method_10084()).method_26215()) {
         return false;
      } else if (!this.mc.field_1687.method_8320(pos.method_10086(2)).method_26215()) {
         return false;
      } else {
         Box box = new Box(pos.method_10084()).method_1009(0.0, 1.0, 0.0);

         for (Entity entity : this.mc.field_1687.method_8335(null, box)) {
            if (!(entity instanceof EndCrystalEntity)) {
               return false;
            }
         }

         return true;
      }
   }

   private void placeCrystal(BlockPos pos) {
      if (this.mc.field_1724 != null && this.mc.field_1761 != null && this.mc.field_1687 != null) {
         boolean offhand = this.mc.field_1724.method_6079().method_31574(Items.field_8301);
         boolean mainhand = this.mc.field_1724.method_6047().method_31574(Items.field_8301);
         if (offhand || mainhand) {
            Hand hand = offhand ? Hand.field_5810 : Hand.field_5808;
            Vec3d hitVec = new Vec3d((double)pos.method_10263() + 0.5, (double)pos.method_10264() + 1.0, (double)pos.method_10260() + 0.5);
            BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.field_11036, pos, false);
            this.mc.field_1761.method_2896(this.mc.field_1724, hand, hitResult);
            this.mc.field_1724.method_6104(hand);
         }
      }
   }

   private boolean hasCrystalInHand() {
      return this.mc.field_1724 == null
         ? false
         : this.mc.field_1724.method_6079().method_31574(Items.field_8301) || this.mc.field_1724.method_6047().method_31574(Items.field_8301);
   }

   private void switchToCrystal() {
      if (this.mc.field_1724 != null) {
         if (!this.mc.field_1724.method_6079().method_31574(Items.field_8301)) {
            for (int i = 0; i < 9; i++) {
               if (this.mc.field_1724.method_31548().method_5438(i).method_31574(Items.field_8301)) {
                  this.mc.field_1724.method_31548().field_7545 = i;
                  return;
               }
            }
         }
      }
   }

   private void calcGrimRotations(Vec3d vec) {
      if (this.mc.field_1724 != null) {
         float yawDelta = MathHelper.method_15393(
            (float)MathHelper.method_15338(
                  Math.toDegrees(Math.atan2(vec.field_1350 - this.mc.field_1724.method_23321(), vec.field_1352 - this.mc.field_1724.method_23317())) - 90.0
               )
               - this.rotationYaw
         );
         float pitchDelta = (float)(
               -Math.toDegrees(
                  Math.atan2(
                     vec.field_1351
                        - (this.mc.field_1724.method_19538().field_1351 + (double)this.mc.field_1724.method_18381(this.mc.field_1724.method_18376())),
                     Math.sqrt(
                        Math.pow(vec.field_1352 - this.mc.field_1724.method_23317(), 2.0) + Math.pow(vec.field_1350 - this.mc.field_1724.method_23321(), 2.0)
                     )
                  )
               )
            )
            - this.rotationPitch;
         float angleToRad = (float)Math.toRadians((double)(27 * (this.mc.field_1724.field_6012 % 30)));
         yawDelta = (float)((double)yawDelta + Math.sin((double)angleToRad) * 3.0) + this.randomFloat(-1.0F, 1.0F);
         pitchDelta += this.randomFloat(-0.6F, 0.6F);
         if (yawDelta > 180.0F) {
            yawDelta -= 180.0F;
         }

         float clampedYawDelta = MathHelper.method_15363(Math.abs(yawDelta), -180.0F, 180.0F);
         float clampedPitchDelta = MathHelper.method_15363(pitchDelta, -45.0F, 45.0F);
         float newYaw = this.rotationYaw + (yawDelta > 0.0F ? clampedYawDelta : -clampedYawDelta);
         float newPitch = MathHelper.method_15363(this.rotationPitch + clampedPitchDelta, -90.0F, 90.0F);
         double gcdFix = Math.pow((Double)this.mc.field_1690.method_42495().method_41753() * 0.6 + 0.2, 3.0) * 1.2;
         this.rotationYaw = (float)((double)newYaw - (double)(newYaw - this.rotationYaw) % gcdFix);
         this.rotationPitch = (float)((double)newPitch - (double)(newPitch - this.rotationPitch) % gcdFix);
         this.rotating = true;
      }
   }

   private void attackCrystal(EndCrystalEntity crystal) {
      if (this.mc.field_1724 != null && this.mc.field_1761 != null) {
         this.mc.field_1761.method_2918(this.mc.field_1724, crystal);
         this.mc.field_1724.method_6104(Hand.field_5808);
      }
   }

   private float randomFloat(float min, float max) {
      return min + (float)Math.random() * (max - min);
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.rotating = false;
      this.targetCrystal = null;
      this.targetPos = null;
      this.lastPlacedPos = null;
      this.breakTimer = 0;
      this.placeTimer = 0;
   }
}
