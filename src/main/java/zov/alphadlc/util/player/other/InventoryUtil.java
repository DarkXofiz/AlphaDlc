package zov.alphadlc.util.player.other;

import com.google.common.collect.Lists;
import com.mojang.text2speech.Narrator;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import java.util.function.Predicate;
import lombok.Generated;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.consume.UseAction;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntry.Reference;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.collection.DefaultedList;
import zov.alphadlc.AlphaDLC;
import zov.alphadlc.module.list.movement.Sprint;
import zov.alphadlc.util.IMinecraft;
import zov.alphadlc.util.base.Instance;
import zov.alphadlc.util.packet.NetworkUtils;

public class InventoryUtil implements IMinecraft {
   public static final InventoryUtil instance = new InventoryUtil();

   public static int searchItem(Item item) {
      for (int i = 0; i < mc.field_1724.method_31548().method_7364(); i++) {
         if (mc.field_1724.method_31548().method_5438(i).method_7909().equals(item)) {
            return i;
         }
      }

      return -1;
   }

   public static int searchItem(Item item, int start, int end) {
      for (int i = start; i < end; i++) {
         if (mc.field_1724.method_31548().method_5438(i).method_7909().equals(item)) {
            return i;
         }
      }

      return -1;
   }

   public static int searchItem(List<Item> items) {
      for (int i = 0; i < mc.field_1724.method_31548().method_7364(); i++) {
         for (Item item : items) {
            if (mc.field_1724.method_31548().method_5438(i).method_7909().equals(item)) {
               return i;
            }
         }
      }

      return -1;
   }

   public static int searchItemHotbar(Item item) {
      for (int i = 0; i < 9; i++) {
         if (mc.field_1724.method_31548().method_5438(i).method_7909().equals(item)) {
            return i;
         }
      }

      return -1;
   }

   public static int searchItemHotbar(List<Item> items) {
      for (int i = 0; i < 9; i++) {
         for (Item item : items) {
            if (mc.field_1724.method_31548().method_5438(i).method_7909().equals(item)) {
               return i;
            }
         }
      }

      return -1;
   }

   public static int searchItemStack(Predicate<ItemStack> predicate) {
      for (int i = 0; i < 45; i++) {
         ItemStack stack = mc.field_1724.method_31548().method_5438(i);
         if (!stack.method_7960() && predicate.test(stack)) {
            return i;
         }
      }

      return -1;
   }

   public static int searchHotbarStack(Predicate<ItemStack> predicate) {
      for (int i = 0; i < 9; i++) {
         ItemStack stack = mc.field_1724.method_31548().method_5438(i);
         if (!stack.method_7960() && predicate.test(stack)) {
            return i;
         }
      }

      return -1;
   }

   public static void swapWithBypassGrim(Runnable runnable) {
      if (mc.field_1724 != null && mc.method_1562() != null) {
         try {
            mc.method_1562().method_52787(new PlayerInputC2SPacket(new PlayerInput(false, false, false, false, false, false, false)));
            if (AlphaDLC.getInstance().getServerManager().isServerSprinting()) {
               mc.field_1724.method_5728(false);
               mc.method_1562().method_52787(new ClientCommandC2SPacket(mc.field_1724, Mode.field_12985));
               if (!Instance.get(Sprint.class).isEnabled()) {
                  mc.field_1690.field_1867.method_23481(false);
               }
            }

            runnable.run();
            NetworkUtils.sendSilentPacket(new CloseHandledScreenC2SPacket(0));
            mc.method_1562().method_52787(new PlayerInputC2SPacket(mc.field_1724.field_3913.field_54155));
         } catch (Exception var2) {
            var2.printStackTrace();
         }
      }
   }

   public static void swapWithBypassPolar(Runnable runnable) {
      swapWithBypassPolar(runnable, 100L);
   }

   public static void swapWithBypassPolar(Runnable runnable, long duration) {
      if (mc.field_1724 != null && mc.method_1562() != null) {
         try {
            SlownessManager.addTask(new SlownessManager.SlowTask(duration, () -> {
               if (mc.field_1724.method_6115()) {
                  mc.field_1761.method_2897(mc.field_1724);
               }

               runnable.run();
               NetworkUtils.sendSilentPacket(new CloseHandledScreenC2SPacket(0));
            }));
         } catch (Exception var4) {
            var4.printStackTrace();
         }
      }
   }

