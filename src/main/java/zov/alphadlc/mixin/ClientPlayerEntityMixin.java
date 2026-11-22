package zov.alphadlc.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zov.alphadlc.event.list.EventCloseInv;
import zov.alphadlc.event.list.EventNoSlow;
import zov.alphadlc.event.list.EventPlayerSync;
import zov.alphadlc.event.list.EventPlayerSyncEnd;
import zov.alphadlc.event.list.EventPlayerUpdate;
import zov.alphadlc.event.list.EventSprintSync;
import zov.alphadlc.module.list.player.FreeCamera;
import zov.alphadlc.module.list.player.NoPush;
import zov.alphadlc.util.base.Instance;

@Mixin({ClientPlayerEntity.class})
public abstract class ClientPlayerEntityMixin {
   @Final
   @Shadow
   protected MinecraftClient field_3937;
   @Final
   @Shadow
   public ClientPlayNetworkHandler field_3944;

   @Shadow
   public abstract void method_3137();

   @Inject(
      method = {"tick"},
      at = {@At("HEAD")}
   )
   private void tick(CallbackInfo ci) {
      new EventPlayerUpdate().post();
   }

   @Inject(
      method = {"sendMovementPackets"},
      at = {@At("HEAD")}
   )
   private void sendMovementPackets(CallbackInfo ci) {
      new EventPlayerSync().post();
   }

   @Inject(
      method = {"sendMovementPackets"},
      at = {@At("RETURN")}
   )
   private void sendMovementPacketsEnd(CallbackInfo ci) {
      new EventPlayerSyncEnd().post();
   }

   @Inject(
      method = {"sendMovementPackets"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/network/ClientPlayerEntity;sendSprintingPacket()V",
         shift = Shift.AFTER,
         ordinal = 0
      )}
   )
   private void onSprint(CallbackInfo ci) {
      new EventSprintSync().post();
   }

   @Redirect(
      method = {"tickMovement"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"
      ),
      require = 0
   )
   private boolean onIsUsingItemRedirect(ClientPlayerEntity player) {
      if (player.method_6115()) {
         EventNoSlow event = new EventNoSlow();
         event.post();
         return player.method_6115() && player.method_5854() == null && !event.isCancelled();
      } else {
         return player.method_6115() && player.method_5854() == null;
      }
   }

   @Inject(
      method = {"pushOutOfBlocks"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void pushOutOfBlocks(double x, double z, CallbackInfo ci) {
      if (Instance.get(FreeCamera.class).isEnabled()
         || Instance.get(NoPush.class).isEnabled() && Instance.get(NoPush.class).objects.isEnabled("\u0411\u043b\u043e\u043a\u0438")) {
         ci.cancel();
      }
   }

   @Overwrite
   public void method_7346() {
      EventCloseInv event = new EventCloseInv();
      event.post();
      if (!event.isCancelled()) {
         ScreenHandler screenHandler = ((PlayerEntity)this).field_7512;
         this.field_3944.method_52787(new CloseHandledScreenC2SPacket(screenHandler.field_7763));
      }

      this.method_3137();
   }
}
