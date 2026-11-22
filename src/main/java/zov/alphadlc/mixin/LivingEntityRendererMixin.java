package zov.alphadlc.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import zov.alphadlc.module.list.combat.KillAura;
import zov.alphadlc.module.list.render.SeeInvisible;
import zov.alphadlc.util.IMinecraft;
import zov.alphadlc.util.base.Instance;
import zov.alphadlc.util.math.RotationUtil;
import zov.alphadlc.util.player.combat.PredictUtils;

@Mixin({LivingEntityRenderer.class})
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>>
   extends EntityRenderer<T, S>
   implements FeatureRendererContext<S, M>,
   IMinecraft {
   @Shadow
   private static float method_62482(LivingEntity entity, float degrees, float tickDelta) {
      return 0.0F;
   }

   @Shadow
   public static boolean method_38563(LivingEntity entity) {
      return false;
   }

   protected LivingEntityRendererMixin(Context context) {
      super(context);
   }

   @Overwrite
   public void method_62355(T livingEntity, S livingEntityRenderState, float f) {
      super.method_62354(livingEntity, livingEntityRenderState, f);
      float yaw = MathHelper.method_17821(f, livingEntity.field_6259, livingEntity.field_6241);
      livingEntityRenderState.field_53446 = method_62482(livingEntity, yaw, f);
      livingEntityRenderState.field_53447 = MathHelper.method_15393(yaw - livingEntityRenderState.field_53446);
      livingEntityRenderState.field_53448 = livingEntity.method_61414(f);
      livingEntityRenderState.field_53464 = livingEntity.method_5797();
      livingEntityRenderState.field_53455 = method_38563(livingEntity);
      if (livingEntityRenderState.field_53455) {
         livingEntityRenderState.field_53448 *= -1.0F;
         livingEntityRenderState.field_53447 *= -1.0F;
      }

      if (!livingEntity.method_5765() && livingEntity.method_5805()) {
         livingEntityRenderState.field_53450 = livingEntity.field_42108.method_48572(f);
         livingEntityRenderState.field_53451 = livingEntity.field_42108.method_48570(f);
      } else {
         livingEntityRenderState.field_53450 = 0.0F;
         livingEntityRenderState.field_53451 = 0.0F;
      }

      if (livingEntity.method_5854() instanceof LivingEntity livingEntity2) {
         livingEntityRenderState.field_53452 = livingEntity2.field_42108.method_48572(f);
      } else {
         livingEntityRenderState.field_53452 = livingEntityRenderState.field_53450;
      }

      livingEntityRenderState.field_53453 = livingEntity.method_55693();
      livingEntityRenderState.field_53454 = livingEntity.method_17825();
      livingEntityRenderState.field_53465 = livingEntity.method_18376();
      livingEntityRenderState.field_53463 = livingEntity.method_18401();
      if (livingEntityRenderState.field_53463 != null) {
         livingEntityRenderState.field_53331 = livingEntity.method_18381(EntityPose.field_18076);
      }

      livingEntityRenderState.field_53456 = livingEntity.method_32314();
      livingEntityRenderState.field_53457 = livingEntity.method_6109();
      livingEntityRenderState.field_53458 = livingEntity.method_5799();
      livingEntityRenderState.field_53459 = livingEntity.method_6123();
      livingEntityRenderState.field_53460 = livingEntity.field_6235 > 0 || livingEntity.field_6213 > 0;
      livingEntityRenderState.field_53449 = livingEntity.field_6213 > 0 ? (float)livingEntity.field_6213 + f : 0.0F;
      livingEntityRenderState.field_53461 = livingEntityRenderState.field_53333 && livingEntity.method_5756(MinecraftClient.method_1551().field_1724);
      livingEntityRenderState.field_53462 = MinecraftClient.method_1551().method_27022(livingEntity);
      if (livingEntityRenderState.field_53333 && livingEntity instanceof PlayerEntity) {
         SeeInvisible seeInvisible = Instance.get(SeeInvisible.class);
         if (seeInvisible != null && seeInvisible.isEnabled()) {
            if (seeInvisible.isOpaque()) {
               livingEntityRenderState.field_53333 = false;
            } else {
               livingEntityRenderState.field_53461 = false;
            }
         }
      }

      if (livingEntity == mc.field_1724 && livingEntity.method_6128()) {
         KillAura killAura = Instance.get(KillAura.class);
         if (killAura != null && killAura.isEnabled() && killAura.visualElytraRotation.getValue()) {
            LivingEntity target = killAura.getTarget();
            if (target != null && target.method_5805() && target.method_6128()) {
               Vec3d playerPos = livingEntity.method_30950(f);
               Vec3d targetPos = target.method_30950(f);
               Vec3d targetLook = target.method_5828(f).method_1029();
               Vec3d targetToPlayer = playerPos.method_1020(targetPos);
               double dot = targetToPlayer.method_1026(targetLook);
               Vec3d predict = PredictUtils.predict(target, killAura.predictValue.getValue());
               double distToPredict = playerPos.method_1022(predict);
               if (dot > 0.0 && distToPredict < 6.0) {
                  Vec3d center = targetPos.method_1031(0.0, (double)target.method_17682() / 2.0, 0.0);
                  Vec2f rotation = RotationUtil.calculate(center);
                  livingEntityRenderState.field_53446 = rotation.field_1343;
                  livingEntityRenderState.field_53447 = 0.0F;
                  livingEntityRenderState.field_53448 = rotation.field_1342;
               }
            }
         }
      }
   }
}
