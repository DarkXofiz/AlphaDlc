package zov.alphadlc.ui;

import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.Generated;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector4f;
import zov.alphadlc.AlphaDLC;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.settings.ItemModelSetting;
import zov.alphadlc.ui.component.Component;
import zov.alphadlc.util.IMinecraft;
import zov.alphadlc.util.render.helper.HoverUtil;
import zov.alphadlc.util.render.math.Animation;
import zov.alphadlc.util.render.math.Easing;
import zov.alphadlc.util.render.math.Scissor;
import zov.alphadlc.util.render.msdf.Fonts;
import zov.alphadlc.util.render.providers.ColorProvider;
import zov.alphadlc.util.render.renderers.DrawUtil;

public class Panel implements IMinecraft {
   public float x;
   public float y;
   public float width;
   public float height;
   public final ModuleCategory category;
   public List<ModuleComponent> moduleComponents = new ArrayList<>();
   private Animation animation = new Animation(Easing.QUINTIC_OUT, 350L);
   private Animation animationAlpha = new Animation(Easing.BOUNCE_OUT, 350L);
   private final Animation scrollbarAnim = new Animation(Easing.CUBIC_IN_OUT, 220L);
   public final Animation slideAnim = new Animation(Easing.QUINTIC_OUT, 320L);
   public int slideDir = 1;
   float scroll;
   float maxScroll;
   private final ClickGuiFrame parent;
   private String cachedTitle;
   private String cachedIcon;
   private float cachedTitleWidth = -1.0F;
   private float cachedIconWidth = -1.0F;

   public Panel(ModuleCategory category, ClickGuiFrame parent) {
      this.category = category;
      this.parent = parent;
      AlphaDLC.getInstance()
         .getModuleStorage()
         .getModules()
         .stream()
         .filter(m -> m.getCategory() == this.category)
         .sorted(Comparator.comparing(m -> m.getName().toLowerCase()))
         .forEach(m -> this.moduleComponents.add(new ModuleComponent(m, this)));
   }

   public void clampScroll() {
      if (this.maxScroll > 0.0F) {
         this.scroll = MathHelper.method_15363(this.scroll, -this.maxScroll, 0.0F);
      } else {
         this.scroll = 0.0F;
      }
   }

