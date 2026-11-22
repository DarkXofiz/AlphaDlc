package zov.alphadlc.module.list.render;

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
import zov.alphadlc.event.list.EventTick;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.module.settings.ModeListSetting;
import zov.alphadlc.module.settings.SliderSetting;
import zov.alphadlc.util.friend.FriendRepository;
import zov.alphadlc.util.render.effects.MasEffectRenderer;
import zov.alphadlc.util.render.providers.ColorProvider;

@ModuleInformation(
   moduleName = "Trails",
   moduleDesc = "\u041e\u0441\u0442\u0430\u0432\u043b\u044f\u0435\u0442 \u0441\u043b\u0435\u0434 \u0437\u0430 \u0441\u0443\u0449\u043d\u043e\u0441\u0442\u044f\u043c\u0438",
   moduleCategory = ModuleCategory.RENDER
)
public class Trails extends Module {
   private static final int PRETTY_HARD_CAP = 1000;
   private final ModeListSetting mode = new ModeListSetting(
      "\u0420\u0435\u0436\u0438\u043c",
      new BooleanSetting("Entity Trails", true),
      new BooleanSetting("Pearl Trail", false),
      new BooleanSetting("\u041a\u0440\u0430\u0441\u0438\u0432\u044b\u0439", false)
   );
   private final SliderSetting duration = new SliderSetting(
      "\u0414\u043b\u0438\u0442\u0435\u043b\u044c\u043d\u043e\u0441\u0442\u044c", 1000.0, 500.0, 2000.0, 100.0
   );
   private final SliderSetting lineWidth = new SliderSetting("\u0422\u043e\u043b\u0449\u0438\u043d\u0430", 2.0, 0.5, 5.0, 0.1F);
   private final SliderSetting opacity = new SliderSetting(
      "\u041d\u0435\u043f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c", 100.0, 0.0, 255.0, 5.0
   );
   private final BooleanSetting firstPerson = new BooleanSetting("\u041e\u0442 \u043f\u0435\u0440\u0432\u043e\u0433\u043e \u043b\u0438\u0446\u0430", false);
   private final BooleanSetting targetSelf = new BooleanSetting("\u0421\u0435\u0431\u0435", true);
   private final BooleanSetting targetFriends = new BooleanSetting("\u041d\u0430 \u0434\u0440\u0443\u0437\u044c\u044f\u0445", true);
   private final BooleanSetting targetPlayers = new BooleanSetting("\u041d\u0430 \u0438\u0433\u0440\u043e\u043a\u0430\u0445", true);
   private final BooleanSetting targetMobs = new BooleanSetting("\u041d\u0430 \u043c\u043e\u0431\u0430\u0445", false);
   private final SliderSetting prettyCount = new SliderSetting(
         "\u041a\u043e\u043b-\u0432\u043e \u043f\u0430\u043b\u043e\u0447\u0435\u043a", 2.0, 1.0, 10.0, 1.0
      )
      .setVisible(() -> this.mode.isEnabled("\u041a\u0440\u0430\u0441\u0438\u0432\u044b\u0439"));
   private final SliderSetting prettyLifetime = new SliderSetting(
         "\u0414\u043b\u0438\u0442\u0435\u043b\u044c\u043d\u043e\u0441\u0442\u044c (\u043c\u0441)", 1800.0, 500.0, 5000.0, 100.0
      )
      .setVisible(() -> this.mode.isEnabled("\u041a\u0440\u0430\u0441\u0438\u0432\u044b\u0439"));
   private final SliderSetting prettySpread = new SliderSetting(
         "\u0421\u0431\u043b\u0438\u0436\u0435\u043d\u043d\u043e\u0441\u0442\u044c", 0.4F, 0.1F, 1.5, 0.1F
      )
      .setVisible(() -> this.mode.isEnabled("\u041a\u0440\u0430\u0441\u0438\u0432\u044b\u0439"));
   private final SliderSetting prettyLineLength = new SliderSetting(
         "\u0414\u043b\u0438\u043d\u0430 \u043f\u0430\u043b\u043e\u0447\u043a\u0438", 0.3F, 0.1F, 1.0, 0.05F
      )
      .setVisible(() -> this.mode.isEnabled("\u041a\u0440\u0430\u0441\u0438\u0432\u044b\u0439"));
   private final BooleanSetting prettyGlow = new BooleanSetting(
         "\u0421\u0432\u0435\u0447\u0435\u043d\u0438\u0435 \u043f\u0430\u043b\u043e\u0447\u0435\u043a", true
      )
      .setVisible(() -> this.mode.isEnabled("\u041a\u0440\u0430\u0441\u0438\u0432\u044b\u0439"));
   private final BooleanSetting prettyRainbow = new BooleanSetting("\u0420\u0430\u0434\u0443\u0436\u043d\u044b\u0439 \u0446\u0432\u0435\u0442", false)
      .setVisible(() -> this.mode.isEnabled("\u041a\u0440\u0430\u0441\u0438\u0432\u044b\u0439"));
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
      if (this.isEnabled() && this.mc.field_1724 != null && this.mc.field_1687 != null) {
         if (this.mode.isEnabled("\u041a\u0440\u0430\u0441\u0438\u0432\u044b\u0439")) {
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

      boolean shouldShow = this.firstPerson.getValue() || !this.mc.field_1690.method_31044().method_31034();
      if (shouldShow) {
         Vec3d pos = this.mc.field_1724.method_19538();
         Vec3d vel = this.mc.field_1724.method_18798();
         double horizontalSpeed = Math.sqrt(vel.field_1352 * vel.field_1352 + vel.field_1350 * vel.field_1350);
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
      float yaw = this.mc.field_1724.method_36454();
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
      double oy = (double)this.mc.field_1724.method_17682() * (0.5 + (this.random.nextDouble() - 0.5) * 0.2 * (double)spreadMultiplier);
      Vec3d p = base.method_1031(ox, oy, oz);
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
         MinecraftClient mc = MinecraftClient.method_1551();
         if (mc.field_1724 != null && mc.field_1687 != null && mc.field_1724.method_5805()) {
            float tickDelta = context.tickCounter().method_60637(true);
            boolean selfVisible = this.firstPerson.getValue() || !mc.field_1690.method_31044().method_31034();
            long currentTime = System.currentTimeMillis();
            boolean entityEnabled = this.mode.isEnabled("Entity Trails");
            boolean pearlEnabled = this.mode.isEnabled("Pearl Trail");
            boolean prettyEnabled = this.mode.isEnabled("\u041a\u0440\u0430\u0441\u0438\u0432\u044b\u0439");
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

                  for (Entity entity : mc.field_1687.method_18112()) {
                     if (entity != null && entity.method_5805() && entity instanceof LivingEntity) {
                        boolean isTarget = false;
                        Trails.EntityType entityType = null;
                        if (entity == mc.field_1724) {
                           isTarget = this.targetSelf.getValue() && selfVisible;
                           entityType = Trails.EntityType.SELF;
                        } else if (entity instanceof PlayerEntity) {
                           PlayerEntity player = (PlayerEntity)entity;
                           boolean isFriend = FriendRepository.isFriend(player.method_5820());
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
                           this.removeEntityData(entity.method_5628());
                        } else {
                           this.entityTypes.put(entity.method_5628(), entityType);
                           Vec3d currentPos = entity.method_30950(tickDelta);
                           long lastTime = this.lastPointTimes.getOrDefault(entity.method_5628(), 0L);
                           boolean shouldAddPoint = currentTime - lastTime >= 15L;
                           if (shouldAddPoint) {
                              Vec3d lastPos = this.lastPositions.get(entity.method_5628());
                              if (lastPos != null) {
                                 double distanceMoved = lastPos.method_1022(currentPos);
                                 Deque<Trails.Point> existing = this.entityPoints.get(entity.method_5628());
                                 shouldAddPoint = distanceMoved > 0.01 || existing == null || existing.isEmpty();
                              }

                              if (shouldAddPoint) {
                                 Deque<Trails.Point> points = this.entityPoints.computeIfAbsent(entity.method_5628(), k -> new ArrayDeque<>());
                                 points.addFirst(new Trails.Point(currentPos, currentTime));
                                 this.lastPointTimes.put(entity.method_5628(), currentTime);
                                 this.lastPositions.put(entity.method_5628(), currentPos);
                              }
                           }
                        }
                     }
                  }
               }

               Iterator<Entry<Integer, Deque<Trails.Point>>> iterator = this.entityPoints.entrySet().iterator();

               while (iterator.hasNext()) {
                  Entry<Integer, Deque<Trails.Point>> entry = iterator.next();
                  Entity entityx = mc.field_1687.method_8469(entry.getKey());
                  if (entityx != null && entityx.method_5805()) {
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
                     Entity entityx = mc.field_1687.method_8469(entry.getKey());
                     if (entityx != null && entityx.method_5805()) {
                        Trails.EntityType entityTypex = this.entityTypes.get(entry.getKey());
                        this.renderTrail(points, context, currentTime, entityx.method_17682(), entityTypex);
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
         Vec3d camPos = camera.method_19326();
         MatrixStack stack = context.matrixStack();
         boolean glow = this.prettyGlow.getValue();
         float lineLength = this.prettyLineLength.getFloatValue();
         float lineWidth = 0.05F;
         long now = System.currentTimeMillis();
         RenderSystem.setShader(ShaderProgramKeys.field_53880);
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
         RenderSystem.disableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.setShaderTexture(0, Identifier.method_60655("mre", "images/glow.png"));
         BufferBuilder buffer = Tessellator.method_1348().method_60827(DrawMode.field_27382, VertexFormats.field_1575);
         boolean hasAnyQuads = false;

         for (Trails.PrettyFig f : this.prettyFigs) {
            float life = (float)(now - f.spawnTime) / (float)f.lifeMs;
            if (!(life >= 1.0F)) {
               float alpha = life < 0.2F ? life * 5.0F : 1.0F - (life - 0.2F) / 0.8F;
               if (!(alpha <= 0.01F)) {
                  double px = MathHelper.method_16436((double)tickDelta, f.prevX, f.x) - camPos.field_1352;
                  double py = MathHelper.method_16436((double)tickDelta, f.prevY, f.y) - camPos.field_1351;
                  double pz = MathHelper.method_16436((double)tickDelta, f.prevZ, f.z) - camPos.field_1350;
                  float r = (float)ColorProvider.red(f.color) / 255.0F;
                  float g = (float)ColorProvider.green(f.color) / 255.0F;
                  float b = (float)ColorProvider.blue(f.color) / 255.0F;
                  stack.method_22903();
                  stack.method_22904(px, py, pz);
                  stack.method_22907(RotationAxis.field_40716.rotationDegrees(-camera.method_19330()));
                  stack.method_22907(RotationAxis.field_40714.rotationDegrees(camera.method_19329()));
                  stack.method_22907(RotationAxis.field_40718.rotationDegrees(f.rotation + f.rotSpeed * tickDelta));
                  Matrix4f m = stack.method_23760().method_23761();
                  if (glow) {
                     float glowSize = lineWidth * 4.0F;
                     float glowHeight = lineLength * 1.5F;
                     this.putLineQuad(buffer, m, glowSize, glowHeight, r, g, b, alpha * 0.3F);
                     hasAnyQuads = true;
                  }

                  this.putLineQuad(buffer, m, lineWidth, lineLength, r, g, b, alpha);
                  hasAnyQuads = true;
                  stack.method_22909();
               }
            }
         }

         if (hasAnyQuads) {
            BufferRenderer.method_43433(buffer.method_60800());
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
      int ai = (int)(MathHelper.method_15363(alpha, 0.0F, 1.0F) * 255.0F);
      float halfWidth = width / 2.0F;
      float halfHeight = height / 2.0F;
      buffer.method_22918(m, -halfWidth, halfHeight, 0.0F).method_22913(0.0F, 0.0F).method_1336(ri, gi, bi, ai);
      buffer.method_22918(m, halfWidth, halfHeight, 0.0F).method_22913(1.0F, 0.0F).method_1336(ri, gi, bi, ai);
      buffer.method_22918(m, halfWidth, -halfHeight, 0.0F).method_22913(1.0F, 1.0F).method_1336(ri, gi, bi, ai);
      buffer.method_22918(m, -halfWidth, -halfHeight, 0.0F).method_22913(0.0F, 1.0F).method_1336(ri, gi, bi, ai);
   }

   private void renderPearlTrails(WorldRenderContext context, float tickDelta, long now) {
      int color = 0xFF000000 | ColorProvider.getColorClient() & 16777215;

      for (Entity entity : MinecraftClient.method_1551().field_1687.method_18112()) {
         if (entity instanceof EnderPearlEntity pearl && pearl.method_5805()) {
            Vec3d position = pearl.method_30950(tickDelta);
            Vec3d velocity = pearl.method_18798();

            for (int i = 0; i < 4; i++) {
               Vec3d framePosition = position.method_1020(velocity.method_1021((double)i * 0.45));
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
      Vec3d camPos = context.camera().method_19326();
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA);
      RenderSystem.disableCull();
      RenderSystem.enableDepthTest();
      RenderSystem.depthMask(false);
      RenderSystem.setShader(ShaderProgramKeys.field_53876);
      Tessellator tessellator = Tessellator.method_1348();
      matrixStack.method_22903();
      matrixStack.method_22904(-camPos.field_1352, -camPos.field_1351, -camPos.field_1350);
      Matrix4f matrix = matrixStack.method_23760().method_23761();
      BufferBuilder buffer = tessellator.method_60827(DrawMode.field_27380, VertexFormats.field_1576);
      int index = 0;

      for (Trails.Point point : points) {
         float lifeRatio = Math.min(1.0F, (float)((double)(currentTime - point.timestamp) / this.duration.getValue()));
         float alpha = (float)(this.opacity.getValue() / 255.0 * (double)(1.0F - this.easeOutQuad(lifeRatio)));
         int color = this.getColorForEntity(entityType, index);
         float r = (float)(color >> 16 & 0xFF) / 255.0F;
         float g = (float)(color >> 8 & 0xFF) / 255.0F;
         float b = (float)(color & 0xFF) / 255.0F;
         buffer.method_22918(matrix, (float)point.pos.field_1352, (float)point.pos.field_1351, (float)point.pos.field_1350).method_22915(r, g, b, alpha);
         buffer.method_22918(matrix, (float)point.pos.field_1352, (float)(point.pos.field_1351 + (double)entityHeight - 0.1), (float)point.pos.field_1350)
            .method_22915(r, g, b, alpha);
         index++;
      }

      BufferRenderer.method_43433(buffer.method_60800());
      RenderSystem.lineWidth((float)this.lineWidth.getValue());
      BufferBuilder bufferLineBottom = tessellator.method_60827(DrawMode.field_29345, VertexFormats.field_1576);
      index = 0;

      for (Trails.Point point : points) {
         float lifeRatio = Math.min(1.0F, (float)((double)(currentTime - point.timestamp) / this.duration.getValue()));
         float alpha = (float)(this.opacity.getValue() / 255.0 * (double)(1.0F - this.easeOutQuad(lifeRatio)));
         int color = this.getColorForEntity(entityType, index);
         float r = (float)(color >> 16 & 0xFF) / 255.0F;
         float g = (float)(color >> 8 & 0xFF) / 255.0F;
         float b = (float)(color & 0xFF) / 255.0F;
         bufferLineBottom.method_22918(matrix, (float)point.pos.field_1352, (float)point.pos.field_1351, (float)point.pos.field_1350)
            .method_22915(r, g, b, alpha);
         index++;
      }

      BufferRenderer.method_43433(bufferLineBottom.method_60800());
      BufferBuilder bufferLineTop = tessellator.method_60827(DrawMode.field_29345, VertexFormats.field_1576);
      index = 0;

      for (Trails.Point point : points) {
         float lifeRatio = Math.min(1.0F, (float)((double)(currentTime - point.timestamp) / this.duration.getValue()));
         float alpha = (float)(this.opacity.getValue() / 255.0 * (double)(1.0F - this.easeOutQuad(lifeRatio)));
         int color = this.getColorForEntity(entityType, index);
         float r = (float)(color >> 16 & 0xFF) / 255.0F;
         float g = (float)(color >> 8 & 0xFF) / 255.0F;
         float b = (float)(color & 0xFF) / 255.0F;
         bufferLineTop.method_22918(
               matrix, (float)point.pos.field_1352, (float)(point.pos.field_1351 + (double)entityHeight - 0.1), (float)point.pos.field_1350
            )
            .method_22915(r, g, b, alpha);
         index++;
      }

      BufferRenderer.method_43433(bufferLineTop.method_60800());
      matrixStack.method_22909();
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
         this.x = pos.field_1352;
         this.y = pos.field_1351;
         this.z = pos.field_1350;
         this.prevX = this.x;
         this.prevY = this.y;
         this.prevZ = this.z;
         this.vx = vel.field_1352;
         this.vy = vel.field_1351;
         this.vz = vel.field_1350;
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
