package zov.alphadlc.module.list.render;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.Last;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import zov.alphadlc.AlphaDLC;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.list.combat.KillAura;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.module.settings.ColorSetting;
import zov.alphadlc.module.settings.ModeSetting;
import zov.alphadlc.module.settings.SliderSetting;
import zov.alphadlc.util.render.math.Animation;
import zov.alphadlc.util.render.math.Easing;
import zov.alphadlc.util.render.math.MathUtil;
import zov.alphadlc.util.render.providers.ColorProvider;

@ModuleInformation(
   moduleName = "Target ESP",
   moduleDesc = "\u0412\u0438\u0437\u0443\u0430\u043b\u044c\u043d\u044b\u0439 \u044d\u0444\u0444\u0435\u043a\u0442 \u043d\u0430 \u0442\u0435\u043a\u0443\u0449\u0435\u0439 \u0446\u0435\u043b\u0438",
   moduleCategory = ModuleCategory.RENDER
)
public class TargetESP extends Module {
   private final ModeSetting mode = new ModeSetting(
      "\u0420\u0435\u0436\u0438\u043c", "Crystals", "Crystals", "Crystals2", "Cubes", "Prizraki", "Ghosts2", "\u0414\u0443\u0448\u0438"
   );
   private final BooleanSetting themeColor = new BooleanSetting("\u0426\u0432\u0435\u0442 \u043e\u0442 \u0442\u0435\u043c\u044b", true);
   private final ColorSetting customColor = new ColorSetting("\u0421\u0432\u043e\u0439 \u0446\u0432\u0435\u0442", -9021441)
      .setVisible(() -> !this.themeColor.getValue());
   private final SliderSetting ghostSpeed = new SliderSetting(
         "\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u043f\u0440\u0438\u0437\u0440\u0430\u043a\u043e\u0432", 1.0, 0.5, 2.0, 0.1F
      )
      .setVisible(() -> this.mode.is("Prizraki") || this.mode.is("Ghosts2"));
   private final SliderSetting prizrakiSize = new SliderSetting(
         "\u0420\u0430\u0437\u043c\u0435\u0440 \u043f\u0440\u0438\u0437\u0440\u0430\u043a\u043e\u0432", 0.4F, 0.1F, 1.0, 0.05F
      )
      .setVisible(() -> this.mode.is("Prizraki"));
   private final Animation animation = new Animation(Easing.EXPO_OUT, 500L);
   private Entity lastTarget = null;
   private boolean registered = false;
   private static long lastPrizrakTime = System.currentTimeMillis();
   private final ArrayList<TargetESP.Particle> particles = new ArrayList<>();
   private static final int PARTICLES_PER_SPAWN = 1;
   private static final float SPAWN_INTERVAL = 0.017F;
   private float spawnAccumulator = 0.0F;
   private long lastCubeTime = 0L;
   private final Animation crystalsAnim = new Animation(Easing.EXPO_OUT, 350L);
   private Entity crystalsLastTarget = null;
   private final Last listener = context -> this.onRenderWorldLast(context.matrixStack(), context.camera(), context.tickCounter().method_60637(true));
   private static final int RIDER_MAX_PARTICLES = 3;
   private static final float RIDER_BASE_MUL = 0.05F;
   private static final float RIDER_ALPHA_STEP = 0.005F;
   private final List<TargetESP.RiderParticle> riderParticles = new ArrayList<>();
   private final Animation riderAnim = new Animation(Easing.EXPO_OUT, 300L);
   private float riderMovingAngle = 0.0F;
   private final Animation ghosts2Animation = new Animation(Easing.EXPO_OUT, 500L);
   private LivingEntity lastTargetGhosts2 = null;
   private final Animation dushiAnim = new Animation(Easing.CUBIC_OUT, 300L);
   private LivingEntity dushiLastTarget = null;
   private float dushiPlavnost = 0.0F;

   @Override
   public void onEnable() {
      if (!this.registered) {
         WorldRenderEvents.LAST.register(this.listener);
         this.registered = true;
      }

      super.onEnable();
   }

   private void onRenderWorldLast(MatrixStack matrices, Camera camera, float tickDelta) {
      if (this.isEnabled()) {
         Entity target = AlphaDLC.getInstance().getModuleStorage().get(KillAura.class).getTarget();
         if (target != null && target != this.mc.field_1724 && !(target instanceof ArmorStandEntity)) {
            this.lastTarget = target;
            this.animation.run(1.0F);
         } else {
            this.animation.run(0.0F);
            if (this.animation.getValue() == 0.0F) {
               this.lastTarget = null;
            }
         }

         if (target != null && this.lastTarget != null) {
            String var9 = this.mode.getValue();
            switch (var9) {
               case "Crystals":
                  this.crystalsLastTarget = this.lastTarget;
                  this.crystalsAnim.run(1.0F);
                  float animx = this.crystalsAnim.getValue();
                  if (animx > 0.001F) {
                     this.renderCrystals(matrices, camera, (LivingEntity)this.crystalsLastTarget, false, animx, tickDelta);
                  }
                  break;
               case "Crystals2":
                  this.crystalsLastTarget = this.lastTarget;
                  this.crystalsAnim.run(1.0F);
                  float anim = this.crystalsAnim.getValue();
                  if (anim > 0.001F) {
                     this.renderCrystals(matrices, camera, (LivingEntity)this.crystalsLastTarget, true, anim, tickDelta);
                  }
                  break;
               case "Cubes":
                  this.renderCubes(matrices, camera, (LivingEntity)this.lastTarget, tickDelta);
                  break;
               case "Prizraki":
                  this.renderPrizraki(matrices, camera, (LivingEntity)this.lastTarget, tickDelta);
                  break;
               case "GhostRider":
                  this.renderGhostRider(matrices, camera, (LivingEntity)this.lastTarget, tickDelta);
                  break;
               case "Ghosts2":
                  this.drawGhosts2(matrices, tickDelta);
                  break;
               case "\u0414\u0443\u0448\u0438":
                  this.dushiLastTarget = (LivingEntity)this.lastTarget;
                  this.dushiAnim.run(1.0F);
                  if (this.dushiAnim.getValue() > 0.001F) {
                     this.renderDushi(matrices, camera, (LivingEntity)this.lastTarget, tickDelta);
                  }
            }
         } else {
            this.lastCubeTime = 0L;
            this.spawnAccumulator = 0.0F;
            this.riderParticles.clear();
            if (this.mode.is("Ghosts2")) {
               this.drawGhosts2(matrices, tickDelta);
            }

            if (this.mode.is("\u0414\u0443\u0448\u0438")) {
               this.dushiAnim.run(0.0F);
               if (this.dushiAnim.getValue() > 0.001F && this.dushiLastTarget != null && this.dushiLastTarget.method_5805()) {
                  this.renderDushi(matrices, camera, this.dushiLastTarget, tickDelta);
               } else if (this.dushiAnim.getValue() <= 0.001F) {
                  this.dushiLastTarget = null;
               }
            }

            if (this.mode.is("Crystals") || this.mode.is("Crystals2")) {
               this.crystalsAnim.run(0.0F);
               float animx = this.crystalsAnim.getValue();
               if (animx > 0.001F && this.crystalsLastTarget != null) {
                  this.renderCrystals(matrices, camera, (LivingEntity)this.crystalsLastTarget, this.mode.is("Crystals2"), animx, tickDelta);
               } else if (animx <= 0.001F) {
                  this.crystalsLastTarget = null;
               }
            }

            if (this.mode.is("Cubes") && !this.particles.isEmpty()) {
               this.renderCubes(matrices, camera, null, tickDelta);
            }
         }
      }
   }

