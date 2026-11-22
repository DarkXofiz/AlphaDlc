package zov.alphadlc.module.list.combat;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import zov.alphadlc.event.list.EventPlayerUpdate;
import zov.alphadlc.event.list.EventPopTotem;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.module.settings.SliderSetting;
import zov.alphadlc.util.math.StopWatch;
import zov.alphadlc.util.player.other.InventoryUtil;
import zov.alphadlc.util.text.ValueUnit;

@ModuleInformation(
   moduleName = "Auto Totem",
   moduleDesc = "\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438 \u0431\u0435\u0440\u0435\u0442 \u0442\u043e\u0442\u0435\u043c \u0432 \u0440\u0443\u043a\u0443",
   moduleCategory = ModuleCategory.COMBAT
)
public class AutoTotem extends Module {
   private final SliderSetting health = new SliderSetting(
      "\u0417\u0434\u043e\u0440\u043e\u0432\u044c\u0435", ValueUnit.abbreviation("\u0425\u041f"), 4.0, 1.0, 20.0, 0.1F
   );
   private final SliderSetting healthOnElytra = new SliderSetting(
      "\u0417\u0434\u043e\u0440\u043e\u0432\u044c\u0435 \u043d\u0430 \u044d\u043b\u0438\u0442\u0440\u0435",
      ValueUnit.abbreviation("\u0425\u041f"),
      11.0,
      1.0,
      20.0,
      0.1F
   );
   private final BooleanSetting crystalsCheck = new BooleanSetting(
      "\u0420\u0430\u0431\u043e\u0442\u0430\u0442\u044c \u043d\u0430 \u043a\u0440\u0438\u0441\u0442\u0430\u043b\u044b", false
   );
   private final BooleanSetting slowness = new BooleanSetting("\u0417\u0430\u043c\u0435\u0434\u043b\u0435\u043d\u0438\u0435", false);
   private final SliderSetting slownessDuration = new SliderSetting(
         "\u0414\u043b\u0438\u0442\u0435\u043b\u044c\u043d\u043e\u0441\u0442\u044c \u0437\u0430\u043c\u0435\u0434\u043b\u0435\u043d\u0438\u044f",
         100.0,
         0.0,
         160.0,
         1.0
      )
      .setVisible(this.slowness::getValue);
   private int swapBackItem = -1;
   private float cooldownTicks;
   private final StopWatch totemStopWatch = new StopWatch();

   @Subscribe
   private void onUpdate(EventPlayerUpdate e) {
      if (this.mc.player != null) {
         if (this.cooldownTicks > 0.0F) {
            this.cooldownTicks--;
         }

         this.updateSwap();
      }
   }

   @Subscribe
   private void onPopTotem(EventPopTotem e) {
      if (this.mc.player != null && e.getPlayer() == this.mc.player) {
         this.cooldownTicks = 5.0F;
      }
   }

   private boolean condition() {
      if (!this.mc.player.isCreative() && !this.mc.player.isSpectator()) {
         boolean crystalsCheck = false;

         for (Entity entity : this.mc.world.getEntities()) {
            if (entity instanceof EndCrystalEntity
               && this.mc.player.getY() >= entity.getY()
               && this.mc.player.getEyePos().distanceTo(entity.getBoundingBox().getCenter()) <= 8.0) {
               crystalsCheck = true;
            }
         }

         return (double)(this.mc.player.getHealth() + this.mc.player.getAbsorptionAmount()) <= this.health.getValue()
            || (double)(this.mc.player.getHealth() + this.mc.player.getAbsorptionAmount()) <= this.healthOnElytra.getValue()
               && this.mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA
            || this.crystalsCheck.getValue() && crystalsCheck;
      } else {
         return false;
      }
   }

   private void updateSwap() {
      ItemStack offHand = this.mc.player.getOffHandStack();
      int totemSlot = InventoryUtil.searchItem(Items.TOTEM_OF_UNDYING);
      if (this.condition() && this.totemStopWatch.isReached(300L) && totemSlot != -1 && offHand.getItem() != Items.TOTEM_OF_UNDYING) {
         if (this.swapBackItem == -1 && !offHand.isEmpty()) {
            this.swapBackItem = totemSlot;
         }

         if (totemSlot >= 0 && totemSlot <= 8) {
            this.swapTotem(() -> this.mc.interactionManager.clickSlot(0, 45, totemSlot, SlotActionType.SWAP, this.mc.player));
         } else if (totemSlot >= 8 && totemSlot <= 45) {
            this.swapTotem(() -> this.mc.interactionManager.clickSlot(0, totemSlot, 40, SlotActionType.SWAP, this.mc.player));
         }

         this.totemStopWatch.reset();
      }

      if (!this.condition() && this.swapBackItem != -1 && this.cooldownTicks == 0.0F) {
         int swapBackSlot = this.swapBackItem;
         if (swapBackSlot >= 0 && swapBackSlot <= 8) {
            this.swapTotem(() -> this.mc.interactionManager.clickSlot(0, 45, swapBackSlot, SlotActionType.SWAP, this.mc.player));
         } else if (swapBackSlot >= 8 && swapBackSlot <= 45) {
            this.swapTotem(() -> this.mc.interactionManager.clickSlot(0, swapBackSlot, 40, SlotActionType.SWAP, this.mc.player));
         }

         this.swapBackItem = -1;
      }
   }

   private void swapTotem(Runnable action) {
      if (this.slowness.getValue()) {
         InventoryUtil.swapWithBypassPolar(action, (long)this.slownessDuration.getValue());
      } else {
         InventoryUtil.swapWithBypassGrim(action);
      }
   }
}
