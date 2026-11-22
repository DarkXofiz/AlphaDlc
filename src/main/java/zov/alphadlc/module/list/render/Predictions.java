package zov.alphadlc.module.list.render;

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
import zov.alphadlc.event.list.EventHUD;
import zov.alphadlc.event.list.EventWorldRender;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.module.settings.ModeSetting;
import zov.alphadlc.util.player.combat.RaytraceUtil;
import zov.alphadlc.util.player.other.WorldUtils;
import zov.alphadlc.util.render.math.ProjectionUtil;
import zov.alphadlc.util.render.msdf.Fonts;
import zov.alphadlc.util.render.providers.ColorProvider;
import zov.alphadlc.util.render.renderers.DrawUtil;

@ModuleInformation(
   moduleName = "Predictions",
   moduleDesc = "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0435\u0442 \u0442\u0440\u0430\u0435\u043a\u0442\u043e\u0440\u0438\u044e \u043f\u043e\u043b\u0435\u0442\u0430 \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u043e\u0432",
   moduleCategory = ModuleCategory.RENDER
)
public class Predictions extends Module {
   private static final Identifier GLOW = Identifier.method_60655("mre", "images/glow.png");
   private final ModeSetting renderMode = new ModeSetting(
      "\u0420\u0435\u0436\u0438\u043c \u0440\u0435\u043d\u0434\u0435\u0440\u0430", "Default", "Default", "Glow"
   );
   private final BooleanSetting walls = new BooleanSetting("\u0427\u0435\u0440\u0435\u0437 \u0441\u0442\u0435\u043d\u044b", true);
   private final BooleanSetting showArrows = new BooleanSetting("\u0421\u0442\u0440\u0435\u043b\u044b", true);
   private final BooleanSetting showTridents = new BooleanSetting("\u0422\u0440\u0435\u0437\u0443\u0431\u0446\u044b", true);
   private final BooleanSetting showThrownItems = new BooleanSetting("\u042d\u043d\u0434\u0435\u0440\u0416\u0435\u043c\u0447\u0443\u0433", true);
   private final BooleanSetting showDroppedItems = new BooleanSetting("\u041f\u0440\u0435\u0434\u043c\u0435\u0442\u044b", false);
   private final List<Predictions.Point> points = new ArrayList<>();

   @Subscribe
   public void onDraw(EventHUD e) {
      for (Predictions.Point point : this.points) {
         Vector2f vec2f = ProjectionUtil.project(point.pos);
         int ticks = point.ticks;
         double time = (double)(ticks * 50) / 1000.0;
         String text = String.format("%.1f", time) + " \u0441\u0435\u043a";
         float textWidth = Fonts.SFREGULAR.get().getWidth(text, 7.0F);
         float centerX = vec2f.method_32118();
         float centerY = vec2f.method_32119();
         float totalHeight = 5.75F;
         float rectX = centerX - textWidth / 2.0F;
         float rectY = centerY - totalHeight / 2.0F;
         float textY = rectY + 5.0F;
         DrawUtil.drawRound(rectX - 7.0F, textY - 2.0F, textWidth + 14.75F, totalHeight + 5.0F, 0.0F, ColorProvider.rgba(0, 0, 0, 120.0F));
         e.getDrawContext().method_51448().method_22903();
         e.getDrawContext().method_51448().method_46416(rectX - 5.0F, textY - 0.75F, 0.0F);
         e.getDrawContext().method_51448().method_22905(0.5F, 0.5F, 1.0F);
         e.getDrawContext().method_51427(point.stack(), 0, 0);
         e.getDrawContext().method_51448().method_22905(1.0F, 1.0F, 1.0F);
         e.getDrawContext().method_51448().method_46416(-(rectX - 5.0F), -(textY - 0.75F), 0.0F);
         e.getDrawContext().method_51448().method_22909();
         DrawUtil.drawText(Fonts.SFREGULAR.get(), text.replace(",", "."), rectX + 4.5F, textY - 0.5F, ColorProvider.rgba(255, 255, 255, 255.0F), 6.75F);
      }
   }

