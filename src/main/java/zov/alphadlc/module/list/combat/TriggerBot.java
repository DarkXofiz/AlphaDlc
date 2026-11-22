package zov.alphadlc.module.list.combat;

import com.google.common.eventbus.Subscribe;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Hand;
import zov.alphadlc.event.list.EventTick;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.BooleanSetting;

@ModuleInformation(
   moduleName = "Trigger Bot",
   moduleDesc = "\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0430\u044f \u0430\u0442\u0430\u043a\u0430 \u043f\u0440\u0438 \u043d\u0430\u0432\u0435\u0434\u0435\u043d\u0438\u0438",
   moduleCategory = ModuleCategory.COMBAT
)
public class TriggerBot extends Module {
   public final BooleanSetting pauseEating = new BooleanSetting(
      "\u041e\u0441\u0442\u0430\u043d\u043e\u0432\u043a\u0430 \u043f\u0440\u0438 \u0435\u0434\u0435", true
   );
   public final BooleanSetting onlyCriticals = new BooleanSetting("\u0422\u043e\u043b\u044c\u043a\u043e \u043a\u0440\u0438\u0442\u044b", true);
   public final BooleanSetting spaceOnly = new BooleanSetting("\u0423\u043c\u043d\u044b\u0435 \u043a\u0440\u0438\u0442\u044b", false);
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
