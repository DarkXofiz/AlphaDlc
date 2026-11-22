package zov.alphadlc.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import zov.alphadlc.event.list.EventChangeSprint;
import zov.alphadlc.module.list.player.NoPush;
import zov.alphadlc.module.list.render.SwingAnimations;
import zov.alphadlc.util.base.Instance;

@Mixin({LivingEntity.class})
public class LivingEntityMixin {
   @Inject(
      method = {"pushAwayFrom"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void cancelPushAway(Entity entity, CallbackInfo ci) {
      if (Instance.get(NoPush.class).isEnabled() && Instance.get(NoPush.class).objects.isEnabled("\u0418\u0433\u0440\u043e\u043a\u0438")) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"setSprinting"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onSetSprinting(boolean sprinting, CallbackInfo ci) {
      if (((net.minecraft.client.network.ClientPlayerEntity) (Object) this) instanceof ClientPlayerEntity && sprinting) {
         EventChangeSprint event = new EventChangeSprint(true);
         event.post();
         if (!event.isSprinting()) {
            ci.cancel();
         }
      }
   }

   @Inject(
      method = {"getHandSwingDuration"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onGetHandSwingDuration(CallbackInfoReturnable<Integer> cir) {
      SwingAnimations swing = Instance.get(SwingAnimations.class);
      if (swing != null && swing.isEnabled()) {
         int speed = (int)swing.speed.getValue();
         cir.setReturnValue(25 - speed * 2);
      }
   }
}
