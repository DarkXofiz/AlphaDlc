package miranware.module.list.misc;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import miranware.event.list.EventPacket;
import miranware.event.list.EventPlayerUpdate;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.BooleanSetting;
import miranware.util.packet.NetworkUtils;
import miranware.util.player.other.InventoryUtil;

@ModuleInformation(
   moduleName = "RWHelper",
   moduleDesc = "Helper for ReallyWorld",
   moduleCategory = ModuleCategory.MISC
)
public class RWHelper extends Module {
   public final BooleanSetting antipolet = new BooleanSetting("Anti-Fly Bypass", false);
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
