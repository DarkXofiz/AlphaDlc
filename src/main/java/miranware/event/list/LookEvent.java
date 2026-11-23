package miranware.event.list;

import lombok.Generated;
import miranware.event.Event;

public class LookEvent extends Event {
   private double yaw;
   private double pitch;

   @Generated
   public double getYaw() {
      return this.yaw;
   }

   @Generated
   public double getPitch() {
      return this.pitch;
   }

   @Generated
   public LookEvent(double yaw, double pitch) {
      this.yaw = yaw;
      this.pitch = pitch;
   }
}
