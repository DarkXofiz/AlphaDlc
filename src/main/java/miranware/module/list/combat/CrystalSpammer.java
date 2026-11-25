package miranware.module.list.combat;

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
import miranware.event.list.EventKeyInput;
import miranware.event.list.EventTick;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.BindSetting;

@ModuleInformation(
   moduleName = "Crystal Spammer",
   moduleDesc = "Fast crystal placement",
   moduleCategory = ModuleCategory.COMBAT
)
public class CrystalSpammer extends Module {
   private final BindSetting key = new BindSetting("Spam Key", -1);
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
               && success2.swingSource() == SwingSource.CLIENT) {
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