   public void render(DrawContext context, int mouseX, int mouseY, float partialTicks) {
      float alphaRatio = MathHelper.method_15363(this.slideAnim.getValue(), 0.0F, 1.0F);
      this.animationAlpha.setValue(alphaRatio);
      float alpha = Math.min(255.0F * alphaRatio, 255.0F);
      float cornerRadius = 8.0F;
      float headerHeight = 25.0F;
      int panelColor = ColorProvider.setAlpha(ColorProvider.getColorClickGui(), (int)(130.0F * alphaRatio));
      DrawUtil.drawRoundBlur(
         this.x, this.y, this.width, this.height, cornerRadius, ColorProvider.rgba(200, 200, 200, (float)((int)(255.0F * alphaRatio))), 14.0F
      );
      DrawUtil.drawRound(
         this.x - 1.0F,
         this.y - 1.0F,
         this.width + 2.0F,
         this.height + 2.0F,
         cornerRadius + 0.5F,
         ColorProvider.rgba(48, 66, 122, (float)((int)(70.0F * alphaRatio)))
      );
      DrawUtil.drawRound(this.x, this.y, this.width, this.height, cornerRadius, panelColor);
      DrawUtil.drawRound(
         this.x,
         this.y,
         this.width,
         headerHeight,
         new Vector4f(cornerRadius, 0.0F, 0.0F, cornerRadius),
         ColorProvider.setAlpha(ColorProvider.getColorHeaderBg(), (int)(45.0F * alphaRatio))
      );
      float iconSize = 8.5F;
      if (this.cachedTitleWidth < 0.0F) {
         String title = this.category.name();
         this.cachedTitle = title.substring(0, 1).toUpperCase() + title.substring(1).toLowerCase();

         this.cachedIcon = switch (this.category) {
            case COMBAT -> "a";
            case MOVEMENT -> "b";
            case RENDER -> "c";
            case PLAYER -> "d";
            case MISC -> "e";
         };
         this.cachedTitleWidth = Fonts.SFREGULAR.get().getWidth(this.cachedTitle, 8.5F);
         this.cachedIconWidth = Fonts.ICONS_MINCED.get().getWidth(this.cachedIcon, iconSize);
      }

      String capitalizedTitle = this.cachedTitle;
      float titleWidth = this.cachedTitleWidth;
      String categoryIcon = this.cachedIcon;
      float iconWidth = this.cachedIconWidth;
      float totalWidth = iconWidth + 3.0F + titleWidth;
      float startX = this.x + this.width / 2.0F - totalWidth / 2.0F - 1.0F;
      float titleY = this.y + headerHeight / 2.0F - 4.25F;
      float iconY = titleY + 1.0F;
      DrawUtil.drawText(Fonts.ICONS_MINCED.get(), categoryIcon, startX, iconY, ColorProvider.setAlpha(ColorProvider.getColorIcons(), (int)alpha), iconSize);
      DrawUtil.drawText(
         Fonts.SFREGULAR.get(),
         capitalizedTitle,
         startX + iconWidth + 3.0F,
         titleY,
         ColorProvider.setAlpha(ColorProvider.getColorHeaderText(), (int)alpha),
         8.5F
      );
      float offset = 2.0F;
      this.clampScroll();
      this.animation.run(this.scroll);
      Scissor.push();
      Scissor.setFromComponentCoordinates((double)this.x, (double)(this.y + headerHeight), (double)this.width, (double)(this.height - headerHeight - 4.0F));

      for (ModuleComponent component : this.moduleComponents) {
         if (!this.parent.searchCheck(component.getModule().getName())) {
            float sideMargin = 6.0F;
            component.setX(this.x + sideMargin);
            component.setY(this.y + headerHeight + offset + this.animation.getValue());
            component.setWidth(this.width - sideMargin * 2.0F);
            float baseHeight = 19.0F;
            float extraHeight = 0.0F;
            if (component.getAnimation().getValue() > 0.01F) {
               extraHeight = 3.0F;
               ObjectListIterator var25 = component.getComponents().iterator();

               while (var25.hasNext()) {
                  Component comp = (Component)var25.next();
                  float visibleProgress = MathHelper.method_15363(comp.getAlphaAnimSetting().getValue(), 0.0F, 1.0F);
                  if (comp.isVisible() || visibleProgress > 0.0F) {
                     extraHeight += comp.getHeight() * visibleProgress;
                  }
               }

               extraHeight += 5.0F;
            }

            component.setHeight(baseHeight + extraHeight * component.getAnimation().getValue());
            Scissor.setFromComponentCoordinates(
               (double)this.x, (double)(this.y + headerHeight), (double)this.width, (double)(this.height - headerHeight - 4.0F)
            );
            component.render(context, mouseX, mouseY, partialTicks);
            Scissor.setFromComponentCoordinates(
               (double)this.x, (double)(this.y + headerHeight), (double)this.width, (double)(this.height - headerHeight - 4.0F)
            );
            offset += component.getHeight() + 5.0F;
         }
      }

      this.maxScroll = Math.max(0.0F, offset - (this.height - headerHeight - 8.0F));
      this.scrollbarAnim.run(this.maxScroll > 0.0F);
      Scissor.unset();
      Scissor.pop();
   }

   public void mouseClicked(double mouseX, double mouseY, int button) {
      if (HoverUtil.isHovered(mouseX, mouseY, (double)this.x, (double)(this.y + 20.0F), (double)this.width, (double)(this.height - 20.0F))) {
         for (ModuleComponent moduleComponent : this.moduleComponents) {
            if (!this.parent.searchCheck(moduleComponent.getModule().getName())) {
               moduleComponent.mouseClicked(mouseX, mouseY, button);
            }
         }
      }
   }

