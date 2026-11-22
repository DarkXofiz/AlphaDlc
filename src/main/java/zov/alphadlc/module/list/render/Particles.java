package zov.alphadlc.module.list.render;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos.Mutable;
import org.joml.Matrix4f;
import zov.alphadlc.event.list.EventAttack;
import zov.alphadlc.event.list.EventPlayerUpdate;
import zov.alphadlc.event.list.EventPopTotem;
import zov.alphadlc.event.list.EventWorldRender;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.module.settings.ModeListSetting;
import zov.alphadlc.module.settings.SliderSetting;
import zov.alphadlc.module.settings.impl.Theme;
import zov.alphadlc.module.settings.impl.ThemeManager;
import zov.alphadlc.util.player.move.MoveUtil;
import zov.alphadlc.util.render.math.MathUtil;

@ModuleInformation(
   moduleName = "Particles",
   moduleDesc = "\u0414\u043e\u0431\u0430\u0432\u043b\u044f\u0435\u0442 \u0447\u0430\u0441\u0442\u0438\u0446\u044b \u043f\u0440\u0438 \u0440\u0430\u0437\u043d\u044b\u0445 \u0443\u0441\u043b\u043e\u0432\u0438\u044f\u0445",
   moduleCategory = ModuleCategory.RENDER
)
public class Particles extends Module {
   private final ModeListSetting type = new ModeListSetting(
      "\u0420\u0435\u0436\u0438\u043c",
      new BooleanSetting("\u0414\u043e\u043b\u043b\u0430\u0440\u044b", true),
      new BooleanSetting("\u0421\u0435\u0440\u0434\u0435\u0447\u043a\u0438", false),
      new BooleanSetting("\u041a\u0440\u0435\u0441\u0442\u0438\u043a\u0438", false),
      new BooleanSetting("\u041c\u043e\u043b\u043d\u0438\u0438", false),
      new BooleanSetting("\u041b\u0438\u043d\u0438\u0438", false),
      new BooleanSetting("\u0421\u0438\u044f\u043d\u0438\u0435", false),
      new BooleanSetting("\u0422\u044b\u043a\u043e\u0432\u043a\u0438", false),
      new BooleanSetting("\u0421\u043d\u0435\u0436\u0438\u043d\u043a\u0438", false),
      new BooleanSetting("\u0412\u0437\u0440\u044b\u0432", false)
   );
   private final ModeListSetting reason = new ModeListSetting(
      "\u0414\u043e\u0431\u0430\u0432\u043b\u044f\u0442\u044c \u043f\u0440\u0438",
      new BooleanSetting("\u0411\u0435\u0437\u0434\u0435\u0439\u0441\u0442\u0432\u0438\u0438", false),
      new BooleanSetting("\u0411\u0435\u0433\u0435", false),
      new BooleanSetting("\u0423\u0434\u0430\u0440\u0435", true),
      new BooleanSetting("\u041f\u0430\u0434\u0435\u043d\u0438\u0438 \u043f\u0435\u0440\u043b\u0430", true),
      new BooleanSetting("\u041f\u0430\u0434\u0435\u043d\u0438\u0438 \u0442\u0440\u0435\u0437\u0443\u0431\u0446\u0430", true),
      new BooleanSetting("\u041f\u0430\u0434\u0435\u043d\u0438\u0438 \u0441\u0442\u0440\u0435\u043b\u044b", true),
      new BooleanSetting("\u0421\u043d\u043e\u0441\u0435 \u0442\u043e\u0442\u0435\u043c\u0430", true)
   );
   private final SliderSetting count = new SliderSetting("\u041a\u043e\u043b\u0438\u0447\u0435\u0441\u0442\u0432\u043e", 10.0, 2.0, 40.0, 1.0);
   private final List<Particles.Particle> particles = new ArrayList<>();
   private boolean isPlayerTotem = false;
   private long lastUpdateTime = System.currentTimeMillis();

   private boolean isPositionInBlock(Vec3d position) {
      BlockPos blockPos = BlockPos.method_49637(position.field_1352, position.field_1351, position.field_1350);
      return this.mc.field_1687.method_8320(blockPos).method_26212(this.mc.field_1687, blockPos);
   }

