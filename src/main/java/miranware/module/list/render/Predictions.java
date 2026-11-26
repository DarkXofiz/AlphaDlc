package miranware.module.list.render;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;
import net.minecraft.block.Blocks;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext.ShapeType;
import org.joml.Matrix4f;
import miranware.event.list.EventHUD;
import miranware.event.list.EventWorldRender;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.BooleanSetting;
import miranware.module.settings.ModeSetting;
import miranware.util.player.combat.RaytraceUtil;
import miranware.util.player.other.WorldUtils;
import miranware.util.render.math.ProjectionUtil;
import miranware.util.render.msdf.Fonts;
import miranware.util.render.providers.ColorProvider;
import miranware.util.render.renderers.DrawUtil;

@ModuleInformation(
   moduleName = "Predictions",
   moduleDesc = "Shows the flight path of items",
   moduleCategory = ModuleCategory.RENDER
)
public class Predictions extends Module {
   private static final Identifier GLOW = Identifier.of("mre", "images/glow.png");
   private final ModeSetting renderMode = new ModeSetting(
      "Render Mode", "Default", "Default", "Glow"
   );
   private final BooleanSetting walls = new BooleanSetting("Through Walls", true);
   private final BooleanSetting showArrows = new BooleanSetting("Arrows", true);
   private final BooleanSetting showTridents = new BooleanSetting("Tridents", true);
   private final BooleanSetting showThrownItems = new BooleanSetting("Ender Pearl", true);
   private final BooleanSetting showDroppedItems = new BooleanSetting("Items", false);
   private final List<Predictions.Point> points = new ArrayList<>();

   @Subscribe
   public void onDraw(EventHUD e) {
      for (Predictions.Point point : this.points) {
         Vector2f vec2f = ProjectionUtil.project(point.pos);
         int ticks = point.ticks;
         double time = (double)(ticks * 50) / 1000.0;
         String text = String.format("%.1f", time) + " sec";
         float textWidth = Fonts.SFREGULAR.get().getWidth(text, 7.0F);
         float centerX = vec2f.getX();
         float centerY = vec2f.getY();
         float totalHeight = 5.75F;
         float rectX = centerX - textWidth / 2.0F;
         float rectY = centerY - totalHeight / 2.0F;
         float textY = rectY + 5.0F;
         DrawUtil.drawRound(rectX - 7.0F, textY - 2.0F, textWidth + 14.75F, totalHeight + 5.0F, 0.0F, ColorProvider.rgba(0, 0, 0, 120.0F));
         e.getDrawContext().getMatrices().push();
         e.getDrawContext().getMatrices().translate(rectX - 5.0F, textY - 0.75F, 0.0F);
         e.getDrawContext().getMatrices().scale(0.5F, 0.5F, 1.0F);
         e.getDrawContext().drawItem(point.stack(), 0, 0);
         e.getDrawContext().getMatrices().scale(1.0F, 1.0F, 1.0F);
         e.getDrawContext().getMatrices().translate(-(rectX - 5.0F), -(textY - 0.75F), 0.0F);
         e.getDrawContext().getMatrices().pop();
         DrawUtil.drawText(Fonts.SFREGULAR.get(), text.replace(",", "."), rectX + 4.5F, textY - 0.5F, ColorProvider.rgba(255, 255, 255, 255.0F), 6.75F);
      }
   }

