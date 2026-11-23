package miranware.ui;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.Objects;
import lombok.Generated;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import miranware.module.Module;
import miranware.module.settings.BindSetting;
import miranware.module.settings.BooleanSetting;
import miranware.module.settings.ColorSetting;
import miranware.module.settings.ItemModelSetting;
import miranware.module.settings.ModeListSetting;
import miranware.module.settings.ModeSetting;
import miranware.module.settings.Setting;
import miranware.module.settings.SliderSetting;
import miranware.module.settings.ThemeSetting;
import miranware.ui.component.Component;
import miranware.ui.component.impl.BindComponent;
import miranware.ui.component.impl.BooleanComponent;
import miranware.ui.component.impl.ColorPickerComponent;
import miranware.ui.component.impl.ItemModelComponent;
import miranware.ui.component.impl.ModeComponent;
import miranware.ui.component.impl.ModeListComponent;
import miranware.ui.component.impl.SliderComponent;
import miranware.ui.component.impl.ThemeComponent;
import miranware.util.cursor.CursorManager;
import miranware.util.render.helper.HoverUtil;
import miranware.util.render.math.Animation;
import miranware.util.render.math.Easing;
import miranware.util.render.math.Scissor;
import miranware.util.render.msdf.Fonts;
import miranware.util.render.providers.ColorProvider;
import miranware.util.render.renderers.DrawUtil;

public class ModuleComponent extends Component {
   private final Module module;
   private final Panel panel;
   private final Animation animation = new Animation(Easing.QUINTIC_OUT, 320L);
   private final Animation hoverAnim = new Animation(Easing.QUINTIC_OUT, 300L);
   private final Animation enabledAnim = new Animation(Easing.QUINTIC_OUT, 400L);
   public boolean open;
   private boolean isHovered;
   private boolean binding;
   private final ObjectArrayList<Component> components = new ObjectArrayList();
   private static float cachedSettingsIconWidth = -1.0F;

   public ModuleComponent(Module module, Panel panel) {
      this.module = module;
      this.panel = panel;

      for (Setting setting : module.getSettings()) {
         Objects.requireNonNull(setting);
         switch (setting) {
            case BooleanSetting option:
               this.components.add(new BooleanComponent(option));
               break;
            case ItemModelSetting optionx:
               this.components.add(new ItemModelComponent(optionx, panel::openItemModelGallery));
               break;
            case ModeSetting optionxx:
               this.components.add(new ModeComponent(optionxx));
               break;
            case ModeListSetting optionxxx:
               this.components.add(new ModeListComponent(optionxxx));
               break;
            case SliderSetting optionxxxx:
               this.components.add(new SliderComponent(optionxxxx));
               break;
            case BindSetting optionxxxxx:
               this.components.add(new BindComponent(optionxxxxx));
               break;
            case ThemeSetting optionxxxxxx:
               this.components.add(new ThemeComponent(optionxxxxxx));
               break;
            case ColorSetting optionxxxxxxx:
               this.components.add(new ColorPickerComponent(optionxxxxxxx));
               break;
            default:
               break;
         }
      }

      if (module.getName().equals("Interface")) {
      }
   }