   @Subscribe
   public void onWorldRender(EventWorldRender event) {
      if (!this.particles.isEmpty()) {
         long currentTime = System.currentTimeMillis();
         this.particles.removeIf(particlex -> currentTime - particlex.startTime > particlex.lifeTime);
         MatrixStack matrices = event.getMatrixStack();
         Vec3d cameraPos = this.mc.field_1773.method_19418().method_19326();
         float camYaw = this.mc.field_1773.method_19418().method_19330();
         float camPitch = this.mc.field_1773.method_19418().method_19329();
         List<String> enabledTypes = this.type.getEnabledModules();
         if (enabledTypes.isEmpty()) {
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
         } else {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(770, 1);
            RenderSystem.setShader(ShaderProgramKeys.field_53880);
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            List<Particles.Particle> renderList = new ArrayList<>();

            for (Particles.Particle particle : this.particles) {
               particle.update();
               Vec3d pos = particle.position;
               double halfSize = (double)particle.size * 0.5;
               Box aabb = new Box(
                  pos.field_1352 - halfSize,
                  pos.field_1351 - halfSize,
                  pos.field_1350 - halfSize,
                  pos.field_1352 + halfSize,
                  pos.field_1351 + halfSize,
                  pos.field_1350 + halfSize
               );
               if (this.mc.field_1769.field_27740.method_23093(aabb)) {
                  renderList.add(particle);
               }
            }

            for (String modeName : enabledTypes) {
               Identifier texture = Identifier.method_60655("mre", "images/" + getTexturePath(modeName));
               RenderSystem.setShaderTexture(0, texture);
               List<Particles.Particle> modeParticles = new ArrayList<>();

               for (Particles.Particle p : renderList) {
                  if (modeName.equals(p.particleType)) {
                     modeParticles.add(p);
                  }
               }

               if (!modeParticles.isEmpty()) {
                  if ("\u0421\u0438\u044f\u043d\u0438\u0435".equals(modeName)) {
                     BufferBuilder buffer = Tessellator.method_1348().method_60827(DrawMode.field_27382, VertexFormats.field_1575);

                     for (Particles.Particle particlex : modeParticles) {
                        this.renderParticleBloom(buffer, matrices, particlex, cameraPos, camYaw, camPitch, 2.0F, 0.15F, 1.0F);
                     }

                     BufferRenderer.method_43433(buffer.method_60800());
                     buffer = Tessellator.method_1348().method_60827(DrawMode.field_27382, VertexFormats.field_1575);

                     for (Particles.Particle particlex : modeParticles) {
                        this.renderParticleBloom(buffer, matrices, particlex, cameraPos, camYaw, camPitch, 1.0F, 0.35F, 1.0F);
                     }

                     BufferRenderer.method_43433(buffer.method_60800());
                     buffer = Tessellator.method_1348().method_60827(DrawMode.field_27382, VertexFormats.field_1575);

                     for (Particles.Particle particlex : modeParticles) {
                        this.renderParticleBloom(buffer, matrices, particlex, cameraPos, camYaw, camPitch, 0.4F, 1.0F, 1.5F);
                     }

                     BufferRenderer.method_43433(buffer.method_60800());
                  } else {
                     BufferBuilder buffer = Tessellator.method_1348().method_60827(DrawMode.field_27382, VertexFormats.field_1575);

                     for (Particles.Particle particlex : modeParticles) {
                        this.renderParticle(buffer, matrices, particlex, cameraPos, camYaw, camPitch);
                     }

                     BufferRenderer.method_43433(buffer.method_60800());
                  }
               }
            }

            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
         }
      }
   }

