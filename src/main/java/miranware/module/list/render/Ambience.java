package miranware.module.list.render;

import com.google.common.eventbus.Subscribe;
import miranware.event.list.EventTick;
import miranware.event.list.EventWorldRender;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.BooleanSetting;
import miranware.module.settings.ColorSetting;
import miranware.module.settings.ModeSetting;
import miranware.module.settings.SliderSetting;
import miranware.module.settings.impl.ThemeManager;
import miranware.util.render.ambience.SkyShaderRenderer;

@ModuleInformation(
   moduleName = "Ambience",
   moduleDesc = "Custom time and fog",
   moduleCategory = ModuleCategory.RENDER
)
public class Ambience extends Module {
   public final ModeSetting timePreset = new ModeSetting(
      "Time",
      "Day",
      "Day",
      "Sunrise",
      "Sunset",
      "Dusk",
      "Night",
      "Midnight"
   );
   public final BooleanSetting customFog = new BooleanSetting("Fog", false);
   public final SliderSetting fogDistance = new SliderSetting(
         "Fog Range", 100.0, 10.0, 500.0, 10.0
      )
      .setVisible(() -> this.customFog.getValue());
   public final SliderSetting fogDensity = new SliderSetting(
         "Fog Density", 0.75, 0.0, 2.0, 0.05
      )
      .setVisible(() -> this.customFog.getValue());
   public final BooleanSetting themeFogColor = new BooleanSetting(
         "Fog Theme Color", false
      )
      .setVisible(() -> this.customFog.getValue());
   public final ColorSetting fogColor = new ColorSetting("Fog Color", -6250336)
      .setVisible(() -> this.customFog.getValue() && !this.themeFogColor.getValue());
   public final BooleanSetting skyShader = new BooleanSetting("Sky Shader", false);
   public final ModeSetting skyShaderMode = new ModeSetting(
         "Mode", "Aurora", "Aurora", "Energy", "Nebula", "Cosmic Veil", "Deep Space", "Matrix", "Void", "Plasma"
      )
      .setVisible(() -> this.skyShader.getValue());
   public final SliderSetting plasmaScale = new SliderSetting(
         "Plasma: Scale", 1.0, 0.2, 3.0, 0.05
      )
      .setVisible(() -> this.skyShader.getValue() && this.skyShaderMode.getValue().equals("Plasma"));
   public final SliderSetting plasmaSpeed = new SliderSetting(
         "Plasma: Speed", 1.0, 0.0, 3.0, 0.05
      )
      .setVisible(() -> this.skyShader.getValue() && this.skyShaderMode.getValue().equals("Plasma"));
   public final BooleanSetting showStars = new BooleanSetting("Stars", true).setVisible(() -> this.skyShader.getValue());
   public final BooleanSetting syncWithTheme = new BooleanSetting(
         "Shader Theme Color", true
      )
      .setVisible(() -> this.skyShader.getValue());
   public final ColorSetting shaderColor = new ColorSetting("Shader Color", -9673774)
      .setVisible(() -> this.skyShader.getValue() && !this.syncWithTheme.getValue());
   public final BooleanSetting removeFog = new BooleanSetting("Remove Fog", true)
      .setVisible(() -> this.skyShader.getValue());
   public final BooleanSetting worldSaturation = new BooleanSetting(
      "World Saturation", false
   );
   public final SliderSetting saturationValue = new SliderSetting(
         "Saturation", 1.0, 0.0, 3.0, 0.05
      )
      .setVisible(() -> this.worldSaturation.getValue());
   private long shaderStartTime = 0L;

   @Override
   public void onEnable() {
      super.onEnable();
      this.shaderStartTime = System.currentTimeMillis();
      if (this.skyShader.getValue()) {
         this.updateSkyShaderConfig();
      }
   }

   @Subscribe
   public void onTick(EventTick event) {
      if (this.skyShader.getValue()) {
         this.updateSkyShaderConfig();
      }
   }

   @Subscribe
   public void onWorldRender(EventWorldRender event) {
      if (this.skyShader.getValue()) {
         this.updateSkyShaderConfig();
      }
   }

   private void updateSkyShaderConfig() {
      SkyShaderRenderer.updateConfig(this);
   }

   public long getShaderStartTime() {
      return this.shaderStartTime;
   }

   public long getCustomTime() {
      String var1 = this.timePreset.getValue();

      return switch (var1) {
         case "Sunrise" -> 23000L;
         case "Day" -> 6000L;
         case "Sunset" -> 12500L;
         case "Dusk" -> 13500L;
         case "Night" -> 16000L;
         case "Midnight" -> 18000L;
         default -> 6000L;
      };
   }

   public boolean isFogEnabled() {
      return this.customFog.getValue();
   }

   public float getFogDistance() {
      return (float)this.fogDistance.getValue();
   }

   public float getFogDensity() {
      return this.fogDensity.getFloatValue();
   }

   public boolean isThemeFogColorEnabled() {
      return this.themeFogColor.getValue();
   }

   public int getFogColor() {
      return this.themeFogColor.getValue() ? ThemeManager.getInstance().getCurrentTheme().getColorFirst() : this.fogColor.getValue();
   }

   public int getSkyShaderColor() {
      return this.syncWithTheme.getValue() ? ThemeManager.getInstance().getCurrentTheme().getColorFirst() : this.shaderColor.getValue();
   }

   public boolean isSkyShaderEnabled() {
      return this.skyShader.getValue();
   }

   public String getSkyShaderMode() {
      return this.skyShaderMode.getValue();
   }

   public float getStarDensity() {
      return 0.7F;
   }

   public float getNebulaStrength() {
      return 0.8F;
   }

   public float getNebulaSpeed() {
      return 0.5F;
   }

   public float getPlasmaScale() {
      return this.plasmaScale.getFloatValue();
   }

   public float getPlasmaSpeed() {
      return this.plasmaSpeed.getFloatValue();
   }

   public boolean isRemoveFogEnabled() {
      return this.removeFog.getValue();
   }
}
