package miranware.ui.component.impl;

import java.awt.Color;
import net.minecraft.client.util.math.MatrixStack;
import miranware.module.settings.ColorSetting;
import miranware.ui.component.Component;
import miranware.util.cursor.CursorManager;
import miranware.util.render.helper.HoverUtil;
import miranware.util.render.math.Animation;
import miranware.util.render.math.Easing;
import miranware.util.render.math.Scissor;
import miranware.util.render.msdf.Fonts;
import miranware.util.render.providers.ColorProvider;
import miranware.util.render.renderers.DrawUtil;

public class ColorPickerComponent extends Component {
   private final ColorSetting setting;
   private boolean opened;
   private final Animation openAnim = new Animation(Easing.QUINTIC_OUT, 300L);
   private boolean draggingSV;
   private boolean draggingHue;
   private float hue;
   private float saturation;
   private float value;

   public ColorPickerComponent(ColorSetting setting) {
      this.setting = setting;
      this.updateHSV();
   }

   private void updateHSV() {
      Color c = new Color(this.setting.getValue(), true);
      float[] hsv = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
      this.hue = hsv[0];
      this.saturation = hsv[1];
      this.value = hsv[2];
   }

   @Override
   public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
      float alpha = Math.min(this.getAlphaAnimSetting().getValue(), 1.0F) * Math.max(Math.min(this.getAlphaAnim().getValue(), 1.0F), 0.0F);
      int alphaInt = (int)(255.0F * alpha);
      this.openAnim.run(this.opened);
      float expandedHeight = 55.0F;
      DrawUtil.drawText(Fonts.SFREGULAR.get(), this.setting.getName(), this.x + 4.5F, this.y + 3.0F, ColorProvider.rgba(255, 255, 255, (float)alphaInt), 7.5F);
      float previewSize = 8.0F;
      float previewX = this.x + this.width - previewSize - 5.0F;
      float previewY = this.y + 2.5F;
      if (HoverUtil.isHovered((double)mouseX, (double)mouseY, (double)previewX, (double)previewY, (double)previewSize, (double)previewSize)) {
         CursorManager.requestHand();
      }

