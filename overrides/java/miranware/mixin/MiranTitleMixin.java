package miranware.mixin;

import miranware.ui.MiranMenuScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Replaces the vanilla title screen with the MiranWare main menu. */
@Mixin(MinecraftClient.class)
public abstract class MiranTitleMixin {
    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen miranware$replaceTitleScreen(Screen screen) {
        if (screen != null && screen.getClass() == TitleScreen.class) {
            return new MiranMenuScreen();
        }
        return screen;
    }
}
