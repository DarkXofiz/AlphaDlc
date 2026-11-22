package zov.alphadlc.ui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import net.minecraft.client.gui.DrawContext;
import org.joml.Vector4f;
import zov.alphadlc.module.settings.impl.Theme;
import zov.alphadlc.module.settings.impl.ThemeManager;
import zov.alphadlc.util.IMinecraft;
import zov.alphadlc.util.cursor.CursorManager;
import zov.alphadlc.util.render.helper.HoverUtil;
import zov.alphadlc.util.render.math.Animation;
import zov.alphadlc.util.render.math.Easing;
import zov.alphadlc.util.render.math.Scissor;
import zov.alphadlc.util.render.msdf.Fonts;
import zov.alphadlc.util.render.providers.ColorProvider;
import zov.alphadlc.util.render.renderers.DrawUtil;

public class ThemeEditor implements IMinecraft {
   private static final String[] SLOTS = new String[]{
      "\u0422\u0435\u043c\u0430 \u043a\u043b\u0438\u0435\u043d\u0442\u0430",
      "\u0426\u0432\u0435\u0442 \u0438\u043a\u043e\u043d\u043e\u043a",
      "\u041d\u0435\u0430\u043a\u0442\u0438\u0432\u043d\u044b\u0439 \u0442\u0435\u043a\u0441\u0442",
      "\u0410\u043a\u0442\u0438\u0432\u043d\u044b\u0439 \u0442\u0435\u043a\u0441\u0442",
      "\u0424\u043e\u043d Interface",
      "\u0424\u043e\u043d ClickGui"
   };
   private static final float W = 124.0F;
   private static final float TAB = 18.0F;
   private static final float ROW_H = 15.0F;
   private static final float PAD = 5.0F;
   private static final float BTN_H = 14.0F;
   private static final float BTN_GAP = 4.0F;
   private static final int[] DEFAULTS = new int[]{-12816651, -12816651, -8485216, -1513240, -16777216, -16777216};
   private static final float SV_SIZE = 58.0F;
   private static final float HUE_W = 7.0F;
   private static final float HUE_GAP = 4.0F;
   private static final float PICKER_PAD = 5.0F;
   private static final float PICKER_W = 69.0F;
   private static final File DIR = new File("alphadlc");
   private static final File FILE = new File(DIR, "theme.json");
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private final Animation expandAnim = new Animation(Easing.QUINTIC_OUT, 380L);
   private final Animation pickerAnim = new Animation(Easing.QUINTIC_OUT, 240L);
   private final Animation appearAnim = new Animation(Easing.QUINTIC_OUT, 340L);
   private boolean expanded;
   private int editingSlot = -1;
   private final float[] hsv = new float[3];
   private boolean draggingSV;
   private boolean draggingH;
   private float pickerX;
   private float pickerY;
   private final int[] colors = new int[SLOTS.length];
   private float x;
   private float y;
   private float height;

   public void resetAppear() {
      this.appearAnim.reset(0.0F);
   }

   public ThemeEditor() {
      Theme t = ThemeManager.getInstance().getCurrentTheme();
      this.colors[0] = t.colorClient;
      this.colors[1] = t.colorIcons;
      this.colors[2] = t.colorInactiveText;
      this.colors[3] = t.colorText;
      this.colors[4] = t.colorInterfaceBg;
      this.colors[5] = t.colorClickGui;
      this.load();
      this.applyColors();
   }

