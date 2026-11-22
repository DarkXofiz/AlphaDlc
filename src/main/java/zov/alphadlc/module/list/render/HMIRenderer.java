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
      float pitch = player.method_36455();
      if (!player.method_5681() && !player.method_20448() && !player.method_6101()) {
         boolean nearGround = player.method_24828();
         if (!nearGround) {
            double feetY = player.method_23318();

            for (int i = 0; i <= 2; i++) {
               BlockPos checkPos = BlockPos.method_49637(player.method_23317(), feetY - (double)i, player.method_23321());
               if (player.method_37908().method_8320(checkPos).method_51367()) {
                  double dist = feetY - (double)checkPos.method_10264() - 1.0;
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
            for (Entity entity : player.method_37908().method_8335(player, player.method_5829().method_1014(6.0))) {
               double yDiff = entity.method_23318() - player.method_23318();
               if (yDiff < -1.9 || yDiff > 2.5) {
                  return pitch;
               }
            }

            return MathHelper.method_15363(pitch, 35.0F, 48.0F);
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
      if (MinecraftClient.method_1551().method_1493()) {
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
      AttributeModifiersComponent modifiers = (AttributeModifiersComponent)stack.method_57824(DataComponentTypes.field_49636);
      if (modifiers == null) {
         return totalDamage;
      } else {
         for (Entry entry : modifiers.comp_2393()) {
            if (entry.comp_2395().equals(EntityAttributes.field_23721)) {
               totalDamage += (float)entry.comp_2396().comp_2449();
            }
         }

         return totalDamage;
      }
   }

   private void altSwing(MatrixStack matrices, Arm arm, float swingProgress) {
      int direction = arm == Arm.field_6183 ? 1 : -1;
      float swingSin = MathHelper.method_15374(swingProgress * (float) Math.PI);
      matrices.method_22907(RotationAxis.field_40716.rotationDegrees((float)direction * (45.0F + swingSin * 0.0F)));
      matrices.method_22907(RotationAxis.field_40716.rotationDegrees((float)direction * -45.0F));
   }

   private boolean isSword(ItemStack stack) {
      return stack.method_31573(ItemTags.field_42611) ? true : stack.method_7909() instanceof SwordItem;
   }

   private boolean isTool(ItemStack stack) {
      return stack.method_31573(ItemTags.field_42612)
         || stack.method_31573(ItemTags.field_42613)
         || stack.method_31573(ItemTags.field_42614)
         || stack.method_31573(ItemTags.field_42615)
         || this.isSword(stack);
   }

   private boolean isSmallBlockItem(ItemStack stack) {
      Block block = Block.method_9503(stack.method_7909());
      BlockState state = block.method_9564();
      return stack.method_31574(Items.field_8276)
         || stack.method_31574(Items.field_8725)
         || stack.method_31574(Items.field_8865)
         || stack.method_31574(Items.field_8366)
         || state.method_26164(BlockTags.field_15490)
         || state.method_26164(BlockTags.field_15463)
         || state.method_26164(BlockTags.field_22414)
         || stack.method_31573(ItemTags.field_15553);
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
      boolean useVanillaLike = stack.method_7976() == UseAction.field_8953 || stack.method_7976() == UseAction.field_8951;
      if (useVanillaLike) {
         boolean bl = hand == Hand.field_5808;
         Arm arm = bl ? player.method_6068() : player.method_6068().method_5928();
         float kj = bl ? 1.0F : -1.0F;
         int l = arm == Arm.field_6183 ? 1 : -1;
         matrices.method_22903();
         if (this.swingAnimations != null) {
            double vx = this.swingAnimations.hmiPosX.getValue();
            double vy = this.swingAnimations.hmiPosY.getValue();
            double vz = this.swingAnimations.hmiPosZ.getValue();
            matrices.method_22904(vx * (double)kj, vy, vz);
         }

         if (player.method_6115() && player.method_6014() > 0 && player.method_6058() == hand) {
            UseAction useAction = stack.method_7976();
            if (useAction == UseAction.field_8953) {
               matrices.method_22903();
               float m1 = (float)stack.method_7935(player) - ((float)player.method_6014() - tickDelta + 1.0F);
               float f1 = m1 / 20.0F;
               if (f1 > 1.0F) {
                  f1 = 1.0F;
               }

               if (f1 > 0.1F) {
                  float g1 = MathHelper.method_15374((m1 - 0.1F) * 1.3F);
                  float j1 = g1 * f1;
                  matrices.method_46416(j1 * 0.0F, j1 * 0.004F, j1 * 0.0F);
               }

               acc.invokeApplyItemArmTransform(matrices, arm, equipProgress);
               matrices.method_46416((float)l * -0.2785682F, 0.18312F, 0.15731531F);
               matrices.method_22907(RotationAxis.field_40714.rotationDegrees(-13.935F));
               matrices.method_22907(RotationAxis.field_40716.rotationDegrees((float)l * 35.3F));
               matrices.method_22907(RotationAxis.field_40718.rotationDegrees((float)l * -9.785F));
               float h = (float)stack.method_7935(player) - ((float)player.method_6014() - tickDelta + 1.0F);
               float g = h / 20.0F;
               g = (g * g + g * 2.0F) / 3.0F;
               if (g > 1.0F) {
                  g = 1.0F;
               }

               if (g > 0.1F) {
                  float i = MathHelper.method_15374((h - 0.1F) * 1.3F);
                  float j = g - 0.1F;
                  float k = i * j;
                  matrices.method_46416(k * 0.0F, k * 0.004F, k * 0.0F);
               }

               matrices.method_46416(g * 0.0F, g * 0.0F, g * 0.04F);
               matrices.method_22905(1.0F, 1.0F, 1.0F + g * 0.2F);
               matrices.method_22907(RotationAxis.field_40715.rotationDegrees((float)l * 45.0F));
               acc.invokeRenderItem(
                  player,
                  stack,
                  arm == Arm.field_6183 ? ModelTransformationMode.field_4322 : ModelTransformationMode.field_4321,
                  arm != Arm.field_6183,
                  matrices,
                  vertexConsumers,
                  light
               );
               matrices.method_22909();
            } else if (useAction == UseAction.field_8951) {
               acc.invokeApplyItemArmTransform(matrices, arm, equipProgress);
               float dt = (float)stack.method_7935(player) - ((float)player.method_6014() - tickDelta + 1.0F);
               float f = dt / 10.0F;
               if (f > 1.0F) {
                  f = 1.0F;
               }

               if (f > 0.1F) {
                  float gx = MathHelper.method_15374((dt - 0.1F) * 1.3F);
                  float j = gx * (f - 0.1F);
                  matrices.method_46416(j * 0.0F, j * 0.004F, j * 0.0F);
               }

               matrices.method_46416(0.0F, 0.0F, 0.1F);
               matrices.method_22907(RotationAxis.field_40714.rotationDegrees(-10.0F));
               matrices.method_46416(0.0F, 0.0F, -0.1F);
               acc.invokeRenderItem(
                  player,
                  stack,
                  arm == Arm.field_6183 ? ModelTransformationMode.field_4322 : ModelTransformationMode.field_4321,
                  arm != Arm.field_6183,
                  matrices,
                  vertexConsumers,
                  light
               );
            }
         } else {
            acc.invokeSwingArm(swingProgress, equipProgress, matrices, bl ? 1 : -1, arm);
            acc.invokeRenderItem(
               player, stack, bl ? ModelTransformationMode.field_4322 : ModelTransformationMode.field_4321, !bl, matrices, vertexConsumers, light
            );
         }

         matrices.method_22909();
      } else {
         float yaw = player.method_36454();
         double radians = Math.toRadians((double)yaw);
         double forwardX = -Math.sin(radians);
         double forwardZ = Math.cos(radians);
         Vec3d vel = player.method_18798();
         double dotProduct = vel.field_1352 * forwardX + vel.field_1350 * forwardZ;
         double crossProduct = vel.field_1352 * forwardZ - vel.field_1350 * forwardX;
         float effectivePitch = this.getEffectivePitch(player);
         float al = effectivePitch != 0.0F ? 90.0F / effectivePitch / 10.0F : 1.0F;
         if (al > 1.0F) {
            al = 1.0F;
         }

         if (al < 0.0F) {
            al = 1.0F;
         }

         boolean blx = hand == Hand.field_5808;
         Arm armx = blx ? player.method_6068() : player.method_6068().method_5928();
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

         matrices.method_22903();
         matrices.method_22903();
         matrices.method_22904(vx * (double)kjx, vy, vz);
         if (rx != 0.0) {
            matrices.method_22907(RotationAxis.field_40714.rotationDegrees((float)rx));
         }

         if (ry != 0.0) {
            matrices.method_22907(RotationAxis.field_40716.rotationDegrees((float)ry * kjx));
         }

         if (rz != 0.0) {
            matrices.method_22907(RotationAxis.field_40718.rotationDegrees((float)rz * kjx));
         }

         if (scale != 1.0) {
            matrices.method_22905((float)scale, (float)scale, (float)scale);
         }

         double tt = deltaTime * animSpeed;
         float speedMultiplier = 1.0F;
         if (this.swingAnimations != null) {
            speedMultiplier = (float)(0.5 + this.swingAnimations.speed.getValue() / 10.0 * 2.0);
         }

         if (Block.method_9503(stack.method_7909()) != Blocks.field_10124
            && (
               !this.isTool(stack)
                  || stack.method_31573(ItemTags.field_41890)
                  || stack.method_31573(ItemTags.field_40109)
                  || stack.method_7976() == UseAction.field_8950
                  || !stack.method_7923()
            )
            && stack.method_7976() != UseAction.field_8953
            && stack.method_7976() != UseAction.field_27079
            && this.getAttackDamage(stack) == 0.0F
            && stack.method_7976() != UseAction.field_8949
            && !stack.method_31574(Items.field_23254)
            && !stack.method_31574(Items.field_8184)
            && !(stack.method_7909() instanceof FishingRodItem)
            && !stack.method_31574(Items.field_8868)) {
            swingProgress = (float)((double)swingProgress * 0.45 * (double)speedMultiplier);
            if (swingProgress > 1.0F) {
               swingProgress = 0.0F;
            }
         } else if (!stack.method_31573(ItemTags.field_42615)) {
            swingProgress = (float)((double)swingProgress * 0.45 * (double)speedMultiplier);
            if (swingProgress > 1.0F) {
               swingProgress = 0.0F;
            }
         }

         float raw_swing_rot = (double)swingProgress < 0.6
            ? MathHelper.method_15374(MathHelper.method_15363(swingProgress, 0.0F, 0.12506F) * 12.56F)
            : MathHelper.method_15374(MathHelper.method_15363(swingProgress, 0.62532F, 0.75038F) * 12.56F);
         float raw_swing = MathHelper.method_15374(swingProgress * 3.14F);
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
               stack.method_31574(Items.field_8287)
                  || stack.method_31574(Items.field_8803)
                  || stack.method_31574(Items.field_8449)
                  || stack.method_31574(Items.field_8543)
                  || stack.method_31574(Items.field_8634)
                  || stack.method_7909() instanceof SplashPotionItem
                  || stack.method_7909() instanceof LingeringPotionItem
            )
            && player.method_6079().method_7960()
            && stack.method_7976() != UseAction.field_8951
            && !stack.method_31574(Items.field_8814)
            && !player.method_5681()
            && !player.method_20448()
            && !player.method_6101()) {
            if (player.method_6068() == Arm.field_6182) {
               blx = !blx;
            }

            float ll = blx ? 1.0F : -1.0F;
            matrices.method_22903();
            matrices.method_22907(RotationAxis.field_40716.rotationDegrees(-25.0F * ll));
            matrices.method_22907(RotationAxis.field_40714.rotationDegrees(-10.0F));
            matrices.method_22907(RotationAxis.field_40716.rotationDegrees(25.0F * ll * swing));
            matrices.method_22907(RotationAxis.field_40714.rotationDegrees(30.0F * swing));
            matrices.method_22904(-0.15 * (double)ll, 0.1, 0.1);
            matrices.method_22904(0.0, -0.55 * (double)swing, 0.4 * (double)swing * 3.14F);
            acc.invokeRenderPlayerArm(matrices, vertexConsumers, light, equipProgress, 0.0F, armx.method_5928());
            matrices.method_22909();
         }

         if (this.wasSwinging[handIdx] && this.swingReturnProgress[handIdx] <= 0.0F) {
            this.left = !this.left;
         }

         if (!stack.method_7960()) {
            if (player.method_6068() == Arm.field_6182) {
               blx = !blx;
            }

            float ll = blx ? 1.0F : -1.0F;
            if ((
                  this.left
                     || stack.method_31573(ItemTags.field_42612)
                     || stack.method_7976() == UseAction.field_8951
                     || stack.method_7976() == UseAction.field_8949
               )
               && !stack.method_31573(ItemTags.field_42615)) {
               if (!this.isSword(stack) && !stack.method_31573(ItemTags.field_42612)) {
                  if (stack.method_7976() == UseAction.field_8951) {
                     matrices.method_22904(0.0, 0.0, 0.45 * (double)swing_rot);
                     matrices.method_22904((double)(-0.25F * kjx * swing), -0.35 * (double)swing_rot, -0.6 * (double)swing);
                     matrices.method_22904(0.0, 0.1 * (double)swing, 0.0);
                     matrices.method_22907(RotationAxis.field_40716.rotationDegrees(15.0F * swing_rot * ll));
                     matrices.method_22907(RotationAxis.field_40718.rotationDegrees(30.0F * swing_rot * ll));
                  } else if (this.isTool(stack) && stack.method_7976() != UseAction.field_8949 && !stack.method_31573(ItemTags.field_42615)) {
                     matrices.method_22904(0.1 * (double)ll * (double)swing_rot, 0.1 * (double)swing_rot, (double)(-0.5F * swing));
                     matrices.method_22907(RotationAxis.field_40713.rotationDegrees(-30.0F * swing_rot));
                     matrices.method_22907(RotationAxis.field_40718.rotationDegrees(-20.0F * swing_rot * ll));
                     matrices.method_22907(RotationAxis.field_40713.rotationDegrees(40.0F * swing));
                  } else if (stack.method_7976() != UseAction.field_8949) {
                     matrices.method_22904(0.1 * (double)ll * (double)swing_rot, 0.1 * (double)swing_rot, -0.1 * (double)swing);
                     matrices.method_22907(RotationAxis.field_40713.rotationDegrees(-30.0F * swing_rot));
                     matrices.method_22907(RotationAxis.field_40718.rotationDegrees(-10.0F * swing_rot * ll));
                     matrices.method_22907(RotationAxis.field_40713.rotationDegrees(40.0F * swing));
                     matrices.method_22907(RotationAxis.field_40716.rotationDegrees(10.0F * swing * ll));
                  } else {
                     matrices.method_22904(0.1 * (double)ll * (double)swing_rot, 0.1 * (double)swing_rot, -0.2 * (double)swing);
                     matrices.method_22907(RotationAxis.field_40713.rotationDegrees(-10.0F * swing_rot));
                     matrices.method_22907(RotationAxis.field_40718.rotationDegrees(-10.0F * swing_rot * ll));
                     matrices.method_22907(RotationAxis.field_40713.rotationDegrees(20.0F * swing));
                  }
               } else {
                  matrices.method_22904(0.8 * (double)ll * (double)swing_rot, 0.3 * (double)swing_rot, (double)(-0.5F * swing));
                  matrices.method_22907(RotationAxis.field_40716.rotationDegrees(15.0F * swing_rot * ll));
                  matrices.method_22907(RotationAxis.field_40713.rotationDegrees(-20.0F * swing_rot));
                  matrices.method_22907(RotationAxis.field_40718.rotationDegrees(-70.0F * swing_rot * ll));
                  if (this.isSword(stack)) {
                     matrices.method_22907(RotationAxis.field_40713.rotationDegrees(40.0F * swing));
                  } else {
                     matrices.method_22907(RotationAxis.field_40713.rotationDegrees(30.0F * swing));
                  }
               }
            } else if (!stack.method_31573(ItemTags.field_42615)) {
               if (this.isSword(stack)) {
                  matrices.method_22904(-0.55 * (double)ll * (double)swing_rot, -0.8 * (double)swing_rot, -0.77 * (double)swing);
                  matrices.method_22907(RotationAxis.field_40716.rotationDegrees(5.0F * swing_rot * ll));
                  matrices.method_22907(RotationAxis.field_40713.rotationDegrees(-30.0F * swing_rot));
                  matrices.method_22907(RotationAxis.field_40718.rotationDegrees(70.0F * swing_rot * ll));
                  matrices.method_22907(RotationAxis.field_40713.rotationDegrees(50.0F * swing));
               } else if (this.isTool(stack) && !stack.method_31573(ItemTags.field_42615)) {
                  matrices.method_22904(0.1 * (double)ll * (double)swing_rot, 0.1 * (double)swing_rot, (double)(-0.5F * swing));
                  matrices.method_22907(RotationAxis.field_40713.rotationDegrees(-30.0F * swing_rot));
                  matrices.method_22907(RotationAxis.field_40718.rotationDegrees(-20.0F * swing_rot * ll));
                  matrices.method_22907(RotationAxis.field_40713.rotationDegrees(40.0F * swing));
               } else {
                  matrices.method_22904(0.1 * (double)ll * (double)swing_rot, 0.1 * (double)swing_rot, -0.1 * (double)swing);
                  matrices.method_22907(RotationAxis.field_40713.rotationDegrees(-30.0F * swing_rot));
                  matrices.method_22907(RotationAxis.field_40718.rotationDegrees(-10.0F * swing_rot * ll));
                  matrices.method_22907(RotationAxis.field_40713.rotationDegrees(40.0F * swing));
                  matrices.method_22907(RotationAxis.field_40716.rotationDegrees(10.0F * swing * ll));
               }
            } else if (stack.method_31573(ItemTags.field_42615)) {
               matrices.method_22904(0.0, 0.15 * (double)swing_rot, (double)(-0.25F * swing_rot));
               matrices.method_22904(0.0, 0.0, -0.2 * (double)swing);
               matrices.method_22907(RotationAxis.field_40716.rotationDegrees(15.0F * swing_rot));
               matrices.method_22907(RotationAxis.field_40714.rotationDegrees(-35.0F * swing_rot));
               matrices.method_22907(RotationAxis.field_40714.rotationDegrees(30.0F * swing));
            }
         }

         if (vel.method_1033() >= 0.08) {
            this.crawlCount = (float)((double)this.crawlCount + 0.1 * vel.method_1033() * 2.0 * tt);
            this.directionalCrawlCount = (float)((double)this.directionalCrawlCount + 0.1 * dotProduct * 4.0 * tt);
            this.directionalCrawlCount = (float)(
               (double)this.directionalCrawlCount
                  + (dotProduct > 0.0 ? 0.1 * Math.abs(crossProduct) * 4.0 * tt : 0.1 * Math.abs(crossProduct) * -1.0 * 4.0 * tt)
            );
         }

         if (vel.field_1351 > 0.0) {
            this.climbCount = (float)((double)this.climbCount + 0.1 * tt);
         }

         if (vel.field_1351 < 0.0) {
            this.climbCount = (float)((double)this.climbCount - 0.1 * tt);
         }

         if ((player.method_20448() || player.method_6101() && !player.method_24828() && Math.abs(vel.field_1351) > 0.0)
            && !player.method_6115()
            && swingProgress == 0.0F) {
            this.clCount = (float)((double)this.clCount + 0.1 * tt);
            if (this.clCount > 1.0F) {
               this.clCount = 1.0F;
            }

            if (!stack.method_31574(Items.field_16539) && !stack.method_31574(Items.field_22016)) {
               matrices.method_22907(RotationAxis.field_40714.rotationDegrees(-20.0F * this.clCount));
            }
         } else {
            this.clCount = (float)((double)this.clCount * Math.pow(0.88F, tt));
         }

         if (swingProgress == 0.0F) {
            matrices.method_46416(blx ? effectivePitch / 650.0F * this.clCount * -1.0F : effectivePitch / 650.0F * this.clCount, 0.0F, 0.0F);
            matrices.method_22907(RotationAxis.field_40714.rotationDegrees(effectivePitch * this.clCount));
         }

         if (!stack.method_31574(Items.field_16539) && !stack.method_31574(Items.field_22016)) {
            matrices.method_46416(0.0F, 0.0F, effectivePitch / 120.0F * this.clCount);
         } else if (swingProgress == 0.0F) {
            matrices.method_46416(0.0F, 0.0F, effectivePitch / 80.0F * this.clCount);
         }

         if (player.method_6101()
            && !player.method_24828()
            && !stack.method_31574(Items.field_16539)
            && !stack.method_31574(Items.field_22016)
            && !player.method_6115()) {
            matrices.method_22904(0.0, 0.1, -0.2);
         }

         if ((player.method_5799() || player.method_32314()) && !player.method_5681() && !player.method_5869()) {
            this.inWaterCounter = (float)((double)this.inWaterCounter + 0.1 * tt);
            if (this.inWaterCounter >= 1.0F) {
               this.inWaterCounter = 1.0F;
            }
         } else {
            this.inWaterCounter = (float)((double)this.inWaterCounter * Math.pow(0.88F, tt));
         }

         float freezingScale = MathHelper.method_15363((float)player.method_32312() / (float)player.method_32315(), 0.0F, 1.0F);
         if (player.method_32314() && (double)freezingScale > 0.1) {
            this.freezeCounter = (float)((double)this.freezeCounter + 0.1 * tt);
         } else {
            this.freezeCounter = (float)((double)this.freezeCounter * Math.pow(0.88F, tt));
         }

         matrices.method_22904(0.0, 0.02 * (double)this.inWaterCounter, 0.0);
         matrices.method_22907(RotationAxis.field_40718.rotationDegrees(8.0F * kjx * this.inWaterCounter));
         matrices.method_22907(RotationAxis.field_40714.rotationDegrees(0.3F * MathHelper.method_15374(this.freezeCounter * 5.0F)));
         if (vel.field_1351 < -0.85 && stack.method_31574(Items.field_49814) && player.method_6047() == stack) {
            this.fallCounter = (float)((double)this.fallCounter + 0.1 * tt);
            if (this.fallCounter >= 1.0F) {
               this.fallCounter = 1.0F;
            }
         } else {
            this.fallCounter = (float)((double)this.fallCounter * Math.pow(0.88F, tt));
         }

         if (blx) {
            matrices.method_22907(RotationAxis.field_40714.rotationDegrees(45.0F * this.fallCounter));
            matrices.method_22904(0.0, -0.2 * (double)this.fallCounter, 0.0);
         }

         this.vertAngleY = (float)((double)this.vertAngleY + vel.field_1351 * 0.015F * tt);
         this.vertAngleY = (float)((double)this.vertAngleY - (double)(0.1F * this.vertAngleY) * tt);
         this.vertAngleY = (float)((double)this.vertAngleY * Math.pow(0.88F, tt));
         this.vertVelocityYSlime = (float)((double)this.vertVelocityYSlime + vel.field_1351 * 0.015F * tt);
         this.vertVelocityYSlime = (float)((double)this.vertVelocityYSlime - (double)(0.1F * this.vertAngleYSlime) * tt);
         this.vertVelocityYSlime = (float)((double)this.vertVelocityYSlime * Math.pow(0.88F, tt));
         this.vertAngleYSlime = (float)((double)this.vertAngleYSlime + (double)this.vertVelocityYSlime * tt);
         matrices.method_46416(0.0F, this.vertAngleY * -1.0F, 0.0F);
         matrices.method_22904(0.0, Math.sin((double)player.field_6012 * 0.1) * 0.007 * (double)kjx, 0.0);
         matrices.method_22907(RotationAxis.field_40716.rotationDegrees(0.15F * MathHelper.method_15374((float)player.field_6012 * 0.15F) * kjx));
         if (!stack.method_7960() || player.method_20448() || player.method_6101() && !player.method_24828() || player.method_5681()) {
            if (player.method_6068() == Arm.field_6182) {
               blx = !blx;
            }

            if (stack.method_7976() == UseAction.field_8949) {
               matrices.method_46416(0.0F, 0.0F, 0.0F);
            } else {
               matrices.method_22904(0.0, -0.1, 0.1);
            }
         }

         if (stack.method_31574(Items.field_16539) || stack.method_31574(Items.field_22016) || stack.method_31573(ItemTags.field_40108)) {
            matrices.method_22904(0.0, 0.1, 0.0);
            if (player.method_5681()) {
               matrices.method_22904(0.0, -0.1, 0.1);
            }
         }

         if (player.method_5681() && swingProgress == 0.0F) {
            double s = (double)((float)player.field_6012 + tickDelta) * 0.1 * 2.0;
            double handRotation = Math.sin(s) * 1.5;
            double smoothRotation = handRotation * 0.8 + this.previousRotation * 0.2;
            matrices.method_22907(RotationAxis.field_40716.rotationDegrees((float)(blx ? smoothRotation : -smoothRotation)));
            matrices.method_46416(0.0F, 0.0F, (float)(smoothRotation * 0.2));
            double k = (double)((float)player.field_6012 + tickDelta) * 0.2;
            double a = Math.cos(k);
            double b = a <= 0.0 ? a * 0.5 : a;
            matrices.method_22907(RotationAxis.field_40715.rotationDegrees((float)(blx ? b * 30.0 : b * -30.0)));
            matrices.method_46416(0.0F, 0.0F, (float)(a * 0.2));
            if (stack.method_7960() && !blx && !player.method_5767()) {
               float j1 = blx ? 1.0F : -1.0F;
               matrices.method_22904((double)j1, 0.0 - (double)equipProgress * 0.3, 0.3);
               matrices.method_22907(RotationAxis.field_40716.rotationDegrees(45.0F * j1));
               matrices.method_22907(RotationAxis.field_40718.rotationDegrees(-40.0F * j1));
               matrices.method_22907(RotationAxis.field_40714.rotationDegrees(30.0F));
               this.altSwing(matrices, armx, swingProgress);
               matrices.method_22905(0.9F, 0.9F, 0.9F);
               acc.invokeRenderPlayerArm(matrices, vertexConsumers, light, 0.0F, 0.0F, armx);
            }

            this.previousRotation = smoothRotation;
         }

         if ((player.method_6101() && !player.method_24828() || player.method_20448() && swingProgress == 0.0F) && !player.method_6115()) {
            double s = (double)((float)player.field_6012 + tickDelta) * 0.1;
            float hx = MathHelper.method_15362((float)s * 2.0F);
            float j = blx ? 1.0F : -1.0F;
            if (player.method_6101()) {
               if (!stack.method_31574(Items.field_16539) && !stack.method_31574(Items.field_22016)) {
                  matrices.method_22907(RotationAxis.field_40714.rotationDegrees(20.0F * hx * j));
               } else {
                  matrices.method_22907(RotationAxis.field_40714.rotationDegrees(1.0F * hx * j));
               }
            }

            if (player.method_20448() && !player.method_6115() && swingProgress == 0.0F) {
               float timeValue = ((float)player.field_6012 + tickDelta) * 0.4F;
               float lx = MathHelper.method_15374(timeValue * this.mouseHolding);
               float dtx = MathHelper.method_15362(timeValue * this.mouseHolding);
               if (stack.method_31574(Items.field_16539) || stack.method_31574(Items.field_22016)) {
                  lx *= 0.14F;
                  dtx *= 0.14F;
               }

               matrices.method_22904(0.2 * (double)lx, 0.3 * (double)lx * (double)j, -0.2 * (double)lx * (double)j * (double)al);
               matrices.method_22907(RotationAxis.field_40716.rotationDegrees(25.0F * lx));
               matrices.method_22907(RotationAxis.field_40714.rotationDegrees(MathHelper.method_15363(20.0F * dtx * j, 0.0F, 20.0F)));
            }

            if (stack.method_7960() && !blx && !player.method_5767() && (!player.method_24828() && player.method_6101() || player.method_20448())) {
               float lx = blx ? 1.0F : -1.0F;
               matrices.method_22904((double)lx, 0.0 - (double)equipProgress * 0.3, 0.3);
               matrices.method_22907(RotationAxis.field_40716.rotationDegrees(45.0F * lx));
               matrices.method_22907(RotationAxis.field_40718.rotationDegrees(-40.0F * lx));
               matrices.method_22907(RotationAxis.field_40714.rotationDegrees(30.0F));
               this.altSwing(matrices, armx, swingProgress);
               matrices.method_22905(0.9F, 0.9F, 0.9F);
               acc.invokeRenderPlayerArm(matrices, vertexConsumers, light, 0.0F, 0.0F, armx);
            }
         }

         if (stack.method_7960()) {
            if (blx && !player.method_5767()) {
               float ll = blx ? 1.0F : -1.0F;
               if ((player.method_24828() || !player.method_6101()) && !player.method_5681() && !player.method_20448()) {
                  if (player.method_6068() == Arm.field_6182) {
                     blx = !blx;
                  }

                  matrices.method_22904(0.0, 0.2 * (double)swing_rot, 0.15 * (double)swing_rot);
                  matrices.method_22904(0.1 * (double)ll * (double)swing, 0.15 * (double)swing, -0.45 * (double)swing);
                  matrices.method_22907(RotationAxis.field_40716.rotationDegrees(35.0F * swing * ll));
                  matrices.method_22907(RotationAxis.field_40714.rotationDegrees(-30.0F * swing));
                  matrices.method_22907(RotationAxis.field_40716.rotationDegrees(-10.0F * swing_rot * ll));
                  matrices.method_22907(RotationAxis.field_40714.rotationDegrees(10.0F * swing_rot));
                  acc.invokeRenderPlayerArm(matrices, vertexConsumers, light, 0.0F, 0.0F, armx);
               } else {
                  matrices.method_22904((double)ll, 0.0 - (double)equipProgress * 0.3, 0.3);
                  matrices.method_22907(RotationAxis.field_40716.rotationDegrees(45.0F * ll));
                  matrices.method_22907(RotationAxis.field_40718.rotationDegrees(-40.0F * ll));
                  matrices.method_22907(RotationAxis.field_40714.rotationDegrees(30.0F));
                  this.altSwing(matrices, armx, swingProgress);
                  matrices.method_22905(0.9F, 0.9F, 0.9F);
                  acc.invokeRenderPlayerArm(matrices, vertexConsumers, light, 0.0F, 0.0F, armx);
               }
            }
         } else if (stack.method_57826(DataComponentTypes.field_49646)) {
            if (blx && acc.getOffHand().method_7960()) {
               matrices.method_22904(0.0, 0.1, 0.0);
               acc.invokeRenderTwoHandedMap(matrices, vertexConsumers, light, pitch, equipProgress, swingProgress);
            } else {
               matrices.method_46416(blx ? -0.1F : 0.1F, 0.1F, 0.0F);
               acc.invokeRenderOneHandedMap(matrices, vertexConsumers, light, equipProgress, armx, swingProgress, stack);
            }
         } else if (stack.method_7976() == UseAction.field_8947) {
            this.renderCrossbow(acc, player, tickDelta, pitch, hand, swingProgress, stack, equipProgress, matrices, vertexConsumers, light, blx, armx, kjx);
         } else {
            this.renderGeneralItem(
               acc, player, tickDelta, pitch, hand, swingProgress, stack, equipProgress, matrices, vertexConsumers, light, blx, armx, kjx, tt, swing, swing_rot
            );
         }

         matrices.method_22909();
         matrices.method_22909();
         this.isAttacking = MinecraftClient.method_1551().field_1690.field_1886.method_1434();
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
      matrices.method_22903();
      boolean bl2 = arm == Arm.field_6183;
      int i = bl2 ? 1 : -1;
      if (player.method_6115() && player.method_6014() > 0 && player.method_6058() == hand) {
         acc.invokeApplyItemArmTransform(matrices, arm, equipProgress);
         matrices.method_46416((float)i * -0.4785682F, -0.24387F, 0.05731531F);
         matrices.method_22907(RotationAxis.field_40714.rotationDegrees(-11.935F));
         matrices.method_22907(RotationAxis.field_40716.rotationDegrees((float)i * 65.3F));
         matrices.method_22907(RotationAxis.field_40718.rotationDegrees((float)i * 9.785F));
         float f = (float)stack.method_7935(player) - ((float)player.method_6014() - tickDelta + 1.0F);
         float g = f / (float)CrossbowItem.method_7775(stack, player);
         if (g > 1.0F) {
            g = 1.0F;
         }

         if (g > 0.1F) {
            float h = MathHelper.method_15374((f - 0.1F) * 1.3F);
            float yawDelta = h * (g - 0.1F);
            matrices.method_46416(yawDelta * 0.0F, yawDelta * 0.004F, yawDelta * 0.0F);
         }

         matrices.method_46416(g * 0.0F, g * 0.0F, g * 0.04F);
         matrices.method_22905(1.0F, 1.0F, 1.0F);
         matrices.method_22907(RotationAxis.field_40715.rotationDegrees((float)i * 45.0F));
      } else {
         acc.invokeSwingArm(swingProgress, equipProgress, matrices, i, arm);
         if (CrossbowItem.method_7781(stack) && swingProgress < 0.001F && bl) {
            matrices.method_46416((float)i * -0.341864F, 0.0F, 0.0F);
            matrices.method_22907(RotationAxis.field_40716.rotationDegrees((float)i * 10.0F));
         }
      }

      float yawDelta = bl ? 1.0F : -1.0F;
      matrices.method_46416(0.0F, 0.0F, -1.0F);
      matrices.method_22904(-0.45 * (double)i, 0.45, 1.7);
      matrices.method_22904((double)yawDelta, 0.0 - (double)equipProgress * 0.3, 0.3);
      matrices.method_22907(RotationAxis.field_40716.rotationDegrees(45.0F * yawDelta));
      matrices.method_22907(RotationAxis.field_40718.rotationDegrees(-40.0F * yawDelta));
      matrices.method_22907(RotationAxis.field_40714.rotationDegrees(30.0F));
      this.altSwing(matrices, arm, swingProgress);
      matrices.method_22905(0.9F, 0.9F, 0.9F);
      acc.invokeRenderPlayerArm(matrices, vertexConsumers, light, 0.0F, 0.0F, arm);
      matrices.method_22904((double)(-0.25F * (float)i), 1.25, 0.05);
      matrices.method_22907(RotationAxis.field_40716.rotationDegrees((float)(-90 * i)));
      matrices.method_22907(RotationAxis.field_40714.rotationDegrees(77.0F));
      matrices.method_22907(RotationAxis.field_40718.rotationDegrees((float)(85 * i)));
      matrices.method_22905(1.2F, 1.2F, 1.2F);
      matrices.method_22907(RotationAxis.field_40714.rotationDegrees(-10.0F));
      matrices.method_22904(0.0, -0.15, 0.15);
      acc.invokeRenderItem(player, stack, bl2 ? ModelTransformationMode.field_4322 : ModelTransformationMode.field_4321, !bl2, matrices, vertexConsumers, light);
      matrices.method_22909();
      if (player.method_6115() && player.method_6014() > 0 && player.method_6058() == hand) {
         float fx = (float)stack.method_7935(player) - ((float)player.method_6014() - tickDelta + 1.0F);
         float gx = fx / (float)CrossbowItem.method_7775(stack, player);
         if (gx > 1.0F) {
            gx = 1.0F;
         }

         if (gx > 0.1F) {
            float h = MathHelper.method_15374((fx - 0.1F) * 1.3F);
            float k = h * (gx - 0.1F);
            matrices.method_46416(k * 0.0F, k * 0.004F, k * 0.0F);
         }

         matrices.method_22907(RotationAxis.field_40715.rotationDegrees((double)gx <= 0.2 ? 75.0F * gx * 5.0F * (float)i : (float)(75 * i)));
         matrices.method_22907(RotationAxis.field_40713.rotationDegrees(10.0F * gx * 1.5F));
         matrices.method_22904(-0.37 * (double)i, 0.0, 0.6);
         matrices.method_22904(0.15 * (double)gx * (double)i, 0.0, 0.0);
         acc.invokeRenderPlayerArm(matrices, vertexConsumers, light, equipProgress, swingProgress, arm.method_5928());
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
      boolean bl2 = arm == Arm.field_6183;
      int l = bl2 ? 1 : -1;
      if (player.method_6115() && player.method_6014() > 0 && player.method_6058() == hand) {
         this.renderUsingItem(
            acc, player, tickDelta, hand, swingProgress, stack, equipProgress, matrices, vertexConsumers, light, bl, arm, kj, l, tt, swing, swing_rot
         );
      } else if (player.method_6123() && stack.method_7976() == UseAction.field_8951) {
         this.riptideCounter = (float)((double)this.riptideCounter + 0.15 * tt);
         float dt = (float)stack.method_7935(player) - ((float)player.method_6014() - tickDelta + 1.0F);
         float f = dt / 10.0F;
         if (f > 1.0F) {
            f = 1.0F;
         }

         if (f > 0.1F) {
            float g = MathHelper.method_15374((dt - 0.1F) * 1.3F);
            float j = g * (f - 0.1F);
            matrices.method_46416(j * 0.0F, j * 0.004F, j * 0.0F);
         }

         matrices.method_22907(RotationAxis.field_40714.rotationDegrees(45.0F - this.riptideCounter * 2.0F));
         matrices.method_22907(RotationAxis.field_40716.rotationDegrees((float)(25 * l)));
         matrices.method_22904(0.2 * (double)l, 0.0, 0.75);
         matrices.method_22904(0.0, 0.0, 0.01 * (double)MathHelper.method_15374(this.riptideCounter * 6.28F));
         acc.invokeRenderPlayerArm(matrices, vertexConsumers, light, equipProgress, swingProgress, arm);
         matrices.method_22907(RotationAxis.field_40714.rotationDegrees(135.0F));
         matrices.method_22907(RotationAxis.field_40718.rotationDegrees((float)(-65 * l)));
         matrices.method_22904((double)(0.65F * (float)l), -1.0, -0.6);
      } else {
         this.riptideCounter = 0.0F;
         if (!stack.method_31574(Items.field_16539) && !stack.method_31574(Items.field_22016) && !stack.method_31573(ItemTags.field_40108)) {
            if (stack.method_7976() == UseAction.field_8949) {
               matrices.method_22904(0.0, -0.2, 0.0);
            }
         } else {
            matrices.method_22904(0.1 * (double)l, 0.0, -0.1);
            matrices.method_22907(RotationAxis.field_40714.rotationDegrees(10.0F));
         }

         matrices.method_22904((double)l, 0.0 - (double)equipProgress * 0.3, 0.3);
         matrices.method_22907(RotationAxis.field_40716.rotationDegrees((float)(45 * l)));
         matrices.method_22907(RotationAxis.field_40718.rotationDegrees((float)(-40 * l)));
         matrices.method_22907(RotationAxis.field_40714.rotationDegrees(30.0F));
         this.altSwing(matrices, arm, swingProgress);
         matrices.method_22905(0.9F, 0.9F, 0.9F);
         acc.invokeRenderPlayerArm(matrices, vertexConsumers, light, 0.0F, 0.0F, arm);
      }

      if (player.method_6115() && player.method_6014() > 0 && player.method_6058() == hand && stack.method_7976() == UseAction.field_8953) {
         matrices.method_22907(RotationAxis.field_40714.rotationDegrees(75.0F));
         matrices.method_22907(RotationAxis.field_40717.rotationDegrees((float)(-15 * l)));
         matrices.method_22904(0.8 * (double)l, (double)(0.0F - equipProgress * 0.3F), -0.1);
      } else {
         matrices.method_22904(-0.3 * (double)l, 0.65, -0.1);
         matrices.method_22907(RotationAxis.field_40716.rotationDegrees((float)(-65 * l)));
         matrices.method_22907(RotationAxis.field_40714.rotationDegrees(10.0F));
      }

      if (stack.method_7909() instanceof BlockItem && !stack.method_31573(ItemTags.field_41890) && !stack.method_31573(ItemTags.field_40109)) {
         this.renderBlockItem(acc, player, stack, matrices, vertexConsumers, light, bl, bl2, l, arm, tt, swing, swing_rot, equipProgress, swingProgress);
      } else {
         boolean isToolOrWeapon = this.isTool(stack)
               && !stack.method_31573(ItemTags.field_41890)
               && !stack.method_31573(ItemTags.field_40109)
               && stack.method_7976() != UseAction.field_8950
               && stack.method_7923()
            || stack.method_7976() == UseAction.field_8953
            || stack.method_7976() == UseAction.field_27079
            || this.getAttackDamage(stack) != 0.0F
            || stack.method_7976() == UseAction.field_8949
            || stack.method_31574(Items.field_23254)
            || stack.method_31574(Items.field_8184)
            || stack.method_7909() instanceof FishingRodItem
            || stack.method_31574(Items.field_8868);
         if (isToolOrWeapon) {
            if (this.isSword(stack)) {
               matrices.method_22907(RotationAxis.field_40715.rotationDegrees((float)(75 * l)));
               matrices.method_22907(RotationAxis.field_40714.rotationDegrees(70.0F));
               matrices.method_22907(RotationAxis.field_40718.rotationDegrees((float)(45 * l)));
               matrices.method_22905(1.2F, 1.2F, 1.2F);
               matrices.method_22907(RotationAxis.field_40714.rotationDegrees(-60.0F * swing));
               matrices.method_22904(0.0, 0.1 * (double)swing, -0.1 * (double)swing);
            } else if (stack.method_7976() == UseAction.field_8951) {
               matrices.method_22907(RotationAxis.field_40715.rotationDegrees((float)(75 * l)));
               matrices.method_22907(RotationAxis.field_40714.rotationDegrees(90.0F));
               matrices.method_22907(RotationAxis.field_40718.rotationDegrees((float)(45 * l)));
               matrices.method_46416(-0.3F * (float)l, 0.0F, 0.0F);
               matrices.method_22905(1.2F, 1.2F, 1.2F);
               matrices.method_22907(RotationAxis.field_40714.rotationDegrees(-40.0F * swing_rot));
               matrices.method_22904(0.0, 0.1 * (double)swing_rot, -0.1 * (double)swing_rot);
            } else if (stack.method_7976() == UseAction.field_8953) {
               matrices.method_22907(RotationAxis.field_40718.rotationDegrees((float)(160 * l)));
               matrices.method_22907(RotationAxis.field_40716.rotationDegrees((float)(-60 * l)));
               matrices.method_22907(RotationAxis.field_40714.rotationDegrees(-70.0F));
               matrices.method_22905(0.75F, 0.75F, 0.75F);
               matrices.method_22904(0.15 * (double)l, bl ? 0.35F : 0.45F, bl ? -0.15F : -0.1F);
               matrices.method_22904(0.17 * (double)l, 0.0, 0.3);
               matrices.method_22907(RotationAxis.field_40716.rotationDegrees((float)(-90 * l)));
            } else if (stack.method_7976() == UseAction.field_8949) {
               matrices.method_22907(RotationAxis.field_40715.rotationDegrees((float)(-154 * l)));
               matrices.method_22907(RotationAxis.field_40714.rotationDegrees(5.0F));
               matrices.method_22907(RotationAxis.field_40718.rotationDegrees((float)(90 * l)));
               matrices.method_22904((double)(-0.012F * (float)l), 0.38, -0.175);
               matrices.method_22905(0.55F, 0.55F, 0.55F);
            } else if (stack.method_31573(ItemTags.field_42615)) {
               matrices.method_22907(RotationAxis.field_40715.rotationDegrees((float)(75 * l)));
               matrices.method_22907(RotationAxis.field_40714.rotationDegrees(70.0F));
               matrices.method_22907(RotationAxis.field_40718.rotationDegrees((float)(45 * l)));
               matrices.method_22905(1.2F, 1.2F, 1.2F);
               matrices.method_22907(RotationAxis.field_40714.rotationDegrees(-80.0F * swing_rot));
               matrices.method_22907(RotationAxis.field_40714.rotationDegrees(30.0F * swing));
               matrices.method_22904(0.07 * (double)l, 0.0, 0.05);
               matrices.method_22907(RotationAxis.field_40716.rotationDegrees((float)(90 * l)));
               matrices.method_22907(RotationAxis.field_40714.rotationDegrees(-15.0F));
            } else {
               matrices.method_22907(RotationAxis.field_40715.rotationDegrees((float)(75 * l)));
               matrices.method_22907(RotationAxis.field_40714.rotationDegrees(70.0F));
               matrices.method_22907(RotationAxis.field_40718.rotationDegrees((float)(45 * l)));
               matrices.method_22905(1.2F, 1.2F, 1.2F);
               matrices.method_22907(RotationAxis.field_40714.rotationDegrees(-25.0F * swing));
               matrices.method_22904(0.0, 0.05 * (double)swing, -0.05 * (double)swing);
            }
         } else {
            matrices.method_22907(RotationAxis.field_40715.rotationDegrees((float)(5 * l)));
            matrices.method_22907(RotationAxis.field_40714.rotationDegrees(-13.0F));
            matrices.method_22907(RotationAxis.field_40718.rotationDegrees((float)(75 * l)));
            matrices.method_22904(-0.015 * (double)l, -0.055, -0.06);
            matrices.method_22905(0.7F, 0.7F, 0.7F);
         }

         if (!stack.method_31574(Items.field_8137)) {
            if (stack.method_31574(Items.field_8301)) {
            }

            this.netherCounter = 0.0F;
         } else {
            this.netherCounter = (float)((double)this.netherCounter + 0.9 * tt);
            matrices.method_22904(0.0, 0.25 + 0.02 * (double)MathHelper.method_15374(this.netherCounter * 0.1F), 0.0);
            matrices.method_22907(RotationAxis.field_40714.rotationDegrees(3.0F * MathHelper.method_15374(this.netherCounter * 0.2F)));
            float ns = 1.0F + 0.01F * MathHelper.method_15374(this.netherCounter);
            matrices.method_22905(ns, ns, ns);
         }

         acc.invokeRenderItem(
            player, stack, bl2 ? ModelTransformationMode.field_4322 : ModelTransformationMode.field_4321, !bl2, matrices, vertexConsumers, light
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
      UseAction useAction = stack.method_7976();
      if (useAction == UseAction.field_8952) {
         acc.invokeApplyItemArmTransform(matrices, arm, equipProgress);
      } else if (useAction == UseAction.field_8950 || useAction == UseAction.field_8946) {
         float yawDelta = (float)stack.method_7935(player) - ((float)player.method_6014() - tickDelta + 1.0F);
         float pitchDelta = yawDelta / 5.0F;
         if (pitchDelta > 1.0F) {
            pitchDelta = 1.0F;
         }

         float k = MathHelper.method_15374(yawDelta / 2.0F * 3.14F) / 10.0F;
         matrices.method_22904((double)l, 0.1, 0.3);
         matrices.method_22904(0.2 * (double)l * (double)pitchDelta, -0.7 * (double)pitchDelta, -0.2 * (double)pitchDelta);
         matrices.method_22904(0.0, -0.2 * (double)k, -0.2 * (double)k);
         matrices.method_46416(0.0F, 0.1F * this.easeInOutBack(MathHelper.method_15374(pitchDelta * 3.14F)), 0.0F);
         matrices.method_22907(RotationAxis.field_40716.rotationDegrees((float)(45 * l)));
         matrices.method_22907(RotationAxis.field_40718.rotationDegrees((float)(-40 * l)));
         matrices.method_22907(RotationAxis.field_40714.rotationDegrees(30.0F));
         this.altSwing(matrices, arm, swingProgress);
         matrices.method_22905(0.9F, 0.9F, 0.9F);
         matrices.method_22907(RotationAxis.field_40716.rotationDegrees(45.0F * pitchDelta * (float)l));
         acc.invokeRenderPlayerArm(matrices, vertexConsumers, light, 0.0F, swingProgress, arm);
      } else if (useAction == UseAction.field_8949) {
         float k = (float)stack.method_7935(player) - ((float)player.method_6014() - tickDelta + 1.0F);
         double s = (double)(k / 4.0F);
         float s2 = k / 6.0F;
         if (s > 1.0) {
            s = 1.0;
         }

         if (s2 > 1.0F) {
            s2 = 1.0F;
         }

         matrices.method_22904(0.0, -0.2, 0.0);
         matrices.method_22904((double)l, 0.0, 0.3);
         matrices.method_22904(0.7 * s * (double)l, 0.0, -1.3 * s);
         matrices.method_22904(-0.2 * (double)l * (double)s2, 0.0, 0.0);
         matrices.method_22907(RotationAxis.field_40714.rotationDegrees((float)(15.0 * Math.sin((double)s2 * 3.14))));
         matrices.method_22907(RotationAxis.field_40716.rotationDegrees((float)(70.0 * s * (double)l)));
         matrices.method_22907(RotationAxis.field_40716.rotationDegrees((float)(45 * l)));
         matrices.method_22907(RotationAxis.field_40718.rotationDegrees((float)(-40 * l)));
         matrices.method_22907(RotationAxis.field_40714.rotationDegrees(30.0F));
         matrices.method_22907(RotationAxis.field_40716.rotationDegrees((float)(5 * l) * (float)s));
         matrices.method_22907(RotationAxis.field_40714.rotationDegrees((float)(-10.0 * s)));
         matrices.method_22904(0.0, 0.0, -0.2 * s);
         this.altSwing(matrices, arm, swingProgress);
         matrices.method_22905(0.9F, 0.9F, 0.9F);
         acc.invokeRenderPlayerArm(matrices, vertexConsumers, light, 0.0F, swingProgress, arm);
         if (l == 1) {
            matrices.method_22904(0.19F, 0.1, -0.24);
            matrices.method_22907(RotationAxis.field_40718.rotationDegrees(0.0F));
            matrices.method_22907(RotationAxis.field_40716.rotationDegrees(4.0F));
            matrices.method_22907(RotationAxis.field_40714.rotationDegrees(-10.4F));
            matrices.method_22904(-0.2, -0.04, 0.15);
         } else {
            matrices.method_22904(-0.19F, 0.1, -0.18);
            matrices.method_22907(RotationAxis.field_40718.rotationDegrees(0.0F));
            matrices.method_22907(RotationAxis.field_40716.rotationDegrees(-4.0F));
            matrices.method_22907(RotationAxis.field_40714.rotationDegrees(-10.4F));
            matrices.method_22904(0.2, -0.04, 0.15);
         }
      } else if (useAction == UseAction.field_8953) {
         matrices.method_22903();
         float m1 = (float)stack.method_7935(player) - ((float)player.method_6014() - tickDelta + 1.0F);
         float f1 = m1 / 20.0F;
         if (f1 > 1.0F) {
            f1 = 1.0F;
         }

         if (f1 > 0.1F) {
            float g1 = MathHelper.method_15374((m1 - 0.1F) * 1.3F);
            float j1 = g1 * f1;
            matrices.method_46416(j1 * 0.0F, j1 * 0.004F, j1 * 0.0F);
         }

         matrices.method_22903();
         matrices.method_46416(bl ? -0.1F : 0.1F, 0.0F, f1 * 0.15F);
         acc.invokeRenderPlayerArm(matrices, vertexConsumers, light, equipProgress, swingProgress, arm);
         matrices.method_22909();
         matrices.method_22904(bl ? -0.5 : 0.5, -0.45, 0.1);
         matrices.method_22907(RotationAxis.field_40714.rotation(0.3F));
         if (bl) {
            matrices.method_22907(RotationAxis.field_40717.rotation(-0.3F));
            matrices.method_22907(RotationAxis.field_40715.rotation(1.0F));
         } else {
            matrices.method_22907(RotationAxis.field_40718.rotation(-0.3F));
            matrices.method_22907(RotationAxis.field_40716.rotation(1.0F));
         }

         acc.invokeRenderPlayerArm(matrices, vertexConsumers, light, equipProgress, swingProgress, arm.method_5928());
         if (bl) {
            matrices.method_22907(RotationAxis.field_40715.rotation(2.5F));
         } else {
            matrices.method_22907(RotationAxis.field_40716.rotation(2.5F));
         }

         matrices.method_22904(bl ? -0.65 : 0.65, -0.35, 0.27);
         matrices.method_22909();
      } else if (useAction == UseAction.field_8951) {
         if (player.method_6079().method_7960() && !player.method_20448() && !player.method_5681() && !player.method_6101()) {
            matrices.method_22903();
            matrices.method_22907(RotationAxis.field_40716.rotationDegrees((float)(-25 * l)));
            matrices.method_22904(-0.15 * (double)l, 0.1, 0.1);
            acc.invokeRenderPlayerArm(matrices, vertexConsumers, light, equipProgress, swingProgress, arm.method_5928());
            matrices.method_22909();
         }

         float dt = (float)stack.method_7935(player) - ((float)player.method_6014() - tickDelta + 1.0F);
         float f = dt / 10.0F;
         if (f > 1.0F) {
            f = 1.0F;
         }

         if (f > 0.1F) {
            float g = MathHelper.method_15374((dt - 0.1F) * 1.3F);
            float j = g * (f - 0.1F);
            matrices.method_46416(j * 0.0F, j * 0.004F, j * 0.0F);
         }

         matrices.method_22907(RotationAxis.field_40714.rotationDegrees(45.0F));
         matrices.method_22907(RotationAxis.field_40716.rotationDegrees((float)(25 * l)));
         matrices.method_22904(0.2 * (double)l, 0.0, 0.8);
         acc.invokeRenderPlayerArm(matrices, vertexConsumers, light, equipProgress, swingProgress, arm);
         matrices.method_22907(RotationAxis.field_40714.rotationDegrees(100.0F));
         matrices.method_22907(RotationAxis.field_40718.rotationDegrees((float)(-55 * l)));
         matrices.method_22904((double)(0.5F * (float)l), -0.7, -0.35);
      } else if (useAction == UseAction.field_27079) {
         float g1 = (float)(player.method_6014() % 10);
         float h1 = g1 - tickDelta + 1.0F;
         float j1 = 1.0F - h1 / 10.0F;
         float n = -15.0F + 75.0F * MathHelper.method_15362(j1 * 2.0F * (float) Math.PI);
         float z = (float)stack.method_7935(player) - ((float)player.method_6014() - tickDelta + 1.0F);
         float x = z / 4.0F;
         if (x > 1.0F) {
            x = 1.0F;
         }

         matrices.method_22907(RotationAxis.field_40716.rotationDegrees((float)(25 * l) * x));
         matrices.method_22904((double)(0.3F * (float)l * x), 0.3 * (double)x, 0.1 * (double)x);
         if (x == 1.0F) {
            matrices.method_22907(RotationAxis.field_40716.rotationDegrees(n / 20.0F));
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
      Block block = Block.method_9503(stack.method_7909());
      if (block != Blocks.field_10124) {
         BlockState state = block.method_9564();
         if (this.isSmallBlockItem(stack)) {
            matrices.method_22904(0.0, 0.0, -0.1);
            matrices.method_22907(RotationAxis.field_40715.rotationDegrees((float)(5 * l)));
            matrices.method_22907(RotationAxis.field_40714.rotationDegrees(15.0F));
            matrices.method_22907(RotationAxis.field_40718.rotationDegrees((float)(75 * l)));
         } else if (stack.method_7964().getString().toLowerCase().contains("torch")) {
            matrices.method_22905(1.5F, 1.5F, 1.5F);
            matrices.method_22907(RotationAxis.field_40715.rotationDegrees((float)(25 * l)));
            matrices.method_22907(RotationAxis.field_40714.rotationDegrees(5.0F));
            matrices.method_22907(RotationAxis.field_40718.rotationDegrees((float)(75 * l)));
            matrices.method_22904(0.2 * (double)l, 0.2, 0.05);
         } else if (!stack.method_31574(Items.field_16539) && !stack.method_31574(Items.field_22016) && !stack.method_31573(ItemTags.field_40108)) {
            matrices.method_22907(RotationAxis.field_40715.rotationDegrees((float)(25 * l)));
            matrices.method_22907(RotationAxis.field_40714.rotationDegrees(5.0F));
            matrices.method_22907(RotationAxis.field_40718.rotationDegrees((float)(75 * l)));
            matrices.method_22904(0.2 * (double)l, 0.2, 0.05);
         } else {
            float dt = (float)(deltaTime * 8.0);
            float yawDelta = player.field_5982 - player.method_36454();
            float pitchDelta = player.field_6004 - player.method_36455();
            this.swingVelocityY += yawDelta * 0.015F * dt;
            this.swingVelocityY += swingProgress * 2.0F * dt;
            this.swingVelocityX += pitchDelta * 0.015F * dt;
            this.swingVelocityY = this.swingVelocityY - 0.1F * this.swingAngleY * dt;
            this.swingVelocityX = this.swingVelocityX - 0.1F * this.swingAngleX * dt;
            this.swingVelocityY = (float)((double)this.swingVelocityY * Math.pow(0.88F, (double)dt));
            this.swingVelocityX = (float)((double)this.swingVelocityX * Math.pow(0.88F, (double)dt));
            this.swingAngleY = this.swingAngleY + this.swingVelocityY * dt;
            this.swingAngleX = this.swingAngleX + this.swingVelocityX * dt;
            double currentSpeed = player.method_18798().method_1033();
            this.swingVelocityZ = (float)(
               (double)this.swingVelocityZ
                  + (bl ? currentSpeed * -15.0 - (double)this.swingVelocityZ : currentSpeed * 15.0 - (double)this.swingVelocityZ) * 0.1F * (double)dt
            );
            if ((currentSpeed > 0.09 && player.method_24828() || player.method_5681() || player.method_6101() && !player.method_24828())
               && (Boolean)MinecraftClient.method_1551().field_1690.method_42448().method_41753()) {
               boolean randomBool = Math.random() > 0.5;
               this.swingVelocityY = (float)((double)this.swingVelocityY + (randomBool ? -5.5 * currentSpeed * (double)dt : 5.5 * currentSpeed * (double)dt));
            }

            matrices.method_22904(0.0, 0.0, -0.1);
            matrices.method_22907(RotationAxis.field_40715.rotationDegrees((float)(35 * l) + this.swingAngleY));
            matrices.method_22907(RotationAxis.field_40714.rotationDegrees(15.0F + this.swingAngleX));
            matrices.method_22907(RotationAxis.field_40718.rotationDegrees((float)(75 * l) + this.swingVelocityZ));
            if (stack.method_31573(ItemTags.field_40108)) {
               matrices.method_22904(0.0, -0.1, 0.0);
               matrices.method_22907(RotationAxis.field_40716.rotationDegrees((float)(-45 * l)));
            }

            matrices.method_22904(0.3 * (double)l, -0.35, 0.0);
            matrices.method_22904(0.0, 0.0, 0.1);
            matrices.method_22905(1.5F, 1.5F, 1.5F);
         }

         BlockRenderManager blockRenderManager = MinecraftClient.method_1551().method_1541();
         matrices.method_22903();
         if (!bl2) {
            matrices.method_46416(-0.4F, 0.0F, 0.0F);
         }

         matrices.method_22905(0.35F, 0.35F, 0.35F);
         matrices.method_22904(-0.9 * (double)l, -0.45, -0.5);
         if (state.method_26164(BlockTags.field_15493)) {
            matrices.method_22904(0.2 * (double)l, -0.15, -0.2);
         }

         if (state.method_26164(BlockTags.field_24076)) {
            matrices.method_22904(0.0, 0.1, 0.0);
         }

         if (stack.method_31574(Items.field_8828)
            || stack.method_31574(Items.field_21086)
            || state.method_26164(BlockTags.field_20339)
            || state.method_26164(BlockTags.field_15503)
            || state.method_26164(BlockTags.field_15462)
            || state.method_26164(BlockTags.field_44469)) {
            this.vertVelocityYSlime = (float)((double)this.vertVelocityYSlime + (double)swingProgress * 0.03 * deltaTime * 8.0);
            if ((
                  player.method_18798().method_1033() > 0.09 && player.method_24828()
                     || player.method_5681()
                     || player.method_20448()
                     || player.method_6101() && !player.method_24828()
               )
               && (Boolean)MinecraftClient.method_1551().field_1690.method_42448().method_41753()) {
               this.vertVelocityYSlime = (float)((double)this.vertVelocityYSlime + -0.05F * player.method_18798().method_1033() * deltaTime * 8.0);
            }

            matrices.method_22905(1.0F, 1.0F + this.vertAngleYSlime * -2.0F, 1.0F);
         }

         if ((float)player.field_6012 - this.prevAge >= 100.0F) {
            this.repPower = !this.repPower;
            this.prevAge = (float)player.field_6012;
         }

         if (stack.method_31573(ItemTags.field_16444) && bl) {
            matrices.method_22904(0.9, 0.0, 0.8);
            matrices.method_22907(RotationAxis.field_40716.rotationDegrees((float)(90 * l)));
         }

         blockRenderManager.method_3353(state, matrices, vertexConsumers, light, OverlayTexture.field_21444);
         matrices.method_22909();
      }
   }
}
