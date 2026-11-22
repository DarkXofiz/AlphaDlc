package zov.alphadlc.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zov.alphadlc.event.list.EventWorldRender;
import zov.alphadlc.module.list.render.Ambience;
import zov.alphadlc.module.list.render.NoRender;
import zov.alphadlc.util.base.Instance;
import zov.alphadlc.util.render.ambience.AmbiencePostProcessor;
import zov.alphadlc.util.render.renderers.DrawUtil;

@Mixin({GameRenderer.class})
public class GameRendererMixin {
   @Inject(
      method = {"renderWorld"},
      at = {@At(
         value = "FIELD",
         target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z",
         opcode = 180,
         ordinal = 0
      )}
   )
   public void hookWorldRender(RenderTickCounter tickCounter, CallbackInfo ci, @Local(ordinal = 2) Matrix4f matrix4f) {
      MatrixStack matrixStack = new MatrixStack();
      matrixStack.multiplyPositionMatrix(matrix4f);
      EventWorldRender event = new EventWorldRender(matrixStack, tickCounter.getTickDelta(false));
      event.post();
      DrawUtil.onRender3D(event.getMatrixStack());
      AmbiencePostProcessor.apply(Instance.get(Ambience.class));
   }

   @Inject(
      method = {"showFloatingItem"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void hideTotemAnimation(ItemStack floatingItem, CallbackInfo ci) {
      NoRender removals = Instance.get(NoRender.class);
      if (removals != null && removals.isEnabled() && removals.elements.isEnabled("\u0422\u043e\u0442\u0435\u043c")) {
         ci.cancel();
      }
   }
}
