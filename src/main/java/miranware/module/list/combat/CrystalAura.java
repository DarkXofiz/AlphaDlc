package miranware.module.list.combat;

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
import miranware.event.list.EventTick;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.BooleanSetting;
import miranware.module.settings.SliderSetting;
import miranware.util.friend.FriendRepository;

@ModuleInformation(
   moduleName = "CrystalAura",
   moduleDesc = "Auto attack with end crystals",
   moduleCategory = ModuleCategory.COMBAT
)
public class CrystalAura extends Module {
   private final BooleanSetting autoPlace = new BooleanSetting("Placement", true);
   private final BooleanSetting autoBreak = new BooleanSetting("Explosion", true);
   private final BooleanSetting noPlayer = new BooleanSetting("Don't Explode Self", true);
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
      if (this.mc.player != null && this.mc.world != null) {
         PlayerEntity target = this.findTarget();
         if (target == null) {
            this.rotating = false;
         } else {
            if (this.autoBreak.getValue()) {
               this.targetCrystal = this.findBestCrystal(target);
               if (this.targetCrystal != null) {
                  this.calcGrimRotations(this.targetCrystal.getBoundingBox().getCenter());
                  if (this.rotating && this.mc.player != null) {
                     this.mc.player.setYaw(this.rotationYaw);
                     this.mc.player.setPitch(this.rotationPitch);
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
                     Vec3d placeVec = this.targetPos.toCenterPos().add(0.0, 1.0, 0.0);
                     this.calcGrimRotations(placeVec);
                     if (this.rotating && this.mc.player != null) {
                        this.mc.player.setYaw(this.rotationYaw);
                        this.mc.player.setPitch(this.rotationPitch);
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
      if (this.mc.world != null && this.mc.player != null) {
         PlayerEntity closest = null;
         double closestDist = Double.MAX_VALUE;

         for (PlayerEntity player : this.mc.world.getPlayers()) {
            if (player != this.mc.player
               && !player.isDead()
               && !(player.getHealth() <= 0.0F)
               && !FriendRepository.getFriends().contains(player.getName().getString())) {
               double dist = (double)this.mc.player.distanceTo(player);
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
      if (this.mc.world != null && this.mc.player != null) {
         EndCrystalEntity bestCrystal = null;
         double closestDistance = Double.MAX_VALUE;

         for (Entity entity : this.mc.world.getEntities()) {
            if (entity instanceof EndCrystalEntity) {
               EndCrystalEntity crystal = (EndCrystalEntity)entity;
               if (crystal.isAlive()) {
                  double distance = (double)this.mc.player.distanceTo(crystal);
                  if (!(distance > this.range.getValue())) {
                     if (target != null) {
                        double targetY = target.getY();
                        double crystalY = crystal.getY();
                        if (targetY < crystalY - 1.0) {
                           continue;
                        }
                     }

                     float crystalYCheck = (float)crystal.getY();
                     float playerY = (float)this.mc.player.getY() + 0.5F;
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
      if (this.mc.world != null && this.mc.player != null && target != null) {
         BlockPos playerPos = this.mc.player.getBlockPos();
         BlockPos bestPos = null;
         double closestToTarget = Double.MAX_VALUE;
         int r = (int)Math.ceil(this.range.getValue());

         for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
               for (int z = -r; z <= r; z++) {
                  BlockPos pos = playerPos.add(x, y, z);
                  if (this.canPlaceCrystal(pos)) {
                     double distToPlayer = this.mc.player.getPos().distanceTo(pos.toCenterPos());
                     if (!(distToPlayer > this.range.getValue())) {
                        double targetY = target.getY();
                        double blockY = (double)pos.getY();
                        if (!(targetY < blockY - 0.2)) {
                           double distToTarget = target.getPos().distanceTo(pos.toCenterPos());
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
      if (this.mc.world == null) {
         return false;
      } else if (!this.mc.world.getBlockState(pos).isOf(Blocks.OBSIDIAN) && !this.mc.world.getBlockState(pos).isOf(Blocks.BEDROCK)) {
         return false;
      } else if (!this.mc.world.getBlockState(pos.up()).isAir()) {
         return false;
      } else if (!this.mc.world.getBlockState(pos.up(2)).isAir()) {
         return false;
      } else {
         Box box = new Box(pos.up()).expand(0.0, 1.0, 0.0);

         for (Entity entity : this.mc.world.getOtherEntities(null, box)) {
            if (!(entity instanceof EndCrystalEntity)) {
               return false;
            }
         }

         return true;
      }
   }

   private void placeCrystal(BlockPos pos) {
      if (this.mc.player != null && this.mc.interactionManager != null && this.mc.world != null) {
         boolean offhand = this.mc.player.getOffHandStack().isOf(Items.END_CRYSTAL);
         boolean mainhand = this.mc.player.getMainHandStack().isOf(Items.END_CRYSTAL);
         if (offhand || mainhand) {
            Hand hand = offhand ? Hand.OFF_HAND : Hand.MAIN_HAND;
            Vec3d hitVec = new Vec3d((double)pos.getX() + 0.5, (double)pos.getY() + 1.0, (double)pos.getZ() + 0.5);
            BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.UP, pos, false);
            this.mc.interactionManager.interactBlock(this.mc.player, hand, hitResult);
            this.mc.player.swingHand(hand);
         }
      }
   }

   private boolean hasCrystalInHand() {
      return this.mc.player == null
         ? false
         : this.mc.player.getOffHandStack().isOf(Items.END_CRYSTAL) || this.mc.player.getMainHandStack().isOf(Items.END_CRYSTAL);
   }

   private void switchToCrystal() {
      if (this.mc.player != null) {
         if (!this.mc.player.getOffHandStack().isOf(Items.END_CRYSTAL)) {
            for (int i = 0; i < 9; i++) {
               if (this.mc.player.getInventory().getStack(i).isOf(Items.END_CRYSTAL)) {
                  this.mc.player.getInventory().selectedSlot = i;
                  return;
               }
            }
         }
      }
   }

   private void calcGrimRotations(Vec3d vec) {
      if (this.mc.player != null) {
         float yawDelta = MathHelper.wrapDegrees(
            (float)MathHelper.wrapDegrees(
                  Math.toDegrees(Math.atan2(vec.z - this.mc.player.getZ(), vec.x - this.mc.player.getX())) - 90.0
               )
               - this.rotationYaw
         );
         float pitchDelta = (float)(
               -Math.toDegrees(
                  Math.atan2(
                     vec.y
                        - (this.mc.player.getPos().y + (double)this.mc.player.getEyeHeight(this.mc.player.getPose())),
                     Math.sqrt(
                        Math.pow(vec.x - this.mc.player.getX(), 2.0) + Math.pow(vec.z - this.mc.player.getZ(), 2.0)
                     )
                  )
               )
            )
            - this.rotationPitch;
         float angleToRad = (float)Math.toRadians((double)(27 * (this.mc.player.age % 30)));
         yawDelta = (float)((double)yawDelta + Math.sin((double)angleToRad) * 3.0) + this.randomFloat(-1.0F, 1.0F);
         pitchDelta += this.randomFloat(-0.6F, 0.6F);
         if (yawDelta > 180.0F) {
            yawDelta -= 180.0F;
         }

         float clampedYawDelta = MathHelper.clamp(Math.abs(yawDelta), -180.0F, 180.0F);
         float clampedPitchDelta = MathHelper.clamp(pitchDelta, -45.0F, 45.0F);
         float newYaw = this.rotationYaw + (yawDelta > 0.0F ? clampedYawDelta : -clampedYawDelta);
         float newPitch = MathHelper.clamp(this.rotationPitch + clampedPitchDelta, -90.0F, 90.0F);
         double gcdFix = Math.pow((Double)this.mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2, 3.0) * 1.2;
         this.rotationYaw = (float)((double)newYaw - (double)(newYaw - this.rotationYaw) % gcdFix);
         this.rotationPitch = (float)((double)newPitch - (double)(newPitch - this.rotationPitch) % gcdFix);
         this.rotating = true;
      }
   }

   private void attackCrystal(EndCrystalEntity crystal) {
      if (this.mc.player != null && this.mc.interactionManager != null) {
         this.mc.interactionManager.attackEntity(this.mc.player, crystal);
         this.mc.player.swingHand(Hand.MAIN_HAND);
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
