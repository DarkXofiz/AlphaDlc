package miranware.module.list.movement;

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
import miranware.event.list.EventPlayerUpdate;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.util.math.StopWatch;
import miranware.util.packet.NetworkUtils;
import miranware.util.player.other.InventoryUtil;

@ModuleInformation(
   moduleName = "Bib Fly",
   moduleDesc = "Allows flying with a chestplate",
   moduleCategory = ModuleCategory.MOVEMENT
)
public class DogFly extends Module {
   StopWatch stopWatch = new StopWatch();

   @Subscribe
   private void onUpdate(EventPlayerUpdate e) {
      if (InventoryUtil.searchItem(Items.ELYTRA) == -1) {
         this.logDirect(
            new Text[]{
               Text.literal("No elytra!").formatted(Formatting.RED)
            }
         );
         this.setEnabled(false);
      } else if (InventoryUtil.searchItem(Items.FIREWORK_ROCKET) == -1) {
         this.logDirect(
            new Text[]{
               Text.literal("No fireworks!")
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
