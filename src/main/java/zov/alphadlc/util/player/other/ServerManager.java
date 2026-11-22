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
      if (mc.field_1724 != null) {
         double y = mc.field_1724.field_6036 - mc.field_1724.method_23318();
         if (mc.field_1724.method_24828()) {
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
         if (packet.method_36171()) {
            this.serverX = packet.method_12269(mc.field_1724.method_23317());
            this.serverY = packet.method_12268(mc.field_1724.method_23318());
            this.serverZ = packet.method_12274(mc.field_1724.method_23321());
         }

         if (packet.method_36172()) {
            this.serverYaw = packet.method_12271(mc.field_1724.method_36454());
            this.serverPitch = packet.method_12270(mc.field_1724.method_36455());
         }

         this.serverOnGround = packet.method_12273();
         this.serverHorizontalCollision = packet.method_61225();
      }

      if (e.getPacket() instanceof UpdateSelectedSlotC2SPacket packet) {
         this.serverSlot = packet.method_12442();
      }

      if (e.getPacket() instanceof ClientCommandC2SPacket packet) {
         switch (packet.method_12365()) {
            case field_12979:
               this.serverSneaking = true;
               break;
            case field_12984:
               this.serverSneaking = false;
         }
      }

      if (e.getPacket() instanceof ClientCommandC2SPacket command) {
         if (command.method_12365().equals(Mode.field_12981)) {
            e.setCancelled(this.serverSprinting);
            if (!e.isCancelled()) {
               this.sprintingChangeTicks = 0.0F;
            }

            this.serverSprinting = true;
         } else if (command.method_12365().equals(Mode.field_12985)) {
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
      if (e.getPacket() instanceof EntityStatusS2CPacket packet && packet.method_11470() == 35) {
         if (!(packet.method_11469(mc.field_1687) instanceof PlayerEntity player)) {
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
