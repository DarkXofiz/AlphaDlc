package miranware.module.list.render;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.effect.StatusEffects;
import miranware.event.list.EventTick;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.BooleanSetting;
import miranware.module.settings.ModeListSetting;

@ModuleInformation(
   moduleName = "Removals",
   moduleDesc = "Removes extra interface elements",
   moduleCategory = ModuleCategory.RENDER
)
public class NoRender extends Module {
   public final ModeListSetting elements = new ModeListSetting(
      "Remove Elements",
      new BooleanSetting("Fire", true),
      new BooleanSetting("Underwater Blur", true),
      new BooleanSetting("Inside Block Vision", true),
      new BooleanSetting("Camera Clip", true),
      new BooleanSetting("Camera Shake", true),
      new BooleanSetting("Totem", true),
      new BooleanSetting("Fishing Rod", true),
      new BooleanSetting("Rain", true),
      new BooleanSetting("Bad Effects", true)
   );

   @Subscribe
   public void onUpdate(EventTick e) {
      if (this.mc.player != null) {
         if (this.elements.isEnabled("Camera Shake")) {
            this.mc.options.getDamageTiltStrength().setValue(0.0);
         } else {
            this.mc.options.getDamageTiltStrength().setValue(0.5);
         }

         if (this.elements.isEnabled("Bad Effects")) {
            this.mc.player.removeStatusEffect(StatusEffects.BLINDNESS);
            this.mc.player.removeStatusEffect(StatusEffects.NAUSEA);
            this.mc.player.removeStatusEffect(StatusEffects.MINING_FATIGUE);
            this.mc.player.removeStatusEffect(StatusEffects.WEAKNESS);
            this.mc.player.removeStatusEffect(StatusEffects.SLOWNESS);
            this.mc.player.removeStatusEffect(StatusEffects.WITHER);
            this.mc.player.removeStatusEffect(StatusEffects.POISON);
            this.mc.player.removeStatusEffect(StatusEffects.HUNGER);
            this.mc.player.removeStatusEffect(StatusEffects.LEVITATION);
            this.mc.player.removeStatusEffect(StatusEffects.UNLUCK);
            this.mc.player.removeStatusEffect(StatusEffects.DARKNESS);
         }
      }
   }

   @Override
   public void onDisable() {
      this.mc.options.getDamageTiltStrength().setValue(0.5);
      super.onDisable();
   }
}