   @Subscribe
   public void onWorldRender(EventWorldRender e) {
      if (this.mc.player != null && this.mc.world != null) {
         this.points.clear();
         float tickDelta = e.getTickDelta();
         long now = System.currentTimeMillis();
         MatrixStack matrices = e.getMatrixStack();
         Camera camera = this.mc.gameRenderer.getCamera();
         boolean depth = !this.walls.getValue();
         if (this.renderMode.is("Glow")) {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
            RenderSystem.setShaderTexture(0, GLOW);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

            for (Entity entity : this.getProjectiles()) {
               Predictions.SimulatedPath simulated = this.simulatePath(entity, tickDelta);
               if (simulated.rawPoints.size() >= 2) {
                  List<Vec3d> smoothPath = this.smoothPath(simulated.rawPoints);
                  if (smoothPath.size() >= 2) {
                     int startColor = ColorProvider.getColorClient();
                     int endColor = ColorProvider.getThemeColorTwo();
                     this.renderGlowPath(buffer, matrices, camera, smoothPath, startColor, endColor, now);
                     if (simulated.hitPos != null) {
                        this.renderImpactGlow(buffer, matrices, camera, simulated.hitPos, endColor, now);
                        this.breakingBad(entity, simulated.hitPos, simulated.hitTicks);
                     }
                  }
               }
            }

            try {
               BufferRenderer.drawWithGlobalProgram(buffer.end());
            } catch (Exception var15) {
            }

            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
         } else {
            this.getProjectiles().forEach(entityx -> {
               Vec3d motion = entityx.getVelocity();
               Vec3d pos = entityx.getPos();
               int ticks = 0;

               for (int i = 0; i < 300; i++) {
                  Vec3d prevPos = pos;
                  pos = pos.add(motion);
                  motion = this.calculateMotion(entityx, prevPos, motion);
                  HitResult result = RaytraceUtil.raycast(prevPos, pos, ShapeType.COLLIDER, entityx);
                  if (!result.getType().equals(Type.MISS)) {
                     pos = result.getPos();
                  }

                  float alpha = MathHelper.clamp((float)i / 25.0F, 0.0F, 1.0F) * 255.0F;
                  int baseColor = ColorProvider.setAlpha(ColorProvider.getColorClient(), (double)alpha);
                  DrawUtil.drawLine(prevPos, pos, baseColor, 2.0F, depth);
                  Vec3d finalPos = pos;
                  boolean inEntity = StreamSupport.<Entity>stream(this.mc.world.getEntities().spliterator(), false).filter(ent -> {
                     if (ent instanceof LivingEntity living && living != this.mc.player && living.isAlive()) {
                        return true;
                     }

                     return false;
                  }).anyMatch(ent -> ent.getBoundingBox().expand(0.25).intersects(prevPos, finalPos));
                  if (result.getType().equals(Type.BLOCK) || pos.y < -128.0 || inEntity || result.getType().equals(Type.ENTITY)) {
                     this.breakingBad(entityx, pos, ticks);
                     break;
                  }

                  ticks++;
               }
            });
         }
      }
   }

   private Predictions.SimulatedPath simulatePath(Entity entity, float tickDelta) {
      List<Vec3d> path = new ArrayList<>();
      Vec3d renderPos = this.getInterpolatedPos(entity, tickDelta);
      Vec3d pos = entity.getPos();
      Vec3d motion = entity.getVelocity();
      Vec3d hitPos = null;
      int hitTicks = 0;
      this.addIfFar(path, renderPos);
      if (renderPos.squaredDistanceTo(pos) > 1.0E-5) {
         this.addIfFar(path, renderPos.lerp(pos, 0.35));
         this.addIfFar(path, renderPos.lerp(pos, 0.7));
      }

      this.addIfFar(path, pos);

      for (int i = 0; i < 220; i++) {
         Vec3d prevPos = pos;
         pos = pos.add(motion);
         motion = this.calculateMotion(entity, prevPos, motion);
         HitResult result = RaytraceUtil.raycast(prevPos, pos, ShapeType.COLLIDER, entity);
         if (result.getType() != Type.MISS) {
            pos = result.getPos();
         }

         this.addIfFar(path, pos);
         boolean hitEntity = this.hitsEntity(entity, prevPos, pos);
         if (result.getType() == Type.BLOCK || result.getType() == Type.ENTITY || hitEntity || pos.y < -128.0) {
            hitPos = pos;
            hitTicks = i + 1;
            break;
         }
      }

      return new Predictions.SimulatedPath(path, hitPos, hitTicks);
   }

   private Vec3d getInterpolatedPos(Entity entity, float tickDelta) {
      return new Vec3d(
         MathHelper.lerp((double)tickDelta, entity.prevX, entity.getX()),
         MathHelper.lerp((double)tickDelta, entity.prevY, entity.getY()),
         MathHelper.lerp((double)tickDelta, entity.prevZ, entity.getZ())
      );
   }

   private void addIfFar(List<Vec3d> list, Vec3d point) {
      if (list.isEmpty() || list.get(list.size() - 1).squaredDistanceTo(point) > 1.0E-6) {
         list.add(point);
      }
   }

