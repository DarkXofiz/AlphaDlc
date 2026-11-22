package zov.alphadlc.module.list.render;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import zov.alphadlc.event.list.EventAttack;
import zov.alphadlc.event.list.EventWorldRender;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.impl.Theme;
import zov.alphadlc.module.settings.impl.ThemeManager;

@ModuleInformation(
   moduleName = "HitBubbles",
   moduleDesc = "\u041a\u0440\u0443\u0433 \u043f\u0440\u0438 \u0443\u0434\u0430\u0440\u0435 \u0438\u0433\u0440\u043e\u043a\u0430",
   moduleCategory = ModuleCategory.RENDER
)
public class HitBubbles extends Module {
   private static final long LIFE_MS = 1600L;
   private final CopyOnWriteArrayList<HitBubbles.HitBubble> bubbles = new CopyOnWriteArrayList<>();
   private final Identifier bubbleTexture = Identifier.method_60655("mre", "images/bubble.png");

   @Override
   public void onDisable() {
      this.bubbles.clear();
      super.onDisable();
   }

   @Subscribe
   public void onAttack(EventAttack event) {
      if (event != null && event.getEntity() != null) {
         if (event.getEntity() instanceof LivingEntity living) {
            if (this.mc.field_1724 != null) {
               Vec3d sideDir = this.getHitSideDirection(living, this.mc.field_1724.method_19538());
               Vec3d pos = this.getHitPosition(living, sideDir);
               float sideYaw = (float)Math.toDegrees(Math.atan2(sideDir.field_1352, sideDir.field_1350));
               this.bubbles.add(new HitBubbles.HitBubble(pos, System.currentTimeMillis(), (float)(Math.random() * 360.0), sideYaw));
            }
         }
      }
   }

   @Subscribe
   public void onWorldRender(EventWorldRender event) {
      long now = System.currentTimeMillis();
      this.bubbles.removeIf(b -> now - b.spawnTime() >= 1600L);
      if (!this.bubbles.isEmpty() && this.mc.field_1724 != null) {
         MatrixStack stack = event.getMatrixStack();
         Vec3d cameraPos = this.mc.field_1773.method_19418().method_19326();
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(770, 1);
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.disableCull();
         RenderSystem.setShader(ShaderProgramKeys.field_53880);
         RenderSystem.setShaderTexture(0, this.bubbleTexture);

         for (HitBubbles.HitBubble bubble : this.bubbles) {
            this.renderSingleBubble(stack, cameraPos, bubble, now);
         }

         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(true);
         RenderSystem.enableCull();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableBlend();
      }
   }

   private void renderSingleBubble(MatrixStack stack, Vec3d cameraPos, HitBubbles.HitBubble bubble, long now) {
      float progress = (float)(now - bubble.spawnTime()) / 1600.0F;
      if (!(progress >= 1.0F)) {
         float inPhase = Math.max(0.0F, Math.min(1.0F, progress / 0.22F));
         float outPhase = Math.max(0.0F, Math.min(1.0F, (progress - 0.225F) / 0.4F));
         float scaleIn = inPhase * inPhase * (3.0F - 2.0F * inPhase);
         float scaleOut = 1.0F - outPhase * outPhase;
         float scale = 0.02F + 1.55F * scaleIn * scaleOut;
         float alpha = 1.0F - outPhase * outPhase * outPhase;
         float rotation = (float)(now - bubble.spawnTime()) / 1.5F + bubble.spinSeed();
         Vec3d rel = bubble.pos().method_1020(cameraPos);
         int color = this.multAlpha(this.getThemeColor(), alpha);
         stack.method_22903();
         stack.method_22904(rel.field_1352, rel.field_1351, rel.field_1350);
         stack.method_22907(RotationAxis.field_40716.rotationDegrees(bubble.sideYaw()));
         stack.method_22907(RotationAxis.field_40714.rotationDegrees(-210.0F));
         stack.method_22907(RotationAxis.field_40718.rotationDegrees(rotation));
         this.drawTexturedQuad(stack, -scale * 0.5F, -scale * 0.5F, scale, scale, color);
         stack.method_22909();
      }
   }

   private void drawTexturedQuad(MatrixStack stack, float x, float y, float width, float height, int color) {
      int r = color >> 16 & 0xFF;
      int g = color >> 8 & 0xFF;
      int b = color & 0xFF;
      int a = color >> 24 & 0xFF;
      if (a > 0) {
         Matrix4f mat = stack.method_23760().method_23761();
         BufferBuilder buffer = Tessellator.method_1348().method_60827(DrawMode.field_27382, VertexFormats.field_1575);
         buffer.method_22918(mat, x, y, 0.0F).method_22913(0.0F, 0.0F).method_1336(r, g, b, a);
         buffer.method_22918(mat, x, y + height, 0.0F).method_22913(0.0F, 1.0F).method_1336(r, g, b, a);
         buffer.method_22918(mat, x + width, y + height, 0.0F).method_22913(1.0F, 1.0F).method_1336(r, g, b, a);
         buffer.method_22918(mat, x + width, y, 0.0F).method_22913(1.0F, 0.0F).method_1336(r, g, b, a);
         BufferRenderer.method_43433(buffer.method_60800());
      }
   }

   private Vec3d getHitSideDirection(LivingEntity target, Vec3d attackerPos) {
      Vec3d dir = attackerPos.method_1020(target.method_19538());
      dir = new Vec3d(dir.field_1352, 0.0, dir.field_1350);
      if (dir.method_1027() < 1.0E-4) {
         Vec3d fallback = target.method_5720();
         dir = new Vec3d(fallback.field_1352, 0.0, fallback.field_1350);
      }

      if (dir.method_1027() < 1.0E-4) {
         dir = new Vec3d(0.0, 0.0, 1.0);
      }

      return dir.method_1029();
   }

   private Vec3d getHitPosition(LivingEntity target, Vec3d sideDir) {
      Vec3d head = new Vec3d(target.method_23317(), target.method_23318() + (double)target.method_17682() + 0.18, target.method_23321());
      return head.method_1019(sideDir.method_1021(0.1));
   }

   private int getThemeColor() {
      Theme theme = ThemeManager.getInstance().getCurrentTheme();
      return theme != null ? theme.color1 : -1;
   }

   private int multAlpha(int color, float alphaMultiplier) {
      int a = color >> 24 & 0xFF;
      int r = color >> 16 & 0xFF;
      int g = color >> 8 & 0xFF;
      int b = color & 0xFF;
      a = (int)((float)a * alphaMultiplier);
      return a << 24 | r << 16 | g << 8 | b;
   }

   private static record HitBubble(Vec3d pos, long spawnTime, float spinSeed, float sideYaw) {
   }
}
