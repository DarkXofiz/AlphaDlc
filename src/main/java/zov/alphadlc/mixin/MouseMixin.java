package zov.alphadlc.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.util.math.Smoother;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zov.alphadlc.AlphaDLC;
import zov.alphadlc.event.list.EventKeyInput;
import zov.alphadlc.event.list.LookEvent;
import zov.alphadlc.module.list.combat.KillAura;

@Mixin({Mouse.class})
public abstract class MouseMixin {
   @Shadow
   @Final
   private MinecraftClient field_1779;
   @Shadow
   private double field_1789;
   @Shadow
   private double field_1787;
   @Final
   @Shadow
   private Smoother field_1793;
   @Final
   @Shadow
   private Smoother field_1782;

   @Inject(
      method = {"updateMouse"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onUpdateMouse(double timeDelta, CallbackInfo ci) {
      if (this.field_1779.field_1724 != null) {
         double sensitivity = (Double)this.field_1779.field_1690.method_42495().method_41753() * 0.6 + 0.2;
         double scaled = sensitivity * sensitivity * sensitivity * 8.0;
         double i;
         double j;
         if (this.field_1779.field_1690.field_1914) {
            i = this.field_1793.method_15429(this.field_1789 * scaled, timeDelta * scaled);
            j = this.field_1782.method_15429(this.field_1787 * scaled, timeDelta * scaled);
         } else if (this.field_1779.field_1690.method_31044().method_31034() && this.field_1779.field_1724.method_31550()) {
            this.field_1793.method_15428();
            this.field_1782.method_15428();
            i = this.field_1789 * sensitivity * sensitivity * sensitivity;
            j = this.field_1787 * sensitivity * sensitivity * sensitivity;
         } else {
            this.field_1793.method_15428();
            this.field_1782.method_15428();
            i = this.field_1789 * scaled;
            j = this.field_1787 * scaled;
         }

         int invert = this.field_1779.field_1690.method_42438().method_41753() ? -1 : 1;
         LookEvent event = new LookEvent(i, j * (double)invert);
         event.post();
         if (Math.abs(event.getYaw()) > 0.01 || Math.abs(event.getPitch()) > 0.01) {
            KillAura.lastPhysicalMoveTime = System.currentTimeMillis();
         }

         if (!event.isCancelled()) {
            this.field_1779.method_1577().method_4908(event.getYaw(), event.getPitch());
            this.field_1779.field_1724.method_5872(event.getYaw(), event.getPitch());
         }

         this.field_1789 = 0.0;
         this.field_1787 = 0.0;
         ci.cancel();
      }
   }

   @Inject(
      method = {"onMouseButton"},
      at = {@At("HEAD")}
   )
   private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
      if (MinecraftClient.method_1551().field_1755 == null && action != 2) {
         AlphaDLC.getInstance().getEventBus().post(new EventKeyInput(button, action));
      }
   }
}
