package zov.alphadlc.util.gps;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import zov.alphadlc.AlphaDLC;
import zov.alphadlc.event.list.EventHUD;
import zov.alphadlc.util.IMinecraft;
import zov.alphadlc.util.render.msdf.Fonts;
import zov.alphadlc.util.render.providers.ColorProvider;
import zov.alphadlc.util.render.renderers.DrawUtil;

public final class GpsRenderer implements IMinecraft {
   private static final GpsRenderer INSTANCE = new GpsRenderer();
   private static final Identifier GPS_ICON = Identifier.method_60655("mre", "textures/gps.png");
   private double targetX;
   private double targetZ;
   private boolean enabled;

   public static GpsRenderer get() {
      return INSTANCE;
   }

   private GpsRenderer() {
      AlphaDLC.getInstance().getEventBus().register(this);
   }

   public void setTarget(double x2, double z2) {
      this.targetX = x2;
      this.targetZ = z2;
   }

   @Subscribe
   private void onHud(EventHUD e2) {
      if (this.enabled) {
         if (MinecraftClient.method_1551().field_1724 != null) {
            DrawContext drawContext = e2.getDrawContext();
            MatrixStack ms = drawContext.method_51448();
            int sw = mc.method_22683().method_4486();
            int sh = mc.method_22683().method_4502();
            Vec3d pos = mc.field_1724.method_19538();
            double dx = this.targetX - pos.field_1352;
            double dz = this.targetZ - pos.field_1350;
            int dist = (int)Math.sqrt(dx * dx + dz * dz);
            float angle = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
            float rot = MathHelper.method_15393(angle - mc.field_1724.method_36454());
            String distTxt = dist + "m";
            int white = ColorProvider.rgba(255, 255, 255, 255.0F);
            double cx = (double)sw / 2.0;
            double cy = (double)sh / 2.0 - 80.0;
            float w1 = Fonts.SFSEMIBOLD.get().getWidth(distTxt, 7.0F);
            DrawUtil.drawText(Fonts.SFSEMIBOLD.get(), distTxt, (float)(cx - (double)(w1 / 2.0F)), (float)(cy + 8.0), white, 7.0F);
            int iconSize = 16;
            float centerX = (float)cx;
            float centerY = (float)(cy + 22.0);
            ms.method_22903();
            ms.method_46416(centerX, centerY, 0.0F);
            ms.method_22907(RotationAxis.field_40718.rotationDegrees(rot));
            ms.method_46416((float)(-iconSize) / 2.0F, (float)(-iconSize) / 2.0F, 0.0F);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            drawContext.method_25290(RenderLayer::method_62277, GPS_ICON, 0, 0, 0.0F, 0.0F, iconSize, iconSize, iconSize, iconSize);
            ms.method_22909();
         }
      }
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }
}
