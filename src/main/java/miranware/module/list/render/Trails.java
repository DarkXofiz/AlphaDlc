package miranware.module.list.render;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
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
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import miranware.event.list.EventTick;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.BooleanSetting;
import miranware.module.settings.ModeListSetting;
import miranware.module.settings.SliderSetting;
import miranware.util.friend.FriendRepository;
import miranware.util.render.effects.MasEffectRenderer;
import miranware.util.render.providers.ColorProvider;

@ModuleInformation(
   moduleName = "Trails",
   moduleDesc = "Leaves a trail behind entities",
   moduleCategory = ModuleCategory.RENDER
)
public class Trails extends Module {
   private static final int PRETTY_HARD_CAP = 1000;
   private final ModeListSetting mode = new ModeListSetting(
      "Mode",
      new BooleanSetting("Entity Trails", true),
      new BooleanSetting("Pearl Trail", false),
      new BooleanSetting("Pretty", false)
   );
   private final SliderSetting duration = new SliderSetting(
      "Duration", 1000.0, 500.0, 2000.0, 100.0
   );
   private final SliderSetting lineWidth = new SliderSetting("Thickness", 2.0, 0.5, 5.0, 0.1F);
   private final SliderSetting opacity = new SliderSetting(
      "Opacity", 100.0, 0.0, 255.0, 5.0
   );
   private final BooleanSetting firstPerson = new BooleanSetting("First Person", false);
   private final BooleanSetting targetSelf = new BooleanSetting("Self", true);
   private final BooleanSetting targetFriends = new BooleanSetting("On Friends", true);
   private final BooleanSetting targetPlayers = new BooleanSetting("On Players", true);
   private final BooleanSetting targetMobs = new BooleanSetting("On Mobs", false);
   private final SliderSetting prettyCount = new SliderSetting(
         "Stick Amount", 2.0, 1.0, 10.0, 1.0
      )
      .setVisible(() -> this.mode.isEnabled("Pretty"));
   private final SliderSetting prettyLifetime = new SliderSetting(
         "Duration (ms)", 1800.0, 500.0, 5000.0, 100.0
      )
      .setVisible(() -> this.mode.isEnabled("Pretty"));
   private final SliderSetting prettySpread = new SliderSetting(
         "Closeness", 0.4F, 0.1F, 1.5, 0.1F
      )
      .setVisible(() -> this.mode.isEnabled("Pretty"));
   private final SliderSetting prettyLineLength = new SliderSetting(
         "Stick Length", 0.3F, 0.1F, 1.0, 0.05F
      )
      .setVisible(() -> this.mode.isEnabled("Pretty"));
   private final BooleanSetting prettyGlow = new BooleanSetting(
         "Stick Glow", true
      )
      .setVisible(() -> this.mode.isEnabled("Pretty"));
   private final BooleanSetting prettyRainbow = new BooleanSetting("Rainbow Color", false)
      .setVisible(() -> this.mode.isEnabled("Pretty"));
   private final Map<Integer, Deque<Trails.Point>> entityPoints = new HashMap<>();
   private final Map<Integer, Vec3d> lastPositions = new HashMap<>();
   private final Map<Integer, Long> lastPointTimes = new HashMap<>();
   private final Map<Integer, Trails.EntityType> entityTypes = new HashMap<>();
   private long lastEntityScan = 0L;
   private final ArrayDeque<Trails.PrettyFig> prettyFigs = new ArrayDeque<>();
   private Vec3d lastPrettySpawnPos = null;
   private final Random random = new Random();

   public Trails() {
      WorldRenderEvents.LAST.register(this::onRender);
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.clearData();
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.clearData();
   }

   private void clearData() {
      this.entityPoints.clear();
      this.lastPositions.clear();
      this.lastPointTimes.clear();
      this.entityTypes.clear();
      this.prettyFigs.clear();
      this.lastPrettySpawnPos = null;
      this.lastEntityScan = 0L;
   }

   @Subscribe
   private void onTick(EventTick e) {
      if (this.isEnabled() && this.mc.player != null && this.mc.world != null) {
         if (this.mode.isEnabled("Pretty")) {
            this.updatePretty();
         } else if (!this.prettyFigs.isEmpty()) {
            this.prettyFigs.clear();
         }
      }
   }

