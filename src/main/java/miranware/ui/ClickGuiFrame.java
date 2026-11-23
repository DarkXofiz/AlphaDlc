package miranware.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.Generated;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4fStack;
import org.lwjgl.glfw.GLFW;
import miranware.module.ModuleCategory;
import miranware.module.list.render.ClickGui;
import miranware.module.settings.ItemModelSetting;
import miranware.ui.component.ItemModelGalleryPopup;
import miranware.ui.component.SearchField;
import miranware.util.IMinecraft;
import miranware.util.base.Instance;
import miranware.util.cursor.CursorManager;
import miranware.util.render.helper.HoverUtil;
import miranware.util.render.math.Animation;
import miranware.util.render.math.Easing;
import miranware.util.render.math.Scissor;
import miranware.util.render.msdf.Fonts;
import miranware.util.render.providers.ColorProvider;
import miranware.util.render.renderers.DrawUtil;

public class ClickGuiFrame extends Screen implements IMinecraft {
   private final List<Panel> panels = new ArrayList<>();
   private final SearchField searchField;
   private final ThemeEditor themeEditor = new ThemeEditor();
   private ItemModelGalleryPopup itemModelGallery;
   private final Animation descAnim = new Animation(Easing.QUINTIC_OUT, 220L);
   private String lastDesc = null;
   private boolean closing = false;
   private static final float SEARCH_HEIGHT = 18.0F;
   private float panelHeightCurrent = -1.0F;
   private long lastFrameNanos = 0L;
   private long handCursor;
   private long iBeamCursor;
   private long pointingCursor;
   private long arrowCursor;
   private boolean cursorsCreated = false;
   private long currentCursor = 0L;
   private String cachedRawQuery = null;
   private String cachedNormalizedQuery = "";

   private void ensureCursors() {
      if (!this.cursorsCreated) {
         this.handCursor = GLFW.glfwCreateStandardCursor(221188);
         this.iBeamCursor = GLFW.glfwCreateStandardCursor(221186);
         this.pointingCursor = GLFW.glfwCreateStandardCursor(221188);
         this.arrowCursor = GLFW.glfwCreateStandardCursor(221185);
         this.cursorsCreated = true;
      }
   }

   private void applyCursor(long cursor) {
      if (cursor != this.currentCursor) {
         GLFW.glfwSetCursor(mc.getWindow().getHandle(), cursor);
         this.currentCursor = cursor;
      }
   }

   public void playOpenAnimation() {
      this.closing = false;
      this.panelHeightCurrent = -1.0F;
      this.lastFrameNanos = 0L;
      this.itemModelGallery = null;

      for (Panel panel : this.panels) {
         panel.slideAnim.reset(0.0F);
      }

      this.themeEditor.resetAppear();
      this.searchField.resetAppear();
   }

