package miranware.module.list.render;

import com.google.common.eventbus.Subscribe;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import miranware.event.list.EventHUD;
import miranware.event.list.EventHandledScreen;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.BooleanSetting;
import miranware.module.settings.SliderSetting;
import miranware.util.render.math.ProjectionUtil;
import miranware.util.render.providers.ColorProvider;
import miranware.util.render.renderers.DrawUtil;

@ModuleInformation(
   moduleName = "ShulkerView",
   moduleDesc = "Shows the contents of shulkers and containers",
   moduleCategory = ModuleCategory.RENDER
)
public class ShulkerView extends Module {
   private final BooleanSetting showGround = new BooleanSetting("On Ground", true);
   private final SliderSetting scale = new SliderSetting("Scale", 0.75, 0.4, 1.5, 0.05);
   private static final int CELL = 18;
   private static final int COLS = 9;
   private static final float PAD = 3.0F;

   @Subscribe
   private void onHud(EventHUD e) {
      if (this.showGround.getValue() && this.mc.world != null) {
         for (Entity entity : this.mc.world.getEntities()) {
            if (entity instanceof ItemEntity) {
               ItemEntity item = (ItemEntity)entity;
               List<ItemStack> contents = this.getContents(item.getStack());
               if (!contents.isEmpty()) {
                  Vector2f pos = ProjectionUtil.project(entity.getPos().add(0.0, (double)entity.getHeight() + 0.3, 0.0));
                  if (pos.getX() != Float.MAX_VALUE) {
                     float sc = this.scale.getFloatValue() * 0.6F;
                     float w = 168.0F * sc;
                     this.draw(e.getDrawContext(), contents, pos.getX() - w / 2.0F, pos.getY(), sc);
                  }
               }
            }
         }
      }
   }

   private List<ItemStack> getContents(ItemStack stack) {
      ContainerComponent component = (ContainerComponent)stack.get(DataComponentTypes.CONTAINER);
      return component != null ? component.stream().toList() : List.of();
   }

   private int rows(int count) {
      return Math.max(1, (int)Math.ceil((double)count / 9.0));
   }

   private void draw(DrawContext ctx, List<ItemStack> stacks, float x, float y, float sc) {
      float w = 168.0F * sc;
      float h = ((float)(this.rows(stacks.size()) * 18) + 6.0F) * sc;
      DrawUtil.drawRound(x, y, w, h, 4.0F, ColorProvider.rgba(18, 18, 24, 225.0F));
      ctx.getMatrices().push();
      ctx.getMatrices().translate(x + 3.0F * sc, y + 3.0F * sc, 400.0F);
      ctx.getMatrices().scale(sc, sc, 1.0F);
      int i = 0;

      for (ItemStack stack : stacks) {
         int cx = i % 9 * 18;
         int cy = i / 9 * 18;
         ctx.drawItem(stack, cx, cy);
         ctx.drawStackOverlay(this.mc.textRenderer, stack, cx, cy);
         i++;
      }

      ctx.getMatrices().pop();
   }
}
