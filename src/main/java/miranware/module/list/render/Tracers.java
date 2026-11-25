package miranware.module.list.render;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import miranware.MiranWare;
import miranware.event.list.EventWorldRender;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.list.combat.AntiBot;
import miranware.module.list.player.FreeCamera;
import miranware.module.settings.BooleanSetting;
import miranware.util.friend.FriendRepository;
import miranware.util.render.lines.VertexUtil;
import miranware.util.render.providers.ColorProvider;

@ModuleInformation(
   moduleName = "Tracers",
   moduleDesc = "Lines to players and entities",
   moduleCategory = ModuleCategory.RENDER
)
public class Tracers extends Module {
   private final BooleanSetting onlyWhenNotVisible = new BooleanSetting(
      "Not In View", false
   );
   private final BooleanSetting onlyNetherite = new BooleanSetting(
      "Netherite Armor", false
   );
   private final BooleanSetting pinkElytra = new BooleanSetting(
      "Pink Elytra Color", false
   );

   @Subscribe
   public void onWorldRender(EventWorldRender event) {
      MatrixStack stack = event.getMatrixStack();
      float tickDelta = this.mc.getRenderTickCounter().getTickDelta(true);
      Vec3d cameraPos = this.mc.gameRenderer.getCamera().getPos();
      Vector3f lookVec = this.mc.gameRenderer.getCamera().getHorizontalPlane();
      Vec3d eyePos = cameraPos.add(new Vec3d(lookVec).multiply(3.0));
      stack.push();
      RenderSystem.enableBlend();
      GL11.glEnable(2848);
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableDepthTest();
      RenderSystem.disableCull();
      RenderSystem.setShader(ShaderProgramKeys.RENDERTYPE_LINES);
      RenderSystem.lineWidth(1.0F);
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.LINES, VertexFormats.LINES);
      boolean need = false;

      for (LivingEntity entity : this.collectTargets()) {
         if ((!this.onlyWhenNotVisible.getValue() || !this.mc.worldRenderer.frustum.isVisible(entity.getBoundingBox())) && entity != this.mc.player) {
            double tx = entity.prevX + (entity.getX() - entity.prevX) * (double)tickDelta;
            double ty = entity.prevY + (entity.getY() - entity.prevY) * (double)tickDelta;
            double tz = entity.prevZ + (entity.getZ() - entity.prevZ) * (double)tickDelta;
            Vec3d targetPos = new Vec3d(tx, ty, tz);
            Color color = new Color(
               FriendRepository.isFriend(entity.getNameForScoreboard())
                  ? ColorProvider.rgba(0, 255, 0, 255.0F)
                  : (this.pinkElytra.getValue() && this.isWearingElytra(entity) ? ColorProvider.rgba(255, 105, 180, 255.0F) : -1)
            );
            VertexUtil.vertexLine(
               stack,
               buffer,
               (float)(eyePos.x - cameraPos.x),
               (float)(eyePos.y - cameraPos.y),
               (float)(eyePos.z - cameraPos.z),
               (float)(targetPos.x - cameraPos.x),
               (float)(targetPos.y - cameraPos.y),
               (float)(targetPos.z - cameraPos.z),
               color
            );
            need = true;
         }
      }

      if (need) {
         BufferRenderer.drawWithGlobalProgram(buffer.end());
      }

      RenderSystem.enableCull();
      RenderSystem.enableDepthTest();
      GL11.glDisable(2848);
      RenderSystem.disableBlend();
      stack.pop();
   }

   private List<LivingEntity> collectTargets() {
      List<LivingEntity> list = new ArrayList<>();

      for (Entity entity : this.mc.world.getEntities()) {
         if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)entity;
            if (living instanceof PlayerEntity) {
               PlayerEntity playerEntity = (PlayerEntity)living;
               if (this.mc.getNetworkHandler() != null && this.mc.getNetworkHandler().getPlayerListEntry(playerEntity.getUuid()) != null) {
                  if (FriendRepository.isFriend(living.getNameForScoreboard())) {
                     if (!this.onlyNetherite.getValue() || this.isWearingAnyNetherite(playerEntity)) {
                        list.add(living);
                     }
                  } else if (this.isValid(living) && (!this.onlyNetherite.getValue() || this.isWearingAnyNetherite(playerEntity))) {
                     list.add(living);
                  }
               }
            }
         }
      }

      return list;
   }

   private boolean isWearingAnyNetherite(PlayerEntity player) {
      for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
         ItemStack stack = player.getEquippedStack(slot);
         if (this.isNetheriteArmor(stack)) {
            return true;
         }
      }

      return false;
   }

   private boolean isWearingElytra(LivingEntity entity) {
      ItemStack chestStack = entity.getEquippedStack(EquipmentSlot.CHEST);
      return chestStack.isOf(Items.ELYTRA);
   }

   private boolean isNetheriteArmor(ItemStack stack) {
      return stack.isOf(Items.NETHERITE_HELMET)
         || stack.isOf(Items.NETHERITE_CHESTPLATE)
         || stack.isOf(Items.NETHERITE_LEGGINGS)
         || stack.isOf(Items.NETHERITE_BOOTS);
   }

   private boolean isValid(Entity entity) {
      if (!entity.isAlive()) {
         return false;
      } else if (entity == MiranWare.getInstance().getModuleStorage().get(FreeCamera.class).fakePlayer) {
         return false;
      } else if (entity instanceof ClientPlayerEntity) {
         return false;
      } else if (entity instanceof ArmorStandEntity) {
         return false;
      } else if (entity instanceof HostileEntity || entity instanceof AmbientEntity) {
         return false;
      } else if (!(entity instanceof PassiveEntity) && !(entity instanceof FishEntity)) {
         if (entity instanceof PlayerEntity p) {
            AntiBot var10000 = MiranWare.getInstance().getModuleStorage().get(AntiBot.class);
            if (AntiBot.isBot(p)) {
               return false;
            }

            if (!FriendRepository.shouldAttack(p)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }
}
