package miranware.module.list.render;

import com.google.common.eventbus.Subscribe;
import java.awt.Color;
import miranware.event.list.EventHUD;
import miranware.event.list.EventWorldRender;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.BooleanSetting;
import miranware.module.settings.ColorSetting;
import miranware.module.settings.SliderSetting;
import miranware.util.render.esp.GlowEspRenderer;

@ModuleInformation(
   moduleName = "Glow ESP",
   moduleDesc = "Glow around players like Fill in GlassHands",
   moduleCategory = ModuleCategory.RENDER
)
public class GlowEsp extends Module {
   private static GlowEsp instance;
   public final SliderSetting glowRadius = new SliderSetting("Blur Amount", 4.0, 1.0, 6.0, 1.0);
   public final SliderSetting glowExposure = new SliderSetting("Brightness", 2.0, 0.5, 5.0, 0.1);
   public final SliderSetting saturation = new SliderSetting("Saturation", 1.0, 0.0, 2.0, 0.1);
   public final BooleanSetting autoColor = new BooleanSetting("Auto Color", true);
   public final BooleanSetting renderSelf = new BooleanSetting("On Self", false);
   public final ColorSetting glowColor1 = new ColorSetting("Color 1", new Color(138, 152, 255, 255).getRGB())
      .setVisible(() -> !this.autoColor.getValue());
   public final ColorSetting glowColor2 = new ColorSetting("Color 2", new Color(255, 107, 172, 255).getRGB())
      .setVisible(() -> !this.autoColor.getValue());
   public final BooleanSetting rainbow = new BooleanSetting("Rainbow Color", false)
      .setVisible(() -> !this.autoColor.getValue());
   public final SliderSetting rainbowSpeed = new SliderSetting(
         "Rainbow Speed", 1.0, 0.1, 5.0, 0.1
      )
      .setVisible(() -> !this.autoColor.getValue() && this.rainbow.getValue());
   public final BooleanSetting outlineEnabled = new BooleanSetting("Outline", false);
   public final SliderSetting outlineWidth = new SliderSetting(
         "Outline Thickness", 1.0, 0.5, 3.0, 0.5
      )
      .setVisible(() -> this.outlineEnabled.getValue());
   public final ColorSetting outlineColor = new ColorSetting(
         "Outline Color", new Color(138, 152, 255, 255).getRGB()
      )
      .setVisible(() -> this.outlineEnabled.getValue() && !this.autoColor.getValue());

   public GlowEsp() {
      instance = this;
   }

   public static GlowEsp getInstance() {
      return instance;
   }

   @Override
   public void onDisable() {
      GlowEspRenderer.getInstance().invalidate();
      super.onDisable();
   }

   @Subscribe
   public void onWorldRender(EventWorldRender event) {
      GlowEspRenderer.getInstance().renderMask(this, event.getMatrixStack(), event.getTickDelta());
   }

   @Subscribe
   public void onHUD(EventHUD event) {
      GlowEspRenderer.getInstance().compositeIfReady(this);
   }
}
