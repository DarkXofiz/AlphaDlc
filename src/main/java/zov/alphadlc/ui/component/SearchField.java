package zov.alphadlc.ui.component;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import zov.alphadlc.util.cursor.CursorManager;
import zov.alphadlc.util.render.helper.HoverUtil;
import zov.alphadlc.util.render.math.Animation;
import zov.alphadlc.util.render.math.Easing;
import zov.alphadlc.util.render.math.Scissor;
import zov.alphadlc.util.render.msdf.Fonts;
import zov.alphadlc.util.render.providers.ColorProvider;
import zov.alphadlc.util.render.renderers.DrawUtil;

public class SearchField {
   private float x;
   private float y;
   private float width;
   private float height;
   public String text = "";
   private boolean focused;
   private final String placeholder;
   private int caret;
   private int selectionAnchor;
   private float scrollX;
   private static final float ICON_BOX_W = 17.0F;
   private static final float RADIUS = 4.0F;
   private static final float FONT_SIZE = 7.0F;
   private static final int MAX_LENGTH = 64;
   private final Animation focusAnim = new Animation(Easing.QUINTIC_OUT, 220L);
   private final Animation appearAnim = new Animation(Easing.QUINTIC_OUT, 340L);

   public SearchField(String placeholder) {
      this.placeholder = placeholder;
   }

   public void resetAppear() {
      this.appearAnim.reset(0.0F);
   }