   private void applyColors() {
      Theme t = ThemeManager.getInstance().getCurrentTheme();
      t.setAccent(this.colors[0]);
      t.colorIcons = this.colors[1];
      t.colorInactiveText = this.colors[2];
      t.colorText = this.colors[3];
      t.colorInterfaceBg = this.colors[4];
      t.colorClickGui = this.colors[5];
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      int sw = mc.getWindow().getScaledWidth();
      int sh = mc.getWindow().getScaledHeight();
      this.expandAnim.run(this.expanded);
      float ep = this.expandAnim.getValue();
      float contentH = 10.0F + (float)SLOTS.length * 15.0F + 4.0F + 14.0F;
      this.height = 18.0F + contentH * ep;
      this.x = (float)sw - 124.0F - 4.0F;
      this.y = (float)sh - this.height - 4.0F;
      this.appearAnim.run(true);
      float appear = this.appearAnim.getValue();
      float appearShift = (1.0F - appear) * (this.height + 14.0F);
      this.y += appearShift;
      Vector4f topRound = new Vector4f(8.0F, 8.0F, 0.0F, 0.0F);
      DrawUtil.drawRoundBlur(this.x, this.y, 124.0F, this.height, topRound, ColorProvider.rgba(200, 200, 200, 255.0F), 14.0F);
      DrawUtil.drawRound(this.x - 0.5F, this.y - 0.5F, 125.0F, this.height + 1.0F, new Vector4f(8.5F, 8.5F, 0.0F, 0.0F), ColorProvider.rgba(48, 66, 122, 70.0F));
      DrawUtil.drawRound(this.x, this.y, 124.0F, this.height, topRound, ColorProvider.setAlpha(ColorProvider.getColorClickGui(), 130));
      DrawUtil.drawRound(this.x, this.y, 124.0F, 18.0F, topRound, ColorProvider.setAlpha(ColorProvider.getColorHeaderBg(), 110));
      float titleSize = 8.5F;
      float titleW = Fonts.SFREGULAR.get().getWidth("Theme", titleSize);
      float arrowW = 7.0F;
      float groupGap = 5.0F;
      float groupTotal = titleW + groupGap + arrowW;
      float groupX = this.x + (124.0F - groupTotal) / 2.0F;
      DrawUtil.drawText(Fonts.SFREGULAR.get(), "Theme", groupX, this.y + (18.0F - titleSize) / 2.0F, ColorProvider.rgba(255, 255, 255, 255.0F), titleSize);
      this.drawArrow(groupX + titleW + groupGap + arrowW / 2.0F, this.y + 9.0F, !this.expanded, ColorProvider.setAlpha(ColorProvider.getColorIcons(), 255));
      boolean tabHover = HoverUtil.isHovered((double)mouseX, (double)mouseY, (double)this.x, (double)this.y, 124.0, 18.0);
      if (tabHover) {
         CursorManager.requestHand();
      }

      if (ep > 0.01F) {
         Scissor.push();
         Scissor.setFromComponentCoordinates((double)this.x, (double)(this.y + 18.0F), 124.0, (double)(this.height - 18.0F));
         float rowY = this.y + 18.0F + 5.0F;

         for (int i = 0; i < SLOTS.length; i++) {
            this.renderRow(i, this.x + 4.0F, rowY, 116.0F, mouseX, mouseY, ep);
            rowY += 15.0F;
         }

         float btnX = this.x + 4.0F;
         float btnY = rowY + 4.0F;
         float btnW = 116.0F;
         boolean btnHover = HoverUtil.isHovered((double)mouseX, (double)mouseY, (double)btnX, (double)btnY, (double)btnW, 14.0);
         if (btnHover && ep > 0.9F) {
            CursorManager.requestHand();
         }

         int btnBg = btnHover ? ColorProvider.rgba(60, 78, 140, (float)((int)(110.0F * ep))) : ColorProvider.rgba(48, 66, 122, (float)((int)(70.0F * ep)));
         DrawUtil.drawRound(btnX, btnY, btnW, 14.0F, 3.0F, btnBg);
         String label = "\u0421\u0431\u0440\u043e\u0441\u0438\u0442\u044c";
         float lw = Fonts.SFREGULAR.get().getWidth(label, 7.0F);
         DrawUtil.drawText(
            Fonts.SFREGULAR.get(), label, btnX + (btnW - lw) / 2.0F, btnY + 3.5F, ColorProvider.rgba(230, 234, 245, (float)((int)(255.0F * ep))), 7.0F
         );
         Scissor.unset();
         Scissor.pop();
      }

      if (this.draggingSV) {
         this.hsv[1] = clamp01((double)(((float)mouseX - this.pickerX) / 58.0F));
         this.hsv[2] = 1.0F - clamp01((double)(((float)mouseY - this.pickerY) / 58.0F));
         this.applyHSV();
      } else if (this.draggingH) {
         this.hsv[0] = clamp01((double)(((float)mouseY - this.pickerY) / 58.0F));
         this.applyHSV();
      }

      this.pickerAnim.run(this.editingSlot >= 0 && this.expanded);
      if (this.pickerAnim.getValue() > 0.01F) {
         this.renderPicker(mouseX, mouseY, this.pickerAnim.getValue());
      }
   }

   private void renderRow(int slot, float rx, float ry, float rw, int mouseX, int mouseY, float ep) {
      boolean hov = HoverUtil.isHovered((double)mouseX, (double)mouseY, (double)rx, (double)ry, (double)rw, 15.0);
      boolean active = this.editingSlot == slot;
      if ((hov || active) && ep > 0.9F) {
         CursorManager.requestHand();
      }

      if (hov || active) {
         DrawUtil.drawRound(rx, ry, rw, 13.5F, 3.0F, ColorProvider.rgba(60, 78, 140, (float)((int)(55.0F * ep))));
      }

      int textColor = active ? ColorProvider.getColorText() : ColorProvider.rgba(210, 214, 230, 255.0F);
      DrawUtil.drawText(Fonts.SFREGULAR.get(), SLOTS[slot], rx + 4.0F, ry + 4.0F, ColorProvider.setAlpha(textColor, (int)(255.0F * ep)), 7.0F);
      float sw = 11.0F;
      float sx = rx + rw - sw - 3.0F;
      float sy = ry + (15.0F - sw) / 2.0F - 0.75F;
      DrawUtil.drawRound(sx - 0.75F, sy - 0.75F, sw + 1.5F, sw + 1.5F, 3.0F, ColorProvider.rgba(255, 255, 255, (float)((int)(60.0F * ep))));
      DrawUtil.drawRound(sx, sy, sw, sw, 2.5F, ColorProvider.setAlpha(this.colors[slot], (int)(255.0F * ep)));
   }

