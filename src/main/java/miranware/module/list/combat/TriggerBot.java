package miranware.module.list.combat;

import com.google.common.eventbus.Subscribe;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Hand;
import miranware.event.list.EventTick;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.BooleanSetting;

@ModuleInformation(
   moduleName = "Trigger Bot",
   moduleDesc = "Automatic attack when aiming",
   moduleCategory = ModuleCategory.COMBAT
)
public class TriggerBot extends Module {
   public final BooleanSetting pauseEating = new BooleanSetting(
      "Stop While Eating", true
   );
   public final BooleanSetting onlyCriticals = new BooleanSetting("Crits Only", true);
   public final BooleanSetting spaceOnly = new BooleanSetting("Smart Crits", false);
   private int delay;

   @Subscribe
   public void onEvent(EventTick e2) {
      if (this.mc.player != null) {
         if (!this.mc.player.isUsingItem() || !this.pauseEating.getValue()) {
            if (this.delay > 0) {
               this.delay--;
            } else if (this.autoCrit()) {
               Entity ent = this.mc.targetedEntity;
               if (ent != null) {
                  this.mc.interactionManager.attackEntity(this.mc.player, ent);
                  this.mc.player.swingHand(Hand.MAIN_HAND);
                  this.delay = 10;
               }
            }
         }
      }
   }

   @Override
   public void onDisable() {
      this.delay = 0;
      super.onDisable();
   }

   private boolean autoCrit() {
      boolean reasonForSkipCrit = !this.onlyCriticals.getValue()
         || this.mc.player.getAbilities().flying
         || this.mc.player.hasStatusEffect(StatusEffects.LEVITATION)
         || this.mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
         || this.mc.world.getBlockState(this.mc.player.getBlockPos()).getBlock() == Blocks.LADDER;
      float f2 = this.mc.player.getAttackCooldownProgress(0.5F);
      float f3 = this.mc.player.isOnGround() ? 1.0F : 0.9F;
      if (f2 < f3) {
         return false;
      } else {
         boolean mergeWithSpeed = this.mc.player.isOnGround();
         if (!this.mc.options.jumpKey.isPressed() && mergeWithSpeed && this.spaceOnly.getValue()) {
            return true;
         } else if (this.mc.player.isInLava()) {
            return true;
         } else {
            return reasonForSkipCrit ? true : !this.mc.player.isOnGround() && this.mc.player.fallDistance > 0.0F;
         }
      }
   }
}
