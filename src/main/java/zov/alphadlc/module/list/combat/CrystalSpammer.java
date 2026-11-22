package zov.alphadlc.module.list.combat;

import com.google.common.eventbus.Subscribe;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.EndCrystalItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult.Success;
import net.minecraft.util.ActionResult.SwingSource;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult.Type;
import zov.alphadlc.event.list.EventKeyInput;
import zov.alphadlc.event.list.EventTick;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.BindSetting;

@ModuleInformation(
   moduleName = "Crystal Spammer",
   moduleDesc = "\u0411\u044b\u0441\u0442\u0440\u0430\u044f \u0443\u0441\u0442\u0430\u043d\u043e\u0432\u043a\u0430 \u043a\u0440\u0438\u0441\u0442\u0430\u043b\u043b\u043e\u0432",
   moduleCategory = ModuleCategory.COMBAT
)
public class CrystalSpammer extends Module {
   private final BindSetting key = new BindSetting("\u041a\u043b\u0430\u0432\u0438\u0448\u0430 \u0441\u043f\u0430\u043c\u0430", -1);
   private boolean active;
   boolean placed;

   @Subscribe
   private void onUpdate(EventTick e) {
      if (this.active && this.mc.field_1724 != null && this.mc.field_1687 != null) {
         if (this.mc.field_1755 != null) {
            this.active = false;
         }

         ClientPlayerEntity player = this.mc.field_1724;
         if (this.holdCrystal(player)) {
            this.placeCrystal();
            if (this.mc.field_1765.method_17783() == Type.field_1331 && this.mc.field_1765 instanceof EntityHitResult hitResult) {
               this.mc.field_1761.method_2918(this.mc.field_1724, hitResult.method_17782());
               player.method_6104(Hand.field_5808);
            }
         }
      }
   }

   @Subscribe
   private void onKey(EventKeyInput e) {
      if (e.getKey() == this.key.getValue()) {
         this.active = e.getAction() == 1 || e.getAction() == 2;
      }
   }

   private boolean holdCrystal(ClientPlayerEntity player) {
      ItemStack main = player.method_6047();
      if (main.method_7909() instanceof EndCrystalItem) {
         return true;
      } else {
         for (int i = 0; i < 9; i++) {
            if (player.method_31548().method_5438(i).method_7909() instanceof EndCrystalItem) {
               player.method_31548().field_7545 = i;
               return true;
            }
         }

         return false;
      }
   }

   private void placeCrystal() {
      if (this.mc.field_1761 != null && this.mc.field_1724 != null) {
         if (this.mc.field_1765 != null && this.mc.field_1765.method_17783() == Type.field_1332) {
            BlockHitResult blockHitResult = (BlockHitResult)this.mc.field_1765;
            ItemStack itemStack = this.mc.field_1724.method_5998(Hand.field_5808);
            int i = itemStack.method_7947();
            if (this.mc.field_1761.method_2896(this.mc.field_1724, Hand.field_5808, blockHitResult) instanceof Success success2
               && success2.comp_2909() == SwingSource.field_52427) {
               this.mc.field_1724.method_6104(Hand.field_5808);
               if (!itemStack.method_7960() && (itemStack.method_7947() != i || this.mc.field_1761.method_2914())) {
                  this.mc.field_1773.field_4012.method_3215(Hand.field_5808);
               }
            }

            this.placed = true;
         }
      }
   }
}
