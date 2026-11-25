package miranware.ui;

import java.io.InputStream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

/**
 * Loads the MiranWare background and logo straight from the mod jar (classpath) and registers
 * them as dynamic textures. Resource ids are not available while the Mojang splash screen is
 * still reloading resources, so the normal resource lookup cannot be used there.
 * Accessors return null on failure; callers must have a fallback.
 */
public final class MiranSplashAssets {
    public static final class Tex {
        public final Identifier id;
        public final int w;
        public final int h;

        Tex(Identifier id, int w, int h) {
            this.id = id;
            this.w = w;
            this.h = h;
        }
    }

    private static final String BASE = "/assets/mre/textures/gui/";
    private static Tex background;
    private static Tex logo;
    private static boolean backgroundTried;
    private static boolean logoTried;

    private MiranSplashAssets() {
    }

    public static Tex background() {
        if (!backgroundTried) {
            backgroundTried = true;
            background = load("miran_bg.png", "dynamic/miran_bg");
        }
        return background;
    }

    public static Tex logo() {
        if (!logoTried) {
            logoTried = true;
            logo = load("miran_logo.png", "dynamic/miran_logo");
        }
        return logo;
    }

    private static Tex load(String file, String idPath) {
        try (InputStream in = MiranSplashAssets.class.getResourceAsStream(BASE + file)) {
            if (in == null) {
                return null;
            }
            NativeImage image = NativeImage.read(in);
            int w = image.getWidth();
            int h = image.getHeight();
            NativeImageBackedTexture texture = new NativeImageBackedTexture(image);
            texture.setFilter(true, false);
            Identifier id = Identifier.of("mre", idPath);
            MinecraftClient.getInstance().getTextureManager().registerTexture(id, texture);
            return new Tex(id, w, h);
        } catch (Throwable t) {
            return null;
        }
    }

    private static int white(float alpha) {
        int a = Math.max(0, Math.min(255, (int) (alpha * 255.0F)));
        return (a << 24) | 0xFFFFFF;
    }

    /** Draws the background scaled to cover the whole area, cropping the overflow evenly. Returns false if unavailable. */
    public static boolean drawBackground(DrawContext context, int width, int height, float alpha) {
        Tex tex = background();
        if (tex == null || alpha <= 0.0F) {
            return tex != null;
        }
        float scale = Math.max(width / (float) tex.w, height / (float) tex.h);
        int regionW = Math.min(tex.w, (int) Math.ceil(width / scale));
        int regionH = Math.min(tex.h, (int) Math.ceil(height / scale));
        float u = (tex.w - regionW) / 2.0F;
        float v = (tex.h - regionH) / 2.0F;
        context.drawTexture(RenderLayer::getGuiTextured, tex.id, 0, 0, u, v, width, height, regionW, regionH, tex.w, tex.h, white(alpha));
        return true;
    }

    /** Draws the logo centred horizontally at the given y. Returns the drawn size as {x, width, height}. */
    public static int[] drawLogo(DrawContext context, int guiWidth, int guiHeight, float alpha) {
        Tex tex = logo();
        if (tex == null) {
            return new int[] {guiWidth / 2, 0, 0};
        }
        int logoW = Math.min((int) (guiWidth * 0.5F), 420);
        int logoH = Math.max(1, Math.round(logoW * (float) tex.h / (float) tex.w));
        int logoX = (guiWidth - logoW) / 2;
        int logoY = (guiHeight - logoH) / 2;
        if (alpha > 0.0F) {
            context.drawTexture(RenderLayer::getGuiTextured, tex.id, logoX, logoY, 0.0F, 0.0F, logoW, logoH, tex.w, tex.h, tex.w, tex.h, white(alpha));
        }
        return new int[] {logoX, logoW, logoH};
    }
}
