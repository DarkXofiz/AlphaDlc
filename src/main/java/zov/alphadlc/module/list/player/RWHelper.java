package zov.alphadlc.module.list.player;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import zov.alphadlc.event.list.EventPacket;
import zov.alphadlc.event.list.EventPlayerUpdate;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.util.packet.NetworkUtils;
import zov.alphadlc.util.player.other.InventoryUtil;

@ModuleInformation(
   moduleName = "RW Helper",
   moduleDesc = "\u041f\u043e\u043c\u043e\u0449\u043d\u0438\u043a \u0434\u043b\u044f ReallyWorld \u0441\u0435\u0440\u0432\u0435\u0440\u0430",
   moduleCategory = ModuleCategory.MISC
)
public class RWHelper extends Module {
   public final BooleanSetting antipolet = new BooleanSetting("\u0410\u043d\u0442\u0438-\u043f\u043e\u043b\u0435\u0442 \u043e\u0431\u0445\u043e\u0434", false);
   boolean need;
   public boolean fireworkUse;

   @Subscribe
   private void onPacket(EventPacket e) {
      if (e.getPacket() instanceof GameMessageS2CPacket p
         && p.content()
            .getString()
            .contains(
               "\u0410\u043d\u0442\u0438 \u041f\u043e\u043b\u0435\u0442 \u00bb \u0412\u044b \u043d\u0435 \u043c\u043e\u0436\u0435\u0442\u0435 \u0432\u0437\u043b\u0435\u0442\u0435\u0442\u044c!"
            )) {
         this.need = true;
      }
   }

   @Subscribe
   private void onPlayerUpdate(EventPlayerUpdate e) {
      if (this.antipolet.getValue()) {
         if (this.need) {
            if (!this.mc.player.isOnGround() && this.mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
               NetworkUtils.sendPacket(new ClientCommandC2SPacket(this.mc.player, Mode.START_FALL_FLYING));
               this.mc.player.startGliding();
               if (this.fireworkUse) {
                  InventoryUtil.swapAndUseHvH(Items.FIREWORK_ROCKET);
                  this.fireworkUse = false;
               }
            } else {
               this.need = false;
            }
         }
      }
   }
}
