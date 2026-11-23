package miranware.util.render.ambience;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import miranware.module.list.render.Ambience;
import miranware.util.render.providers.ResourceProvider;

public final class SkyShaderRenderer {
   private static final MinecraftClient MC = MinecraftClient.getInstance();
   private static final ShaderProgramKey SKY = key("sky");
   private static final ShaderProgramKey AURORA = key("sky_aurora");
   private static final ShaderProgramKey NEBULA = key("sky_nebula");
   private static final ShaderProgramKey MATRIX = key("sky_matrix");
   private static final ShaderProgramKey PLASMA = key("sky_plasma");
   private static volatile SkyShaderRenderer.Config config;

   private SkyShaderRenderer() {
   }

   private static ShaderProgramKey key(String name) {
      return new ShaderProgramKey(ResourceProvider.getShaderIdentifier(name), VertexFormats.POSITION_COLOR, Defines.EMPTY);
   }

   public static void updateConfig(Ambience module) {
      if (module != null) {
         int primary = module.getSkyShaderColor();
         float r = (float)(primary >> 16 & 0xFF) / 255.0F;
         float g = (float)(primary >> 8 & 0xFF) / 255.0F;
         float b = (float)(primary & 0xFF) / 255.0F;
         int fog = module.getFogColor();
         float fogR = (float)(fog >> 16 & 0xFF) / 255.0F;
         float fogG = (float)(fog >> 8 & 0xFF) / 255.0F;
         float fogB = (float)(fog & 0xFF) / 255.0F;
         config = new SkyShaderRenderer.Config(
            module.getSkyShaderMode(),
            primary,
            r,
            g,
            b,
            fog,
            fogR,
            fogG,
            fogB,
            module.getFogDensity(),
            module.showStars.getValue(),
            module.getStarDensity(),
            module.getNebulaStrength(),
            module.getPlasmaScale(),
            module.getPlasmaSpeed()
         );
      }
   }

   public static void render(Ambience module) {
      if (module != null && module.isEnabled() && module.isSkyShaderEnabled()) {
         SkyShaderRenderer.Config c = config;
         if (c == null
            || !c.mode.equals(module.getSkyShaderMode())
            || c.color != module.getSkyShaderColor()
            || c.fogColor != module.getFogColor()
            || c.fogDensity != module.getFogDensity()) {
            updateConfig(module);
            c = config;
         }

         if (c != null) {
            String shaderName = c.mode;

            ShaderProgramKey key = switch (shaderName) {
               case "Aurora" -> AURORA;
               case "Nebula" -> NEBULA;
               case "Matrix" -> MATRIX;
               case "Plasma" -> PLASMA;
               default -> SKY;
            };
            ShaderProgram shader = RenderSystem.setShader(key);
            if (shader != null) {
               float time = (float)(System.currentTimeMillis() - module.getShaderStartTime()) / 1000.0F;
               set(shader, "AnimTime", time);
               set(shader, "iTime", time);
               set(shader, "SkyMode", genericMode(c.mode));
               set(shader, "StarDensity", c.showStars ? c.starDensity : 0.0F);
               set(shader, "ShowStars", c.showStars ? 1.0F : 0.0F);
               set(shader, "uShowStars", c.showStars ? 1 : 0);
               set(shader, "NebulaStrength", c.nebulaStrength);
               set(shader, "NebIntensity", 0.8F);
               set(shader, "PlasmaScale", c.plasmaScale);
               set(shader, "PlasmaSpeed", c.plasmaSpeed);
               set(shader, "uScale", c.plasmaScale);
               set(shader, "uSpeed", c.plasmaSpeed);
               set(shader, "uIntensity", 1.0F);
               set(shader, "SkyFogColor", c.fogR, c.fogG, c.fogB);
               set(shader, "FogDensity", c.fogDensity);
               applyColor(shader, c);
               RenderSystem.disableDepthTest();
               RenderSystem.depthMask(false);
               RenderSystem.disableCull();
               RenderSystem.disableBlend();
               drawSkyCube();
               RenderSystem.depthMask(true);
               RenderSystem.enableDepthTest();
               RenderSystem.enableCull();
            }
         }
      }
   }

   private static void applyColor(ShaderProgram shader, SkyShaderRenderer.Config c) {
      set(shader, "uColor", c.r, c.g, c.b);
      set(shader, "SkyZenith", c.r * 0.1F, c.g * 0.1F, c.b * 0.18F + 0.02F);
      set(shader, "SkyHorizon", c.r * 0.45F + 0.04F, c.g * 0.45F + 0.04F, c.b * 0.45F + 0.06F);
      set(shader, "NebColor1", c.r, c.g, c.b);
      set(shader, "NebColor2", c.b, Math.min(1.0F, c.r + 0.25F), Math.min(1.0F, c.g + 0.2F));
      set(shader, "StarColor", 0.9F, 0.94F, 1.0F);
   }

   private static float genericMode(String mode) {
      return switch (mode) {
         case "Cosmic Veil" -> 2.0F;
         case "Deep Space" -> 3.0F;
         case "Void" -> 4.0F;
         default -> 0.0F;
      };
   }

   private static void drawSkyCube() {
      float s = 100.0F;
      BufferBuilder b = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
      face(b, -s, -s, s, s, -s, s, s, s, s, -s, s, s);
      face(b, s, -s, -s, -s, -s, -s, -s, s, -s, s, s, -s);
      face(b, -s, -s, -s, -s, -s, s, -s, s, s, -s, s, -s);
      face(b, s, -s, s, s, -s, -s, s, s, -s, s, s, s);
      face(b, -s, s, s, s, s, s, s, s, -s, -s, s, -s);
      face(b, -s, -s, -s, s, -s, -s, s, -s, s, -s, -s, s);
      BufferRenderer.drawWithGlobalProgram(b.end());
   }

   private static void face(
      BufferBuilder b, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4
   ) {
      b.vertex(x1, y1, z1).color(-1);
      b.vertex(x2, y2, z2).color(-1);
      b.vertex(x3, y3, z3).color(-1);
      b.vertex(x4, y4, z4).color(-1);
   }

   private static void set(ShaderProgram shader, String name, float value) {
      if (shader.getUniform(name) != null) {
         shader.getUniform(name).set(value);
      }
   }

   private static void set(ShaderProgram shader, String name, int value) {
      if (shader.getUniform(name) != null) {
         shader.getUniform(name).set(value);
      }
   }

   private static void set(ShaderProgram shader, String name, float x, float y, float z) {
      if (shader.getUniform(name) != null) {
         shader.getUniform(name).set(x, y, z);
      }
   }

   private static record Config(
      String mode,
      int color,
      float r,
      float g,
      float b,
      int fogColor,
      float fogR,
      float fogG,
      float fogB,
      float fogDensity,
      boolean showStars,
      float starDensity,
      float nebulaStrength,
      float plasmaScale,
      float plasmaSpeed
   ) {
   }
}
