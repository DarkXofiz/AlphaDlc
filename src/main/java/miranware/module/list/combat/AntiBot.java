package miranware.module.list.combat;

import com.google.common.eventbus.Subscribe;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import miranware.event.list.EventTick;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.ModeSetting;
import miranware.util.base.Instance;

@ModuleInformation(
   moduleName = "Anti Bot",
   moduleDesc = "Removes all bots from the world",
   moduleCategory = ModuleCategory.COMBAT
)
public class AntiBot extends Module {
   private final ModeSetting mode = new ModeSetting("Bypass", "ReallyWorld", "ReallyWorld", "Matrix", "LonyGrief");
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
