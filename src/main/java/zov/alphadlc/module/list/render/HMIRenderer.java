package zov.alphadlc.module.list.render;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.AttributeModifiersComponent.Entry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.BlockItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.LingeringPotionItem;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.consume.UseAction;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public class HMIRenderer {
   private static double prevTime = 0.0;
   public static double deltaTime = 0.0;
   private boolean repPower = false;
   private float prevAge = 0.0F;
   private double previousRotation = 0.0;
   private float swingAngleY = 0.0F;
   private float swingAngleX = 0.0F;
   private float swingVelocityY = 0.0F;
   private float swingVelocityX = 0.0F;
   private float swingVelocityZ = 0.0F;
   private float vertAngleY = 0.0F;
   private float vertVelocityYSlime = 0.0F;
   private float vertAngleYSlime = 0.0F;
   private float riptideCounter = 0.0F;
   private float netherCounter = 0.0F;
   private float fallCounter = 0.0F;
   private float inWaterCounter = 0.0F;
   private float freezeCounter = 0.0F;
   private float clCount = 0.0F;
   private float crawlCount = 0.0F;
   private float directionalCrawlCount = 0.0F;
   private float climbCount = 0.0F;
   private float mouseHolding = 1.0F;
   private boolean isAttacking = false;
   private boolean left = false;
   private final boolean[] wasSwinging = new boolean[]{false, false};
   private final float[] swingReturnProgress = new float[]{0.0F, 0.0F};
   private final float[] lastSwing = new float[]{0.0F, 0.0F};
   private final float[] lastSwingRot = new float[]{0.0F, 0.0F};
   private SwingAnimations swingAnimations;
   private static final boolean ENABLE_SWIMMING_ANIM = true;
   private static final boolean ENABLE_CLIMB_AND_CRAWL = true;
   private static final boolean ENABLE_PUNCHING = true;
   private static final boolean MB3D_COMPAT = false;

   private float getEffectivePitch(AbstractClientPlayerEntity player) {
      float pitch = player.getPitch();
      if (!player.isSwimming() && !player.isCrawling() && !player.isClimbing()) {
         boolean nearGround = player.isOnGround();
         if (!nearGround) {
            double feetY = player.getY();

            for (int i = 0; i <= 2; i++) {
               BlockPos checkPos = BlockPos.ofFloored(player.getX(), feetY - (double)i, player.getZ());
               if (player.getWorld().getBlockState(checkPos).isSolid()) {
                  double dist = feetY - (double)checkPos.getY() - 1.0;
                  if (dist <= 1.5) {
                     nearGround = true;
                  }
                  break;
               }
            }
         }

         if (!nearGround) {
            return pitch;
         } else {
            for (Entity entity : player.getWorld().getOtherEntities(player, player.getBoundingBox().expand(6.0))) {
               double yDiff = entity.getY() - player.getY();
               if (yDiff < -1.9 || yDiff > 2.5) {
                  return pitch;
               }
            }

            return MathHelper.clamp(pitch, 35.0F, 48.0F);
         }
      } else {
         return pitch;
      }
   }

   public void setSwingAnimations(SwingAnimations sa) {
      this.swingAnimations = sa;
   }

   public void updateDeltaTime() {
      long currentTime = System.nanoTime();
      double currentSec = (double)currentTime / 1.0E9;
      deltaTime = currentSec - prevTime;
      prevTime = currentSec;
      if (MinecraftClient.getInstance().isPaused()) {
         deltaTime = 0.0;
      } else {
         deltaTime = Math.min(0.05, deltaTime);
      }
   }

   private float easeInOutBack(float x) {
      float c1 = 1.70158F;
      float c2 = c1 * 1.525F;
      return (float)(
         (double)x < 0.5
            ? Math.pow(2.0 * (double)x, 2.0) * (((double)c2 + 1.0) * 2.0 * (double)x - (double)c2) / 2.0
            : (Math.pow(2.0 * (double)x - 2.0, 2.0) * (((double)c2 + 1.0) * (double)(x * 2.0F - 2.0F) + (double)c2) + 2.0) / 2.0
      );
   }

   private float getAttackDamage(ItemStack stack) {
      float totalDamage = 0.0F;
      AttributeModifiersComponent modifiers = (AttributeModifiersComponent)stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
      if (modifiers == null) {
         return totalDamage;
      } else {
         for (Entry entry : modifiers.comp_2393()) {
            if (entry.comp_2395().equals(EntityAttributes.ATTACK_DAMAGE)) {
               totalDamage += (float)entry.comp_2396().comp_2449();
            }
         }

         return totalDamage;
      }
   }

   private void altSwing(MatrixStack matrices, Arm arm, float swingProgress) {
      int direction = arm == Arm.RIGHT ? 1 : -1;
      float swingSin = MathHelper.sin(swingProgress * (float) Math.PI);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)direction * (45.0F + swingSin * 0.0F)));
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)direction * -45.0F));
   }

   private boolean isSword(ItemStack stack) {
      return stack.isIn(ItemTags.SWORDS) ? true : stack.getItem() instanceof SwordItem;
   }

   private boolean isTool(ItemStack stack) {
      return stack.isIn(ItemTags.AXES)
         || stack.isIn(ItemTags.HOES)
         || stack.isIn(ItemTags.PICKAXES)
         || stack.isIn(ItemTags.SHOVELS)
         || this.isSword(stack);
   }

   private boolean isSmallBlockItem(ItemStack stack) {
      Block block = Block.getBlockFromItem(stack.getItem());
      BlockState state = block.getDefaultState();
      return stack.isOf(Items.STRING)
         || stack.isOf(Items.REDSTONE)
         || stack.isOf(Items.LEVER)
         || stack.isOf(Items.TRIPWIRE_HOOK)
         || state.isIn(BlockTags.IMPERMEABLE)
         || state.isIn(BlockTags.RAILS)
         || state.isIn(BlockTags.CLIMBABLE)
         || stack.isIn(ItemTags.DOORS);
   }

   public void renderHMI(
      HeldItemRenderer renderer,
      HeldItemRendererAccessor acc,
      AbstractClientPlayerEntity player,
      float tickDelta,
      float pitch,
      Hand hand,
      float swingProgress,
      ItemStack stack,
      float equipProgress,
      MatrixStack matrices,
      VertexConsumerProvider vertexConsumers,
      int light
   ) {
      this.updateDeltaTime();
      boolean useVanillaLike = stack.getUseAction() == UseAction.BOW || stack.getUseAction() == UseAction.SPEAR;
      if (useVanillaLike) {
         boolean bl = hand == Hand.MAIN_HAND;
         Arm arm = bl ? player.getMainArm() : player.getMainArm().getOpposite();
         float kj = bl ? 1.0F : -1.0F;
         int l = arm == Arm.RIGHT ? 1 : -1;
         matrices.push();
         if (this.swingAnimations != null) {
            double vx = this.swingAnimations.hmiPosX.getValue();
            double vy = this.swingAnimations.hmiPosY.getValue();
            double vz = this.swingAnimations.hmiPosZ.getValue();
            matrices.translate(vx * (double)kj, vy, vz);
         }

         if (player.isUsingItem() && player.getItemUseTimeLeft() > 0 && player.getActiveHand() == hand) {
            UseAction useAction = stack.getUseAction();
            if (useAction == UseAction.BOW) {
               matrices.push();
               float m1 = (float)stack.getMaxUseTime(player) - ((float)player.getItemUseTimeLeft() - tickDelta + 1.0F);
               float f1 = m1 / 20.0F;
               if (f1 > 1.0F) {
                  f1 = 1.0F;
               }

               if (f1 > 0.1F) {
                  float g1 = MathHelper.sin((m1 - 0.1F) * 1.3F);
                  float j1 = g1 * f1;
                  matrices.translate(j1 * 0.0F, j1 * 0.004F, j1 * 0.0F);
               }

               acc.invokeApplyItemArmTransform(matrices, arm, equipProgress);
               matrices.translate((float)l * -0.2785682F, 0.18312F, 0.15731531F);
               matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-13.935F));
               matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)l * 35.3F));
               matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)l * -9.785F));
               float h = (float)stack.getMaxUseTime(player) - ((float)player.getItemUseTimeLeft() - tickDelta + 1.0F);
               float g = h / 20.0F;
               g = (g * g + g * 2.0F) / 3.0F;
               if (g > 1.0F) {
                  g = 1.0F;
               }

               if (g > 0.1F) {
                  float i = MathHelper.sin((h - 0.1F) * 1.3F);
                  float j = g - 0.1F;
                  float k = i * j;
                  matrices.translate(k * 0.0F, k * 0.004F, k * 0.0F);
               }

               matrices.translate(g * 0.0F, g * 0.0F, g * 0.04F);
               matrices.scale(1.0F, 1.0F, 1.0F + g * 0.2F);
               matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float)l * 45.0F));
               acc.invokeRenderItem(
                  player,
                  stack,
                  arm == Arm.RIGHT ? ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : ModelTransformationMode.FIRST_PERSON_LEFT_HAND,
                  arm != Arm.RIGHT,
                  matrices,
                  vertexConsumers,
                  light
               );
               matrices.pop();
            } else if (useAction == UseAction.SPEAR) {
               acc.invokeApplyItemArmTransform(matrices, arm, equipProgress);
               float dt = (float)stack.getMaxUseTime(player) - ((float)player.getItemUseTimeLeft() - tickDelta + 1.0F);
               float f = dt / 10.0F;
               if (f > 1.0F) {
                  f = 1.0F;
               }

               if (f > 0.1F) {
                  float gx = MathHelper.sin((dt - 0.1F) * 1.3F);
                  float j = gx * (f - 0.1F);
                  matrices.translate(j * 0.0F, j * 0.004F, j * 0.0F);
               }

               matrices.translate(0.0F, 0.0F, 0.1F);
               matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-10.0F));
               matrices.translate(0.0F, 0.0F, -0.1F);
               acc.invokeRenderItem(
                  player,
                  stack,
                  arm == Arm.RIGHT ? ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : ModelTransformationMode.FIRST_PERSON_LEFT_HAND,
                  arm != Arm.RIGHT,
                  matrices,
                  vertexConsumers,
                  light
               );
            }
         } else {
            acc.invokeSwingArm(swingProgress, equipProgress, matrices, bl ? 1 : -1, arm);
            acc.invokeRenderItem(
               player, stack, bl ? ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : ModelTransformationMode.FIRST_PERSON_LEFT_HAND, !bl, matrices, vertexConsumers, light
            );
         }

         matrices.pop();
      } else {
         float yaw = player.getYaw();
         double radians = Math.toRadians((double)yaw);
         double forwardX = -Math.sin(radians);
         double forwardZ = Math.cos(radians);
         Vec3d vel = player.getVelocity();
         double dotProduct = vel.x * forwardX + vel.z * forwardZ;
         double crossProduct = vel.x * forwardZ - vel.z * forwardX;
         float effectivePitch = this.getEffectivePitch(player);
         float al = effectivePitch != 0.0F ? 90.0F / effectivePitch / 10.0F : 1.0F;
         if (al > 1.0F) {
            al = 1.0F;
         }

         if (al < 0.0F) {
            al = 1.0F;
         }

         boolean blx = hand == Hand.MAIN_HAND;
         Arm armx = blx ? player.getMainArm() : player.getMainArm().getOpposite();
         float kjx = blx ? 1.0F : -1.0F;
         double vx = 0.0;
         double vy = 0.0;
         double vz = 0.0;
         double rx = 0.0;
         double ry = 0.0;
         double rz = 0.0;
         double scale = 1.0;
         double animSpeed = 8.0;
         if (this.swingAnimations != null) {
            vx = this.swingAnimations.hmiPosX.getValue();
            vy = this.swingAnimations.hmiPosY.getValue();
            vz = this.swingAnimations.hmiPosZ.getValue();
            rx = this.swingAnimations.hmiRotX.getValue();
            ry = this.swingAnimations.hmiRotY.getValue();
            rz = this.swingAnimations.hmiRotZ.getValue();
            scale = this.swingAnimations.hmiScale.getValue();
            animSpeed = 8.0;
         }

         matrices.push();
         matrices.push();
         matrices.translate(vx * (double)kjx, vy, vz);
         if (rx != 0.0) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((float)rx));
         }

         if (ry != 0.0) {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)ry * kjx));
         }

         if (rz != 0.0) {
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)rz * kjx));
         }

         if (scale != 1.0) {
            matrices.scale((float)scale, (float)scale, (float)scale);
         }

         double tt = deltaTime * animSpeed;
         float speedMultiplier = 1.0F;
         if (this.swingAnimations != null) {
            speedMultiplier = (float)(0.5 + this.swingAnimations.speed.getValue() / 10.0 * 2.0);
         }

         if (Block.getBlockFromItem(stack.getItem()) != Blocks.AIR
            && (
               !this.isTool(stack)
                  || stack.isIn(ItemTags.TRIMMABLE_ARMOR)
                  || stack.isIn(ItemTags.BOOKSHELF_BOOKS)
                  || stack.getUseAction() == UseAction.EAT
                  || !stack.isEnchantable()
            )
            && stack.getUseAction() != UseAction.BOW
            && stack.getUseAction() != UseAction.SPYGLASS
            && this.getAttackDamage(stack) == 0.0F
            && stack.getUseAction() != UseAction.BLOCK
            && !stack.isOf(Items.WARPED_FUNGUS_ON_A_STICK)
            && !stack.isOf(Items.CARROT_ON_A_STICK)
            && !(stack.getItem() instanceof FishingRodItem)
            && !stack.isOf(Items.SHEARS)) {
            swingProgress = (float)((double)swingProgress * 0.45 * (double)speedMultiplier);
            if (swingProgress > 1.0F) {
               swingProgress = 0.0F;
            }
         } else if (!stack.isIn(ItemTags.SHOVELS)) {
            swingProgress = (float)((double)swingProgress * 0.45 * (double)speedMultiplier);
            if (swingProgress > 1.0F) {
               swingProgress = 0.0F;
            }
         }

         float raw_swing_rot = (double)swingProgress < 0.6
            ? MathHelper.sin(MathHelper.clamp(swingProgress, 0.0F, 0.12506F) * 12.56F)
            : MathHelper.sin(MathHelper.clamp(swingProgress, 0.62532F, 0.75038F) * 12.56F);
         float raw_swing = MathHelper.sin(swingProgress * 3.14F);
         raw_swing = this.easeInOutBack(raw_swing);
         float swing_rot = raw_swing_rot;
         float swing = raw_swing;
         int handIdx = blx ? 0 : 1;
         if (swingProgress > 0.001F) {
            this.wasSwinging[handIdx] = true;
            this.lastSwing[handIdx] = raw_swing;
            this.lastSwingRot[handIdx] = raw_swing_rot;
            this.swingReturnProgress[handIdx] = 1.0F;
         } else if (this.wasSwinging[handIdx] && this.swingReturnProgress[handIdx] > 0.0F) {
            this.swingReturnProgress[handIdx] = this.swingReturnProgress[handIdx] - (float)(deltaTime * 5.5);
            if (this.swingReturnProgress[handIdx] < 0.0F) {
               this.swingReturnProgress[handIdx] = 0.0F;
            }

            float t = this.swingReturnProgress[handIdx] * this.swingReturnProgress[handIdx];
            swing = this.lastSwing[handIdx] * t;
            swing_rot = this.lastSwingRot[handIdx] * t;
         } else {
            this.wasSwinging[handIdx] = false;
            this.swingReturnProgress[handIdx] = 0.0F;
         }

         if ((
               stack.isOf(Items.EXPERIENCE_BOTTLE)
                  || stack.isOf(Items.EGG)
                  || stack.isOf(Items.ENDER_EYE)
                  || stack.isOf(Items.SNOWBALL)
                  || stack.isOf(Items.ENDER_PEARL)
                  || stack.getItem() instanceof SplashPotionItem
                  || stack.getItem() instanceof LingeringPotionItem
            )
            && player.getOffHandStack().isEmpty()
            && stack.getUseAction() != UseAction.SPEAR
            && !stack.isOf(Items.FIRE_CHARGE)
            && !player.isSwimming()
            && !player.isCrawling()
            && !player.isClimbing()) {
            if (player.getMainArm() == Arm.LEFT) {
               blx = !blx;
            }

            float ll = blx ? 1.0F : -1.0F;
            matrices.push();
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-25.0F * ll));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-10.0F));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(25.0F * ll * swing));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(30.0F * swing));
            matrices.translate(-0.15 * (double)ll, 0.1, 0.1);
            matrices.translate(0.0, -0.55 * (double)swing, 0.4 * (double)swing * 3.14F);
            acc.invokeRenderPlayerArm(matrices, vertexConsumers, light, equipProgress, 0.0F, armx.getOpposite());
            matrices.pop();
         }

         if (this.wasSwinging[handIdx] && this.swingReturnProgress[handIdx] <= 0.0F) {
            this.left = !this.left;
         }

         if (!stack.isEmpty()) {
            if (player.getMainArm() == Arm.LEFT) {
               blx = !blx;
            }

            float ll = blx ? 1.0F : -1.0F;
            if ((
                  this.left
                     || stack.isIn(ItemTags.AXES)
                     || stack.getUseAction() == UseAction.SPEAR
                     || stack.getUseAction() == UseAction.BLOCK
               )
               && !stack.isIn(ItemTags.SHOVELS)) {
               if (!this.isSword(stack) && !stack.isIn(ItemTags.AXES)) {
                  if (stack.getUseAction() == UseAction.SPEAR) {
                     matrices.translate(0.0, 0.0, 0.45 * (double)swing_rot);
                     matrices.translate((double)(-0.25F * kjx * swing), -0.35 * (double)swing_rot, -0.6 * (double)swing);
                     matrices.translate(0.0, 0.1 * (double)swing, 0.0);
                     matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(15.0F * swing_rot * ll));
                     matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(30.0F * swing_rot * ll));
                  } else if (this.isTool(stack) && stack.getUseAction() != UseAction.BLOCK && !stack.isIn(ItemTags.SHOVELS)) {
                     matrices.translate(0.1 * (double)ll * (double)swing_rot, 0.1 * (double)swing_rot, (double)(-0.5F * swing));
                     matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(-30.0F * swing_rot));
                     matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-20.0F * swing_rot * ll));
                     matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(40.0F * swing));
                  } else if (stack.getUseAction() != UseAction.BLOCK) {
                     matrices.translate(0.1 * (double)ll * (double)swing_rot, 0.1 * (double)swing_rot, -0.1 * (double)swing);
                     matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(-30.0F * swing_rot));
                     matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-10.0F * swing_rot * ll));
                     matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(40.0F * swing));
                     matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(10.0F * swing * ll));
                  } else {
                     matrices.translate(0.1 * (double)ll * (double)swing_rot, 0.1 * (double)swing_rot, -0.2 * (double)swing);
                     matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(-10.0F * swing_rot));
                     matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-10.0F * swing_rot * ll));
                     matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(20.0F * swing));
                  }
               } else {
                  matrices.translate(0.8 * (double)ll * (double)swing_rot, 0.3 * (double)swing_rot, (double)(-0.5F * swing));
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(15.0F * swing_rot * ll));
                  matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(-20.0F * swing_rot));
                  matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-70.0F * swing_rot * ll));
                  if (this.isSword(stack)) {
                     matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(40.0F * swing));
                  } else {
                     matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(30.0F * swing));
                  }
               }
            } else if (!stack.isIn(ItemTags.SHOVELS)) {
               if (this.isSword(stack)) {
                  matrices.translate(-0.55 * (double)ll * (double)swing_rot, -0.8 * (double)swing_rot, -0.77 * (double)swing);
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(5.0F * swing_rot * ll));
                  matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(-30.0F * swing_rot));
                  matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(70.0F * swing_rot * ll));
                  matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(50.0F * swing));
               } else if (this.isTool(stack) && !stack.isIn(ItemTags.SHOVELS)) {
                  matrices.translate(0.1 * (double)ll * (double)swing_rot, 0.1 * (double)swing_rot, (double)(-0.5F * swing));
                  matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(-30.0F * swing_rot));
                  matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-20.0F * swing_rot * ll));
                  matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(40.0F * swing));
               } else {
                  matrices.translate(0.1 * (double)ll * (double)swing_rot, 0.1 * (double)swing_rot, -0.1 * (double)swing);
                  matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(-30.0F * swing_rot));
                  matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-10.0F * swing_rot * ll));
                  matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(40.0F * swing));
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(10.0F * swing * ll));
               }
            } else if (stack.isIn(ItemTags.SHOVELS)) {
               matrices.translate(0.0, 0.15 * (double)swing_rot, (double)(-0.25F * swing_rot));
               matrices.translate(0.0, 0.0, -0.2 * (double)swing);
               matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(15.0F * swing_rot));
               matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-35.0F * swing_rot));
               matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(30.0F * swing));
            }
         }

         if (vel.length() >= 0.08) {
            this.crawlCount = (float)((double)this.crawlCount + 0.1 * vel.length() * 2.0 * tt);
            this.directionalCrawlCount = (float)((double)this.directionalCrawlCount + 0.1 * dotProduct * 4.0 * tt);
            this.directionalCrawlCount = (float)(
               (double)this.directionalCrawlCount
                  + (dotProduct > 0.0 ? 0.1 * Math.abs(crossProduct) * 4.0 * tt : 0.1 * Math.abs(crossProduct) * -1.0 * 4.0 * tt)
            );
         }

         if (vel.y > 0.0) {
            this.climbCount = (float)((double)this.climbCount + 0.1 * tt);
         }

         if (vel.y < 0.0) {
            this.climbCount = (float)((double)this.climbCount - 0.1 * tt);
         }

         if ((player.isCrawling() || player.isClimbing() && !player.isOnGround() && Math.abs(vel.y) > 0.0)
            && !player.isUsingItem()
            && swingProgress == 0.0F) {
            this.clCount = (float)((double)this.clCount + 0.1 * tt);
            if (this.clCount > 1.0F) {
               this.clCount = 1.0F;
            }

            if (!stack.isOf(Items.LANTERN) && !stack.isOf(Items.SOUL_LANTERN)) {
               matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-20.0F * this.clCount));
            }
         } else {
            this.clCount = (float)((double)this.clCount * Math.pow(0.88F, tt));
         }

         if (swingProgress == 0.0F) {
            matrices.translate(blx ? effectivePitch / 650.0F * this.clCount * -1.0F : effectivePitch / 650.0F * this.clCount, 0.0F, 0.0F);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(effectivePitch * this.clCount));
         }

         if (!stack.isOf(Items.LANTERN) && !stack.isOf(Items.SOUL_LANTERN)) {
            matrices.translate(0.0F, 0.0F, effectivePitch / 120.0F * this.clCount);
         } else if (swingProgress == 0.0F) {
            matrices.translate(0.0F, 0.0F, effectivePitch / 80.0F * this.clCount);
         }

         if (player.isClimbing()
            && !player.isOnGround()
            && !stack.isOf(Items.LANTERN)
            && !stack.isOf(Items.SOUL_LANTERN)
            && !player.isUsingItem()) {
            matrices.translate(0.0, 0.1, -0.2);
         }

         if ((player.isTouchingWater() || player.isFrozen()) && !player.isSwimming() && !player.isSubmergedInWater()) {
            this.inWaterCounter = (float)((double)this.inWaterCounter + 0.1 * tt);
            if (this.inWaterCounter >= 1.0F) {
               this.inWaterCounter = 1.0F;
            }
         } else {
            this.inWaterCounter = (float)((double)this.inWaterCounter * Math.pow(0.88F, tt));
         }

         float freezingScale = MathHelper.clamp((float)player.getFrozenTicks() / (float)player.getMinFreezeDamageTicks(), 0.0F, 1.0F);
         if (player.isFrozen() && (double)freezingScale > 0.1) {
            this.freezeCounter = (float)((double)this.freezeCounter + 0.1 * tt);
         } else {
            this.freezeCounter = (float)((double)this.freezeCounter * Math.pow(0.88F, tt));
         }

         matrices.translate(0.0, 0.02 * (double)this.inWaterCounter, 0.0);
         matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(8.0F * kjx * this.inWaterCounter));
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(0.3F * MathHelper.sin(this.freezeCounter * 5.0F)));
         if (vel.y < -0.85 && stack.isOf(Items.MACE) && player.getMainHandStack() == stack) {
            this.fallCounter = (float)((double)this.fallCounter + 0.1 * tt);
            if (this.fallCounter >= 1.0F) {
               this.fallCounter = 1.0F;
            }
         } else {
            this.fallCounter = (float)((double)this.fallCounter * Math.pow(0.88F, tt));
         }

         if (blx) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(45.0F * this.fallCounter));
            matrices.translate(0.0, -0.2 * (double)this.fallCounter, 0.0);
         }

         this.vertAngleY = (float)((double)this.vertAngleY + vel.y * 0.015F * tt);
         this.vertAngleY = (float)((double)this.vertAngleY - (double)(0.1F * this.vertAngleY) * tt);
         this.vertAngleY = (float)((double)this.vertAngleY * Math.pow(0.88F, tt));
         this.vertVelocityYSlime = (float)((double)this.vertVelocityYSlime + vel.y * 0.015F * tt);
         this.vertVelocityYSlime = (float)((double)this.vertVelocityYSlime - (double)(0.1F * this.vertAngleYSlime) * tt);
         this.vertVelocityYSlime = (float)((double)this.vertVelocityYSlime * Math.pow(0.88F, tt));
         this.vertAngleYSlime = (float)((double)this.vertAngleYSlime + (double)this.vertVelocityYSlime * tt);
         matrices.translate(0.0F, this.vertAngleY * -1.0F, 0.0F);
         matrices.translate(0.0, Math.sin((double)player.age * 0.1) * 0.007 * (double)kjx, 0.0);
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(0.15F * MathHelper.sin((float)player.age * 0.15F) * kjx));
         if (!stack.isEmpty() || player.isCrawling() || player.isClimbing() && !player.isOnGround() || player.isSwimming()) {
            if (player.getMainArm() == Arm.LEFT) {
               blx = !blx;
            }

            if (stack.getUseAction() == UseAction.BLOCK) {
               matrices.translate(0.0F, 0.0F, 0.0F);
            } else {
               matrices.translate(0.0, -0.1, 0.1);
            }
         }

         if (stack.isOf(Items.LANTERN) || stack.isOf(Items.SOUL_LANTERN) || stack.isIn(ItemTags.HANGING_SIGNS)) {
            matrices.translate(0.0, 0.1, 0.0);
            if (player.isSwimming()) {
               matrices.translate(0.0, -0.1, 0.1);
            }
         }

         if (player.isSwimming() && swingProgress == 0.0F) {
            double s = (double)((float)player.age + tickDelta) * 0.1 * 2.0;
            double handRotation = Math.sin(s) * 1.5;
            double smoothRotation = handRotation * 0.8 + this.previousRotation * 0.2;
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)(blx ? smoothRotation : -smoothRotation)));
            matrices.translate(0.0F, 0.0F, (float)(smoothRotation * 0.2));
            double k = (double)((float)player.age + tickDelta) * 0.2;
            double a = Math.cos(k);
            double b = a <= 0.0 ? a * 0.5 : a;
            matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float)(blx ? b * 30.0 : b * -30.0)));
            matrices.translate(0.0F, 0.0F, (float)(a * 0.2));
            if (stack.isEmpty() && !blx && !player.isInvisible()) {
               float j1 = blx ? 1.0F : -1.0F;
               matrices.translate((double)j1, 0.0 - (double)equipProgress * 0.3, 0.3);
               matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45.0F * j1));
               matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-40.0F * j1));
               matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(30.0F));
               this.altSwing(matrices, armx, swingProgress);
               matrices.scale(0.9F, 0.9F, 0.9F);
               acc.invokeRenderPlayerArm(matrices, vertexConsumers, light, 0.0F, 0.0F, armx);
            }

            this.previousRotation = smoothRotation;
         }

         if ((player.isClimbing() && !player.isOnGround() || player.isCrawling() && swingProgress == 0.0F) && !player.isUsingItem()) {
            double s = (double)((float)player.age + tickDelta) * 0.1;
            float hx = MathHelper.cos((float)s * 2.0F);
            float j = blx ? 1.0F : -1.0F;
            if (player.isClimbing()) {
               if (!stack.isOf(Items.LANTERN) && !stack.isOf(Items.SOUL_LANTERN)) {
                  matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(20.0F * hx * j));
               } else {
                  matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(1.0F * hx * j));
               }
            }

            if (player.isCrawling() && !player.isUsingItem() && swingProgress == 0.0F) {
               float timeValue = ((float)player.age + tickDelta) * 0.4F;
               float lx = MathHelper.sin(timeValue * this.mouseHolding);
               float dtx = MathHelper.cos(timeValue * this.mouseHolding);
               if (stack.isOf(Items.LANTERN) || stack.isOf(Items.SOUL_LANTERN)) {
                  lx *= 0.14F;
                  dtx *= 0.14F;
               }

               matrices.translate(0.2 * (double)lx, 0.3 * (double)lx * (double)j, -0.2 * (double)lx * (double)j * (double)al);
               matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(25.0F * lx));
               matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(MathHelper.clamp(20.0F * dtx * j, 0.0F, 20.0F)));
            }

            if (stack.isEmpty() && !blx && !player.isInvisible() && (!player.isOnGround() && player.isClimbing() || player.isCrawling())) {
               float lx = blx ? 1.0F : -1.0F;
               matrices.translate((double)lx, 0.0 - (double)equipProgress * 0.3, 0.3);
               matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45.0F * lx));
               matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-40.0F * lx));
               matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(30.0F));
               this.altSwing(matrices, armx, swingProgress);
               matrices.scale(0.9F, 0.9F, 0.9F);
               acc.invokeRenderPlayerArm(matrices, vertexConsumers, light, 0.0F, 0.0F, armx);
            }
         }

         if (stack.isEmpty()) {
            if (blx && !player.isInvisible()) {
               float ll = blx ? 1.0F : -1.0F;
               if ((player.isOnGround() || !player.isClimbing()) && !player.isSwimming() && !player.isCrawling()) {
                  if (player.getMainArm() == Arm.LEFT) {
                     blx = !blx;
                  }

                  matrices.translate(0.0, 0.2 * (double)swing_rot, 0.15 * (double)swing_rot);
                  matrices.translate(0.1 * (double)ll * (double)swing, 0.15 * (double)swing, -0.45 * (double)swing);
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(35.0F * swing * ll));
                  matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-30.0F * swing));
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-10.0F * swing_rot * ll));
                  matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(10.0F * swing_rot));
                  acc.invokeRenderPlayerArm(matrices, vertexConsumers, light, 0.0F, 0.0F, armx);
               } else {
                  matrices.translate((double)ll, 0.0 - (double)equipProgress * 0.3, 0.3);
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45.0F * ll));
                  matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-40.0F * ll));
                  matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(30.0F));
                  this.altSwing(matrices, armx, swingProgress);
                  matrices.scale(0.9F, 0.9F, 0.9F);
                  acc.invokeRenderPlayerArm(matrices, vertexConsumers, light, 0.0F, 0.0F, armx);
               }
            }
         } else if (stack.contains(DataComponentTypes.MAP_ID)) {
            if (blx && acc.getOffHand().isEmpty()) {
               matrices.translate(0.0, 0.1, 0.0);
               acc.invokeRenderTwoHandedMap(matrices, vertexConsumers, light, pitch, equipProgress, swingProgress);
            } else {
               matrices.translate(blx ? -0.1F : 0.1F, 0.1F, 0.0F);
               acc.invokeRenderOneHandedMap(matrices, vertexConsumers, light, equipProgress, armx, swingProgress, stack);
            }
         } else if (stack.getUseAction() == UseAction.CROSSBOW) {
            this.renderCrossbow(acc, player, tickDelta, pitch, hand, swingProgress, stack, equipProgress, matrices, vertexConsumers, light, blx, armx, kjx);
         } else {
            this.renderGeneralItem(
               acc, player, tickDelta, pitch, hand, swingProgress, stack, equipProgress, matrices, vertexConsumers, light, blx, armx, kjx, tt, swing, swing_rot
            );
         }

         matrices.pop();
         matrices.pop();
         this.isAttacking = MinecraftClient.getInstance().options.attackKey.isPressed();
      }
   }

   private void renderCrossbow(
      HeldItemRendererAccessor acc,
      AbstractClientPlayerEntity player,
      float tickDelta,
      float pitch,
      Hand hand,
      float swingProgress,
      ItemStack stack,
      float equipProgress,
      MatrixStack matrices,
      VertexConsumerProvider vertexConsumers,
      int light,
      boolean bl,
      Arm arm,
      float kj
   ) {
      matrices.push();
      boolean bl2 = arm == Arm.RIGHT;
      int i = bl2 ? 1 : -1;
      if (player.isUsingItem() && player.getItemUseTimeLeft() > 0 && player.getActiveHand() == hand) {
         acc.invokeApplyItemArmTransform(matrices, arm, equipProgress);
         matrices.translate((float)i * -0.4785682F, -0.24387F, 0.05731531F);
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-11.935F));
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)i * 65.3F));
         matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)i * 9.785F));
         float f = (float)stack.getMaxUseTime(player) - ((float)player.getItemUseTimeLeft() - tickDelta + 1.0F);
         float g = f / (float)CrossbowItem.getPullTime(stack, player);
         if (g > 1.0F) {
            g = 1.0F;
         }

         if (g > 0.1F) {
            float h = MathHelper.sin((f - 0.1F) * 1.3F);
            float yawDelta = h * (g - 0.1F);
            matrices.translate(yawDelta * 0.0F, yawDelta * 0.004F, yawDelta * 0.0F);
         }

         matrices.translate(g * 0.0F, g * 0.0F, g * 0.04F);
         matrices.scale(1.0F, 1.0F, 1.0F);
         matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float)i * 45.0F));
      } else {
         acc.invokeSwingArm(swingProgress, equipProgress, matrices, i, arm);
         if (CrossbowItem.isCharged(stack) && swingProgress < 0.001F && bl) {
            matrices.translate((float)i * -0.341864F, 0.0F, 0.0F);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)i * 10.0F));
         }
      }

      float yawDelta = bl ? 1.0F : -1.0F;
      matrices.translate(0.0F, 0.0F, -1.0F);
      matrices.translate(-0.45 * (double)i, 0.45, 1.7);
      matrices.translate((double)yawDelta, 0.0 - (double)equipProgress * 0.3, 0.3);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45.0F * yawDelta));
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-40.0F * yawDelta));
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(30.0F));
      this.altSwing(matrices, arm, swingProgress);
      matrices.scale(0.9F, 0.9F, 0.9F);
      acc.invokeRenderPlayerArm(matrices, vertexConsumers, light, 0.0F, 0.0F, arm);
      matrices.translate((double)(-0.25F * (float)i), 1.25, 0.05);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)(-90 * i)));
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(77.0F));
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)(85 * i)));
      matrices.scale(1.2F, 1.2F, 1.2F);
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-10.0F));
      matrices.translate(0.0, -0.15, 0.15);
      acc.invokeRenderItem(player, stack, bl2 ? ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : ModelTransformationMode.FIRST_PERSON_LEFT_HAND, !bl2, matrices, vertexConsumers, light);
      matrices.pop();
      if (player.isUsingItem() && player.getItemUseTimeLeft() > 0 && player.getActiveHand() == hand) {
         float fx = (float)stack.getMaxUseTime(player) - ((float)player.getItemUseTimeLeft() - tickDelta + 1.0F);
         float gx = fx / (float)CrossbowItem.getPullTime(stack, player);
         if (gx > 1.0F) {
            gx = 1.0F;
         }

         if (gx > 0.1F) {
            float h = MathHelper.sin((fx - 0.1F) * 1.3F);
            float k = h * (gx - 0.1F);
            matrices.translate(k * 0.0F, k * 0.004F, k * 0.0F);
         }

         matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((double)gx <= 0.2 ? 75.0F * gx * 5.0F * (float)i : (float)(75 * i)));
         matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(10.0F * gx * 1.5F));
         matrices.translate(-0.37 * (double)i, 0.0, 0.6);
         matrices.translate(0.15 * (double)gx * (double)i, 0.0, 0.0);
         acc.invokeRenderPlayerArm(matrices, vertexConsumers, light, equipProgress, swingProgress, arm.getOpposite());
      }
   }

   private void renderGeneralItem(
      HeldItemRendererAccessor acc,
      AbstractClientPlayerEntity player,
      float tickDelta,
      float pitch,
      Hand hand,
      float swingProgress,
      ItemStack stack,
      float equipProgress,
      MatrixStack matrices,
      VertexConsumerProvider vertexConsumers,
      int light,
      boolean bl,
      Arm arm,
      float kj,
      double tt,
      float swing,
      float swing_rot
   ) {
      boolean bl2 = arm == Arm.RIGHT;
      int l = bl2 ? 1 : -1;
      if (player.isUsingItem() && player.getItemUseTimeLeft() > 0 && player.getActiveHand() == hand) {
         this.renderUsingItem(
            acc, player, tickDelta, hand, swingProgress, stack, equipProgress, matrices, vertexConsumers, light, bl, arm, kj, l, tt, swing, swing_rot
         );
      } else if (player.isUsingRiptide() && stack.getUseAction() == UseAction.SPEAR) {
         this.riptideCounter = (float)((double)this.riptideCounter + 0.15 * tt);
         float dt = (float)stack.getMaxUseTime(player) - ((float)player.getItemUseTimeLeft() - tickDelta + 1.0F);
         float f = dt / 10.0F;
         if (f > 1.0F) {
            f = 1.0F;
         }

         if (f > 0.1F) {
            float g = MathHelper.sin((dt - 0.1F) * 1.3F);
            float j = g * (f - 0.1F);
            matrices.translate(j * 0.0F, j * 0.004F, j * 0.0F);
         }

         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(45.0F - this.riptideCounter * 2.0F));
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)(25 * l)));
         matrices.translate(0.2 * (double)l, 0.0, 0.75);
         matrices.translate(0.0, 0.0, 0.01 * (double)MathHelper.sin(this.riptideCounter * 6.28F));
         acc.invokeRenderPlayerArm(matrices, vertexConsumers, light, equipProgress, swingProgress, arm);
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(135.0F));
         matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)(-65 * l)));
         matrices.translate((double)(0.65F * (float)l), -1.0, -0.6);
      } else {
         this.riptideCounter = 0.0F;
         if (!stack.isOf(Items.LANTERN) && !stack.isOf(Items.SOUL_LANTERN) && !stack.isIn(ItemTags.HANGING_SIGNS)) {
            if (stack.getUseAction() == UseAction.BLOCK) {
               matrices.translate(0.0, -0.2, 0.0);
            }
         } else {
            matrices.translate(0.1 * (double)l, 0.0, -0.1);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(10.0F));
         }

         matrices.translate((double)l, 0.0 - (double)equipProgress * 0.3, 0.3);
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)(45 * l)));
         matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)(-40 * l)));
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(30.0F));
         this.altSwing(matrices, arm, swingProgress);
         matrices.scale(0.9F, 0.9F, 0.9F);
         acc.invokeRenderPlayerArm(matrices, vertexConsumers, light, 0.0F, 0.0F, arm);
      }

      if (player.isUsingItem() && player.getItemUseTimeLeft() > 0 && player.getActiveHand() == hand && stack.getUseAction() == UseAction.BOW) {
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(75.0F));
         matrices.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees((float)(-15 * l)));
         matrices.translate(0.8 * (double)l, (double)(0.0F - equipProgress * 0.3F), -0.1);
      } else {
         matrices.translate(-0.3 * (double)l, 0.65, -0.1);
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)(-65 * l)));
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(10.0F));
      }

      if (stack.getItem() instanceof BlockItem && !stack.isIn(ItemTags.TRIMMABLE_ARMOR) && !stack.isIn(ItemTags.BOOKSHELF_BOOKS)) {
         this.renderBlockItem(acc, player, stack, matrices, vertexConsumers, light, bl, bl2, l, arm, tt, swing, swing_rot, equipProgress, swingProgress);
      } else {
         boolean isToolOrWeapon = this.isTool(stack)
               && !stack.isIn(ItemTags.TRIMMABLE_ARMOR)
               && !stack.isIn(ItemTags.BOOKSHELF_BOOKS)
               && stack.getUseAction() != UseAction.EAT
               && stack.isEnchantable()
            || stack.getUseAction() == UseAction.BOW
            || stack.getUseAction() == UseAction.SPYGLASS
            || this.getAttackDamage(stack) != 0.0F
            || stack.getUseAction() == UseAction.BLOCK
            || stack.isOf(Items.WARPED_FUNGUS_ON_A_STICK)
            || stack.isOf(Items.CARROT_ON_A_STICK)
            || stack.getItem() instanceof FishingRodItem
            || stack.isOf(Items.SHEARS);
         if (isToolOrWeapon) {
            if (this.isSword(stack)) {
               matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float)(75 * l)));
               matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(70.0F));
               matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)(45 * l)));
               matrices.scale(1.2F, 1.2F, 1.2F);
               matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-60.0F * swing));
               matrices.translate(0.0, 0.1 * (double)swing, -0.1 * (double)swing);
            } else if (stack.getUseAction() == UseAction.SPEAR) {
               matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float)(75 * l)));
               matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
               matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)(45 * l)));
               matrices.translate(-0.3F * (float)l, 0.0F, 0.0F);
               matrices.scale(1.2F, 1.2F, 1.2F);
               matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-40.0F * swing_rot));
               matrices.translate(0.0, 0.1 * (double)swing_rot, -0.1 * (double)swing_rot);
            } else if (stack.getUseAction() == UseAction.BOW) {
               matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)(160 * l)));
               matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)(-60 * l)));
               matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-70.0F));
               matrices.scale(0.75F, 0.75F, 0.75F);
               matrices.translate(0.15 * (double)l, bl ? 0.35F : 0.45F, bl ? -0.15F : -0.1F);
               matrices.translate(0.17 * (double)l, 0.0, 0.3);
               matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)(-90 * l)));
            } else if (stack.getUseAction() == UseAction.BLOCK) {
               matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float)(-154 * l)));
               matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(5.0F));
               matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)(90 * l)));
               matrices.translate((double)(-0.012F * (float)l), 0.38, -0.175);
               matrices.scale(0.55F, 0.55F, 0.55F);
            } else if (stack.isIn(ItemTags.SHOVELS)) {
               matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float)(75 * l)));
               matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(70.0F));
               matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)(45 * l)));
               matrices.scale(1.2F, 1.2F, 1.2F);
               matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-80.0F * swing_rot));
               matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(30.0F * swing));
               matrices.translate(0.07 * (double)l, 0.0, 0.05);
               matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)(90 * l)));
               matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-15.0F));
            } else {
               matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float)(75 * l)));
               matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(70.0F));
               matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)(45 * l)));
               matrices.scale(1.2F, 1.2F, 1.2F);
               matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-25.0F * swing));
               matrices.translate(0.0, 0.05 * (double)swing, -0.05 * (double)swing);
            }
         } else {
            matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float)(5 * l)));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-13.0F));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)(75 * l)));
            matrices.translate(-0.015 * (double)l, -0.055, -0.06);
            matrices.scale(0.7F, 0.7F, 0.7F);
         }

         if (!stack.isOf(Items.NETHER_STAR)) {
            if (stack.isOf(Items.END_CRYSTAL)) {
            }

            this.netherCounter = 0.0F;
         } else {
            this.netherCounter = (float)((double)this.netherCounter + 0.9 * tt);
            matrices.translate(0.0, 0.25 + 0.02 * (double)MathHelper.sin(this.netherCounter * 0.1F), 0.0);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(3.0F * MathHelper.sin(this.netherCounter * 0.2F)));
            float ns = 1.0F + 0.01F * MathHelper.sin(this.netherCounter);
            matrices.scale(ns, ns, ns);
         }

         acc.invokeRenderItem(
            player, stack, bl2 ? ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : ModelTransformationMode.FIRST_PERSON_LEFT_HAND, !bl2, matrices, vertexConsumers, light
         );
      }
   }

   private void renderUsingItem(
      HeldItemRendererAccessor acc,
      AbstractClientPlayerEntity player,
      float tickDelta,
      Hand hand,
      float swingProgress,
      ItemStack stack,
      float equipProgress,
      MatrixStack matrices,
      VertexConsumerProvider vertexConsumers,
      int light,
      boolean bl,
      Arm arm,
      float kj,
      int l,
      double tt,
      float swing,
      float swing_rot
   ) {
      UseAction useAction = stack.getUseAction();
      if (useAction == UseAction.NONE) {
         acc.invokeApplyItemArmTransform(matrices, arm, equipProgress);
      } else if (useAction == UseAction.EAT || useAction == UseAction.DRINK) {
         float yawDelta = (float)stack.getMaxUseTime(player) - ((float)player.getItemUseTimeLeft() - tickDelta + 1.0F);
         float pitchDelta = yawDelta / 5.0F;
         if (pitchDelta > 1.0F) {
            pitchDelta = 1.0F;
         }

         float k = MathHelper.sin(yawDelta / 2.0F * 3.14F) / 10.0F;
         matrices.translate((double)l, 0.1, 0.3);
         matrices.translate(0.2 * (double)l * (double)pitchDelta, -0.7 * (double)pitchDelta, -0.2 * (double)pitchDelta);
         matrices.translate(0.0, -0.2 * (double)k, -0.2 * (double)k);
         matrices.translate(0.0F, 0.1F * this.easeInOutBack(MathHelper.sin(pitchDelta * 3.14F)), 0.0F);
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)(45 * l)));
         matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)(-40 * l)));
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(30.0F));
         this.altSwing(matrices, arm, swingProgress);
         matrices.scale(0.9F, 0.9F, 0.9F);
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45.0F * pitchDelta * (float)l));
         acc.invokeRenderPlayerArm(matrices, vertexConsumers, light, 0.0F, swingProgress, arm);
      } else if (useAction == UseAction.BLOCK) {
         float k = (float)stack.getMaxUseTime(player) - ((float)player.getItemUseTimeLeft() - tickDelta + 1.0F);
         double s = (double)(k / 4.0F);
         float s2 = k / 6.0F;
         if (s > 1.0) {
            s = 1.0;
         }

         if (s2 > 1.0F) {
            s2 = 1.0F;
         }

         matrices.translate(0.0, -0.2, 0.0);
         matrices.translate((double)l, 0.0, 0.3);
         matrices.translate(0.7 * s * (double)l, 0.0, -1.3 * s);
         matrices.translate(-0.2 * (double)l * (double)s2, 0.0, 0.0);
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((float)(15.0 * Math.sin((double)s2 * 3.14))));
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)(70.0 * s * (double)l)));
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)(45 * l)));
         matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)(-40 * l)));
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(30.0F));
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)(5 * l) * (float)s));
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((float)(-10.0 * s)));
         matrices.translate(0.0, 0.0, -0.2 * s);
         this.altSwing(matrices, arm, swingProgress);
         matrices.scale(0.9F, 0.9F, 0.9F);
         acc.invokeRenderPlayerArm(matrices, vertexConsumers, light, 0.0F, swingProgress, arm);
         if (l == 1) {
            matrices.translate(0.19F, 0.1, -0.24);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(0.0F));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(4.0F));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-10.4F));
            matrices.translate(-0.2, -0.04, 0.15);
         } else {
            matrices.translate(-0.19F, 0.1, -0.18);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(0.0F));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-4.0F));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-10.4F));
            matrices.translate(0.2, -0.04, 0.15);
         }
      } else if (useAction == UseAction.BOW) {
         matrices.push();
         float m1 = (float)stack.getMaxUseTime(player) - ((float)player.getItemUseTimeLeft() - tickDelta + 1.0F);
         float f1 = m1 / 20.0F;
         if (f1 > 1.0F) {
            f1 = 1.0F;
         }

         if (f1 > 0.1F) {
            float g1 = MathHelper.sin((m1 - 0.1F) * 1.3F);
            float j1 = g1 * f1;
            matrices.translate(j1 * 0.0F, j1 * 0.004F, j1 * 0.0F);
         }

         matrices.push();
         matrices.translate(bl ? -0.1F : 0.1F, 0.0F, f1 * 0.15F);
         acc.invokeRenderPlayerArm(matrices, vertexConsumers, light, equipProgress, swingProgress, arm);
         matrices.pop();
         matrices.translate(bl ? -0.5 : 0.5, -0.45, 0.1);
         matrices.multiply(RotationAxis.POSITIVE_X.rotation(0.3F));
         if (bl) {
            matrices.multiply(RotationAxis.NEGATIVE_Z.rotation(-0.3F));
            matrices.multiply(RotationAxis.NEGATIVE_Y.rotation(1.0F));
         } else {
            matrices.multiply(RotationAxis.POSITIVE_Z.rotation(-0.3F));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation(1.0F));
         }

         acc.invokeRenderPlayerArm(matrices, vertexConsumers, light, equipProgress, swingProgress, arm.getOpposite());
         if (bl) {
            matrices.multiply(RotationAxis.NEGATIVE_Y.rotation(2.5F));
         } else {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation(2.5F));
         }

         matrices.translate(bl ? -0.65 : 0.65, -0.35, 0.27);
         matrices.pop();
      } else if (useAction == UseAction.SPEAR) {
         if (player.getOffHandStack().isEmpty() && !player.isCrawling() && !player.isSwimming() && !player.isClimbing()) {
            matrices.push();
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)(-25 * l)));
            matrices.translate(-0.15 * (double)l, 0.1, 0.1);
            acc.invokeRenderPlayerArm(matrices, vertexConsumers, light, equipProgress, swingProgress, arm.getOpposite());
            matrices.pop();
         }

         float dt = (float)stack.getMaxUseTime(player) - ((float)player.getItemUseTimeLeft() - tickDelta + 1.0F);
         float f = dt / 10.0F;
         if (f > 1.0F) {
            f = 1.0F;
         }

         if (f > 0.1F) {
            float g = MathHelper.sin((dt - 0.1F) * 1.3F);
            float j = g * (f - 0.1F);
            matrices.translate(j * 0.0F, j * 0.004F, j * 0.0F);
         }

         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(45.0F));
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)(25 * l)));
         matrices.translate(0.2 * (double)l, 0.0, 0.8);
         acc.invokeRenderPlayerArm(matrices, vertexConsumers, light, equipProgress, swingProgress, arm);
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(100.0F));
         matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)(-55 * l)));
         matrices.translate((double)(0.5F * (float)l), -0.7, -0.35);
      } else if (useAction == UseAction.SPYGLASS) {
         float g1 = (float)(player.getItemUseTimeLeft() % 10);
         float h1 = g1 - tickDelta + 1.0F;
         float j1 = 1.0F - h1 / 10.0F;
         float n = -15.0F + 75.0F * MathHelper.cos(j1 * 2.0F * (float) Math.PI);
         float z = (float)stack.getMaxUseTime(player) - ((float)player.getItemUseTimeLeft() - tickDelta + 1.0F);
         float x = z / 4.0F;
         if (x > 1.0F) {
            x = 1.0F;
         }

         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)(25 * l) * x));
         matrices.translate((double)(0.3F * (float)l * x), 0.3 * (double)x, 0.1 * (double)x);
         if (x == 1.0F) {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(n / 20.0F));
         }

         acc.invokeRenderPlayerArm(matrices, vertexConsumers, light, equipProgress, swingProgress, arm);
      }
   }

   private void renderBlockItem(
      HeldItemRendererAccessor acc,
      AbstractClientPlayerEntity player,
      ItemStack stack,
      MatrixStack matrices,
      VertexConsumerProvider vertexConsumers,
      int light,
      boolean bl,
      boolean bl2,
      int l,
      Arm arm,
      double tt,
      float swing,
      float swing_rot,
      float equipProgress,
      float swingProgress
   ) {
      Block block = Block.getBlockFromItem(stack.getItem());
      if (block != Blocks.AIR) {
         BlockState state = block.getDefaultState();
         if (this.isSmallBlockItem(stack)) {
            matrices.translate(0.0, 0.0, -0.1);
            matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float)(5 * l)));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(15.0F));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)(75 * l)));
         } else if (stack.getName().getString().toLowerCase().contains("torch")) {
            matrices.scale(1.5F, 1.5F, 1.5F);
            matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float)(25 * l)));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(5.0F));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)(75 * l)));
            matrices.translate(0.2 * (double)l, 0.2, 0.05);
         } else if (!stack.isOf(Items.LANTERN) && !stack.isOf(Items.SOUL_LANTERN) && !stack.isIn(ItemTags.HANGING_SIGNS)) {
            matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float)(25 * l)));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(5.0F));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)(75 * l)));
            matrices.translate(0.2 * (double)l, 0.2, 0.05);
         } else {
            float dt = (float)(deltaTime * 8.0);
            float yawDelta = player.prevYaw - player.getYaw();
            float pitchDelta = player.prevPitch - player.getPitch();
            this.swingVelocityY += yawDelta * 0.015F * dt;
            this.swingVelocityY += swingProgress * 2.0F * dt;
            this.swingVelocityX += pitchDelta * 0.015F * dt;
            this.swingVelocityY = this.swingVelocityY - 0.1F * this.swingAngleY * dt;
            this.swingVelocityX = this.swingVelocityX - 0.1F * this.swingAngleX * dt;
            this.swingVelocityY = (float)((double)this.swingVelocityY * Math.pow(0.88F, (double)dt));
            this.swingVelocityX = (float)((double)this.swingVelocityX * Math.pow(0.88F, (double)dt));
            this.swingAngleY = this.swingAngleY + this.swingVelocityY * dt;
            this.swingAngleX = this.swingAngleX + this.swingVelocityX * dt;
            double currentSpeed = player.getVelocity().length();
            this.swingVelocityZ = (float)(
               (double)this.swingVelocityZ
                  + (bl ? currentSpeed * -15.0 - (double)this.swingVelocityZ : currentSpeed * 15.0 - (double)this.swingVelocityZ) * 0.1F * (double)dt
            );
            if ((currentSpeed > 0.09 && player.isOnGround() || player.isSwimming() || player.isClimbing() && !player.isOnGround())
               && (Boolean)MinecraftClient.getInstance().options.getBobView().getValue()) {
               boolean randomBool = Math.random() > 0.5;
               this.swingVelocityY = (float)((double)this.swingVelocityY + (randomBool ? -5.5 * currentSpeed * (double)dt : 5.5 * currentSpeed * (double)dt));
            }

            matrices.translate(0.0, 0.0, -0.1);
            matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float)(35 * l) + this.swingAngleY));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(15.0F + this.swingAngleX));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)(75 * l) + this.swingVelocityZ));
            if (stack.isIn(ItemTags.HANGING_SIGNS)) {
               matrices.translate(0.0, -0.1, 0.0);
               matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)(-45 * l)));
            }

            matrices.translate(0.3 * (double)l, -0.35, 0.0);
            matrices.translate(0.0, 0.0, 0.1);
            matrices.scale(1.5F, 1.5F, 1.5F);
         }

         BlockRenderManager blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();
         matrices.push();
         if (!bl2) {
            matrices.translate(-0.4F, 0.0F, 0.0F);
         }

         matrices.scale(0.35F, 0.35F, 0.35F);
         matrices.translate(-0.9 * (double)l, -0.45, -0.5);
         if (state.isIn(BlockTags.BUTTONS)) {
            matrices.translate(0.2 * (double)l, -0.15, -0.2);
         }

         if (state.isIn(BlockTags.PRESSURE_PLATES)) {
            matrices.translate(0.0, 0.1, 0.0);
         }

         if (stack.isOf(Items.SLIME_BLOCK)
            || stack.isOf(Items.HONEY_BLOCK)
            || state.isIn(BlockTags.FLOWERS)
            || state.isIn(BlockTags.LEAVES)
            || state.isIn(BlockTags.SAPLINGS)
            || state.isIn(BlockTags.SWORD_EFFICIENT)) {
            this.vertVelocityYSlime = (float)((double)this.vertVelocityYSlime + (double)swingProgress * 0.03 * deltaTime * 8.0);
            if ((
                  player.getVelocity().length() > 0.09 && player.isOnGround()
                     || player.isSwimming()
                     || player.isCrawling()
                     || player.isClimbing() && !player.isOnGround()
               )
               && (Boolean)MinecraftClient.getInstance().options.getBobView().getValue()) {
               this.vertVelocityYSlime = (float)((double)this.vertVelocityYSlime + -0.05F * player.getVelocity().length() * deltaTime * 8.0);
            }

            matrices.scale(1.0F, 1.0F + this.vertAngleYSlime * -2.0F, 1.0F);
         }

         if ((float)player.age - this.prevAge >= 100.0F) {
            this.repPower = !this.repPower;
            this.prevAge = (float)player.age;
         }

         if (stack.isIn(ItemTags.BEDS) && bl) {
            matrices.translate(0.9, 0.0, 0.8);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)(90 * l)));
         }

         blockRenderManager.renderBlockAsEntity(state, matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV);
         matrices.pop();
      }
   }
}
