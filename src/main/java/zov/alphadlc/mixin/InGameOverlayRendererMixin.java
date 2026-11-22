package zov.alphadlc.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zov.alphadlc.module.list.render.NoRender;
import zov.alphadlc.util.base.Instance;

@Mixin({InGameOverlayRenderer.class})
public class InGameOverlayRendererMixin {
   @Inject(
      method = {"renderFireOverlay"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void renderFireOverlay(MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
      if (Instance.get(NoRender.class).isEnabled() && Instance.get(NoRender.class).elements.isEnabled("\u041e\u0433\u043e\u043d\u044c")) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"renderUnderwaterOverlay"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void renderUnderwaterOverlay(MinecraftClient client, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
      if (Instance.get(NoRender.class).isEnabled()
         && Instance.get(NoRender.class).elements.isEnabled("\u0420\u0430\u0437\u043c\u044b\u0442\u0438\u0435 \u0432 \u0432\u043e\u0434\u0435")) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"renderInWallOverlay"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void renderInWallOverlay(Sprite sprite, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
      if (Instance.get(NoRender.class).isEnabled()
         && Instance.get(NoRender.class).elements.isEnabled("\u0417\u0440\u0435\u043d\u0438\u0435 \u0432 \u0431\u043b\u043e\u043a\u0430\u0445")) {
         ci.cancel();
      }
   }
}
