package zov.alphadlc.module.list.player;

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
import zov.alphadlc.event.list.EventKeyInput;
import zov.alphadlc.event.list.EventTick;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.list.movement.Sprint;
import zov.alphadlc.module.settings.BindSetting;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.module.settings.ModeSetting;
import zov.alphadlc.util.base.Instance;

@ModuleInformation(
   moduleName = "ServerHelper",
   moduleDesc = "\u041f\u043e\u043c\u043e\u0449\u043d\u0438\u043a \u0434\u043b\u044f \u0441\u0435\u0440\u0432\u0435\u0440\u0430",
   moduleCategory = ModuleCategory.PLAYER
)
public class ServerHelper extends Module {
   private final ModeSetting server = new ModeSetting("\u0421\u0435\u0440\u0432\u0435\u0440", "ReallyWorld", "ReallyWorld");
   private final BindSetting antiFlyKey = new BindSetting("\u0410\u043d\u0442\u0438 \u041f\u043e\u043b\u0435\u0442", 71)
      .setVisible(() -> this.server.is("ReallyWorld"));
   private final BindSetting grinchPotionKey = new BindSetting("\u0417\u0435\u043b\u044c\u0435 \u0413\u0440\u0438\u043d\u0447\u0430", -1)
      .setVisible(() -> this.server.is("ReallyWorld"));
   private final BindSetting autoShiftKey = new BindSetting("AutoShift (\u0428\u0438\u0444\u0442 \u0434\u043b\u044f \u0448\u0430\u0440\u0430)", -1)
      .setVisible(() -> this.server.is("ReallyWorld"));
   private final BindSetting newYearHorrorKey = new BindSetting("\u041d\u043e\u0432\u043e\u0433\u043e\u0434\u043d\u0438\u0439 \u0443\u0436\u0430\u0441", -1)
      .setVisible(() -> this.server.is("ReallyWorld"));
   private final BindSetting darkEssenceKey = new BindSetting(
         "\u042d\u0441\u0441\u0435\u043d\u0446\u0438\u044f \u043a\u0440\u043e\u043c\u0435\u0448\u043d\u0438\u043a\u0430", -1
      )
      .setVisible(() -> this.server.is("ReallyWorld"));
   private final BindSetting snowballKey = new BindSetting("\u0421\u043d\u0435\u0436\u043e\u043a", -1).setVisible(() -> this.server.is("ReallyWorld"));
   private final BindSetting trapKey = new BindSetting("\u0422\u0440\u0430\u043f\u043a\u0430", -1).setVisible(() -> this.server.is("ReallyWorld"));
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
      if (!stack.method_31574(Items.field_8436)) {
         return false;
      } else {
         String name = stack.method_7964().getString().toLowerCase();
         return name.contains("\u0433\u0440\u0438\u043d\u0447");
      }
   }

   private boolean isNewYearHorror(ItemStack stack) {
      if (!stack.method_31574(Items.field_8436)) {
         return false;
      } else {
         String name = stack.method_7964().getString().toLowerCase();
         return name.contains("\u043d\u043e\u0432\u043e\u0433\u043e\u0434\u043d\u0438\u0439 \u0443\u0436\u0430\u0441");
      }
   }

   private boolean isDarkEssence(ItemStack stack) {
      if (!stack.method_31574(Items.field_8436)) {
         return false;
      } else {
         String name = stack.method_7964().getString().toLowerCase();
         return name.contains("\u044d\u0441\u0441\u0435\u043d\u0446\u0438\u044f \u043a\u0440\u043e\u043c\u0435\u0448\u043d\u0438\u043a\u0430");
      }
   }

   private boolean isSnowball(ItemStack stack) {
      if (!stack.method_31574(Items.field_8436)) {
         return false;
      } else {
         String name = stack.method_7964().getString().toLowerCase();
         return name.contains("\u0441\u043d\u0435\u0436\u043e\u043a");
      }
   }

   private boolean isAntiFly(ItemStack stack) {
      return stack.method_31574(Items.field_8450);
   }

   private boolean isTrapItem(ItemStack stack) {
      return stack.method_31574(Items.field_8207);
   }

   private boolean isTrapPotion(ItemStack stack) {
      if (!stack.method_31574(Items.field_8436)) {
         return false;
      } else {
         String name = stack.method_7964().getString().toLowerCase();
         return name.contains("\u043b\u043e\u0432\u0443\u0448") || name.contains("\u043b\u043e\u0432\u0443\u0448\u043a");
      }
   }

   private int findSlot(Predicate<ItemStack> predicate, int from, int to) {
      for (int i2 = from; i2 < to; i2++) {
         if (predicate.test(this.mc.field_1724.method_31548().method_5438(i2))) {
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
      if (this.mc.field_1755 == null) {
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
         if (this.wasInPvp && !inPvp && this.mc.field_1724.field_6012 >= this.nextFixTick) {
            this.mc.field_1724.field_3944.method_45730("fix all");
            this.nextFixTick = this.mc.field_1724.field_6012 + 40;
         }

         this.wasInPvp = inPvp;
      } else {
         this.wasInPvp = this.isPvp();
      }
   }

   private boolean isPvp() {
      return this.mc.field_1724 != null && this.mc.field_1687 != null
         ? this.mc.field_1687.method_18456().stream().filter(p -> p != this.mc.field_1724).anyMatch(p -> p.method_5858(this.mc.field_1724) < 100.0)
         : false;
   }

   private void pulseShift() {
      if (this.mc.field_1690 != null) {
         this.mc.field_1690.field_1832.method_23481(true);
         new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
               if (ServerHelper.this.mc.field_1690 != null) {
                  ServerHelper.this.mc.field_1690.field_1832.method_23481(false);
               }
            }
         }, 150L);
      }
   }

   private void useTrapItem() {
      if (this.mc.field_1724 != null) {
         if (this.isTrapItem(this.mc.field_1724.method_6047())) {
            this.mc.field_1761.method_2919(this.mc.field_1724, Hand.field_5808);
         } else if (this.isTrapItem(this.mc.field_1724.method_6079())) {
            this.mc.field_1761.method_2919(this.mc.field_1724, Hand.field_5810);
         } else {
            int slot = this.findTrapInHotbar();
            if (slot != -1) {
               int prev = this.mc.field_1724.method_31548().field_7545;
               this.mc.field_1724.method_31548().field_7545 = slot;
               this.mc.field_1724.field_3944.method_52787(new UpdateSelectedSlotC2SPacket(slot));
               this.mc.field_1761.method_2919(this.mc.field_1724, Hand.field_5808);
               this.mc.field_1724.method_31548().field_7545 = prev;
               this.mc.field_1724.field_3944.method_52787(new UpdateSelectedSlotC2SPacket(prev));
            } else {
               slot = this.findTrapInInventory();
               if (slot != -1) {
                  int prev = this.mc.field_1724.method_31548().field_7545;
                  int syncId = this.mc.field_1724.field_7512.field_7763;
                  this.mc.field_1761.method_2906(syncId, slot, prev, SlotActionType.field_7791, this.mc.field_1724);
                  this.mc.field_1761.method_2919(this.mc.field_1724, Hand.field_5808);
                  this.mc.field_1761.method_2906(syncId, slot, prev, SlotActionType.field_7791, this.mc.field_1724);
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
      if (this.mc.field_1724 != null && this.mc.method_1562() != null) {
         if (predicate.test(this.mc.field_1724.method_6079())) {
            this.shiftClick();
         } else {
            int hotbarSlot = this.findSlot(predicate, 0, 9);
            if (hotbarSlot != -1) {
               boolean wasSprinting = this.stopSprintingIfNeeded();
               this.swapToOffhand(this.mc.field_1724.field_7512.field_7763, 45, hotbarSlot);
               this.shiftClick();
               this.swapToOffhand(this.mc.field_1724.field_7512.field_7763, 45, hotbarSlot);
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
      this.mc.method_1562().method_52787(new ClientCommandC2SPacket(this.mc.field_1724, Mode.field_12979));
      this.mc.method_1562().method_52787(new ClientCommandC2SPacket(this.mc.field_1724, Mode.field_12984));
   }

   private void swapToOffhand(int syncId, int slot, int button) {
      this.mc.field_1761.method_2906(syncId, slot, button, SlotActionType.field_7791, this.mc.field_1724);
      this.mc.method_1562().method_52787(new CloseHandledScreenC2SPacket(0));
   }

   private boolean stopSprintingIfNeeded() {
      if (!this.mc.field_1724.method_5624()) {
         return false;
      } else {
         this.mc.method_1562().method_52787(new PlayerInputC2SPacket(new PlayerInput(false, false, false, false, false, false, false)));
         this.mc.field_1724.method_5728(false);
         this.mc.method_1562().method_52787(new ClientCommandC2SPacket(this.mc.field_1724, Mode.field_12985));
         if (!Instance.get(Sprint.class).isEnabled()) {
            this.mc.field_1690.field_1867.method_23481(false);
         }

         return true;
      }
   }

   private void resumeSprinting() {
      this.mc.method_1562().method_52787(new PlayerInputC2SPacket(this.mc.field_1724.field_3913.field_54155));
   }
}
