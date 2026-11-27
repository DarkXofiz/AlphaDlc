package miranware.module.list.render;

import com.google.common.eventbus.Subscribe;
import java.awt.Color;
import miranware.event.list.EventHUD;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.BooleanSetting;
import miranware.module.settings.ColorSetting;
import miranware.module.settings.ModeSetting;
import miranware.module.settings.SliderSetting;
import miranware.util.render.hands.GlassHandsRenderer;

@ModuleInformation(
   moduleName = "Hands",
   moduleDesc = "Pretty shaders for hands",
   moduleCategory = ModuleCategory.RENDER
)
public class GlassHands extends Module {
   public final ModeSetting mode = new ModeSetting(
      "Mode",
      "Blur",
      "Blur",
      "Glass",
      "Fill",
      "Wave"
   );
   public final SliderSetting waveSpeed = new SliderSetting("Wave Speed", 1.2, 0.1, 5.0, 0.1)
      .setVisible(() -> this.mode.is("Pretty"));
   public final SliderSetting waveScale = new SliderSetting("Wave Frequency", 1.0, 1.0, 3.0, 0.1)
      .setVisible(() -> this.mode.is("Pretty"));
   public final SliderSetting outline = new SliderSetting(
         "Outline Width", 1.2, 0.01, 5.0, 0.01
      )
      .setVisible(
         () -> !this.mode.is("Blur")
               && !this.mode.is("Outline")
               && !this.mode.is("Glass")
               && !this.mode.is("Fill")
               && !this.mode.is("Wave")
      );
   public final SliderSetting glow = new SliderSetting("Glow Strength", 1.0, 0.0, 5.0, 0.01)
      .setVisible(
         () -> !this.mode.is("Blur")
               && !this.mode.is("Outline")
               && !this.mode.is("Glass")
               && !this.mode.is("Fill")
               && !this.mode.is("Wave")
      );
   public final SliderSetting fill = new SliderSetting("Fill", 0.6, 0.0, 1.0, 0.01)
      .setVisible(
         () -> !this.mode.is("Blur")
               && !this.mode.is("Outline")
               && !this.mode.is("Glass")
               && !this.mode.is("Fill")
               && !this.mode.is("Wave")
      );
   public final SliderSetting alpha = new SliderSetting("Opacity", 1.0, 0.0, 1.0, 0.05)
      .setVisible(
         () -> !this.mode.is("Blur")
               && !this.mode.is("Outline")
               && !this.mode.is("Glass")
               && !this.mode.is("Fill")
               && !this.mode.is("Wave")
      );
   public final BooleanSetting glowItemColor = new BooleanSetting("Item Color", false)
      .setVisible(() -> this.mode.is("Glow"));
   public final SliderSetting blurStrength = new SliderSetting("Blur Strength", 4.0, 1.0, 8.0, 1.0)
      .setVisible(() -> this.mode.is("Blur"));
   public final SliderSetting blurTint = new SliderSetting("Hue", 0.3, 0.0, 1.0, 0.05)
      .setVisible(() -> this.mode.is("Blur"));
   public final BooleanSetting blurRainbow = new BooleanSetting("Rainbow Color", false)
      .setVisible(() -> this.mode.is("Blur"));
   public final SliderSetting blurRainbowSpeed = new SliderSetting(
         "Rainbow Speed", 1.0, 0.1, 5.0, 0.1
      )
      .setVisible(() -> this.mode.is("Blur") && this.blurRainbow.getValue());
   public final SliderSetting trailIntensity = new SliderSetting(
         "Trail Brightness", 0.8, 0.3, 3.0, 0.1
      )
      .setVisible(() -> this.mode.is("Trail"));
   public final SliderSetting trailGlowSize = new SliderSetting(
         "Glow Size", 2.0, 0.5, 5.0, 0.1
      )
      .setVisible(() -> this.mode.is("Trail"));
   public final SliderSetting trailBlur = new SliderSetting("Blur Amount", 6.0, 1.0, 10.0, 1.0)
      .setVisible(() -> this.mode.is("Trail"));
   public final BooleanSetting rainbow = new BooleanSetting("Rainbow Color", false)
      .setVisible(() -> this.mode.is("Trail"));
   public final SliderSetting rainbowSpeed = new SliderSetting(
         "Rainbow Speed", 1.0, 0.1, 5.0, 0.1
      )
      .setVisible(() -> this.mode.is("Trail") && this.rainbow.getValue());
   public final SliderSetting trailIntensityM = new SliderSetting(
         "Intensity", 0.72, 0.1, 1.5, 0.05
      )
      .setVisible(() -> this.mode.is("Trail"));
   public final SliderSetting trailSpeed = new SliderSetting("Speed", 1.05, 0.2, 3.0, 0.05)
      .setVisible(() -> this.mode.is("Trail"));
   public final SliderSetting trailLength = new SliderSetting("Trail Length", 0.32, 0.1, 1.0, 0.05)
      .setVisible(() -> this.mode.is("Trail"));
   public final SliderSetting trailSoftness = new SliderSetting("Softness", 1.05, 0.4, 2.0, 0.05)
      .setVisible(() -> this.mode.is("Trail"));
   public final SliderSetting trailBlurRadius = new SliderSetting("Blur Amount", 1.05, 0.2, 2.5, 0.05)
      .setVisible(() -> this.mode.is("Trail"));
   public final SliderSetting trailSmoke = new SliderSetting("Smoke", 0.22, 0.0, 0.8, 0.05).setVisible(() -> this.mode.is("Trail"));
   public final SliderSetting trailAttack = new SliderSetting("Hit", 0.58, 0.0, 1.0, 0.05).setVisible(() -> this.mode.is("Trail"));
   public final SliderSetting trailCamera = new SliderSetting(
         "Camera Follow", 0.45, 0.0, 1.5, 0.05
      )
      .setVisible(() -> this.mode.is("Trail"));
   public final BooleanSetting trailItemColor = new BooleanSetting("Item Color", false)
      .setVisible(() -> this.mode.is("Trail"));
   public final SliderSetting glassMixFactor = new SliderSetting("Blending", 0.0, 0.0, 1.0, 0.01)
      .setVisible(() -> this.mode.is("Glass"));
   public final BooleanSetting glassGlowEnabled = new BooleanSetting("Glow Effect", true)
      .setVisible(() -> this.mode.is("Glass"));
   public final SliderSetting glassGlowRadius = new SliderSetting("Glow Radius", 3.0, 1.0, 6.0, 1.0)
      .setVisible(() -> this.mode.is("Glass") && this.glassGlowEnabled.getValue());
   public final BooleanSetting glassOuterGlow = new BooleanSetting("Outer Glow", true)
      .setVisible(() -> this.mode.is("Glass") && this.glassGlowEnabled.getValue());
   public final SliderSetting glassGlowExposure = new SliderSetting(
         "Glow Brightness", 2.0, 0.5, 5.0, 0.1
      )
      .setVisible(() -> this.mode.is("Glass") && this.glassGlowEnabled.getValue() && this.glassOuterGlow.getValue());
   public final ColorSetting glassGlowColor1 = new ColorSetting(
         "Glow Color 1", new Color(138, 152, 255, 255).getRGB()
      )
      .setVisible(() -> this.mode.is("Glass") && this.glassGlowEnabled.getValue());
   public final ColorSetting glassGlowColor2 = new ColorSetting(
         "Glow Color 2", new Color(255, 107, 172, 255).getRGB()
      )
      .setVisible(() -> this.mode.is("Glass") && this.glassGlowEnabled.getValue());
   public final BooleanSetting glassRainbow = new BooleanSetting("Rainbow Color", false)
      .setVisible(() -> this.mode.is("Glass") && this.glassGlowEnabled.getValue());
   public final SliderSetting glassRainbowSpeed = new SliderSetting(
         "Rainbow Speed", 1.0, 0.1, 5.0, 0.1
      )
      .setVisible(() -> this.mode.is("Glass") && this.glassGlowEnabled.getValue() && this.glassRainbow.getValue());
   public final SliderSetting plasmaSpeed = new SliderSetting("Speed", 1.0, 0.1, 3.0, 0.1)
      .setVisible(() -> this.mode.is("Plasma"));
   public final SliderSetting plasmaScale = new SliderSetting("Scale", 1.0, 0.5, 3.0, 0.1)
      .setVisible(() -> this.mode.is("Plasma"));
   public final BooleanSetting plasmaStars = new BooleanSetting("Stars", true).setVisible(() -> this.mode.is("Plasma"));
   public final BooleanSetting fillGlassMode = new BooleanSetting("Glass", false)
      .setVisible(() -> this.mode.is("Fill"));
   public final SliderSetting fillGlassMix = new SliderSetting("Blending", 0.0, 0.0, 1.0, 0.01)
      .setVisible(() -> this.mode.is("Fill") && this.fillGlassMode.getValue());
   public final ColorSetting fillColor = new ColorSetting(
         "Fill Color", new Color(255, 68, 68, 255).getRGB()
      )
      .setVisible(() -> this.mode.is("Fill") && !this.fillGlassMode.getValue());
   public final BooleanSetting fillRainbow = new BooleanSetting("Rainbow Color", false)
      .setVisible(() -> this.mode.is("Fill") && !this.fillGlassMode.getValue());
   public final SliderSetting fillRainbowSpeed = new SliderSetting(
         "Rainbow Speed", 1.0, 0.1, 5.0, 0.1
      )
      .setVisible(() -> this.mode.is("Fill") && !this.fillGlassMode.getValue() && this.fillRainbow.getValue());
   public final SliderSetting fillAlpha = new SliderSetting(
         "Fill Opacity", 0.8, 0.0, 1.0, 0.05
      )
      .setVisible(() -> this.mode.is("Fill") && !this.fillGlassMode.getValue());
   public final BooleanSetting fillKeepShading = new BooleanSetting("Keep Shadows", true)
      .setVisible(() -> this.mode.is("Fill") && !this.fillGlassMode.getValue());
   public final SliderSetting fillShadingStrength = new SliderSetting("Shadow Strength", 0.3, 0.0, 1.0, 0.05)
      .setVisible(() -> this.mode.is("Fill") && !this.fillGlassMode.getValue() && this.fillKeepShading.getValue());
   public final BooleanSetting fillOutlineEnabled = new BooleanSetting("Outline", false)
      .setVisible(() -> this.mode.is("Fill"));
   public final SliderSetting fillOutlineWidth = new SliderSetting(
         "Outline Thickness", 1.0, 0.5, 3.0, 0.5
      )
      .setVisible(() -> this.mode.is("Fill") && this.fillOutlineEnabled.getValue());
   public final ColorSetting fillOutlineColor = new ColorSetting(
         "Outline Color", new Color(138, 152, 255, 255).getRGB()
      )
      .setVisible(() -> this.mode.is("Fill") && this.fillOutlineEnabled.getValue());
   public final BooleanSetting fillGlowEnabled = new BooleanSetting("Glow Effect", true)
      .setVisible(() -> this.mode.is("Fill"));
   public final SliderSetting fillGlowRadius = new SliderSetting("Blur Amount", 4.0, 1.0, 6.0, 1.0)
      .setVisible(() -> this.mode.is("Fill") && this.fillGlowEnabled.getValue());
   public final BooleanSetting fillOuterGlow = new BooleanSetting("Outer Glow", true)
      .setVisible(() -> this.mode.is("Fill") && this.fillGlowEnabled.getValue());
   public final SliderSetting fillGlowExposure = new SliderSetting("Brightness", 2.0, 0.5, 5.0, 0.1)
      .setVisible(() -> this.mode.is("Fill") && this.fillGlowEnabled.getValue() && this.fillOuterGlow.getValue());
   public final BooleanSetting fillAutoColor = new BooleanSetting("Auto Color", false)
      .setVisible(() -> this.mode.is("Fill") && (this.fillGlowEnabled.getValue() || this.fillOutlineEnabled.getValue()));
   public final SliderSetting fillSaturation = new SliderSetting("Saturation", 1.4, 0.5, 3.0, 0.1)
      .setVisible(
         () -> this.mode.is("Fill")
               && (this.fillGlowEnabled.getValue() || this.fillOutlineEnabled.getValue())
               && this.fillAutoColor.getValue()
      );
   public final ColorSetting fillGlowColor1 = new ColorSetting(
         "Glow Color 1", new Color(138, 152, 255, 255).getRGB()
      )
      .setVisible(() -> this.mode.is("Fill") && this.fillGlowEnabled.getValue() && !this.fillAutoColor.getValue());
   public final ColorSetting fillGlowColor2 = new ColorSetting(
         "Glow Color 2", new Color(255, 107, 172, 255).getRGB()
      )
      .setVisible(() -> this.mode.is("Fill") && this.fillGlowEnabled.getValue() && !this.fillAutoColor.getValue());
   public final BooleanSetting fillTrailEnabled = new BooleanSetting("Trail", true)
      .setVisible(() -> this.mode.is("Fill") && this.fillGlowEnabled.getValue() && this.fillOuterGlow.getValue());
   public final SliderSetting fillTrailFade = new SliderSetting(
         "Fade Speed", 0.009, 0.002, 0.2, 0.002
      )
      .setVisible(
         () -> this.mode.is("Fill")
               && this.fillGlowEnabled.getValue()
               && this.fillOuterGlow.getValue()
               && this.fillTrailEnabled.getValue()
      );
   public final SliderSetting fillTrailRise = new SliderSetting("Rise", 0.14, 0.0, 1.5, 0.05)
      .setVisible(
         () -> this.mode.is("Fill")
               && this.fillGlowEnabled.getValue()
               && this.fillOuterGlow.getValue()
               && this.fillTrailEnabled.getValue()
      );
   public final SliderSetting fillTrailSway = new SliderSetting("Sway", 0.025, 0.0, 0.2, 0.005)
      .setVisible(
         () -> this.mode.is("Fill")
               && this.fillGlowEnabled.getValue()
               && this.fillOuterGlow.getValue()
               && this.fillTrailEnabled.getValue()
      );
   public final SliderSetting fillTrailTurb = new SliderSetting(
         "Turbulence", 0.0, 0.0, 0.6, 0.01
      )
      .setVisible(
         () -> this.mode.is("Fill")
               && this.fillGlowEnabled.getValue()
               && this.fillOuterGlow.getValue()
               && this.fillTrailEnabled.getValue()
      );
   public final SliderSetting fillTrailFlicker = new SliderSetting("Flicker", 0.0, 0.0, 0.2, 0.01)
      .setVisible(
         () -> this.mode.is("Fill")
               && this.fillGlowEnabled.getValue()
               && this.fillOuterGlow.getValue()
               && this.fillTrailEnabled.getValue()
      );
   public final BooleanSetting fillTrailBurst = new BooleanSetting("Hit Blowback", true)
      .setVisible(
         () -> this.mode.is("Fill")
               && this.fillGlowEnabled.getValue()
               && this.fillOuterGlow.getValue()
               && this.fillTrailEnabled.getValue()
      );
   public final SliderSetting fillTrailBurstPower = new SliderSetting("Blowback Strength", 2.5, 1.0, 10.0, 0.5)
      .setVisible(
         () -> this.mode.is("Fill")
               && this.fillGlowEnabled.getValue()
               && this.fillOuterGlow.getValue()
               && this.fillTrailEnabled.getValue()
               && this.fillTrailBurst.getValue()
      );
   public final BooleanSetting fillTrailModel = new BooleanSetting("Model Trail", true)
      .setVisible(
         () -> this.mode.is("Fill")
               && this.fillGlowEnabled.getValue()
               && this.fillOuterGlow.getValue()
               && this.fillTrailEnabled.getValue()
      );
   public final SliderSetting fillTrailModelAlpha = new SliderSetting(
         "Model Opacity", 0.4, 0.1, 1.0, 0.05
      )
      .setVisible(
         () -> this.mode.is("Fill")
               && this.fillGlowEnabled.getValue()
               && this.fillOuterGlow.getValue()
               && this.fillTrailEnabled.getValue()
               && this.fillTrailModel.getValue()
      );
   public final SliderSetting waveSpeedX = new SliderSetting("X Speed", 0.22, 0.0, 1.5, 0.01)
      .setVisible(() -> this.mode.is("Wave"));
   public final SliderSetting waveSpeedY = new SliderSetting("Y Speed", 0.15, 0.0, 1.5, 0.01)
      .setVisible(() -> this.mode.is("Wave"));
   public final SliderSetting waveScaleM = new SliderSetting("Scale", 1.35, 0.2, 4.0, 0.05)
      .setVisible(() -> this.mode.is("Wave"));
   public final SliderSetting waveDensity = new SliderSetting("Density", 1.15, 0.5, 3.0, 0.05)
      .setVisible(() -> this.mode.is("Wave"));
   public final SliderSetting waveGlow = new SliderSetting("Wave Strength", 1.0, 0.2, 3.0, 0.05)
      .setVisible(() -> this.mode.is("Wave"));
   public final SliderSetting waveFillAlpha = new SliderSetting(
         "Opacity", 0.85, 0.1, 1.0, 0.05
      )
      .setVisible(() -> this.mode.is("Wave"));
   public final SliderSetting waveModelVisibility = new SliderSetting(
         "Model Visibility", 0.25, 0.0, 1.0, 0.05
      )
      .setVisible(() -> this.mode.is("Wave"));
   public final BooleanSetting waveGlowEnabled = new BooleanSetting("Glow Effect", true)
      .setVisible(() -> this.mode.is("Wave"));
   public final SliderSetting waveGlowRadius = new SliderSetting("Blur Amount", 4.0, 1.0, 6.0, 1.0)
      .setVisible(() -> this.mode.is("Wave") && this.waveGlowEnabled.getValue());
   public final BooleanSetting waveOuterGlow = new BooleanSetting("Outer Glow", true)
      .setVisible(() -> this.mode.is("Wave") && this.waveGlowEnabled.getValue());
   public final SliderSetting waveGlowExposure = new SliderSetting("Brightness", 2.0, 0.5, 5.0, 0.1)
      .setVisible(() -> this.mode.is("Wave") && this.waveGlowEnabled.getValue() && this.waveOuterGlow.getValue());
   public final BooleanSetting waveAutoColor = new BooleanSetting("Auto Color", false)
      .setVisible(() -> this.mode.is("Wave") && this.waveGlowEnabled.getValue());
   public final SliderSetting waveSaturation = new SliderSetting("Saturation", 1.4, 0.5, 3.0, 0.1)
      .setVisible(() -> this.mode.is("Wave") && this.waveGlowEnabled.getValue() && this.waveAutoColor.getValue());
   public final ColorSetting waveGlowColor1 = new ColorSetting(
         "Glow Color 1", new Color(138, 152, 255, 255).getRGB()
      )
      .setVisible(() -> this.mode.is("Wave") && this.waveGlowEnabled.getValue() && !this.waveAutoColor.getValue());
   public final ColorSetting waveGlowColor2 = new ColorSetting(
         "Glow Color 2", new Color(255, 107, 172, 255).getRGB()
      )
      .setVisible(() -> this.mode.is("Wave") && this.waveGlowEnabled.getValue() && !this.waveAutoColor.getValue());
   public final BooleanSetting waveRainbow = new BooleanSetting("Rainbow Color", false)
      .setVisible(() -> this.mode.is("Wave") && this.waveGlowEnabled.getValue() && !this.waveAutoColor.getValue());
   public final SliderSetting waveRainbowSpeed = new SliderSetting(
         "Rainbow Speed", 1.0, 0.1, 5.0, 0.1
      )
      .setVisible(
         () -> this.mode.is("Wave")
               && this.waveGlowEnabled.getValue()
               && !this.waveAutoColor.getValue()
               && this.waveRainbow.getValue()
      );
   public final BooleanSetting waveTrailEnabled = new BooleanSetting("Trail", true)
      .setVisible(() -> this.mode.is("Wave") && this.waveGlowEnabled.getValue() && this.waveOuterGlow.getValue());
   public final SliderSetting waveTrailFade = new SliderSetting(
         "Fade Speed", 0.009, 0.002, 0.2, 0.002
      )
      .setVisible(
         () -> this.mode.is("Wave")
               && this.waveGlowEnabled.getValue()
               && this.waveOuterGlow.getValue()
               && this.waveTrailEnabled.getValue()
      );
   public final SliderSetting waveTrailRise = new SliderSetting("Rise", 0.14, 0.0, 1.5, 0.05)
      .setVisible(
         () -> this.mode.is("Wave")
               && this.waveGlowEnabled.getValue()
               && this.waveOuterGlow.getValue()
               && this.waveTrailEnabled.getValue()
      );
   public final SliderSetting waveTrailSway = new SliderSetting("Sway", 0.025, 0.0, 0.2, 0.005)
      .setVisible(
         () -> this.mode.is("Wave")
               && this.waveGlowEnabled.getValue()
               && this.waveOuterGlow.getValue()
               && this.waveTrailEnabled.getValue()
      );
   public final SliderSetting waveTrailTurb = new SliderSetting(
         "Turbulence", 0.0, 0.0, 0.6, 0.01
      )
      .setVisible(
         () -> this.mode.is("Wave")
               && this.waveGlowEnabled.getValue()
               && this.waveOuterGlow.getValue()
               && this.waveTrailEnabled.getValue()
      );
   public final SliderSetting waveTrailFlicker = new SliderSetting("Flicker", 0.0, 0.0, 0.2, 0.01)
      .setVisible(
         () -> this.mode.is("Wave")
               && this.waveGlowEnabled.getValue()
               && this.waveOuterGlow.getValue()
               && this.waveTrailEnabled.getValue()
      );
   public final BooleanSetting waveTrailBurst = new BooleanSetting("Hit Blowback", true)
      .setVisible(
         () -> this.mode.is("Wave")
               && this.waveGlowEnabled.getValue()
               && this.waveOuterGlow.getValue()
               && this.waveTrailEnabled.getValue()
      );
   public final SliderSetting waveTrailBurstPower = new SliderSetting("Blowback Strength", 2.5, 1.0, 10.0, 0.5)
      .setVisible(
         () -> this.mode.is("Wave")
               && this.waveGlowEnabled.getValue()
               && this.waveOuterGlow.getValue()
               && this.waveTrailEnabled.getValue()
               && this.waveTrailBurst.getValue()
      );
   public final BooleanSetting waveTrailModel = new BooleanSetting("Model Trail", true)
      .setVisible(
         () -> this.mode.is("Wave")
               && this.waveGlowEnabled.getValue()
               && this.waveOuterGlow.getValue()
               && this.waveTrailEnabled.getValue()
      );
   public final SliderSetting waveTrailModelAlpha = new SliderSetting(
         "Model Opacity", 0.4, 0.1, 1.0, 0.05
      )
      .setVisible(
         () -> this.mode.is("Wave")
               && this.waveGlowEnabled.getValue()
               && this.waveOuterGlow.getValue()
               && this.waveTrailEnabled.getValue()
               && this.waveTrailModel.getValue()
      );
   private final GlassHandsRenderer renderer = GlassHandsRenderer.getInstance();

   @Override
   public void onDisable() {
      this.renderer.invalidateState();
      super.onDisable();
   }

   @Subscribe
   public void onHUD(EventHUD event) {
      if (this.isEnabled()) {
         this.renderer.renderOverlayIfPending(this);
      }
   }
}