   private List<Vec3d> smoothPath(List<Vec3d> raw) {
      if (raw.size() < 2) {
         return raw;
      } else {
         List<Vec3d> out = new ArrayList<>();
         out.add(raw.get(0));

         for (int i = 0; i < raw.size() - 1; i++) {
            Vec3d p0 = raw.get(Math.max(0, i - 1));
            Vec3d p1 = raw.get(i);
            Vec3d p2 = raw.get(i + 1);
            Vec3d p3 = raw.get(Math.min(raw.size() - 1, i + 2));
            double dist = p1.distanceTo(p2);
            int steps = MathHelper.clamp((int)Math.ceil(dist / 0.08), 3, 8);

            for (int j = 1; j <= steps; j++) {
               float t = (float)j / (float)steps;
               out.add(this.catmullRom(p0, p1, p2, p3, t));
            }
         }

         return out;
      }
   }

   private Vec3d catmullRom(Vec3d p0, Vec3d p1, Vec3d p2, Vec3d p3, float t) {
      double t2 = (double)(t * t);
      double t3 = t2 * (double)t;
      double x = 0.5
         * (
            2.0 * p1.x
               + (-p0.x + p2.x) * (double)t
               + (2.0 * p0.x - 5.0 * p1.x + 4.0 * p2.x - p3.x) * t2
               + (-p0.x + 3.0 * p1.x - 3.0 * p2.x + p3.x) * t3
         );
      double y = 0.5
         * (
            2.0 * p1.y
               + (-p0.y + p2.y) * (double)t
               + (2.0 * p0.y - 5.0 * p1.y + 4.0 * p2.y - p3.y) * t2
               + (-p0.y + 3.0 * p1.y - 3.0 * p2.y + p3.y) * t3
         );
      double z = 0.5
         * (
            2.0 * p1.z
               + (-p0.z + p2.z) * (double)t
               + (2.0 * p0.z - 5.0 * p1.z + 4.0 * p2.z - p3.z) * t2
               + (-p0.z + 3.0 * p1.z - 3.0 * p2.z + p3.z) * t3
         );
      return new Vec3d(x, y, z);
   }

   private boolean hitsEntity(Entity projectile, Vec3d from, Vec3d to) {
      return this.mc.world == null ? false : !this.mc.world.getOtherEntities(projectile, new Box(from, to).expand(0.3), ent -> {
         if (ent instanceof LivingEntity living && living != this.mc.player && living.isAlive()) {
            return true;
         }

         return false;
      }).isEmpty();
   }

   private void renderGlowPath(BufferBuilder buffer, MatrixStack matrices, Camera camera, List<Vec3d> path, int startColor, int endColor, long now) {
      if (path.size() >= 2) {
         float anim = (float)(now % 100000L) / 1000.0F;
         int lastIndex = path.size() - 1;

         for (int i = 0; i < path.size(); i++) {
            Vec3d pos = path.get(i);
            float progress = (float)i / (float)lastIndex;
            float fade = 1.0F - progress;
            fade = fade * fade * (3.0F - 2.0F * fade);
            float shimmer = 1.0F + 0.025F * MathHelper.sin(anim * 5.0F + progress * 10.0F);
            float size = (0.065F + fade * 0.11F) * shimmer;
            int color = this.interpolateColor(startColor, endColor, progress);
            int bright = this.interpolateColor(color, -1, 0.3F);
            this.drawGlow(buffer, matrices, camera, pos, size * 2.7F, this.withAlpha(color, (int)(fade * 24.0F)));
            this.drawGlow(buffer, matrices, camera, pos, size * 1.55F, this.withAlpha(color, (int)(fade * 72.0F)));
            this.drawGlow(buffer, matrices, camera, pos, size * 0.78F, this.withAlpha(bright, (int)(fade * 150.0F)));
         }
      }
   }

   private void renderImpactGlow(BufferBuilder buffer, MatrixStack matrices, Camera camera, Vec3d pos, int color, long now) {
      float pulse = 1.0F + 0.08F * MathHelper.sin((float)(now % 3000L) / 120.0F);
      int bright = this.interpolateColor(color, -1, 0.45F);
      this.drawGlow(buffer, matrices, camera, pos, 0.7F * pulse, this.withAlpha(color, 45));
      this.drawGlow(buffer, matrices, camera, pos, 0.42F * pulse, this.withAlpha(color, 115));
      this.drawGlow(buffer, matrices, camera, pos, 0.22F * pulse, this.withAlpha(bright, 220));
   }

