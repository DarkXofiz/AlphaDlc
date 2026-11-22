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
      if (this.active && this.mc.player != null && this.mc.world != null) {
         if (this.mc.currentScreen != null) {
            this.active = false;
         }

         ClientPlayerEntity player = this.mc.player;
         if (this.holdCrystal(player)) {
            this.placeCrystal();
            if (this.mc.crosshairTarget.getType() == Type.ENTITY && this.mc.crosshairTarget instanceof EntityHitResult hitResult) {
               this.mc.interactionManager.attackEntity(this.mc.player, hitResult.getEntity());
               player.swingHand(Hand.MAIN_HAND);
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
      ItemStack main = player.getMainHandStack();
      if (main.getItem() instanceof EndCrystalItem) {
         return true;
      } else {
         for (int i = 0; i < 9; i++) {
            if (player.getInventory().getStack(i).getItem() instanceof EndCrystalItem) {
               player.getInventory().selectedSlot = i;
               return true;
            }
         }

         return false;
      }
   }

   private void placeCrystal() {
      if (this.mc.interactionManager != null && this.mc.player != null) {
         if (this.mc.crosshairTarget != null && this.mc.crosshairTarget.getType() == Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult)this.mc.crosshairTarget;
            ItemStack itemStack = this.mc.player.getStackInHand(Hand.MAIN_HAND);
            int i = itemStack.getCount();
            if (this.mc.interactionManager.interactBlock(this.mc.player, Hand.MAIN_HAND, blockHitResult) instanceof Success success2
               && success2.comp_2909() == SwingSource.CLIENT) {
               this.mc.player.swingHand(Hand.MAIN_HAND);
               if (!itemStack.isEmpty() && (itemStack.getCount() != i || this.mc.interactionManager.hasCreativeInventory())) {
                  this.mc.gameRenderer.firstPersonRenderer.resetEquipProgress(Hand.MAIN_HAND);
               }
            }

            this.placed = true;
         }
      }
   }
}