   public static void swapAndUseHvH(Item item) {
      if (!mc.field_1724.method_7357().method_7904(new ItemStack(item))) {
         int slot = searchItem(item, 9, 45);
         int slotHotbar = searchItem(item, 0, 9);
         int previousSlot = mc.field_1724.method_31548().field_7545;
         if (mc.field_1724.method_6047().method_7909() == item) {
            mc.field_1761.method_2919(mc.field_1724, Hand.field_5808);
         } else if (mc.field_1724.method_6079().method_7909() == item) {
            mc.field_1761.method_2919(mc.field_1724, Hand.field_5810);
         } else {
            if (slotHotbar != -1) {
               mc.method_1562().method_52787(new UpdateSelectedSlotC2SPacket(slotHotbar));
               mc.field_1761.method_2919(mc.field_1724, Hand.field_5808);
               mc.method_1562().method_52787(new UpdateSelectedSlotC2SPacket(previousSlot));
            }

            if (slotHotbar == -1 && slot != -1) {
               int slotCorrectable = -1;

               for (int slotNone = 0; slotNone < 8; slotNone++) {
                  ItemStack stack = mc.field_1724.method_31548().method_5438(slotNone);
                  if (stack.method_7960()) {
                     slotCorrectable = slotNone;
                  }

                  UseAction action = stack.method_7976();
                  if (action == UseAction.field_8952) {
                     slotCorrectable = slotNone;
                  }
               }

               if (slotCorrectable == -1) {
                  mc.method_1562().method_52787(new PlayerInputC2SPacket(new PlayerInput(false, false, false, false, false, false, false)));
                  if (AlphaDLC.getInstance().getServerManager().isServerSprinting()) {
                     mc.field_1724.method_5728(false);
                     mc.method_1562().method_52787(new ClientCommandC2SPacket(mc.field_1724, Mode.field_12985));
                     if (!Instance.get(Sprint.class).isEnabled()) {
                        mc.field_1690.field_1867.method_23481(false);
                     }
                  }

                  mc.field_1761.method_2906(0, slot, 8, SlotActionType.field_7791, mc.field_1724);
                  mc.method_1562().method_52787(new CloseHandledScreenC2SPacket(0));
                  mc.method_1562().method_52787(new UpdateSelectedSlotC2SPacket(8));
                  mc.field_1761.method_2919(mc.field_1724, Hand.field_5808);
                  mc.method_1562().method_52787(new UpdateSelectedSlotC2SPacket(previousSlot));
                  mc.method_1562().method_52787(new PlayerInputC2SPacket(mc.field_1724.field_3913.field_54155));
               } else {
                  mc.method_1562().method_52787(new PlayerInputC2SPacket(new PlayerInput(false, false, false, false, false, false, false)));
                  if (AlphaDLC.getInstance().getServerManager().isServerSprinting()) {
                     mc.field_1724.method_5728(false);
                     mc.method_1562().method_52787(new ClientCommandC2SPacket(mc.field_1724, Mode.field_12985));
                     if (!Instance.get(Sprint.class).isEnabled()) {
                        mc.field_1690.field_1867.method_23481(false);
                     }
                  }

                  mc.field_1761.method_2906(0, slot, slotCorrectable, SlotActionType.field_7791, mc.field_1724);
                  mc.method_1562().method_52787(new CloseHandledScreenC2SPacket(0));
                  mc.method_1562().method_52787(new UpdateSelectedSlotC2SPacket(slotCorrectable));
                  mc.field_1761.method_2919(mc.field_1724, Hand.field_5808);
                  mc.method_1562().method_52787(new UpdateSelectedSlotC2SPacket(previousSlot));
                  mc.field_1761.method_2906(0, slot, slotCorrectable, SlotActionType.field_7791, mc.field_1724);
                  mc.method_1562().method_52787(new CloseHandledScreenC2SPacket(0));
                  mc.method_1562().method_52787(new PlayerInputC2SPacket(mc.field_1724.field_3913.field_54155));
               }
            }
         }
      }
   }