   public void mouseReleased(double mouseX, double mouseY, int button) {
      for (ModuleComponent moduleComponent : this.moduleComponents) {
         if (!this.parent.searchCheck(moduleComponent.getModule().getName())) {
            moduleComponent.mouseReleased(mouseX, mouseY, button);
         }
      }
   }

   public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      if (HoverUtil.isHovered(mouseX, mouseY, (double)this.x, (double)this.y, (double)this.width, (double)this.height)) {
         this.scroll += (float)(verticalAmount * 30.0);
         this.clampScroll();
      }
   }

   public void keyPressed(int keyCode, int scanCode, int modifiers) {
      for (ModuleComponent moduleComponent : this.moduleComponents) {
         moduleComponent.keyPressed(keyCode, scanCode, modifiers);
      }
   }

   public void openItemModelGallery(ItemModelSetting setting) {
      this.parent.openItemModelGallery(setting);
   }

   @Generated
   public float getX() {
      return this.x;
   }

   @Generated
   public float getY() {
      return this.y;
   }

   @Generated
   public float getWidth() {
      return this.width;
   }

   @Generated
   public float getHeight() {
      return this.height;
   }

   @Generated
   public ModuleCategory getCategory() {
      return this.category;
   }

   @Generated
   public List<ModuleComponent> getModuleComponents() {
      return this.moduleComponents;
   }

   @Generated
   public Animation getAnimation() {
      return this.animation;
   }

   @Generated
   public Animation getAnimationAlpha() {
      return this.animationAlpha;
   }

   @Generated
   public Animation getScrollbarAnim() {
      return this.scrollbarAnim;
   }

   @Generated
   public Animation getSlideAnim() {
      return this.slideAnim;
   }

   @Generated
   public int getSlideDir() {
      return this.slideDir;
   }

   @Generated
   public float getScroll() {
      return this.scroll;
   }

   @Generated
   public float getMaxScroll() {
      return this.maxScroll;
   }

   @Generated
   public ClickGuiFrame getParent() {
      return this.parent;
   }

   @Generated
   public String getCachedTitle() {
      return this.cachedTitle;
   }

   @Generated
   public String getCachedIcon() {
      return this.cachedIcon;
   }

   @Generated
   public float getCachedTitleWidth() {
      return this.cachedTitleWidth;
   }

   @Generated
   public float getCachedIconWidth() {
      return this.cachedIconWidth;
   }

   @Generated
   public void setX(float x) {
      this.x = x;
   }

   @Generated
   public void setY(float y) {
      this.y = y;
   }

   @Generated
   public void setWidth(float width) {
      this.width = width;
   }

   @Generated
   public void setHeight(float height) {
      this.height = height;
   }

   @Generated
   public void setModuleComponents(List<ModuleComponent> moduleComponents) {
      this.moduleComponents = moduleComponents;
   }

   @Generated
   public void setAnimation(Animation animation) {
      this.animation = animation;
   }

   @Generated
   public void setAnimationAlpha(Animation animationAlpha) {
      this.animationAlpha = animationAlpha;
   }

   @Generated
   public void setSlideDir(int slideDir) {
      this.slideDir = slideDir;
   }

   @Generated
   public void setScroll(float scroll) {
      this.scroll = scroll;
   }

   @Generated
   public void setMaxScroll(float maxScroll) {
      this.maxScroll = maxScroll;
   }

   @Generated
   public void setCachedTitle(String cachedTitle) {
      this.cachedTitle = cachedTitle;
   }

   @Generated
   public void setCachedIcon(String cachedIcon) {
      this.cachedIcon = cachedIcon;
   }

   @Generated
   public void setCachedTitleWidth(float cachedTitleWidth) {
      this.cachedTitleWidth = cachedTitleWidth;
   }

   @Generated
   public void setCachedIconWidth(float cachedIconWidth) {
      this.cachedIconWidth = cachedIconWidth;
   }
}
