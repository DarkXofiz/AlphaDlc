package zov.alphadlc.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zov.alphadlc.AlphaDLC;
import zov.alphadlc.event.list.EventHandledScreen;

@Mixin({HandledScreen.class})
public abstract class HandledScreenMixin {
   @Shadow
   @Nullable
   protected Slot field_2787;

   @Inject(
      method = {"render"},
      at = {@At("RETURN")}
   )
   private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      AlphaDLC.getInstance().getEventBus().post(new EventHandledScreen(this.field_2787, context, mouseX, mouseY));
   }
}