   private void renderPrizraki(MatrixStack matrices, Camera camera, LivingEntity target, float partialTicks) {
      Vec3d camPos = camera.method_19326();
      float camYaw = camera.method_19330();
      float camPitch = camera.method_19329();
      double tx = MathHelper.method_16436((double)partialTicks, target.field_6038, target.method_23317());
      double ty = MathHelper.method_16436((double)partialTicks, target.field_5971, target.method_23318()) + 0.38 + (double)target.method_17682() / 2.0;
      double tz = MathHelper.method_16436((double)partialTicks, target.field_5989, target.method_23321());
      double rx = tx - camPos.field_1352 + 0.2;
      double ry = ty - camPos.field_1351;
      double rz = tz - camPos.field_1350;
      double radius = 0.4 + (double)target.method_17681() / 2.0;
      float speed = 30.0F / this.ghostSpeed.getFloatValue();
      float size = this.prizrakiSize.getFloatValue();
      double distance = 6.0;
      int length = 34;
      long now = System.currentTimeMillis();
      int colorInt = this.getRenderColor();
      float r = (float)ColorProvider.red(colorInt) / 255.0F;
      float g = (float)ColorProvider.green(colorInt) / 255.0F;
      float b = (float)ColorProvider.blue(colorInt) / 255.0F;
      RenderSystem.setShader(ShaderProgramKeys.field_53880);
      RenderSystem.setShaderTexture(0, this.spriteTexture());
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
      RenderSystem.disableCull();
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      BufferBuilder buffer = Tessellator.method_1348().method_60827(DrawMode.field_27382, VertexFormats.field_1575);

      for (int trail = 0; trail < 3; trail++) {
         for (int i = 0; i < length; i++) {
            double angle = 0.05F * ((double)(now - lastPrizrakTime) - (double)i * distance) / (double)speed;
            double s = Math.sin(angle * Math.PI) * radius;
            double c = Math.cos(angle * Math.PI) * radius;
            double o = trail == 0 ? Math.cos(angle * Math.PI) * radius : Math.sin(angle * Math.PI) * radius;
            float t = (float)i / (float)(length - 1);
            float curSize = size * (1.0F - t * 0.5F);
            float alpha = 1.0F - t * 0.9F;
            double px = rx + (trail == 1 ? -s : s);
            double py = ry + o;
            double pz = rz + (trail == 2 ? c : -c);
            float cr = Math.min(1.0F, r * 1.5F);
            float cg = Math.min(1.0F, g * 1.5F);
            float cb = Math.min(1.0F, b * 1.5F);
            this.putBloomQuad(buffer, matrices, px, py, pz, curSize * 0.6F, r, g, b, alpha * 0.15F, camYaw, camPitch);
            this.putBloomQuad(buffer, matrices, px, py, pz, curSize * 0.35F, r, g, b, alpha * 0.35F, camYaw, camPitch);
            this.putBloomQuad(buffer, matrices, px, py, pz, curSize * 0.15F, cr, cg, cb, alpha, camYaw, camPitch);
         }
      }

      BufferRenderer.method_43433(buffer.method_60800());
      this.restoreRenderState();
   }

