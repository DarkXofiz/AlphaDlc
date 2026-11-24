package miranware.module.list.misc;

import com.google.common.eventbus.Subscribe;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;
import miranware.event.list.EventPacket;
import miranware.event.list.EventTick;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.ModeSetting;
import miranware.module.settings.SliderSetting;
import miranware.util.player.other.InventoryUtil;

@ModuleInformation(
   moduleName = "ServerJoiner",
   moduleDesc = "Auto join a server through the compass/GUI",
   moduleCategory = ModuleCategory.MISC
)
public class ServerJoiner extends Module {
   private final ModeSetting server = new ModeSetting(
      "Server",
      "ReallyWorld",
      "ReallyWorld",
      "SpookyTime Duels"
   );
   private final SliderSetting griefNumber = new SliderSetting("Grief Number", 1.0, 1.0, 54.0, 1.0)
      .setVisible(() -> this.server.is("ReallyWorld"));
   private final ModeSetting compassName = new ModeSetting(
         "Compass Name",
         "\u0432\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u0440\u0435\u0436\u0438\u043c",
         "\u0432\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u0440\u0435\u0436\u0438\u043c",
         "\u0434\u0443\u044d\u043b\u0438",
         "\u043c\u044f\u0441\u043e\u0440\u0443\u0431\u043a\u0430",
         "\u043a\u0432\u0430\u0434\u0440\u043e\u043c\u044f\u0441\u043e"
      )
      .setVisible(() -> this.server.is("SpookyTime Duels"));
   private long lastActionTime;

   @Subscribe
   private void onTick(EventTick ignored) {
      if (this.isEnabled()) {
         if (this.mc.getWindow() != null && GLFW.glfwGetKey(this.mc.getWindow().getHandle(), 260) == 1) {
            this.setEnabled(false);
         } else if (this.mc.player != null && this.mc.world != null) {
            if (this.mc.currentScreen == null) {
               if (this.server.is("SpookyTime Duels")) {
                  if (System.currentTimeMillis() - this.lastActionTime > 250L) {
                     this.useCompass();
                  }
               } else if (this.mc.player.age < 5) {
                  this.useCompass();
               }
            } else {
               if (this.mc.currentScreen instanceof GenericContainerScreen container) {
                  this.navigateGui(container);
               }
            }
         }
      }
   }

   private void navigateGui(GenericContainerScreen container) {
      GenericContainerScreenHandler handler = (GenericContainerScreenHandler)container.getScreenHandler();
      String title = container.getTitle().getString().toLowerCase();

      for (int i = 0; i < handler.slots.size(); i++) {
         if (((Slot)handler.slots.get(i)).hasStack()) {
            String slotName = ((Slot)handler.slots.get(i)).getStack().getName().getString().toLowerCase();
            if (this.server.is("ReallyWorld")) {
               if (slotName.contains("\u0433\u0440\u0438\u0444\u0435\u0440\u0441\u043a\u043e\u0435 \u0432\u044b\u0436\u0438\u0432\u0430\u043d\u0438\u0435")
                  && this.canClick()) {
                  this.click(handler.syncId, i, SlotActionType.PICKUP);
               }

               int number = this.griefNumber.getIntValue();
               if (slotName.contains("\u0433\u0440\u0438\u0444 #" + number) && this.canClick()) {
                  this.click(handler.syncId, i, SlotActionType.PICKUP);
               }
            } else if (this.server.is("SpookyTime Duels")
               && title.contains("\u0432\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u0440\u0435\u0436\u0438\u043c")
               && this.canClick()) {
               this.click(handler.syncId, 14, SlotActionType.QUICK_MOVE);
            }
         }
      }
   }

   private void click(int syncId, int slot, SlotActionType action) {
      if (this.mc.interactionManager != null && this.mc.player != null) {
         this.mc.interactionManager.clickSlot(syncId, slot, 0, action, this.mc.player);
         this.lastActionTime = System.currentTimeMillis();
      }
   }

   private void useCompass() {
      if (this.mc.player != null && this.mc.world != null && this.mc.getNetworkHandler() != null && this.mc.interactionManager != null) {
         int slot = InventoryUtil.searchItemHotbar(Items.COMPASS);
         if (slot != -1) {
            this.mc.player.getInventory().selectedSlot = slot;
            this.mc.interactionManager.syncSelectedSlot();
            this.mc.interactionManager.interactItem(this.mc.player, Hand.MAIN_HAND);
            this.lastActionTime = System.currentTimeMillis();
         }
      }
   }

   @Subscribe
   private void onPacket(EventPacket e) {
      if (this.isEnabled() && e.getType() == EventPacket.Type.RECEIVE) {
         if (e.getPacket() instanceof DisconnectS2CPacket packet) {
            String message = packet.reason().getString().toLowerCase();
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
