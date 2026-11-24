package miranware.mixin;

import miranware.ui.MiranSplashAssets;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Draws the MiranWare background on every menu screen shown outside a world. */
@Mixin(Screen.class)
public abstract class MiranScreenBackgroundMixin {
    @Inject(method = "renderPanoramaBackground", at = @At("HEAD"), cancellable = true)
    private void miranware$background(DrawContext context, float delta, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null) {
            return;
        }
        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();
        if (MiranSplashAssets.drawBackground(context, width, height, 1.0F)) {
            ci.cancel();
        }
    }
}
