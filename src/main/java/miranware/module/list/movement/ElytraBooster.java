package miranware.module.list.movement;

import com.google.common.eventbus.Subscribe;
import java.util.Locale;
import net.minecraft.util.math.MathHelper;
import miranware.event.list.EventHUD;
import miranware.event.list.FireworkEvent;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.list.combat.KillAura;
import miranware.module.settings.BooleanSetting;
import miranware.module.settings.ModeSetting;
import miranware.module.settings.SliderSetting;
import miranware.util.base.Instance;
import miranware.util.render.msdf.Fonts;
import miranware.util.render.providers.ColorProvider;
import miranware.util.render.renderers.DrawUtil;

@ModuleInformation(
   moduleName = "SuperFirework",
   moduleDesc = "Firework Boost",
   moduleCategory = ModuleCategory.MOVEMENT
)
public final class ElytraBooster extends Module {
   public static final ElytraBooster INSTANCE = new ElytraBooster();
   private static final int[] AI_YAW_VECTORS = new int[]{-45, 45, 135, -135};
   private static final int[] AI_PITCH_VECTORS = new int[]{-45, 45};
   private final BooleanSetting showOverlay = new BooleanSetting("Show Info", true);
   private final BooleanSetting boost = new BooleanSetting("Boost", true);
   private final ModeSetting mode = new ModeSetting(
         "Boost Mode",
         "Bravo",
         "Bravo",
         "Custom",
         "ReallyWorld"
      )
      .setVisible(this.boost::getValue);
   private final BooleanSetting maxspeed = new BooleanSetting(
         "Angle Speed", false
      )
      .setVisible(() -> this.boost.getValue() && this.mode.is("Custom"));
   private final SliderSetting speedxz = new SliderSetting("XZ Speed", 1.65, 1.5, 2.5, 0.01)
      .setVisible(() -> this.boost.getValue() && this.mode.is("Custom") && !this.maxspeed.getValue());
   private final SliderSetting speedy = new SliderSetting("Y Speed", 1.59, 1.5, 2.5, 0.01)
      .setVisible(() -> this.boost.getValue() && this.mode.is("Custom") && !this.maxspeed.getValue());
   private final SliderSetting yaw05 = new SliderSetting("Yaw 0-5\u00b0", 1.6, 1.5, 2.5, 0.01)
      .setVisible(() -> this.boost.getValue() && this.mode.is("Custom") && this.maxspeed.getValue());
   private final SliderSetting yaw510 = new SliderSetting("Yaw 5-10\u00b0", 1.62, 1.5, 2.5, 0.01)
      .setVisible(() -> this.boost.getValue() && this.mode.is("Custom") && this.maxspeed.getValue());
   private final SliderSetting yaw1015 = new SliderSetting("Yaw 10-15\u00b0", 1.65, 1.5, 2.5, 0.01)
      .setVisible(() -> this.boost.getValue() && this.mode.is("Custom") && this.maxspeed.getValue());
   private final SliderSetting yaw1520 = new SliderSetting("Yaw 15-20\u00b0", 1.68, 1.5, 2.5, 0.01)
      .setVisible(() -> this.boost.getValue() && this.mode.is("Custom") && this.maxspeed.getValue());
   private final SliderSetting yaw2025 = new SliderSetting("Yaw 20-25\u00b0", 1.74, 1.5, 2.5, 0.01)
      .setVisible(() -> this.boost.getValue() && this.mode.is("Custom") && this.maxspeed.getValue());
   private final SliderSetting yaw2530 = new SliderSetting("Yaw 25-30\u00b0", 1.8, 1.5, 2.5, 0.01)
      .setVisible(() -> this.boost.getValue() && this.mode.is("Custom") && this.maxspeed.getValue());
   private final SliderSetting yaw3035 = new SliderSetting("Yaw 30-35\u00b0", 1.8, 1.5, 2.5, 0.01)
      .setVisible(() -> this.boost.getValue() && this.mode.is("Custom") && this.maxspeed.getValue());
   private final SliderSetting yaw3540 = new SliderSetting("Yaw 35-40\u00b0", 1.8, 1.5, 2.5, 0.01)
      .setVisible(() -> this.boost.getValue() && this.mode.is("Custom") && this.maxspeed.getValue());
   private final SliderSetting yaw4045 = new SliderSetting("Yaw 40-45\u00b0", 1.82, 1.5, 2.5, 0.01)
      .setVisible(() -> this.boost.getValue() && this.mode.is("Custom") && this.maxspeed.getValue());
   private final SliderSetting pitch05 = new SliderSetting("Pitch 0-5\u00b0", 1.59, 1.5, 2.5, 0.01)
      .setVisible(() -> this.boost.getValue() && this.mode.is("Custom") && this.maxspeed.getValue());
   private final SliderSetting pitch510 = new SliderSetting("Pitch 5-10\u00b0", 1.6, 1.5, 2.5, 0.01)
      .setVisible(() -> this.boost.getValue() && this.mode.is("Custom") && this.maxspeed.getValue());
   private final SliderSetting pitch1015 = new SliderSetting("Pitch 10-15\u00b0", 1.61, 1.5, 2.5, 0.01)
      .setVisible(() -> this.boost.getValue() && this.mode.is("Custom") && this.maxspeed.getValue());
   private final SliderSetting pitch1520 = new SliderSetting("Pitch 15-20\u00b0", 1.62, 1.5, 2.5, 0.01)
      .setVisible(() -> this.boost.getValue() && this.mode.is("Custom") && this.maxspeed.getValue());
   private final SliderSetting pitch2025 = new SliderSetting("Pitch 20-25\u00b0", 1.68, 1.5, 2.5, 0.01)
      .setVisible(() -> this.boost.getValue() && this.mode.is("Custom") && this.maxspeed.getValue());
   private final SliderSetting pitch2530 = new SliderSetting("Pitch 25-30\u00b0", 1.74, 1.5, 2.5, 0.01)
      .setVisible(() -> this.boost.getValue() && this.mode.is("Custom") && this.maxspeed.getValue());
   private final SliderSetting pitch3035 = new SliderSetting("Pitch 30-35\u00b0", 1.95, 1.5, 2.5, 0.01)
      .setVisible(() -> this.boost.getValue() && this.mode.is("Custom") && this.maxspeed.getValue());
   private final SliderSetting pitch3540 = new SliderSetting("Pitch 35-40\u00b0", 2.0, 1.5, 2.5, 0.01)
      .setVisible(() -> this.boost.getValue() && this.mode.is("Custom") && this.maxspeed.getValue());
   private final SliderSetting pitch4045 = new SliderSetting("Pitch 40-45\u00b0", 2.2, 1.5, 2.5, 0.01)
      .setVisible(() -> this.boost.getValue() && this.mode.is("Custom") && this.maxspeed.getValue());
   private final SliderSetting[] yawSettings = new SliderSetting[]{
      this.yaw05, this.yaw510, this.yaw1015, this.yaw1520, this.yaw2025, this.yaw2530, this.yaw3035, this.yaw3540, this.yaw4045
   };
   private final SliderSetting[] pitchSettings = new SliderSetting[]{
      this.pitch05, this.pitch510, this.pitch1015, this.pitch1520, this.pitch2025, this.pitch2530, this.pitch3035, this.pitch3540, this.pitch4045
   };

