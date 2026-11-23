package miranware.module.list.movement;

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
import miranware.event.list.EventPacket;
import miranware.event.list.EventTick;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.BooleanSetting;
import miranware.module.settings.ModeSetting;
import miranware.module.settings.SliderSetting;
import miranware.util.packet.NetworkUtils;

@ModuleInformation(
   moduleName = "Blink",
   moduleDesc = "Delays movement packets",
   moduleCategory = ModuleCategory.MOVEMENT
)
public class Blink extends Module {
   private final BooleanSetting dummy = new BooleanSetting("Fake Player", true);
   private final BooleanSetting ambush = new BooleanSetting("Ambush", false);
   private final BooleanSetting renderBox = new BooleanSetting("Render Box", true);
   private final BooleanSetting autoReset = new BooleanSetting("Auto Reset", false);
   private final SliderSetting resetAfter = new SliderSetting("Reset After", 100.0, 1.0, 1000.0, 1.0)
      .setVisible(this.autoReset::getValue);
   private final ModeSetting resetAction = new ModeSetting("Action", "Blink", "Blink", "Reset")
      .setVisible(this.autoReset::getValue);
   private final BooleanSetting autoDisable = new BooleanSetting("Auto Disable", true)
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
