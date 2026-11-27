package miranware.module.list.render;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import miranware.event.list.EventTick;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;

@ModuleInformation(
   moduleName = "Full Bright",
   moduleDesc = "Full brightness without darkness",
   moduleCategory = ModuleCategory.RENDER
)
public class FullBright extends Module {
   @Subscribe
   private void onUpdate(EventTick e) {
      if (this.mc.player != null && this.mc.world != null) {
         if (!this.mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
            this.mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, -1, 3));
         }
      }
   }

   @Override
   public void onDisable() {
      if (this.mc.player != null) {
         this.mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
         super.onDisable();
      }
   }
}