   private void renderPicker(int mouseX, int mouseY, float anim) {
      int a = (int)(255.0F * anim);
      float px = this.pickerX;
      float py = this.pickerY;
      DrawUtil.drawRoundBlur(px - 5.0F, py - 5.0F, 79.0F, 68.0F, 5.0F, ColorProvider.rgba(200, 200, 200, (float)((int)(255.0F * anim))), 12.0F);
      DrawUtil.drawRound(px - 5.0F, py - 5.0F, 79.0F, 68.0F, 5.0F, ColorProvider.setAlpha(ColorProvider.getColorClickGui(), (int)(140.0F * anim)));
      int cHue = ColorProvider.setAlpha(Color.HSBtoRGB(this.hsv[0], 1.0F, 1.0F), a);
      int white = ColorProvider.rgba(255, 255, 255, (float)a);
      int clearWhite = ColorProvider.rgba(255, 255, 255, 0.0F);
      int black = ColorProvider.rgba(0, 0, 0, (float)a);
      int clearBlack = ColorProvider.rgba(0, 0, 0, 0.0F);
      DrawUtil.drawRound(px, py, 58.0F, 58.0F, 2.0F, cHue);
      DrawUtil.drawRound(px, py, 58.0F, 58.0F, 2.0F, white, white, clearWhite, clearWhite);
      DrawUtil.drawRound(px, py, 58.0F, 58.0F, 2.0F, clearBlack, black, black, clearBlack);
      float scx = px + this.hsv[1] * 58.0F;
      float scy = py + (1.0F - this.hsv[2]) * 58.0F;
      DrawUtil.drawRound(scx - 2.5F, scy - 2.5F, 5.0F, 5.0F, 2.5F, ColorProvider.rgba(0, 0, 0, (float)((int)(180.0F * anim))));
      DrawUtil.drawRound(scx - 1.75F, scy - 1.75F, 3.5F, 3.5F, 1.75F, white);
      float hueX = px + 58.0F + 4.0F;

      for (float i = 0.0F; i <= 58.0F; i += 0.5F) {
         DrawUtil.drawRound(hueX, py + i, 7.0F, 1.0F, 0.0F, ColorProvider.setAlpha(Color.HSBtoRGB(i / 58.0F, 1.0F, 1.0F), a));
      }

      float hcy = py + this.hsv[0] * 58.0F;
      DrawUtil.drawRound(hueX - 1.5F, hcy - 2.0F, 10.0F, 4.0F, 2.0F, ColorProvider.rgba(0, 0, 0, (float)((int)(180.0F * anim))));
      DrawUtil.drawRound(hueX - 0.5F, hcy - 1.0F, 8.0F, 2.0F, 1.0F, white);
   }

