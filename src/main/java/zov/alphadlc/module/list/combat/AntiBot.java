package zov.alphadlc.module.list.combat;

import com.google.common.eventbus.Subscribe;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import zov.alphadlc.event.list.EventTick;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.ModeSetting;
import zov.alphadlc.util.base.Instance;

@ModuleInformation(
   moduleName = "Anti Bot",
   moduleDesc = "\u0423\u0434\u0430\u043b\u044f\u0435\u0442 \u0432\u0441\u0435\u0445 \u0431\u043e\u0442\u043e\u0432 \u0438\u0437 \u043c\u0438\u0440\u0430",
   moduleCategory = ModuleCategory.COMBAT
)
public class AntiBot extends Module {
   private final ModeSetting mode = new ModeSetting("\u041e\u0431\u0445\u043e\u0434", "ReallyWorld", "ReallyWorld", "Matrix", "LonyGrief");
   public static final List<Entity> bot = new ArrayList<>();

   @Subscribe
   public void onUpdate(EventTick e) {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
         for (Entity entity : this.mc.field_1687.method_18112()) {
            if (entity instanceof PlayerEntity) {
               PlayerEntity player = (PlayerEntity)entity;
               if (!player.equals(this.mc.field_1724)) {
                  boolean isBot = false;
                  if (this.mode.is("ReallyWorld")) {
                     boolean hasValidArmor = player.method_31548()
                        .field_7548
                        .stream()
                        .allMatch(armorItem -> armorItem.method_7909() != Items.field_8162 && armorItem.method_7923() && !armorItem.method_7986());
                     boolean hasValidEquipment = player.method_6079().method_7909() == Items.field_8162
                        && player.method_31548()
                           .field_7548
                           .stream()
                           .anyMatch(
                              armorItem -> armorItem.method_7909() == Items.field_8370
                                    || armorItem.method_7909() == Items.field_8570
                                    || armorItem.method_7909() == Items.field_8577
                                    || armorItem.method_7909() == Items.field_8267
                                    || armorItem.method_7909() == Items.field_8660
                                    || armorItem.method_7909() == Items.field_8396
                                    || armorItem.method_7909() == Items.field_8523
                                    || armorItem.method_7909() == Items.field_8743
                           );
                     boolean hasFullFood = player.method_7344().method_7586() == 20;
                     isBot = hasValidArmor && hasValidEquipment && hasFullFood;
                  } else if (this.mode.is("Matrix")) {
                     UUID offlineUuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + player.method_5477().getString()).getBytes(StandardCharsets.UTF_8));
                     isBot = player.method_5805() && !bot.contains(player) && !player.method_5667().equals(offlineUuid);
                  } else if (this.mode.is("LonyGrief")) {
                     String playerName = player.method_5476().getString().trim();
                     String[] nameParts = playerName.split("\\s+");
                     boolean isBotCandidate = nameParts.length == 1 && !playerName.contains("?");
                     isBot = isBotCandidate;
                  }

                  if (isBot) {
                     if (!bot.contains(player)) {
                        bot.add(player);
                     }
                  } else {
                     bot.remove(player);
                  }
               }
            }
         }
      }
   }

   @Override
   public void onDisable() {
      bot.clear();
      super.onDisable();
   }

   public static boolean isBot(Entity entity) {
      AntiBot instance = Instance.get(AntiBot.class);
      return instance != null && instance.isEnabled() && bot.contains(entity);
   }
}
