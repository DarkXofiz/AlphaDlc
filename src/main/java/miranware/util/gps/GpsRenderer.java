package miranware.util.gps;

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
import miranware.MiranWare;
import miranware.event.list.EventHUD;
import miranware.util.IMinecraft;
import miranware.util.render.msdf.Fonts;
import miranware.util.render.providers.ColorProvider;
import miranware.util.render.renderers.DrawUtil;

public final class GpsRenderer implements IMinecraft {
   private static final GpsRenderer INSTANCE = new GpsRenderer();
   private static final Identifier GPS_ICON = Identifier.of("mre", "textures/gps.png");
   private double targetX;
   private double targetZ;
   private boolean enabled;

   public static GpsRenderer get() {
      return INSTANCE;
   }

   private GpsRenderer() {
      MiranWare.getInstance().getEventBus().register(this);
   }

   public void setTarget(double x2, double z2) {
      this.targetX = x2;
      this.targetZ = z2;
   }

   @Subscribe
   private void onHud(EventHUD e2) {
      if (this.enabled) {
         if (MinecraftClient.getInstance().player != null) {
            DrawContext drawContext = e2.getDrawContext();
            MatrixStack ms = drawContext.getMatrices();
            int sw = mc.getWindow().getScaledWidth();
            int sh = mc.getWindow().getScaledHeight();
            Vec3d pos = mc.player.getPos();
            double dx = this.targetX - pos.x;
            double dz = this.targetZ - pos.z;
            int dist = (int)Math.sqrt(dx * dx + dz * dz);
            float angle = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
            float rot = MathHelper.wrapDegrees(angle - mc.player.getYaw());
            String distTxt = dist + "m";
            int white = ColorProvider.rgba(255, 255, 255, 255.0F);
            double cx = (double)sw / 2.0;
            double cy = (double)sh / 2.0 - 80.0;
            float w1 = Fonts.SFSEMIBOLD.get().getWidth(distTxt, 7.0F);
            DrawUtil.drawText(Fonts.SFSEMIBOLD.get(), distTxt, (float)(cx - (double)(w1 / 2.0F)), (float)(cy + 8.0), white, 7.0F);
            int iconSize = 16;
            float centerX = (float)cx;
            float centerY = (float)(cy + 22.0);
            ms.push();
            ms.translate(centerX, centerY, 0.0F);
            ms.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rot));
            ms.translate((float)(-iconSize) / 2.0F, (float)(-iconSize) / 2.0F, 0.0F);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            drawContext.drawTexture(RenderLayer::getGuiTextured, GPS_ICON, 0, 0, 0.0F, 0.0F, iconSize, iconSize, iconSize, iconSize);
            ms.pop();
         }
      }
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }
}