   public ElytraBooster.BoostValues computeBoost(float yaw, float pitch) {
      if (this.boost.getValue() && this.mc.player != null) {
         KillAura aura = Instance.get(KillAura.class);
         if (aura != null && aura.isEnabled()) {
            yaw = MathHelper.wrapDegrees(aura.lastYaw);
            pitch = aura.lastPitch;
         }

         if (pitch < -75.0F) {
            return new ElytraBooster.BoostValues(1.5F, 1.5F);
         } else if (this.mode.is("Custom")) {
            return this.handleCustomMode(yaw, pitch);
         } else if (this.mode.is("Bravo")) {
            return new ElytraBooster.BoostValues(1.8F, 1.8F);
         } else {
            return this.mode.is("ReallyWorld") ? this.handleReallyWorldMode(yaw, pitch) : new ElytraBooster.BoostValues(1.5F, 1.5F);
         }
      } else {
         return new ElytraBooster.BoostValues(1.5F, 1.5F);
      }
   }

   private ElytraBooster.BoostValues handleCustomMode(float yaw, float pitch) {
      if (!this.maxspeed.getValue()) {
         return new ElytraBooster.BoostValues(this.speedxz.getFloatValue(), this.speedy.getFloatValue());
      } else {
         float convertedYaw = this.convertAngleToRange(MathHelper.wrapDegrees(yaw));
         float convertedPitch = this.convertAngleToRange(Math.abs(pitch));
         float xzSpeed = this.getSpeedForYaw(convertedYaw);
         float ySpeed = this.getSpeedForPitch(convertedPitch);
         if (ySpeed > xzSpeed) {
            xzSpeed = ySpeed;
         }

         return new ElytraBooster.BoostValues(xzSpeed, ySpeed);
      }
   }

   private ElytraBooster.BoostValues handleReallyWorldMode(float yaw, float pitch) {
      float speed = this.getAiBoost(pitch, yaw, false, true);
      return new ElytraBooster.BoostValues(speed, speed);
   }

   private float getSpeedForYaw(float yaw) {
      int index = (int)(yaw / 5.0F);
      if (index >= this.yawSettings.length) {
         index = this.yawSettings.length - 1;
      }

      if (index < 0) {
         index = 0;
      }

      return this.yawSettings[index].getFloatValue();
   }

   private float getSpeedForPitch(float pitch) {
      int index = (int)(pitch / 5.0F);
      if (index >= this.pitchSettings.length) {
         index = this.pitchSettings.length - 1;
      }

      if (index < 0) {
         index = 0;
      }

      return this.pitchSettings[index].getFloatValue();
   }

