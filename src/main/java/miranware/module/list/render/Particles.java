package miranware.module.list.render;

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
import miranware.event.list.EventAttack;
import miranware.event.list.EventPlayerUpdate;
import miranware.event.list.EventPopTotem;
import miranware.event.list.EventWorldRender;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.BooleanSetting;
import miranware.module.settings.ModeListSetting;
import miranware.module.settings.SliderSetting;
import miranware.module.settings.impl.Theme;
import miranware.module.settings.impl.ThemeManager;
import miranware.util.player.move.MoveUtil;
import miranware.util.render.math.MathUtil;

@ModuleInformation(
   moduleName = "Particles",
   moduleDesc = "Adds particles under different conditions",
   moduleCategory = ModuleCategory.RENDER
)
public class Particles extends Module {
   private final ModeListSetting type = new ModeListSetting(
      "Mode",
      new BooleanSetting("Dollars", true),
      new BooleanSetting("Hearts", false),
      new BooleanSetting("Crosses", false),
      new BooleanSetting("Lightning", false),
      new BooleanSetting("Lines", false),
      new BooleanSetting("Shine", false),
      new BooleanSetting("Pumpkins", false),
      new BooleanSetting("Snowflakes", false),
      new BooleanSetting("Explosion", false)
   );
   private final ModeListSetting reason = new ModeListSetting(
      "Add On",
      new BooleanSetting("Idling", false),
      new BooleanSetting("Running", false),
      new BooleanSetting("Hit", true),
      new BooleanSetting("Pearl Fall", true),
      new BooleanSetting("Trident Fall", true),
      new BooleanSetting("Arrow Fall", true),
      new BooleanSetting("Totem Pop", true)
   );
   private final SliderSetting count = new SliderSetting("Amount", 10.0, 2.0, 40.0, 1.0);
   private final List<Particles.Particle> particles = new ArrayList<>();
   private boolean isPlayerTotem = false;
   private long lastUpdateTime = System.currentTimeMillis();

   private boolean isPositionInBlock(Vec3d position) {
      BlockPos blockPos = BlockPos.ofFloored(position.x, position.y, position.z);
      return this.mc.world.getBlockState(blockPos).isSolidBlock(this.mc.world, blockPos);
   }