   public ClickGuiFrame() {
      super(Text.of("Avalora Frame"));
      this.searchField = new SearchField("Search...");

      for (ModuleCategory category : ModuleCategory.values()) {
         this.panels.add(new Panel(category, this));
      }
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      CursorManager.reset();
      CursorManager.resetIBeam();
      CursorManager.resetClick();
      int windowWidth = mc.getWindow().getScaledWidth();
      int windowHeight = mc.getWindow().getScaledHeight();
      float guiScale = this.guiScale();
      float centerX = (float)windowWidth / 2.0F;
      float centerY = (float)windowHeight / 2.0F;
      int mouseGx = (int)(((float)mouseX - centerX) / guiScale + centerX);
      int mouseGy = (int)(((float)mouseY - centerY) / guiScale + centerY);
      mouseX = mouseGx;
      mouseY = mouseGy;
      Scissor.setGuiTransform(guiScale, centerX, centerY);
      Matrix4fStack modelView = RenderSystem.getModelViewStack();
      modelView.pushMatrix();
      modelView.translate(centerX, centerY, 0.0F);
      modelView.scale(guiScale, guiScale, 1.0F);
      modelView.translate(-centerX, -centerY, 0.0F);
      int panelCount = Math.max(1, this.panels.size());
      float spacing = 7.0F;
      float sideMargin = 12.0F;
      float panelWidth = (float)Math.floor((double)Math.min(122.0F, ((float)windowWidth - sideMargin * 2.0F - spacing * (float)(panelCount - 1)) / (float)panelCount));
      panelWidth = Math.max(60.0F, panelWidth);
      float panelTotalWidth = panelWidth * (float)panelCount + spacing * (float)(panelCount - 1);
      float startX = (float)Math.floor((double)(((float)windowWidth - panelTotalWidth) / 2.0F));
      float bottomReserve = 8.0F + SEARCH_HEIGHT + 4.0F;
      float maxPanelHeight = (float)windowHeight - 24.0F - bottomReserve;
      float naturalHeight = 150.0F;

      for (Panel measured : this.panels) {
         naturalHeight = Math.max(naturalHeight, measured.naturalHeight());
      }

      float targetHeight = Math.max(80.0F, Math.min(maxPanelHeight, naturalHeight));
      long nowNanos = System.nanoTime();
      float frameSeconds = this.lastFrameNanos == 0L ? 0.016F : Math.min(0.1F, (float)(nowNanos - this.lastFrameNanos) / 1.0E9F);
      this.lastFrameNanos = nowNanos;
      this.panelHeightCurrent = this.panelHeightCurrent < 0.0F
         ? targetHeight
         : this.panelHeightCurrent + (targetHeight - this.panelHeightCurrent) * Math.min(1.0F, 12.0F * frameSeconds);
      float panelHeight = this.panelHeightCurrent;
      float panelY = (float)Math.floor((double)(((float)windowHeight - (panelHeight + bottomReserve)) / 2.0F));
      float offscreen = (float)windowHeight / 2.0F + panelHeight;

      for (int i = 0; i < this.panels.size(); i++) {
         Panel panel = this.panels.get(i);
         panel.slideDir = i % 2 == 0 ? 1 : -1;
         panel.slideAnim.run(this.closing ? 0.0F : 1.0F);
         float slide = MathHelper.clamp(panel.slideAnim.getValue(), 0.0F, 1.0F);
         float yOffset = (1.0F - slide) * (float)panel.slideDir * offscreen;
         panel.setX(startX + (float)i * (panelWidth + spacing));
         panel.setY(panelY + yOffset);
         panel.setWidth(panelWidth);
         panel.setHeight(panelHeight);
         panel.render(context, mouseX, mouseY, delta);
      }

      if (this.closing) {
         boolean allClosed = true;

         for (Panel panel : this.panels) {
            if (panel.slideAnim.getValue() > 0.02F) {
               allClosed = false;
               break;
            }
         }

         if (allClosed) {
            this.closing = false;
            modelView.popMatrix();
            Scissor.resetGuiTransform();
            this.close();
            return;
         }
      }

      float searchW = Math.min(150.0F, Math.max(110.0F, panelWidth * 1.25F));
      float searchH = SEARCH_HEIGHT;
      float searchX = (float)Math.floor((double)((float)windowWidth / 2.0F - searchW / 2.0F));
      float searchY = panelY + panelHeight + 8.0F;
      this.searchField.setBounds(searchX, searchY, searchW, searchH);
      this.searchField.render(context, mouseX, mouseY, delta);
      String hoveredDesc = null;

      for (Panel panelx : this.panels) {
         boolean isMouseInPanel = HoverUtil.isHovered(
            (double)mouseX, (double)mouseY, (double)panelx.getX(), (double)panelx.getY(), (double)panelx.getWidth(), (double)panelx.getHeight()
         );

         for (ModuleComponent component : panelx.getModuleComponents()) {
            if (component.isHovered() && isMouseInPanel && this.searchField.isEmpty()) {
               String desc = component.getModule().getDesc();
               if (desc != null && !desc.isEmpty()) {
                  hoveredDesc = desc;
               }
            }
         }
      }

      if (hoveredDesc != null) {
         this.lastDesc = hoveredDesc;
      }

      this.descAnim.run(hoveredDesc != null);
      float da = this.descAnim.getValue();
      if (da > 0.01F && this.lastDesc != null) {
         float size = 7.5F;
         float padX = 8.0F;
         float padY = 5.5F;
         float textWidth = Fonts.SFREGULAR.get().getWidth(this.lastDesc, size);
         float tooltipW = textWidth + padX * 2.0F;
         float tooltipH = size + padY * 2.0F;
         float tooltipX = MathHelper.clamp((float)windowWidth / 2.0F - tooltipW / 2.0F, 4.0F, (float)windowWidth - tooltipW - 4.0F);
         float tooltipY = panelY - tooltipH - 8.0F;
         int a = (int)(255.0F * da);
         float cx = tooltipX + tooltipW / 2.0F;
         float cy = tooltipY + tooltipH / 2.0F;
         float scale = 0.92F + 0.08F * da;
         context.getMatrices().push();
         context.getMatrices().translate(cx, cy, 0.0F);
         context.getMatrices().scale(scale, scale, 1.0F);
         context.getMatrices().translate(-cx, -cy, 0.0F);
         DrawUtil.drawRoundBlur(tooltipX, tooltipY, tooltipW, tooltipH, 4.0F, ColorProvider.rgba(200, 200, 200, (float)((int)(255.0F * da))), 12.0F);
         DrawUtil.drawRound(tooltipX, tooltipY, tooltipW, tooltipH, 4.0F, ColorProvider.setAlpha(ColorProvider.getColorClickGui(), (int)(130.0F * da)));
         DrawUtil.drawRound(
            tooltipX - 0.5F, tooltipY - 0.5F, tooltipW + 1.0F, tooltipH + 1.0F, 4.5F, ColorProvider.rgba(48, 66, 122, (float)((int)(90.0F * da)))
         );
         DrawUtil.drawText(
            Fonts.SFREGULAR.get(),
            this.lastDesc,
            tooltipX + padX,
            tooltipY + (tooltipH - size) / 2.0F + 0.2F,
            ColorProvider.setAlpha(ColorProvider.getColorText(), a),
            size
         );
         context.getMatrices().pop();
      }

      this.themeEditor.render(context, mouseX, mouseY, delta);
      if (this.itemModelGallery != null) {
         this.itemModelGallery.render(context, mouseX, mouseY, delta);
      }

      modelView.popMatrix();
      Scissor.resetGuiTransform();
      this.ensureCursors();
      long desiredCursor;
      if (CursorManager.shouldBeHand()) {
         desiredCursor = this.handCursor;
      } else if (CursorManager.shouldIBeam()) {
         desiredCursor = this.iBeamCursor;
      } else if (CursorManager.shouldClick()) {
         desiredCursor = this.pointingCursor;
      } else {
         desiredCursor = this.arrowCursor;
      }

      this.applyCursor(desiredCursor);
   }

