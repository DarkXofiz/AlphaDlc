package miranware.util.render.math;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Rectangle;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;

public class Scissor {
   private static Scissor.State state = new Scissor.State();
   private static final List<Scissor.State> stateStack = Lists.newArrayList();
   private static float guiScale = 1.0F;
   private static float guiCenterX = 0.0F;
   private static float guiCenterY = 0.0F;

   public static void setGuiTransform(float scale, float centerX, float centerY) {
      guiScale = scale;
      guiCenterX = centerX;
      guiCenterY = centerY;
   }

   public static void resetGuiTransform() {
      guiScale = 1.0F;
      guiCenterX = 0.0F;
      guiCenterY = 0.0F;
   }

   public static void push() {
      stateStack.add(state.clone());
   }

   public static void pop() {
      state = stateStack.remove(stateStack.size() - 1);
   }

   public static void unset() {
      RenderSystem.disableScissor();
      state.enabled = false;
   }

   public static void setFromComponentCoordinates(int x, int y, int width, int height) {
      setFromComponentCoordinates((double)x, (double)y, (double)width, (double)height);
   }

   public static void setFromComponentCoordinates(double x, double y, double width, double height) {
      Window window = MinecraftClient.getInstance().getWindow();
      int scaleFactor = (int)window.getScaleFactor();
      if (guiScale != 1.0F) {
         x = (double)guiCenterX + (x - (double)guiCenterX) * (double)guiScale;
         y = (double)guiCenterY + (y - (double)guiCenterY) * (double)guiScale;
         width *= (double)guiScale;
         height *= (double)guiScale;
      }

      int screenX = (int)(x * (double)scaleFactor);
      int screenY = (int)(y * (double)scaleFactor);
      int screenWidth = (int)(width * (double)scaleFactor);
      int screenHeight = (int)(height * (double)scaleFactor);
      screenY = window.getHeight() - screenY - screenHeight;
      set(screenX, screenY, screenWidth, screenHeight);
   }

   public static void setFromComponentCoordinates(double x, double y, double width, double height, float scale) {
      Window window = MinecraftClient.getInstance().getWindow();
      float halfRest = (1.0F - scale) / 2.0F;
      double testX = x + width * (double)halfRest;
      double testY = y + height * (double)halfRest;
      double testW = width * (double)scale;
      double testH = height * (double)scale;
      testX = testX * (double)scale + ((double)window.getScaledWidth() - testW) * (double)halfRest;
      int scaleFactor = (int)window.getScaleFactor();
      int screenX = (int)(testX * (double)scaleFactor);
      int screenY = (int)(testY * (double)scaleFactor);
      int screenWidth = (int)(testW * (double)scaleFactor);
      int screenHeight = (int)(testH * (double)scaleFactor);
      screenY = window.getHeight() - screenY - screenHeight;
      set(screenX, screenY, screenWidth, screenHeight);
   }

   public static void scissor(Window window, double x, double y, double width, double height) {
      if (x + width != x && y + height != y && !(x < 0.0) && !(y + height < 0.0)) {
         double scaleFactor = window.getScaleFactor();
         int sx = (int)Math.round(x * scaleFactor);
         int sy = (int)Math.round(((double)window.getScaledHeight() - (y + height)) * scaleFactor);
         int sw = (int)Math.round(width * scaleFactor);
         int sh = (int)Math.round(height * scaleFactor);
         RenderSystem.enableScissor(sx, sy, sw, sh);
      }
   }

   public static void set(int x, int y, int width, int height) {
      Window window = MinecraftClient.getInstance().getWindow();
      Rectangle screen = new Rectangle(0, 0, window.getWidth(), window.getHeight());
      Rectangle current = state.enabled ? new Rectangle(state.x, state.y, state.width, state.height) : screen;
      Rectangle target = new Rectangle(x + state.transX, y + state.transY, width, height);
      Rectangle result = current.intersection(target).intersection(screen);
      if (result.width < 0) {
         result.width = 0;
      }

      if (result.height < 0) {
         result.height = 0;
      }

      state.enabled = true;
      state.x = result.x;
      state.y = result.y;
      state.width = result.width;
      state.height = result.height;
      RenderSystem.enableScissor(result.x, result.y, result.width, result.height);
   }

   public static void translate(int x, int y) {
      state.transX = x;
      state.transY = y;
   }

   public static void translateFromComponentCoordinates(int x, int y) {
      Window window = MinecraftClient.getInstance().getWindow();
      int scaleFactor = (int)window.getScaleFactor();
      int screenX = x * scaleFactor;
      int screenY = y * scaleFactor;
      screenY = window.getScaledHeight() * scaleFactor - screenY;
      translate(screenX, screenY);
   }

   private static class State implements Cloneable {
      public boolean enabled;
      public int transX;
      public int transY;
      public int x;
      public int y;
      public int width;
      public int height;

      public Scissor.State clone() {
         try {
            return (Scissor.State)super.clone();
         } catch (CloneNotSupportedException var2) {
            throw new AssertionError(var2);
         }
      }
   }
}
