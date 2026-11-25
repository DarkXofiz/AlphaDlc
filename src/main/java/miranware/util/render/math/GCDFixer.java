package miranware.util.render.math;

import lombok.Generated;
import miranware.util.IMinecraft;

public final class GCDFixer implements IMinecraft {
   public static float getFixRotate(float rot) {
      return getDeltaMouse(rot) * getGCDValue();
   }

   public static float getGCDValue() {
      return (float)((double)getGCD() * 0.15);
   }

   public static float getGCD() {
      double var11 = (Double)mc.options.getMouseSensitivity().getValue() / 0.15F / 8.0;
      double var9 = Math.cbrt(var11);
      float f1;
      return (f1 = (float)((var9 - 0.2F) / 0.6F * 0.6 + 0.2)) * f1 * f1 * 8.0F;
   }

   public static float getDeltaMouse(float delta) {
      return (float)Math.round(delta / getGCDValue());
   }

   @Generated
   private GCDFixer() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
