package zov.alphadlc.mixin;

import java.util.Comparator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({EnderPearlEntity.class})
public class EnderPearlEntityMixin {
   @Inject(
      method = {"<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;)V"},
      at = {@At("RETURN")}
   )
   private void fixOwner(CallbackInfo ci) {
      if (MinecraftClient.method_1551().field_1724 != null && MinecraftClient.method_1551().field_1687 != null) {
         EnderPearlEntity entity = (EnderPearlEntity)this;
         entity.method_7432(
            MinecraftClient.method_1551()
               .field_1687
               .method_18456()
               .stream()
               .filter(me -> me.method_5739(entity) > 1.0F)
               .min(Comparator.comparingDouble(me -> (double)me.method_5739(entity)))
               .orElse(null)
         );
      }
   }
}
