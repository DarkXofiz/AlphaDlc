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
      if (this.mc.field_1724 != null && this.mc.field_1687 != null && this.mc.field_1761 != null) {
         if (!this.mc.field_1724.method_6128()) {
            if (System.currentTimeMillis() - this.lastThrow >= (long)this.throwDelay.getValue()) {
               if (!this.mc.field_1724.method_7357().method_7904(new ItemStack(Items.field_8634))) {
                  if (InventoryUtil.searchItemHotbar(Items.field_8634) != -1) {
                     LivingEntity target = this.resolveTarget();
                     if (target != null) {
                        Vec3d landing = target.method_19538();
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

         for (PlayerEntity player : this.mc.field_1687.method_18456()) {
            if (this.isValid(player)) {
               double distance = (double)this.mc.field_1724.method_5739(player);
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
      if (entity != null && entity != this.mc.field_1724 && entity.method_5805()) {
         if (entity instanceof PlayerEntity player && FriendRepository.isFriend(player.method_5820())) {
            return false;
         }

         return true;
      } else {
         return false;
      }
   }

   private float[] findRotation(Vec3d target) {
      Vec3d eye = this.mc.field_1724.method_33571();
      double dx = target.field_1352 - eye.field_1352;
      double dz = target.field_1350 - eye.field_1350;
      float yaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
      float bestPitch = 0.0F;
      double bestError = Double.MAX_VALUE;

      for (float pitch = 80.0F; pitch >= -30.0F; pitch -= 0.5F) {
         Vec3d landing = this.simulate(yaw, pitch);
         if (landing != null) {
            double error = landing.method_1025(target);
            if (error < bestError) {
               bestError = error;
               bestPitch = pitch;
            }
         }
      }

      if (bestError == Double.MAX_VALUE) {
         return null;
      } else {
         return bestError > 36.0 ? null : new float[]{yaw, MathHelper.method_15363(bestPitch, -90.0F, 90.0F)};
      }
   }

   private Vec3d simulate(float yaw, float pitch) {
      double yr = Math.toRadians((double)yaw);
      double pr = Math.toRadians((double)pitch);
      double x = this.mc.field_1724.method_23317() - Math.cos(yr) * 0.16;
      double y = this.mc.field_1724.method_23318() + (double)this.mc.field_1724.method_18381(this.mc.field_1724.method_18376()) - 0.1;
      double z = this.mc.field_1724.method_23321() - Math.sin(yr) * 0.16;
      double vx = -Math.sin(yr) * Math.cos(pr) * 1.5;
      double vy = -Math.sin(pr) * 1.5 + this.mc.field_1724.method_18798().field_1351;
      double vz = Math.cos(yr) * Math.cos(pr) * 1.5;
      double bottom = (double)this.mc.field_1687.method_31607();

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

         BlockPos bp = BlockPos.method_49637(x, y, z);
         if (!this.mc.field_1687.method_8320(bp).method_26220(this.mc.field_1687, bp).method_1110()) {
            return this.snap(x, y, z);
         }
      }

      return null;
   }

   private Vec3d snap(double x, double y, double z) {
      return new Vec3d((double)MathHelper.method_15357(x) + 0.5, (double)MathHelper.method_15357(y), (double)MathHelper.method_15357(z) + 0.5);
   }

   private void throwPearl(float yaw, float pitch) {
      int slot = InventoryUtil.searchItemHotbar(Items.field_8634);
      if (slot != -1) {
         int previous = this.mc.field_1724.method_31548().field_7545;
         float prevYaw = this.mc.field_1724.method_36454();
         float prevPitch = this.mc.field_1724.method_36455();
         if (slot != previous) {
            this.mc.field_1724.method_31548().field_7545 = slot;
            this.mc.field_1761.method_2911();
         }

         this.mc.field_1724.method_36456(yaw);
         this.mc.field_1724.method_36457(pitch);
         this.mc.field_1761.method_2919(this.mc.field_1724, Hand.field_5808);
         this.mc.field_1724.method_6104(Hand.field_5808);
         if (slot != previous) {
            this.mc.field_1724.method_31548().field_7545 = previous;
            this.mc.field_1761.method_2911();
         }

         if (this.restoreLook.getValue()) {
            this.mc.field_1724.method_36456(prevYaw);
            this.mc.field_1724.method_36457(prevPitch);
         }
      }
   }
}
