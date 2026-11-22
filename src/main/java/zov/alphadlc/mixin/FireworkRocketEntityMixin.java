package zov.alphadlc.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zov.alphadlc.event.list.FireworkEvent;

@Mixin({FireworkRocketEntity.class})
public abstract class FireworkRocketEntityMixin {
   @Shadow
   public LivingEntity field_7616;
   @Unique
   private static final MinecraftClient mc = MinecraftClient.method_1551();

   @ModifyConstant(
      method = {"tick"},
      constant = {@Constant(
         doubleValue = 1.5
      )}
   )
   private double replaceSpeed(double original) {
      FireworkEvent event = new FireworkEvent(this.field_7616, (float)original);
      event.post();
      return (double)event.getSpeed();
   }

   @Inject(
      method = {"tick"},
      at = {@At("HEAD")}
   )
   private void onTick(CallbackInfo ci) {
      if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1724.method_6128()) {
         if (this.field_7616 != mc.field_1724) {
            if (!this.hasActiveOwnFirework()) {
               FireworkRocketEntity self = (FireworkRocketEntity)this;
               Vec3d fireworkPos = self.method_19538();
               Vec3d playerPos = mc.field_1724.method_19538();
               double distance = fireworkPos.method_1022(playerPos);
               if (distance < 5.0) {
                  Vec3d fireworkVel = self.method_18798();
                  if (fireworkVel.method_1027() < 0.01) {
                     return;
                  }

                  Vec3d toPlayer = playerPos.method_1020(fireworkPos).method_1029();
                  double dot = fireworkVel.method_1029().method_1026(toPlayer);
                  if (dot > 0.4) {
                     Vec3d fireworkDir = fireworkVel.method_1029();
                     Vec3d right = new Vec3d(fireworkDir.field_1352, 0.0, -fireworkDir.field_1350).method_1029();
                     double dotRight = toPlayer.method_1026(right);
                     if (dotRight < 0.0) {
                        right = right.method_1021(-1.0);
                     }

                     double urgency = 1.0 - distance / 5.0;
                     double strength = 0.25 * urgency;
                     double upward = 0.1 * urgency;
                     Vec3d currentVel = mc.field_1724.method_18798();
                     Vec3d dodge = right.method_1021(strength).method_1031(0.0, upward, 0.0);
                     mc.field_1724.method_18799(currentVel.method_1019(dodge));
                  }
               }
            }
         }
      }
   }

   @Unique
   private boolean hasActiveOwnFirework() {
      for (Entity entity : mc.field_1687.method_18112()) {
         if (entity instanceof FireworkRocketEntity firework) {
            IFireworkRocketEntityAccessor accessor = (IFireworkRocketEntityAccessor)firework;
            if (accessor.getShooter() == mc.field_1724) {
               return true;
            }
         }
      }

      return false;
   }
}