   public void setBounds(float x, float y, float width, float height) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
   }

   private float textAreaX() {
      return this.x + 17.0F + 3.0F;
   }

   private float textAreaW() {
      return this.width - 17.0F - 6.0F;
   }

   private float widthOf(String s) {
      return Fonts.SFREGULAR.get().getWidth(s, 7.0F);
   }

   private boolean hasSelection() {
      return this.caret != this.selectionAnchor;
   }

   private int selMin() {
      return Math.min(this.caret, this.selectionAnchor);
   }

   private int selMax() {
      return Math.max(this.caret, this.selectionAnchor);
   }

   public void render(DrawContext context, int mouseX, int mouseY, float partialTicks) {
      boolean hovered = HoverUtil.isHovered((double)mouseX, (double)mouseY, (double)this.x, (double)this.y, (double)this.width, (double)this.height);
      if (hovered) {
         CursorManager.requestIBeam();
      }

      this.focusAnim.run(this.focused);
      float fa = this.focusAnim.getValue();
      this.appearAnim.run(true);
      float ap = this.appearAnim.getValue();
      float ay = this.y + (1.0F - ap) * 9.0F;
      DrawUtil.drawRoundBlur(this.x, ay, this.width, this.height, 4.0F, ColorProvider.rgba(200, 200, 200, (float)((int)(255.0F * ap))), 14.0F);
      int borderColor = ColorProvider.interpolateColor(
         ColorProvider.rgba(48, 66, 122, (float)((int)(70.0F * ap))), ColorProvider.setAlpha(ColorProvider.getColorClient(), (int)(150.0F * ap)), fa
      );
      DrawUtil.drawRound(this.x - 0.5F, ay - 0.5F, this.width + 1.0F, this.height + 1.0F, 4.5F, borderColor);
      DrawUtil.drawRound(this.x, ay, this.width, this.height, 4.0F, ColorProvider.setAlpha(ColorProvider.getColorClickGui(), (int)(130.0F * ap)));
      float iconW = Fonts.ICONS_MINCED.get().getWidth("l", 10.0F);
      float iconX = this.x + (17.0F - iconW) / 2.0F + 1.0F;
      float iconY = ay + this.height / 2.0F - 4.5F;
      int iconColor = ColorProvider.interpolateColor(
         ColorProvider.setAlpha(ColorProvider.getColorIcons(), (int)(160.0F * ap)),
         ColorProvider.setAlpha(ColorProvider.getColorIcons(), (int)(255.0F * ap)),
         fa
      );
      DrawUtil.drawText(Fonts.ICONS_MINCED.get(), "l", iconX, iconY, iconColor, 10.0F);
      float taX = this.textAreaX();
      float taW = this.textAreaW();
      float textY = ay + this.height / 2.0F - 3.5F + 0.2F;
      this.updateScroll(taW);
      Scissor.push();
      Scissor.setFromComponentCoordinates((double)taX, (double)ay, (double)taW, (double)this.height);
      if (this.text.isEmpty() && !this.focused) {
         DrawUtil.drawText(
            Fonts.SFREGULAR.get(), this.placeholder, taX, textY, ColorProvider.setAlpha(ColorProvider.getColorInactiveText(), (int)(255.0F * ap)), 7.0F
         );
      } else {
         if (this.hasSelection()) {
            float selX1 = taX - this.scrollX + this.widthOf(this.text.substring(0, this.selMin()));
            float selX2 = taX - this.scrollX + this.widthOf(this.text.substring(0, this.selMax()));
            DrawUtil.drawRound(
               selX1, ay + 3.0F, selX2 - selX1, this.height - 6.0F, 1.5F, ColorProvider.setAlpha(ColorProvider.getColorClient(), (int)(90.0F * ap))
            );
         }

         DrawUtil.drawText(
            Fonts.SFREGULAR.get(), this.text, taX - this.scrollX, textY, ColorProvider.setAlpha(ColorProvider.getColorText(), (int)(255.0F * ap)), 7.0F
         );
         if (this.focused && System.currentTimeMillis() % 1000L > 500L) {
            float caretX = taX - this.scrollX + this.widthOf(this.text.substring(0, this.caret));
            DrawUtil.drawRound(caretX, ay + 3.0F, 0.8F, this.height - 6.0F, 0.0F, ColorProvider.setAlpha(ColorProvider.getColorText(), (int)(230.0F * ap)));
         }
      }

      Scissor.unset();
      Scissor.pop();
   }

   private void updateScroll(float areaW) {
      float caretPos = this.widthOf(this.text.substring(0, this.caret));
      if (caretPos - this.scrollX > areaW - 2.0F) {
         this.scrollX = caretPos - areaW + 2.0F;
      }

      if (caretPos - this.scrollX < 0.0F) {
         this.scrollX = caretPos;
      }

      float total = this.widthOf(this.text);
      if (total - this.scrollX < areaW - 2.0F) {
         this.scrollX = Math.max(0.0F, total - areaW + 2.0F);
      }

      if (total <= areaW) {
         this.scrollX = 0.0F;
      }
   }

   public void charTyped(char codePoint, int modifiers) {
      if (this.focused) {
         if ((modifiers & 14) == 0) {
            if (codePoint >= ' ' && codePoint != 127) {
               this.insert(String.valueOf(codePoint));
            }
         }
      }
   }

   public void keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.focused) {
         boolean ctrl = (modifiers & 2) != 0 || (modifiers & 8) != 0;
         boolean shift = (modifiers & 1) != 0;
         if (ctrl) {
            switch (keyCode) {
               case 65:
                  this.selectAll();
                  return;
               case 67:
                  this.copySelection();
                  return;
               case 86:
                  this.paste();
                  return;
               case 88:
                  this.cutSelection();
                  return;
            }
         }

         switch (keyCode) {
            case 256:
            case 257:
            case 335:
               this.focused = false;
               break;
            case 259:
               if (this.hasSelection()) {
                  this.deleteSelection();
               } else if (ctrl) {
                  int start = this.prevWordBoundary(this.caret);
                  this.text = this.text.substring(0, start) + this.text.substring(this.caret);
                  this.caret = start;
                  this.selectionAnchor = this.caret;
               } else if (this.caret > 0) {
                  this.text = this.text.substring(0, this.caret - 1) + this.text.substring(this.caret);
                  this.caret--;
                  this.selectionAnchor = this.caret;
               }
               break;
            case 261:
               if (this.hasSelection()) {
                  this.deleteSelection();
               } else if (this.caret < this.text.length()) {
                  this.text = this.text.substring(0, this.caret) + this.text.substring(this.caret + 1);
               }
               break;
            case 262: {
               int target = ctrl ? this.nextWordBoundary(this.caret) : Math.min(this.text.length(), this.caret + 1);
               this.moveCaret(target, shift);
               break;
            }
            case 263: {
               int target = ctrl ? this.prevWordBoundary(this.caret) : Math.max(0, this.caret - 1);
               this.moveCaret(target, shift);
               break;
            }
            case 268:
               this.moveCaret(0, shift);
               break;
            case 269:
               this.moveCaret(this.text.length(), shift);
         }
      }
   }

   private void insert(String s) {
      if (this.hasSelection()) {
         this.deleteSelection();
      }

      int free = 64 - this.text.length();
      if (free > 0) {
         if (s.length() > free) {
            s = s.substring(0, free);
         }

         this.text = this.text.substring(0, this.caret) + s + this.text.substring(this.caret);
         this.caret = this.caret + s.length();
         this.selectionAnchor = this.caret;
      }
   }

   private void deleteSelection() {
      int a = this.selMin();
      int b = this.selMax();
      this.text = this.text.substring(0, a) + this.text.substring(b);
      this.caret = a;
      this.selectionAnchor = a;
   }

   private void moveCaret(int target, boolean keepSelection) {
      this.caret = Math.max(0, Math.min(this.text.length(), target));
      if (!keepSelection) {
         this.selectionAnchor = this.caret;
      }
   }

   private void selectAll() {
      this.selectionAnchor = 0;
      this.caret = this.text.length();
   }

   private void copySelection() {
      if (this.hasSelection()) {
         MinecraftClient.method_1551().field_1774.method_1455(this.text.substring(this.selMin(), this.selMax()));
      }
   }

   private void cutSelection() {
      if (this.hasSelection()) {
         this.copySelection();
         this.deleteSelection();
      }
   }

   private void paste() {
      String clip = MinecraftClient.method_1551().field_1774.method_1460();
      if (clip != null && !clip.isEmpty()) {
         clip = clip.replaceAll("[\\n\\r\\t]", " ");
         this.insert(clip);
      }
   }

   private int prevWordBoundary(int from) {
      int i = from;

      while (i > 0 && this.text.charAt(i - 1) == ' ') {
         i--;
      }

      while (i > 0 && this.text.charAt(i - 1) != ' ') {
         i--;
      }

      return i;
   }

   private int nextWordBoundary(int from) {
      int i = from;
      int len = this.text.length();

      while (i < len && this.text.charAt(i) == ' ') {
         i++;
      }

      while (i < len && this.text.charAt(i) != ' ') {
         i++;
      }

      return i;
   }

   public void mouseClicked(double mouseX, double mouseY, int button) {
      boolean inside = HoverUtil.isHovered(mouseX, mouseY, (double)this.x, (double)this.y, (double)this.width, (double)this.height);
      this.focused = inside;
      if (inside) {
         int index = this.caretFromMouse((float)mouseX);
         this.caret = index;
         this.selectionAnchor = index;
      }
   }

   private int caretFromMouse(float mouseX) {
      float local = mouseX - this.textAreaX() + this.scrollX;
      int best = 0;
      float bestDist = Float.MAX_VALUE;

      for (int i = 0; i <= this.text.length(); i++) {
         float w = this.widthOf(this.text.substring(0, i));
         float d = Math.abs(w - local);
         if (d < bestDist) {
            bestDist = d;
            best = i;
         }
      }

      return best;
   }

   public boolean isFocused() {
      return this.focused;
   }

   public boolean isEmpty() {
      return this.text.isEmpty();
   }
}
