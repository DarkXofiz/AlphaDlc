package zov.alphadlc.module.list.movement;

import com.google.common.eventbus.Subscribe;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import zov.alphadlc.event.list.EventCloseInv;
import zov.alphadlc.event.list.EventPacket;
import zov.alphadlc.event.list.EventPlayerUpdate;
import zov.alphadlc.event.list.MoveInputEvent;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.ModeSetting;
import zov.alphadlc.util.packet.NetworkUtils;
import zov.alphadlc.util.player.move.MoveUtil;

@ModuleInformation(
   moduleName = "Gui Move",
   moduleDesc = "\u0412\u0437\u0430\u0438\u043c\u043e\u0434\u0435\u0439\u0441\u0442\u0432\u0438\u0435 \u0441 \u0438\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u0451\u043c \u043f\u0440\u0438 \u0434\u0432\u0438\u0436\u0435\u043d\u0438\u0438",
   moduleCategory = ModuleCategory.MOVEMENT
)
public class GuiMove extends Module {
   private final ModeSetting mode = new ModeSetting(
      "\u041e\u0431\u0445\u043e\u0434",
      "\u041b\u0435\u0433\u0438\u0442",
      "Vanilla",
      "Reallyworld",
      "Funtime",
      "Holyworld",
      "SpookyTime",
      "LonyGrief",
      "\u041b\u0435\u0433\u0438\u0442"
   );
   private final List<Packet<?>> pending = new ArrayList<>();
   private int freezeTicks;
   private boolean flushPending;

   @Override
   public void onDisable() {
      super.onDisable();
      this.flush();
      this.pending.clear();
      this.freezeTicks = 0;
      this.flushPending = false;
   }

   private boolean isVanilla() {
      return this.mode.is("Vanilla");
   }

   private boolean isLegit() {
      return this.mode.is("\u041b\u0435\u0433\u0438\u0442");
   }

   private boolean usesBypass() {
      return !this.isVanilla() && !this.isLegit();
   }

   private int freezeWindow() {
      String var1 = this.mode.getValue();

      return switch (var1) {
         case "Reallyworld" -> 3;
         case "Funtime" -> 4;
         case "Holyworld" -> 5;
         case "SpookyTime" -> 3;
         case "LonyGrief" -> 2;
         default -> 0;
      };
   }

   @Subscribe
   private void onPacket(EventPacket e) {
      if (this.mc.field_1724 != null && !this.isVanilla()) {
         if (this.mc.field_1755 != null && !(this.mc.field_1755 instanceof ChatScreen)) {
            if (e.getType() == EventPacket.Type.RECEIVE && e.getPacket() instanceof CloseScreenS2CPacket && this.usesBypass() && !this.pending.isEmpty()) {
               e.cancelEvent();
            } else if (e.getType() == EventPacket.Type.SEND) {
               if (e.getPacket() instanceof ClickSlotC2SPacket click) {
                  if (this.mc.field_1755 instanceof InventoryScreen) {
                     if (MoveUtil.hasPlayerMovement()) {
                        if (this.isLegit()) {
                           e.cancelEvent();
                        } else {
                           this.pending.add(click);
                           e.cancelEvent();
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @Subscribe
   private void onCloseInv(EventCloseInv e) {
      if (this.mc.field_1724 != null && !this.isVanilla() && !this.isLegit()) {
         if (!this.pending.isEmpty()) {
            e.cancelEvent();
            this.flushPending = true;
            this.freezeTicks = this.freezeWindow();
         }
      }
   }

   @Subscribe
   private void onMoveInput(MoveInputEvent e) {
      if (this.freezeTicks > 0) {
         e.cancel();
      }
   }

   @Subscribe
   private void onUpdate(EventPlayerUpdate ignored) {
      if (this.mc.field_1724 != null && !this.isVanilla()) {
         if (this.freezeTicks > 0) {
            this.freezeTicks--;
            if (this.freezeTicks == 0 && this.flushPending) {
               this.flush();
               NetworkUtils.sendSilentPacket(new CloseHandledScreenC2SPacket(this.mc.field_1724.field_7512.field_7763));
               this.flushPending = false;
            }
         }

         if (this.mc.field_1755 != null && !(this.mc.field_1755 instanceof ChatScreen)) {
            for (KeyBinding key : new KeyBinding[]{
               this.mc.field_1690.field_1894,
               this.mc.field_1690.field_1881,
               this.mc.field_1690.field_1913,
               this.mc.field_1690.field_1849,
               this.mc.field_1690.field_1903
            }) {
               key.method_23481(InputUtil.method_15987(this.mc.method_22683().method_4490(), InputUtil.method_15981(key.method_1428()).method_1444()));
            }
         }
      }
   }

   private void flush() {
      if (!this.pending.isEmpty()) {
         this.pending.forEach(NetworkUtils::sendSilentPacket);
         this.pending.clear();
      }
   }
}