   @Override
   public void render(DrawContext context, int mouseX, int mouseY, float partialTicks) {
      this.isHovered = HoverUtil.isHovered((double)mouseX, (double)mouseY, (double)this.x, (double)this.y, (double)this.width, 19.0);
      this.hoverAnim.run(this.isHovered);
      this.animation.run(this.open);
      this.enabledAnim.run(this.module.isEnabled());
      if (HoverUtil.isHovered((double)mouseX, (double)mouseY, (double)this.x, (double)this.y, (double)this.width, 19.0)) {
         CursorManager.requestHand();
      }

      float alpha = Math.max(Math.min(this.panel.getAnimationAlpha().getValue(), 1.0F), 0.0F);
      int textColor = ColorProvider.interpolateColor(
         ColorProvider.setAlpha(ColorProvider.getColorInactiveText(), (int)(255.0F * alpha)),
         ColorProvider.setAlpha(ColorProvider.getColorText(), (int)(255.0F * alpha)),
         this.enabledAnim.getValue()
      );
      float highlightProgress = Math.max(this.hoverAnim.getValue(), this.enabledAnim.getValue());
      int outlineAlpha = (int)((25.0F + 40.0F * highlightProgress) * alpha);
      int outlineColor = ColorProvider.rgba(255, 255, 255, (float)outlineAlpha);
      int innerColor = ColorProvider.interpolateColor(
         ColorProvider.setAlpha(ColorProvider.getColorMain(), (int)(28.0F * alpha)),
         ColorProvider.setAlpha(ColorProvider.getColorVisualModules(), (int)(22.0F * alpha)),
         this.enabledAnim.getValue()
      );
      float currentHeight = 19.0F + (this.height - 19.0F) * this.animation.getValue();
      float visTop = this.panel.getY() + 25.0F;
      float visBottom = this.panel.getY() + this.panel.getHeight() - 4.0F;
      if (!(this.y + currentHeight < visTop) && !(this.y > visBottom)) {
         DrawUtil.drawRoundBlur(
            this.x, this.y, this.width, currentHeight - 0.5F, 3.0F, ColorProvider.rgba(200, 200, 200, (float)((int)(255.0F * alpha))), 10.0F
         );
         DrawUtil.drawRound(
            this.x - 1.0F, this.y - 1.0F, this.width + 2.0F, currentHeight + 1.0F, 3.5F, ColorProvider.rgba(48, 66, 122, (float)((int)(55.0F * alpha)))
         );
         DrawUtil.drawRound(this.x, this.y, this.width, currentHeight - 0.5F, 3.0F, innerColor);
         if (this.binding) {
            DrawUtil.drawText(
               Fonts.SFREGULAR.get(),
               "Press a key...",
               this.x
                  + this.width / 2.0F
                  - Fonts.SFREGULAR.get().getWidth("Press a key...", 7.5F) / 2.0F,
               this.y + 5.75F,
               ColorProvider.rgba(255, 255, 255, (float)((int)(255.0F * alpha))),
               7.5F
            );
         } else {
            float textY = this.y + 5.75F;
            if (!this.components.isEmpty()) {
               String icon = "{/}";
               if (cachedSettingsIconWidth < 0.0F) {
                  cachedSettingsIconWidth = Fonts.SFREGULAR.get().getWidth(icon, 7.5F);
               }

               float iconWidth = cachedSettingsIconWidth;
               DrawUtil.drawText(
                  Fonts.SFREGULAR.get(), icon, this.x + 3.0F, textY, ColorProvider.setAlpha(ColorProvider.getColorInactiveText(), (int)(180.0F * alpha)), 7.5F
               );
               DrawUtil.drawText(Fonts.SFREGULAR.get(), " " + this.module.getName(), this.x + 3.0F + iconWidth, textY, textColor, 7.5F);
            } else {
               DrawUtil.drawText(Fonts.SFREGULAR.get(), this.module.getName(), this.x + 3.0F, textY, textColor, 7.5F);
            }

            float toggleW = 20.0F;
            float toggleH = 10.0F;
            float toggleX = this.x + this.width - toggleW - 4.0F;
            float toggleY = this.y + (19.0F - toggleH) / 2.0F;
            int toggleBgColor = ColorProvider.interpolateColor(
               ColorProvider.rgba(40, 52, 92, (float)((int)(150.0F * alpha))),
               ColorProvider.setAlpha(ColorProvider.getColorVisualModules(), (int)(200.0F * alpha)),
               this.enabledAnim.getValue()
            );
            DrawUtil.drawRound(toggleX, toggleY, toggleW, toggleH, toggleH / 2.0F, toggleBgColor);
            float circleSize = toggleH - 2.5F;
            float circleX = toggleX + 1.25F + (toggleW - circleSize - 2.5F) * this.enabledAnim.getValue();
            float circleY = toggleY + 1.25F;
            DrawUtil.drawRound(circleX, circleY, circleSize, circleSize, circleSize / 2.0F, ColorProvider.rgba(255, 255, 255, (float)((int)(255.0F * alpha))));
         }

         if (this.animation.getValue() > 0.01F) {
            float compY = this.y + 22.0F;
            float panelTop = this.panel.getY() + 25.0F;
            float panelBottom = this.panel.getY() + this.panel.getHeight() - 4.0F;
            float boxBottom = this.y + currentHeight;
            float intersectY = Math.max(this.y + 19.0F, panelTop);
            float intersectBottom = Math.min(boxBottom, panelBottom);
            float intersectHeight = Math.max(0.0F, intersectBottom - intersectY);
            float darkHeight = currentHeight - 19.0F;
            if (darkHeight > 0.0F) {
               DrawUtil.drawRound(
                  this.x + 1.0F,
                  this.y + 19.0F,
                  this.width - 2.0F,
                  darkHeight,
                  0.0F,
                  ColorProvider.rgba(0, 0, 0, (float)((int)(30.0F * alpha * this.animation.getValue())))
               );
            }

            ObjectListIterator var35 = this.components.iterator();

            while (var35.hasNext()) {
               Component component = (Component)var35.next();
               component.getAlphaAnim().setValue(Math.min(this.panel.getAnimationAlpha().getValue(), 1.0F) * this.animation.getValue());
               component.getAlphaAnimSetting().run(component.isVisible());
               float visibleProgress = MathHelper.clamp(component.getAlphaAnimSetting().getValue(), 0.0F, 1.0F);
               if (component.isVisible() || visibleProgress > 0.0F) {
                  component.setX(this.x);
                  component.setY(compY);
                  component.setWidth(this.width - 4.0F);
                  Scissor.push();
                  Scissor.setFromComponentCoordinates((double)this.x, (double)intersectY, (double)this.width, (double)intersectHeight);
                  component.render(context, mouseX, mouseY, partialTicks);
                  Scissor.unset();
                  Scissor.pop();
                  compY += component.getHeight() * visibleProgress;
               }
            }
         }
      } else {
         this.isHovered = false;
      }
   }

