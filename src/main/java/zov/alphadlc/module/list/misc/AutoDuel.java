package zov.alphadlc.module.list.misc;

import com.google.common.eventbus.Subscribe;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import zov.alphadlc.event.list.EventPacket;
import zov.alphadlc.event.list.EventTick;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.module.settings.ModeListSetting;
import zov.alphadlc.util.math.StopWatch;

@ModuleInformation(
   moduleName = "Auto Duel",
   moduleDesc = "\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438 \u0440\u0430\u0441\u0441\u044b\u043b\u0430\u0435\u0442 /duel \u0438 \u0432\u044b\u0431\u0438\u0440\u0430\u0435\u0442 \u043a\u0438\u0442",
   moduleCategory = ModuleCategory.MISC
)
public class AutoDuel extends Module {
   private final ModeListSetting kits = new ModeListSetting(
      "\u0420\u0430\u0437\u0440\u0435\u0448\u0451\u043d\u043d\u044b\u0435 \u043a\u0438\u0442\u044b",
      new BooleanSetting("\u0429\u0438\u0442\u044b", false),
      new BooleanSetting("\u0428\u0438\u043f\u044b 3", false),
      new BooleanSetting("\u041b\u0443\u043a", false),
      new BooleanSetting("\u0422\u043e\u0442\u0435\u043c\u044b", false),
      new BooleanSetting("\u041d\u043e\u0414\u0435\u0431\u0430\u0444\u0444", false),
      new BooleanSetting("\u0428\u0430\u0440\u044b", true),
      new BooleanSetting("\u041a\u043b\u0430\u0441\u0441\u0438\u043a", false),
      new BooleanSetting("\u0427\u0438\u0442\u0435\u0440\u0441\u043a\u0438\u0439 \u0440\u0430\u0439", false),
      new BooleanSetting("\u0411\u0435\u0437 \u044d\u043d\u0434\u0435\u0440-\u0436\u0435\u043c\u0447\u0443\u0433\u0430", false)
   );
   private static final Pattern NAME_PATTERN = Pattern.compile("^\\w{3,16}$");
   private final List<String> sent = new ArrayList<>();
   private final StopWatch duelDelay = new StopWatch();
   private final StopWatch clearSentDelay = new StopWatch();
   private final StopWatch kitChoiceDelay = new StopWatch();
   private final StopWatch duelSetupDelay = new StopWatch();

   @Override
   public void onEnable() {
      super.onEnable();
      this.sent.clear();
   }

   @Subscribe
   public void onTick(EventTick e) {
      if (this.mc.player != null && this.mc.getNetworkHandler() != null) {
         List<String> players = this.getOnlinePlayers();
         if (this.clearSentDelay.isReached(800L * (long)Math.max(players.size(), 1))) {
            this.sent.clear();
            this.clearSentDelay.reset();
         }

         for (String player : players) {
            if (!this.sent.contains(player) && !player.equals(this.mc.player.getGameProfile().getName())) {
               if (this.duelDelay.isReached(1000L)) {
                  this.mc.getNetworkHandler().sendChatCommand("duel " + player);
                  this.sent.add(player);
                  this.duelDelay.reset();
               }
               break;
            }
         }

         if (this.mc.currentScreen instanceof GenericContainerScreen screen) {
            String title = screen.getTitle().getString();
            if (title.contains("\u0412\u044b\u0431\u043e\u0440 \u043d\u0430\u0431\u043e\u0440\u0430") || title.contains("Kit selection")) {
               this.handleKitSelection();
            } else if (title.contains("\u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0430 \u043f\u043e\u0435\u0434\u0438\u043d\u043a\u0430")
               || title.contains("Duel setup")) {
               this.handleDuelSetup();
            }
         }
      }
   }

   @Subscribe
   public void onPacket(EventPacket e) {
      if (e.getPacket() instanceof GameMessageS2CPacket packet) {
         String text = packet.content().getString().toLowerCase();
         if ((
               text.contains("\u043d\u0430\u0447\u0430\u043b\u043e")
                     && text.contains("\u0447\u0435\u0440\u0435\u0437")
                     && text.contains("\u0441\u0435\u043a\u0443\u043d\u0434")
                  || text.contains(
                     "\u0432\u043e \u0432\u0440\u0435\u043c\u044f \u043f\u043e\u0435\u0434\u0438\u043d\u043a\u0430 \u0437\u0430\u043f\u0440\u0435\u0449\u0435\u043d\u043e \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u044c \u043a\u043e\u043c\u0430\u043d\u0434\u044b"
                  )
                  || text.contains("duel") && text.contains("during") && text.contains("forbidden")
            )
            && this.isEnabled()) {
            this.setEnabled(false);
         }
      }
   }

   private List<String> getOnlinePlayers() {
      return this.mc
         .getNetworkHandler()
         .getPlayerList()
         .stream()
         .map(entry -> entry.getProfile().getName())
         .filter(name -> NAME_PATTERN.matcher(name).matches())
         .collect(Collectors.toList());
   }

   private void handleKitSelection() {
      List<Integer> slots = new ArrayList<>();
      List<BooleanSetting> settings = this.kits.getSettings();

      for (int i = 0; i < settings.size(); i++) {
         if (settings.get(i).getValue()) {
            slots.add(i);
         }
      }

      if (!slots.isEmpty()) {
         Collections.shuffle(slots);
         int slotId = slots.get(0);
         if (this.kitChoiceDelay.isReached(90L)) {
            this.clickSlot(slotId);
            this.kitChoiceDelay.reset();
         }
      }
   }

   private void handleDuelSetup() {
      if (this.duelSetupDelay.isReached(90L)) {
         this.clickSlot(0);
         this.duelSetupDelay.reset();
      }
   }

   private void clickSlot(int slotId) {
      if (this.mc.interactionManager != null && this.mc.player != null) {
         int syncId = this.mc.player.currentScreenHandler.syncId;
         this.mc.interactionManager.clickSlot(syncId, slotId, 0, SlotActionType.QUICK_MOVE, this.mc.player);
      }
   }
}