   private void drawArrow(float cx, float cy, boolean up, int color) {
      float w = 7.0F;
      float h = 4.0F;
      int rows = 8;
      float top = cy - h / 2.0F;

      for (int i = 0; i < rows; i++) {
         float frac = (float)i / (float)(rows - 1);
         float rowW = up ? w * frac : w * (1.0F - frac);
         float ry = top + frac * h;
         DrawUtil.drawRound(cx - rowW / 2.0F, ry, rowW, h / (float)rows + 0.5F, 0.0F, color);
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (HoverUtil.isHovered(mouseX, mouseY, (double)this.x, (double)this.y, 124.0, 18.0) && button == 0) {
         this.expanded = !this.expanded;
         if (!this.expanded) {
            this.editingSlot = -1;
         }

         return true;
      } else if (this.expandAnim.getValue() < 0.5F) {
         return HoverUtil.isHovered(mouseX, mouseY, (double)this.x, (double)this.y, 124.0, (double)this.height);
      } else {
         if (this.editingSlot >= 0 && this.pickerAnim.getValue() > 0.5F) {
            if (HoverUtil.isHovered(mouseX, mouseY, (double)this.pickerX, (double)this.pickerY, 58.0, 58.0) && button == 0) {
               this.draggingSV = true;
               this.hsv[1] = clamp01((double)((float)(mouseX - (double)this.pickerX) / 58.0F));
               this.hsv[2] = 1.0F - clamp01((double)((float)(mouseY - (double)this.pickerY) / 58.0F));
               this.applyHSV();
               return true;
            }

            if (HoverUtil.isHovered(mouseX, mouseY, (double)(this.pickerX + 58.0F + 4.0F - 1.0F), (double)this.pickerY, 9.0, 58.0) && button == 0) {
               this.draggingH = true;
               this.hsv[0] = clamp01((double)((float)(mouseY - (double)this.pickerY) / 58.0F));
               this.applyHSV();
               return true;
            }

            boolean insidePicker = HoverUtil.isHovered(mouseX, mouseY, (double)(this.pickerX - 5.0F), (double)(this.pickerY - 5.0F), 79.0, 68.0);
            if (insidePicker) {
               return true;
            }
         }

         float btnY = this.y + 18.0F + 5.0F + (float)SLOTS.length * 15.0F + 4.0F;
         if (HoverUtil.isHovered(mouseX, mouseY, (double)(this.x + 4.0F), (double)btnY, 116.0, 14.0) && button == 0) {
            this.resetToDefault();
            return true;
         } else {
            float rowY = this.y + 18.0F + 5.0F;

            for (int i = 0; i < SLOTS.length; i++) {
               if (HoverUtil.isHovered(mouseX, mouseY, (double)(this.x + 4.0F), (double)rowY, 116.0, 15.0) && button == 0) {
                  this.editingSlot = this.editingSlot == i ? -1 : i;
                  if (this.editingSlot >= 0) {
                     this.loadHSV(i);
                     this.pickerX = this.x - 69.0F - 5.0F - 4.0F;
                     this.pickerY = rowY + 7.5F - 29.0F;
                     float sh = (float)mc.getWindow().getScaledHeight();
                     this.pickerY = Math.max(7.0F, Math.min(this.pickerY, sh - 58.0F - 5.0F - 2.0F));
                  }

                  return true;
               }

               rowY += 15.0F;
            }

            return HoverUtil.isHovered(mouseX, mouseY, (double)this.x, (double)this.y, 124.0, (double)this.height);
         }
      }
   }

   public void mouseReleased(double mouseX, double mouseY, int button) {
      if (this.draggingSV || this.draggingH) {
         this.save();
      }

      this.draggingSV = false;
      this.draggingH = false;
   }

   private void resetToDefault() {
      System.arraycopy(DEFAULTS, 0, this.colors, 0, this.colors.length);
      this.editingSlot = -1;
      this.applyColors();
      this.save();
   }

   public static void applyStartupTheme() {
      int[] c = (int[])DEFAULTS.clone();
      if (FILE.exists()) {
         try {
            JsonObject json = JsonParser.parseString(Files.readString(FILE.toPath())).getAsJsonObject();
            if (json.has("colors")) {
               JsonArray arr = json.getAsJsonArray("colors");

               for (int i = 0; i < Math.min(arr.size(), c.length); i++) {
                  c[i] = arr.get(i).getAsInt();
               }
            }
         } catch (Exception var4) {
         }
      }

      Theme t = ThemeManager.getInstance().getCurrentTheme();
      t.setAccent(c[0]);
      t.colorIcons = c[1];
      t.colorInactiveText = c[2];
      t.colorText = c[3];
      t.colorInterfaceBg = c[4];
      t.colorClickGui = c[5];
   }

   private void applyHSV() {
      if (this.editingSlot >= 0) {
         this.colors[this.editingSlot] = Color.HSBtoRGB(this.hsv[0], this.hsv[1], this.hsv[2]) | 0xFF000000;
         this.applyColors();
      }
   }

   private void loadHSV(int slot) {
      Color c = new Color(this.colors[slot], true);
      Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), this.hsv);
   }

   private static float clamp01(double v) {
      return (float)Math.max(0.0, Math.min(1.0, v));
   }

   public void save() {
      try {
         if (!DIR.exists()) {
            DIR.mkdirs();
         }

         JsonObject json = new JsonObject();
         JsonArray arr = new JsonArray();

         for (int c : this.colors) {
            arr.add(c);
         }

         json.add("colors", arr);
         Files.writeString(FILE.toPath(), GSON.toJson(json));
      } catch (IOException var7) {
         var7.printStackTrace();
      }
   }

   public void load() {
      if (FILE.exists()) {
         try {
            JsonObject json = JsonParser.parseString(Files.readString(FILE.toPath())).getAsJsonObject();
            if (json.has("colors")) {
               JsonArray arr = json.getAsJsonArray("colors");

               for (int i = 0; i < Math.min(arr.size(), this.colors.length); i++) {
                  this.colors[i] = arr.get(i).getAsInt();
               }
            }
         } catch (Exception var4) {
            var4.printStackTrace();
         }
      }
   }
}
