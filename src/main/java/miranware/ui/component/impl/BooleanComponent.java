package miranware.ui.component.impl;

import net.minecraft.client.util.math.MatrixStack;
import miranware.module.settings.BooleanSetting;
import miranware.ui.component.Component;
import miranware.util.cursor.CursorManager;
import miranware.util.render.helper.HoverUtil;
import miranware.util.render.msdf.Fonts;
import miranware.util.render.providers.ColorProvider;
import miranware.util.render.renderers.DrawUtil;

public class BooleanComponent extends Component {
   private final BooleanSetting setting;
   private boolean binding;

   public BooleanComponent(BooleanSetting setting) {
      this.setting = setting;
   }

   @Override
   public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
      float alpha = Math.max(Math.min(this.getAlphaAnimSetting().getValue(), 1.0F), 0.0F) * Math.max(Math.min(this.getAlphaAnim().getValue(), 1.0F), 0.0F);
      int alphaInt = (int)(255.0F * alpha);
      float toggleW = 15.0F;
      float toggleH = 8.0F;
      float toggleX = this.x + this.width - toggleW - 2.5F;
      float toggleY = this.y + 4.0F;
      if (HoverUtil.isHovered((double)mouseX, (double)mouseY, (double)toggleX, (double)toggleY, (double)toggleW, (double)toggleH)) {
         CursorManager.requestHand();
      }

      DrawUtil.drawText(
         Fonts.SFREGULAR.get(),
         this.binding ? "Binding..." : this.setting.getName(),
         this.x + 4.5F,
         this.y + 5.0F,
         ColorProvider.setAlpha(ColorProvider.getColorText(), alphaInt),
         7.5F,
         0.4F,
         1.0F,
         this.width - toggleW - 8.0F
      );
      float anim = this.setting.getAnimation().getValue();
      if (anim > 0.01F) {
         int indicator = ColorProvider.setAlpha(ColorProvider.getColorIndicator(), alphaInt);
         int inactiveIndicator = ColorProvider.setAlpha(ColorProvider.getColorInactiveIndicator(), alphaInt);
         int bgColor = ColorProvider.interpolateColor(inactiveIndicator, indicator, anim);
         DrawUtil.drawRound(toggleX, toggleY, toggleW, toggleH, toggleH / 2.0F, bgColor);
      } else {
         DrawUtil.drawRound(toggleX, toggleY, toggleW, toggleH, toggleH / 2.0F, ColorProvider.setAlpha(ColorProvider.getColorInactiveIndicator(), alphaInt));
      }

      float knobSize = toggleH - 1.0F;
      float knobMinX = toggleX + 0.5F;
      float knobMaxX = toggleX + toggleW - knobSize - 0.5F;
      float knobX = knobMinX + (knobMaxX - knobMinX) * anim;
      float knobY = toggleY + 0.5F;
      DrawUtil.drawCircle(
         knobX + knobSize / 2.0F, knobY + knobSize / 2.0F, knobSize / 2.0F, ColorProvider.setAlpha(ColorProvider.getColorSliderCircle(), alphaInt)
      );
      this.setHeight(16.0F);
   }

   @Override
   public void mouseClicked(double mouseX, double mouseY, int button) {
      float toggleW = 15.0F;
      float toggleH = 8.0F;
      float toggleX = this.x + this.width - toggleW - 2.5F;
      float toggleY = this.y + 4.0F;
      if (HoverUtil.isHovered(mouseX, mouseY, (double)toggleX, (double)toggleY, (double)toggleW, (double)toggleH)) {
         if (button == 0) {
            this.setting.setValue(!this.setting.getValue());
         }

         if (button == 2 && this.binding) {
            this.binding = false;
            return;
         }

         if (this.binding) {
            this.setting.setKey(button);
            this.binding = false;
         }

         if (button == 2) {
            this.binding = true;
         }
      }
   }

   @Override
   public void keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.binding) {
         if (keyCode == 256) {
            this.binding = false;
            return;
         }

         if (keyCode == 261) {
            this.setting.setKey(-1);
            return;
         }

         this.setting.setKey(keyCode);
         this.binding = false;
      }
   }

   @Override
   public boolean isVisible() {
      return this.setting.visible.get();
   }
}
