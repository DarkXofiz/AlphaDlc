package zov.alphadlc.module.list.render;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import zov.alphadlc.event.list.EventPacket;
import zov.alphadlc.event.list.EventWorldRender;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.util.render.providers.ColorProvider;

@ModuleInformation(
   moduleName = "Soul ESP",
   moduleDesc = "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0435\u0442 \u0434\u0443\u0448\u0443 \u0438\u0433\u0440\u043e\u043a\u0430 \u043f\u043e\u0441\u043b\u0435 \u0441\u043c\u0435\u0440\u0442\u0438",
   moduleCategory = ModuleCategory.RENDER
)
public class SoulESP extends Module {
   private static final float DURATION = 3.0F;
   private static final float HEIGHT = 3.5F;
   private final List<SoulESP.Ghost> ghosts = new ArrayList<>();

   @Subscribe
   public void onPacket(EventPacket e) {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
         if (e.getPacket() instanceof EntityStatusS2CPacket packet) {
            byte status = packet.method_11470();
            if ((status == 3 || status == 35) && packet.method_11469(this.mc.field_1687) instanceof PlayerEntity player) {
               if (player == this.mc.field_1724) {
                  return;
               }

               this.ghosts
                  .add(
                     new SoulESP.Ghost(player.method_19538(), player.method_43078(), player.method_5715(), (float)player.field_6012, System.currentTimeMillis())
                  );
            }
         }
      }
   }

   @Subscribe
   public void onRender(EventWorldRender e) {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null && !this.ghosts.isEmpty()) {
         long now = System.currentTimeMillis();
         float dur = 3000.0F;
         this.ghosts.removeIf(gx -> (float)(now - gx.time) >= dur);
         Vec3d cam = this.mc.field_1773.method_19418().method_19326();
         MatrixStack m = e.getMatrixStack();
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableDepthTest();
         RenderSystem.disableCull();
         RenderSystem.setShader(ShaderProgramKeys.field_53876);

         for (SoulESP.Ghost g : this.ghosts) {
            float t = (float)(now - g.time) / dur;
            if (!(t >= 1.0F)) {
               float alpha = (1.0F - t) * 0.6F;
               float rise = 3.5F * this.ease(t);
               int colorFirst = ColorProvider.getColorClient();
               int colorSecond = ColorProvider.getColorClient();
               int c = ColorProvider.interpolateColor(colorFirst, colorSecond, t);
               float r = (float)ColorProvider.red(c) / 255.0F;
               float gr = (float)ColorProvider.green(c) / 255.0F;
               float b = (float)ColorProvider.blue(c) / 255.0F;
               m.method_22903();
               m.method_22904(g.pos.field_1352 - cam.field_1352, g.pos.field_1351 - cam.field_1351 + (double)rise, g.pos.field_1350 - cam.field_1350);
               m.method_22907(RotationAxis.field_40716.rotationDegrees(180.0F - g.yaw));
               m.method_22905(-1.0F, -1.0F, 1.0F);
               m.method_22904(0.0, -1.5, 0.0);
               if (g.sneak) {
                  m.method_22904(0.0, 0.2, 0.0);
                  m.method_22907(RotationAxis.field_40714.rotationDegrees(28.0F));
               }

               Matrix4f mat = m.method_23760().method_23761();
               BufferBuilder buf = Tessellator.method_1348().method_60827(DrawMode.field_27382, VertexFormats.field_1576);
               float u = 0.0625F;
               float swing = MathHelper.method_15374(g.phase * 0.6662F) * 0.6F;
               this.box(buf, mat, -4.0F * u, 0.0F, -2.0F * u, 8.0F * u, 12.0F * u, 4.0F * u, r, gr, b, alpha);
               this.box(buf, mat, -4.0F * u, -8.0F * u, -4.0F * u, 8.0F * u, 8.0F * u, 8.0F * u, r, gr, b, alpha);
               m.method_22903();
               m.method_46416(-6.0F * u, 2.0F * u, 0.0F);
               m.method_22907(RotationAxis.field_40714.rotation(-swing));
               m.method_46416(6.0F * u, -2.0F * u, 0.0F);
               this.box(buf, m.method_23760().method_23761(), -8.0F * u, -2.0F * u, -2.0F * u, 4.0F * u, 12.0F * u, 4.0F * u, r, gr, b, alpha);
               m.method_22909();
               m.method_22903();
               m.method_46416(6.0F * u, 2.0F * u, 0.0F);
               m.method_22907(RotationAxis.field_40714.rotation(swing));
               m.method_46416(-6.0F * u, -2.0F * u, 0.0F);
               this.box(buf, m.method_23760().method_23761(), 4.0F * u, -2.0F * u, -2.0F * u, 4.0F * u, 12.0F * u, 4.0F * u, r, gr, b, alpha);
               m.method_22909();
               m.method_22903();
               m.method_46416(-2.0F * u, 12.0F * u, 0.0F);
               m.method_22907(RotationAxis.field_40714.rotation(swing));
               m.method_46416(2.0F * u, -12.0F * u, 0.0F);
               this.box(buf, m.method_23760().method_23761(), -4.0F * u, 12.0F * u, -2.0F * u, 4.0F * u, 12.0F * u, 4.0F * u, r, gr, b, alpha);
               m.method_22909();
               m.method_22903();
               m.method_46416(2.0F * u, 12.0F * u, 0.0F);
               m.method_22907(RotationAxis.field_40714.rotation(-swing));
               m.method_46416(-2.0F * u, -12.0F * u, 0.0F);
               this.box(buf, m.method_23760().method_23761(), 0.0F, 12.0F * u, -2.0F * u, 4.0F * u, 12.0F * u, 4.0F * u, r, gr, b, alpha);
               m.method_22909();
               BufferRenderer.method_43433(buf.method_60800());
               m.method_22909();
            }
         }

         RenderSystem.enableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.disableBlend();
      }
   }

   private void box(BufferBuilder b, Matrix4f m, float x, float y, float z, float sx, float sy, float sz, float r, float g, float bl, float a) {
      float x2 = x + sx;
      float y2 = y + sy;
      float z2 = z + sz;
      b.method_22918(m, x, y, z2).method_22915(r, g, bl, a);
      b.method_22918(m, x2, y, z2).method_22915(r, g, bl, a);
      b.method_22918(m, x2, y2, z2).method_22915(r, g, bl, a);
      b.method_22918(m, x, y2, z2).method_22915(r, g, bl, a);
      b.method_22918(m, x2, y, z).method_22915(r, g, bl, a);
      b.method_22918(m, x, y, z).method_22915(r, g, bl, a);
      b.method_22918(m, x, y2, z).method_22915(r, g, bl, a);
      b.method_22918(m, x2, y2, z).method_22915(r, g, bl, a);
      b.method_22918(m, x, y, z).method_22915(r, g, bl, a);
      b.method_22918(m, x, y, z2).method_22915(r, g, bl, a);
      b.method_22918(m, x, y2, z2).method_22915(r, g, bl, a);
      b.method_22918(m, x, y2, z).method_22915(r, g, bl, a);
      b.method_22918(m, x2, y, z2).method_22915(r, g, bl, a);
      b.method_22918(m, x2, y, z).method_22915(r, g, bl, a);
      b.method_22918(m, x2, y2, z).method_22915(r, g, bl, a);
      b.method_22918(m, x2, y2, z2).method_22915(r, g, bl, a);
      b.method_22918(m, x, y2, z2).method_22915(r, g, bl, a);
      b.method_22918(m, x2, y2, z2).method_22915(r, g, bl, a);
      b.method_22918(m, x2, y2, z).method_22915(r, g, bl, a);
      b.method_22918(m, x, y2, z).method_22915(r, g, bl, a);
      b.method_22918(m, x, y, z).method_22915(r, g, bl, a);
      b.method_22918(m, x2, y, z).method_22915(r, g, bl, a);
      b.method_22918(m, x2, y, z2).method_22915(r, g, bl, a);
      b.method_22918(m, x, y, z2).method_22915(r, g, bl, a);
   }

   private float ease(float t) {
      return 1.0F - (float)Math.pow((double)(1.0F - MathHelper.method_15363(t, 0.0F, 1.0F)), 3.0);
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.ghosts.clear();
   }

   private static class Ghost {
      Vec3d pos;
      float yaw;
      boolean sneak;
      float phase;
      long time;

      Ghost(Vec3d pos, float yaw, boolean sneak, float phase, long time) {
         this.pos = pos;
         this.yaw = yaw;
         this.sneak = sneak;
         this.phase = phase;
         this.time = time;
      }
   }
}