   @Subscribe
   public void onPlayerUpdate(EventPlayerUpdate event) {
      long currentTime = System.currentTimeMillis();
      if (this.reason.isEnabled("\u0411\u0435\u0437\u0434\u0435\u0439\u0441\u0442\u0432\u0438\u0438")
         && !MoveUtil.hasPlayerMovement()
         && currentTime - this.lastUpdateTime > 50L) {
         this.addIdleParticles();
         this.lastUpdateTime = currentTime;
      }

      if (this.reason.isEnabled("\u0411\u0435\u0433\u0435") && MoveUtil.hasPlayerMovement()) {
         this.addMovingParticles();
      }

      for (Entity entity : this.mc.field_1687.method_18112()) {
         if (this.reason.isEnabled("\u041f\u0430\u0434\u0435\u043d\u0438\u0438 \u043f\u0435\u0440\u043b\u0430")
            && entity instanceof EnderPearlEntity pearl
            && !pearl.method_24828()) {
            this.createProjectileParticles(pearl.method_19538());
         }

         if (this.reason.isEnabled("\u041f\u0430\u0434\u0435\u043d\u0438\u0438 \u0442\u0440\u0435\u0437\u0443\u0431\u0446\u0430")
            && entity instanceof TridentEntity trident
            && this.isProjectileFlying(trident)) {
            this.createProjectileParticles(trident.method_19538());
         }

         if (this.reason.isEnabled("\u041f\u0430\u0434\u0435\u043d\u0438\u0438 \u0441\u0442\u0440\u0435\u043b\u044b")
            && entity instanceof ArrowEntity arrow
            && this.isProjectileFlying(arrow)) {
            this.createProjectileParticles(arrow.method_19538());
         }
      }
   }

   @Subscribe
   public void onAttack(EventAttack event) {
      if (this.reason.isEnabled("\u0423\u0434\u0430\u0440\u0435") && event.getEntity() != null) {
         Entity target = event.getEntity();

         for (int i = 0; i < this.count.getIntValue(); i++) {
            double targetX = target.method_23317() + (double)MathUtil.random(-0.4F, 0.4F);
            double targetY = target.method_23318() + (double)MathUtil.random(-0.4F, target.method_17682() + 0.4F);
            double targetZ = target.method_23321() + (double)MathUtil.random(-0.4F, 0.4F);
            if (!this.isPositionInBlock(new Vec3d(targetX, targetY, targetZ))) {
               float baseMx = MathUtil.random(-0.8F, 0.8F) * 2.0F;
               float baseMy = MathUtil.random(-0.25F, 1.4F);
               float baseMz = MathUtil.random(-0.8F, 0.8F) * 2.0F;
               float smooth = 0.5F;
               long life = (long)MathUtil.random(1000.0F, 1200.0F);
               Vec3d velocity = new Vec3d((double)(baseMx * 0.075F), (double)(baseMy * 0.075F), (double)(baseMz * 0.075F));
               this.addParticle(targetX, targetY, targetZ, velocity, this.getThemeColor(), 0.6F, life, smooth, 7.0E-4F);
            }
         }
      }
   }

   @Subscribe
   public void onPopTotem(EventPopTotem event) {
      if (this.reason.isEnabled("\u0421\u043d\u043e\u0441\u0435 \u0442\u043e\u0442\u0435\u043c\u0430")) {
         this.isPlayerTotem = event.getPlayer() == this.mc.field_1724;
         if (!this.isPlayerTotem) {
            Vec3d pos = event.getPlayer().method_19538();
            double centerX = pos.field_1352;
            double centerY = pos.field_1351 + (double)event.getPlayer().method_17682() / 2.0;
            double centerZ = pos.field_1350;

            for (int i = 0; i < this.count.getIntValue(); i++) {
               double theta = Math.random() * 2.0 * Math.PI;
               double phi = Math.random() * Math.PI;
               double speed = (Math.random() * 0.5 + 0.5) * 0.1;
               double vx = Math.sin(phi) * Math.cos(theta) * speed;
               double vy = Math.sin(phi) * Math.sin(theta) * speed;
               double vz = Math.cos(phi) * speed;
               double spawnX = centerX + (double)MathUtil.random(-0.3F, 0.3F);
               double spawnY = centerY + (double)MathUtil.random(-0.3F, 0.3F);
               double spawnZ = centerZ + (double)MathUtil.random(-0.3F, 0.3F);
               if (!this.isPositionInBlock(new Vec3d(spawnX, spawnY, spawnZ))) {
                  int color = Math.random() < 0.7 ? -16711936 : -256;
                  float smooth = 2.0F;
                  long life = (long)MathUtil.random(1500.0F, 2000.0F);
                  this.addParticle(spawnX, spawnY, spawnZ, new Vec3d(vx, vy, vz), color, 0.6F, life, smooth, 5.0E-5F);
               }
            }
         }
      }
   }