      DrawUtil.drawRound(
         previewX - 0.5F, previewY - 0.5F, previewSize + 1.0F, previewSize + 1.0F, 1.5F, ColorProvider.rgba(48, 66, 122, (float)((int)(140.0F * alpha)))
      );
      DrawUtil.drawRound(previewX, previewY, previewSize, previewSize, 1.5F, this.setting.getValue());
      if (this.openAnim.getValue() > 0.01F) {
         float pickerY = this.y + 13.0F;
         float animH = this.openAnim.getValue() * expandedHeight;
         float animAlpha = alpha * this.openAnim.getValue();
         int animAlphaInt = (int)(255.0F * animAlpha);
         Scissor.push();
         Scissor.setFromComponentCoordinates((double)this.x, (double)pickerY, (double)this.width, (double)animH);
         DrawUtil.drawRound(
            this.x + 4.5F, pickerY - 0.5F, this.width - 9.0F, expandedHeight + 1.0F, 2.5F, ColorProvider.rgba(48, 66, 122, (float)((int)(140.0F * animAlpha)))
         );
         DrawUtil.drawRound(this.x + 5.0F, pickerY, this.width - 10.0F, expandedHeight, 2.5F, ColorProvider.rgba(16, 22, 42, (float)animAlphaInt));
         float svX = this.x + 9.0F;
         float svY = pickerY + 4.0F;
         float svSize = 40.0F;
         int cHue = ColorProvider.setAlpha(Color.HSBtoRGB(this.hue, 1.0F, 1.0F), animAlphaInt);
         int cWhite = ColorProvider.rgba(255, 255, 255, (float)animAlphaInt);
         int cClearWhite = ColorProvider.rgba(255, 255, 255, 0.0F);
         int cBlack = ColorProvider.rgba(0, 0, 0, (float)animAlphaInt);
         int cClearBlack = ColorProvider.rgba(0, 0, 0, 0.0F);
         DrawUtil.drawRound(svX, svY, svSize, svSize, 2.0F, cHue);
         DrawUtil.drawRound(svX, svY, svSize, svSize, 2.0F, cWhite, cWhite, cClearWhite, cClearWhite);
         DrawUtil.drawRound(svX, svY, svSize, svSize, 2.0F, cClearBlack, cBlack, cBlack, cClearBlack);
         float svCursorX = svX + this.saturation * svSize;
         float svCursorY = svY + (1.0F - this.value) * svSize;
         DrawUtil.drawRound(svCursorX - 2.5F, svCursorY - 2.5F, 5.0F, 5.0F, 2.5F, ColorProvider.rgba(0, 0, 0, (float)((int)(180.0F * animAlpha))));
         DrawUtil.drawRound(svCursorX - 1.5F, svCursorY - 1.5F, 3.0F, 3.0F, 1.5F, ColorProvider.rgba(255, 255, 255, (float)animAlphaInt));
         float hueX = svX + svSize + 6.0F;
         float hueY = svY;
         float hueW = 6.0F;

         for (float i = 0.0F; i <= svSize; i += 0.5F) {
            int color = ColorProvider.setAlpha(Color.HSBtoRGB(i / svSize, 1.0F, 1.0F), animAlphaInt);
            DrawUtil.drawRound(hueX, hueY + i, hueW, 1.0F, 0.0F, color);
         }

         float hueCursorY = hueY + this.hue * svSize;
         DrawUtil.drawRound(hueX - 1.5F, hueCursorY - 2.5F, hueW + 3.0F, 5.0F, 2.0F, ColorProvider.rgba(0, 0, 0, (float)((int)(180.0F * animAlpha))));
         DrawUtil.drawRound(hueX - 0.5F, hueCursorY - 1.5F, hueW + 1.0F, 3.0F, 1.0F, ColorProvider.rgba(255, 255, 255, (float)animAlphaInt));
         if (this.draggingSV) {
            this.saturation = Math.max(0.0F, Math.min(1.0F, ((float)mouseX - svX) / svSize));
            this.value = 1.0F - Math.max(0.0F, Math.min(1.0F, ((float)mouseY - svY) / svSize));
            this.syncColor();
         } else if (this.draggingHue) {
            this.hue = Math.max(0.0F, Math.min(1.0F, ((float)mouseY - hueY) / svSize));
            this.syncColor();
         }

         Scissor.unset();
         Scissor.pop();
      }

      this.setHeight(13.0F + this.openAnim.getValue() * expandedHeight);
   }

   private void syncColor() {
      int rgb = Color.HSBtoRGB(this.hue, this.saturation, this.value);
      this.setting.setValue(rgb | 0xFF000000);
   }

   @Override
   public void mouseClicked(double mouseX, double mouseY, int button) {
      float previewSize = 8.0F;
      float previewX = this.x + this.width - previewSize - 5.0F;
      float previewY = this.y + 2.5F;
      if (HoverUtil.isHovered(mouseX, mouseY, (double)previewX, (double)previewY, (double)previewSize, (double)previewSize) && button == 1) {
         this.opened = !this.opened;
      } else {
         if (this.opened && button == 0) {
            float svX = this.x + 9.0F;
            float svY = this.y + 13.0F + 4.0F;
            float svSize = 40.0F;
            float hueX = svX + svSize + 6.0F;
            if (HoverUtil.isHovered(mouseX, mouseY, (double)svX, (double)svY, (double)svSize, (double)svSize)) {
               this.draggingSV = true;
            } else if (HoverUtil.isHovered(mouseX, mouseY, (double)(hueX - 2.0F), (double)svY, 10.0, (double)svSize)) {
               this.draggingHue = true;
            }
         }
      }
   }

   @Override
   public void mouseReleased(double mouseX, double mouseY, int button) {
      this.draggingSV = false;
      this.draggingHue = false;
   }

   @Override
   public boolean isVisible() {
      return this.setting.visible.get();
   }
}