   @Override
   public void mouseClicked(double mouseX, double mouseY, int button) {
      if (this.isHovered(mouseX, mouseY, 19.0F)) {
         if (button == 0) {
            this.module.setEnabled(!this.module.isEnabled());
         }

         if (button == 1 && !this.components.isEmpty()) {
            this.open = !this.open;
         }

         if (button == 2) {
            this.binding = !this.binding;
         }
      }

      if (this.open) {
         ObjectListIterator var6 = this.components.iterator();

         while (var6.hasNext()) {
            Component component = (Component)var6.next();
            if (component.isVisible() && component.getAlphaAnimSetting().getValue() > 0.5F) {
               component.mouseClicked(mouseX, mouseY, button);
            }
         }
      }
   }

   @Override
   public void mouseReleased(double mouseX, double mouseY, int button) {
      if (this.open) {
         ObjectListIterator var6 = this.components.iterator();

         while (var6.hasNext()) {
            Component component = (Component)var6.next();
            component.mouseReleased(mouseX, mouseY, button);
         }
      }
   }

   @Override
   public void keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.binding) {
         if (keyCode != 256 && keyCode != 261) {
            this.module.setKey(keyCode);
         } else {
            this.module.setKey(-1);
         }

         this.binding = false;
      }

      if (this.open) {
         ObjectListIterator var4 = this.components.iterator();

         while (var4.hasNext()) {
            Component component = (Component)var4.next();
            component.keyPressed(keyCode, scanCode, modifiers);
         }
      }
   }

   public boolean isBinding() {
      return this.binding;
   }

   private boolean isHovered(double mouseX, double mouseY, float heightCheck) {
      return HoverUtil.isHovered(mouseX, mouseY, (double)this.x, (double)this.y, (double)this.width, (double)heightCheck);
   }

   @Generated
   public Module getModule() {
      return this.module;
   }

   @Generated
   public Panel getPanel() {
      return this.panel;
   }

   @Generated
   public Animation getAnimation() {
      return this.animation;
   }

   @Generated
   public Animation getHoverAnim() {
      return this.hoverAnim;
   }

   @Generated
   public Animation getEnabledAnim() {
      return this.enabledAnim;
   }

   @Generated
   public boolean isOpen() {
      return this.open;
   }

   @Generated
   public boolean isHovered() {
      return this.isHovered;
   }

   @Generated
   public ObjectArrayList<Component> getComponents() {
      return this.components;
   }
}
