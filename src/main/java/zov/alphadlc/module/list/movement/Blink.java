package zov.alphadlc.module.list.movement;

import com.google.common.eventbus.Subscribe;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.Last;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket.Mode;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.util.math.Box;
import org.joml.Matrix4f;
import zov.alphadlc.event.list.EventPacket;
import zov.alphadlc.event.list.EventTick;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.module.settings.ModeSetting;
import zov.alphadlc.module.settings.SliderSetting;
import zov.alphadlc.util.packet.NetworkUtils;

@ModuleInformation(
   moduleName = "Blink",
   moduleDesc = "\u0417\u0430\u0434\u0435\u0440\u0436\u0438\u0432\u0430\u0435\u0442 \u043f\u0430\u043a\u0435\u0442\u044b \u0434\u0432\u0438\u0436\u0435\u043d\u0438\u044f",
   moduleCategory = ModuleCategory.MOVEMENT
)
public class Blink extends Module {
   private final BooleanSetting dummy = new BooleanSetting("\u0424\u0435\u0439\u043a \u0438\u0433\u0440\u043e\u043a", true);
   private final BooleanSetting ambush = new BooleanSetting("\u0417\u0430\u0441\u0430\u0434\u0430", false);
   private final BooleanSetting renderBox = new BooleanSetting("\u0420\u0435\u043d\u0434\u0435\u0440 \u0431\u043e\u043a\u0441\u0430", true);
   private final BooleanSetting autoReset = new BooleanSetting("\u0410\u0432\u0442\u043e \u0441\u0431\u0440\u043e\u0441", false);
   private final SliderSetting resetAfter = new SliderSetting("\u0421\u0431\u0440\u043e\u0441 \u043f\u043e\u0441\u043b\u0435", 100.0, 1.0, 1000.0, 1.0)
      .setVisible(this.autoReset::getValue);
   private final ModeSetting resetAction = new ModeSetting("\u0414\u0435\u0439\u0441\u0442\u0432\u0438\u0435", "Blink", "Blink", "Reset")
      .setVisible(this.autoReset::getValue);
   private final BooleanSetting autoDisable = new BooleanSetting("\u0410\u0432\u0442\u043e \u0432\u044b\u043a\u043b", true)
      .setVisible(this.autoReset::getValue);
   private final CopyOnWriteArrayList<Packet<?>> packets = new CopyOnWriteArrayList<>();
   private OtherClientPlayerEntity dummyPlayer = null;
   private Box box = null;
   private boolean renderListenerRegistered = false;
   private final Last renderListener = this::onRender3D;