   @Subscribe
   public void onWorldRender(EventWorldRender event) {
      if (!this.particles.isEmpty()) {
         long currentTime = System.currentTimeMillis();
         this.particles.removeIf(particlex -> currentTime - particlex.startTime > particlex.lifeTime);
         MatrixStack matrices = event.getMatrixStack();
         Vec3d cameraPos = this.mc.gameRenderer.getCamera().getPos();
         float camYaw = this.mc.gameRenderer.getCamera().getYaw();
         float camPitch = this.mc.gameRenderer.getCamera().getPitch();
         List<String> enabledTypes = this.type.getEnabledModules();
         if (enabledTypes.isEmpty()) {
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
         } else {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(770, 1);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            List<Particles.Particle> renderList = new ArrayList<>();

            for (Particles.Particle particle : this.particles) {
               particle.update();
               Vec3d pos = particle.position;
               double halfSize = (double)particle.size * 0.5;
               Box aabb = new Box(
                  pos.x - halfSize,
                  pos.y - halfSize,
                  pos.z - halfSize,
                  pos.x + halfSize,
                  pos.y + halfSize,
                  pos.z + halfSize
               );
               if (this.mc.worldRenderer.frustum.isVisible(aabb)) {
                  renderList.add(particle);
               }
            }

            for (String modeName : enabledTypes) {
               Identifier texture = Identifier.of("mre", "images/" + getTexturePath(modeName));
               RenderSystem.setShaderTexture(0, texture);
               List<Particles.Particle> modeParticles = new ArrayList<>();

               for (Particles.Particle p : renderList) {
                  if (modeName.equals(p.particleType)) {
                     modeParticles.add(p);
                  }
               }

               if (!modeParticles.isEmpty()) {
                  if ("Shine".equals(modeName)) {
                     BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

                     for (Particles.Particle particlex : modeParticles) {
                        this.renderParticleBloom(buffer, matrices, particlex, cameraPos, camYaw, camPitch, 2.0F, 0.15F, 1.0F);
                     }

                     BufferRenderer.drawWithGlobalProgram(buffer.end());
                     buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

                     for (Particles.Particle particlex : modeParticles) {
                        this.renderParticleBloom(buffer, matrices, particlex, cameraPos, camYaw, camPitch, 1.0F, 0.35F, 1.0F);
                     }

                     BufferRenderer.drawWithGlobalProgram(buffer.end());
                     buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

                     for (Particles.Particle particlex : modeParticles) {
                        this.renderParticleBloom(buffer, matrices, particlex, cameraPos, camYaw, camPitch, 0.4F, 1.0F, 1.5F);
                     }

                     BufferRenderer.drawWithGlobalProgram(buffer.end());
                  } else {
                     BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

                     for (Particles.Particle particlex : modeParticles) {
                        this.renderParticle(buffer, matrices, particlex, cameraPos, camYaw, camPitch);
                     }

                     BufferRenderer.drawWithGlobalProgram(buffer.end());
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
      if (this.reason.isEnabled("Idling")
         && !MoveUtil.hasPlayerMovement()
         && currentTime - this.lastUpdateTime > 50L) {
         this.addIdleParticles();
         this.lastUpdateTime = currentTime;
      }

      if (this.reason.isEnabled("Running") && MoveUtil.hasPlayerMovement()) {
         this.addMovingParticles();
      }

      for (Entity entity : this.mc.world.getEntities()) {
         if (this.reason.isEnabled("Pearl Fall")
            && entity instanceof EnderPearlEntity pearl
            && !pearl.isOnGround()) {
            this.createProjectileParticles(pearl.getPos());
         }

         if (this.reason.isEnabled("Trident Fall")
            && entity instanceof TridentEntity trident
            && this.isProjectileFlying(trident)) {
            this.createProjectileParticles(trident.getPos());
         }

         if (this.reason.isEnabled("Arrow Fall")
            && entity instanceof ArrowEntity arrow
            && this.isProjectileFlying(arrow)) {
            this.createProjectileParticles(arrow.getPos());
         }
      }
   }

   @Subscribe
   public void onAttack(EventAttack event) {
      if (this.reason.isEnabled("Hit") && event.getEntity() != null) {
         Entity target = event.getEntity();

         for (int i = 0; i < this.count.getIntValue(); i++) {
            double targetX = target.getX() + (double)MathUtil.random(-0.4F, 0.4F);
            double targetY = target.getY() + (double)MathUtil.random(-0.4F, target.getHeight() + 0.4F);
            double targetZ = target.getZ() + (double)MathUtil.random(-0.4F, 0.4F);
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
      if (this.reason.isEnabled("Totem Pop")) {
         this.isPlayerTotem = event.getPlayer() == this.mc.player;
         if (!this.isPlayerTotem) {
            Vec3d pos = event.getPlayer().getPos();
            double centerX = pos.x;
            double centerY = pos.y + (double)event.getPlayer().getHeight() / 2.0;
            double centerZ = pos.z;

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
         this.mc.player.getX(),
         this.mc.player.getY() + (double)this.mc.player.getHeight() / 2.0,
         this.mc.player.getZ()
      );

      for (int i = 0; i < this.count.getIntValue(); i++) {
         double distance = (double)MathUtil.random(7.0F, 35.0F);
         double angle = Math.toRadians((double)MathUtil.random(0.0F, 360.0F));
         double height = (double)MathUtil.random(-7.0F, 25.0F);
         Vec3d offset = new Vec3d(Math.cos(angle) * distance, height, Math.sin(angle) * distance);
         Vec3d spawnPos = base.add(offset);
         if (!this.isPositionInBlock(spawnPos)) {
            long life = (long)MathUtil.random(1500.0F, 2000.0F);
            double speed = Math.random() < 0.8 ? (double)MathUtil.random(0.015F, 0.03F) : 0.125;
            double phi = Math.toRadians((double)MathUtil.random(0.0F, 360.0F));
            float smooth = 3.0F;
            Vec3d velocity = new Vec3d(Math.cos(phi) * speed, MathUtil.random(-speed * 0.1F, speed * 0.1F), Math.sin(phi) * speed);
            this.addParticle(spawnPos.x, spawnPos.y, spawnPos.z, velocity, this.getThemeColor(), 0.6F, life, smooth, 5.0E-5F);
         }
      }
   }

   private void addMovingParticles() {
      double speed = Math.sqrt(
         this.mc.player.getVelocity().x * this.mc.player.getVelocity().x
            + this.mc.player.getVelocity().z * this.mc.player.getVelocity().z
      );
      Vec3d direction;
      if (speed < 0.01) {
         direction = this.mc.player.getRotationVec(1.0F).multiply(-1.0);
      } else if (this.mc.player.isGliding()) {
         direction = this.mc.player.getVelocity().normalize().multiply(-1.0);
      } else {
         Vec3d motion = this.mc.player.getVelocity();
         direction = new Vec3d(-motion.x / speed, 0.0, -motion.z / speed);
      }

      double distanceBehind = (this.mc.player.isGliding() ? 1.2 : 0.5) + (speed > 0.1 ? speed * 1.5 : 0.0);
      double offsetX = (double)MathUtil.random(-0.35F, 0.35F);
      double offsetZ = (double)MathUtil.random(-0.35F, 0.35F);
      double posX = this.mc.player.getX() + direction.x * distanceBehind + offsetX;
      double posY = this.mc.player.isGliding()
         ? this.mc.player.getY()
            + (double)this.mc.player.getHeight() / 2.0
            + direction.y * distanceBehind
            + (double)MathUtil.random(-0.35F, 0.35F)
         : this.mc.player.getY() + (double)MathUtil.random(0.2F, this.mc.player.getHeight() + 0.1F);
      double posZ = this.mc.player.getZ() + direction.z * distanceBehind + offsetZ;
      if (!this.isPositionInBlock(new Vec3d(posX, posY, posZ))) {
         double baseSpeed = 0.075;
         Vec3d velocity = direction.multiply(baseSpeed)
            .add(new Vec3d((double)MathUtil.random(-0.01F, 0.01F), (double)MathUtil.random(-0.05F, 0.01F), (double)MathUtil.random(-0.01F, 0.01F)))
            .multiply(0.1F);
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
         Vec3d particlePos = new Vec3d(position.x + dx, position.y + dy, position.z + dz);
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
               particlePos.x,
               particlePos.y,
               particlePos.z,
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
      if (!projectile.isOnGround() && !(projectile.getVelocity().lengthSquared() <= 1.0E-4)) {
         Vec3d pos = projectile.getPos();
         Vec3d motion = projectile.getVelocity().normalize().multiply(0.5);
         BlockPos currentPos = BlockPos.ofFloored(pos);
         BlockPos frontPos = BlockPos.ofFloored(pos.add(motion));
         return this.mc.world.getBlockState(currentPos).isAir() && this.mc.world.getBlockState(frontPos).isAir();
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
                     safePos.x, safePos.y, safePos.z, velocity, color, size, lifeTime, smooth, gravity, particleType
                  )
               );
         }
      }
   }

   private Vec3d checkCollision(double x, double y, double z, float size) {
      double half = (double)size * 0.5;
      int minX = MathHelper.floor(x - half);
      int maxX = MathHelper.floor(x + half);
      int minY = MathHelper.floor(y - half);
      int maxY = MathHelper.floor(y + half);
      int minZ = MathHelper.floor(z - half);
      int maxZ = MathHelper.floor(z + half);
      Mutable pos = new Mutable();

      for (int bx = minX; bx <= maxX; bx++) {
         for (int by = minY; by <= maxY; by++) {
            for (int bz = minZ; bz <= maxZ; bz++) {
               BlockState state = this.mc.world.getBlockState(pos.set(bx, by, bz));
               if (!state.isAir()) {
                  return null;
               }
            }
         }
      }

      return new Vec3d(x, y, z);
   }

   private void renderParticle(BufferBuilder buffer, MatrixStack matrices, Particles.Particle particle, Vec3d cameraPos, float camYaw, float camPitch) {
      if (!(particle.alpha <= 0.001F) && !(particle.size <= 1.0E-4F)) {
         matrices.push();
         double rx = particle.position.x - cameraPos.x;
         double ry = particle.position.y - cameraPos.y;
         double rz = particle.position.z - cameraPos.z;
         matrices.translate(rx, ry, rz);
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camYaw));
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camPitch));
         float size = particle.size;
         matrices.scale(size, size, size);
         Matrix4f matrix = matrices.peek().getPositionMatrix();
         int r = particle.color >> 16 & 0xFF;
         int g = particle.color >> 8 & 0xFF;
         int b = particle.color & 0xFF;
         int a = (int)(particle.alpha * 255.0F);
         buffer.vertex(matrix, -0.5F, 0.5F, 0.0F).texture(0.0F, 1.0F).color(r, g, b, a);
         buffer.vertex(matrix, 0.5F, 0.5F, 0.0F).texture(1.0F, 1.0F).color(r, g, b, a);
         buffer.vertex(matrix, 0.5F, -0.5F, 0.0F).texture(1.0F, 0.0F).color(r, g, b, a);
         buffer.vertex(matrix, -0.5F, -0.5F, 0.0F).texture(0.0F, 0.0F).color(r, g, b, a);
         matrices.pop();
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
         matrices.push();
         double rx = particle.position.x - cameraPos.x;
         double ry = particle.position.y - cameraPos.y;
         double rz = particle.position.z - cameraPos.z;
         matrices.translate(rx, ry, rz);
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camYaw));
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camPitch));
         float size = particle.size * sizeMultiplier;
         matrices.scale(size, size, size);
         Matrix4f matrix = matrices.peek().getPositionMatrix();
         int r = Math.min(255, (int)((float)(particle.color >> 16 & 0xFF) * colorBoost));
         int g = Math.min(255, (int)((float)(particle.color >> 8 & 0xFF) * colorBoost));
         int b = Math.min(255, (int)((float)(particle.color & 0xFF) * colorBoost));
         int a = (int)(particle.alpha * alphaMultiplier * 255.0F);
         buffer.vertex(matrix, -0.5F, 0.5F, 0.0F).texture(0.0F, 1.0F).color(r, g, b, a);
         buffer.vertex(matrix, 0.5F, 0.5F, 0.0F).texture(1.0F, 1.0F).color(r, g, b, a);
         buffer.vertex(matrix, 0.5F, -0.5F, 0.0F).texture(1.0F, 0.0F).color(r, g, b, a);
         buffer.vertex(matrix, -0.5F, -0.5F, 0.0F).texture(0.0F, 0.0F).color(r, g, b, a);
         matrices.pop();
      }
   }

   private int getThemeColor() {
      Theme theme = ThemeManager.getInstance().getCurrentTheme();
      return theme != null ? theme.color1 : -1;
   }

   private static String getTexturePath(String displayName) {
      return switch (displayName) {
         case "Hearts" -> "heart.png";
         case "Crosses" -> "cross.png";
         case "Lightning" -> "lightning.png";
         case "Lines" -> "line.png";
         case "Shine" -> "firefly.png";
         case "Pumpkins" -> "pumpkin.png";
         case "Snowflakes" -> "snow.png";
         case "Explosion" -> "star.png";
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
      private static MinecraftClient mc = MinecraftClient.getInstance();

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
         double vx = this.velocity.x;
         double vy = this.velocity.y;
         double vz = this.velocity.z;
         double newX = this.position.x;
         double newY = this.position.y;
         double newZ = this.position.z;
         newX += vx * factor * deltaSec * 60.0;
         if (checkCollision(newX, this.position.y, this.position.z, this.size) == null) {
            vx = -vx * 0.8;
            newX = this.position.x;
         }

         newY += vy * factor * deltaSec * 60.0;
         if (checkCollision(newX, newY, this.position.z, this.size) == null) {
            vy = -vy * 1.5;
            newY = this.position.y;
         }

         newZ += vz * factor * deltaSec * 60.0;
         if (checkCollision(newX, newY, newZ, this.size) == null) {
            vz = -vz * 0.8;
            newZ = this.position.z;
         }

         this.position = new Vec3d(newX, newY, newZ);
         this.velocity = new Vec3d(vx * 0.9999, vy * 0.9999 - this.gravity, vz * 0.9999);
         this.alpha = 1.0F - progress;
      }

      private static Vec3d checkCollision(double x, double y, double z, float size) {
         if (mc != null && mc.world != null) {
            double half = (double)size * 0.5;
            int minX = MathHelper.floor(x - half);
            int maxX = MathHelper.floor(x + half);
            int minY = MathHelper.floor(y - half);
            int maxY = MathHelper.floor(y + half);
            int minZ = MathHelper.floor(z - half);
            int maxZ = MathHelper.floor(z + half);
            Mutable pos = new Mutable();

            for (int bx = minX; bx <= maxX; bx++) {
               for (int by = minY; by <= maxY; by++) {
                  for (int bz = minZ; bz <= maxZ; bz++) {
                     BlockState state = mc.world.getBlockState(pos.set(bx, by, bz));
                     if (!state.isAir() && state.isSolidBlock(mc.world, pos)) {
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
