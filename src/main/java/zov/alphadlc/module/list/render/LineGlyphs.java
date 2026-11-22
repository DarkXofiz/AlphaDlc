package zov.alphadlc.module.list.render;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.Last;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import zov.alphadlc.event.list.EventTick;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.module.settings.ColorSetting;
import zov.alphadlc.module.settings.ModeSetting;
import zov.alphadlc.module.settings.SliderSetting;
import zov.alphadlc.util.render.providers.ColorProvider;

@ModuleInformation(
   moduleName = "Line Glyphs",
   moduleDesc = "\u041f\u0443\u043d\u043a\u0442\u0438\u0440\u043d\u044b\u0435 \u0433\u043b\u0438\u0444\u044b-\u043b\u0438\u043d\u0438\u0438 \u0441\u043e \u0441\u0432\u0435\u0442\u044f\u0449\u0438\u043c\u0438\u0441\u044f \u0442\u043e\u0447\u043a\u0430\u043c\u0438",
   moduleCategory = ModuleCategory.RENDER
)
public class LineGlyphs extends Module {
   private static final int SPAWN_ATTEMPTS_PER_TICK = 4;
   private final SliderSetting count = new SliderSetting("\u041a\u043e\u043b\u0438\u0447\u0435\u0441\u0442\u0432\u043e", 70.0, 10.0, 200.0, 5.0);
   private final BooleanSetting slow = new BooleanSetting("\u041c\u0435\u0434\u043b\u0435\u043d\u043d\u043e", true);
   private final SliderSetting thickness = new SliderSetting("\u0422\u043e\u043b\u0449\u0438\u043d\u0430", 2.0, 0.5, 5.0, 0.1F);
   private final SliderSetting dashLength = new SliderSetting("\u0414\u043b\u0438\u043d\u0430 \u0434\u044d\u0448\u0430", 0.12F, 0.03F, 1.0, 0.01F);
   private final SliderSetting dashGap = new SliderSetting("\u041f\u0440\u043e\u043c\u0435\u0436\u0443\u0442\u043e\u043a", 0.08F, 0.02F, 1.0, 0.01F);
   private final SliderSetting opacity = new SliderSetting("\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c", 1.0, 0.1F, 1.0, 0.05F);
   private final SliderSetting spawnRadius = new SliderSetting(
      "\u0420\u0430\u0434\u0438\u0443\u0441 \u0441\u043f\u0430\u0432\u043d\u0430", 16.0, 4.0, 48.0, 1.0
   );
   private final BooleanSetting glowDots = new BooleanSetting("\u0413\u043b\u043e\u0443-\u0442\u043e\u0447\u043a\u0438", true);
   private final SliderSetting glowSize = new SliderSetting("\u0420\u0430\u0437\u043c\u0435\u0440 \u0442\u043e\u0447\u0435\u043a", 0.12F, 0.03F, 0.4F, 0.01F)
      .setVisible(this.glowDots::getValue);
   private final ModeSetting colorMode = new ModeSetting(
      "\u0420\u0435\u0436\u0438\u043c \u0446\u0432\u0435\u0442\u0430",
      "\u041a\u043b\u0438\u0435\u043d\u0442\u0441\u043a\u0438\u0439",
      "\u041a\u043b\u0438\u0435\u043d\u0442\u0441\u043a\u0438\u0439",
      "\u0420\u0430\u0434\u0443\u0436\u043d\u044b\u0439",
      "\u041a\u0430\u0441\u0442\u043e\u043c"
   );
   private final ColorSetting customColor = new ColorSetting("\u0426\u0432\u0435\u0442", -9021441)
      .setVisible(() -> this.colorMode.is("\u041a\u0430\u0441\u0442\u043e\u043c"));
   private final List<LineGlyphs.Glyph> glyphs = new ArrayList<>();
   private final Random random = new Random();
   private boolean registered = false;
   private final Last listener = context -> {
      if (this.isEnabled()) {
         this.onRender(context.matrixStack(), context.camera());
      }
   };