   private void updatePretty() {
      long now = System.currentTimeMillis();
      Iterator<Trails.PrettyFig> it = this.prettyFigs.iterator();

      while (it.hasNext()) {
         Trails.PrettyFig f = it.next();
         f.tick();
         if (now - f.spawnTime > f.lifeMs) {
            it.remove();
         }
      }

      boolean shouldShow = this.firstPerson.getValue() || !this.mc.options.getPerspective().isFirstPerson();
      if (shouldShow) {
         Vec3d pos = this.mc.player.getPos();
         Vec3d vel = this.mc.player.getVelocity();
         double horizontalSpeed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
         boolean isRunning = horizontalSpeed > 0.01;
         if (isRunning) {
            int spawnPerTick = (int)this.prettyCount.getValue();
            int canSpawn = Math.min(spawnPerTick, 1000 - this.prettyFigs.size());

            for (int i = 0; i < canSpawn; i++) {
               this.spawnPretty(pos);
            }
         }

         while (this.prettyFigs.size() > 1000) {
            this.prettyFigs.pollFirst();
         }
      }
   }

   private void spawnPretty(Vec3d base) {
      float yaw = this.mc.player.getYaw();
      double yawRad = Math.toRadians((double)yaw);
      double backwardX = Math.sin(yawRad);
      double backwardZ = -Math.cos(yawRad);
      float spreadMultiplier = this.prettySpread.getFloatValue();
      double backDist = (0.3 + this.random.nextDouble() * 0.5) * (double)spreadMultiplier;
      double sideOffset = (this.random.nextDouble() - 0.5) * 0.4 * (double)spreadMultiplier;
      double sideX = -Math.cos(yawRad) * sideOffset;
      double sideZ = -Math.sin(yawRad) * sideOffset;
      double ox = backwardX * backDist + sideX;
      double oz = backwardZ * backDist + sideZ;
      double oy = (double)this.mc.player.getHeight() * (0.5 + (this.random.nextDouble() - 0.5) * 0.2 * (double)spreadMultiplier);
      Vec3d p = base.add(ox, oy, oz);
      Vec3d vel = new Vec3d((this.random.nextDouble() - 0.5) * 0.02, 0.01 + this.random.nextDouble() * 0.02, (this.random.nextDouble() - 0.5) * 0.02);
      long life = (long)this.prettyLifetime.getValue();
      float rot = this.random.nextFloat() * 360.0F;
      float rotSpeed = (this.random.nextFloat() - 0.5F) * 6.0F;
      int color = this.prettyRainbow.getValue()
         ? 0xFF000000 | Color.HSBtoRGB((float)(System.currentTimeMillis() % 3600L) / 3600.0F + this.random.nextFloat() * 0.1F, 0.7F, 1.0F) & 16777215
         : ColorProvider.getColorClient();
      this.prettyFigs.addLast(new Trails.PrettyFig(p, vel, life, rot, rotSpeed, color));
   }

