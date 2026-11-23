package miranware.module.list.combat;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.LivingEntity;
import miranware.event.list.EventEntityHitBox;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.BooleanSetting;

@ModuleInformation(
   moduleName = "NoEntityTrace",
   moduleDesc = "Increases entity hitboxes",
   moduleCategory = ModuleCategory.COMBAT
)
public class NoEntityTrace extends Module {
   private final BooleanSetting invisible = new BooleanSetting("Invisibles", true);

   @Subscribe
   private void onEntityHitBox(EventEntityHitBox e2) {
      if (e2.getEntity() instanceof LivingEntity entity2) {
         if (!entity2.isInvisible() || this.invisible.getValue()) {
            e2.setSize(-0.75F);
         }
      }
   }
}
