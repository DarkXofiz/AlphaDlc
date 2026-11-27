package miranware.mixin;

import net.minecraft.client.render.entity.model.ElytraEntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

// Java 21 / Fabric 1.21.4 yarn | vanilla wings use Dilation(1.0F) = 4 units thick, cape is 1 unit; -0.5F makes the wing 1 unit thick (UV unaffected)
@Mixin({ElytraEntityModel.class})
public class ElytraEntityModelMixin {
   @ModifyArg(
      method = {"getTexturedModelData"},
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/Dilation;<init>(F)V"),
      index = 0,
      require = 0
   )
   private static float miranware$thinWings(float original) {
      return -0.5F;
   }
}
