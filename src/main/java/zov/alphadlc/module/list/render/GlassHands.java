package zov.alphadlc.module.list.render;

import com.google.common.eventbus.Subscribe;
import java.awt.Color;
import zov.alphadlc.event.list.EventHUD;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.module.settings.ColorSetting;
import zov.alphadlc.module.settings.ModeSetting;
import zov.alphadlc.module.settings.SliderSetting;
import zov.alphadlc.util.render.hands.GlassHandsRenderer;

@ModuleInformation(
   moduleName = "Hands",
   moduleDesc = "\u041a\u0440\u0430\u0441\u0438\u0432\u044b\u0435 \u0428\u0435\u0439\u0434\u0435\u0440\u044b \u043d\u0430 \u0440\u0443\u043a\u0438",
   moduleCategory = ModuleCategory.RENDER
)
public class GlassHands extends Module {
   public final ModeSetting mode = new ModeSetting(
      "\u0420\u0435\u0436\u0438\u043c",
      "\u0411\u043b\u044e\u0440",
      "\u0411\u043b\u044e\u0440",
      "\u0421\u0442\u0435\u043a\u043b\u043e",
      "\u0417\u0430\u043b\u0438\u0432\u043a\u0430",
      "\u0412\u043e\u043b\u043d\u0430"
   );
   public final SliderSetting waveSpeed = new SliderSetting("\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0432\u043e\u043b\u043d", 1.2, 0.1, 5.0, 0.1)
      .setVisible(() -> this.mode.is("\u041a\u0440\u0430\u0441\u0438\u0432\u044b\u0439"));
   public final SliderSetting waveScale = new SliderSetting("\u0427\u0430\u0441\u0442\u043e\u0442\u0430 \u0432\u043e\u043b\u043d", 1.0, 1.0, 3.0, 0.1)
      .setVisible(() -> this.mode.is("\u041a\u0440\u0430\u0441\u0438\u0432\u044b\u0439"));
   public final SliderSetting outline = new SliderSetting(
         "\u0428\u0438\u0440\u0438\u043d\u0430 \u043e\u0431\u0432\u043e\u0434\u043a\u0438", 1.2, 0.01, 5.0, 0.01
      )
      .setVisible(
         () -> !this.mode.is("\u0411\u043b\u044e\u0440")
               && !this.mode.is("\u041e\u0431\u0432\u043e\u0434\u043a\u0430")
               && !this.mode.is("\u0421\u0442\u0435\u043a\u043b\u043e")
               && !this.mode.is("\u0417\u0430\u043b\u0438\u0432\u043a\u0430")
               && !this.mode.is("\u0412\u043e\u043b\u043d\u0430")
      );
   public final SliderSetting glow = new SliderSetting("\u0421\u0438\u043b\u0430 \u0441\u0432\u0435\u0447\u0435\u043d\u0438\u044f", 1.0, 0.0, 5.0, 0.01)
      .setVisible(
         () -> !this.mode.is("\u0411\u043b\u044e\u0440")
               && !this.mode.is("\u041e\u0431\u0432\u043e\u0434\u043a\u0430")
               && !this.mode.is("\u0421\u0442\u0435\u043a\u043b\u043e")
               && !this.mode.is("\u0417\u0430\u043b\u0438\u0432\u043a\u0430")
               && !this.mode.is("\u0412\u043e\u043b\u043d\u0430")
      );
   public final SliderSetting fill = new SliderSetting("\u0417\u0430\u043b\u0438\u0432\u043a\u0430", 0.6, 0.0, 1.0, 0.01)
      .setVisible(
         () -> !this.mode.is("\u0411\u043b\u044e\u0440")
               && !this.mode.is("\u041e\u0431\u0432\u043e\u0434\u043a\u0430")
               && !this.mode.is("\u0421\u0442\u0435\u043a\u043b\u043e")
               && !this.mode.is("\u0417\u0430\u043b\u0438\u0432\u043a\u0430")
               && !this.mode.is("\u0412\u043e\u043b\u043d\u0430")
      );
   public final SliderSetting alpha = new SliderSetting("\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c", 1.0, 0.0, 1.0, 0.05)
      .setVisible(
         () -> !this.mode.is("\u0411\u043b\u044e\u0440")
               && !this.mode.is("\u041e\u0431\u0432\u043e\u0434\u043a\u0430")
               && !this.mode.is("\u0421\u0442\u0435\u043a\u043b\u043e")
               && !this.mode.is("\u0417\u0430\u043b\u0438\u0432\u043a\u0430")
               && !this.mode.is("\u0412\u043e\u043b\u043d\u0430")
      );
   public final BooleanSetting glowItemColor = new BooleanSetting("\u0426\u0432\u0435\u0442 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u0430", false)
      .setVisible(() -> this.mode.is("\u0421\u0432\u0435\u0447\u0435\u043d\u0438\u0435"));
   public final SliderSetting blurStrength = new SliderSetting("\u0421\u0438\u043b\u0430 \u0431\u043b\u044e\u0440\u0430", 4.0, 1.0, 8.0, 1.0)
      .setVisible(() -> this.mode.is("\u0411\u043b\u044e\u0440"));
   public final SliderSetting blurTint = new SliderSetting("\u041e\u0442\u0442\u0435\u043d\u043e\u043a", 0.3, 0.0, 1.0, 0.05)
      .setVisible(() -> this.mode.is("\u0411\u043b\u044e\u0440"));
   public final BooleanSetting blurRainbow = new BooleanSetting("\u0420\u0430\u0434\u0443\u0436\u043d\u044b\u0439 \u0446\u0432\u0435\u0442", false)
      .setVisible(() -> this.mode.is("\u0411\u043b\u044e\u0440"));
   public final SliderSetting blurRainbowSpeed = new SliderSetting(
         "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0440\u0430\u0434\u0443\u0433\u0438", 1.0, 0.1, 5.0, 0.1
      )
      .setVisible(() -> this.mode.is("\u0411\u043b\u044e\u0440") && this.blurRainbow.getValue());
   public final SliderSetting trailIntensity = new SliderSetting(
         "\u042f\u0440\u043a\u043e\u0441\u0442\u044c \u0448\u043b\u0435\u0439\u0444\u0430", 0.8, 0.3, 3.0, 0.1
      )
      .setVisible(() -> this.mode.is("\u0428\u043b\u0435\u0439\u0444"));
   public final SliderSetting trailGlowSize = new SliderSetting(
         "\u0420\u0430\u0437\u043c\u0435\u0440 \u0441\u0432\u0435\u0447\u0435\u043d\u0438\u044f", 2.0, 0.5, 5.0, 0.1
      )
      .setVisible(() -> this.mode.is("\u0428\u043b\u0435\u0439\u0444"));
   public final SliderSetting trailBlur = new SliderSetting("\u0420\u0430\u0437\u043c\u044b\u0442\u0438\u0435", 6.0, 1.0, 10.0, 1.0)
      .setVisible(() -> this.mode.is("\u0428\u043b\u0435\u0439\u0444"));
   public final BooleanSetting rainbow = new BooleanSetting("\u0420\u0430\u0434\u0443\u0436\u043d\u044b\u0439 \u0446\u0432\u0435\u0442", false)
      .setVisible(() -> this.mode.is("\u0428\u043b\u0435\u0439\u0444"));
   public final SliderSetting rainbowSpeed = new SliderSetting(
         "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0440\u0430\u0434\u0443\u0433\u0438", 1.0, 0.1, 5.0, 0.1
      )
      .setVisible(() -> this.mode.is("\u0428\u043b\u0435\u0439\u0444") && this.rainbow.getValue());
   public final SliderSetting trailIntensityM = new SliderSetting(
         "\u0418\u043d\u0442\u0435\u043d\u0441\u0438\u0432\u043d\u043e\u0441\u0442\u044c", 0.72, 0.1, 1.5, 0.05
      )
      .setVisible(() -> this.mode.is("Trail"));
   public final SliderSetting trailSpeed = new SliderSetting("\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c", 1.05, 0.2, 3.0, 0.05)
      .setVisible(() -> this.mode.is("Trail"));
   public final SliderSetting trailLength = new SliderSetting("\u0414\u043b\u0438\u043d\u0430 \u0448\u043b\u0435\u0439\u0444\u0430", 0.32, 0.1, 1.0, 0.05)
      .setVisible(() -> this.mode.is("Trail"));
   public final SliderSetting trailSoftness = new SliderSetting("\u041c\u044f\u0433\u043a\u043e\u0441\u0442\u044c", 1.05, 0.4, 2.0, 0.05)
      .setVisible(() -> this.mode.is("Trail"));
   public final SliderSetting trailBlurRadius = new SliderSetting("\u0420\u0430\u0437\u043c\u044b\u0442\u0438\u0435", 1.05, 0.2, 2.5, 0.05)
      .setVisible(() -> this.mode.is("Trail"));
   public final SliderSetting trailSmoke = new SliderSetting("\u0414\u044b\u043c", 0.22, 0.0, 0.8, 0.05).setVisible(() -> this.mode.is("Trail"));
   public final SliderSetting trailAttack = new SliderSetting("\u0423\u0434\u0430\u0440", 0.58, 0.0, 1.0, 0.05).setVisible(() -> this.mode.is("Trail"));
   public final SliderSetting trailCamera = new SliderSetting(
         "\u0421\u043b\u0435\u0434\u043e\u0432\u0430\u043d\u0438\u0435 \u043a\u0430\u043c\u0435\u0440\u044b", 0.45, 0.0, 1.5, 0.05
      )
      .setVisible(() -> this.mode.is("Trail"));
   public final BooleanSetting trailItemColor = new BooleanSetting("\u0426\u0432\u0435\u0442 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u0430", false)
      .setVisible(() -> this.mode.is("Trail"));
   public final SliderSetting glassMixFactor = new SliderSetting("\u0421\u043c\u0435\u0448\u0438\u0432\u0430\u043d\u0438\u0435", 0.0, 0.0, 1.0, 0.01)
      .setVisible(() -> this.mode.is("\u0421\u0442\u0435\u043a\u043b\u043e"));
   public final BooleanSetting glassGlowEnabled = new BooleanSetting("\u0413\u043b\u043e\u0432", true)
      .setVisible(() -> this.mode.is("\u0421\u0442\u0435\u043a\u043b\u043e"));
   public final SliderSetting glassGlowRadius = new SliderSetting("\u0420\u0430\u0434\u0438\u0443\u0441 \u0433\u043b\u043e\u0432\u0430", 3.0, 1.0, 6.0, 1.0)
      .setVisible(() -> this.mode.is("\u0421\u0442\u0435\u043a\u043b\u043e") && this.glassGlowEnabled.getValue());
   public final BooleanSetting glassOuterGlow = new BooleanSetting("\u0412\u043d\u0435\u0448\u043d\u0438\u0439 \u0433\u043b\u043e\u0432", true)
      .setVisible(() -> this.mode.is("\u0421\u0442\u0435\u043a\u043b\u043e") && this.glassGlowEnabled.getValue());
   public final SliderSetting glassGlowExposure = new SliderSetting(
         "\u042f\u0440\u043a\u043e\u0441\u0442\u044c \u0433\u043b\u043e\u0432\u0430", 2.0, 0.5, 5.0, 0.1
      )
      .setVisible(() -> this.mode.is("\u0421\u0442\u0435\u043a\u043b\u043e") && this.glassGlowEnabled.getValue() && this.glassOuterGlow.getValue());
   public final ColorSetting glassGlowColor1 = new ColorSetting(
         "\u0426\u0432\u0435\u0442 \u0433\u043b\u043e\u0432\u0430 1", new Color(138, 152, 255, 255).getRGB()
      )
      .setVisible(() -> this.mode.is("\u0421\u0442\u0435\u043a\u043b\u043e") && this.glassGlowEnabled.getValue());
   public final ColorSetting glassGlowColor2 = new ColorSetting(
         "\u0426\u0432\u0435\u0442 \u0433\u043b\u043e\u0432\u0430 2", new Color(255, 107, 172, 255).getRGB()
      )
      .setVisible(() -> this.mode.is("\u0421\u0442\u0435\u043a\u043b\u043e") && this.glassGlowEnabled.getValue());
   public final BooleanSetting glassRainbow = new BooleanSetting("\u0420\u0430\u0434\u0443\u0436\u043d\u044b\u0439 \u0446\u0432\u0435\u0442", false)
      .setVisible(() -> this.mode.is("\u0421\u0442\u0435\u043a\u043b\u043e") && this.glassGlowEnabled.getValue());
   public final SliderSetting glassRainbowSpeed = new SliderSetting(
         "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0440\u0430\u0434\u0443\u0433\u0438", 1.0, 0.1, 5.0, 0.1
      )
      .setVisible(() -> this.mode.is("\u0421\u0442\u0435\u043a\u043b\u043e") && this.glassGlowEnabled.getValue() && this.glassRainbow.getValue());
   public final SliderSetting plasmaSpeed = new SliderSetting("\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c", 1.0, 0.1, 3.0, 0.1)
      .setVisible(() -> this.mode.is("Plasma"));
   public final SliderSetting plasmaScale = new SliderSetting("\u041c\u0430\u0441\u0448\u0442\u0430\u0431", 1.0, 0.5, 3.0, 0.1)
      .setVisible(() -> this.mode.is("Plasma"));
   public final BooleanSetting plasmaStars = new BooleanSetting("\u0417\u0432\u0451\u0437\u0434\u044b", true).setVisible(() -> this.mode.is("Plasma"));
   public final BooleanSetting fillGlassMode = new BooleanSetting("\u0421\u0442\u0435\u043a\u043b\u043e", false)
      .setVisible(() -> this.mode.is("\u0417\u0430\u043b\u0438\u0432\u043a\u0430"));
   public final SliderSetting fillGlassMix = new SliderSetting("\u0421\u043c\u0435\u0448\u0438\u0432\u0430\u043d\u0438\u0435", 0.0, 0.0, 1.0, 0.01)
      .setVisible(() -> this.mode.is("\u0417\u0430\u043b\u0438\u0432\u043a\u0430") && this.fillGlassMode.getValue());
   public final ColorSetting fillColor = new ColorSetting(
         "\u0426\u0432\u0435\u0442 \u0437\u0430\u043b\u0438\u0432\u043a\u0438", new Color(255, 68, 68, 255).getRGB()
      )
      .setVisible(() -> this.mode.is("\u0417\u0430\u043b\u0438\u0432\u043a\u0430") && !this.fillGlassMode.getValue());
   public final BooleanSetting fillRainbow = new BooleanSetting("\u0420\u0430\u0434\u0443\u0436\u043d\u044b\u0439 \u0446\u0432\u0435\u0442", false)
      .setVisible(() -> this.mode.is("\u0417\u0430\u043b\u0438\u0432\u043a\u0430") && !this.fillGlassMode.getValue());
   public final SliderSetting fillRainbowSpeed = new SliderSetting(
         "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0440\u0430\u0434\u0443\u0433\u0438", 1.0, 0.1, 5.0, 0.1
      )
      .setVisible(() -> this.mode.is("\u0417\u0430\u043b\u0438\u0432\u043a\u0430") && !this.fillGlassMode.getValue() && this.fillRainbow.getValue());
   public final SliderSetting fillAlpha = new SliderSetting(
         "\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c \u0437\u0430\u043b\u0438\u0432\u043a\u0438", 0.8, 0.0, 1.0, 0.05
      )
      .setVisible(() -> this.mode.is("\u0417\u0430\u043b\u0438\u0432\u043a\u0430") && !this.fillGlassMode.getValue());
   public final BooleanSetting fillKeepShading = new BooleanSetting("\u0421\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u0442\u0435\u043d\u0438", true)
      .setVisible(() -> this.mode.is("\u0417\u0430\u043b\u0438\u0432\u043a\u0430") && !this.fillGlassMode.getValue());
   public final SliderSetting fillShadingStrength = new SliderSetting("\u0421\u0438\u043b\u0430 \u0442\u0435\u043d\u0435\u0439", 0.3, 0.0, 1.0, 0.05)
      .setVisible(() -> this.mode.is("\u0417\u0430\u043b\u0438\u0432\u043a\u0430") && !this.fillGlassMode.getValue() && this.fillKeepShading.getValue());
   public final BooleanSetting fillOutlineEnabled = new BooleanSetting("\u041e\u0431\u0432\u043e\u0434\u043a\u0430", false)
      .setVisible(() -> this.mode.is("\u0417\u0430\u043b\u0438\u0432\u043a\u0430"));
   public final SliderSetting fillOutlineWidth = new SliderSetting(
         "\u0422\u043e\u043b\u0449\u0438\u043d\u0430 \u043e\u0431\u0432\u043e\u0434\u043a\u0438", 1.0, 0.5, 3.0, 0.5
      )
      .setVisible(() -> this.mode.is("\u0417\u0430\u043b\u0438\u0432\u043a\u0430") && this.fillOutlineEnabled.getValue());
   public final ColorSetting fillOutlineColor = new ColorSetting(
         "\u0426\u0432\u0435\u0442 \u043e\u0431\u0432\u043e\u0434\u043a\u0438", new Color(138, 152, 255, 255).getRGB()
      )
      .setVisible(() -> this.mode.is("\u0417\u0430\u043b\u0438\u0432\u043a\u0430") && this.fillOutlineEnabled.getValue());
   public final BooleanSetting fillGlowEnabled = new BooleanSetting("\u0413\u043b\u043e\u0432", true)
      .setVisible(() -> this.mode.is("\u0417\u0430\u043b\u0438\u0432\u043a\u0430"));
   public final SliderSetting fillGlowRadius = new SliderSetting("\u0420\u0430\u0437\u043c\u044b\u0442\u0438\u0435", 4.0, 1.0, 6.0, 1.0)
      .setVisible(() -> this.mode.is("\u0417\u0430\u043b\u0438\u0432\u043a\u0430") && this.fillGlowEnabled.getValue());
   public final BooleanSetting fillOuterGlow = new BooleanSetting("\u0412\u043d\u0435\u0448\u043d\u0438\u0439 \u0433\u043b\u043e\u0432", true)
      .setVisible(() -> this.mode.is("\u0417\u0430\u043b\u0438\u0432\u043a\u0430") && this.fillGlowEnabled.getValue());
   public final SliderSetting fillGlowExposure = new SliderSetting("\u042f\u0440\u043a\u043e\u0441\u0442\u044c", 2.0, 0.5, 5.0, 0.1)
      .setVisible(() -> this.mode.is("\u0417\u0430\u043b\u0438\u0432\u043a\u0430") && this.fillGlowEnabled.getValue() && this.fillOuterGlow.getValue());
   public final BooleanSetting fillAutoColor = new BooleanSetting("\u0410\u0432\u0442\u043e \u0446\u0432\u0435\u0442", false)
      .setVisible(() -> this.mode.is("\u0417\u0430\u043b\u0438\u0432\u043a\u0430") && (this.fillGlowEnabled.getValue() || this.fillOutlineEnabled.getValue()));
   public final SliderSetting fillSaturation = new SliderSetting("\u041d\u0430\u0441\u044b\u0449\u0435\u043d\u043d\u043e\u0441\u0442\u044c", 1.4, 0.5, 3.0, 0.1)
      .setVisible(
         () -> this.mode.is("\u0417\u0430\u043b\u0438\u0432\u043a\u0430")
               && (this.fillGlowEnabled.getValue() || this.fillOutlineEnabled.getValue())
               && this.fillAutoColor.getValue()
      );
   public final ColorSetting fillGlowColor1 = new ColorSetting(
         "\u0426\u0432\u0435\u0442 \u0433\u043b\u043e\u0432\u0430 1", new Color(138, 152, 255, 255).getRGB()
      )
      .setVisible(() -> this.mode.is("\u0417\u0430\u043b\u0438\u0432\u043a\u0430") && this.fillGlowEnabled.getValue() && !this.fillAutoColor.getValue());
   public final ColorSetting fillGlowColor2 = new ColorSetting(
         "\u0426\u0432\u0435\u0442 \u0433\u043b\u043e\u0432\u0430 2", new Color(255, 107, 172, 255).getRGB()
      )
      .setVisible(() -> this.mode.is("\u0417\u0430\u043b\u0438\u0432\u043a\u0430") && this.fillGlowEnabled.getValue() && !this.fillAutoColor.getValue());
   public final BooleanSetting fillTrailEnabled = new BooleanSetting("\u0428\u043b\u0435\u0439\u0444", true)
      .setVisible(() -> this.mode.is("\u0417\u0430\u043b\u0438\u0432\u043a\u0430") && this.fillGlowEnabled.getValue() && this.fillOuterGlow.getValue());
   public final SliderSetting fillTrailFade = new SliderSetting(
         "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0437\u0430\u0442\u0443\u0445\u0430\u043d\u0438\u044f", 0.009, 0.002, 0.2, 0.002
      )
      .setVisible(
         () -> this.mode.is("\u0417\u0430\u043b\u0438\u0432\u043a\u0430")
               && this.fillGlowEnabled.getValue()
               && this.fillOuterGlow.getValue()
               && this.fillTrailEnabled.getValue()
      );
   public final SliderSetting fillTrailRise = new SliderSetting("\u041f\u043e\u0434\u044a\u0451\u043c", 0.14, 0.0, 1.5, 0.05)
      .setVisible(
         () -> this.mode.is("\u0417\u0430\u043b\u0438\u0432\u043a\u0430")
               && this.fillGlowEnabled.getValue()
               && this.fillOuterGlow.getValue()
               && this.fillTrailEnabled.getValue()
      );
   public final SliderSetting fillTrailSway = new SliderSetting("\u041a\u0430\u0447\u0430\u043d\u0438\u0435", 0.025, 0.0, 0.2, 0.005)
      .setVisible(
         () -> this.mode.is("\u0417\u0430\u043b\u0438\u0432\u043a\u0430")
               && this.fillGlowEnabled.getValue()
               && this.fillOuterGlow.getValue()
               && this.fillTrailEnabled.getValue()
      );
   public final SliderSetting fillTrailTurb = new SliderSetting(
         "\u0422\u0443\u0440\u0431\u0443\u043b\u0435\u043d\u0442\u043d\u043e\u0441\u0442\u044c", 0.0, 0.0, 0.6, 0.01
      )
      .setVisible(
         () -> this.mode.is("\u0417\u0430\u043b\u0438\u0432\u043a\u0430")
               && this.fillGlowEnabled.getValue()
               && this.fillOuterGlow.getValue()
               && this.fillTrailEnabled.getValue()
      );
   public final SliderSetting fillTrailFlicker = new SliderSetting("\u041c\u0435\u0440\u0446\u0430\u043d\u0438\u0435", 0.0, 0.0, 0.2, 0.01)
      .setVisible(
         () -> this.mode.is("\u0417\u0430\u043b\u0438\u0432\u043a\u0430")
               && this.fillGlowEnabled.getValue()
               && this.fillOuterGlow.getValue()
               && this.fillTrailEnabled.getValue()
      );
   public final BooleanSetting fillTrailBurst = new BooleanSetting("\u0421\u0434\u0443\u0432 \u043f\u0440\u0438 \u0443\u0434\u0430\u0440\u0435", true)
      .setVisible(
         () -> this.mode.is("\u0417\u0430\u043b\u0438\u0432\u043a\u0430")
               && this.fillGlowEnabled.getValue()
               && this.fillOuterGlow.getValue()
               && this.fillTrailEnabled.getValue()
      );
   public final SliderSetting fillTrailBurstPower = new SliderSetting("\u0421\u0438\u043b\u0430 \u0441\u0434\u0443\u0432\u0430", 2.5, 1.0, 10.0, 0.5)
      .setVisible(
         () -> this.mode.is("\u0417\u0430\u043b\u0438\u0432\u043a\u0430")
               && this.fillGlowEnabled.getValue()
               && this.fillOuterGlow.getValue()
               && this.fillTrailEnabled.getValue()
               && this.fillTrailBurst.getValue()
      );
   public final BooleanSetting fillTrailModel = new BooleanSetting("\u0428\u043b\u0435\u0439\u0444 \u043c\u043e\u0434\u0435\u043b\u0438", true)
      .setVisible(
         () -> this.mode.is("\u0417\u0430\u043b\u0438\u0432\u043a\u0430")
               && this.fillGlowEnabled.getValue()
               && this.fillOuterGlow.getValue()
               && this.fillTrailEnabled.getValue()
      );
   public final SliderSetting fillTrailModelAlpha = new SliderSetting(
         "\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c \u043c\u043e\u0434\u0435\u043b\u0438", 0.4, 0.1, 1.0, 0.05
      )
      .setVisible(
         () -> this.mode.is("\u0417\u0430\u043b\u0438\u0432\u043a\u0430")
               && this.fillGlowEnabled.getValue()
               && this.fillOuterGlow.getValue()
               && this.fillTrailEnabled.getValue()
               && this.fillTrailModel.getValue()
      );
   public final SliderSetting waveSpeedX = new SliderSetting("\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c X", 0.22, 0.0, 1.5, 0.01)
      .setVisible(() -> this.mode.is("\u0412\u043e\u043b\u043d\u0430"));
   public final SliderSetting waveSpeedY = new SliderSetting("\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c Y", 0.15, 0.0, 1.5, 0.01)
      .setVisible(() -> this.mode.is("\u0412\u043e\u043b\u043d\u0430"));
   public final SliderSetting waveScaleM = new SliderSetting("\u041c\u0430\u0441\u0448\u0442\u0430\u0431", 1.35, 0.2, 4.0, 0.05)
      .setVisible(() -> this.mode.is("\u0412\u043e\u043b\u043d\u0430"));
   public final SliderSetting waveDensity = new SliderSetting("\u041f\u043b\u043e\u0442\u043d\u043e\u0441\u0442\u044c", 1.15, 0.5, 3.0, 0.05)
      .setVisible(() -> this.mode.is("\u0412\u043e\u043b\u043d\u0430"));
   public final SliderSetting waveGlow = new SliderSetting("\u0421\u0438\u043b\u0430 \u0432\u043e\u043b\u043d", 1.0, 0.2, 3.0, 0.05)
      .setVisible(() -> this.mode.is("\u0412\u043e\u043b\u043d\u0430"));
   public final SliderSetting waveFillAlpha = new SliderSetting(
         "\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c", 0.85, 0.1, 1.0, 0.05
      )
      .setVisible(() -> this.mode.is("\u0412\u043e\u043b\u043d\u0430"));
   public final SliderSetting waveModelVisibility = new SliderSetting(
         "\u0412\u0438\u0434\u0438\u043c\u043e\u0441\u0442\u044c \u043c\u043e\u0434\u0435\u043b\u0438", 0.25, 0.0, 1.0, 0.05
      )
      .setVisible(() -> this.mode.is("\u0412\u043e\u043b\u043d\u0430"));
   public final BooleanSetting waveGlowEnabled = new BooleanSetting("\u0413\u043b\u043e\u0432", true)
      .setVisible(() -> this.mode.is("\u0412\u043e\u043b\u043d\u0430"));
   public final SliderSetting waveGlowRadius = new SliderSetting("\u0420\u0430\u0437\u043c\u044b\u0442\u0438\u0435", 4.0, 1.0, 6.0, 1.0)
      .setVisible(() -> this.mode.is("\u0412\u043e\u043b\u043d\u0430") && this.waveGlowEnabled.getValue());
   public final BooleanSetting waveOuterGlow = new BooleanSetting("\u0412\u043d\u0435\u0448\u043d\u0438\u0439 \u0433\u043b\u043e\u0432", true)
      .setVisible(() -> this.mode.is("\u0412\u043e\u043b\u043d\u0430") && this.waveGlowEnabled.getValue());
   public final SliderSetting waveGlowExposure = new SliderSetting("\u042f\u0440\u043a\u043e\u0441\u0442\u044c", 2.0, 0.5, 5.0, 0.1)
      .setVisible(() -> this.mode.is("\u0412\u043e\u043b\u043d\u0430") && this.waveGlowEnabled.getValue() && this.waveOuterGlow.getValue());
   public final BooleanSetting waveAutoColor = new BooleanSetting("\u0410\u0432\u0442\u043e \u0446\u0432\u0435\u0442", false)
      .setVisible(() -> this.mode.is("\u0412\u043e\u043b\u043d\u0430") && this.waveGlowEnabled.getValue());
   public final SliderSetting waveSaturation = new SliderSetting("\u041d\u0430\u0441\u044b\u0449\u0435\u043d\u043d\u043e\u0441\u0442\u044c", 1.4, 0.5, 3.0, 0.1)
      .setVisible(() -> this.mode.is("\u0412\u043e\u043b\u043d\u0430") && this.waveGlowEnabled.getValue() && this.waveAutoColor.getValue());
   public final ColorSetting waveGlowColor1 = new ColorSetting(
         "\u0426\u0432\u0435\u0442 \u0433\u043b\u043e\u0432\u0430 1", new Color(138, 152, 255, 255).getRGB()
      )
      .setVisible(() -> this.mode.is("\u0412\u043e\u043b\u043d\u0430") && this.waveGlowEnabled.getValue() && !this.waveAutoColor.getValue());
   public final ColorSetting waveGlowColor2 = new ColorSetting(
         "\u0426\u0432\u0435\u0442 \u0433\u043b\u043e\u0432\u0430 2", new Color(255, 107, 172, 255).getRGB()
      )
      .setVisible(() -> this.mode.is("\u0412\u043e\u043b\u043d\u0430") && this.waveGlowEnabled.getValue() && !this.waveAutoColor.getValue());
   public final BooleanSetting waveRainbow = new BooleanSetting("\u0420\u0430\u0434\u0443\u0436\u043d\u044b\u0439 \u0446\u0432\u0435\u0442", false)
      .setVisible(() -> this.mode.is("\u0412\u043e\u043b\u043d\u0430") && this.waveGlowEnabled.getValue() && !this.waveAutoColor.getValue());
   public final SliderSetting waveRainbowSpeed = new SliderSetting(
         "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0440\u0430\u0434\u0443\u0433\u0438", 1.0, 0.1, 5.0, 0.1
      )
      .setVisible(
         () -> this.mode.is("\u0412\u043e\u043b\u043d\u0430")
               && this.waveGlowEnabled.getValue()
               && !this.waveAutoColor.getValue()
               && this.waveRainbow.getValue()
      );
   public final BooleanSetting waveTrailEnabled = new BooleanSetting("\u0428\u043b\u0435\u0439\u0444", true)
      .setVisible(() -> this.mode.is("\u0412\u043e\u043b\u043d\u0430") && this.waveGlowEnabled.getValue() && this.waveOuterGlow.getValue());
   public final SliderSetting waveTrailFade = new SliderSetting(
         "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0437\u0430\u0442\u0443\u0445\u0430\u043d\u0438\u044f", 0.009, 0.002, 0.2, 0.002
      )
      .setVisible(
         () -> this.mode.is("\u0412\u043e\u043b\u043d\u0430")
               && this.waveGlowEnabled.getValue()
               && this.waveOuterGlow.getValue()
               && this.waveTrailEnabled.getValue()
      );
   public final SliderSetting waveTrailRise = new SliderSetting("\u041f\u043e\u0434\u044a\u0451\u043c", 0.14, 0.0, 1.5, 0.05)
      .setVisible(
         () -> this.mode.is("\u0412\u043e\u043b\u043d\u0430")
               && this.waveGlowEnabled.getValue()
               && this.waveOuterGlow.getValue()
               && this.waveTrailEnabled.getValue()
      );
   public final SliderSetting waveTrailSway = new SliderSetting("\u041a\u0430\u0447\u0430\u043d\u0438\u0435", 0.025, 0.0, 0.2, 0.005)
      .setVisible(
         () -> this.mode.is("\u0412\u043e\u043b\u043d\u0430")
               && this.waveGlowEnabled.getValue()
               && this.waveOuterGlow.getValue()
               && this.waveTrailEnabled.getValue()
      );
   public final SliderSetting waveTrailTurb = new SliderSetting(
         "\u0422\u0443\u0440\u0431\u0443\u043b\u0435\u043d\u0442\u043d\u043e\u0441\u0442\u044c", 0.0, 0.0, 0.6, 0.01
      )
      .setVisible(
         () -> this.mode.is("\u0412\u043e\u043b\u043d\u0430")
               && this.waveGlowEnabled.getValue()
               && this.waveOuterGlow.getValue()
               && this.waveTrailEnabled.getValue()
      );
   public final SliderSetting waveTrailFlicker = new SliderSetting("\u041c\u0435\u0440\u0446\u0430\u043d\u0438\u0435", 0.0, 0.0, 0.2, 0.01)
      .setVisible(
         () -> this.mode.is("\u0412\u043e\u043b\u043d\u0430")
               && this.waveGlowEnabled.getValue()
               && this.waveOuterGlow.getValue()
               && this.waveTrailEnabled.getValue()
      );
   public final BooleanSetting waveTrailBurst = new BooleanSetting("\u0421\u0434\u0443\u0432 \u043f\u0440\u0438 \u0443\u0434\u0430\u0440\u0435", true)
      .setVisible(
         () -> this.mode.is("\u0412\u043e\u043b\u043d\u0430")
               && this.waveGlowEnabled.getValue()
               && this.waveOuterGlow.getValue()
               && this.waveTrailEnabled.getValue()
      );
   public final SliderSetting waveTrailBurstPower = new SliderSetting("\u0421\u0438\u043b\u0430 \u0441\u0434\u0443\u0432\u0430", 2.5, 1.0, 10.0, 0.5)
      .setVisible(
         () -> this.mode.is("\u0412\u043e\u043b\u043d\u0430")
               && this.waveGlowEnabled.getValue()
               && this.waveOuterGlow.getValue()
               && this.waveTrailEnabled.getValue()
               && this.waveTrailBurst.getValue()
      );
   public final BooleanSetting waveTrailModel = new BooleanSetting("\u0428\u043b\u0435\u0439\u0444 \u043c\u043e\u0434\u0435\u043b\u0438", true)
      .setVisible(
         () -> this.mode.is("\u0412\u043e\u043b\u043d\u0430")
               && this.waveGlowEnabled.getValue()
               && this.waveOuterGlow.getValue()
               && this.waveTrailEnabled.getValue()
      );
   public final SliderSetting waveTrailModelAlpha = new SliderSetting(
         "\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c \u043c\u043e\u0434\u0435\u043b\u0438", 0.4, 0.1, 1.0, 0.05
      )
      .setVisible(
         () -> this.mode.is("\u0412\u043e\u043b\u043d\u0430")
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
