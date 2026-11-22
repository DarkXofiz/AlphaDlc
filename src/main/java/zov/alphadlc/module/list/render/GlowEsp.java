package zov.alphadlc.module.list.render;

import com.google.common.eventbus.Subscribe;
import java.awt.Color;
import zov.alphadlc.event.list.EventHUD;
import zov.alphadlc.event.list.EventWorldRender;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.module.settings.ColorSetting;
import zov.alphadlc.module.settings.SliderSetting;
import zov.alphadlc.util.render.esp.GlowEspRenderer;

@ModuleInformation(
   moduleName = "Glow ESP",
   moduleDesc = "Glow \u0432\u043e\u043a\u0440\u0443\u0433 \u0438\u0433\u0440\u043e\u043a\u043e\u0432 \u043a\u0430\u043a Fill \u0432 GlassHands",
   moduleCategory = ModuleCategory.RENDER
)
public class GlowEsp extends Module {
   private static GlowEsp instance;
   public final SliderSetting glowRadius = new SliderSetting("\u0420\u0430\u0437\u043c\u044b\u0442\u0438\u0435", 4.0, 1.0, 6.0, 1.0);
   public final SliderSetting glowExposure = new SliderSetting("\u042f\u0440\u043a\u043e\u0441\u0442\u044c", 2.0, 0.5, 5.0, 0.1);
   public final SliderSetting saturation = new SliderSetting("\u041d\u0430\u0441\u044b\u0449\u0435\u043d\u043d\u043e\u0441\u0442\u044c", 1.0, 0.0, 2.0, 0.1);
   public final BooleanSetting autoColor = new BooleanSetting("\u0410\u0432\u0442\u043e \u0446\u0432\u0435\u0442", true);
   public final BooleanSetting renderSelf = new BooleanSetting("\u041d\u0430 \u0441\u0435\u0431\u044f", false);
   public final ColorSetting glowColor1 = new ColorSetting("\u0426\u0432\u0435\u0442 1", new Color(138, 152, 255, 255).getRGB())
      .setVisible(() -> !this.autoColor.getValue());
   public final ColorSetting glowColor2 = new ColorSetting("\u0426\u0432\u0435\u0442 2", new Color(255, 107, 172, 255).getRGB())
      .setVisible(() -> !this.autoColor.getValue());
   public final BooleanSetting rainbow = new BooleanSetting("\u0420\u0430\u0434\u0443\u0436\u043d\u044b\u0439 \u0446\u0432\u0435\u0442", false)
      .setVisible(() -> !this.autoColor.getValue());
   public final SliderSetting rainbowSpeed = new SliderSetting(
         "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u0440\u0430\u0434\u0443\u0433\u0438", 1.0, 0.1, 5.0, 0.1
      )
      .setVisible(() -> !this.autoColor.getValue() && this.rainbow.getValue());
   public final BooleanSetting outlineEnabled = new BooleanSetting("\u041e\u0431\u0432\u043e\u0434\u043a\u0430", false);
   public final SliderSetting outlineWidth = new SliderSetting(
         "\u0422\u043e\u043b\u0449\u0438\u043d\u0430 \u043e\u0431\u0432\u043e\u0434\u043a\u0438", 1.0, 0.5, 3.0, 0.5
      )
      .setVisible(() -> this.outlineEnabled.getValue());
   public final ColorSetting outlineColor = new ColorSetting(
         "\u0426\u0432\u0435\u0442 \u043e\u0431\u0432\u043e\u0434\u043a\u0438", new Color(138, 152, 255, 255).getRGB()
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
