package zov.alphadlc.util.math;

import lombok.Generated;
import net.minecraft.util.math.MathHelper;
import zov.alphadlc.util.IMinecraft;

public final class Counter implements IMinecraft {
   private static int currentFPS;

   public static void updateFPS() {
      int prevFPS = mc.method_47599();
      currentFPS = MathHelper.method_48781(0.5F, prevFPS, currentFPS);
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
