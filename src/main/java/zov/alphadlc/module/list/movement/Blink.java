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
      if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
         this.box = this.mc.field_1724.method_5829();
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
      if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
         Packet<?> packet = e.getPacket();
         if (e.getType() != EventPacket.Type.RECEIVE || !(packet instanceof PlayerRespawnS2CPacket) && !(packet instanceof GameJoinS2CPacket)) {
            if (e.getType() == EventPacket.Type.SEND) {
               if (packet instanceof ClientStatusC2SPacket status && status.method_12119() == Mode.field_12774) {
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
      if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
         if (this.autoReset.getValue() && this.packets.size() > this.resetAfter.getIntValue()) {
            if (this.resetAction.is("Reset")) {
               this.cancelPackets();
            } else if (this.resetAction.is("Blink")) {
               this.flushPackets();
               if (this.dummyPlayer != null) {
                  this.dummyPlayer.method_5719(this.mc.field_1724);
               }

               this.box = this.mc.field_1724.method_5829();
            }

            if (this.autoDisable.getValue()) {
               this.setEnabled(false);
            }
         }
      }
   }

   private void onRender3D(WorldRenderContext context) {
      if (this.isEnabled() && this.mc.field_1724 != null && this.renderBox.getValue() && this.box != null) {
         Camera camera = context.camera();
         double camX = camera.method_19326().field_1352;
         double camY = camera.method_19326().field_1351;
         double camZ = camera.method_19326().field_1350;
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.setShader(ShaderProgramKeys.field_53876);
         Tessellator tessellator = Tessellator.method_1348();
         BufferBuilder buffer = tessellator.method_60827(DrawMode.field_29344, VertexFormats.field_1576);
         Matrix4f matrix = context.matrixStack().method_23760().method_23761();
         double minX = this.box.field_1323 - camX;
         double minY = this.box.field_1322 - camY;
         double minZ = this.box.field_1321 - camZ;
         double maxX = this.box.field_1320 - camX;
         double maxY = this.box.field_1325 - camY;
         double maxZ = this.box.field_1324 - camZ;
         buffer.method_22918(matrix, (float)minX, (float)minY, (float)minZ).method_1336(255, 255, 255, 255);
         buffer.method_22918(matrix, (float)maxX, (float)minY, (float)minZ).method_1336(255, 255, 255, 255);
         buffer.method_22918(matrix, (float)maxX, (float)minY, (float)minZ).method_1336(255, 255, 255, 255);
         buffer.method_22918(matrix, (float)maxX, (float)minY, (float)maxZ).method_1336(255, 255, 255, 255);
         buffer.method_22918(matrix, (float)maxX, (float)minY, (float)maxZ).method_1336(255, 255, 255, 255);
         buffer.method_22918(matrix, (float)minX, (float)minY, (float)maxZ).method_1336(255, 255, 255, 255);
         buffer.method_22918(matrix, (float)minX, (float)minY, (float)maxZ).method_1336(255, 255, 255, 255);
         buffer.method_22918(matrix, (float)minX, (float)minY, (float)minZ).method_1336(255, 255, 255, 255);
         buffer.method_22918(matrix, (float)minX, (float)maxY, (float)minZ).method_1336(255, 255, 255, 255);
         buffer.method_22918(matrix, (float)maxX, (float)maxY, (float)minZ).method_1336(255, 255, 255, 255);
         buffer.method_22918(matrix, (float)maxX, (float)maxY, (float)minZ).method_1336(255, 255, 255, 255);
         buffer.method_22918(matrix, (float)maxX, (float)maxY, (float)maxZ).method_1336(255, 255, 255, 255);
         buffer.method_22918(matrix, (float)maxX, (float)maxY, (float)maxZ).method_1336(255, 255, 255, 255);
         buffer.method_22918(matrix, (float)minX, (float)maxY, (float)maxZ).method_1336(255, 255, 255, 255);
         buffer.method_22918(matrix, (float)minX, (float)maxY, (float)maxZ).method_1336(255, 255, 255, 255);
         buffer.method_22918(matrix, (float)minX, (float)maxY, (float)minZ).method_1336(255, 255, 255, 255);
         buffer.method_22918(matrix, (float)minX, (float)minY, (float)minZ).method_1336(255, 255, 255, 255);
         buffer.method_22918(matrix, (float)minX, (float)maxY, (float)minZ).method_1336(255, 255, 255, 255);
         buffer.method_22918(matrix, (float)maxX, (float)minY, (float)minZ).method_1336(255, 255, 255, 255);
         buffer.method_22918(matrix, (float)maxX, (float)maxY, (float)minZ).method_1336(255, 255, 255, 255);
         buffer.method_22918(matrix, (float)maxX, (float)minY, (float)maxZ).method_1336(255, 255, 255, 255);
         buffer.method_22918(matrix, (float)maxX, (float)maxY, (float)maxZ).method_1336(255, 255, 255, 255);
         buffer.method_22918(matrix, (float)minX, (float)minY, (float)maxZ).method_1336(255, 255, 255, 255);
         buffer.method_22918(matrix, (float)minX, (float)maxY, (float)maxZ).method_1336(255, 255, 255, 255);
         BufferRenderer.method_43433(buffer.method_60800());
         RenderSystem.depthMask(true);
         RenderSystem.enableDepthTest();
         RenderSystem.disableBlend();
      }
   }

   private void createDummyPlayer() {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
         GameProfile profile = new GameProfile(UUID.randomUUID(), this.mc.field_1724.method_5477().getString());
         this.dummyPlayer = new OtherClientPlayerEntity(this.mc.field_1687, profile);
         this.dummyPlayer.method_5878(this.mc.field_1724);
         this.dummyPlayer.method_23327(this.mc.field_1724.method_23317(), this.mc.field_1724.method_23318(), this.mc.field_1724.method_23321());
         this.dummyPlayer.method_5847(this.mc.field_1724.method_5791());
         this.mc.field_1687.method_53875(this.dummyPlayer);
      }
   }

   private void removeDummyPlayer() {
      if (this.dummyPlayer != null && this.mc.field_1687 != null) {
         this.mc.field_1687.method_2945(this.dummyPlayer.method_5628(), RemovalReason.field_26999);
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