   private void onRender(WorldRenderContext context) {
      if (this.isEnabled()) {
         MinecraftClient mc = MinecraftClient.getInstance();
         if (mc.player != null && mc.world != null && mc.player.isAlive()) {
            float tickDelta = context.tickCounter().getTickDelta(true);
            boolean selfVisible = this.firstPerson.getValue() || !mc.options.getPerspective().isFirstPerson();
            long currentTime = System.currentTimeMillis();
            boolean entityEnabled = this.mode.isEnabled("Entity Trails");
            boolean pearlEnabled = this.mode.isEnabled("Pearl Trail");
            boolean prettyEnabled = this.mode.isEnabled("Pretty");
            if (pearlEnabled) {
               this.renderPearlTrails(context, tickDelta, currentTime);
            }

            if (prettyEnabled) {
               this.renderPretty(context, tickDelta);
            }

            if (!entityEnabled) {
               if (!this.entityPoints.isEmpty()) {
                  this.entityPoints.clear();
                  this.lastPositions.clear();
                  this.lastPointTimes.clear();
                  this.entityTypes.clear();
               }
            } else {
               boolean scanNow = currentTime - this.lastEntityScan >= 15L;
               if (scanNow) {
                  this.lastEntityScan = currentTime;

                  for (Entity entity : mc.world.getEntities()) {
                     if (entity != null && entity.isAlive() && entity instanceof LivingEntity) {
                        boolean isTarget = false;
                        Trails.EntityType entityType = null;
                        if (entity == mc.player) {
                           isTarget = this.targetSelf.getValue() && selfVisible;
                           entityType = Trails.EntityType.SELF;
                        } else if (entity instanceof PlayerEntity) {
                           PlayerEntity player = (PlayerEntity)entity;
                           boolean isFriend = FriendRepository.isFriend(player.getNameForScoreboard());
                           if (isFriend) {
                              isTarget = this.targetFriends.getValue();
                              entityType = Trails.EntityType.FRIEND;
                           } else {
                              isTarget = this.targetPlayers.getValue();
                              entityType = Trails.EntityType.PLAYER;
                           }
                        } else if (entity instanceof MobEntity) {
                           isTarget = this.targetMobs.getValue();
                           entityType = Trails.EntityType.MOB;
                        }

                        if (!isTarget) {
                           this.removeEntityData(entity.getId());
                        } else {
                           this.entityTypes.put(entity.getId(), entityType);
                           Vec3d currentPos = entity.getLerpedPos(tickDelta);
                           long lastTime = this.lastPointTimes.getOrDefault(entity.getId(), 0L);
                           boolean shouldAddPoint = currentTime - lastTime >= 15L;
                           if (shouldAddPoint) {
                              Vec3d lastPos = this.lastPositions.get(entity.getId());
                              if (lastPos != null) {
                                 double distanceMoved = lastPos.distanceTo(currentPos);
                                 Deque<Trails.Point> existing = this.entityPoints.get(entity.getId());
                                 shouldAddPoint = distanceMoved > 0.01 || existing == null || existing.isEmpty();
                              }

                              if (shouldAddPoint) {
                                 Deque<Trails.Point> points = this.entityPoints.computeIfAbsent(entity.getId(), k -> new ArrayDeque<>());
                                 points.addFirst(new Trails.Point(currentPos, currentTime));
                                 this.lastPointTimes.put(entity.getId(), currentTime);
                                 this.lastPositions.put(entity.getId(), currentPos);
                              }
                           }
                        }
                     }
                  }
               }

               Iterator<Entry<Integer, Deque<Trails.Point>>> iterator = this.entityPoints.entrySet().iterator();

               while (iterator.hasNext()) {
                  Entry<Integer, Deque<Trails.Point>> entry = iterator.next();
                  Entity entityx = mc.world.getEntityById(entry.getKey());
                  if (entityx != null && entityx.isAlive()) {
                     Deque<Trails.Point> points = entry.getValue();
                     points.removeIf(point -> (float)(currentTime - point.timestamp) > (float)this.duration.getValue());
                     if (points.isEmpty()) {
                        iterator.remove();
                        this.removeEntityData(entry.getKey());
                     }
                  } else {
                     iterator.remove();
                     this.removeEntityData(entry.getKey());
                  }
               }

               for (Entry<Integer, Deque<Trails.Point>> entry : this.entityPoints.entrySet()) {
                  Deque<Trails.Point> points = entry.getValue();
                  if (points.size() >= 2) {
                     Entity entityx = mc.world.getEntityById(entry.getKey());
                     if (entityx != null && entityx.isAlive()) {
                        Trails.EntityType entityTypex = this.entityTypes.get(entry.getKey());
                        this.renderTrail(points, context, currentTime, entityx.getHeight(), entityTypex);
                     }
                  }
               }
            }
         } else {
            if (!this.entityPoints.isEmpty()) {
               this.clearData();
            }
         }
      }
   }

