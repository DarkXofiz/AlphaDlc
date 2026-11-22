package zov.alphadlc.mixin;

import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.util.PlayerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zov.alphadlc.event.list.MoveInputEvent;

@Mixin({KeyboardInput.class})
public abstract class KeyboardInputMixin extends Input {
   @Inject(
      method = {"tick"},
      at = {@At("TAIL")}
   )
   private void onTick(CallbackInfo ci) {
      float forward = this.field_3905;
      float strafe = this.field_3907;
      boolean jump = ((KeyboardInput)this).field_54155.comp_3163();
      boolean sneak = ((KeyboardInput)this).field_54155.comp_3164();
      MoveInputEvent event = new MoveInputEvent(forward, strafe, jump, sneak, 0.3);
      event.post();
      if (event.isCancelled()) {
         this.field_3905 = 0.0F;
         this.field_3907 = 0.0F;
      } else {
         this.field_3905 = event.getForward();
         this.field_3907 = event.getStrafe();
         ((KeyboardInput)this).field_54155 = new PlayerInput(
            event.getForward() > 0.0F,
            event.getForward() < 0.0F,
            event.getStrafe() > 0.0F,
            event.getStrafe() < 0.0F,
            event.isJump(),
            event.isSneaking(),
            ((KeyboardInput)this).field_54155.comp_3165()
         );
      }
   }
}
