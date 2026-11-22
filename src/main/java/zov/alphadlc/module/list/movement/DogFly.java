package zov.alphadlc.module.list.movement;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.PlayerInput;
import zov.alphadlc.event.list.EventPlayerUpdate;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.util.math.StopWatch;
import zov.alphadlc.util.packet.NetworkUtils;
import zov.alphadlc.util.player.other.InventoryUtil;

@ModuleInformation(
   moduleName = "Bib Fly",
   moduleDesc = "\u041f\u043e\u0437\u0432\u043e\u043b\u044f\u0435\u0442 \u043b\u0435\u0442\u0430\u0442\u044c \u043d\u0430 \u043d\u0430\u0433\u0440\u0443\u0434\u043d\u0438\u043a\u0435",
   moduleCategory = ModuleCategory.MOVEMENT
)
public class DogFly extends Module {
   StopWatch stopWatch = new StopWatch();

   @Subscribe
   private void onUpdate(EventPlayerUpdate e) {
      if (InventoryUtil.searchItem(Items.ELYTRA) == -1) {
         this.logDirect(
            new Text[]{
               Text.literal("\u041d\u0435\u043c\u0430 \u044d\u043b\u0438\u0442\u0440 \u043f\u0430\u0434\u043b\u0430").formatted(Formatting.RED)
            }
         );
         this.setEnabled(false);
      } else if (InventoryUtil.searchItem(Items.FIREWORK_ROCKET) == -1) {
         this.logDirect(
            new Text[]{
               Text.literal("\u041d\u0435\u043c\u0430 \u0444\u0435\u0435\u0440\u0432\u0435\u0440\u043a\u043e\u0432 \u043f\u0430\u0434\u043b\u0430")
                  .formatted(Formatting.RED)
            }
         );
         this.setEnabled(false);
      } else {
         if (this.mc.player.isOnGround()
            && !this.mc.player.isInLava()
            && !this.mc.player.isSwimming()
            && !this.mc.player.hasStatusEffect(StatusEffects.LEVITATION)) {
            this.mc.player.jump();
         }

         if (this.stopWatch.isReached(550L)
            && !this.mc.player.isOnGround()
            && !this.mc.player.isSwimming()
            && !this.mc.player.hasStatusEffect(StatusEffects.LEVITATION)) {
            int slot = InventoryUtil.searchItem(Items.ELYTRA);
            int fireworkSlot = InventoryUtil.searchItem(Items.FIREWORK_ROCKET);
            if (slot != -1 && fireworkSlot != -1) {
               if (slot >= 0 && slot <= 8) {
                  InventoryUtil.swapWithBypassGrim(() -> {
                     this.mc.interactionManager.clickSlot(0, 6, slot, SlotActionType.SWAP, this.mc.player);
                     NetworkUtils.sendSilentPacket(new CloseHandledScreenC2SPacket(0));
                     this.mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(this.mc.player, Mode.START_FALL_FLYING));
                     this.mc.player.startGliding();
                     NetworkUtils.sendSilentPacket(new PlayerInputC2SPacket(new PlayerInput(false, false, false, false, true, false, false)));
                     NetworkUtils.sendSilentPacket(new PlayerInputC2SPacket(new PlayerInput(false, false, false, false, false, false, false)));
                     InventoryUtil.swapAndUseHvH(Items.FIREWORK_ROCKET);
                     this.mc.interactionManager.clickSlot(0, 6, slot, SlotActionType.SWAP, this.mc.player);
                  });
               } else {
                  InventoryUtil.swapWithBypassGrim(() -> {
                     this.mc.interactionManager.clickSlot(0, slot, 8, SlotActionType.SWAP, this.mc.player);
                     this.mc.interactionManager.clickSlot(0, 6, 8, SlotActionType.SWAP, this.mc.player);
                     NetworkUtils.sendSilentPacket(new CloseHandledScreenC2SPacket(0));
                     this.mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(this.mc.player, Mode.START_FALL_FLYING));
                     this.mc.player.startGliding();
                     NetworkUtils.sendSilentPacket(new PlayerInputC2SPacket(new PlayerInput(false, false, false, false, true, false, false)));
                     NetworkUtils.sendSilentPacket(new PlayerInputC2SPacket(new PlayerInput(false, false, false, false, false, false, false)));
                     InventoryUtil.swapAndUseHvH(Items.FIREWORK_ROCKET);
                     this.mc.interactionManager.clickSlot(0, 6, 8, SlotActionType.SWAP, this.mc.player);
                     this.mc.interactionManager.clickSlot(0, slot, 8, SlotActionType.SWAP, this.mc.player);
                  });
               }

               this.stopWatch.reset();
            }
         }
      }
   }
}
