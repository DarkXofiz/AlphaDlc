package miranware.mixin;

import net.minecraft.client.world.ClientWorld.Properties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import miranware.MiranWare;
import miranware.module.list.render.Ambience;

@Mixin({Properties.class})
public class WorldMixin {
   @Inject(
      method = {"getTimeOfDay"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onGetTimeOfDay(CallbackInfoReturnable<Long> cir) {
      try {
         if (MiranWare.getInstance() == null) {
            return;
         }

         if (MiranWare.getInstance().getModuleStorage() == null) {
            return;
         }

         Ambience ambience = MiranWare.getInstance().getModuleStorage().get(Ambience.class);
         if (ambience != null && ambience.isEnabled()) {
            cir.setReturnValue(ambience.getCustomTime());
         }
      } catch (Exception var3) {
      }
   }
}
