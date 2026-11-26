package miranware.module.list.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.entity.EntityType;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.BooleanSetting;
import miranware.module.settings.ColorSetting;
import miranware.module.settings.ModeSetting;
import miranware.module.settings.SliderSetting;
import miranware.util.render.providers.ResourceProvider;

@ModuleInformation(
   moduleName = "Chams",
   moduleDesc = "Draws players with a model like Totem Angel",
   moduleCategory = ModuleCategory.RENDER
)
public class Chams extends Module {
   private static final ShaderProgramKey FILL_SHADER = new ShaderProgramKey(
      ResourceProvider.getShaderIdentifier("chams_fill"), VertexFormats.POSITION_TEXTURE_COLOR, Defines.EMPTY
   );
   private static final ShaderProgramKey WAVE_SHADER = new ShaderProgramKey(
      ResourceProvider.getShaderIdentifier("chams_wave"), VertexFormats.POSITION_TEXTURE_COLOR, Defines.EMPTY
   );
   private static Chams instance;
   private final ModeSetting mode = new ModeSetting("Mode", "Vanilla", "Vanilla", "Animated Fill", "Wave");
   private final ColorSetting color = new ColorSetting("Color", -9021441);
   private final SliderSetting alpha = new SliderSetting("Opacity", 0.35F, 0.05F, 1.0, 0.05F);
   private final SliderSetting shaderAlpha = new SliderSetting(
         "Shader Opacity", 0.35F, 0.05F, 1.0, 0.05F
      )
      .setVisible(() -> this.mode.is("Animated Fill") || this.mode.is("Wave"));
   private final SliderSetting brightness = new SliderSetting("Brightness", 0.6F, 0.1F, 1.0, 0.05F);
   private final SliderSetting shaderSpeed = new SliderSetting("Speed", 1.0, 0.1F, 3.0, 0.05F)
      .setVisible(() -> !this.mode.is("Vanilla"));
   private final SliderSetting shaderScale = new SliderSetting("Scale", 1.35F, 0.5, 3.0, 0.05F)
      .setVisible(() -> this.mode.is("Animated Fill"));
   private final SliderSetting lineWidth = new SliderSetting("Line Thickness", 1.0, 0.1F, 3.0, 0.05F);
   private final BooleanSetting throughWalls = new BooleanSetting("Through Walls", true);
   private final BooleanSetting glow = new BooleanSetting("Glow", true);
   private final BooleanSetting blink = new BooleanSetting("Blink", false);
   private final SliderSetting glowIntensity = new SliderSetting(
      "Glow Strength", 1.5, 0.5, 4.0, 0.1F
   );
   private final SliderSetting glowLayers = new SliderSetting("Glow Layers", 3.0, 1.0, 6.0, 1.0);

   public Chams() {
      instance = this;
      LivingEntityFeatureRendererRegistrationCallback.EVENT
         .register((LivingEntityFeatureRendererRegistrationCallback)(entityType, entityRenderer, registrationHelper, context) -> {
            if (entityType == EntityType.PLAYER) {
               registrationHelper.register(new ChamsRenderer((net.minecraft.client.render.entity.feature.FeatureRendererContext<net.minecraft.client.render.entity.state.PlayerEntityRenderState, net.minecraft.client.render.entity.model.PlayerEntityModel>) (Object) entityRenderer));
            }
         });
   }

   static Chams getInstance() {
      return instance;
   }

   boolean isMode(String value) {
      return this.mode.is(value);
   }

   int getColor() {
      return this.color.getValue();
   }

   float getAlpha() {
      return this.alpha.getFloatValue();
   }

   float getShaderAlpha() {
      return this.shaderAlpha.getFloatValue();
   }

   float getBrightness() {
      return this.brightness.getFloatValue();
   }

   float getShaderSpeed() {
      return this.shaderSpeed.getFloatValue();
   }

   float getShaderScale() {
      return this.shaderScale.getFloatValue();
   }

   float getLineWidth() {
      return this.lineWidth.getFloatValue();
   }

   boolean isThroughWalls() {
      return this.throughWalls.getValue();
   }

   boolean hasGlow() {
      return this.glow.getValue();
   }

   boolean isBlinking() {
      return this.blink.getValue();
   }

   float getGlowIntensity() {
      return this.glowIntensity.getFloatValue();
   }

   int getGlowLayers() {
      return Math.max(1, this.glowLayers.getIntValue());
   }

   static ShaderProgram bindAnimatedFill(float time, float speed, float scale, float glowStrength) {
      ShaderProgram shader = RenderSystem.setShader(FILL_SHADER);
      setUniform(shader, "time", time);
      setUniform(shader, "speedX", 0.22F * speed);
      setUniform(shader, "speedY", 0.15F * speed);
      setUniform(shader, "scale", scale);
      setUniform(shader, "density", 1.15F);
      setUniform(shader, "glowStrength", glowStrength);
      return shader;
   }

   static ShaderProgram bindWave(float time) {
      ShaderProgram shader = RenderSystem.setShader(WAVE_SHADER);
      setUniform(shader, "Time", time);
      return shader;
   }

   private static void setUniform(ShaderProgram shader, String name, float value) {
      if (shader != null && shader.getUniform(name) != null) {
         shader.getUniform(name).set(value);
      }
   }
}
