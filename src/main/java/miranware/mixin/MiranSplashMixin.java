package miranware.mixin;

import java.util.Optional;
import java.util.function.Consumer;
import miranware.ui.MiranSplashAssets;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.resource.ResourceReload;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Replaces the Mojang loading screen with the MiranWare background, logo and progress bar. */
@Mixin(SplashOverlay.class)
public abstract class MiranSplashMixin {
    private static final int BAR_COLOR = 0x8A2BE2;

    @Final
    @Shadow
    private boolean reloading;
    @Shadow
    private float progress;
    @Shadow
    private long reloadCompleteTime;
    @Shadow
    private long reloadStartTime;
    @Final
    @Shadow
    private ResourceReload reload;
    @Final
    @Shadow
    private Consumer<Optional<Throwable>> exceptionHandler;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void miranware$renderSplash(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ci.cancel();
        MinecraftClient client = MinecraftClient.getInstance();
        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();
        long now = Util.getMeasuringTimeMs();
        if (this.reloading && this.reloadStartTime == -1L) {
            this.reloadStartTime = now;
        }

        float fadeOut = this.reloadCompleteTime > -1L ? (float) (now - this.reloadCompleteTime) / 600.0F : -1.0F;
        float fadeIn = this.reloadStartTime > -1L ? (float) (now - this.reloadStartTime) / 500.0F : -1.0F;
        float backgroundAlpha;
        float logoAlpha;
        if (fadeOut >= 0.0F) {
            if (client.currentScreen != null) {
                client.currentScreen.render(context, 0, 0, delta);
            }
            float t = 1.0F - MathHelper.clamp(fadeOut, 0.0F, 1.0F);
            backgroundAlpha = t * t * (3.0F - 2.0F * t);
            logoAlpha = backgroundAlpha;
        } else if (this.reloading) {
            if (client.currentScreen != null && fadeIn < 1.0F) {
                client.currentScreen.render(context, mouseX, mouseY, delta);
            }
            backgroundAlpha = MathHelper.clamp(fadeIn, 0.15F, 1.0F);
            logoAlpha = MathHelper.clamp(fadeIn, 0.0F, 1.0F);
        } else {
            backgroundAlpha = 1.0F;
            logoAlpha = 1.0F;
        }

        if (!MiranSplashAssets.drawBackground(context, width, height, backgroundAlpha)) {
            int a = Math.max(0, Math.min(255, Math.round(backgroundAlpha * 255.0F)));
            context.fill(0, 0, width, height, (a << 24) | 0x050509);
        }

        int[] logo = MiranSplashAssets.drawLogo(context, width, height, logoAlpha);
        int logoX = logo[0];
        int logoW = logo[1];
        int logoH = logo[2];
        int logoY = (height - logoH) / 2;
        int barW = (int) (logoW * 0.6F);
        if (barW > 0) {
            int barH = 4;
            int barX = (width - barW) / 2;
            int barY = logoY + logoH + 14;
            int fill = Math.max(barH, (int) (barW * MathHelper.clamp(this.progress, 0.0F, 1.0F)));
            int trackAlpha = Math.round(35.0F * logoAlpha);
            int barAlpha = Math.round(255.0F * logoAlpha);
            context.fill(barX, barY, barX + barW, barY + barH, (trackAlpha << 24) | 0xFFFFFF);
            context.fill(barX, barY, barX + fill, barY + barH, (barAlpha << 24) | BAR_COLOR);
        }

        float actual = this.reload.getProgress();
        this.progress = MathHelper.clamp(this.progress * 0.95F + actual * 0.05F, 0.0F, 1.0F);
        if (fadeOut >= 1.0F) {
            client.setOverlay(null);
        }

        if (this.reloadCompleteTime == -1L && this.reload.isComplete() && (!this.reloading || fadeIn >= 1.0F)) {
            try {
                this.reload.throwException();
                this.exceptionHandler.accept(Optional.empty());
            } catch (Throwable t) {
                this.exceptionHandler.accept(Optional.of(t));
            }

            this.reloadCompleteTime = Util.getMeasuringTimeMs();
            if (client.currentScreen != null) {
                client.currentScreen.init(client, width, height);
            }
        }
    }
}
