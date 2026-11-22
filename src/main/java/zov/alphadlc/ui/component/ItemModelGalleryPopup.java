package zov.alphadlc.ui.component;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import zov.alphadlc.module.settings.ItemModelSetting;
import zov.alphadlc.util.cursor.CursorManager;
import zov.alphadlc.util.render.helper.HoverUtil;
import zov.alphadlc.util.render.math.Scissor;
import zov.alphadlc.util.render.msdf.Fonts;
import zov.alphadlc.util.render.providers.ColorProvider;
import zov.alphadlc.util.render.renderers.DrawUtil;

public class ItemModelGalleryPopup implements IComponent {
   private static final int COLUMNS = 6;
   private static final float WIDTH = 380.0F;
   private static final float MAX_HEIGHT = 292.0F;
   private static final float PADDING = 10.0F;
   private static final float CELL_GAP = 4.0F;
   private static final float CELL_HEIGHT = 45.0F;
   private static final float ROW_PITCH = 49.0F;
   private final ItemModelSetting setting;
   private final SearchField searchField = new SearchField("Search models...");
   private final List<String> filteredModels = new ArrayList<>();
   private String appliedQuery = "";
   private String hoveredModel;
   private float x;
   private float y;
   private float width;
   private float height;
   private float gridX;
   private float gridY;
   private float gridWidth;
   private float gridHeight;
   private float scroll;

   public ItemModelGalleryPopup(ItemModelSetting setting) {
      this.setting = setting;
      this.filteredModels.addAll(setting.getModes());
      this.searchField.resetAppear();
   }

