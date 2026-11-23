package miranware.module.list.render;

import com.google.common.eventbus.Subscribe;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import miranware.event.list.EventHUD;
import miranware.event.list.EventTick;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.BooleanSetting;
import miranware.module.settings.SliderSetting;
import miranware.util.render.providers.ColorProvider;
import miranware.util.render.renderers.DrawUtil;

@ModuleInformation(
   moduleName = "HitIndicator",
   moduleDesc = "Shows a red arc on the side the damage came from",
   moduleCategory = ModuleCategory.RENDER
)
public class HitIndicator extends Module {
   private final SliderSetting radius = new SliderSetting("Radius", 125.0, 60.0, 260.0, 5.0);
   private final SliderSetting arcLength = new SliderSetting("Arc Length", 76.0, 30.0, 150.0, 1.0);
   private final SliderSetting thickness = new SliderSetting("Thickness", 2.4F, 1.0, 7.0, 0.1F);
   private final SliderSetting duration = new SliderSetting("Display Time", 700.0, 250.0, 1600.0, 25.0);
   private final SliderSetting opacity = new SliderSetting("Opacity", 0.9F, 0.2F, 1.0, 0.05F);
   private final SliderSetting sourceRange = new SliderSetting(
      "Source Distance", 32.0, 6.0, 64.0, 1.0
   );
   private final BooleanSetting glow = new BooleanSetting("Glow", true);
   private final List<HitIndicator.Indicator> indicators = new ArrayList<>();
   private float lastHealth = -1.0F;

   @Override
   public void onEnable() {
      super.onEnable();
      this.lastHealth = this.getTotalHealth();
      this.indicators.clear();
   }

   @Override
   public void onDisable() {
      this.indicators.clear();
      this.lastHealth = -1.0F;
      super.onDisable();
   }

   @Subscribe
   private void onTick(EventTick event) {
      if (this.mc.player != null && this.mc.world != null) {
         float currentHealth = this.getTotalHealth();
         if (this.lastHealth < 0.0F) {
            this.lastHealth = currentHealth;
         } else {
            if (currentHealth + 0.01F < this.lastHealth) {
               Float yaw = this.resolveDamageYaw();
               if (yaw != null) {
                  this.addIndicator(yaw);
               }
            }

            this.lastHealth = currentHealth;
         }
      } else {
         this.lastHealth = -1.0F;
         this.indicators.clear();
      }
   }

   @Subscribe
   private void onHud(EventHUD event) {
      if (this.mc.player != null && this.mc.world != null && !this.indicators.isEmpty() && !this.mc.options.hudHidden) {
         long now = System.currentTimeMillis();
         float width = (float)this.mc.getWindow().getScaledWidth();
         float height = (float)this.mc.getWindow().getScaledHeight();
         float centerX = width / 2.0F;
         float centerY = height / 2.0F;
         float maxRadius = Math.max(24.0F, Math.min(centerX, centerY) - 8.0F);
         float drawRadius = Math.min(this.radius.getFloatValue(), maxRadius);
         float cameraYaw = this.mc.gameRenderer.getCamera().getYaw();
         Iterator<HitIndicator.Indicator> iterator = this.indicators.iterator();

         while (iterator.hasNext()) {
            HitIndicator.Indicator indicator = iterator.next();
            float age = (float)(now - indicator.spawnTime);
            float lifetime = this.duration.getFloatValue();
            if (age >= lifetime) {
               iterator.remove();
            } else {
               float progress = MathHelper.clamp(age / lifetime, 0.0F, 1.0F);
               float fadeIn = MathHelper.clamp(age / 90.0F, 0.0F, 1.0F);
               float alpha = this.opacity.getFloatValue() * fadeIn * (1.0F - progress * progress);
               if (!(alpha <= 0.02F)) {
                  float relativeYaw = MathHelper.wrapDegrees(indicator.worldYaw - cameraYaw);
                  int baseAlpha = (int)(alpha * 255.0F);
                  int color = ColorProvider.rgba(255, 35, 45, (float)baseAlpha);
                  if (this.glow.getValue()) {
                     int glowAlpha = (int)((float)baseAlpha * 0.22F);
                     int glowColor = ColorProvider.rgba(255, 35, 45, (float)glowAlpha);
                     this.drawArc(centerX, centerY, drawRadius, relativeYaw, this.arcLength.getFloatValue(), this.thickness.getFloatValue() + 3.2F, glowColor);
                  }

                  this.drawArc(centerX, centerY, drawRadius, relativeYaw, this.arcLength.getFloatValue(), this.thickness.getFloatValue(), color);
               }
            }
         }
      }
   }

   private float getTotalHealth() {
      return this.mc.player == null ? -1.0F : this.mc.player.getHealth() + this.mc.player.getAbsorptionAmount();
   }

   private void addIndicator(float worldYaw) {
      this.indicators.add(new HitIndicator.Indicator(worldYaw, System.currentTimeMillis()));

      while (this.indicators.size() > 8) {
         this.indicators.remove(0);
      }
   }

   private Float resolveDamageYaw() {
      Entity source = this.getNearestLivingSource();
      if (!this.isValidSource(source)) {
         return null;
      } else {
         double dx = source.getX() - this.mc.player.getX();
         double dz = source.getZ() - this.mc.player.getZ();
         return dx * dx + dz * dz < 1.0E-4 ? null : (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
      }
   }

   private Entity getNearestLivingSource() {
      Entity nearest = null;
      Entity nearestSwinging = null;
      double bestDistance = (double)(this.sourceRange.getFloatValue() * this.sourceRange.getFloatValue());
      double bestSwingingDistance = bestDistance;

      for (Entity entity : this.mc.world.getEntities()) {
         if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)entity;
            if (this.isValidSource(entity)) {
               double distance = this.mc.player.squaredDistanceTo(entity);
               if (living.handSwingTicks < 10 && distance < bestSwingingDistance) {
                  bestSwingingDistance = distance;
                  nearestSwinging = entity;
               }

               if (distance < bestDistance) {
                  bestDistance = distance;
                  nearest = entity;
               }
            }
         }
      }

      return nearestSwinging != null ? nearestSwinging : nearest;
   }

   private boolean isValidSource(Entity entity) {
      return entity != null
         && entity != this.mc.player
         && entity.isAlive()
         && this.mc.player != null
         && this.mc.player.squaredDistanceTo(entity) <= (double)(this.sourceRange.getFloatValue() * this.sourceRange.getFloatValue());
   }

   private void drawArc(float centerX, float centerY, float radius, float yaw, float length, float lineWidth, int color) {
      float start = yaw - length / 2.0F;
      float end = yaw + length / 2.0F;
      DrawUtil.drawRingArc(centerX, centerY, radius, lineWidth, start, end, color);
   }

   private static class Indicator {
      private final float worldYaw;
      private final long spawnTime;

      private Indicator(float worldYaw, long spawnTime) {
         this.worldYaw = worldYaw;
         this.spawnTime = spawnTime;
      }
   }
}
