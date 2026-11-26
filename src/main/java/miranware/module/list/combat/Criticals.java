package miranware.module.list.combat;

import com.google.common.eventbus.Subscribe;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import miranware.event.list.EventAttack;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;

@ModuleInformation(
   moduleName = "Criticals",
   moduleDesc = "All hits will be critical",
   moduleCategory = ModuleCategory.COMBAT
)
public class Criticals extends Module {
   @Subscribe
   private void onAttack(EventAttack e) {
      if (this.mc.player.fallDistance == 0.0F && !this.mc.player.isOnGround()) {
         this.mc.player.fallDistance = 0.001F;
         this.mc
            .player
            .networkHandler
            .sendPacket(
               new Full(
                  this.mc.player.getX(),
                  this.mc.player.getY() - 9.99999999E-5,
                  this.mc.player.getZ(),
                  this.mc.player.getYaw(),
                  this.mc.player.getPitch(),
                  false,
                  this.mc.player.horizontalCollision
               )
            );
      }
   }
}
