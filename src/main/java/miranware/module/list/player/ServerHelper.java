package miranware.module.list.player;

import com.google.common.eventbus.Subscribe;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Predicate;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.PlayerInput;
import miranware.event.list.EventKeyInput;
import miranware.event.list.EventTick;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.list.movement.Sprint;
import miranware.module.settings.BindSetting;
import miranware.module.settings.BooleanSetting;
import miranware.module.settings.ModeSetting;
import miranware.util.base.Instance;

@ModuleInformation(
   moduleName = "ServerHelper",
   moduleDesc = "Server helper",
   moduleCategory = ModuleCategory.PLAYER
)
public class ServerHelper extends Module {
   private final ModeSetting server = new ModeSetting("Server", "ReallyWorld", "ReallyWorld");
   private final BindSetting antiFlyKey = new BindSetting("Anti Fly", 71)
      .setVisible(() -> this.server.is("ReallyWorld"));
   private final BindSetting grinchPotionKey = new BindSetting("Grinch Potion", -1)
      .setVisible(() -> this.server.is("ReallyWorld"));
   private final BindSetting autoShiftKey = new BindSetting("AutoShift (Shift for ball)", -1)
      .setVisible(() -> this.server.is("ReallyWorld"));
   private final BindSetting newYearHorrorKey = new BindSetting("New Year Horror", -1)
      .setVisible(() -> this.server.is("ReallyWorld"));
   private final BindSetting darkEssenceKey = new BindSetting(
         "Abyss Essence", -1
      )
      .setVisible(() -> this.server.is("ReallyWorld"));
   private final BindSetting snowballKey = new BindSetting("Snowball", -1).setVisible(() -> this.server.is("ReallyWorld"));
   private final BindSetting trapKey = new BindSetting("Trap", -1).setVisible(() -> this.server.is("ReallyWorld"));
   private final BooleanSetting autoFixAll = new BooleanSetting("Auto /fix all", false).setVisible(() -> this.server.is("ReallyWorld"));
   private boolean useAntiFly;
   private boolean useGrinchPotion;
   private boolean useAutoShift;
   private boolean useNewYearHorror;
   private boolean useDarkEssence;
   private boolean useSnowball;
   private boolean useTrap;
   private boolean wasInPvp;
   private int nextFixTick;

   public int getAntiFlyKey() {
      return this.antiFlyKey.getValue();
   }

   public int getGrinchPotionKey() {
      return this.grinchPotionKey.getValue();
   }

   public int getAutoShiftKey() {
      return this.autoShiftKey.getValue();
   }

   public int getNewYearHorrorKey() {
      return this.newYearHorrorKey.getValue();
   }

   public int getDarkEssenceKey() {
      return this.darkEssenceKey.getValue();
   }

   public int getSnowballKey() {
      return this.snowballKey.getValue();
   }

   public int getTrapKey() {
      return this.trapKey.getValue();
   }

   private boolean isGrinchPotion(ItemStack stack) {
      if (!stack.isOf(Items.SPLASH_POTION)) {
         return false;
      } else {
         String name = stack.getName().getString().toLowerCase();
         return name.contains("\u0433\u0440\u0438\u043d\u0447");
      }
   }

   private boolean isNewYearHorror(ItemStack stack) {
      if (!stack.isOf(Items.SPLASH_POTION)) {
         return false;
      } else {
         String name = stack.getName().getString().toLowerCase();
         return name.contains("\u043d\u043e\u0432\u043e\u0433\u043e\u0434\u043d\u0438\u0439 \u0443\u0436\u0430\u0441");
      }
   }

   private boolean isDarkEssence(ItemStack stack) {
      if (!stack.isOf(Items.SPLASH_POTION)) {
         return false;
      } else {
         String name = stack.getName().getString().toLowerCase();
         return name.contains("\u044d\u0441\u0441\u0435\u043d\u0446\u0438\u044f \u043a\u0440\u043e\u043c\u0435\u0448\u043d\u0438\u043a\u0430");
      }
   }

