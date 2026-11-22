package zov.alphadlc.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import zov.alphadlc.event.list.ChatEvent;

@Mixin({Screen.class})
public class ScreenMixin {
   @Inject(
      at = {@At(
         value = "INVOKE",
         target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;)V",
         remap = false,
         ordinal = 1
      )},
      method = {"handleTextClick"},
      cancellable = true
   )
   public void handleCustomClickEvent(Style style, CallbackInfoReturnable<Boolean> cir) {
      ClickEvent clickEvent = style.method_10970();
      if (clickEvent != null) {
         new ChatEvent(clickEvent.method_10844()).post();
         cir.setReturnValue(true);
         cir.cancel();
      }
   }
}
