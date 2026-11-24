package miranware.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * MiranWare main menu: full screen background, logo with a loading bar intro,
 * then the logo slides up and the menu buttons fade in.
 */
public class MiranMenuScreen extends Screen {
    private static final Identifier BACKGROUND = Identifier.of("mre", "textures/gui/miran_bg.png");
    private static final Identifier LOGO = Identifier.of("mre", "textures/gui/miran_logo.png");
    private static final int BG_W = 1672;
    private static final int BG_H = 941;
    private static final int LOGO_W = 1024;
    private static final int LOGO_H = 172;

    private static final long INTRO_MS = 2200L;
    private static final long SLIDE_MS = 700L;
    private static final long FADE_MS = 500L;

    // The Mojang splash screen already shows the logo and progress bar, so the menu skips its own intro.
    private static boolean introPlayed = true;

    private static final int BAR_COLOR = 0x8A2BE2;
    private static final int BUTTON_COLOR = 0x101018;
    private static final int BUTTON_HOVER_COLOR = 0x7B2FF7;

    private final List<MenuButton> buttons = new ArrayList<>();
    private long startTime;

    public MiranMenuScreen() {
        super(Text.literal("MiranWare"));
        this.startTime = Util.getMeasuringTimeMs();
        if (introPlayed) {
            this.startTime -= INTRO_MS + SLIDE_MS + FADE_MS + 100L;
        }
    }

    @Override
    protected void init() {
        this.buttons.clear();
        MinecraftClient mc = MinecraftClient.getInstance();
        this.buttons.add(new MenuButton("SINGLEPLAYER", () -> mc.setScreen(new SelectWorldScreen(this))));
        this.buttons.add(new MenuButton("MULTIPLAYER", () -> mc.setScreen(new MultiplayerScreen(this))));
        this.buttons.add(new MenuButton("SETTINGS", () -> mc.setScreen(new OptionsScreen(this, mc.options))));
        this.buttons.add(new MenuButton("CLICKGUI", () -> mc.setScreen(new ClickGuiFrame())));
        this.buttons.add(new MenuButton("QUIT GAME", mc::scheduleStop));
    }

    private static float ease(float t) {
        float c = Math.max(0.0F, Math.min(1.0F, t));
        return 1.0F - (1.0F - c) * (1.0F - c) * (1.0F - c);
    }

    private static int argb(float alpha, int rgb) {
        int a = Math.max(0, Math.min(255, (int) (alpha * 255.0F)));
        return (a << 24) | (rgb & 0xFFFFFF);
    }

    private float introProgress() {
        return Math.min(1.0F, (Util.getMeasuringTimeMs() - this.startTime) / (float) INTRO_MS);
    }

    private float slideProgress() {
        long t = Util.getMeasuringTimeMs() - this.startTime - INTRO_MS;
        return ease(t / (float) SLIDE_MS);
    }

    private float fadeProgress() {
        long t = Util.getMeasuringTimeMs() - this.startTime - INTRO_MS - SLIDE_MS;
        return ease(t / (float) FADE_MS);
    }

    private int buttonWidth() {
        return 150;
    }

    private int buttonHeight() {
        return 20;
    }

    private int logoWidth() {
        return Math.min(520, (int) (this.width * 0.46F));
    }

    private int logoHeight() {
        return logoWidth() * LOGO_H / LOGO_W;
    }

    private int logoY(float slide) {
        int centerY = this.height / 2 - logoHeight() / 2;
        int topY = (int) (this.height * 0.16F);
        return (int) (centerY + (topY - centerY) * slide);
    }

    /** Full screen cover-scaled background. Shared with ClickGuiFrame so it can repaint the menu bg each frame. */
    static void drawBackground(DrawContext context, int width, int height) {
        float scale = Math.max(width / (float) BG_W, height / (float) BG_H);
        int drawW = (int) Math.ceil(BG_W * scale);
        int drawH = (int) Math.ceil(BG_H * scale);
        int bgX = (width - drawW) / 2;
        int bgY = (height - drawH) / 2;
        context.drawTexture(RenderLayer::getGuiTextured, BACKGROUND, bgX, bgY, 0.0F, 0.0F, drawW, drawH, BG_W, BG_H, BG_W, BG_H);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        drawBackground(context, this.width, this.height);

        float intro = introProgress();
        float slide = slideProgress();
        float fade = fadeProgress();
        if (intro >= 1.0F) {
            introPlayed = true;
        }

        // slight darkening once the menu is shown
        context.fill(0, 0, this.width, this.height, argb(0.45F * Math.max(slide, 0.0F), 0x000000));

        // logo
        int lw = logoWidth();
        int lh = logoHeight();
        int lx = (this.width - lw) / 2;
        int ly = logoY(slide);
        context.drawTexture(RenderLayer::getGuiTextured, LOGO, lx, ly, 0.0F, 0.0F, lw, lh, LOGO_W, LOGO_H, LOGO_W, LOGO_H);

        // loading bar (intro only)
        if (intro < 1.0F || slide < 1.0F) {
            int barW = (int) (lw * 0.9F);
            int barX = (this.width - barW) / 2;
            int barY = ly + lh + 12;
            float barAlpha = 1.0F - Math.min(1.0F, slide * 2.0F);
            context.fill(barX, barY, barX + (int) (barW * intro), barY + 3, argb(barAlpha, BAR_COLOR));
        }

        // buttons
        if (fade > 0.0F) {
            int bw = buttonWidth();
            int bh = buttonHeight();
            int bx = (this.width - bw) / 2;
            int by = ly + lh + 28;
            for (int i = 0; i < this.buttons.size(); i++) {
                MenuButton b = this.buttons.get(i);
                if (i == this.buttons.size() - 1) {
                    by += 10;
                }
                b.x = bx;
                b.y = by;
                b.w = bw;
                b.h = bh;
                boolean hovered = b.contains(mouseX, mouseY);
                int base = hovered ? BUTTON_HOVER_COLOR : BUTTON_COLOR;
                context.fill(bx - 1, by - 1, bx + bw + 1, by + bh + 1, argb(0.35F * fade, 0xFFFFFF));
                context.fill(bx, by, bx + bw, by + bh, argb((hovered ? 0.95F : 0.78F) * fade, base));
                if (fade > 0.1F) {
                    context.drawCenteredTextWithShadow(this.textRenderer, b.label, this.width / 2, by + (bh - 8) / 2, argb(fade, 0xFFFFFF));
                }
                by += bh + 5;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && fadeProgress() > 0.6F) {
            for (MenuButton b : this.buttons) {
                if (b.contains(mouseX, mouseY)) {
                    b.action.run();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    private static final class MenuButton {
        final String label;
        final Runnable action;
        int x;
        int y;
        int w;
        int h;

        MenuButton(String label, Runnable action) {
            this.label = label;
            this.action = action;
        }

        boolean contains(double mx, double my) {
            return mx >= this.x && mx <= this.x + this.w && my >= this.y && my <= this.y + this.h;
        }
    }
}
