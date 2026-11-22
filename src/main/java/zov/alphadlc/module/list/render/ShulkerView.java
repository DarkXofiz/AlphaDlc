package zov.alphadlc.module.list.render;

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
import zov.alphadlc.event.list.EventHUD;
import zov.alphadlc.event.list.EventHandledScreen;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.module.settings.SliderSetting;
import zov.alphadlc.util.render.math.ProjectionUtil;
import zov.alphadlc.util.render.providers.ColorProvider;
import zov.alphadlc.util.render.renderers.DrawUtil;

@ModuleInformation(
   moduleName = "ShulkerView",
   moduleDesc = "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0435\u0442 \u0441\u043e\u0434\u0435\u0440\u0436\u0438\u043c\u043e\u0435 \u0448\u0430\u043b\u043a\u0435\u0440\u043e\u0432 \u0438 \u043a\u043e\u043d\u0442\u0435\u0439\u043d\u0435\u0440\u043e\u0432",
   moduleCategory = ModuleCategory.RENDER
)
public class ShulkerView extends Module {
   private final BooleanSetting showHover = new BooleanSetting("\u0412 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u0435", true);
   private final BooleanSetting showGround = new BooleanSetting("\u041d\u0430 \u0437\u0435\u043c\u043b\u0435", true);
   private final SliderSetting scale = new SliderSetting("\u041c\u0430\u0441\u0448\u0442\u0430\u0431", 0.75, 0.4, 1.5, 0.05);
   private static final int CELL = 18;
   private static final int COLS = 9;
   private static final float PAD = 3.0F;

   @Subscribe
   private void onHandledScreen(EventHandledScreen e) {
      if (this.showHover.getValue()) {
         Slot slot = e.getSlotHover();
         if (slot != null && slot.method_7681()) {
            List<ItemStack> contents = this.getContents(slot.method_7677());
            if (!contents.isEmpty()) {
               float sc = this.scale.getFloatValue();
               float w = 168.0F * sc;
               float h = ((float)(this.rows(contents.size()) * 18) + 6.0F) * sc;
               float x = (float)(e.getMouseX() + 8);
               float y = (float)(e.getMouseY() + 8);
               if (x + w > (float)this.mc.method_22683().method_4486()) {
                  x = (float)e.getMouseX() - w - 8.0F;
               }

               if (y + h > (float)this.mc.method_22683().method_4502()) {
                  y = (float)e.getMouseY() - h - 8.0F;
               }

               this.draw(e.getDrawContext(), contents, x, y, sc);
            }
         }
      }
   }

   @Subscribe
   private void onHud(EventHUD e) {
      if (this.showGround.getValue() && this.mc.field_1687 != null) {
         for (Entity entity : this.mc.field_1687.method_18112()) {
            if (entity instanceof ItemEntity) {
               ItemEntity item = (ItemEntity)entity;
               List<ItemStack> contents = this.getContents(item.method_6983());
               if (!contents.isEmpty()) {
                  Vector2f pos = ProjectionUtil.project(entity.method_19538().method_1031(0.0, (double)entity.method_17682() + 0.3, 0.0));
                  if (pos.method_32118() != Float.MAX_VALUE) {
                     float sc = this.scale.getFloatValue() * 0.6F;
                     float w = 168.0F * sc;
                     this.draw(e.getDrawContext(), contents, pos.method_32118() - w / 2.0F, pos.method_32119(), sc);
                  }
               }
            }
         }
      }
   }

   private List<ItemStack> getContents(ItemStack stack) {
      ContainerComponent component = (ContainerComponent)stack.method_57824(DataComponentTypes.field_49622);
      return component != null ? component.method_57489().toList() : List.of();
   }

   private int rows(int count) {
      return Math.max(1, (int)Math.ceil((double)count / 9.0));
   }

   private void draw(DrawContext ctx, List<ItemStack> stacks, float x, float y, float sc) {
      float w = 168.0F * sc;
      float h = ((float)(this.rows(stacks.size()) * 18) + 6.0F) * sc;
      DrawUtil.drawRound(x, y, w, h, 4.0F, ColorProvider.rgba(18, 18, 24, 225.0F));
      ctx.method_51448().method_22903();
      ctx.method_51448().method_46416(x + 3.0F * sc, y + 3.0F * sc, 400.0F);
      ctx.method_51448().method_22905(sc, sc, 1.0F);
      int i = 0;

      for (ItemStack stack : stacks) {
         int cx = i % 9 * 18;
         int cy = i / 9 * 18;
         ctx.method_51427(stack, cx, cy);
         ctx.method_51431(this.mc.field_1772, stack, cx, cy);
         i++;
      }

      ctx.method_51448().method_22909();
   }
}
