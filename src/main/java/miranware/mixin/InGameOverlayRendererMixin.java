package miranware.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import miranware.module.list.render.NoRender;
import miranware.util.base.Instance;

@Mixin({InGameOverlayRenderer.class})
public class InGameOverlayRendererMixin {
   @Inject(
      method = {"renderFireOverlay"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void renderFireOverlay(MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
      if (Instance.get(NoRender.class).isEnabled() && Instance.get(NoRender.class).elements.isEnabled("Fire")) {
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
         && Instance.get(NoRender.class).elements.isEnabled("Underwater Blur")) {
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
         && Instance.get(NoRender.class).elements.isEnabled("Inside Block Vision")) {
         ci.cancel();
      }
   }
}