   private void addIdleParticles() {
      Vec3d base = new Vec3d(
         this.mc.field_1724.method_23317(),
         this.mc.field_1724.method_23318() + (double)this.mc.field_1724.method_17682() / 2.0,
         this.mc.field_1724.method_23321()
      );

      for (int i = 0; i < this.count.getIntValue(); i++) {
         double distance = (double)MathUtil.random(7.0F, 35.0F);
         double angle = Math.toRadians((double)MathUtil.random(0.0F, 360.0F));
         double height = (double)MathUtil.random(-7.0F, 25.0F);
         Vec3d offset = new Vec3d(Math.cos(angle) * distance, height, Math.sin(angle) * distance);
         Vec3d spawnPos = base.method_1019(offset);
         if (!this.isPositionInBlock(spawnPos)) {
            long life = (long)MathUtil.random(1500.0F, 2000.0F);
            double speed = Math.random() < 0.8 ? (double)MathUtil.random(0.015F, 0.03F) : 0.125;
            double phi = Math.toRadians((double)MathUtil.random(0.0F, 360.0F));
            float smooth = 3.0F;
            Vec3d velocity = new Vec3d(Math.cos(phi) * speed, MathUtil.random(-speed * 0.1F, speed * 0.1F), Math.sin(phi) * speed);
            this.addParticle(spawnPos.field_1352, spawnPos.field_1351, spawnPos.field_1350, velocity, this.getThemeColor(), 0.6F, life, smooth, 5.0E-5F);
         }
      }
   }

   private void addMovingParticles() {
      double speed = Math.sqrt(
         this.mc.field_1724.method_18798().field_1352 * this.mc.field_1724.method_18798().field_1352
            + this.mc.field_1724.method_18798().field_1350 * this.mc.field_1724.method_18798().field_1350
      );
      Vec3d direction;
      if (speed < 0.01) {
         direction = this.mc.field_1724.method_5828(1.0F).method_1021(-1.0);
      } else if (this.mc.field_1724.method_6128()) {
         direction = this.mc.field_1724.method_18798().method_1029().method_1021(-1.0);
      } else {
         Vec3d motion = this.mc.field_1724.method_18798();
         direction = new Vec3d(-motion.field_1352 / speed, 0.0, -motion.field_1350 / speed);
      }

      double distanceBehind = (this.mc.field_1724.method_6128() ? 1.2 : 0.5) + (speed > 0.1 ? speed * 1.5 : 0.0);
      double offsetX = (double)MathUtil.random(-0.35F, 0.35F);
      double offsetZ = (double)MathUtil.random(-0.35F, 0.35F);
      double posX = this.mc.field_1724.method_23317() + direction.field_1352 * distanceBehind + offsetX;
      double posY = this.mc.field_1724.method_6128()
         ? this.mc.field_1724.method_23318()
            + (double)this.mc.field_1724.method_17682() / 2.0
            + direction.field_1351 * distanceBehind
            + (double)MathUtil.random(-0.35F, 0.35F)
         : this.mc.field_1724.method_23318() + (double)MathUtil.random(0.2F, this.mc.field_1724.method_17682() + 0.1F);
      double posZ = this.mc.field_1724.method_23321() + direction.field_1350 * distanceBehind + offsetZ;
      if (!this.isPositionInBlock(new Vec3d(posX, posY, posZ))) {
         double baseSpeed = 0.075;
         Vec3d velocity = direction.method_1021(baseSpeed)
            .method_1019(new Vec3d((double)MathUtil.random(-0.01F, 0.01F), (double)MathUtil.random(-0.05F, 0.01F), (double)MathUtil.random(-0.01F, 0.01F)))
            .method_1021(0.1F);
         long life = (long)MathUtil.random(1500.0F, 2000.0F);
         this.addParticle(posX, posY, posZ, velocity, this.getThemeColor(), 0.6F, life, 3.0F, 5.0E-5F);
      }
   }

