package zov.alphadlc.module.list.player;

import com.google.common.eventbus.Subscribe;
import com.mojang.authlib.GameProfile;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.player.PlayerPosition;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.util.math.Vec3d;
import zov.alphadlc.event.list.EventPacket;
import zov.alphadlc.event.list.EventTick;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.SliderSetting;
import zov.alphadlc.util.packet.NetworkUtils;
import zov.alphadlc.util.player.move.MoveUtil;

@ModuleInformation(
   moduleName = "Free Camera",
   moduleDesc = "\u0421\u0432\u043e\u0431\u043e\u0434\u043d\u0430\u044f \u043a\u0430\u043c\u0435\u0440\u0430 \u0431\u0435\u0437 \u0434\u0432\u0438\u0436\u0435\u043d\u0438\u044f \u0442\u0435\u043b\u0430",
   moduleCategory = ModuleCategory.PLAYER
)
public class FreeCamera extends Module {
   public final SliderSetting xyi = new SliderSetting("\u0421\u043a\u043e\u0440\u043e\u0441\u0442\u044c \u043f\u043e Y", 0.5, 0.1, 1.0, 0.1F);
   private Vec3d frozenPos;
   private float frozenYaw;
   private float frozenPitch;
   public OtherClientPlayerEntity fakePlayer;

   @Override
   public void onEnable() {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null && this.mc.method_1562() != null) {
         this.frozenPos = this.mc.field_1724.method_19538();
         this.frozenYaw = this.mc.field_1724.method_36454();
         this.frozenPitch = this.mc.field_1724.method_36455();
         GameProfile profile = new GameProfile(UUID.randomUUID(), this.mc.field_1724.method_5477().getString());
         this.fakePlayer = new OtherClientPlayerEntity(this.mc.field_1687, profile);
         this.fakePlayer.method_5878(this.mc.field_1724);
         this.fakePlayer.method_23327(this.frozenPos.field_1352, this.frozenPos.field_1351, this.frozenPos.field_1350);
         this.fakePlayer.method_36456(this.frozenYaw);
         this.fakePlayer.method_36457(this.frozenPitch);
         this.mc.field_1687.method_53875(this.fakePlayer);
         this.mc.field_1724.field_5960 = true;
         this.mc.field_1724.method_18799(Vec3d.field_1353);
         super.onEnable();
      }
   }

   @Override
   public void onDisable() {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
         this.mc.field_1724.field_5960 = false;
         this.mc.field_1724.method_23327(this.frozenPos.field_1352, this.frozenPos.field_1351, this.frozenPos.field_1350);
         this.mc.field_1724.method_36456(this.frozenYaw);
         this.mc.field_1724.method_36457(this.frozenPitch);
         this.mc.field_1724.method_18799(Vec3d.field_1353);
         if (this.fakePlayer != null) {
            this.mc.field_1687.method_2945(this.fakePlayer.method_5628(), RemovalReason.field_26999);
            this.fakePlayer = null;
         }

         super.onDisable();
      }
   }

   @Subscribe
   private void onLivingUpdate(EventTick e) {
      if (this.mc.field_1724 != null) {
         this.mc.field_1724.field_5960 = true;
         this.mc.field_1724.method_18799(Vec3d.field_1353);
         float speed = (float)this.xyi.getValue();
         Vec3d motion = Vec3d.field_1353;
         MoveUtil.setMotion(1.0);
         if (this.mc.field_1690.field_1903.method_1434()) {
            motion = motion.method_1031(0.0, (double)speed, 0.0);
         }

         if (this.mc.field_1690.field_1832.method_1434()) {
            motion = motion.method_1023(0.0, (double)speed, 0.0);
         }

         this.mc.field_1724.method_18800(this.mc.field_1724.method_18798().field_1352, motion.field_1351, this.mc.field_1724.method_18798().field_1350);
      }
   }

   private void setPosition(PlayerPosition pos, Set<PositionFlag> flags) {
      PlayerPosition playerPosition = PlayerPosition.method_64621(this.fakePlayer);
      PlayerPosition playerPosition2 = PlayerPosition.method_63639(playerPosition, pos, flags);
      this.frozenPos = new Vec3d(
         playerPosition2.comp_3148().method_10216(), playerPosition2.comp_3148().method_10214(), playerPosition2.comp_3148().method_10215()
      );
      this.frozenYaw = playerPosition2.comp_3150();
      this.frozenPitch = playerPosition2.comp_3151();
   }

   @Subscribe
   private void onPacket(EventPacket e) {
      if (e.getPacket() instanceof PlayerPositionLookS2CPacket packet) {
         if (this.mc.field_1687 == null || this.mc.field_1724 == null || !this.mc.field_1724.method_5805()) {
            return;
         }

         NetworkUtils.sendPacket(new TeleportConfirmC2SPacket(packet.comp_3133()));
         if (!this.mc.field_1724.method_5765()) {
            this.setPosition(packet.comp_3228(), packet.comp_3229());
         }

         NetworkUtils.sendSilentPacket(
            new Full(
               this.frozenPos.method_10216(), this.frozenPos.method_10214(), this.frozenPos.method_10215(), this.frozenYaw, this.frozenPitch, false, false
            )
         );
         e.cancelEvent();
      }

      if (e.getPacket() instanceof PlayerMoveC2SPacket) {
         e.cancelEvent();
      }

      if (e.getPacket() instanceof PlayerInteractBlockC2SPacket p) {
         NetworkUtils.sendPacket(
            new PlayerInteractItemC2SPacket(p.method_12546(), p.method_42080(), this.mc.field_1724.method_36454(), this.mc.field_1724.method_36455())
         );
         e.cancelEvent();
      }

      if (e.getPacket() instanceof PlayerRespawnS2CPacket) {
         this.setEnabled(false);
      }

      if (e.getPacket() instanceof GameJoinS2CPacket) {
         this.setEnabled(false);
      }

      if (e.getPacket() instanceof DisconnectS2CPacket) {
         this.setEnabled(false);
      }
   }
}
