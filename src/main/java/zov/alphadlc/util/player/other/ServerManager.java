package zov.alphadlc.util.player.other;

import com.google.common.eventbus.Subscribe;
import lombok.Generated;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import zov.alphadlc.AlphaDLC;
import zov.alphadlc.event.list.EventPacket;
import zov.alphadlc.event.list.EventPlayerUpdate;
import zov.alphadlc.event.list.EventPopTotem;
import zov.alphadlc.event.list.EventTick;
import zov.alphadlc.util.IMinecraft;

public class ServerManager implements IMinecraft {
   private int serverSlot;
   private float serverYaw;
   private float serverPitch;
   private float fallDistance;
   private double serverX;
   private double serverY;
   private double serverZ;
   private boolean serverOnGround;
   private boolean serverSprinting;
   private boolean serverSneaking;
   private boolean serverHorizontalCollision;
   private float sprintingChangeTicks;

   public ServerManager() {
      AlphaDLC.getInstance().getEventBus().register(this);
   }

   @Subscribe
   private void onTick(EventTick ignored) {
      if (mc.player != null) {
         double y = mc.player.prevY - mc.player.getY();
         if (mc.player.isOnGround()) {
            this.fallDistance = 0.0F;
         } else if (y > 0.0) {
            this.fallDistance += (float)y;
         }
      }
   }

   @Subscribe
   private void onPlayerUpdate(EventPlayerUpdate ignored) {
      this.sprintingChangeTicks++;
   }

   @Subscribe
   public void onPacketSend(EventPacket e) {
      if (e.getPacket() instanceof PlayerMoveC2SPacket packet) {
         if (packet.changesPosition()) {
            this.serverX = packet.getX(mc.player.getX());
            this.serverY = packet.getY(mc.player.getY());
            this.serverZ = packet.getZ(mc.player.getZ());
         }

         if (packet.changesLook()) {
            this.serverYaw = packet.getYaw(mc.player.getYaw());
            this.serverPitch = packet.getPitch(mc.player.getPitch());
         }

         this.serverOnGround = packet.isOnGround();
         this.serverHorizontalCollision = packet.horizontalCollision();
      }

      if (e.getPacket() instanceof UpdateSelectedSlotC2SPacket packet) {
         this.serverSlot = packet.getSelectedSlot();
      }

      if (e.getPacket() instanceof ClientCommandC2SPacket packet) {
         switch (packet.getMode()) {
            case PRESS_SHIFT_KEY:
               this.serverSneaking = true;
               break;
            case RELEASE_SHIFT_KEY:
               this.serverSneaking = false;
         }
      }

      if (e.getPacket() instanceof ClientCommandC2SPacket command) {
         if (command.getMode().equals(Mode.START_SPRINTING)) {
            e.setCancelled(this.serverSprinting);
            if (!e.isCancelled()) {
               this.sprintingChangeTicks = 0.0F;
            }

            this.serverSprinting = true;
         } else if (command.getMode().equals(Mode.STOP_SPRINTING)) {
            e.setCancelled(!this.serverSprinting);
            if (!e.isCancelled()) {
               this.sprintingChangeTicks = 0.0F;
            }

            this.serverSprinting = false;
         }
      }
   }

   @Subscribe
   public void onPacketReceive(EventPacket e) {
      if (e.getPacket() instanceof EntityStatusS2CPacket packet && packet.getStatus() == 35) {
         if (!(packet.getEntity(mc.world) instanceof PlayerEntity player)) {
            return;
         }

         new EventPopTotem(player).post();
      }
   }

   @Generated
   public int getServerSlot() {
      return this.serverSlot;
   }

   @Generated
   public float getServerYaw() {
      return this.serverYaw;
   }

   @Generated
   public float getServerPitch() {
      return this.serverPitch;
   }

   @Generated
   public float getFallDistance() {
      return this.fallDistance;
   }

   @Generated
   public double getServerX() {
      return this.serverX;
   }

   @Generated
   public double getServerY() {
      return this.serverY;
   }

   @Generated
   public double getServerZ() {
      return this.serverZ;
   }

   @Generated
   public boolean isServerOnGround() {
      return this.serverOnGround;
   }

   @Generated
   public boolean isServerSprinting() {
      return this.serverSprinting;
   }

   @Generated
   public boolean isServerSneaking() {
      return this.serverSneaking;
   }

   @Generated
   public boolean isServerHorizontalCollision() {
      return this.serverHorizontalCollision;
   }

   @Generated
   public float getSprintingChangeTicks() {
      return this.sprintingChangeTicks;
   }
}