   private void createProjectileParticles(Vec3d position) {
      int particleColor = this.getThemeColor();

      for (int i = 0; i < this.count.getIntValue(); i++) {
         double distance = 0.0;
         double angle = Math.toRadians((double)MathUtil.random(0.0F, 360.0F));
         double cosAngle = Math.cos(angle);
         double sinAngle = Math.sin(angle);
         double dx = cosAngle * 0.0;
         double dz = sinAngle * 0.0;
         double dy = (double)MathUtil.random(0.1F, 0.35F);
         Vec3d particlePos = new Vec3d(position.field_1352 + dx, position.field_1351 + dy, position.field_1350 + dz);
         if (!this.isPositionInBlock(particlePos)) {
            float life = MathUtil.random(2400.0F, 2800.0F);
            float speedMin = MathUtil.random(0.015F, 0.0375F);
            float speedMax = MathUtil.random(0.05F, 0.075F);
            double speedFinal = (double)MathUtil.random(speedMin, speedMax);
            double speedFinalY = speedFinal * 0.4;
            double angleVel = Math.toRadians((double)MathUtil.random(0.0F, 360.0F));
            double cosVel = Math.cos(angleVel);
            double sinVel = Math.sin(angleVel);
            double velX = cosVel * speedFinal;
            double velZ = sinVel * speedFinal;
            double velY = MathUtil.random(-speedFinalY, speedFinalY);
            this.addParticle(
               particlePos.field_1352,
               particlePos.field_1351,
               particlePos.field_1350,
               new Vec3d(velX, velY, velZ),
               particleColor,
               0.375F,
               (long)life,
               2.0F,
               5.0E-5F
            );
         }
      }
   }

   private boolean isProjectileFlying(Entity projectile) {
      if (!projectile.method_24828() && !(projectile.method_18798().method_1027() <= 1.0E-4)) {
         Vec3d pos = projectile.method_19538();
         Vec3d motion = projectile.method_18798().method_1029().method_1021(0.5);
         BlockPos currentPos = BlockPos.method_49638(pos);
         BlockPos frontPos = BlockPos.method_49638(pos.method_1019(motion));
         return this.mc.field_1687.method_8320(currentPos).method_26215() && this.mc.field_1687.method_8320(frontPos).method_26215();
      } else {
         return false;
      }
   }

   private void addParticle(double x, double y, double z, Vec3d velocity, int color, float size, long lifeTime, float smooth, double gravity) {
      List<String> enabledTypes = this.type.getEnabledModules();
      if (!enabledTypes.isEmpty()) {
         String particleType = enabledTypes.get((int)(Math.random() * (double)enabledTypes.size()));
         Vec3d safePos = this.checkCollision(x, y, z, size);
         if (safePos != null) {
            this.particles
               .add(
                  new Particles.Particle(
                     safePos.field_1352, safePos.field_1351, safePos.field_1350, velocity, color, size, lifeTime, smooth, gravity, particleType
                  )
               );
         }
      }
   }

   private Vec3d checkCollision(double x, double y, double z, float size) {
      double half = (double)size * 0.5;
      int minX = MathHelper.method_15357(x - half);
      int maxX = MathHelper.method_15357(x + half);
      int minY = MathHelper.method_15357(y - half);
      int maxY = MathHelper.method_15357(y + half);
      int minZ = MathHelper.method_15357(z - half);
      int maxZ = MathHelper.method_15357(z + half);
      Mutable pos = new Mutable();

      for (int bx = minX; bx <= maxX; bx++) {
         for (int by = minY; by <= maxY; by++) {
            for (int bz = minZ; bz <= maxZ; bz++) {
               BlockState state = this.mc.field_1687.method_8320(pos.method_10103(bx, by, bz));
               if (!state.method_26215()) {
                  return null;
               }
            }
         }
      }

      return new Vec3d(x, y, z);
   }

