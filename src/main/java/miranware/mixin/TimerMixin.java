package miranware.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import miranware.util.math.TimerUtil;

@Mixin({MinecraftClient.class})
public abstract class TimerMixin {
   @Unique
   private long miranwareLastRealTime = 0L;
   @Unique
   private long miranwareFakeTime = 0L;

   @ModifyArg(
      method = {"render"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/RenderTickCounter$Dynamic;beginRenderTick(JZ)I"
      ),
      index = 0
   )
   private long modifyRenderTime(long currentTime) {
      if (TimerUtil.speed == 1.0F) {
         this.miranwareLastRealTime = currentTime;
         this.miranwareFakeTime = currentTime;
         return currentTime;
      } else if (this.miranwareLastRealTime == 0L) {
         this.miranwareLastRealTime = currentTime;
         this.miranwareFakeTime = currentTime;
         return currentTime;
      } else {
         long realElapsed = currentTime - this.miranwareLastRealTime;
         this.miranwareLastRealTime = currentTime;
         this.miranwareFakeTime = this.miranwareFakeTime + (long)((double)realElapsed * (double)TimerUtil.speed);
         return this.miranwareFakeTime;
      }
   }
}