   private void renderPretty(WorldRenderContext context, float tickDelta) {
      if (!this.prettyFigs.isEmpty()) {
         Camera camera = context.camera();
         Vec3d camPos = camera.getPos();
         MatrixStack stack = context.matrixStack();
         boolean glow = this.prettyGlow.getValue();
         float lineLength = this.prettyLineLength.getFloatValue();
         float lineWidth = 0.05F;
         long now = System.currentTimeMillis();
         RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
         RenderSystem.disableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.setShaderTexture(0, Identifier.of("mre", "images/glow.png"));
         BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
         boolean hasAnyQuads = false;

         for (Trails.PrettyFig f : this.prettyFigs) {
            float life = (float)(now - f.spawnTime) / (float)f.lifeMs;
            if (!(life >= 1.0F)) {
               float alpha = life < 0.2F ? life * 5.0F : 1.0F - (life - 0.2F) / 0.8F;
               if (!(alpha <= 0.01F)) {
                  double px = MathHelper.lerp((double)tickDelta, f.prevX, f.x) - camPos.x;
                  double py = MathHelper.lerp((double)tickDelta, f.prevY, f.y) - camPos.y;
                  double pz = MathHelper.lerp((double)tickDelta, f.prevZ, f.z) - camPos.z;
                  float r = (float)ColorProvider.red(f.color) / 255.0F;
                  float g = (float)ColorProvider.green(f.color) / 255.0F;
                  float b = (float)ColorProvider.blue(f.color) / 255.0F;
                  stack.push();
                  stack.translate(px, py, pz);
                  stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
                  stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                  stack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(f.rotation + f.rotSpeed * tickDelta));
                  Matrix4f m = stack.peek().getPositionMatrix();
                  if (glow) {
                     float glowSize = lineWidth * 4.0F;
                     float glowHeight = lineLength * 1.5F;
                     this.putLineQuad(buffer, m, glowSize, glowHeight, r, g, b, alpha * 0.3F);
                     hasAnyQuads = true;
                  }

                  this.putLineQuad(buffer, m, lineWidth, lineLength, r, g, b, alpha);
                  hasAnyQuads = true;
                  stack.pop();
               }
            }
         }

         if (hasAnyQuads) {
            BufferRenderer.drawWithGlobalProgram(buffer.end());
         }

         RenderSystem.depthMask(true);
         RenderSystem.enableCull();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableBlend();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         RenderSystem.setShaderTexture(0, 0);
      }
   }

   private void putLineQuad(BufferBuilder buffer, Matrix4f m, float width, float height, float r, float g, float b, float alpha) {
      int ri = (int)(r * 255.0F);
      int gi = (int)(g * 255.0F);
      int bi = (int)(b * 255.0F);
      int ai = (int)(MathHelper.clamp(alpha, 0.0F, 1.0F) * 255.0F);
      float halfWidth = width / 2.0F;
      float halfHeight = height / 2.0F;
      buffer.vertex(m, -halfWidth, halfHeight, 0.0F).texture(0.0F, 0.0F).color(ri, gi, bi, ai);
      buffer.vertex(m, halfWidth, halfHeight, 0.0F).texture(1.0F, 0.0F).color(ri, gi, bi, ai);
      buffer.vertex(m, halfWidth, -halfHeight, 0.0F).texture(1.0F, 1.0F).color(ri, gi, bi, ai);
      buffer.vertex(m, -halfWidth, -halfHeight, 0.0F).texture(0.0F, 1.0F).color(ri, gi, bi, ai);
   }

   private void renderPearlTrails(WorldRenderContext context, float tickDelta, long now) {
      int color = 0xFF000000 | ColorProvider.getColorClient() & 16777215;

      for (Entity entity : MinecraftClient.getInstance().world.getEntities()) {
         if (entity instanceof EnderPearlEntity pearl && pearl.isAlive()) {
            Vec3d position = pearl.getLerpedPos(tickDelta);
            Vec3d velocity = pearl.getVelocity();

            for (int i = 0; i < 4; i++) {
               Vec3d framePosition = position.subtract(velocity.multiply((double)i * 0.45));
               long frameAge = (now + (long)i * 120L) % 600L;
               MasEffectRenderer.render(
                  "pearl_trail", context.matrixStack(), context.camera(), framePosition, frameAge, 600L, 0.32F * (1.0F - (float)i * 0.14F), color
               );
            }
         }
      }
   }

   private void renderTrail(Deque<Trails.Point> points, WorldRenderContext context, long currentTime, float entityHeight, Trails.EntityType entityType) {
      MatrixStack matrixStack = context.matrixStack();
      Vec3d camPos = context.camera().getPos();
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA);
      RenderSystem.disableCull();
      RenderSystem.enableDepthTest();
      RenderSystem.depthMask(false);
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      Tessellator tessellator = Tessellator.getInstance();
      matrixStack.push();
      matrixStack.translate(-camPos.x, -camPos.y, -camPos.z);
      Matrix4f matrix = matrixStack.peek().getPositionMatrix();
      BufferBuilder buffer = tessellator.begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
      int index = 0;

      for (Trails.Point point : points) {
         float lifeRatio = Math.min(1.0F, (float)((double)(currentTime - point.timestamp) / this.duration.getValue()));
         float alpha = (float)(this.opacity.getValue() / 255.0 * (double)(1.0F - this.easeOutQuad(lifeRatio)));
         int color = this.getColorForEntity(entityType, index);
         float r = (float)(color >> 16 & 0xFF) / 255.0F;
         float g = (float)(color >> 8 & 0xFF) / 255.0F;
         float b = (float)(color & 0xFF) / 255.0F;
         buffer.vertex(matrix, (float)point.pos.x, (float)point.pos.y, (float)point.pos.z).color(r, g, b, alpha);
         buffer.vertex(matrix, (float)point.pos.x, (float)(point.pos.y + (double)entityHeight - 0.1), (float)point.pos.z)
            .color(r, g, b, alpha);
         index++;
      }

      BufferRenderer.drawWithGlobalProgram(buffer.end());
      RenderSystem.lineWidth((float)this.lineWidth.getValue());
      BufferBuilder bufferLineBottom = tessellator.begin(DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
      index = 0;

      for (Trails.Point point : points) {
         float lifeRatio = Math.min(1.0F, (float)((double)(currentTime - point.timestamp) / this.duration.getValue()));
         float alpha = (float)(this.opacity.getValue() / 255.0 * (double)(1.0F - this.easeOutQuad(lifeRatio)));
         int color = this.getColorForEntity(entityType, index);
         float r = (float)(color >> 16 & 0xFF) / 255.0F;
         float g = (float)(color >> 8 & 0xFF) / 255.0F;
         float b = (float)(color & 0xFF) / 255.0F;
         bufferLineBottom.vertex(matrix, (float)point.pos.x, (float)point.pos.y, (float)point.pos.z)
            .color(r, g, b, alpha);
         index++;
      }

      BufferRenderer.drawWithGlobalProgram(bufferLineBottom.end());
      BufferBuilder bufferLineTop = tessellator.begin(DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
      index = 0;

      for (Trails.Point point : points) {
         float lifeRatio = Math.min(1.0F, (float)((double)(currentTime - point.timestamp) / this.duration.getValue()));
         float alpha = (float)(this.opacity.getValue() / 255.0 * (double)(1.0F - this.easeOutQuad(lifeRatio)));
         int color = this.getColorForEntity(entityType, index);
         float r = (float)(color >> 16 & 0xFF) / 255.0F;
         float g = (float)(color >> 8 & 0xFF) / 255.0F;
         float b = (float)(color & 0xFF) / 255.0F;
         bufferLineTop.vertex(
               matrix, (float)point.pos.x, (float)(point.pos.y + (double)entityHeight - 0.1), (float)point.pos.z
            )
            .color(r, g, b, alpha);
         index++;
      }

      BufferRenderer.drawWithGlobalProgram(bufferLineTop.end());
      matrixStack.pop();
      RenderSystem.enableCull();
      RenderSystem.depthMask(true);
      RenderSystem.disableBlend();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   private int getColorForEntity(Trails.EntityType entityType, int index) {
      int clientColor = ColorProvider.getColorClient();
      if (entityType == null) {
         return clientColor;
      } else {
         switch (entityType) {
            case SELF:
            case FRIEND:
            default:
               return clientColor;
            case PLAYER:
               return new Color(255, 85, 85).getRGB();
            case MOB:
               return new Color(255, 170, 0).getRGB();
         }
      }
   }

   private float easeOutQuad(float x) {
      return 1.0F - (1.0F - x) * (1.0F - x);
   }

   private void removeEntityData(int entityId) {
      this.lastPositions.remove(entityId);
      this.lastPointTimes.remove(entityId);
      this.entityTypes.remove(entityId);
   }

   private static enum EntityType {
      SELF,
      FRIEND,
      PLAYER,
      MOB;
   }

   public static class Point {
      public final Vec3d pos;
      public final long timestamp;

      public Point(Vec3d pos, long timestamp) {
         this.pos = pos;
         this.timestamp = timestamp;
      }
   }

   private static final class PrettyFig {
      double x;
      double y;
      double z;
      double prevX;
      double prevY;
      double prevZ;
      double vx;
      double vy;
      double vz;
      final long spawnTime = System.currentTimeMillis();
      final long lifeMs;
      float rotation;
      final float rotSpeed;
      final int color;

      PrettyFig(Vec3d pos, Vec3d vel, long lifeMs, float rotation, float rotSpeed, int color) {
         this.x = pos.x;
         this.y = pos.y;
         this.z = pos.z;
         this.prevX = this.x;
         this.prevY = this.y;
         this.prevZ = this.z;
         this.vx = vel.x;
         this.vy = vel.y;
         this.vz = vel.z;
         this.lifeMs = lifeMs;
         this.rotation = rotation;
         this.rotSpeed = rotSpeed;
         this.color = color;
      }

      void tick() {
         this.prevX = this.x;
         this.prevY = this.y;
         this.prevZ = this.z;
         this.x = this.x + this.vx;
         this.y = this.y + this.vy;
         this.z = this.z + this.vz;
         this.vy *= 0.96;
         this.vx *= 0.98;
         this.vz *= 0.98;
         this.rotation = this.rotation + this.rotSpeed;
      }
   }
}