   @Subscribe
   public void onWorldRender(EventWorldRender e) {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
         this.points.clear();
         float tickDelta = e.getTickDelta();
         long now = System.currentTimeMillis();
         MatrixStack matrices = e.getMatrixStack();
         Camera camera = this.mc.field_1773.method_19418();
         boolean depth = !this.walls.getValue();
         if (this.renderMode.is("Glow")) {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
            RenderSystem.setShaderTexture(0, GLOW);
            RenderSystem.setShader(ShaderProgramKeys.field_53880);
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            BufferBuilder buffer = Tessellator.method_1348().method_60827(DrawMode.field_27382, VertexFormats.field_1575);

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
               BufferRenderer.method_43433(buffer.method_60800());
            } catch (Exception var15) {
            }

            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
         } else {
            this.getProjectiles().forEach(entityx -> {
               Vec3d motion = entityx.method_18798();
               Vec3d pos = entityx.method_19538();
               int ticks = 0;

               for (int i = 0; i < 300; i++) {
                  Vec3d prevPos = pos;
                  pos = pos.method_1019(motion);
                  motion = this.calculateMotion(entityx, prevPos, motion);
                  HitResult result = RaytraceUtil.raycast(prevPos, pos, ShapeType.field_17558, entityx);
                  if (!result.method_17783().equals(Type.field_1333)) {
                     pos = result.method_17784();
                  }

                  float alpha = MathHelper.method_15363((float)i / 25.0F, 0.0F, 1.0F) * 255.0F;
                  int baseColor = ColorProvider.setAlpha(ColorProvider.getColorClient(), (double)alpha);
                  DrawUtil.drawLine(prevPos, pos, baseColor, 2.0F, depth);
                  Vec3d finalPos = pos;
                  boolean inEntity = StreamSupport.<Entity>stream(this.mc.field_1687.method_18112().spliterator(), false).filter(ent -> {
                     if (ent instanceof LivingEntity living && living != this.mc.field_1724 && living.method_5805()) {
                        return true;
                     }

                     return false;
                  }).anyMatch(ent -> ent.method_5829().method_1014(0.25).method_993(prevPos, finalPos));
                  if (result.method_17783().equals(Type.field_1332) || pos.field_1351 < -128.0 || inEntity || result.method_17783().equals(Type.field_1331)) {
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
      Vec3d pos = entity.method_19538();
      Vec3d motion = entity.method_18798();
      Vec3d hitPos = null;
      int hitTicks = 0;
      this.addIfFar(path, renderPos);
      if (renderPos.method_1025(pos) > 1.0E-5) {
         this.addIfFar(path, renderPos.method_35590(pos, 0.35));
         this.addIfFar(path, renderPos.method_35590(pos, 0.7));
      }

      this.addIfFar(path, pos);

      for (int i = 0; i < 220; i++) {
         Vec3d prevPos = pos;
         pos = pos.method_1019(motion);
         motion = this.calculateMotion(entity, prevPos, motion);
         HitResult result = RaytraceUtil.raycast(prevPos, pos, ShapeType.field_17558, entity);
         if (result.method_17783() != Type.field_1333) {
            pos = result.method_17784();
         }

         this.addIfFar(path, pos);
         boolean hitEntity = this.hitsEntity(entity, prevPos, pos);
         if (result.method_17783() == Type.field_1332 || result.method_17783() == Type.field_1331 || hitEntity || pos.field_1351 < -128.0) {
            hitPos = pos;
            hitTicks = i + 1;
            break;
         }
      }

      return new Predictions.SimulatedPath(path, hitPos, hitTicks);
   }

   private Vec3d getInterpolatedPos(Entity entity, float tickDelta) {
      return new Vec3d(
         MathHelper.method_16436((double)tickDelta, entity.field_6014, entity.method_23317()),
         MathHelper.method_16436((double)tickDelta, entity.field_6036, entity.method_23318()),
         MathHelper.method_16436((double)tickDelta, entity.field_5969, entity.method_23321())
      );
   }

   private void addIfFar(List<Vec3d> list, Vec3d point) {
      if (list.isEmpty() || list.get(list.size() - 1).method_1025(point) > 1.0E-6) {
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
            double dist = p1.method_1022(p2);
            int steps = MathHelper.method_15340((int)Math.ceil(dist / 0.08), 3, 8);

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
            2.0 * p1.field_1352
               + (-p0.field_1352 + p2.field_1352) * (double)t
               + (2.0 * p0.field_1352 - 5.0 * p1.field_1352 + 4.0 * p2.field_1352 - p3.field_1352) * t2
               + (-p0.field_1352 + 3.0 * p1.field_1352 - 3.0 * p2.field_1352 + p3.field_1352) * t3
         );
      double y = 0.5
         * (
            2.0 * p1.field_1351
               + (-p0.field_1351 + p2.field_1351) * (double)t
               + (2.0 * p0.field_1351 - 5.0 * p1.field_1351 + 4.0 * p2.field_1351 - p3.field_1351) * t2
               + (-p0.field_1351 + 3.0 * p1.field_1351 - 3.0 * p2.field_1351 + p3.field_1351) * t3
         );
      double z = 0.5
         * (
            2.0 * p1.field_1350
               + (-p0.field_1350 + p2.field_1350) * (double)t
               + (2.0 * p0.field_1350 - 5.0 * p1.field_1350 + 4.0 * p2.field_1350 - p3.field_1350) * t2
               + (-p0.field_1350 + 3.0 * p1.field_1350 - 3.0 * p2.field_1350 + p3.field_1350) * t3
         );
      return new Vec3d(x, y, z);
   }

   private boolean hitsEntity(Entity projectile, Vec3d from, Vec3d to) {
      return this.mc.field_1687 == null ? false : !this.mc.field_1687.method_8333(projectile, new Box(from, to).method_1014(0.3), ent -> {
         if (ent instanceof LivingEntity living && living != this.mc.field_1724 && living.method_5805()) {
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
            float shimmer = 1.0F + 0.025F * MathHelper.method_15374(anim * 5.0F + progress * 10.0F);
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
      float pulse = 1.0F + 0.08F * MathHelper.method_15374((float)(now % 3000L) / 120.0F);
      int bright = this.interpolateColor(color, -1, 0.45F);
      this.drawGlow(buffer, matrices, camera, pos, 0.7F * pulse, this.withAlpha(color, 45));
      this.drawGlow(buffer, matrices, camera, pos, 0.42F * pulse, this.withAlpha(color, 115));
      this.drawGlow(buffer, matrices, camera, pos, 0.22F * pulse, this.withAlpha(bright, 220));
   }

   private void drawGlow(BufferBuilder buffer, MatrixStack matrices, Camera camera, Vec3d worldPos, float scale, int color) {
      Vec3d camPos = camera.method_19326();
      matrices.method_22903();
      matrices.method_22904(worldPos.field_1352 - camPos.field_1352, worldPos.field_1351 - camPos.field_1351, worldPos.field_1350 - camPos.field_1350);
      matrices.method_22907(RotationAxis.field_40716.rotationDegrees(-camera.method_19330()));
      matrices.method_22907(RotationAxis.field_40714.rotationDegrees(camera.method_19329()));
      Matrix4f mat = matrices.method_23760().method_23761();
      buffer.method_22918(mat, -scale, scale, 0.0F).method_22913(0.0F, 1.0F).method_39415(color);
      buffer.method_22918(mat, scale, scale, 0.0F).method_22913(1.0F, 1.0F).method_39415(color);
      buffer.method_22918(mat, scale, -scale, 0.0F).method_22913(1.0F, 0.0F).method_39415(color);
      buffer.method_22918(mat, -scale, -scale, 0.0F).method_22913(0.0F, 0.0F).method_39415(color);
      matrices.method_22909();
   }

   private int withAlpha(int color, int alpha) {
      alpha = MathHelper.method_15340(alpha, 0, 255);
      return alpha << 24 | color & 16777215;
   }

   private int interpolateColor(int start, int end, float t) {
      t = MathHelper.method_15363(t, 0.0F, 1.0F);
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
      return StreamSupport.<Entity>stream(this.mc.field_1687.method_18112().spliterator(), false).filter(e -> {
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
      boolean isInWater = Objects.requireNonNull(this.mc.field_1687)
         .method_8320(BlockPos.method_49638(prevPos))
         .method_26227()
         .method_15767(FluidTags.field_15517);
      Objects.requireNonNull(entity);

      float multiply = switch (entity) {
         case TridentEntity i -> 0.99F;
         case PersistentProjectileEntity ix when isInWater -> 0.6F;
         default -> isInWater ? 0.8F : 0.99F;
      };
      return motion.method_1021((double)multiply).method_1031(0.0, -entity.method_56989(), 0.0);
   }

   private void breakingBad(Entity entity, Vec3d pos, int ticks) {
      Objects.requireNonNull(entity);
      switch (entity) {
         case ItemEntity item:
            this.points.add(new Predictions.Point(item.method_6983(), pos, ticks));
            break;
         case ThrownItemEntity thrown:
            this.points.add(new Predictions.Point(thrown.method_7495(), pos, ticks));
            break;
         case PersistentProjectileEntity persistent:
            this.points.add(new Predictions.Point(persistent.method_54759(), pos, ticks));
            break;
      }
   }

   private boolean visible(Entity entity) {
      boolean posChange = entity.method_23317() == entity.field_6014
         && entity.method_23318() == entity.field_6036
         && entity.method_23321() == entity.field_5969;
      boolean itemEntityCheck = entity instanceof ItemEntity
         && (entity.method_24828() || WorldUtils.isBoxInBlock(entity.method_5829().method_1014(2.0), Blocks.field_10382));
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
