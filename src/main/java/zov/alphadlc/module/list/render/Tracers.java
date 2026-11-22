package zov.alphadlc.module.list.render;

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
import zov.alphadlc.AlphaDLC;
import zov.alphadlc.event.list.EventWorldRender;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.list.combat.AntiBot;
import zov.alphadlc.module.list.player.FreeCamera;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.util.friend.FriendRepository;
import zov.alphadlc.util.render.lines.VertexUtil;
import zov.alphadlc.util.render.providers.ColorProvider;

@ModuleInformation(
   moduleName = "Tracers",
   moduleDesc = "\u041b\u0438\u043d\u0438\u0438 \u0434\u043e \u0438\u0433\u0440\u043e\u043a\u043e\u0432 \u0438 \u0441\u0443\u0449\u043d\u043e\u0441\u0442\u0435\u0439",
   moduleCategory = ModuleCategory.RENDER
)
public class Tracers extends Module {
   private final BooleanSetting onlyWhenNotVisible = new BooleanSetting(
      "\u041d\u0435 \u0432 \u043f\u043e\u043b\u0435 \u0437\u0440\u0435\u043d\u0438\u044f", false
   );
   private final BooleanSetting onlyNetherite = new BooleanSetting(
      "\u041d\u0435\u0437\u0435\u0440\u0438\u0442\u043e\u0432\u0430\u044f \u0431\u0440\u043e\u043d\u044f", false
   );
   private final BooleanSetting pinkElytra = new BooleanSetting(
      "\u0420\u043e\u0437\u043e\u0432\u044b\u0439 \u0446\u0432\u0435\u0442 \u042d\u043b\u0438\u0442\u0440", false
   );

   @Subscribe
   public void onWorldRender(EventWorldRender event) {
      MatrixStack stack = event.getMatrixStack();
      float tickDelta = this.mc.method_61966().method_60637(true);
      Vec3d cameraPos = this.mc.field_1773.method_19418().method_19326();
      Vector3f lookVec = this.mc.field_1773.method_19418().method_19335();
      Vec3d eyePos = cameraPos.method_1019(new Vec3d(lookVec).method_1021(3.0));
      stack.method_22903();
      RenderSystem.enableBlend();
      GL11.glEnable(2848);
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableDepthTest();
      RenderSystem.disableCull();
      RenderSystem.setShader(ShaderProgramKeys.field_53864);
      RenderSystem.lineWidth(1.0F);
      BufferBuilder buffer = Tessellator.method_1348().method_60827(DrawMode.field_27377, VertexFormats.field_29337);
      boolean need = false;

      for (LivingEntity entity : this.collectTargets()) {
         if ((!this.onlyWhenNotVisible.getValue() || !this.mc.field_1769.field_27740.method_23093(entity.method_5829())) && entity != this.mc.field_1724) {
            double tx = entity.field_6014 + (entity.method_23317() - entity.field_6014) * (double)tickDelta;
            double ty = entity.field_6036 + (entity.method_23318() - entity.field_6036) * (double)tickDelta;
            double tz = entity.field_5969 + (entity.method_23321() - entity.field_5969) * (double)tickDelta;
            Vec3d targetPos = new Vec3d(tx, ty, tz);
            Color color = new Color(
               FriendRepository.isFriend(entity.method_5820())
                  ? ColorProvider.rgba(0, 255, 0, 255.0F)
                  : (this.pinkElytra.getValue() && this.isWearingElytra(entity) ? ColorProvider.rgba(255, 105, 180, 255.0F) : -1)
            );
            VertexUtil.vertexLine(
               stack,
               buffer,
               (float)(eyePos.field_1352 - cameraPos.field_1352),
               (float)(eyePos.field_1351 - cameraPos.field_1351),
               (float)(eyePos.field_1350 - cameraPos.field_1350),
               (float)(targetPos.field_1352 - cameraPos.field_1352),
               (float)(targetPos.field_1351 - cameraPos.field_1351),
               (float)(targetPos.field_1350 - cameraPos.field_1350),
               color
            );
            need = true;
         }
      }

      if (need) {
         BufferRenderer.method_43433(buffer.method_60800());
      }

      RenderSystem.enableCull();
      RenderSystem.enableDepthTest();
      GL11.glDisable(2848);
      RenderSystem.disableBlend();
      stack.method_22909();
   }

   private List<LivingEntity> collectTargets() {
      List<LivingEntity> list = new ArrayList<>();

      for (Entity entity : this.mc.field_1687.method_18112()) {
         if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)entity;
            if (living instanceof PlayerEntity) {
               PlayerEntity playerEntity = (PlayerEntity)living;
               if (this.mc.method_1562() != null && this.mc.method_1562().method_2871(playerEntity.method_5667()) != null) {
                  if (FriendRepository.isFriend(living.method_5820())) {
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
      for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.field_6169, EquipmentSlot.field_6174, EquipmentSlot.field_6172, EquipmentSlot.field_6166}) {
         ItemStack stack = player.method_6118(slot);
         if (this.isNetheriteArmor(stack)) {
            return true;
         }
      }

      return false;
   }

   private boolean isWearingElytra(LivingEntity entity) {
      ItemStack chestStack = entity.method_6118(EquipmentSlot.field_6174);
      return chestStack.method_31574(Items.field_8833);
   }

   private boolean isNetheriteArmor(ItemStack stack) {
      return stack.method_31574(Items.field_22027)
         || stack.method_31574(Items.field_22028)
         || stack.method_31574(Items.field_22029)
         || stack.method_31574(Items.field_22030);
   }

   private boolean isValid(Entity entity) {
      if (!entity.method_5805()) {
         return false;
      } else if (entity == AlphaDLC.getInstance().getModuleStorage().get(FreeCamera.class).fakePlayer) {
         return false;
      } else if (entity instanceof ClientPlayerEntity) {
         return false;
      } else if (entity instanceof ArmorStandEntity) {
         return false;
      } else if (entity instanceof HostileEntity || entity instanceof AmbientEntity) {
         return false;
      } else if (!(entity instanceof PassiveEntity) && !(entity instanceof FishEntity)) {
         if (entity instanceof PlayerEntity p) {
            AntiBot var10000 = AlphaDLC.getInstance().getModuleStorage().get(AntiBot.class);
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