   private boolean isSnowball(ItemStack stack) {
      if (!stack.isOf(Items.SPLASH_POTION)) {
         return false;
      } else {
         String name = stack.getName().getString().toLowerCase();
         return name.contains("\u0441\u043d\u0435\u0436\u043e\u043a");
      }
   }

   private boolean isAntiFly(ItemStack stack) {
      return stack.isOf(Items.FIREWORK_STAR);
   }

   private boolean isTrapItem(ItemStack stack) {
      return stack.isOf(Items.HEART_OF_THE_SEA);
   }

   private boolean isTrapPotion(ItemStack stack) {
      if (!stack.isOf(Items.SPLASH_POTION)) {
         return false;
      } else {
         String name = stack.getName().getString().toLowerCase();
         return name.contains("\u043b\u043e\u0432\u0443\u0448") || name.contains("\u043b\u043e\u0432\u0443\u0448\u043a");
      }
   }

   private int findSlot(Predicate<ItemStack> predicate, int from, int to) {
      for (int i2 = from; i2 < to; i2++) {
         if (predicate.test(this.mc.player.getInventory().getStack(i2))) {
            return i2;
         }
      }

      return -1;
   }

   private int findTrapInHotbar() {
      return this.findSlot(this::isTrapItem, 0, 9);
   }

   private int findTrapInInventory() {
      return this.findSlot(this::isTrapItem, 9, 45);
   }

   @Subscribe
   private void onKey(EventKeyInput e2) {
      if (this.mc.currentScreen == null) {
         if (e2.getAction() == 1) {
            if (e2.getKey() == this.antiFlyKey.getValue()) {
               this.useAntiFly = true;
            }

            if (e2.getKey() == this.grinchPotionKey.getValue()) {
               this.useGrinchPotion = true;
            }

            if (e2.getKey() == this.autoShiftKey.getValue()) {
               this.useAutoShift = true;
            }

            if (e2.getKey() == this.newYearHorrorKey.getValue()) {
               this.useNewYearHorror = true;
            }

            if (e2.getKey() == this.darkEssenceKey.getValue()) {
               this.useDarkEssence = true;
            }

            if (e2.getKey() == this.snowballKey.getValue()) {
               this.useSnowball = true;
            }

            if (e2.getKey() == this.trapKey.getValue()) {
               this.useTrap = true;
            }
         }
      }
   }

   @Subscribe
   private void onTick(EventTick e2) {
      if (this.useAntiFly) {
         this.useAntiFly = false;
         this.useItem(this::isAntiFly);
      }

      if (this.useGrinchPotion) {
         this.useGrinchPotion = false;
         this.useItem(this::isGrinchPotion);
      }

      if (this.useAutoShift) {
         this.useAutoShift = false;
         this.pulseShift();
      }

      if (this.useNewYearHorror) {
         this.useNewYearHorror = false;
         this.useItem(this::isNewYearHorror);
      }

      if (this.useDarkEssence) {
         this.useDarkEssence = false;
         this.useItem(this::isDarkEssence);
      }

      if (this.useSnowball) {
         this.useSnowball = false;
         this.useItem(this::isSnowball);
      }

      if (this.useTrap) {
         this.useTrap = false;
         this.useTrapItem();
      }

      this.handleAutoFixAll();
   }

   private void handleAutoFixAll() {
      if (this.server.is("ReallyWorld") && this.autoFixAll.getValue()) {
         boolean inPvp = this.isPvp();
         if (this.wasInPvp && !inPvp && this.mc.player.age >= this.nextFixTick) {
            this.mc.player.networkHandler.sendChatCommand("fix all");
            this.nextFixTick = this.mc.player.age + 40;
         }

         this.wasInPvp = inPvp;
      } else {
         this.wasInPvp = this.isPvp();
      }
   }

   private boolean isPvp() {
      return this.mc.player != null && this.mc.world != null
         ? this.mc.world.getPlayers().stream().filter(p -> p != this.mc.player).anyMatch(p -> p.squaredDistanceTo(this.mc.player) < 100.0)
         : false;
   }