   @Override
   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      int screenWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
      int screenHeight = MinecraftClient.getInstance().getWindow().getScaledHeight();
      this.width = Math.min(380.0F, (float)screenWidth - 20.0F);
      this.height = Math.min(292.0F, (float)screenHeight - 20.0F);
      this.x = ((float)screenWidth - this.width) / 2.0F;
      this.y = ((float)screenHeight - this.height) / 2.0F;
      this.updateFilter();
      DrawUtil.drawRound(0.0F, 0.0F, (float)screenWidth, (float)screenHeight, 0.0F, ColorProvider.rgba(0, 0, 0, 95.0F));
      DrawUtil.drawRoundBlur(this.x, this.y, this.width, this.height, 7.0F, ColorProvider.rgba(200, 200, 200, 255.0F), 16.0F);
      DrawUtil.drawRound(this.x - 1.0F, this.y - 1.0F, this.width + 2.0F, this.height + 2.0F, 7.5F, ColorProvider.rgba(48, 66, 122, 145.0F));
      DrawUtil.drawRound(this.x, this.y, this.width, this.height, 7.0F, ColorProvider.setAlpha(ColorProvider.getColorClickGui(), 235));
      DrawUtil.drawText(Fonts.SFMEDIUM.get(), "Item Replacer models", this.x + 10.0F, this.y + 9.0F, ColorProvider.getColorText(), 9.0F);
      String count = this.filteredModels.size() + " / " + this.setting.getModes().size();
      float countWidth = Fonts.SFREGULAR.get().getWidth(count, 7.0F);
      DrawUtil.drawText(Fonts.SFREGULAR.get(), count, this.x + this.width - 10.0F - countWidth, this.y + 10.0F, ColorProvider.getColorInactiveText(), 7.0F);
      this.searchField.setBounds(this.x + 10.0F, this.y + 25.0F, this.width - 20.0F, 18.0F);
      this.searchField.render(context, mouseX, mouseY, delta);
      this.gridX = this.x + 10.0F;
      this.gridY = this.y + 50.0F;
      this.gridWidth = this.width - 20.0F;
      this.gridHeight = Math.max(1.0F, this.height - 75.0F);
      this.hoveredModel = null;
      Scissor.push();
      Scissor.setFromComponentCoordinates((double)this.gridX, (double)this.gridY, (double)this.gridWidth, (double)this.gridHeight);
      this.renderVisibleRows(context, mouseX, mouseY);
      Scissor.unset();
      Scissor.pop();
      this.renderScrollbar();
      this.renderFooter();
   }

   private void updateFilter() {
      String query = this.searchField.text.trim().toLowerCase(Locale.ROOT);
      if (!query.equals(this.appliedQuery)) {
         this.appliedQuery = query;
         this.filteredModels.clear();

         for (String model : this.setting.getModes()) {
            if (query.isEmpty() || model.toLowerCase(Locale.ROOT).contains(query)) {
               this.filteredModels.add(model);
            }
         }

         this.scroll = 0.0F;
      }
   }

   private void renderVisibleRows(DrawContext context, int mouseX, int mouseY) {
      if (this.filteredModels.isEmpty()) {
         String empty = "No matching models";
         float textWidth = Fonts.SFREGULAR.get().getWidth(empty, 8.0F);
         DrawUtil.drawText(
            Fonts.SFREGULAR.get(),
            empty,
            this.gridX + (this.gridWidth - textWidth) / 2.0F,
            this.gridY + this.gridHeight / 2.0F - 4.0F,
            ColorProvider.getColorInactiveText(),
            8.0F
         );
      } else {
         float maxScroll = this.maxScroll();
         this.scroll = MathHelper.clamp(this.scroll, 0.0F, maxScroll);
         float cellWidth = (this.gridWidth - 20.0F) / 6.0F;
         int rowCount = this.rowCount();
         int firstRow = Math.max(0, (int)Math.floor((double)(this.scroll / 49.0F)));
         int lastRow = Math.min(rowCount - 1, (int)Math.floor((double)((this.scroll + this.gridHeight - 0.001F) / 49.0F)));

         for (int row = firstRow; row <= lastRow; row++) {
            float cellY = this.gridY + (float)row * 49.0F - this.scroll;

            for (int column = 0; column < 6; column++) {
               int index = row * 6 + column;
               if (index >= this.filteredModels.size()) {
                  break;
               }

               String model = this.filteredModels.get(index);
               float cellX = this.gridX + (float)column * (cellWidth + 4.0F);
               this.renderCell(context, model, cellX, cellY, cellWidth, mouseX, mouseY);
            }
         }
      }
   }

   private void renderCell(DrawContext context, String model, float cellX, float cellY, float cellWidth, int mouseX, int mouseY) {
      boolean hovered = HoverUtil.isHovered((double)mouseX, (double)mouseY, (double)cellX, (double)cellY, (double)cellWidth, 45.0)
         && HoverUtil.isHovered((double)mouseX, (double)mouseY, (double)this.gridX, (double)this.gridY, (double)this.gridWidth, (double)this.gridHeight);
      boolean selected = this.setting.is(model);
      if (hovered) {
         this.hoveredModel = model;
         CursorManager.requestHand();
      }

      int outline = selected
         ? ColorProvider.setAlpha(ColorProvider.getColorClient(), 255)
         : (hovered ? ColorProvider.rgba(100, 130, 220, 180.0F) : ColorProvider.rgba(48, 66, 122, 90.0F));
      float outlineSize = selected ? 1.5F : 0.75F;
      DrawUtil.drawRound(cellX - outlineSize, cellY - outlineSize, cellWidth + outlineSize * 2.0F, 45.0F + outlineSize * 2.0F, 4.0F, outline);
      DrawUtil.drawRound(
         cellX, cellY, cellWidth, 45.0F, 3.0F, ColorProvider.setAlpha(hovered ? ColorProvider.getColorButton() : ColorProvider.getColorInactiveButton(), 205)
      );
      ItemStack preview = this.setting.getPreviewStack(model);
      if (preview != null) {
         float iconSize = 31.0F;
         float scale = iconSize / 16.0F;
         context.getMatrices().push();
         context.getMatrices().translate(cellX + (cellWidth - iconSize) / 2.0F, cellY + (45.0F - iconSize) / 2.0F, 100.0F);
         context.getMatrices().scale(scale, scale, 1.0F);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         context.drawItem(preview, 0, 0);
         context.getMatrices().pop();
      }
   }

   private void renderScrollbar() {
      float maxScroll = this.maxScroll();
      if (!(maxScroll <= 0.0F)) {
         float contentHeight = (float)this.rowCount() * 49.0F - 4.0F;
         float trackX = this.x + this.width - 4.0F;
         float thumbHeight = Math.max(18.0F, this.gridHeight * (this.gridHeight / contentHeight));
         float thumbY = this.gridY + (this.gridHeight - thumbHeight) * (this.scroll / maxScroll);
         DrawUtil.drawRound(trackX, this.gridY, 1.5F, this.gridHeight, 0.75F, ColorProvider.rgba(48, 66, 122, 100.0F));
         DrawUtil.drawRound(trackX, thumbY, 1.5F, thumbHeight, 0.75F, ColorProvider.setAlpha(ColorProvider.getColorClient(), 210));
      }
   }

   private void renderFooter() {
      String label = this.hoveredModel != null ? this.hoveredModel : "Esc or click outside to close";
      float textWidth = Fonts.SFREGULAR.get().getWidth(label, 7.0F);
      DrawUtil.drawText(
         Fonts.SFREGULAR.get(),
         label,
         this.x + (this.width - textWidth) / 2.0F,
         this.y + this.height - 14.0F,
         this.hoveredModel != null ? ColorProvider.getColorText() : ColorProvider.getColorInactiveText(),
         7.0F
      );
   }

   @Override
   public void mouseClicked(double mouseX, double mouseY, int button) {
      this.searchField.mouseClicked(mouseX, mouseY, button);
      if (button == 0 && HoverUtil.isHovered(mouseX, mouseY, (double)this.gridX, (double)this.gridY, (double)this.gridWidth, (double)this.gridHeight)) {
         float cellWidth = (this.gridWidth - 20.0F) / 6.0F;
         int row = (int)Math.floor((mouseY - (double)this.gridY + (double)this.scroll) / 49.0);
         int column = (int)Math.floor((mouseX - (double)this.gridX) / (double)(cellWidth + 4.0F));
         if (column >= 0 && column < 6) {
            float localX = (float)(mouseX - (double)this.gridX - (double)((float)column * (cellWidth + 4.0F)));
            float localY = (float)(mouseY - (double)this.gridY + (double)this.scroll - (double)((float)row * 49.0F));
            if (!(localX > cellWidth) && !(localY > 45.0F)) {
               int index = row * 6 + column;
               if (index >= 0 && index < this.filteredModels.size()) {
                  this.setting.setValue(this.filteredModels.get(index));
               }
            }
         }
      }
   }

   @Override
   public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      if (HoverUtil.isHovered(mouseX, mouseY, (double)this.gridX, (double)this.gridY, (double)this.gridWidth, (double)this.gridHeight)) {
         this.scroll = MathHelper.clamp(this.scroll - (float)verticalAmount * 28.0F, 0.0F, this.maxScroll());
      }
   }

   @Override
   public void keyPressed(int keyCode, int scanCode, int modifiers) {
      this.searchField.keyPressed(keyCode, scanCode, modifiers);
   }

   @Override
   public void charTyped(char chr, int modifiers) {
      this.searchField.charTyped(chr, modifiers);
   }

   public boolean contains(double mouseX, double mouseY) {
      return HoverUtil.isHovered(mouseX, mouseY, (double)this.x, (double)this.y, (double)this.width, (double)this.height);
   }

   private int rowCount() {
      return (this.filteredModels.size() + 6 - 1) / 6;
   }

   private float maxScroll() {
      float contentHeight = Math.max(0.0F, (float)this.rowCount() * 49.0F - 4.0F);
      return Math.max(0.0F, contentHeight - this.gridHeight);
   }
}
