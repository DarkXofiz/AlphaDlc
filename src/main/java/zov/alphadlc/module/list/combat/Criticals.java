package zov.alphadlc.module.list.combat;

import com.google.common.eventbus.Subscribe;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import zov.alphadlc.event.list.EventAttack;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;

@ModuleInformation(
   moduleName = "Criticals",
   moduleDesc = "\u0412\u0441\u0435 \u0443\u0434\u0430\u0440\u044b \u0431\u0443\u0434\u0443\u0442 \u043a\u0440\u0438\u0442\u0438\u0447\u0435\u0441\u043a\u0438\u043c\u0438",
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