   private void pulseShift() {
      if (this.mc.options != null) {
         this.mc.options.sneakKey.setPressed(true);
         new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
               if (ServerHelper.this.mc.options != null) {
                  ServerHelper.this.mc.options.sneakKey.setPressed(false);
               }
            }
         }, 150L);
      }
   }

   private void useTrapItem() {
      if (this.mc.player != null) {
         if (this.isTrapItem(this.mc.player.getMainHandStack())) {
            this.mc.interactionManager.interactItem(this.mc.player, Hand.MAIN_HAND);
         } else if (this.isTrapItem(this.mc.player.getOffHandStack())) {
            this.mc.interactionManager.interactItem(this.mc.player, Hand.OFF_HAND);
         } else {
            int slot = this.findTrapInHotbar();
            if (slot != -1) {
               int prev = this.mc.player.getInventory().selectedSlot;
               this.mc.player.getInventory().selectedSlot = slot;
               this.mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
               this.mc.interactionManager.interactItem(this.mc.player, Hand.MAIN_HAND);
               this.mc.player.getInventory().selectedSlot = prev;
               this.mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(prev));
            } else {
               slot = this.findTrapInInventory();
               if (slot != -1) {
                  int prev = this.mc.player.getInventory().selectedSlot;
                  int syncId = this.mc.player.currentScreenHandler.syncId;
                  this.mc.interactionManager.clickSlot(syncId, slot, prev, SlotActionType.SWAP, this.mc.player);
                  this.mc.interactionManager.interactItem(this.mc.player, Hand.MAIN_HAND);
                  this.mc.interactionManager.clickSlot(syncId, slot, prev, SlotActionType.SWAP, this.mc.player);
               }
            }
         }
      }
   }

   private int findTrapPotionInHotbar() {
      return this.findSlot(this::isTrapPotion, 0, 9);
   }

   private int findTrapPotionInInventory() {
      return this.findSlot(this::isTrapPotion, 9, 45);
   }

   private void useItem(Predicate<ItemStack> predicate) {
      if (this.mc.player != null && this.mc.getNetworkHandler() != null) {
         if (predicate.test(this.mc.player.getOffHandStack())) {
            this.shiftClick();
         } else {
            int hotbarSlot = this.findSlot(predicate, 0, 9);
            if (hotbarSlot != -1) {
               boolean wasSprinting = this.stopSprintingIfNeeded();
               this.swapToOffhand(this.mc.player.currentScreenHandler.syncId, 45, hotbarSlot);
               this.shiftClick();
               this.swapToOffhand(this.mc.player.currentScreenHandler.syncId, 45, hotbarSlot);
               if (wasSprinting) {
                  this.resumeSprinting();
               }
            } else {
               int invSlot = this.findSlot(predicate, 9, 45);
               if (invSlot != -1) {
                  boolean wasSprinting = this.stopSprintingIfNeeded();
                  this.swapToOffhand(0, invSlot, 40);
                  this.shiftClick();
                  this.swapToOffhand(0, invSlot, 40);
                  if (wasSprinting) {
                     this.resumeSprinting();
                  }
               }
            }
         }
      }
   }

   private void shiftClick() {
      this.mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(this.mc.player, Mode.PRESS_SHIFT_KEY));
      this.mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(this.mc.player, Mode.RELEASE_SHIFT_KEY));
   }

   private void swapToOffhand(int syncId, int slot, int button) {
      this.mc.interactionManager.clickSlot(syncId, slot, button, SlotActionType.SWAP, this.mc.player);
      this.mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(0));
   }

   private boolean stopSprintingIfNeeded() {
      if (!this.mc.player.isSprinting()) {
         return false;
      } else {
         this.mc.getNetworkHandler().sendPacket(new PlayerInputC2SPacket(new PlayerInput(false, false, false, false, false, false, false)));
         this.mc.player.setSprinting(false);
         this.mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(this.mc.player, Mode.STOP_SPRINTING));
         if (!Instance.get(Sprint.class).isEnabled()) {
            this.mc.options.sprintKey.setPressed(false);
         }

         return true;
      }
   }

   private void resumeSprinting() {
      this.mc.getNetworkHandler().sendPacket(new PlayerInputC2SPacket(this.mc.player.input.playerInput));
   }
}
