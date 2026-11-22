package zov.alphadlc.module.list.render;

import com.google.common.eventbus.Subscribe;
import zov.alphadlc.event.list.EventTick;
import zov.alphadlc.event.list.EventWorldRender;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.module.settings.ColorSetting;
import zov.alphadlc.module.settings.ModeSetting;
import zov.alphadlc.module.settings.SliderSetting;
import zov.alphadlc.module.settings.impl.ThemeManager;
import zov.alphadlc.util.render.ambience.SkyShaderRenderer;

@ModuleInformation(
   moduleName = "Ambience",
   moduleDesc = "\u041a\u0430\u0441\u0442\u043e\u043c\u043d\u043e\u0435 \u0432\u0440\u0435\u043c\u044f \u0438 \u0442\u0443\u043c\u0430\u043d",
   moduleCategory = ModuleCategory.RENDER
)
public class Ambience extends Module {
   public final ModeSetting timePreset = new ModeSetting(
      "\u0412\u0440\u0435\u043c\u044f",
      "\u0414\u0435\u043d\u044c",
      "\u0414\u0435\u043d\u044c",
      "\u0420\u0430\u0441\u0441\u0432\u0435\u0442",
      "\u0417\u0430\u043a\u0430\u0442",
      "\u0421\u0443\u043c\u0435\u0440\u043a\u0438",
      "\u041d\u043e\u0447\u044c",
      "\u041f\u043e\u043b\u043d\u043e\u0447\u044c"
   );
   public final BooleanSetting customFog = new BooleanSetting("\u0422\u0443\u043c\u0430\u043d", false);
   public final SliderSetting fogDistance = new SliderSetting(
         "\u0414\u0430\u043b\u044c\u043d\u043e\u0441\u0442\u044c \u0442\u0443\u043c\u0430\u043d\u0430", 100.0, 10.0, 500.0, 10.0
      )
      .setVisible(() -> this.customFog.getValue());
   public final SliderSetting fogDensity = new SliderSetting(
         "\u041f\u043b\u043e\u0442\u043d\u043e\u0441\u0442\u044c \u0442\u0443\u043c\u0430\u043d\u0430", 0.75, 0.0, 2.0, 0.05
      )
      .setVisible(() -> this.customFog.getValue());
   public final BooleanSetting themeFogColor = new BooleanSetting(
         "\u0426\u0432\u0435\u0442 \u0442\u0443\u043c\u0430\u043d\u0430 \u043e\u0442 \u0442\u0435\u043c\u044b", false
      )
      .setVisible(() -> this.customFog.getValue());
   public final ColorSetting fogColor = new ColorSetting("\u0426\u0432\u0435\u0442 \u0442\u0443\u043c\u0430\u043d\u0430", -6250336)
      .setVisible(() -> this.customFog.getValue() && !this.themeFogColor.getValue());
   public final BooleanSetting skyShader = new BooleanSetting("\u0428\u0435\u0439\u0434\u0435\u0440 \u043d\u0435\u0431\u0430", false);
   public final ModeSetting skyShaderMode = new ModeSetting(
         "\u0420\u0435\u0436\u0438\u043c", "Aurora", "Aurora", "Energy", "Nebula", "Cosmic Veil", "Deep Space", "Matrix", "Void", "Plasma"
      )
      .setVisible(() -> this.skyShader.getValue());
   public final SliderSetting plasmaScale = new SliderSetting(
         "\u041f\u043b\u0430\u0437\u043c\u0430: \u043c\u0430\u0441\u0448\u0442\u0430\u0431", 1.0, 0.2, 3.0, 0.05
      )
      .setVisible(() -> this.skyShader.getValue() && this.skyShaderMode.getValue().equals("Plasma"));
   public final SliderSetting plasmaSpeed = new SliderSetting(
         "\u041f\u043b\u0430\u0437\u043c\u0430: \u0441\u043a\u043e\u0440\u043e\u0441\u0442\u044c", 1.0, 0.0, 3.0, 0.05
      )
      .setVisible(() -> this.skyShader.getValue() && this.skyShaderMode.getValue().equals("Plasma"));
   public final BooleanSetting showStars = new BooleanSetting("\u0417\u0432\u0451\u0437\u0434\u044b", true).setVisible(() -> this.skyShader.getValue());
   public final BooleanSetting syncWithTheme = new BooleanSetting(
         "\u0426\u0432\u0435\u0442 \u0448\u0435\u0439\u0434\u0435\u0440\u0430 \u043e\u0442 \u0442\u0435\u043c\u044b", true
      )
      .setVisible(() -> this.skyShader.getValue());
   public final ColorSetting shaderColor = new ColorSetting("\u0426\u0432\u0435\u0442 \u0448\u0435\u0439\u0434\u0435\u0440\u0430", -9673774)
      .setVisible(() -> this.skyShader.getValue() && !this.syncWithTheme.getValue());
   public final BooleanSetting removeFog = new BooleanSetting("\u0423\u0434\u0430\u043b\u044f\u0442\u044c \u0442\u0443\u043c\u0430\u043d", true)
      .setVisible(() -> this.skyShader.getValue());
   public final BooleanSetting worldSaturation = new BooleanSetting(
      "\u041d\u0430\u0441\u044b\u0449\u0435\u043d\u043d\u043e\u0441\u0442\u044c \u043c\u0438\u0440\u0430", false
   );
   public final SliderSetting saturationValue = new SliderSetting(
         "\u041d\u0430\u0441\u044b\u0449\u0435\u043d\u043d\u043e\u0441\u0442\u044c", 1.0, 0.0, 3.0, 0.05
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
         case "\u0420\u0430\u0441\u0441\u0432\u0435\u0442" -> 23000L;
         case "\u0414\u0435\u043d\u044c" -> 6000L;
         case "\u0417\u0430\u043a\u0430\u0442" -> 12500L;
         case "\u0421\u0443\u043c\u0435\u0440\u043a\u0438" -> 13500L;
         case "\u041d\u043e\u0447\u044c" -> 16000L;
         case "\u041f\u043e\u043b\u043d\u043e\u0447\u044c" -> 18000L;
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
