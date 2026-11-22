package zov.alphadlc.util.rotation;

import com.google.common.eventbus.Subscribe;
import lombok.Generated;
import net.minecraft.util.math.MathHelper;
import zov.alphadlc.event.list.LookEvent;
import zov.alphadlc.event.list.RotationEvent;

public class FreeLookComponent extends Component {
   private static boolean active;
   private static float freeYaw;
   private static float freePitch;

   @Subscribe
   public void onEvent(LookEvent event) {
      if (active) {
         this.rotateTowards(event.getYaw(), event.getPitch());
         event.cancelEvent();
      }
   }

   @Subscribe
   public void onEvent(RotationEvent event) {
      if (active) {
         event.setYaw(freeYaw);
         event.setPitch(freePitch);
      } else {
         freeYaw = event.getYaw();
         freePitch = event.getPitch();
      }
   }

   private void rotateTowards(double targetYaw, double targetPitch) {
      freePitch = MathHelper.method_15363((float)((double)freePitch + targetPitch * 0.15), -90.0F, 90.0F);
      freeYaw = (float)((double)freeYaw + targetYaw * 0.15);
   }

   @Generated
   public static boolean isActive() {
      return active;
   }

   @Generated
   public static void setActive(boolean active) {
      FreeLookComponent.active = active;
   }

   @Generated
   public static float getFreeYaw() {
      return freeYaw;
   }

   @Generated
   public static float getFreePitch() {
      return freePitch;
   }

   @Generated
   public static void setFreeYaw(float freeYaw) {
      FreeLookComponent.freeYaw = freeYaw;
   }

   @Generated
   public static void setFreePitch(float freePitch) {
      FreeLookComponent.freePitch = freePitch;
   }
}