   private void renderParticle(BufferBuilder buffer, MatrixStack matrices, Particles.Particle particle, Vec3d cameraPos, float camYaw, float camPitch) {
      if (!(particle.alpha <= 0.001F) && !(particle.size <= 1.0E-4F)) {
         matrices.method_22903();
         double rx = particle.position.field_1352 - cameraPos.field_1352;
         double ry = particle.position.field_1351 - cameraPos.field_1351;
         double rz = particle.position.field_1350 - cameraPos.field_1350;
         matrices.method_22904(rx, ry, rz);
         matrices.method_22907(RotationAxis.field_40716.rotationDegrees(-camYaw));
         matrices.method_22907(RotationAxis.field_40714.rotationDegrees(camPitch));
         float size = particle.size;
         matrices.method_22905(size, size, size);
         Matrix4f matrix = matrices.method_23760().method_23761();
         int r = particle.color >> 16 & 0xFF;
         int g = particle.color >> 8 & 0xFF;
         int b = particle.color & 0xFF;
         int a = (int)(particle.alpha * 255.0F);
         buffer.method_22918(matrix, -0.5F, 0.5F, 0.0F).method_22913(0.0F, 1.0F).method_1336(r, g, b, a);
         buffer.method_22918(matrix, 0.5F, 0.5F, 0.0F).method_22913(1.0F, 1.0F).method_1336(r, g, b, a);
         buffer.method_22918(matrix, 0.5F, -0.5F, 0.0F).method_22913(1.0F, 0.0F).method_1336(r, g, b, a);
         buffer.method_22918(matrix, -0.5F, -0.5F, 0.0F).method_22913(0.0F, 0.0F).method_1336(r, g, b, a);
         matrices.method_22909();
      }
   }

   private void renderParticleBloom(
      BufferBuilder buffer,
      MatrixStack matrices,
      Particles.Particle particle,
      Vec3d cameraPos,
      float camYaw,
      float camPitch,
      float sizeMultiplier,
      float alphaMultiplier,
      float colorBoost
   ) {
      if (!(particle.alpha <= 0.001F) && !(particle.size <= 1.0E-4F)) {
         matrices.method_22903();
         double rx = particle.position.field_1352 - cameraPos.field_1352;
         double ry = particle.position.field_1351 - cameraPos.field_1351;
         double rz = particle.position.field_1350 - cameraPos.field_1350;
         matrices.method_22904(rx, ry, rz);
         matrices.method_22907(RotationAxis.field_40716.rotationDegrees(-camYaw));
         matrices.method_22907(RotationAxis.field_40714.rotationDegrees(camPitch));
         float size = particle.size * sizeMultiplier;
         matrices.method_22905(size, size, size);
         Matrix4f matrix = matrices.method_23760().method_23761();
         int r = Math.min(255, (int)((float)(particle.color >> 16 & 0xFF) * colorBoost));
         int g = Math.min(255, (int)((float)(particle.color >> 8 & 0xFF) * colorBoost));
         int b = Math.min(255, (int)((float)(particle.color & 0xFF) * colorBoost));
         int a = (int)(particle.alpha * alphaMultiplier * 255.0F);
         buffer.method_22918(matrix, -0.5F, 0.5F, 0.0F).method_22913(0.0F, 1.0F).method_1336(r, g, b, a);
         buffer.method_22918(matrix, 0.5F, 0.5F, 0.0F).method_22913(1.0F, 1.0F).method_1336(r, g, b, a);
         buffer.method_22918(matrix, 0.5F, -0.5F, 0.0F).method_22913(1.0F, 0.0F).method_1336(r, g, b, a);
         buffer.method_22918(matrix, -0.5F, -0.5F, 0.0F).method_22913(0.0F, 0.0F).method_1336(r, g, b, a);
         matrices.method_22909();
      }
   }

   private int getThemeColor() {
      Theme theme = ThemeManager.getInstance().getCurrentTheme();
      return theme != null ? theme.color1 : -1;
   }

   private static String getTexturePath(String displayName) {
      return switch (displayName) {
         case "\u0421\u0435\u0440\u0434\u0435\u0447\u043a\u0438" -> "heart.png";
         case "\u041a\u0440\u0435\u0441\u0442\u0438\u043a\u0438" -> "cross.png";
         case "\u041c\u043e\u043b\u043d\u0438\u0438" -> "lightning.png";
         case "\u041b\u0438\u043d\u0438\u0438" -> "line.png";
         case "\u0421\u0438\u044f\u043d\u0438\u0435" -> "firefly.png";
         case "\u0422\u044b\u043a\u043e\u0432\u043a\u0438" -> "pumpkin.png";
         case "\u0421\u043d\u0435\u0436\u0438\u043d\u043a\u0438" -> "snow.png";
         case "\u0412\u0437\u0440\u044b\u0432" -> "star.png";
         default -> "dollar.png";
      };
   }

   @Override
   public void onDisable() {
      this.particles.clear();
      super.onDisable();
   }

