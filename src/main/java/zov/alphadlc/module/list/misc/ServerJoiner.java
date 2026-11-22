package zov.alphadlc.module.list.misc;

import com.google.common.eventbus.Subscribe;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;
import zov.alphadlc.event.list.EventPacket;
import zov.alphadlc.event.list.EventTick;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.ModeSetting;
import zov.alphadlc.module.settings.SliderSetting;
import zov.alphadlc.util.player.other.InventoryUtil;

@ModuleInformation(
   moduleName = "ServerJoiner",
   moduleDesc = "\u0410\u0432\u0442\u043e\u0432\u0445\u043e\u0434 \u043d\u0430 \u0441\u0435\u0440\u0432\u0435\u0440 \u0447\u0435\u0440\u0435\u0437 \u043a\u043e\u043c\u043f\u0430\u0441/GUI",
   moduleCategory = ModuleCategory.MISC
)
public class ServerJoiner extends Module {
   private final ModeSetting server = new ModeSetting(
      "\u0421\u0435\u0440\u0432\u0435\u0440",
      "\u0420\u0438\u043b\u043b\u0438\u0412\u043e\u0440\u043b\u0434",
      "\u0420\u0438\u043b\u043b\u0438\u0412\u043e\u0440\u043b\u0434",
      "\u0421\u043f\u0443\u043a\u0438\u0422\u0430\u0439\u043c \u0434\u0443\u044d\u043b\u0438"
   );
   private final SliderSetting griefNumber = new SliderSetting("\u041d\u043e\u043c\u0435\u0440 \u0433\u0440\u0438\u0444\u0430", 1.0, 1.0, 54.0, 1.0)
      .setVisible(() -> this.server.is("\u0420\u0438\u043b\u043b\u0438\u0412\u043e\u0440\u043b\u0434"));
   private final ModeSetting compassName = new ModeSetting(
         "\u041d\u0430\u0437\u0432\u0430\u043d\u0438\u0435 \u043a\u043e\u043c\u043f\u0430\u0441\u0430",
         "\u0432\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u0440\u0435\u0436\u0438\u043c",
         "\u0432\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u0440\u0435\u0436\u0438\u043c",
         "\u0434\u0443\u044d\u043b\u0438",
         "\u043c\u044f\u0441\u043e\u0440\u0443\u0431\u043a\u0430",
         "\u043a\u0432\u0430\u0434\u0440\u043e\u043c\u044f\u0441\u043e"
      )
      .setVisible(() -> this.server.is("\u0421\u043f\u0443\u043a\u0438\u0422\u0430\u0439\u043c \u0434\u0443\u044d\u043b\u0438"));
   private long lastActionTime;

   @Subscribe
   private void onTick(EventTick ignored) {
      if (this.isEnabled()) {
         if (this.mc.method_22683() != null && GLFW.glfwGetKey(this.mc.method_22683().method_4490(), 260) == 1) {
            this.setEnabled(false);
         } else if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
            if (this.mc.field_1755 == null) {
               if (this.server.is("\u0421\u043f\u0443\u043a\u0438\u0422\u0430\u0439\u043c \u0434\u0443\u044d\u043b\u0438")) {
                  if (System.currentTimeMillis() - this.lastActionTime > 250L) {
                     this.useCompass();
                  }
               } else if (this.mc.field_1724.field_6012 < 5) {
                  this.useCompass();
               }
            } else {
               if (this.mc.field_1755 instanceof GenericContainerScreen container) {
                  this.navigateGui(container);
               }
            }
         }
      }
   }

   private void navigateGui(GenericContainerScreen container) {
      GenericContainerScreenHandler handler = (GenericContainerScreenHandler)container.method_17577();
      String title = container.method_25440().getString().toLowerCase();

      for (int i = 0; i < handler.field_7761.size(); i++) {
         if (((Slot)handler.field_7761.get(i)).method_7681()) {
            String slotName = ((Slot)handler.field_7761.get(i)).method_7677().method_7964().getString().toLowerCase();
            if (this.server.is("\u0420\u0438\u043b\u043b\u0438\u0412\u043e\u0440\u043b\u0434")) {
               if (slotName.contains("\u0433\u0440\u0438\u0444\u0435\u0440\u0441\u043a\u043e\u0435 \u0432\u044b\u0436\u0438\u0432\u0430\u043d\u0438\u0435")
                  && this.canClick()) {
                  this.click(handler.field_7763, i, SlotActionType.field_7790);
               }

               int number = this.griefNumber.getIntValue();
               if (slotName.contains("\u0433\u0440\u0438\u0444 #" + number) && this.canClick()) {
                  this.click(handler.field_7763, i, SlotActionType.field_7790);
               }
            } else if (this.server.is("\u0421\u043f\u0443\u043a\u0438\u0422\u0430\u0439\u043c \u0434\u0443\u044d\u043b\u0438")
               && title.contains("\u0432\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u0440\u0435\u0436\u0438\u043c")
               && this.canClick()) {
               this.click(handler.field_7763, 14, SlotActionType.field_7794);
            }
         }
      }
   }

   private void click(int syncId, int slot, SlotActionType action) {
      if (this.mc.field_1761 != null && this.mc.field_1724 != null) {
         this.mc.field_1761.method_2906(syncId, slot, 0, action, this.mc.field_1724);
         this.lastActionTime = System.currentTimeMillis();
      }
   }

   private void useCompass() {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null && this.mc.method_1562() != null && this.mc.field_1761 != null) {
         int slot = InventoryUtil.searchItemHotbar(Items.field_8251);
         if (slot != -1) {
            this.mc.field_1724.method_31548().field_7545 = slot;
            this.mc.field_1761.method_2911();
            this.mc.field_1761.method_2919(this.mc.field_1724, Hand.field_5808);
            this.lastActionTime = System.currentTimeMillis();
         }
      }
   }

   @Subscribe
   private void onPacket(EventPacket e) {
      if (this.isEnabled() && e.getType() == EventPacket.Type.RECEIVE) {
         if (e.getPacket() instanceof DisconnectS2CPacket packet) {
            String message = packet.comp_2325().getString().toLowerCase();
            boolean retry = message.contains("\u0441\u0435\u0440\u0432\u0435\u0440 \u043f\u0435\u0440\u0435\u043f\u043e\u043b\u043d\u0435\u043d")
               || message.contains("\u043f\u043e\u0434\u043e\u0436\u0434\u0438\u0442\u0435")
               || message.contains("\u0432\u044b \u0443\u0436\u0435 \u043f\u043e\u0434\u043a\u043b\u044e\u0447\u0435\u043d\u044b")
               || message.contains("\u0432\u044b \u0431\u044b\u043b\u0438 \u043a\u0438\u043a\u043d\u0443\u0442\u044b")
               || message.contains("\u0431\u043e\u043b\u044c\u0448\u043e\u0439 \u043f\u043e\u0442\u043e\u043a \u0438\u0433\u0440\u043e\u043a\u043e\u0432")
               || message.contains("\u0441\u0435\u0440\u0432\u0435\u0440 \u0437\u0430\u043f\u043e\u043b\u043d\u0435\u043d");
            if (retry) {
               this.useCompass();
            }
         }
      }
   }

   private boolean canClick() {
      return System.currentTimeMillis() - this.lastActionTime > 50L;
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.lastActionTime = 0L;
   }
}