   private void drawGlow(BufferBuilder buffer, MatrixStack matrices, Camera camera, Vec3d worldPos, float scale, int color) {
      Vec3d camPos = camera.getPos();
      matrices.push();
      matrices.translate(worldPos.x - camPos.x, worldPos.y - camPos.y, worldPos.z - camPos.z);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
      Matrix4f mat = matrices.peek().getPositionMatrix();
      buffer.vertex(mat, -scale, scale, 0.0F).texture(0.0F, 1.0F).color(color);
      buffer.vertex(mat, scale, scale, 0.0F).texture(1.0F, 1.0F).color(color);
      buffer.vertex(mat, scale, -scale, 0.0F).texture(1.0F, 0.0F).color(color);
      buffer.vertex(mat, -scale, -scale, 0.0F).texture(0.0F, 0.0F).color(color);
      matrices.pop();
   }

   private int withAlpha(int color, int alpha) {
      alpha = MathHelper.clamp(alpha, 0, 255);
      return alpha << 24 | color & 16777215;
   }

   private int interpolateColor(int start, int end, float t) {
      t = MathHelper.clamp(t, 0.0F, 1.0F);
      int sr = start >> 16 & 0xFF;
      int sg = start >> 8 & 0xFF;
      int sb = start & 0xFF;
      int er = end >> 16 & 0xFF;
      int eg = end >> 8 & 0xFF;
      int eb = end & 0xFF;
      int r = (int)((float)sr + (float)(er - sr) * t);
      int g = (int)((float)sg + (float)(eg - sg) * t);
      int b = (int)((float)sb + (float)(eb - sb) * t);
      return 0xFF000000 | r << 16 | g << 8 | b;
   }

   public List<Entity> getProjectiles() {
      return StreamSupport.<Entity>stream(this.mc.world.getEntities().spliterator(), false).filter(e -> {
         if (this.visible(e)) {
            return false;
         } else if (e instanceof TridentEntity) {
            return this.showTridents.getValue();
         } else if (e instanceof PersistentProjectileEntity) {
            return this.showArrows.getValue();
         } else if (e instanceof ThrownItemEntity) {
            return this.showThrownItems.getValue();
         } else {
            return e instanceof ItemEntity ? this.showDroppedItems.getValue() : false;
         }
      }).toList();
   }

   public Vec3d calculateMotion(Entity entity, Vec3d prevPos, Vec3d motion) {
      boolean isInWater = Objects.requireNonNull(this.mc.world)
         .getBlockState(BlockPos.ofFloored(prevPos))
         .getFluidState()
         .isIn(FluidTags.WATER);
      Objects.requireNonNull(entity);

      float multiply = switch (entity) {
         case TridentEntity i -> 0.99F;
         case PersistentProjectileEntity ix when isInWater -> 0.6F;
         default -> isInWater ? 0.8F : 0.99F;
      };
      return motion.multiply((double)multiply).add(0.0, -entity.getFinalGravity(), 0.0);
   }

   private void breakingBad(Entity entity, Vec3d pos, int ticks) {
      Objects.requireNonNull(entity);
      switch (entity) {
         case ItemEntity item:
            this.points.add(new Predictions.Point(item.getStack(), pos, ticks));
            break;
         case ThrownItemEntity thrown:
            this.points.add(new Predictions.Point(thrown.getStack(), pos, ticks));
            break;
         case PersistentProjectileEntity persistent:
            this.points.add(new Predictions.Point(persistent.getItemStack(), pos, ticks));
            break;
         default:
            break;
      }
   }

   private boolean visible(Entity entity) {
      boolean posChange = entity.getX() == entity.prevX
         && entity.getY() == entity.prevY
         && entity.getZ() == entity.prevZ;
      boolean itemEntityCheck = entity instanceof ItemEntity
         && (entity.isOnGround() || WorldUtils.isBoxInBlock(entity.getBoundingBox().expand(2.0), Blocks.WATER));
      return posChange || itemEntityCheck;
   }

   private static record Point(ItemStack stack, Vec3d pos, int ticks) {
   }

   private static class SimulatedPath {
      private final List<Vec3d> rawPoints;
      private final Vec3d hitPos;
      private final int hitTicks;

      private SimulatedPath(List<Vec3d> rawPoints, Vec3d hitPos, int hitTicks) {
         this.rawPoints = rawPoints;
         this.hitPos = hitPos;
         this.hitTicks = hitTicks;
      }
   }
}
