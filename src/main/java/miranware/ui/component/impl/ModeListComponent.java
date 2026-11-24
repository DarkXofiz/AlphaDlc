package miranware.ui.component.impl;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.util.math.MatrixStack;
import miranware.module.settings.BooleanSetting;
import miranware.module.settings.ModeListSetting;
import miranware.ui.component.Component;
import miranware.util.cursor.CursorManager;
import miranware.util.render.helper.HoverUtil;
import miranware.util.render.math.Animation;
import miranware.util.render.math.Easing;
import miranware.util.render.msdf.Fonts;
import miranware.util.render.providers.ColorProvider;
import miranware.util.render.renderers.DrawUtil;

public class ModeListComponent extends Component {
   private final ModeListSetting setting;
   private static final float NAME_HEIGHT = 10.0F;
   private static final float OPTION_H = 11.0F;
   private static final float GAP = 2.0F;
   private static final float PADDING = 4.5F;
   private static final float RADIUS = 1.0F;
   private final List<Animation> anims = new ArrayList<>();

   public ModeListComponent(ModeListSetting setting) {
      this.setting = setting;

      for (int i = 0; i < setting.getSettings().size(); i++) {
         this.anims.add(new Animation(Easing.QUINTIC_OUT, 250L));
      }
   }

   private float optionWidth(String name) {
      return Fonts.SFREGULAR.get().getWidth(name, 7.5F) + 8.0F;
   }

   private float calcHeight() {
      float maxRowWidth = this.width - 9.0F;
      float rowWidth = 0.0F;
      float totalHeight = 12.0F;
      boolean firstInRow = true;

      for (BooleanSetting s : this.setting.getSettings()) {
         float ow = this.optionWidth(s.getName());
         if (!firstInRow && rowWidth + ow > maxRowWidth) {
            totalHeight += 13.0F;
            rowWidth = ow + 2.0F;
         } else {
            rowWidth += ow + 2.0F;
            firstInRow = false;
         }
      }

      return totalHeight + 11.0F;
   }

   @Override
   public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
      float animValue = this.getAlphaAnimSetting().getValue();
      float alpha = Math.max(Math.min(animValue * this.getAlphaAnim().getValue(), 1.0F), 0.0F);
      int alphaInt = (int)(255.0F * alpha);
      if (!(alpha < 0.02F)) {
         float totalHeight = this.calcHeight();
         this.setHeight((totalHeight + 2.0F) * animValue);
         DrawUtil.drawText(
            Fonts.SFREGULAR.get(), this.setting.getName(), this.x + 4.5F, this.y + 1.5F, ColorProvider.setAlpha(ColorProvider.getColorText(), alphaInt), 7.5F
         );
         float curX = this.x + 4.5F;
         float curY = this.y + 10.0F + 2.0F;
         boolean firstInRow = true;
         int i = 0;

         for (BooleanSetting s : this.setting.getSettings()) {
            float ow = this.optionWidth(s.getName());
            if (!firstInRow && curX + ow > this.x + this.width - 4.5F) {
               curX = this.x + 4.5F;
               curY += 13.0F;
            }

            firstInRow = false;
            boolean selected = s.getValue();
            Animation anim = this.anims.get(i);
            anim.run(selected);
            float av = anim.getValue();
            if (HoverUtil.isHovered((double)mouseX, (double)mouseY, (double)curX, (double)curY, (double)ow, 11.0)) {
               CursorManager.requestHand();
            }

            int bgColor = ColorProvider.interpolateColor(
               ColorProvider.setAlpha(ColorProvider.getColorInactiveButton(), alphaInt), ColorProvider.setAlpha(ColorProvider.getColorButton(), alphaInt), av
            );
            DrawUtil.drawRound(curX, curY, ow, 11.0F, 1.0F, bgColor);
            int textColor = ColorProvider.interpolateColor(
               ColorProvider.setAlpha(ColorProvider.getColorInactiveText(), alphaInt), ColorProvider.setAlpha(ColorProvider.getColorText(), alphaInt), av
            );
            float tw = Fonts.SFREGULAR.get().getWidth(s.getName(), 7.5F);
            DrawUtil.drawText(Fonts.SFREGULAR.get(), s.getName(), curX + (ow - tw) / 2.0F, curY + 1.75F, textColor, 7.5F);
            curX += ow + 2.0F;
            i++;
         }
      }
   }

   @Override
   public void mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0) {
         float curX = this.x + 4.5F;
         float curY = this.y + 10.0F + 2.0F;
         boolean firstInRow = true;

         for (BooleanSetting s : this.setting.getSettings()) {
            float ow = this.optionWidth(s.getName());
            if (!firstInRow && curX + ow > this.x + this.width - 4.5F) {
               curX = this.x + 4.5F;
               curY += 13.0F;
            }

            firstInRow = false;
            if (HoverUtil.isHovered(mouseX, mouseY, (double)curX, (double)curY, (double)ow, 11.0)) {
               s.setValue(!s.getValue());
               s.setClicked(true);
               return;
            }

            curX += ow + 2.0F;
         }
      }
   }

   @Override
   public boolean isVisible() {
      return this.setting.visible.get();
   }
}
