package zov.alphadlc.module.list.combat;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import zov.alphadlc.event.list.EventPlayerUpdate;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.module.settings.SliderSetting;
import zov.alphadlc.util.base.Instance;
import zov.alphadlc.util.friend.FriendRepository;
import zov.alphadlc.util.player.other.InventoryUtil;

@ModuleInformation(
   moduleName = "TargetPearl",
   moduleDesc = "\u041c\u0435\u0442\u0430\u0435\u0442 \u044d\u043d\u0434\u0435\u0440-\u043f\u0435\u0440\u043b \u0442\u043e\u0447\u043d\u043e \u0432 \u0446\u0435\u043b\u044c",
   moduleCategory = ModuleCategory.COMBAT
)
public class TargetPearl extends Module {
   private final BooleanSetting onlyTarget = new BooleanSetting(
      "\u0422\u043e\u043b\u044c\u043a\u043e \u043f\u043e \u0442\u0430\u0440\u0433\u0435\u0442\u0443 KillAura", false
   );
   private final SliderSetting maxRange = new SliderSetting(
      "\u041c\u0430\u043a\u0441. \u0434\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f", 40.0, 10.0, 120.0, 5.0
   );
   private final SliderSetting minDistance = new SliderSetting(
      "\u041c\u0438\u043d. \u0434\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f", 8.0, 3.0, 40.0, 1.0
   );
   private final SliderSetting throwDelay = new SliderSetting("\u0417\u0430\u0434\u0435\u0440\u0436\u043a\u0430 (\u043c\u0441)", 1500.0, 200.0, 4000.0, 100.0);
   private final BooleanSetting restoreLook = new BooleanSetting("\u0412\u0435\u0440\u043d\u0443\u0442\u044c \u0432\u0437\u0433\u043b\u044f\u0434", true);
   private static final int MAX_SIM_TICKS = 160;
   private static final double THROW_VELOCITY = 1.5;
   private long lastThrow;

   @Subscribe
   private void onUpdate(EventPlayerUpdate ignored) {
      if (this.mc.player != null && this.mc.world != null && this.mc.interactionManager != null) {
         if (!this.mc.player.isGliding()) {
            if (System.currentTimeMillis() - this.lastThrow >= (long)this.throwDelay.getValue()) {
               if (!this.mc.player.getItemCooldownManager().isCoolingDown(new ItemStack(Items.ENDER_PEARL))) {
                  if (InventoryUtil.searchItemHotbar(Items.ENDER_PEARL) != -1) {
                     LivingEntity target = this.resolveTarget();
                     if (target != null) {
                        Vec3d landing = target.getPos();
                        float[] rot = this.findRotation(landing);
                        if (rot != null) {
                           this.throwPearl(rot[0], rot[1]);
                           this.lastThrow = System.currentTimeMillis();
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private LivingEntity resolveTarget() {
      if (this.onlyTarget.getValue()) {
         KillAura aura = Instance.get(KillAura.class);
         LivingEntity auraTarget = aura == null ? null : aura.getTarget();
         return this.isValid(auraTarget) ? auraTarget : null;
      } else {
         PlayerEntity best = null;
         double bestDist = Double.MAX_VALUE;
         double min = this.minDistance.getValue();
         double max = this.maxRange.getValue();

         for (PlayerEntity player : this.mc.world.getPlayers()) {
            if (this.isValid(player)) {
               double distance = (double)this.mc.player.distanceTo(player);
               if (!(distance < min) && !(distance > max) && distance < bestDist) {
                  bestDist = distance;
                  best = player;
               }
            }
         }

         return best;
      }
   }

   private boolean isValid(LivingEntity entity) {
      if (entity != null && entity != this.mc.player && entity.isAlive()) {
         if (entity instanceof PlayerEntity player && FriendRepository.isFriend(player.getNameForScoreboard())) {
            return false;
         }

         return true;
      } else {
         return false;
      }
   }

   private float[] findRotation(Vec3d target) {
      Vec3d eye = this.mc.player.getEyePos();
      double dx = target.x - eye.x;
      double dz = target.z - eye.z;
      float yaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
      float bestPitch = 0.0F;
      double bestError = Double.MAX_VALUE;

      for (float pitch = 80.0F; pitch >= -30.0F; pitch -= 0.5F) {
         Vec3d landing = this.simulate(yaw, pitch);
         if (landing != null) {
            double error = landing.squaredDistanceTo(target);
            if (error < bestError) {
               bestError = error;
               bestPitch = pitch;
            }
         }
      }

      if (bestError == Double.MAX_VALUE) {
         return null;
      } else {
         return bestError > 36.0 ? null : new float[]{yaw, MathHelper.clamp(bestPitch, -90.0F, 90.0F)};
      }
   }

   private Vec3d simulate(float yaw, float pitch) {
      double yr = Math.toRadians((double)yaw);
      double pr = Math.toRadians((double)pitch);
      double x = this.mc.player.getX() - Math.cos(yr) * 0.16;
      double y = this.mc.player.getY() + (double)this.mc.player.getEyeHeight(this.mc.player.getPose()) - 0.1;
      double z = this.mc.player.getZ() - Math.sin(yr) * 0.16;
      double vx = -Math.sin(yr) * Math.cos(pr) * 1.5;
      double vy = -Math.sin(pr) * 1.5 + this.mc.player.getVelocity().y;
      double vz = Math.cos(yr) * Math.cos(pr) * 1.5;
      double bottom = (double)this.mc.world.getBottomY();

      for (int i = 0; i < 160; i++) {
         x += vx;
         y += vy;
         z += vz;
         vx *= 0.99;
         vy = vy * 0.99 - 0.03;
         vz *= 0.99;
         if (y <= bottom) {
            return this.snap(x, y, z);
         }

         BlockPos bp = BlockPos.ofFloored(x, y, z);
         if (!this.mc.world.getBlockState(bp).getCollisionShape(this.mc.world, bp).isEmpty()) {
            return this.snap(x, y, z);
         }
      }

      return null;
   }

   private Vec3d snap(double x, double y, double z) {
      return new Vec3d((double)MathHelper.floor(x) + 0.5, (double)MathHelper.floor(y), (double)MathHelper.floor(z) + 0.5);
   }

   private void throwPearl(float yaw, float pitch) {
      int slot = InventoryUtil.searchItemHotbar(Items.ENDER_PEARL);
      if (slot != -1) {
         int previous = this.mc.player.getInventory().selectedSlot;
         float prevYaw = this.mc.player.getYaw();
         float prevPitch = this.mc.player.getPitch();
         if (slot != previous) {
            this.mc.player.getInventory().selectedSlot = slot;
            this.mc.interactionManager.syncSelectedSlot();
         }

         this.mc.player.setYaw(yaw);
         this.mc.player.setPitch(pitch);
         this.mc.interactionManager.interactItem(this.mc.player, Hand.MAIN_HAND);
         this.mc.player.swingHand(Hand.MAIN_HAND);
         if (slot != previous) {
            this.mc.player.getInventory().selectedSlot = previous;
            this.mc.interactionManager.syncSelectedSlot();
         }

         if (this.restoreLook.getValue()) {
            this.mc.player.setYaw(prevYaw);
            this.mc.player.setPitch(prevPitch);
         }
      }
   }
}
