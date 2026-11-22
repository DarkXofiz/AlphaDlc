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
      if (InventoryUtil.searchItem(Items.field_8833) == -1) {
         this.logDirect(
            new Text[]{
               Text.method_43470("\u041d\u0435\u043c\u0430 \u044d\u043b\u0438\u0442\u0440 \u043f\u0430\u0434\u043b\u0430").method_27692(Formatting.field_1061)
            }
         );
         this.setEnabled(false);
      } else if (InventoryUtil.searchItem(Items.field_8639) == -1) {
         this.logDirect(
            new Text[]{
               Text.method_43470("\u041d\u0435\u043c\u0430 \u0444\u0435\u0435\u0440\u0432\u0435\u0440\u043a\u043e\u0432 \u043f\u0430\u0434\u043b\u0430")
                  .method_27692(Formatting.field_1061)
            }
         );
         this.setEnabled(false);
      } else {
         if (this.mc.field_1724.method_24828()
            && !this.mc.field_1724.method_5771()
            && !this.mc.field_1724.method_5681()
            && !this.mc.field_1724.method_6059(StatusEffects.field_5902)) {
            this.mc.field_1724.method_6043();
         }

         if (this.stopWatch.isReached(550L)
            && !this.mc.field_1724.method_24828()
            && !this.mc.field_1724.method_5681()
            && !this.mc.field_1724.method_6059(StatusEffects.field_5902)) {
            int slot = InventoryUtil.searchItem(Items.field_8833);
            int fireworkSlot = InventoryUtil.searchItem(Items.field_8639);
            if (slot != -1 && fireworkSlot != -1) {
               if (slot >= 0 && slot <= 8) {
                  InventoryUtil.swapWithBypassGrim(() -> {
                     this.mc.field_1761.method_2906(0, 6, slot, SlotActionType.field_7791, this.mc.field_1724);
                     NetworkUtils.sendSilentPacket(new CloseHandledScreenC2SPacket(0));
                     this.mc.field_1724.field_3944.method_52787(new ClientCommandC2SPacket(this.mc.field_1724, Mode.field_12982));
                     this.mc.field_1724.method_23669();
                     NetworkUtils.sendSilentPacket(new PlayerInputC2SPacket(new PlayerInput(false, false, false, false, true, false, false)));
                     NetworkUtils.sendSilentPacket(new PlayerInputC2SPacket(new PlayerInput(false, false, false, false, false, false, false)));
                     InventoryUtil.swapAndUseHvH(Items.field_8639);
                     this.mc.field_1761.method_2906(0, 6, slot, SlotActionType.field_7791, this.mc.field_1724);
                  });
               } else {
                  InventoryUtil.swapWithBypassGrim(() -> {
                     this.mc.field_1761.method_2906(0, slot, 8, SlotActionType.field_7791, this.mc.field_1724);
                     this.mc.field_1761.method_2906(0, 6, 8, SlotActionType.field_7791, this.mc.field_1724);
                     NetworkUtils.sendSilentPacket(new CloseHandledScreenC2SPacket(0));
                     this.mc.field_1724.field_3944.method_52787(new ClientCommandC2SPacket(this.mc.field_1724, Mode.field_12982));
                     this.mc.field_1724.method_23669();
                     NetworkUtils.sendSilentPacket(new PlayerInputC2SPacket(new PlayerInput(false, false, false, false, true, false, false)));
                     NetworkUtils.sendSilentPacket(new PlayerInputC2SPacket(new PlayerInput(false, false, false, false, false, false, false)));
                     InventoryUtil.swapAndUseHvH(Items.field_8639);
                     this.mc.field_1761.method_2906(0, 6, 8, SlotActionType.field_7791, this.mc.field_1724);
                     this.mc.field_1761.method_2906(0, slot, 8, SlotActionType.field_7791, this.mc.field_1724);
                  });
               }

               this.stopWatch.reset();
            }
         }
      }
   }
}