   @Override
   public void onEnable() {
      super.onEnable();
      this.glyphs.clear();
      if (!this.registered) {
         WorldRenderEvents.LAST.register(this.listener);
         this.registered = true;
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.glyphs.clear();
   }

   @Subscribe
   private void onTick(EventTick e) {
      if (this.mc.player != null && this.mc.world != null) {
         this.glyphs.removeIf(LineGlyphs.Glyph::isDead);
         int cap = this.count.getIntValue();
         int attempts = 4;

         while (attempts-- > 0 && this.glyphs.size() < cap) {
            this.glyphs.add(new LineGlyphs.Glyph(this.spawnPos(), 7 + this.random.nextInt(6)));
         }

         for (LineGlyphs.Glyph glyph : this.glyphs) {
            glyph.tick();
         }
      }
   }

   private void onRender(MatrixStack matrix, Camera camera) {
      if (!this.glyphs.isEmpty()) {
         Vec3d cam = camera.getPos();
         Matrix4f base = matrix.peek().getPositionMatrix();
         float baseOpacity = this.opacity.getFloatValue();
         float halfWidth = this.thickness.getFloatValue() * 0.02F;
         float dash = this.dashLength.getFloatValue();
         float gap = this.dashGap.getFloatValue();
         float cullRadius = this.spawnRadius.getFloatValue() * 1.75F;
         double cullDistSq = (double)(cullRadius * cullRadius);
         boolean dots = this.glowDots.getValue();
         float dotSize = this.glowSize.getFloatValue();
         RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
         RenderSystem.disableCull();
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
         int glyphIndex = 0;

         for (LineGlyphs.Glyph glyph : this.glyphs) {
            if (glyph.nodes.size() < 2) {
               glyphIndex++;
            } else {
               int[] first = glyph.nodes.get(0);
               double ddx = (double)first[0] - cam.x;
               double ddy = (double)first[1] - cam.y;
               double ddz = (double)first[2] - cam.z;
               if (ddx * ddx + ddy * ddy + ddz * ddz > cullDistSq) {
                  glyphIndex++;
               } else {
                  float alphaBase = glyph.alpha() * baseOpacity;
                  if (alphaBase <= 0.01F) {
                     glyphIndex++;
                  } else {
                     double[] a = new double[3];
                     double[] b = new double[3];
                     int size = glyph.nodes.size();

                     for (int i = 1; i < size; i++) {
                        glyph.getNodeCoord(i - 1, a);
                        glyph.getNodeCoord(i, b);
                        float segAlpha = alphaBase * (0.35F + (float)i / (float)size * 0.65F);
                        int color = ColorProvider.setAlpha(this.baseColor(glyphIndex + i * 40), (int)(MathHelper.clamp(segAlpha, 0.0F, 1.0F) * 255.0F));
                        this.appendDashes(
                           buffer,
                           base,
                           a[0] - cam.x,
                           a[1] - cam.y,
                           a[2] - cam.z,
                           b[0] - cam.x,
                           b[1] - cam.y,
                           b[2] - cam.z,
                           halfWidth,
                           dash,
                           gap,
                           color
                        );
                     }

                     glyphIndex++;
                  }
               }
            }
         }

         try {
            BuiltBuffer builtBuffer = buffer.end();
            if (builtBuffer != null) {
               BufferRenderer.drawWithGlobalProgram(builtBuffer);
            }
         } catch (IllegalStateException var33) {
         }

         if (dots) {
            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
            RenderSystem.setShaderTexture(0, Identifier.of("mre", "images/glow.png"));
            BufferBuilder dotBuffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            glyphIndex = 0;

            for (LineGlyphs.Glyph glyphx : this.glyphs) {
               if (glyphx.nodes.isEmpty()) {
                  glyphIndex++;
               } else {
                  float alphaBase = glyphx.alpha() * baseOpacity;
                  if (alphaBase <= 0.01F) {
                     glyphIndex++;
                  } else {
                     double[] a = new double[3];
                     int size = glyphx.nodes.size();

                     for (int i = 0; i < size; i++) {
                        glyphx.getNodeCoord(i, a);
                        int color = this.baseColor(glyphIndex + i * 40);
                        this.appendGlowDot(
                           dotBuffer, matrix, camera, a[0] - cam.x, a[1] - cam.y, a[2] - cam.z, dotSize, color, alphaBase
                        );
                     }

                     glyphIndex++;
                  }
               }
            }

            try {
               BuiltBuffer dotBuiltBuffer = dotBuffer.end();
               if (dotBuiltBuffer != null) {
                  BufferRenderer.drawWithGlobalProgram(dotBuiltBuffer);
               }
            } catch (IllegalStateException var32) {
            }

            RenderSystem.setShaderTexture(0, 0);
         }

         RenderSystem.depthMask(true);
         RenderSystem.enableDepthTest();
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
         RenderSystem.defaultBlendFunc();
      }
   }

   private void appendDashes(
      BufferBuilder buffer,
      Matrix4f matrix,
      double x1,
      double y1,
      double z1,
      double x2,
      double y2,
      double z2,
      float halfWidth,
      float dash,
      float gap,
      int color
   ) {
      double dx = x2 - x1;
      double dy = y2 - y1;
      double dz = z2 - z1;
      double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (!(len < 1.0E-6)) {
         dx /= len;
         dy /= len;
         dz /= len;
         double period = Math.max(0.01, (double)(dash + gap));

         for (double t = 0.0; t < len; t += period) {
            double t2 = Math.min(t + (double)dash, len);
            double ax = x1 + dx * t;
            double ay = y1 + dy * t;
            double az = z1 + dz * t;
            double bx = x1 + dx * t2;
            double by = y1 + dy * t2;
            double bz = z1 + dz * t2;
            this.appendStick(buffer, matrix, ax, ay, az, bx, by, bz, dx, dy, dz, halfWidth, color);
         }
      }
   }

   private void appendStick(
      BufferBuilder buffer,
      Matrix4f matrix,
      double ax,
      double ay,
      double az,
      double bx,
      double by,
      double bz,
      double dx,
      double dy,
      double dz,
      float halfWidth,
      int color
   ) {
      double mx = (ax + bx) * 0.5;
      double my = (ay + by) * 0.5;
      double mz = (az + bz) * 0.5;
      double viewLen = Math.sqrt(mx * mx + my * my + mz * mz);
      double vx;
      double vy;
      double vz;
      if (viewLen < 1.0E-6) {
         vx = 0.0;
         vy = 0.0;
         vz = 1.0;
      } else {
         vx = mx / viewLen;
         vy = my / viewLen;
         vz = mz / viewLen;
      }

      double ox = dy * vz - dz * vy;
      double oy = dz * vx - dx * vz;
      double oz = dx * vy - dy * vx;
      double olen = Math.sqrt(ox * ox + oy * oy + oz * oz);
      if (olen < 1.0E-6) {
         ox = -dz;
         oy = 0.0;
         oz = dx;
         olen = Math.sqrt(ox * ox + dx * dx);
         if (olen < 1.0E-6) {
            ox = 1.0;
            oy = 0.0;
            oz = 0.0;
            olen = 1.0;
         }
      }

      ox = ox / olen * (double)halfWidth;
      oy = oy / olen * (double)halfWidth;
      oz = oz / olen * (double)halfWidth;
      this.quad(buffer, matrix, ax, ay, az, bx, by, bz, ox, oy, oz, color);
   }

   private void quad(
      BufferBuilder buffer, Matrix4f m, double ax, double ay, double az, double bx, double by, double bz, double ox, double oy, double oz, int color
   ) {
      buffer.vertex(m, (float)(ax + ox), (float)(ay + oy), (float)(az + oz)).color(color);
      buffer.vertex(m, (float)(ax - ox), (float)(ay - oy), (float)(az - oz)).color(color);
      buffer.vertex(m, (float)(bx - ox), (float)(by - oy), (float)(bz - oz)).color(color);
      buffer.vertex(m, (float)(bx + ox), (float)(by + oy), (float)(bz + oz)).color(color);
   }

   private void appendGlowDot(BufferBuilder buffer, MatrixStack stack, Camera camera, double x, double y, double z, float size, int color, float alpha) {
      stack.push();
      stack.translate(x, y, z);
      stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
      stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
      Matrix4f m = stack.peek().getPositionMatrix();
      int outer = ColorProvider.setAlpha(color, (int)(MathHelper.clamp(alpha * 0.3F, 0.0F, 1.0F) * 255.0F));
      float outerSize = size * 3.0F;
      this.billboardQuadTextured(buffer, m, outerSize, outer);
      int inner = ColorProvider.setAlpha(color, (int)(MathHelper.clamp(alpha, 0.0F, 1.0F) * 255.0F));
      this.billboardQuadTextured(buffer, m, size, inner);
      stack.pop();
   }

   private void billboardQuadTextured(BufferBuilder buffer, Matrix4f m, float size, int color) {
      float half = size / 2.0F;
      int r = color >> 16 & 0xFF;
      int g = color >> 8 & 0xFF;
      int b = color & 0xFF;
      int a = color >> 24 & 0xFF;
      buffer.vertex(m, -half, half, 0.0F).texture(0.0F, 0.0F).color(r, g, b, a);
      buffer.vertex(m, half, half, 0.0F).texture(1.0F, 0.0F).color(r, g, b, a);
      buffer.vertex(m, half, -half, 0.0F).texture(1.0F, 1.0F).color(r, g, b, a);
      buffer.vertex(m, -half, -half, 0.0F).texture(0.0F, 1.0F).color(r, g, b, a);
   }

   private int baseColor(int index) {
      String var2 = this.colorMode.getValue();

      return switch (var2) {
         case "\u0420\u0430\u0434\u0443\u0436\u043d\u044b\u0439" -> {
            float hue = ((float)System.currentTimeMillis() / 20.0F + (float)index * 8.0F) % 360.0F / 360.0F;
            yield 0xFF000000 | Color.HSBtoRGB(hue, 0.7F, 1.0F) & 16777215;
         }
         case "\u041a\u0430\u0441\u0442\u043e\u043c" -> this.customColor.getValue();
         default -> ColorProvider.getColorClient();
      };
   }

   private int[] spawnPos() {
      double fov = (double)((Integer)this.mc.options.getFov().getValue()).intValue();
      double yaw = Math.toRadians((double)this.mc.player.getYaw() + (this.random.nextDouble() - 0.5) * fov * 1.5);
      float radius = this.spawnRadius.getFloatValue();
      float minDistance = Math.max(4.0F, radius * 0.25F);
      double distance = (double)minDistance + this.random.nextDouble() * (double)(radius - minDistance);
      int dx = (int)(-(Math.sin(yaw) * distance));
      int dy = this.random.nextInt(12);
      int dz = (int)(Math.cos(yaw) * distance);
      Vec3d eye = this.mc.player.getEyePos();
      return new int[]{(int)eye.x + dx, (int)eye.y + dy, (int)eye.z + dz};
   }

   private int[] randomDirection() {
      return new int[]{this.random.nextInt(4) * 90, (this.random.nextInt(3) - 1) * 90};
   }

   private int[] nextDirection(int[] previous) {
      int nextB = switch (previous[1]) {
         case 0 -> this.random.nextBoolean() ? 90 : -90;
         case 90 -> this.random.nextBoolean() ? 0 : -90;
         default -> this.random.nextBoolean() ? 0 : 90;
      };
      int nextA = (previous[0] + 90 * (1 + this.random.nextInt(3))) % 360;
      if (nextA == previous[0]) {
         nextA = (nextA + 90) % 360;
      }

      return new int[]{nextA, nextB};
   }

   private int[] step(int[] position, int[] direction, int radius) {
      double yaw = Math.toRadians((double)direction[0]);
      double pitch = Math.toRadians((double)direction[1]);
      double horizontalRadius = (double)radius;
      int deltaY = (int)(Math.sin(pitch) * horizontalRadius);
      if (pitch != 0.0) {
         horizontalRadius = 0.0;
      }

      int deltaX = (int)(-(Math.sin(yaw) * horizontalRadius));
      int deltaZ = (int)(Math.cos(yaw) * horizontalRadius);
      return new int[]{position[0] + deltaX, position[1] + deltaY, position[2] + deltaZ};
   }

   private class Glyph {
      final List<int[]> nodes = new ArrayList<>();
      int[] direction;
      int stepsLeft;
      int ticksLeft;
      int lastSet;
      boolean dying;
      int age;
      int dyingAge;

      Glyph(int[] spawn, int steps) {
         this.nodes.add(spawn);
         this.direction = LineGlyphs.this.randomDirection();
         this.stepsLeft = steps;
      }

      void tick() {
         this.age++;
         if (this.stepsLeft == 0) {
            if (!this.dying) {
               this.dying = true;
               this.dyingAge = this.age;
            }
         } else if (this.ticksLeft > 0) {
            this.ticksLeft = this.ticksLeft - (LineGlyphs.this.slow.getValue() ? 1 : 2);
            if (this.ticksLeft < 0) {
               this.ticksLeft = 0;
            }
         } else {
            this.direction = LineGlyphs.this.nextDirection(this.direction);
            this.lastSet = this.ticksLeft = LineGlyphs.this.random.nextInt(3);
            this.nodes.add(LineGlyphs.this.step(this.nodes.get(this.nodes.size() - 1), this.direction, Math.max(1, this.ticksLeft)));
            this.stepsLeft--;
         }
      }

      float alpha() {
         float in = Math.min(1.0F, (float)this.age / 6.0F);
         if (!this.dying) {
            return in;
         } else {
            float out = Math.max(0.0F, 1.0F - (float)(this.age - this.dyingAge) / 8.0F);
            return Math.min(in, out);
         }
      }

      boolean isDead() {
         return this.dying && this.age - this.dyingAge > 8;
      }

      void getNodeCoord(int index, double[] out) {
         int[] node = this.nodes.get(index);
         double x = (double)node[0];
         double y = (double)node[1];
         double z = (double)node[2];
         if (index == this.nodes.size() - 1 && this.nodes.size() >= 2) {
            int[] previous = this.nodes.get(index - 1);
            float advance = this.lastSet > 0 ? Math.max(0.0F, Math.min(1.0F, 1.0F - (float)this.ticksLeft / (float)this.lastSet)) : 1.0F;
            x = MathHelper.lerp((double)advance, (double)previous[0], x);
            y = MathHelper.lerp((double)advance, (double)previous[1], y);
            z = MathHelper.lerp((double)advance, (double)previous[2], z);
         }

         out[0] = x;
         out[1] = y;
         out[2] = z;
      }
   }
}
