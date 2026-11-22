package zov.alphadlc.module.list.movement;

import com.google.common.eventbus.Subscribe;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import zov.alphadlc.event.list.EventPacket;
import zov.alphadlc.event.list.EventPlayerUpdate;
import zov.alphadlc.event.list.MoveInputEvent;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.ModeSetting;
import zov.alphadlc.util.player.move.MoveUtil;

@ModuleInformation(
   moduleName = "Speed",
   moduleDesc = "\u0423\u0432\u0435\u043b\u0438\u0447\u0435\u043d\u0438\u0435 \u0441\u043a\u043e\u0440\u043e\u0441\u0442\u0438 \u043f\u0435\u0440\u0435\u0434\u0432\u0438\u0436\u0435\u043d\u0438\u044f",
   moduleCategory = ModuleCategory.MOVEMENT
)
public class Speed extends Module {
   private final ModeSetting mode = new ModeSetting("Mode", "RW", "RW", "Collision");
   private int ticks;
   private int groundTicks;
   final Map<Entity, Vec3d> previousPositions = new HashMap<>();

   @Subscribe
   public void onPlayerUpdate(EventPlayerUpdate event) {
      if (this.mode.is("Collision")) {
         this.tickCollision();
      } else if (this.mode.is("RW")) {
         this.tickRW();
      }
   }

   private void tickRW() {
      if (!this.canUseRW()) {
         this.resetRWState(true);
      } else {
         if (this.ticks > 3) {
            double boost = 0.03;
            if (this.ticks % 2 == 0) {
               this.mc.player.addVelocity(0.0, 0.03, 0.0);
               boost = this.mc.player.isOnGround() ? 0.085 : 0.03;
            }

            double yaw = Math.toRadians(MoveUtil.getDirection());
            double x2 = -Math.sin(yaw);
            double z2 = Math.cos(yaw);
            if (this.mc.player.input.movementForward == -1.0F) {
               x2 = 0.0;
               z2 = 0.0;
            }

            this.mc.player.addVelocity(x2 * boost, 0.0, z2 * boost);
         }

         this.ticks++;
         if (this.ticks % 2 == 0) {
            this.mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(this.mc.player, Mode.START_FALL_FLYING));
         }
      }
   }

   private void tickCollision() {
      if (MoveUtil.isMoving()) {
         double scanRadius = 1.25;
         Box searchBox = this.mc.player.getBoundingBox().expand(scanRadius);

         for (Entity entity2 : this.mc.world.getOtherEntities(this.mc.player, searchBox, entity -> entity instanceof PlayerEntity)) {
            if (entity2 instanceof PlayerEntity) {
               double distanceX = Math.abs(this.mc.player.getX() - entity2.getX());
               double distanceZ = Math.abs(this.mc.player.getZ() - entity2.getZ());
               if (!(distanceX > 2.1) && !(distanceZ > 1.3)) {
                  double entitySpeed = this.getEntitySpeed(entity2);
                  if (entitySpeed < 5.0) {
                     double boostAmount = 0.02;
                     Box collisionBox = this.mc.player.getBoundingBox().expand(0.1);
                     List<Entity> collisionEntities = this.mc.world.getOtherEntities(this.mc.player, collisionBox, e2 -> e2 instanceof PlayerEntity);
                     if (!collisionEntities.isEmpty()) {
                        double[] motion = this.forward(boostAmount);
                        this.mc.player.addVelocity(motion[0], 0.0, motion[1]);
                     }
                  } else {
                     double boostAmount = 0.032;
                     Box checkBox = this.mc.player.getBoundingBox().expand(1.25);
                     List<Entity> potentialCollisions = this.mc.world.getOtherEntities(this.mc.player, checkBox, e2 -> e2 instanceof PlayerEntity);
                     int collisions = 0;

                     for (Entity collisionEntity : potentialCollisions) {
                        double distToCollision = (double)this.mc.player.distanceTo(collisionEntity);
                        if (distToCollision <= 1.25) {
                           collisions++;
                        }
                     }

                     if (collisions > 0) {
                        double[] motion = this.forward(boostAmount);
                        this.mc.player.addVelocity(motion[0], 0.0, motion[1]);
                     }
                  }
                  break;
               }
            }
         }
      }
   }

   @Subscribe
   public void onMoveInput(MoveInputEvent event) {
      if (this.mode.is("RW") && this.mc.player != null && MoveUtil.isMoving()) {
         this.groundTicks = this.mc.player.verticalCollision ? ++this.groundTicks : 0;
         if (this.groundTicks >= 1) {
            this.mc.player.jump();
         }
      } else {
         this.groundTicks = 0;
      }
   }

   @Subscribe
   public void onPacket(EventPacket event) {
      if (this.mode.is("RW") && this.mc.player != null && event.getType() == EventPacket.Type.RECEIVE) {
         if (event.getPacket() instanceof PlayerPositionLookS2CPacket && this.ticks % 2 == 1) {
            this.ticks++;
         }
      }
   }

   public double getEntitySpeed(Entity entity) {
      Vec3d currentPos = entity.getPos();
      Vec3d previousPos = this.previousPositions.getOrDefault(entity, currentPos);
      double dx = currentPos.x - previousPos.x;
      double dz = currentPos.z - previousPos.z;
      double speed = Math.sqrt(dx * dx + dz * dz) * 20.0;
      this.previousPositions.put(entity, currentPos);
      return speed;
   }

   private double[] forward(double speed) {
      float forward = this.mc.player.input.movementForward;
      float strafe = this.mc.player.input.movementSideways;
      float yaw = this.mc.player.getYaw();
      if (forward != 0.0F) {
         if (strafe > 0.0F) {
            yaw += forward > 0.0F ? -45.0F : 45.0F;
         } else if (strafe < 0.0F) {
            yaw += forward > 0.0F ? 45.0F : -45.0F;
         }

         strafe = 0.0F;
         if (forward > 0.0F) {
            forward = 1.0F;
         } else if (forward < 0.0F) {
            forward = -1.0F;
         }
      }

      double sin = Math.sin(Math.toRadians((double)(yaw + 90.0F)));
      double cos = Math.cos(Math.toRadians((double)(yaw + 90.0F)));
      double posX = (double)forward * speed * cos + (double)strafe * speed * sin;
      double posZ = (double)forward * speed * sin - (double)strafe * speed * cos;
      return new double[]{posX, posZ};
   }

   private boolean canUseRW() {
      return this.mc.player != null
         && this.mc.world != null
         && this.mc.player.networkHandler != null
         && MoveUtil.isMoving()
         && !this.mc.player.hasVehicle()
         && !this.mc.player.getAbilities().flying;
   }

   private void resetRWState(boolean resetTimer) {
      this.ticks = 0;
      this.groundTicks = 0;
   }

   @Override
   public void onEnable() {
      this.resetRWState(true);
      super.onEnable();
   }

   @Override
   public void onDisable() {
      this.resetRWState(true);
      super.onDisable();
   }
}
