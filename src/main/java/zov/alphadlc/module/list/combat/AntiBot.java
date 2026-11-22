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
      if (this.mc.player != null && this.mc.world != null) {
         for (Entity entity : this.mc.world.getEntities()) {
            if (entity instanceof PlayerEntity) {
               PlayerEntity player = (PlayerEntity)entity;
               if (!player.equals(this.mc.player)) {
                  boolean isBot = false;
                  if (this.mode.is("ReallyWorld")) {
                     boolean hasValidArmor = player.getInventory()
                        .armor
                        .stream()
                        .allMatch(armorItem -> armorItem.getItem() != Items.AIR && armorItem.isEnchantable() && !armorItem.isDamaged());
                     boolean hasValidEquipment = player.getOffHandStack().getItem() == Items.AIR
                        && player.getInventory()
                           .armor
                           .stream()
                           .anyMatch(
                              armorItem -> armorItem.getItem() == Items.LEATHER_BOOTS
                                    || armorItem.getItem() == Items.LEATHER_LEGGINGS
                                    || armorItem.getItem() == Items.LEATHER_CHESTPLATE
                                    || armorItem.getItem() == Items.LEATHER_HELMET
                                    || armorItem.getItem() == Items.IRON_BOOTS
                                    || armorItem.getItem() == Items.IRON_LEGGINGS
                                    || armorItem.getItem() == Items.IRON_CHESTPLATE
                                    || armorItem.getItem() == Items.IRON_HELMET
                           );
                     boolean hasFullFood = player.getHungerManager().getFoodLevel() == 20;
                     isBot = hasValidArmor && hasValidEquipment && hasFullFood;
                  } else if (this.mode.is("Matrix")) {
                     UUID offlineUuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + player.getName().getString()).getBytes(StandardCharsets.UTF_8));
                     isBot = player.isAlive() && !bot.contains(player) && !player.getUuid().equals(offlineUuid);
                  } else if (this.mode.is("LonyGrief")) {
                     String playerName = player.getDisplayName().getString().trim();
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
