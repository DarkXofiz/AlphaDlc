package zov.alphadlc.module.list.misc;

import com.google.common.eventbus.Subscribe;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import zov.alphadlc.event.list.EventPacket;
import zov.alphadlc.event.list.EventTick;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.BooleanSetting;

@ModuleInformation(
   moduleName = "Well Helper",
   moduleDesc = "\u041f\u043e\u043c\u043e\u0449\u043d\u0438\u043a \u0434\u043b\u044f Well",
   moduleCategory = ModuleCategory.MISC
)
public class WellHelper extends Module {
   private final BooleanSetting invseeElytraCheck = new BooleanSetting("Invsee \u044d\u043b\u0438\u0442\u0440\u044b", true);
   private final List<String> pendingPlayers = new ArrayList<>();
   private String currentInvseePlayer = null;
   private boolean waitingForInvsee = false;
   private boolean collectingPlayers = false;
   private int tickDelay = 0;
   private int collectTimeout = 0;
   private static final Pattern PLAYER_PATTERN = Pattern.compile("([a-zA-Z0-9_]{3,16})\\s+\\d+");

   @Subscribe
   private void onPacket(EventPacket e) {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
         if (e.getType() == EventPacket.Type.RECEIVE) {
            if (e.getPacket() instanceof GameMessageS2CPacket p && this.invseeElytraCheck.getValue()) {
               String text = p.comp_763().getString();
               if (text.toLowerCase().contains("\u0438\u0433\u0440\u043e\u043a\u0438 \u0440\u044f\u0434\u043e\u043c")) {
                  this.pendingPlayers.clear();
                  this.currentInvseePlayer = null;
                  this.waitingForInvsee = false;
                  this.collectingPlayers = true;
                  this.collectTimeout = 6;
                  return;
               }

               if (this.collectingPlayers) {
                  Matcher matcher = PLAYER_PATTERN.matcher(text);
                  if (matcher.find()) {
                     String playerName = matcher.group(1);
                     if (!playerName.equalsIgnoreCase(this.mc.field_1724.method_5477().getString()) && !this.pendingPlayers.contains(playerName)) {
                        this.pendingPlayers.add(playerName);
                     }

                     this.collectTimeout = 6;
                  }
               }
            }
         }
      }
   }

   @Subscribe
   private void onTick(EventTick e) {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null && this.invseeElytraCheck.getValue()) {
         if (this.collectingPlayers) {
            this.collectTimeout--;
            if (this.collectTimeout <= 0) {
               this.collectingPlayers = false;
               if (!this.pendingPlayers.isEmpty()) {
                  this.tickDelay = 10;
               }
            }
         } else if (this.tickDelay > 0) {
            this.tickDelay--;
            if (this.tickDelay == 0 && !this.pendingPlayers.isEmpty() && !this.waitingForInvsee) {
               this.checkNextPlayer();
            }
         } else {
            if (this.waitingForInvsee && this.currentInvseePlayer != null && this.mc.field_1755 instanceof GenericContainerScreen screen) {
               boolean hasElytra = false;
               int containerSlots = ((GenericContainerScreenHandler)screen.method_17577()).method_17388() * 9;

               for (int i = 0; i < containerSlots; i++) {
                  Slot slot = (Slot)((GenericContainerScreenHandler)screen.method_17577()).field_7761.get(i);
                  ItemStack stack = slot.method_7677();
                  if (stack.method_7909() == Items.field_8833) {
                     hasElytra = true;
                     break;
                  }
               }

               if (hasElytra) {
                  this.logDirect(
                     new Text[]{
                        Text.method_43470(this.currentInvseePlayer)
                           .method_27692(Formatting.field_1060)
                           .method_10852(
                              Text.method_43470(" \u0438\u043c\u0435\u0435\u0442 \u042d\u041b\u0418\u0422\u0420\u042b!").method_27692(Formatting.field_1076)
                           )
                     }
                  );
               }

               this.mc.field_1724.method_7346();
               this.currentInvseePlayer = null;
               this.waitingForInvsee = false;
               if (!this.pendingPlayers.isEmpty()) {
                  this.tickDelay = 6;
               }
            }
         }
      }
   }

   private void checkNextPlayer() {
      if (!this.pendingPlayers.isEmpty() && this.mc.method_1562() != null) {
         this.currentInvseePlayer = this.pendingPlayers.remove(0);
         this.waitingForInvsee = true;
         this.mc.method_1562().method_45730("invsee " + this.currentInvseePlayer);
      }
   }
}