   public static void swapAndUseLegit(Item item) {
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (!mc.field_1724.method_7357().method_7904(new ItemStack(item))) {
            int slot = searchItem(item, 9, 45);
            int slotHotbar = searchItem(item, 0, 9);
            int previousSlot = mc.field_1724.method_31548().field_7545;
            if (mc.field_1724.method_6047().method_7909() == item) {
               mc.field_1761.method_2919(mc.field_1724, Hand.field_5808);
            } else if (mc.field_1724.method_6079().method_7909() == item) {
               mc.field_1761.method_2919(mc.field_1724, Hand.field_5810);
            } else if (slotHotbar != -1) {
               mc.field_1724.method_31548().field_7545 = slotHotbar;
               mc.field_1761.method_2911();
               mc.field_1761
                  .method_41931(
                     mc.field_1687,
                     sequence -> new PlayerInteractItemC2SPacket(Hand.field_5808, sequence, mc.field_1724.method_36454(), mc.field_1724.method_36455())
                  );
               mc.field_1724.method_31548().field_7545 = previousSlot;
               mc.field_1761.method_2911();
            } else {
               if (slotHotbar == -1 && slot != -1) {
                  int slotCorrectable = -1;

                  for (int slotNone = 0; slotNone < 9; slotNone++) {
                     ItemStack stack = mc.field_1724.method_31548().method_5438(slotNone);
                     if (stack.method_7960()) {
                        slotCorrectable = slotNone;
                        break;
                     }

                     UseAction action = stack.method_7976();
                     if (action == UseAction.field_8952) {
                        slotCorrectable = slotNone;
                     }
                  }

                  if (slotCorrectable != -1) {
                     mc.method_1562().method_52787(new PlayerInputC2SPacket(new PlayerInput(false, false, false, false, false, false, false)));
                     mc.field_1761.method_2906(0, slot, slotCorrectable, SlotActionType.field_7791, mc.field_1724);
                     mc.method_1562().method_52787(new CloseHandledScreenC2SPacket(0));
                     mc.field_1724.method_31548().field_7545 = slotCorrectable;
                     mc.field_1761.method_2911();
                     mc.field_1761
                        .method_41931(
                           mc.field_1687,
                           sequence -> new PlayerInteractItemC2SPacket(Hand.field_5808, sequence, mc.field_1724.method_36454(), mc.field_1724.method_36455())
                        );
                     mc.field_1724.method_31548().field_7545 = previousSlot;
                     mc.field_1761.method_2911();
                     mc.field_1761.method_2906(0, slot, slotCorrectable, SlotActionType.field_7791, mc.field_1724);
                     mc.method_1562().method_52787(new CloseHandledScreenC2SPacket(0));
                     mc.method_1562().method_52787(new PlayerInputC2SPacket(mc.field_1724.field_3913.field_54155));
                  }
               }
            }
         }
      }
   }

   public static void clickSlotNoSync(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player) {
      ScreenHandler screenHandler = player.field_7512;
      if (syncId != screenHandler.field_7763) {
         Narrator.LOGGER.warn("Ignoring click in mismatching container. Click in {}, player has {}.", syncId, screenHandler.field_7763);
      } else {
         DefaultedList<Slot> defaultedList = screenHandler.field_7761;
         int i = defaultedList.size();
         List<ItemStack> list = Lists.newArrayListWithCapacity(i);

         for (Slot slot : defaultedList) {
            list.add(slot.method_7677().method_7972());
         }

         screenHandler.method_7593(slotId, button, actionType, player);
         Int2ObjectMap<ItemStack> int2ObjectMap = new Int2ObjectOpenHashMap();

         for (int j = 0; j < i; j++) {
            ItemStack itemStack = list.get(j);
            ItemStack itemStack2 = ((Slot)defaultedList.get(j)).method_7677();
            if (!ItemStack.method_7973(itemStack, itemStack2)) {
               int2ObjectMap.put(j, itemStack2.method_7972());
            }
         }

         NetworkUtils.sendSilentPacket(
            new ClickSlotC2SPacket(syncId, screenHandler.method_37421(), slotId, button, actionType, screenHandler.method_34255().method_7972(), int2ObjectMap)
         );
      }
   }

   public static int findBestElytraSlot() {
      int bestSlot = -1;
      double bestScore = -1.0;
      RegistryEntry<Enchantment> protection = (RegistryEntry<Enchantment>)((Registry)MinecraftClient.method_1551()
            .field_1687
            .method_30349()
            .method_46759(RegistryKeys.field_41265)
            .get())
         .method_10223(Enchantments.field_9111.method_29177())
         .orElseThrow();
      RegistryEntry<Enchantment> unbreaking = (RegistryEntry<Enchantment>)((Registry)MinecraftClient.method_1551()
            .field_1687
            .method_30349()
            .method_46759(RegistryKeys.field_41265)
            .get())
         .method_10223(Enchantments.field_9119.method_29177())
         .orElseThrow();
      RegistryEntry<Enchantment> mending = (RegistryEntry<Enchantment>)((Registry)MinecraftClient.method_1551()
            .field_1687
            .method_30349()
            .method_46759(RegistryKeys.field_41265)
            .get())
         .method_10223(Enchantments.field_9101.method_29177())
         .orElseThrow();

      for (int slot = 0; slot < 36; slot++) {
         ItemStack stack = mc.field_1724.method_31548().method_5438(slot);
         if (stack.method_31574(Items.field_8833)) {
            int protLevel = EnchantmentHelper.method_8225(protection, stack);
            int unbLevel = EnchantmentHelper.method_8225(unbreaking, stack);
            int mendLevel = EnchantmentHelper.method_8225(mending, stack);
            int maxDurability = stack.method_7936();
            int currentDamage = stack.method_7919();
            double durabilityRatio = (double)(maxDurability - currentDamage) / (double)maxDurability;
            double score = (double)(protLevel * 100 + unbLevel * 10 + (mendLevel > 0 ? 1 : 0)) + durabilityRatio * 10.0;
            if (score > bestScore) {
               bestScore = score;
               bestSlot = slot;
            }
         }
      }

      return bestSlot;
   }

   public static int findBestChestplateSlot() {
      int bestSlot = -1;
      double bestScore = -1.0;
      Reference<Enchantment> protection = (Reference<Enchantment>)((Registry)mc.field_1687.method_30349().method_46759(RegistryKeys.field_41265).get())
         .method_10223(Enchantments.field_9111.method_29177())
         .orElseThrow();
      Reference<Enchantment> unbreaking = (Reference<Enchantment>)((Registry)mc.field_1687.method_30349().method_46759(RegistryKeys.field_41265).get())
         .method_10223(Enchantments.field_9119.method_29177())
         .orElseThrow();
      Reference<Enchantment> mending = (Reference<Enchantment>)((Registry)mc.field_1687.method_30349().method_46759(RegistryKeys.field_41265).get())
         .method_10223(Enchantments.field_9101.method_29177())
         .orElseThrow();

      for (int slot = 0; slot < 36; slot++) {
         ItemStack stack = mc.field_1724.method_31548().method_5438(slot);
         Item protLevel = stack.method_7909();
         if (protLevel instanceof ArmorItem) {
            ArmorItem armor = (ArmorItem)protLevel;
            int protLevelx = EnchantmentHelper.method_8225(protection, stack);
            int unbLevel = EnchantmentHelper.method_8225(unbreaking, stack);
            int mendLevel = EnchantmentHelper.method_8225(mending, stack);
            int armorTypePriority = getChestplatePriority(armor);
            int maxDurability = stack.method_7936();
            int currentDamage = stack.method_7919();
            double durabilityRatio = (double)(maxDurability - currentDamage) / (double)maxDurability;
            double score = (double)(armorTypePriority * 10000 + protLevelx * 100 + unbLevel * 10 + (mendLevel > 0 ? 1 : 0)) + durabilityRatio * 10.0;
            if (score > bestScore) {
               bestScore = score;
               bestSlot = slot;
            }
         }
      }

      return bestSlot;
   }

   private static int getArmorPriority(Item item) {
      if (item == Items.field_22027 || item == Items.field_22028 || item == Items.field_22029 || item == Items.field_22030) {
         return 6;
      } else if (item == Items.field_8805 || item == Items.field_8058 || item == Items.field_8348 || item == Items.field_8285) {
         return 5;
      } else if (item == Items.field_8743 || item == Items.field_8523 || item == Items.field_8396 || item == Items.field_8660) {
         return 4;
      } else if (item == Items.field_8283 || item == Items.field_8873 || item == Items.field_8218 || item == Items.field_8313) {
         return 3;
      } else if (item == Items.field_8862 || item == Items.field_8678 || item == Items.field_8416 || item == Items.field_8753) {
         return 2;
      } else {
         return item != Items.field_8267 && item != Items.field_8577 && item != Items.field_8570 && item != Items.field_8370 ? 0 : 1;
      }
   }

   private static int getChestplatePriority(Item item) {
      if (item == Items.field_22028) {
         return 6;
      } else if (item == Items.field_8058) {
         return 5;
      } else if (item == Items.field_8523) {
         return 4;
      } else if (item == Items.field_8873) {
         return 3;
      } else if (item == Items.field_8678) {
         return 2;
      } else {
         return item == Items.field_8577 ? 1 : 0;
      }
   }

   public static int getBestArmorSlot(EquipmentSlot slot) {
      int bestSlot = -1;
      double bestScore = -1.0;

      for (int i = 0; i < 36; i++) {
         ItemStack stack = mc.field_1724.method_31548().method_5438(i);
         if (stack.method_7909() instanceof ArmorItem && getNeededArmorSlot(stack) == slot) {
            double score = getArmorScore(stack);
            if (score > bestScore) {
               bestScore = score;
               bestSlot = i;
            }
         }
      }

      return bestSlot;
   }

   private static EquipmentSlot getNeededArmorSlot(ItemStack stack) {
      Item item = stack.method_7909();
      if (item instanceof ArmorItem) {
         if (item == Items.field_22027
            || item == Items.field_8805
            || item == Items.field_8743
            || item == Items.field_8862
            || item == Items.field_8283
            || item == Items.field_8267) {
            return EquipmentSlot.field_6169;
         }

         if (item == Items.field_22028
            || item == Items.field_8058
            || item == Items.field_8523
            || item == Items.field_8678
            || item == Items.field_8873
            || item == Items.field_8577) {
            return EquipmentSlot.field_6174;
         }

         if (item == Items.field_22029
            || item == Items.field_8348
            || item == Items.field_8396
            || item == Items.field_8416
            || item == Items.field_8218
            || item == Items.field_8570) {
            return EquipmentSlot.field_6172;
         }

         if (item == Items.field_22030
            || item == Items.field_8285
            || item == Items.field_8660
            || item == Items.field_8753
            || item == Items.field_8313
            || item == Items.field_8370) {
            return EquipmentSlot.field_6166;
         }
      }

      return null;
   }

   public static double getArmorScore(ItemStack stack) {
      double score = 0.0;
      score += (double)getArmorPriority(stack.method_7909());
      Reference<Enchantment> protection = (Reference<Enchantment>)((Registry)mc.field_1687.method_30349().method_46759(RegistryKeys.field_41265).get())
         .method_10223(Enchantments.field_9111.method_29177())
         .orElseThrow();
      Reference<Enchantment> unbreaking = (Reference<Enchantment>)((Registry)mc.field_1687.method_30349().method_46759(RegistryKeys.field_41265).get())
         .method_10223(Enchantments.field_9119.method_29177())
         .orElseThrow();
      Reference<Enchantment> mending = (Reference<Enchantment>)((Registry)mc.field_1687.method_30349().method_46759(RegistryKeys.field_41265).get())
         .method_10223(Enchantments.field_9101.method_29177())
         .orElseThrow();
      score += (double)EnchantmentHelper.method_8225(protection, stack) * 0.75;
      score += (double)EnchantmentHelper.method_8225(unbreaking, stack) * 0.3;
      score += (double)EnchantmentHelper.method_8225(mending, stack) * 0.1;
      double durabilityFactor = 1.0;
      if (stack.method_7963()) {
         int max = stack.method_7936();
         int left = max - stack.method_7919();
         durabilityFactor = Math.max(0.05, (double)left / (double)max);
      }

      return score * durabilityFactor;
   }

   @Generated
   public static InventoryUtil getInstance() {
      return instance;
   }
}