   public void removed() {
      super.removed();
      if (this.cursorsCreated) {
         GLFW.glfwSetCursor(mc.getWindow().getHandle(), 0L);
         GLFW.glfwDestroyCursor(this.handCursor);
         GLFW.glfwDestroyCursor(this.iBeamCursor);
         GLFW.glfwDestroyCursor(this.pointingCursor);
         GLFW.glfwDestroyCursor(this.arrowCursor);
         this.cursorsCreated = false;
         this.currentCursor = 0L;
      }
   }

   public boolean searchCheck(String text) {
      if (this.searchField.isEmpty()) {
         return false;
      } else {
         String raw = this.searchField.text;
         if (!raw.equals(this.cachedRawQuery)) {
            this.cachedRawQuery = raw;
            this.cachedNormalizedQuery = raw.replaceAll(" ", "").toLowerCase();
         }

         return !text.replaceAll(" ", "").toLowerCase().contains(this.cachedNormalizedQuery);
      }
   }

   public void openItemModelGallery(ItemModelSetting setting) {
      this.itemModelGallery = new ItemModelGalleryPopup(setting);
   }

   private float guiScale() {
      ClickGui module = Instance.get(ClickGui.class);
      return module != null ? (float)module.size.getValue() : 1.0F;
   }

   private double scaleMouseX(double mouseX) {
      float s = this.guiScale();
      double cx = (double)mc.getWindow().getScaledWidth() / 2.0;
      return (mouseX - cx) / (double)s + cx;
   }

