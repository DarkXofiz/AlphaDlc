package miranware.ui.component.impl;

import net.minecraft.client.util.math.MatrixStack;
import miranware.module.settings.BindSetting;
import miranware.ui.component.Component;
import miranware.util.cursor.CursorManager;
import miranware.util.keyboard.KeyStorage;
import miranware.util.render.helper.HoverUtil;
import miranware.util.render.msdf.Fonts;
import miranware.util.render.providers.ColorProvider;
import miranware.util.render.renderers.DrawUtil;

public class BindComponent extends Component {
   private final BindSetting setting;
   private boolean binding;
   private static final float SETTING_HEIGHT = 14.0F;
   private static final float BIND_H = 10.0F;
   private static final float PADDING = 4.5F;
   private static final float RADIUS = 1.0F;

   public BindComponent(BindSetting setting) {
      this.setting = setting;
   }

   private String getKeyText() {
      if (this.binding) {
         return "...";
      } else {
         return this.setting.getValue() == -1 ? "None" : KeyStorage.getKey(this.setting.getValue());
      }
   }

   @Override
   public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
      float alpha = Math.max(Math.min(this.getAlphaAnimSetting().getValue(), 1.0F), 0.0F) * Math.max(Math.min(this.getAlphaAnim().getValue(), 1.0F), 0.0F);
      int alphaInt = (int)(255.0F * alpha);
      if (!(alpha < 0.02F)) {
         String keyText = this.getKeyText();
         float keyTextW = Fonts.SFREGULAR.get().getWidth(keyText, 6.5F);
         float bindBoxW = Math.max(16.0F, keyTextW + 8.0F);
         float bindX = this.x + this.width - bindBoxW - 4.5F;
         float bindY = this.y + 2.0F;
         if (HoverUtil.isHovered((double)mouseX, (double)mouseY, (double)bindX, (double)bindY, (double)bindBoxW, 10.0)) {
            CursorManager.requestHand();
         }

         DrawUtil.drawText(
            Fonts.SFREGULAR.get(), this.setting.getName(), this.x + 4.5F, this.y + 3.5F, ColorProvider.setAlpha(ColorProvider.getColorText(), alphaInt), 7.5F
         );
         boolean hasBind = this.setting.getValue() != -1;
         int bgColor;
         if (this.binding) {
            bgColor = ColorProvider.setAlpha(ColorProvider.getColorButton(), (int)(35.0F * alpha));
         } else if (hasBind) {
            bgColor = ColorProvider.setAlpha(ColorProvider.getColorButton(), (int)(80.0F * alpha));
         } else {
            bgColor = ColorProvider.setAlpha(ColorProvider.getColorInactiveButton(), (int)(80.0F * alpha));
         }

         DrawUtil.drawRound(bindX, bindY, bindBoxW, 10.0F, 1.0F, bgColor);
         int textColor = !this.binding && !hasBind
            ? ColorProvider.setAlpha(ColorProvider.getColorInactiveText(), alphaInt)
            : ColorProvider.setAlpha(ColorProvider.getColorText(), alphaInt);
         float keyX = bindX + (bindBoxW - keyTextW) / 2.0F;
         DrawUtil.drawText(Fonts.SFREGULAR.get(), keyText, keyX, bindY + 1.25F, textColor, 7.5F);
         this.setHeight(14.0F);
      }
   }

   @Override
   public void mouseClicked(double mouseX, double mouseY, int button) {
      String keyText = this.getKeyText();
      float keyTextW = Fonts.SFREGULAR.get().getWidth(keyText, 7.5F);
      float bindBoxW = Math.max(16.0F, keyTextW + 8.0F);
      float bindX = this.x + this.width - bindBoxW - 4.5F;
      float bindY = this.y + 2.0F;
      if (this.binding) {
         if (button != 0) {
            this.setting.setValue(button);
         }

         this.binding = false;
      } else if (HoverUtil.isHovered(mouseX, mouseY, (double)bindX, (double)bindY, (double)bindBoxW, 10.0) && button == 0) {
         this.binding = true;
      }
   }

   @Override
   public void keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.binding) {
         if (keyCode != 256 && keyCode != 261) {
            this.setting.setValue(keyCode);
         } else {
            this.setting.setValue(-1);
         }

         this.binding = false;
      }
   }

   @Override
   public boolean isVisible() {
      return this.setting.visible.get();
   }
}
