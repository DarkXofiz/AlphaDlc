package zov.alphadlc.mixin;

import com.google.common.base.MoreObjects;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.item.HeldItemRenderer.HandRenderType;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zov.alphadlc.module.list.render.GlassHands;
import zov.alphadlc.module.list.render.HMIRenderer;
import zov.alphadlc.module.list.render.HeldItemRendererAccessor;
import zov.alphadlc.module.list.render.SwingAnimations;
import zov.alphadlc.module.list.render.ViewModel;
import zov.alphadlc.util.base.Instance;
import zov.alphadlc.util.render.hands.GlassHandsRenderer;

@Mixin({HeldItemRenderer.class})
public abstract class HeldItemRendererMixin implements HeldItemRendererAccessor {
   @Shadow
   private ItemStack field_4047;
   @Shadow
   private float field_4043;
   @Shadow
   private float field_4053;
   @Shadow
   private float field_4051;
   @Shadow
   private float field_4052;
   @Shadow
   private ItemStack field_4048;

   @Shadow
   protected abstract void method_3228(
      AbstractClientPlayerEntity var1,
      float var2,
      float var3,
      Hand var4,
      float var5,
      ItemStack var6,
      float var7,
      MatrixStack var8,
      VertexConsumerProvider var9,
      int var10
   );

   @Shadow
   protected abstract void method_65816(float var1, float var2, MatrixStack var3, int var4, Arm var5);

   @Shadow
   private static HandRenderType method_33303(ClientPlayerEntity player) {
      throw new AssertionError();
   }

   @Invoker("renderArmHoldingItem")
   @Override
   public abstract void invokeRenderPlayerArm(MatrixStack var1, VertexConsumerProvider var2, int var3, float var4, float var5, Arm var6);

   @Invoker("applyEquipOffset")
   @Override
   public abstract void invokeApplyItemArmTransform(MatrixStack var1, Arm var2, float var3);

   @Invoker("swingArm")
   @Override
   public abstract void invokeSwingArm(float var1, float var2, MatrixStack var3, int var4, Arm var5);

   @Invoker("renderItem")
   @Override
   public abstract void invokeRenderItem(
      LivingEntity var1, ItemStack var2, ModelTransformationMode var3, boolean var4, MatrixStack var5, VertexConsumerProvider var6, int var7
   );

   @Invoker("renderMapInBothHands")
   @Override
   public abstract void invokeRenderTwoHandedMap(MatrixStack var1, VertexConsumerProvider var2, int var3, float var4, float var5, float var6);

   @Invoker("renderMapInOneHand")
   @Override
   public abstract void invokeRenderOneHandedMap(MatrixStack var1, VertexConsumerProvider var2, int var3, float var4, Arm var5, float var6, ItemStack var7);

   @Accessor("offHand")
   @Override
   public abstract ItemStack getOffHand();

   @Inject(
      method = {"renderFirstPersonItem"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void injectHMIHead(
      AbstractClientPlayerEntity player,
      float tickDelta,
      float pitch,
      Hand hand,
      float swingProgress,
      ItemStack item,
      float equipProgress,
      MatrixStack matrices,
      VertexConsumerProvider vertexConsumers,
      int light,
      CallbackInfo ci
   ) {
      SwingAnimations swingAnimation = Instance.get(SwingAnimations.class);
      if (swingAnimation != null && swingAnimation.isEnabled() && swingAnimation.isHMI()) {
         HMIRenderer hmi = swingAnimation.getHMIRenderer();
         hmi.renderHMI((HeldItemRenderer)this, this, player, tickDelta, pitch, hand, swingProgress, item, equipProgress, matrices, vertexConsumers, light);
         ci.cancel();
      }
   }

   @Inject(
      method = {"renderFirstPersonItem"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/util/math/MatrixStack;push()V",
         shift = Shift.AFTER,
         ordinal = 0
      )}
   )
   public void injectAfterMatrixPushHandPosition(
      AbstractClientPlayerEntity player,
      float tickDelta,
      float pitch,
      Hand hand,
      float swingProgress,
      ItemStack item,
      float equipProgress,
      MatrixStack matrices,
      VertexConsumerProvider vertexConsumers,
      int light,
      CallbackInfo ci
   ) {
      ViewModel viewModel = Instance.get(ViewModel.class);
      if (viewModel != null && viewModel.isEnabled() && !item.method_7960() && !item.method_57826(DataComponentTypes.field_49646)) {
         boolean isMainHand = hand == Hand.field_5808;
         Arm arm = isMainHand ? player.method_6068() : player.method_6068().method_5928();
         viewModel.applyHandPosition(matrices, arm);
      }
   }

   @Redirect(
      method = {"renderFirstPersonItem"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/item/HeldItemRenderer;swingArm(FFLnet/minecraft/client/util/math/MatrixStack;ILnet/minecraft/util/Arm;)V",
         ordinal = 2
      )
   )
   public void redirectSwingArmForCustomAnim(HeldItemRenderer instance, float swingProgress, float equipProgress, MatrixStack matrices, int armX, Arm arm) {
      SwingAnimations swingAnimation = Instance.get(SwingAnimations.class);
      if (swingAnimation == null || !swingAnimation.isEnabled()) {
         this.method_65816(swingProgress, equipProgress, matrices, armX, arm);
      } else if (arm == Arm.field_6183) {
         swingAnimation.renderSwordAnimation(matrices, swingProgress, equipProgress, arm);
      } else {
         this.method_65816(swingProgress, equipProgress, matrices, armX, arm);
      }
   }

   @Overwrite
   public void method_22976(float tickDelta, MatrixStack matrices, Immediate vertexConsumers, ClientPlayerEntity player, int light) {
      GlassHands glassHands = Instance.get(GlassHands.class);
      boolean glassActive = glassHands != null && glassHands.isEnabled();
      if (glassActive) {
         GlassHandsRenderer.getInstance().captureBeforeHands(glassHands);
      }

      float f = player.method_6055(tickDelta);
      Hand hand = (Hand)MoreObjects.firstNonNull(player.field_6266, Hand.field_5808);
      float g = player.method_61414(tickDelta);
      HandRenderType handRenderType = method_33303(player);
      if (handRenderType.field_28387) {
         float j = hand == Hand.field_5808 ? f : 0.0F;
         float k = 1.0F - MathHelper.method_16439(tickDelta, this.field_4053, this.field_4043);
         this.method_3228(player, tickDelta, g, Hand.field_5808, j, this.field_4047, k, matrices, vertexConsumers, light);
      }

      if (handRenderType.field_28388) {
         float j = hand == Hand.field_5810 ? f : 0.0F;
         float k = 1.0F - MathHelper.method_16439(tickDelta, this.field_4051, this.field_4052);
         this.method_3228(player, tickDelta, g, Hand.field_5810, j, this.field_4048, k, matrices, vertexConsumers, light);
      }

      vertexConsumers.method_22993();
      if (glassActive) {
         GlassHandsRenderer.getInstance().captureAfterHands(glassHands);
      }
   }
}