   private double scaleMouseY(double mouseY) {
      float s = this.guiScale();
      double cy = (double)mc.getWindow().getScaledHeight() / 2.0;
      return (mouseY - cy) / (double)s + cy;
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      mouseX = this.scaleMouseX(mouseX);
      mouseY = this.scaleMouseY(mouseY);
      if (this.itemModelGallery != null) {
         if (!this.itemModelGallery.contains(mouseX, mouseY)) {
            this.itemModelGallery = null;
         } else {
            this.itemModelGallery.mouseClicked(mouseX, mouseY, button);
         }

         return true;
      } else if (this.themeEditor.mouseClicked(mouseX, mouseY, button)) {
         return true;
      } else {
         this.searchField.mouseClicked(mouseX, mouseY, button);
         if (this.searchField.isEmpty()) {
            for (Panel panel : this.panels) {
               if (HoverUtil.isHovered(mouseX, mouseY, (double)panel.getX(), (double)panel.getY(), (double)panel.getWidth(), (double)panel.getHeight())) {
                  panel.mouseClicked(mouseX, mouseY, button);
               }
            }
         } else {
            for (Panel panelx : this.panels) {
               panelx.mouseClicked(mouseX, mouseY, button);
            }
         }

         return super.mouseClicked(mouseX, mouseY, button);
      }
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      mouseX = this.scaleMouseX(mouseX);
      mouseY = this.scaleMouseY(mouseY);
      if (this.itemModelGallery != null) {
         this.itemModelGallery.mouseReleased(mouseX, mouseY, button);
         return true;
      } else {
         this.themeEditor.mouseReleased(mouseX, mouseY, button);

         for (Panel panel : this.panels) {
            panel.mouseReleased(mouseX, mouseY, button);
         }

         return super.mouseReleased(mouseX, mouseY, button);
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      mouseX = this.scaleMouseX(mouseX);
      mouseY = this.scaleMouseY(mouseY);
      if (this.itemModelGallery != null) {
         this.itemModelGallery.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
         return true;
      } else {
         for (Panel panel : this.panels) {
            panel.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
         }

         return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
      }
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.itemModelGallery != null) {
         if (keyCode == 256) {
            this.itemModelGallery = null;
         } else {
            this.itemModelGallery.keyPressed(keyCode, scanCode, modifiers);
         }

         return true;
      } else {
         boolean anyModuleBinding = false;

         label67:
         for (Panel panel : this.panels) {
            Iterator var7 = panel.getModuleComponents().iterator();

            while (true) {
               if (var7.hasNext()) {
                  ModuleComponent component = (ModuleComponent)var7.next();
                  if (!component.isBinding()) {
                     continue;
                  }

                  anyModuleBinding = true;
               }

               if (anyModuleBinding) {
                  break label67;
               }
               break;
            }
         }

         if (keyCode == 256 && anyModuleBinding) {
            for (Panel panel : this.panels) {
               panel.keyPressed(keyCode, scanCode, modifiers);
            }

            return true;
         } else if (this.searchField.isFocused()) {
            this.searchField.keyPressed(keyCode, scanCode, modifiers);
            return true;
         } else if (keyCode == 256) {
            this.ensureCursors();
            this.applyCursor(this.arrowCursor);
            this.closing = true;
            return true;
         } else {
            for (Panel panel : this.panels) {
               panel.keyPressed(keyCode, scanCode, modifiers);
            }

            return super.keyPressed(keyCode, scanCode, modifiers);
         }
      }
   }

   public boolean charTyped(char chr, int modifiers) {
      if (this.itemModelGallery != null) {
         this.itemModelGallery.charTyped(chr, modifiers);
         return true;
      } else if (this.searchField.isFocused()) {
         this.searchField.charTyped(chr, modifiers);
         return true;
      } else {
         return super.charTyped(chr, modifiers);
      }
   }

   public boolean shouldPause() {
      return false;
   }

   @Generated
   public List<Panel> getPanels() {
      return this.panels;
   }

   @Generated
   public SearchField getSearchField() {
      return this.searchField;
   }

   @Generated
   public ThemeEditor getThemeEditor() {
      return this.themeEditor;
   }

   @Generated
   public ItemModelGalleryPopup getItemModelGallery() {
      return this.itemModelGallery;
   }

   @Generated
   public Animation getDescAnim() {
      return this.descAnim;
   }

   @Generated
   public String getLastDesc() {
      return this.lastDesc;
   }

   @Generated
   public boolean isClosing() {
      return this.closing;
   }

   @Generated
   public long getHandCursor() {
      return this.handCursor;
   }

   @Generated
   public long getIBeamCursor() {
      return this.iBeamCursor;
   }

   @Generated
   public long getPointingCursor() {
      return this.pointingCursor;
   }

   @Generated
   public long getArrowCursor() {
      return this.arrowCursor;
   }

   @Generated
   public boolean isCursorsCreated() {
      return this.cursorsCreated;
   }

   @Generated
   public long getCurrentCursor() {
      return this.currentCursor;
   }

   @Generated
   public String getCachedRawQuery() {
      return this.cachedRawQuery;
   }

   @Generated
   public String getCachedNormalizedQuery() {
      return this.cachedNormalizedQuery;
   }
}