   private float convertAngleToRange(float angle) {
      float absAngle = Math.abs(angle);
      if (absAngle > 90.0F) {
         absAngle = 180.0F - absAngle;
      }

      if (absAngle > 45.0F) {
         absAngle = 90.0F - absAngle;
      }

      return absAngle;
   }

   private float getAiBoost(float pitch, float yaw, boolean isBravo, boolean applyRwCap) {
      if (Math.abs(pitch) > 55.0F) {
         return 1.55F;
      } else {
         float boost = this.adjustBoostForYaw(yaw, applyRwCap);
         boost = this.adjustBoostForPitch(pitch, boost);
         boost = Math.max(isBravo ? 1.65F : 1.6F, boost);
         return Math.min(boost, isBravo ? 1.9F : 2.2F);
      }
   }

   private float adjustBoostForYaw(float yaw, boolean applyRwCap) {
      int idx = findClosestVector(yaw, AI_YAW_VECTORS);
      if (idx == -1) {
         return 1.6F;
      } else {
         float dist = Math.abs(MathHelper.wrapDegrees(yaw) - (float)AI_YAW_VECTORS[idx]);
         float maxBoost = 2.2F;
         float minBoostVal = 1.6F;
         float maxDistance = 12.0F;
         float smartBoost = 0.0F;
         if (dist <= maxDistance) {
            float ratio = dist / maxDistance;
            smartBoost = maxBoost - (maxBoost - minBoostVal) * ratio;
         }

         float variableSpeed = getVariableSpeed(dist);
         float finalSpeed = Math.max(smartBoost, variableSpeed);
         return applyRwCap ? Math.min(finalSpeed, 1.8F) : finalSpeed;
      }
   }

   private float adjustBoostForPitch(float pitch, float boost) {
      int idx = findClosestVector(pitch, AI_PITCH_VECTORS);
      if (idx == -1) {
         return boost;
      } else {
         float dist = Math.abs(Math.abs(pitch) - (float)Math.abs(AI_PITCH_VECTORS[idx]));
         if (dist < 30.0F) {
            boost += 0.4F * (1.0F - dist / 30.0F);
         }

         return boost;
      }
   }

   private static float getVariableSpeed(float dist) {
      float[] thresholds = new float[]{4.0F, 8.0F, 11.0F, 15.0F, 21.0F, 28.0F};
      float[] speeds = new float[]{2.2F, 2.1F, 2.0F, 1.9F, 1.8F, 1.7F, 1.6F};
      int level = 0;

      while (level < thresholds.length && dist >= thresholds[level]) {
         level++;
      }

      return speeds[level];
   }

   private static int findClosestVector(float angle, int[] vectors) {
      int minIdx = -1;
      float minDist = Float.MAX_VALUE;

      for (int i = 0; i < vectors.length; i++) {
         float d = Math.abs(MathHelper.wrapDegrees(angle) - (float)vectors[i]);
         if (d < minDist) {
            minDist = d;
            minIdx = i;
         }
      }

      return minIdx;
   }

   @Subscribe
   private void onHud(EventHUD event) {
      if (this.showOverlay.getValue() && this.mc.player != null) {
         if (this.mc.player.isGliding()) {
            float yaw = this.mc.player.getYaw();
            float pitch = this.mc.player.getPitch();
            KillAura aura = Instance.get(KillAura.class);
            if (aura != null && aura.isEnabled()) {
               yaw = MathHelper.wrapDegrees(aura.lastYaw);
               pitch = aura.lastPitch;
            }

            String pitchText = String.format(Locale.US, "Y: %.1f\u00b0", pitch);
            String yawText = String.format(Locale.US, "XZ: %.1f\u00b0", MathHelper.wrapDegrees(yaw));
            String text = pitchText + "  \u00b7  " + yawText;
            int color = ColorProvider.getColorClient();
            int sw = this.mc.getWindow().getScaledWidth();
            int sh = this.mc.getWindow().getScaledHeight();
            float w = Fonts.SFBOLD.get().getWidth(text, 8.0F);
            float x = (float)sw / 2.0F - w / 2.0F;
            float y = (float)sh / 2.0F + 32.0F;
            DrawUtil.drawText(Fonts.SFBOLD.get(), text, x, y, ColorProvider.setAlpha(color, 230), 8.0F);
         }
      }
   }

   @Subscribe
   private void onFirework(FireworkEvent event) {
      if (this.mc.player != null && this.boost.getValue() && event.getBoostedEntity() == this.mc.player) {
         float yaw = this.mc.player.getYaw();
         float pitch = this.mc.player.getPitch();
         KillAura aura = Instance.get(KillAura.class);
         if (aura != null && aura.isEnabled()) {
            yaw = MathHelper.wrapDegrees(aura.lastYaw);
            pitch = aura.lastPitch;
         }

         ElytraBooster.BoostValues values = this.computeBoost(yaw, pitch);
         event.setSpeed(Math.max(values.speedXZ(), values.speedY()));
      }
   }

   public static record BoostValues(float speedXZ, float speedY) {
   }
}