   public static class Particle {
      Vec3d position;
      Vec3d velocity;
      int color;
      float size;
      float alpha = 1.0F;
      float smoothFactor;
      long lifeTime;
      long startTime;
      double gravity;
      String particleType;
      private long lastUpdateNs;
      private static MinecraftClient mc = MinecraftClient.method_1551();

      public Particle(double x, double y, double z, Vec3d velocity, int color, float size, long lifeTime, float smooth, double gravity, String particleType) {
         this.position = new Vec3d(x, y, z);
         this.velocity = velocity;
         this.color = color;
         this.size = size;
         this.lifeTime = lifeTime;
         this.startTime = System.currentTimeMillis();
         this.lastUpdateNs = System.nanoTime();
         this.smoothFactor = smooth;
         this.gravity = gravity;
         this.particleType = particleType;
      }

      public void update() {
         long nowNs = System.nanoTime();
         double deltaSec = (double)(nowNs - this.lastUpdateNs) / 1.0E9;
         this.lastUpdateNs = nowNs;
         long now = System.currentTimeMillis();
         float progress = Math.min(1.0F, (float)(now - this.startTime) / (float)this.lifeTime);
         double factor = Math.pow(1.0 - (double)progress, (double)this.smoothFactor);
         double vx = this.velocity.field_1352;
         double vy = this.velocity.field_1351;
         double vz = this.velocity.field_1350;
         double newX = this.position.field_1352;
         double newY = this.position.field_1351;
         double newZ = this.position.field_1350;
         newX += vx * factor * deltaSec * 60.0;
         if (checkCollision(newX, this.position.field_1351, this.position.field_1350, this.size) == null) {
            vx = -vx * 0.8;
            newX = this.position.field_1352;
         }

         newY += vy * factor * deltaSec * 60.0;
         if (checkCollision(newX, newY, this.position.field_1350, this.size) == null) {
            vy = -vy * 1.5;
            newY = this.position.field_1351;
         }

         newZ += vz * factor * deltaSec * 60.0;
         if (checkCollision(newX, newY, newZ, this.size) == null) {
            vz = -vz * 0.8;
            newZ = this.position.field_1350;
         }

         this.position = new Vec3d(newX, newY, newZ);
         this.velocity = new Vec3d(vx * 0.9999, vy * 0.9999 - this.gravity, vz * 0.9999);
         this.alpha = 1.0F - progress;
      }

      private static Vec3d checkCollision(double x, double y, double z, float size) {
         if (mc != null && mc.field_1687 != null) {
            double half = (double)size * 0.5;
            int minX = MathHelper.method_15357(x - half);
            int maxX = MathHelper.method_15357(x + half);
            int minY = MathHelper.method_15357(y - half);
            int maxY = MathHelper.method_15357(y + half);
            int minZ = MathHelper.method_15357(z - half);
            int maxZ = MathHelper.method_15357(z + half);
            Mutable pos = new Mutable();

            for (int bx = minX; bx <= maxX; bx++) {
               for (int by = minY; by <= maxY; by++) {
                  for (int bz = minZ; bz <= maxZ; bz++) {
                     BlockState state = mc.field_1687.method_8320(pos.method_10103(bx, by, bz));
                     if (!state.method_26215() && state.method_26212(mc.field_1687, pos)) {
                        return null;
                     }
                  }
               }
            }

            return new Vec3d(x, y, z);
         } else {
            return new Vec3d(x, y, z);
         }
      }

      @Generated
      public Vec3d getPosition() {
         return this.position;
      }

      @Generated
      public Vec3d getVelocity() {
         return this.velocity;
      }

      @Generated
      public int getColor() {
         return this.color;
      }

      @Generated
      public float getSize() {
         return this.size;
      }

      @Generated
      public float getAlpha() {
         return this.alpha;
      }

      @Generated
      public float getSmoothFactor() {
         return this.smoothFactor;
      }

      @Generated
      public long getLifeTime() {
         return this.lifeTime;
      }

      @Generated
      public long getStartTime() {
         return this.startTime;
      }

      @Generated
      public double getGravity() {
         return this.gravity;
      }

      @Generated
      public String getParticleType() {
         return this.particleType;
      }

      @Generated
      public long getLastUpdateNs() {
         return this.lastUpdateNs;
      }
   }
}
