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
      if (this.mc.field_1724.field_6017 == 0.0F && !this.mc.field_1724.method_24828()) {
         this.mc.field_1724.field_6017 = 0.001F;
         this.mc
            .field_1724
            .field_3944
            .method_52787(
               new Full(
                  this.mc.field_1724.method_23317(),
                  this.mc.field_1724.method_23318() - 9.99999999E-5,
                  this.mc.field_1724.method_23321(),
                  this.mc.field_1724.method_36454(),
                  this.mc.field_1724.method_36455(),
                  false,
                  this.mc.field_1724.field_5976
               )
            );
      }
   }
}
