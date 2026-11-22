package zov.alphadlc.mixin;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import zov.alphadlc.module.list.render.Cape;

@Mixin({AbstractClientPlayerEntity.class})
public class AbstractClientPlayerEntityMixin {
   private static final Identifier CUSTOM_CAPE = Identifier.of("mre", "textures/entity/cape.png");

   @Inject(
      method = {"getSkinTextures"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void onGetSkinTextures(CallbackInfoReturnable<SkinTextures> cir) {
      Cape capeModule = Cape.getInstance();
      if (capeModule != null && capeModule.shouldRenderCape((AbstractClientPlayerEntity)this)) {
         SkinTextures original = (SkinTextures)cir.getReturnValue();
         cir.setReturnValue(new SkinTextures(original.comp_1626(), original.comp_1911(), CUSTOM_CAPE, CUSTOM_CAPE, original.comp_1629(), original.comp_1630()));
      }
   }
}
