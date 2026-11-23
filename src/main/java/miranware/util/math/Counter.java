package miranware.util.math;

import lombok.Generated;
import net.minecraft.util.math.MathHelper;
import miranware.util.IMinecraft;

public final class Counter implements IMinecraft {
   private static int currentFPS;

   public static void updateFPS() {
      int prevFPS = mc.getCurrentFps();
      currentFPS = MathHelper.lerp(0.5F, prevFPS, currentFPS);
   }

   @Generated
   private Counter() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   @Generated
   public static int getCurrentFPS() {
      return currentFPS;
   }
}