   private void renderCrystals(MatrixStack matrices, Camera camera, LivingEntity target, boolean sharp, float anim, float partialTicks) {
      if (target != null && this.mc.field_1724 != null) {
         float eased = easeOutCubic(anim);
         float time = ((float)this.mc.field_1724.field_6012 + partialTicks) * 6.0F;
         Vec3d camPos = camera.method_19326();
         double tx = MathHelper.method_16436((double)partialTicks, target.field_6038, target.method_23317());
         double ty = MathHelper.method_16436((double)partialTicks, target.field_5971, target.method_23318());
         double tz = MathHelper.method_16436((double)partialTicks, target.field_5989, target.method_23321());
         float camYaw = camera.method_19330();
         float camPitch = camera.method_19329();
         float entityHeight = target.method_17682();
         float halfWidth = target.method_17681() * 0.5F;
         int baseColor = this.getRenderColor();
         float r = Math.min(1.0F, (float)ColorProvider.red(baseColor) / 255.0F * 1.3F);
         float g = Math.min(1.0F, (float)ColorProvider.green(baseColor) / 255.0F * 1.3F);
         float b = Math.min(1.0F, (float)ColorProvider.blue(baseColor) / 255.0F * 1.3F);
         matrices.method_22903();
         matrices.method_22904(tx - camPos.field_1352, ty - camPos.field_1351, tz - camPos.field_1350);
         int crystalCount = 18;
         float pelvisY = entityHeight * 0.35F;
         float torsoY = entityHeight * 0.55F;
         float neckY = entityHeight * 0.74F;
         RenderSystem.setShader(ShaderProgramKeys.field_53876);
         RenderSystem.enableBlend();
         RenderSystem.disableCull();
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA);
         BufferBuilder buf = Tessellator.method_1348().method_60827(DrawMode.field_27379, VertexFormats.field_1576);
         boolean hasCrystals = false;

         for (int i = 0; i < crystalCount; i++) {
            float s1 = (float)Math.sin((double)((float)i * 1.7F + 0.3F)) * 0.5F + 0.5F;
            float s2 = (float)Math.cos((double)((float)i * 2.3F + 0.7F)) * 0.5F + 0.5F;
            float s3 = (float)Math.sin((double)((float)i * 3.1F + 1.1F)) * 0.5F + 0.5F;
            float angle = time + (float)i * (360.0F / (float)crystalCount) + s1 * 12.0F;
            float radius = halfWidth + 0.25F + s3 * 0.15F;
            float cx = radius * (float)Math.cos(Math.toRadians((double)angle));
            float cz = radius * (float)Math.sin(Math.toRadians((double)angle));
            float cy = s2 * entityHeight;
            float scale = 0.18F * eased;
            if (!(scale < 0.001F)) {
               float lookY = this.getCrystalLookY(cy, entityHeight, pelvisY, torsoY, neckY);
               float dx = -cx;
               float dy = lookY - cy;
               float dz = -cz;
               float yaw = (float)Math.toDegrees(Math.atan2((double)dz, (double)dx));
               float pitch = (float)Math.toDegrees(Math.atan2((double)dy, (double)((float)Math.sqrt((double)(dx * dx + dz * dz)))));
               this.drawCrystalShape(
                  buf, matrices, cx, cy, cz, scale, yaw, pitch, (int)(r * 255.0F), (int)(g * 255.0F), (int)(b * 255.0F), (int)(200.0F * anim), sharp
               );
               hasCrystals = true;
            }
         }

         if (hasCrystals) {
            BufferRenderer.method_43433(buf.method_60800());
         }

         RenderSystem.setShader(ShaderProgramKeys.field_53880);
         RenderSystem.setShaderTexture(0, this.spriteTexture());
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
         BufferBuilder glowBuf = Tessellator.method_1348().method_60827(DrawMode.field_27382, VertexFormats.field_1575);
         boolean hasGlow = false;

         for (int ix = 0; ix < crystalCount; ix++) {
            float s1 = (float)Math.sin((double)((float)ix * 1.7F + 0.3F)) * 0.5F + 0.5F;
            float s2 = (float)Math.cos((double)((float)ix * 2.3F + 0.7F)) * 0.5F + 0.5F;
            float s3 = (float)Math.sin((double)((float)ix * 3.1F + 1.1F)) * 0.5F + 0.5F;
            float angle = time + (float)ix * (360.0F / (float)crystalCount) + s1 * 12.0F;
            float radius = halfWidth + 0.25F + s3 * 0.15F;
            float cx = radius * (float)Math.cos(Math.toRadians((double)angle));
            float cz = radius * (float)Math.sin(Math.toRadians((double)angle));
            float cy = s2 * entityHeight;
            float scale = 0.18F * eased;
            if (anim * 0.15F > 0.001F && scale > 1.0E-4F) {
               this.putBloomQuad(glowBuf, matrices, (double)cx, (double)cy, (double)cz, scale * 5.5F, r, g, b, anim * 0.15F, camYaw, camPitch);
               this.putBloomQuad(glowBuf, matrices, (double)cx, (double)cy, (double)cz, scale * 3.5F, r, g, b, anim * 0.25F, camYaw, camPitch);
               hasGlow = true;
            }
         }

         if (hasGlow) {
            BufferRenderer.method_43433(glowBuf.method_60800());
         }

         matrices.method_22909();
         RenderSystem.depthMask(true);
         RenderSystem.enableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableBlend();
      }
   }

   private float getCrystalLookY(float cy, float h, float pelvis, float torso, float neck) {
      float n = cy / h;
      if (n < 0.33F) {
         return pelvis;
      } else {
         return n < 0.6F ? torso : neck;
      }
   }

   private void drawCrystalShape(
      BufferBuilder buf, MatrixStack ms, float x, float y, float z, float scale, float yaw, float pitch, int r, int g, int b, int a, boolean sharp
   ) {
      ms.method_22903();
      ms.method_46416(x, y, z);
      ms.method_22907(RotationAxis.field_40716.rotationDegrees(-yaw));
      ms.method_22907(RotationAxis.field_40718.rotationDegrees(pitch));
      ms.method_22905(scale, scale, scale);
      Matrix4f m = ms.method_23760().method_23761();
      float w = sharp ? 0.35F : 0.7F;
      float h = sharp ? 1.2F : 1.0F;
      this.tri(buf, m, h, 0.0F, 0.0F, 0.0F, w, 0.0F, 0.0F, 0.0F, w, r, g, b, a);
      this.tri(buf, m, h, 0.0F, 0.0F, 0.0F, 0.0F, w, 0.0F, -w, 0.0F, r, g, b, a);
      this.tri(buf, m, h, 0.0F, 0.0F, 0.0F, -w, 0.0F, 0.0F, 0.0F, -w, r, g, b, a);
      this.tri(buf, m, h, 0.0F, 0.0F, 0.0F, 0.0F, -w, 0.0F, w, 0.0F, r, g, b, a);
      this.tri(buf, m, -h, 0.0F, 0.0F, 0.0F, w, 0.0F, 0.0F, 0.0F, w, r, g, b, a);
      this.tri(buf, m, -h, 0.0F, 0.0F, 0.0F, 0.0F, w, 0.0F, -w, 0.0F, r, g, b, a);
      this.tri(buf, m, -h, 0.0F, 0.0F, 0.0F, -w, 0.0F, 0.0F, 0.0F, -w, r, g, b, a);
      this.tri(buf, m, -h, 0.0F, 0.0F, 0.0F, 0.0F, -w, 0.0F, w, 0.0F, r, g, b, a);
      ms.method_22909();
   }

   private void tri(
      BufferBuilder buf, Matrix4f m, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, int r, int g, int b, int a
   ) {
      buf.method_22918(m, x1, y1, z1).method_1336(r, g, b, a);
      buf.method_22918(m, x2, y2, z2).method_1336(r, g, b, a);
      buf.method_22918(m, x3, y3, z3).method_1336(r, g, b, a);
   }

   private void renderCubes(MatrixStack matrices, Camera camera, LivingEntity target, float partialTicks) {
      long now = System.currentTimeMillis();
      if (this.lastCubeTime == 0L) {
         this.lastCubeTime = now;
      }

      float dt = Math.min((float)(now - this.lastCubeTime) / 1000.0F, 0.1F);
      this.lastCubeTime = now;
      if (target != null) {
         this.spawnAccumulator += dt;

         while (this.spawnAccumulator >= 0.017F) {
            this.spawnAccumulator -= 0.017F;

            for (int i = 0; i < 1; i++) {
               double rand = (double)MathUtil.random(0.0F, 360.0F);
               double px = Math.cos(Math.toRadians(rand)) * 0.7;
               double py = MathUtil.random(0.04, 0.2);
               double pz = Math.sin(Math.toRadians(rand)) * 0.7;
               this.particles.add(new TargetESP.Particle(target, px, py, pz));
            }
         }
      }

      float camYaw = camera.method_19330();
      float camPitch = camera.method_19329();
      Iterator<TargetESP.Particle> it = this.particles.iterator();
      ArrayList<TargetESP.Particle> toRender = new ArrayList<>();

      while (it.hasNext()) {
         TargetESP.Particle p = it.next();
         p.update(dt);
         if (now - p.time > 1000L) {
            it.remove();
         } else {
            toRender.add(p);
         }
      }

      int color = this.getRenderColor();

      for (TargetESP.Particle p : toRender) {
         p.renderCube(matrices, camera, color, partialTicks);
      }

      if (!toRender.isEmpty()) {
         RenderSystem.setShader(ShaderProgramKeys.field_53880);
         RenderSystem.setShaderTexture(0, this.spriteTexture());
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
         RenderSystem.disableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(false);
         BufferBuilder builder = Tessellator.method_1348().method_60827(DrawMode.field_27382, VertexFormats.field_1575);
         boolean hasBloom = false;

         for (TargetESP.Particle p : toRender) {
            if (p.renderBloom(builder, matrices, camera, color, camYaw, camPitch, partialTicks)) {
               hasBloom = true;
            }
         }

         if (hasBloom) {
            BufferRenderer.method_43433(builder.method_60800());
         }

         RenderSystem.depthMask(true);
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableBlend();
         RenderSystem.enableCull();
      }
   }

   private void renderGhostRider(MatrixStack matrices, Camera camera, LivingEntity target, float partialTicks) {
      while (this.riderParticles.size() < 3) {
         this.riderParticles
            .add(
               new TargetESP.RiderParticle(new Vec3d(target.method_23317(), target.method_23318() + (double)target.method_17682() / 2.0, target.method_23321()))
            );
      }

      float fps = (float)Math.max(this.mc.method_47599(), 5);
      float fpsFactor = 500.0F / fps;
      this.riderMovingAngle = (this.riderMovingAngle + 20.0F * fpsFactor / 55.0F) % 36000.0F;
      this.riderAnim.run(target.field_6235 > 7 ? 1.0F : 0.0F);
      Vec3d camPos = camera.method_19326();
      float camYaw = camera.method_19330();
      float camPitch = camera.method_19329();
      double tx = MathHelper.method_16436((double)partialTicks, target.field_6038, target.method_23317());
      double ty = MathHelper.method_16436((double)partialTicks, target.field_5971, target.method_23318());
      double tz = MathHelper.method_16436((double)partialTicks, target.field_5989, target.method_23321());
      int colorInt = this.getRenderColor();
      float r = (float)ColorProvider.red(colorInt) / 255.0F;
      float g = (float)ColorProvider.green(colorInt) / 255.0F;
      float b = (float)ColorProvider.blue(colorInt) / 255.0F;
      RenderSystem.setShader(ShaderProgramKeys.field_53880);
      RenderSystem.setShaderTexture(0, this.spriteTexture());
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
      RenderSystem.disableCull();
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      BufferBuilder builder = Tessellator.method_1348().method_60827(DrawMode.field_27382, VertexFormats.field_1575);
      List<TargetESP.RiderParticle> toRemove = new ArrayList<>();
      float mul = 0.05F * fpsFactor;

      for (int i = 0; i < this.riderParticles.size(); i++) {
         TargetESP.RiderParticle p = this.riderParticles.get(i);
         float angleOffset = (float)i * 120.0F;
         float currentAngle = this.riderMovingAngle + angleOffset;
         double rad = Math.toRadians((double)currentAngle);
         float collapseAnim = this.riderAnim.getValue();
         float orbitRadius = 0.6F * (1.0F - collapseAnim);
         double targetX = tx + Math.sin(rad) * (double)orbitRadius;
         double targetY = ty + 0.2 + (double)target.method_17682() / 2.0 * Math.sin(Math.toRadians((double)(this.riderMovingAngle / ((float)i + 1.0F))));
         double targetZ = tz + Math.cos(rad) * (double)orbitRadius;
         double mx = (targetX - p.pos.field_1352) * (double)mul;
         double my = (targetY - p.pos.field_1351) * (double)mul;
         double mz = (targetZ - p.pos.field_1350) * (double)mul;
         p.pos = p.pos.method_1031(mx, my, mz);
         p.alpha = Math.max(0.0F, p.alpha - 0.005F * fpsFactor);
         if (p.alpha <= 0.0F) {
            toRemove.add(p);
         } else {
            double px = p.pos.field_1352 - camPos.field_1352;
            double py = p.pos.field_1351 - camPos.field_1351;
            double pz = p.pos.field_1350 - camPos.field_1350;
            float cr = Math.min(1.0F, r * 1.5F);
            float cg = Math.min(1.0F, g * 1.5F);
            float cb = Math.min(1.0F, b * 1.5F);
            this.putBloomQuad(builder, matrices, px, py, pz, 0.6F, r, g, b, p.alpha * 0.15F, camYaw, camPitch);
            this.putBloomQuad(builder, matrices, px, py, pz, 0.35F, r, g, b, p.alpha * 0.35F, camYaw, camPitch);
            this.putBloomQuad(builder, matrices, px, py, pz, 0.15F, cr, cg, cb, p.alpha, camYaw, camPitch);
         }
      }

      this.riderParticles.removeAll(toRemove);
      if (!this.riderParticles.isEmpty()) {
         BufferRenderer.method_43433(builder.method_60800());
      } else {
         builder.method_60800();
      }

      this.restoreRenderState();
   }

   private void drawGhosts2(MatrixStack matrices, float tickDelta) {
      Camera camera = this.mc.field_1773.method_19418();
      if (camera != null) {
         Vec3d camPos = camera.method_19326();
         LivingEntity target = null;
         KillAura killAura = AlphaDLC.getInstance().getModuleStorage().get(KillAura.class);
         if (killAura != null && killAura.isEnabled()) {
            target = killAura.getTarget();
         }

         if (target != null) {
            this.lastTargetGhosts2 = target;
            this.ghosts2Animation.run(1.0F);
         } else {
            this.ghosts2Animation.run(0.0F);
            if (this.ghosts2Animation.getValue() == 0.0F) {
               this.lastTargetGhosts2 = null;
            }
         }

         if (this.lastTargetGhosts2 != null) {
            if (!((double)this.ghosts2Animation.getValue() <= 0.01)) {
               float alphaAnim = this.ghosts2Animation.getValue();
               float easing = 1.0F - (float)Math.pow((double)(1.0F - alphaAnim), 3.0);
               this.renderGhosts2(matrices, camera, camPos, tickDelta, this.lastTargetGhosts2, easing);
            }
         }
      }
   }

   private void renderGhosts2(MatrixStack matrices, Camera camera, Vec3d camPos, float tickDelta, Entity entity, float alpha) {
      double x = MathHelper.method_16436((double)tickDelta, entity.field_6038, entity.method_23317());
      double y = MathHelper.method_16436((double)tickDelta, entity.field_5971, entity.method_23318()) + (double)entity.method_17682() / 2.0;
      double z = MathHelper.method_16436((double)tickDelta, entity.field_5989, entity.method_23321());
      double t = (double)System.currentTimeMillis() * 0.001 * (double)this.ghostSpeed.getFloatValue();
      double h = (double)entity.method_17682();
      double[] oy = new double[]{-h * 0.35 + 0.15, 0.0, h * 0.45 - 0.15};
      double[] r = new double[]{0.62, 0.58, 0.56};
      double[] phase = new double[]{0.0, 0.33, 0.66};
      float baseScale = (float)(0.16F * Math.max(0.2, (double)alpha));
      int segments = 47;
      double tailSpan = Math.PI * 3.0 / 5.0;
      double headCut = 1.0 - (double)alpha;
      double headFadeWidth = 0.002;
      int colorTheme = this.getRenderColor();
      RenderSystem.depthMask(false);
      RenderSystem.disableCull();
      RenderSystem.enableBlend();

      for (int i = 0; i < 3; i++) {
         double headAng = (t + phase[i]) * Math.PI * 2.0;

         for (int j = 0; j < segments; j++) {
            double s = (double)j / (double)(segments - 1);
            if (!(s < headCut - headFadeWidth)) {
               double headFade = MathHelper.method_15350((s - (headCut - headFadeWidth)) / headFadeWidth, 0.0, 1.0);
               double ease = 1.0 - s;
               double ang = headAng - s * tailSpan;
               double rr = r[i] * (1.0 - 0.06 * s) + 0.1 * Math.sin(ang * 1.45 + (double)i);
               double dx = Math.cos(ang) * rr;
               double dz = Math.sin(ang) * rr;
               double dy = oy[i] + 0.12 * Math.sin(ang * 1.25 + (double)i * 0.6);
               float headSize = i == 1 ? 1.1F : 1.0F;
               float tailSize = 0.002F;
               float size = (float)((double)baseScale * ((double)tailSize + (double)(headSize - tailSize) * Math.pow(ease, 0.6)));
               float whiteAlpha = Math.min(1.0F, 1.05F * (float)((double)alpha * Math.pow(ease, 0.9) * headFade));
               float colorAlpha = Math.min(0.55F, 1.1F * (float)((double)alpha * Math.pow(ease, 1.1) * headFade));
               if (!(size < 1.0E-4F) || !(whiteAlpha < 0.001F) || !(colorAlpha < 0.001F)) {
                  float colorHaloSize = size * 1.34F;
                  float whiteHaloSize = size * 0.8F;
                  int whiteA = Math.max(0, Math.min(255, (int)(whiteAlpha * 255.0F)));
                  int colA = Math.max(0, Math.min(255, (int)(colorAlpha * 255.0F)));
                  int cr = ColorProvider.red(colorTheme);
                  int cg = ColorProvider.green(colorTheme);
                  int cb = ColorProvider.blue(colorTheme);
                  matrices.method_22903();
                  matrices.method_22904(x - camPos.field_1352 + dx, y - camPos.field_1351 + dy, z - camPos.field_1350 + dz);
                  matrices.method_22907(RotationAxis.field_40716.rotationDegrees(-camera.method_19330()));
                  matrices.method_22907(RotationAxis.field_40714.rotationDegrees(camera.method_19329()));
                  matrices.method_22905(-1.0F, -1.0F, 1.0F);
                  Matrix4f m = matrices.method_23760().method_23761();
                  RenderSystem.disableDepthTest();
                  RenderSystem.enableBlend();
                  RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
                  RenderSystem.setShader(ShaderProgramKeys.field_53880);
                  RenderSystem.setShaderTexture(0, this.spriteTexture());
                  BufferBuilder buf = Tessellator.method_1348().method_60827(DrawMode.field_27382, VertexFormats.field_1575);
                  buf.method_22918(m, -whiteHaloSize, -whiteHaloSize, 0.0F).method_22913(0.0F, 1.0F).method_1336(255, 255, 255, whiteA);
                  buf.method_22918(m, whiteHaloSize, -whiteHaloSize, 0.0F).method_22913(1.0F, 1.0F).method_1336(255, 255, 255, whiteA);
                  buf.method_22918(m, whiteHaloSize, whiteHaloSize, 0.0F).method_22913(1.0F, 0.0F).method_1336(255, 255, 255, whiteA);
                  buf.method_22918(m, -whiteHaloSize, whiteHaloSize, 0.0F).method_22913(0.0F, 0.0F).method_1336(255, 255, 255, whiteA);
                  BufferRenderer.method_43433(buf.method_60800());
                  buf = Tessellator.method_1348().method_60827(DrawMode.field_27382, VertexFormats.field_1575);
                  buf.method_22918(m, -colorHaloSize, -colorHaloSize, 0.0F).method_22913(0.0F, 1.0F).method_1336(cr, cg, cb, colA);
                  buf.method_22918(m, colorHaloSize, -colorHaloSize, 0.0F).method_22913(1.0F, 1.0F).method_1336(cr, cg, cb, colA);
                  buf.method_22918(m, colorHaloSize, colorHaloSize, 0.0F).method_22913(1.0F, 0.0F).method_1336(cr, cg, cb, colA);
                  buf.method_22918(m, -colorHaloSize, colorHaloSize, 0.0F).method_22913(0.0F, 0.0F).method_1336(cr, cg, cb, colA);
                  BufferRenderer.method_43433(buf.method_60800());
                  RenderSystem.defaultBlendFunc();
                  RenderSystem.disableBlend();
                  RenderSystem.enableDepthTest();
                  matrices.method_22909();
               }
            }
         }
      }

      RenderSystem.depthMask(true);
      RenderSystem.defaultBlendFunc();
      RenderSystem.enableCull();
   }

   private void renderDushi(MatrixStack matrices, Camera camera, LivingEntity target, float tickDelta) {
      Vec3d camPos = camera.method_19326();
      double tx = MathHelper.method_16436((double)tickDelta, target.field_6038, target.method_23317());
      double ty = MathHelper.method_16436((double)tickDelta, target.field_5971, target.method_23318());
      double tz = MathHelper.method_16436((double)tickDelta, target.field_5989, target.method_23321());
      double x = tx - camPos.field_1352;
      double y = ty - camPos.field_1351;
      double z = tz - camPos.field_1350;
      float width = target.method_17681() * 1.5F;
      int color = this.getRenderColor();
      float animVal = this.dushiAnim.getValue();
      float alpha = animVal;
      this.dushiPlavnost = (float)(System.currentTimeMillis() % 100000L) / 1.5F;
      int r = ColorProvider.red(color);
      int g = ColorProvider.green(color);
      int b = ColorProvider.blue(color);
      RenderSystem.setShader(ShaderProgramKeys.field_53880);
      RenderSystem.setShaderTexture(0, this.spriteTexture());
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
      RenderSystem.disableCull();
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      BufferBuilder builder = Tessellator.method_1348().method_60827(DrawMode.field_27382, VertexFormats.field_1575);
      int step = 2;
      int wormTick = 0;
      int wormCD = 0;

      for (int i = 0; i < 360; i += step) {
         float size = 0.13F + 0.005F * (float)wormTick;
         float bigSize = 0.7F + 0.005F * (float)wormTick;
         if (wormCD > 0) {
            wormCD -= step;
         } else if ((wormTick += step) > 50) {
            wormCD = 100;
            wormTick = 0;
         } else {
            float val = Math.max(0.5F, 1.2F - 0.5F * animVal);
            float angleRad = (float)Math.toRadians((double)((float)i + this.dushiPlavnost));
            float sin = (float)(Math.sin((double)angleRad) * (double)width * (double)val);
            float cos = (float)(Math.cos((double)angleRad) * (double)width * (double)val);
            float waveY = (float)Math.sin(Math.toRadians((double)((float)i / 2.0F + this.dushiPlavnost / 5.0F)));
            matrices.method_22903();
            matrices.method_22904(x + (double)sin, y + (double)(target.method_17682() / 1.5F) + (double)(target.method_17682() / 3.0F * waveY), z + (double)cos);
            matrices.method_22907(camera.method_23767());
            Matrix4f mat = matrices.method_23760().method_23761();
            float bigA = alpha * 0.05F;
            int bigAlpha = Math.min(255, Math.max(0, (int)(bigA * 255.0F)));
            builder.method_22918(mat, -bigSize / 2.0F, bigSize / 2.0F, 0.0F).method_22913(0.0F, 1.0F).method_1336(r, g, b, bigAlpha);
            builder.method_22918(mat, bigSize / 2.0F, bigSize / 2.0F, 0.0F).method_22913(1.0F, 1.0F).method_1336(r, g, b, bigAlpha);
            builder.method_22918(mat, bigSize / 2.0F, -bigSize / 2.0F, 0.0F).method_22913(1.0F, 0.0F).method_1336(r, g, b, bigAlpha);
            builder.method_22918(mat, -bigSize / 2.0F, -bigSize / 2.0F, 0.0F).method_22913(0.0F, 0.0F).method_1336(r, g, b, bigAlpha);
            int coreAlpha = Math.min(255, Math.max(0, (int)(alpha * 255.0F)));
            builder.method_22918(mat, -size / 2.0F, size / 2.0F, 0.0F).method_22913(0.0F, 1.0F).method_1336(r, g, b, coreAlpha);
            builder.method_22918(mat, size / 2.0F, size / 2.0F, 0.0F).method_22913(1.0F, 1.0F).method_1336(r, g, b, coreAlpha);
            builder.method_22918(mat, size / 2.0F, -size / 2.0F, 0.0F).method_22913(1.0F, 0.0F).method_1336(r, g, b, coreAlpha);
            builder.method_22918(mat, -size / 2.0F, -size / 2.0F, 0.0F).method_22913(0.0F, 0.0F).method_1336(r, g, b, coreAlpha);
            matrices.method_22909();
         }
      }

      BufferRenderer.method_43433(builder.method_60800());
      this.restoreRenderState();
   }

   private void putBloomQuad(
      BufferBuilder builder, MatrixStack ms, double x, double y, double z, float scale, float r, float g, float b, float a, float camYaw, float camPitch
   ) {
      if (!(a <= 0.001F) && !(scale <= 1.0E-4F)) {
         ms.method_22903();
         ms.method_22904(x, y, z);
         ms.method_22907(RotationAxis.field_40716.rotationDegrees(-camYaw));
         ms.method_22907(RotationAxis.field_40714.rotationDegrees(camPitch));
         ms.method_22905(scale, scale, scale);
         Matrix4f m = ms.method_23760().method_23761();
         int ri = (int)(r * 255.0F);
         int gi = (int)(g * 255.0F);
         int bi = (int)(b * 255.0F);
         int ai = (int)(a * 255.0F);
         builder.method_22918(m, -0.5F, 0.5F, 0.0F).method_22913(0.0F, 1.0F).method_1336(ri, gi, bi, ai);
         builder.method_22918(m, 0.5F, 0.5F, 0.0F).method_22913(1.0F, 1.0F).method_1336(ri, gi, bi, ai);
         builder.method_22918(m, 0.5F, -0.5F, 0.0F).method_22913(1.0F, 0.0F).method_1336(ri, gi, bi, ai);
         builder.method_22918(m, -0.5F, -0.5F, 0.0F).method_22913(0.0F, 0.0F).method_1336(ri, gi, bi, ai);
         ms.method_22909();
      }
   }

   private static float easeOutCubic(float t) {
      float u = 1.0F - t;
      return 1.0F - u * u * u;
   }

   private void restoreRenderState() {
      RenderSystem.enableDepthTest();
      RenderSystem.depthMask(true);
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableBlend();
      RenderSystem.enableCull();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   private Identifier spriteTexture() {
      return Identifier.method_60655("mre", "images/bloom.png");
   }

   private int getRenderColor() {
      return this.themeColor.getValue() ? ColorProvider.getColorClient() : this.customColor.getValue();
   }

   public static class Particle {
      double x;
      double y;
      double z;
      long time;
      LivingEntity entity;

      public Particle(LivingEntity entity, double x, double y, double z) {
         this.entity = entity;
         this.x = x;
         this.y = y;
         this.z = z;
         this.time = System.currentTimeMillis();
      }

      public void update(float dt) {
         this.y = this.y + MathUtil.random(0.01, 0.04) * (double)(dt * 60.0F);
      }

      public void renderCube(MatrixStack ms, Camera camera, int colorInt, float partialTicks) {
         if (this.entity != null) {
            double alive = (double)(System.currentTimeMillis() - this.time);
            float life = Math.min(1.0F, (float)alive / 1000.0F);
            float alpha = life > 0.8F ? 1.0F - (life - 0.8F) * 5.0F : (alive < 200.0 ? (float)alive / 200.0F : 1.0F);
            if (!(alpha <= 0.001F)) {
               Vec3d cam = camera.method_19326();
               double ex = MathHelper.method_16436((double)partialTicks, this.entity.field_6038, this.entity.method_23317());
               double ey = MathHelper.method_16436((double)partialTicks, this.entity.field_5971, this.entity.method_23318());
               double ez = MathHelper.method_16436((double)partialTicks, this.entity.field_5989, this.entity.method_23321());
               float scale = 0.12F;
               int color = ColorProvider.setAlpha(colorInt, (int)(alpha * 255.0F));
               ms.method_22903();
               ms.method_22904(ex - cam.field_1352 + this.x, ey - cam.field_1351 + this.y, ez - cam.field_1350 + this.z);
               double rot = alive / 10.0;
               ms.method_22907(RotationAxis.field_40714.rotationDegrees((float)rot));
               ms.method_22907(RotationAxis.field_40716.rotationDegrees((float)rot));
               ms.method_22907(RotationAxis.field_40718.rotationDegrees((float)rot));
               ms.method_22905(scale, scale, scale);
               RenderSystem.setShader(ShaderProgramKeys.field_53876);
               RenderSystem.enableBlend();
               RenderSystem.enableDepthTest();
               RenderSystem.disableCull();
               RenderSystem.depthMask(false);
               RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
               this.drawBox(ms, color);
               RenderSystem.depthMask(true);
               RenderSystem.disableBlend();
               RenderSystem.enableCull();
               ms.method_22909();
            }
         }
      }

      public boolean renderBloom(BufferBuilder builder, MatrixStack ms, Camera camera, int colorInt, float camYaw, float camPitch, float partialTicks) {
         if (this.entity == null) {
            return false;
         } else {
            double alive = (double)(System.currentTimeMillis() - this.time);
            float life = Math.min(1.0F, (float)alive / 1000.0F);
            float alpha = life > 0.8F ? 1.0F - (life - 0.8F) * 5.0F : (alive < 200.0 ? (float)alive / 200.0F : 1.0F);
            if (alpha <= 0.001F) {
               return false;
            } else {
               float r = (float)ColorProvider.red(colorInt) / 255.0F;
               float g = (float)ColorProvider.green(colorInt) / 255.0F;
               float b = (float)ColorProvider.blue(colorInt) / 255.0F;
               Vec3d cam = camera.method_19326();
               double ex = MathHelper.method_16436((double)partialTicks, this.entity.field_6038, this.entity.method_23317());
               double ey = MathHelper.method_16436((double)partialTicks, this.entity.field_5971, this.entity.method_23318());
               double ez = MathHelper.method_16436((double)partialTicks, this.entity.field_5989, this.entity.method_23321());
               ms.method_22903();
               ms.method_22904(ex - cam.field_1352 + this.x, ey - cam.field_1351 + this.y, ez - cam.field_1350 + this.z);
               ms.method_22907(RotationAxis.field_40716.rotationDegrees(-camYaw));
               ms.method_22907(RotationAxis.field_40714.rotationDegrees(camPitch));
               ms.method_22905(0.9F, 0.9F, 0.9F);
               Matrix4f m = ms.method_23760().method_23761();
               int ai = (int)(alpha * 0.12F * 255.0F);
               int ri = (int)(r * 255.0F);
               int gi = (int)(g * 255.0F);
               int bi = (int)(b * 255.0F);
               builder.method_22918(m, -0.5F, 0.5F, 0.0F).method_22913(0.0F, 1.0F).method_1336(ri, gi, bi, ai);
               builder.method_22918(m, 0.5F, 0.5F, 0.0F).method_22913(1.0F, 1.0F).method_1336(ri, gi, bi, ai);
               builder.method_22918(m, 0.5F, -0.5F, 0.0F).method_22913(1.0F, 0.0F).method_1336(ri, gi, bi, ai);
               builder.method_22918(m, -0.5F, -0.5F, 0.0F).method_22913(0.0F, 0.0F).method_1336(ri, gi, bi, ai);
               ms.method_22909();
               return true;
            }
         }
      }

      private void drawBox(MatrixStack ms, int color) {
         float min = -0.5F;
         float max = 0.5F;
         float r = (float)ColorProvider.red(color) / 255.0F;
         float g = (float)ColorProvider.green(color) / 255.0F;
         float b = (float)ColorProvider.blue(color) / 255.0F;
         float a = (float)ColorProvider.alpha(color) / 255.0F;
         float lineA = a / 4.0F;
         float fillA = a / 12.0F;
         BufferBuilder buf = Tessellator.method_1348().method_60827(DrawMode.field_29344, VertexFormats.field_1576);
         Matrix4f m = ms.method_23760().method_23761();
         this.addLine(buf, m, min, min, min, max, min, min, r, g, b, lineA);
         this.addLine(buf, m, min, max, min, max, max, min, r, g, b, lineA);
         this.addLine(buf, m, min, min, max, max, min, max, r, g, b, lineA);
         this.addLine(buf, m, min, max, max, max, max, max, r, g, b, lineA);
         this.addLine(buf, m, min, min, min, min, max, min, r, g, b, lineA);
         this.addLine(buf, m, max, min, min, max, max, min, r, g, b, lineA);
         this.addLine(buf, m, min, min, max, min, max, max, r, g, b, lineA);
         this.addLine(buf, m, max, min, max, max, max, max, r, g, b, lineA);
         this.addLine(buf, m, min, min, min, min, min, max, r, g, b, lineA);
         this.addLine(buf, m, max, min, min, max, min, max, r, g, b, lineA);
         this.addLine(buf, m, min, max, min, min, max, max, r, g, b, lineA);
         this.addLine(buf, m, max, max, min, max, max, max, r, g, b, lineA);
         BufferRenderer.method_43433(buf.method_60800());
         BufferBuilder fb = Tessellator.method_1348().method_60827(DrawMode.field_27382, VertexFormats.field_1576);
         this.addFace(fb, m, min, min, min, max, min, max, r, g, b, fillA);
         this.addFace(fb, m, min, max, min, max, max, max, r, g, b, fillA);
         this.addFace(fb, m, min, min, min, max, max, min, r, g, b, fillA);
         this.addFace(fb, m, min, min, max, max, max, max, r, g, b, fillA);
         this.addFace(fb, m, min, min, min, min, max, max, r, g, b, fillA);
         this.addFace(fb, m, max, min, min, max, max, max, r, g, b, fillA);
         BufferRenderer.method_43433(fb.method_60800());
      }

      private void addLine(BufferBuilder buf, Matrix4f m, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a) {
         buf.method_22918(m, x1, y1, z1).method_22915(r, g, b, a);
         buf.method_22918(m, x2, y2, z2).method_22915(r, g, b, a);
      }

      private void addFace(BufferBuilder buf, Matrix4f m, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a) {
         buf.method_22918(m, x1, y1, z1).method_22915(r, g, b, a);
         buf.method_22918(m, x2, y1, z1).method_22915(r, g, b, a);
         buf.method_22918(m, x2, y1, z2).method_22915(r, g, b, a);
         buf.method_22918(m, x1, y1, z2).method_22915(r, g, b, a);
         buf.method_22918(m, x1, y2, z1).method_22915(r, g, b, a);
         buf.method_22918(m, x1, y2, z2).method_22915(r, g, b, a);
         buf.method_22918(m, x2, y2, z2).method_22915(r, g, b, a);
         buf.method_22918(m, x2, y2, z1).method_22915(r, g, b, a);
         buf.method_22918(m, x1, y1, z1).method_22915(r, g, b, a);
         buf.method_22918(m, x1, y2, z1).method_22915(r, g, b, a);
         buf.method_22918(m, x2, y2, z1).method_22915(r, g, b, a);
         buf.method_22918(m, x2, y1, z1).method_22915(r, g, b, a);
         buf.method_22918(m, x1, y1, z2).method_22915(r, g, b, a);
         buf.method_22918(m, x2, y1, z2).method_22915(r, g, b, a);
         buf.method_22918(m, x2, y2, z2).method_22915(r, g, b, a);
         buf.method_22918(m, x1, y2, z2).method_22915(r, g, b, a);
         buf.method_22918(m, x1, y1, z1).method_22915(r, g, b, a);
         buf.method_22918(m, x1, y1, z2).method_22915(r, g, b, a);
         buf.method_22918(m, x1, y2, z2).method_22915(r, g, b, a);
         buf.method_22918(m, x1, y2, z1).method_22915(r, g, b, a);
         buf.method_22918(m, x2, y1, z1).method_22915(r, g, b, a);
         buf.method_22918(m, x2, y2, z1).method_22915(r, g, b, a);
         buf.method_22918(m, x2, y2, z2).method_22915(r, g, b, a);
         buf.method_22918(m, x2, y1, z2).method_22915(r, g, b, a);
      }
   }

   private static class RiderParticle {
      Vec3d pos;
      float alpha;

      RiderParticle(Vec3d pos) {
         this.pos = pos;
         this.alpha = 0.3F;
      }
   }
}
