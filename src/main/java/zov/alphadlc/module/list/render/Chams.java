package zov.alphadlc.module.list.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.entity.EntityType;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.module.settings.ColorSetting;
import zov.alphadlc.module.settings.ModeSetting;
import zov.alphadlc.module.settings.SliderSetting;
import zov.alphadlc.util.render.providers.ResourceProvider;

@ModuleInformation(
   moduleName = "Chams",
   moduleDesc = "\u0420\u0438\u0441\u0443\u0435\u0442 \u0438\u0433\u0440\u043e\u043a\u043e\u0432 \u043c\u043e\u0434\u0435\u043b\u044c\u043a\u043e\u0439 \u043a\u0430\u043a \u0432 Totem Angel",
   moduleCategory = ModuleCategory.RENDER
)
public class Chams extends Module {
   private static final ShaderProgramKey FILL_SHADER = new ShaderProgramKey(
      ResourceProvider.getShaderIdentifier("chams_fill"), VertexFormats.field_1575, Defines.field_53930
   );
   private static final ShaderProgramKey WAVE_SHADER = new ShaderProgramKey(
      ResourceProvider.getShaderIdentifier("chams_wave"), VertexFormats.field_1575, Defines.field_53930
   );
   private static Chams instance;
   private final ModeSetting mode = new ModeSetting("\u0420\u0435\u0436\u0438\u043c", "Vanilla", "Vanilla", "Animated Fill", "Wave");
   private final ColorSetting color = new ColorSetting("\u0426\u0432\u0435\u0442", -9021441);
   private final SliderSetting alpha = new SliderSetting("\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c", 0.35F, 0.05F, 1.0, 0.05F);
   private final SliderSetting shaderAlpha = new SliderSetting(
         "\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c \u0448\u0435\u0439\u0434\u0435\u0440\u0430", 0.35F, 0.05F, 1.0, 0.05F
      )
      .setVisible(() -> this.mode.is("Animated Fill") || this.mode.is("Wave"));
   private final SliderSetting brightness = new SliderSetting("\u042f\u0440\u043a\u043e\u0441\u0442\u044c", 0.6F, 0.1F, 1.0, 0.05F);
   private final SliderSetting shaderSpeed = new SliderSetting("\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c", 1.0, 0.1F, 3.0, 0.05F)
      .setVisible(() -> !this.mode.is("Vanilla"));
   private final SliderSetting shaderScale = new SliderSetting("\u041c\u0430\u0441\u0448\u0442\u0430\u0431", 1.35F, 0.5, 3.0, 0.05F)
      .setVisible(() -> this.mode.is("Animated Fill"));
   private final SliderSetting lineWidth = new SliderSetting("\u0422\u043e\u043b\u0449\u0438\u043d\u0430 \u043b\u0438\u043d\u0438\u0439", 1.0, 0.1F, 3.0, 0.05F);
   private final BooleanSetting throughWalls = new BooleanSetting("\u0421\u043a\u0432\u043e\u0437\u044c \u0441\u0442\u0435\u043d\u044b", true);
   private final BooleanSetting glow = new BooleanSetting("\u0421\u0432\u0435\u0447\u0435\u043d\u0438\u0435", true);
   private final BooleanSetting blink = new BooleanSetting("\u041c\u0438\u0433\u0430\u0442\u044c", false);
   private final SliderSetting glowIntensity = new SliderSetting(
      "\u0421\u0438\u043b\u0430 \u0441\u0432\u0435\u0447\u0435\u043d\u0438\u044f", 1.5, 0.5, 4.0, 0.1F
   );
   private final SliderSetting glowLayers = new SliderSetting("\u0421\u043b\u043e\u0438 \u0441\u0432\u0435\u0447\u0435\u043d\u0438\u044f", 3.0, 1.0, 6.0, 1.0);

   public Chams() {
      instance = this;
      LivingEntityFeatureRendererRegistrationCallback.EVENT
         .register((LivingEntityFeatureRendererRegistrationCallback)(entityType, entityRenderer, registrationHelper, context) -> {
            if (entityType == EntityType.field_6097) {
               registrationHelper.register(new ChamsRenderer(entityRenderer));
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
      if (shader != null && shader.method_34582(name) != null) {
         shader.method_34582(name).method_1251(value);
      }
   }
}
