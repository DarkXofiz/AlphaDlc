package zov.alphadlc.ui.component.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import zov.alphadlc.module.settings.SliderSetting;
import zov.alphadlc.ui.component.Component;
import zov.alphadlc.util.render.helper.HoverUtil;
import zov.alphadlc.util.render.math.Animation;
import zov.alphadlc.util.render.math.Easing;
import zov.alphadlc.util.render.msdf.Fonts;
import zov.alphadlc.util.render.providers.ColorProvider;
import zov.alphadlc.util.render.renderers.DrawUtil;

public class SliderComponent extends Component {
   private final SliderSetting setting;
   private boolean drag;
   private final Animation sliderAnimation = new Animation(Easing.QUINTIC_OUT, 100L);

   public SliderComponent(SliderSetting setting) {
      this.setting = setting;
   }

   private double round(double num, double increment) {
      double v = (double)Math.round(num / increment) * increment;
      return new BigDecimal(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
   }

   private String formatNumber(double value) {
      return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
   }

   @Override
   public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
      float alpha = Math.min(this.getAlphaAnimSetting().getValue(), 1.0F) * Math.max(Math.min(this.getAlphaAnim().getValue(), 1.0F), 0.0F);
      int alphaInt = (int)(255.0F * alpha);
      String numberText = this.formatNumber(this.setting.getValue());
      float trackWidth = this.width - 9.0F;
      this.sliderAnimation
         .run((float)((double)trackWidth * (this.setting.getValue() - this.setting.getMin()) / (this.setting.getMax() - this.setting.getMin())));
      DrawUtil.drawText(
         Fonts.SFREGULAR.get(),
         this.setting.getName(),
         this.x + 4.5F,
         this.y + 3.0F,
         ColorProvider.setAlpha(ColorProvider.getColorText(), alphaInt),
         7.5F,
         0.6F,
         1.0F,
         trackWidth
      );
      DrawUtil.drawText(
         Fonts.SFREGULAR.get(),
         numberText,
         this.x + this.width - 4.5F - Fonts.SFREGULAR.get().getWidth(numberText, 7.5F),
         this.y + 1.0F,
         ColorProvider.setAlpha(ColorProvider.getColorInactiveText(), alphaInt),
         7.5F
      );
      float trackY = this.y + 14.0F;
      DrawUtil.drawRound(
         this.x + 3.0F, trackY - 3.5F, trackWidth + 1.0F, 4.0F, 1.0F, ColorProvider.setAlpha(ColorProvider.getColorSliderWindow(), (int)(100.0F * alpha))
      );
      DrawUtil.drawRound(this.x + 3.5F, trackY - 3.0F, trackWidth, 3.0F, 1.0F, ColorProvider.setAlpha(ColorProvider.getColorSliderWindow(), alphaInt));
      float fillWidth = MathHelper.method_15363(this.sliderAnimation.getValue(), 0.0F, trackWidth);
      int sliderColor = ColorProvider.setAlpha(ColorProvider.getColorSlider(), alphaInt);
      DrawUtil.drawRound(this.x + 3.5F, trackY - 3.5F, fillWidth, 4.0F, 1.0F, sliderColor);
      float circleSize = this.drag ? 7.0F : 5.5F;
      float circleX = this.x + 3.5F + fillWidth;
      float circleY = trackY - 1.5F;
      DrawUtil.drawRound(
         circleX - circleSize / 2.0F,
         circleY - circleSize / 2.0F,
         circleSize,
         circleSize,
         circleSize / 2.0F,
         ColorProvider.setAlpha(ColorProvider.getColorSliderCircle(), alphaInt)
      );
      if (this.drag) {
         double val = (double)(((float)mouseX - (this.x + 3.5F)) / trackWidth) * (this.setting.getMax() - this.setting.getMin()) + this.setting.getMin();
         this.setting.setValue((double)((float)MathHelper.method_15350(this.round(val, this.setting.getStep()), this.setting.getMin(), this.setting.getMax())));
      }

      this.setHeight(15.0F);
   }

   @Override
   public void mouseClicked(double mouseX, double mouseY, int button) {
      if (HoverUtil.isHovered(mouseX, mouseY, (double)(this.x + 3.0F), (double)(this.y + 8.0F), (double)(this.width - 6.0F), 8.0) && button == 0) {
         this.drag = true;
      }
   }

   @Override
   public void mouseReleased(double mouseX, double mouseY, int button) {
      this.drag = false;
   }

   @Override
   public void keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 256) {
         this.drag = false;
      }
   }

   @Override
   public boolean isVisible() {
      return this.setting.visible.get();
   }
}