   @Override
   public void onEnable() {
      super.onEnable();
      if (this.mc.player != null && this.mc.world != null) {
         this.box = this.mc.player.getBoundingBox();
         this.packets.clear();
         if (this.dummy.getValue()) {
            this.createDummyPlayer();
         }

         if (!this.renderListenerRegistered) {
            WorldRenderEvents.LAST.register(this.renderListener);
            this.renderListenerRegistered = true;
         }
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.flushPackets();
      this.removeDummyPlayer();
      this.packets.clear();
      this.box = null;
   }

   @Subscribe
   public void onPacket(EventPacket e) {
      if (this.mc.player != null && this.mc.world != null) {
         Packet<?> packet = e.getPacket();
         if (e.getType() != EventPacket.Type.RECEIVE || !(packet instanceof PlayerRespawnS2CPacket) && !(packet instanceof GameJoinS2CPacket)) {
            if (e.getType() == EventPacket.Type.SEND) {
               if (packet instanceof ClientStatusC2SPacket status && status.getMode() == Mode.PERFORM_RESPAWN) {
                  this.setEnabled(false);
                  return;
               }

               if (this.ambush.getValue() && packet instanceof PlayerInteractEntityC2SPacket) {
                  this.setEnabled(false);
                  return;
               }

               this.packets.add(packet);
               e.setCancelled(true);
            }
         } else {
            this.setEnabled(false);
         }
      }
   }

   @Subscribe
   public void onTick(EventTick event) {
      if (this.mc.player != null && this.mc.world != null) {
         if (this.autoReset.getValue() && this.packets.size() > this.resetAfter.getIntValue()) {
            if (this.resetAction.is("Reset")) {
               this.cancelPackets();
            } else if (this.resetAction.is("Blink")) {
               this.flushPackets();
               if (this.dummyPlayer != null) {
                  this.dummyPlayer.copyPositionAndRotation(this.mc.player);
               }

               this.box = this.mc.player.getBoundingBox();
            }

            if (this.autoDisable.getValue()) {
               this.setEnabled(false);
            }
         }
      }
   }

   private void onRender3D(WorldRenderContext context) {
      if (this.isEnabled() && this.mc.player != null && this.renderBox.getValue() && this.box != null) {
         Camera camera = context.camera();
         double camX = camera.getPos().x;
         double camY = camera.getPos().y;
         double camZ = camera.getPos().z;
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder buffer = tessellator.begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
         Matrix4f matrix = context.matrixStack().peek().getPositionMatrix();
         double minX = this.box.minX - camX;
         double minY = this.box.minY - camY;
         double minZ = this.box.minZ - camZ;
         double maxX = this.box.maxX - camX;
         double maxY = this.box.maxY - camY;
         double maxZ = this.box.maxZ - camZ;
         buffer.vertex(matrix, (float)minX, (float)minY, (float)minZ).color(255, 255, 255, 255);
         buffer.vertex(matrix, (float)maxX, (float)minY, (float)minZ).color(255, 255, 255, 255);
         buffer.vertex(matrix, (float)maxX, (float)minY, (float)minZ).color(255, 255, 255, 255);
         buffer.vertex(matrix, (float)maxX, (float)minY, (float)maxZ).color(255, 255, 255, 255);
         buffer.vertex(matrix, (float)maxX, (float)minY, (float)maxZ).color(255, 255, 255, 255);
         buffer.vertex(matrix, (float)minX, (float)minY, (float)maxZ).color(255, 255, 255, 255);
         buffer.vertex(matrix, (float)minX, (float)minY, (float)maxZ).color(255, 255, 255, 255);
         buffer.vertex(matrix, (float)minX, (float)minY, (float)minZ).color(255, 255, 255, 255);
         buffer.vertex(matrix, (float)minX, (float)maxY, (float)minZ).color(255, 255, 255, 255);
         buffer.vertex(matrix, (float)maxX, (float)maxY, (float)minZ).color(255, 255, 255, 255);
         buffer.vertex(matrix, (float)maxX, (float)maxY, (float)minZ).color(255, 255, 255, 255);
         buffer.vertex(matrix, (float)maxX, (float)maxY, (float)maxZ).color(255, 255, 255, 255);
         buffer.vertex(matrix, (float)maxX, (float)maxY, (float)maxZ).color(255, 255, 255, 255);
         buffer.vertex(matrix, (float)minX, (float)maxY, (float)maxZ).color(255, 255, 255, 255);
         buffer.vertex(matrix, (float)minX, (float)maxY, (float)maxZ).color(255, 255, 255, 255);
         buffer.vertex(matrix, (float)minX, (float)maxY, (float)minZ).color(255, 255, 255, 255);
         buffer.vertex(matrix, (float)minX, (float)minY, (float)minZ).color(255, 255, 255, 255);
         buffer.vertex(matrix, (float)minX, (float)maxY, (float)minZ).color(255, 255, 255, 255);
         buffer.vertex(matrix, (float)maxX, (float)minY, (float)minZ).color(255, 255, 255, 255);
         buffer.vertex(matrix, (float)maxX, (float)maxY, (float)minZ).color(255, 255, 255, 255);
         buffer.vertex(matrix, (float)maxX, (float)minY, (float)maxZ).color(255, 255, 255, 255);
         buffer.vertex(matrix, (float)maxX, (float)maxY, (float)maxZ).color(255, 255, 255, 255);
         buffer.vertex(matrix, (float)minX, (float)minY, (float)maxZ).color(255, 255, 255, 255);
         buffer.vertex(matrix, (float)minX, (float)maxY, (float)maxZ).color(255, 255, 255, 255);
         BufferRenderer.drawWithGlobalProgram(buffer.end());
         RenderSystem.depthMask(true);
         RenderSystem.enableDepthTest();
         RenderSystem.disableBlend();
      }
   }

   private void createDummyPlayer() {
      if (this.mc.player != null && this.mc.world != null) {
         GameProfile profile = new GameProfile(UUID.randomUUID(), this.mc.player.getName().getString());
         this.dummyPlayer = new OtherClientPlayerEntity(this.mc.world, profile);
         this.dummyPlayer.copyFrom(this.mc.player);
         this.dummyPlayer.setPos(this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ());
         this.dummyPlayer.setHeadYaw(this.mc.player.getHeadYaw());
         this.mc.world.addEntity(this.dummyPlayer);
      }
   }

   private void removeDummyPlayer() {
      if (this.dummyPlayer != null && this.mc.world != null) {
         this.mc.world.removeEntity(this.dummyPlayer.getId(), RemovalReason.DISCARDED);
         this.dummyPlayer = null;
      }
   }

   private void flushPackets() {
      for (Packet<?> packet : this.packets) {
         NetworkUtils.sendSilentPacket(packet);
      }

      this.packets.clear();
   }

   private void cancelPackets() {
      this.packets.clear();
   }
}
