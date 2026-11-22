package zov.alphadlc.module.list.render;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import zov.alphadlc.event.list.EventHUD;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.ColorSetting;
import zov.alphadlc.module.settings.SliderSetting;
import zov.alphadlc.util.friend.FriendRepository;
import zov.alphadlc.util.render.providers.ColorProvider;

@ModuleInformation(
   moduleName = "Arrows",
   moduleDesc = "HUD-\u0441\u0442\u0440\u0435\u043b\u043a\u0438, \u0443\u043a\u0430\u0437\u044b\u0432\u0430\u044e\u0449\u0438\u0435 \u043d\u0430 \u0438\u0433\u0440\u043e\u043a\u043e\u0432",
   moduleCategory = ModuleCategory.RENDER
)
public class Arrows extends Module {
   private static final Identifier ICON = Identifier.method_60655("mre", "images/triangle.png");
   private final SliderSetting radius = new SliderSetting("\u0420\u0430\u0434\u0438\u0443\u0441", 50.0, 30.0, 120.0, 1.0);
   private final SliderSetting sizeSetting = new SliderSetting("\u0420\u0430\u0437\u043c\u0435\u0440", 32.0, 16.0, 64.0, 1.0);
   private final ColorSetting color = new ColorSetting("\u0426\u0432\u0435\u0442", -6913281);

   @Subscribe
   private void onHud(EventHUD e) {
      if (this.mc.field_1687 != null && this.mc.field_1724 != null) {
         if (!this.mc.field_1690.field_1842) {
            if (this.mc.field_1690.method_31044().equals(Perspective.field_26664)) {
               List<AbstractClientPlayerEntity> players = this.mc.field_1687.method_18456();
               if (!players.isEmpty()) {
                  DrawContext ctx = e.getDrawContext();
                  MatrixStack matrix = ctx.method_51448();
                  float tickDelta = this.mc.method_61966().method_60637(true);
                  float cameraYaw = this.mc.field_1773.method_19418().method_19330();
                  float middleW = (float)this.mc.method_22683().method_4486() / 2.0F;
                  float middleH = (float)this.mc.method_22683().method_4502() / 2.0F;
                  float posY = middleH - this.radius.getFloatValue();
                  float size = this.sizeSetting.getFloatValue();
                  RenderSystem.enableBlend();
                  RenderSystem.disableCull();
                  RenderSystem.disableDepthTest();
                  RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA);
                  RenderSystem.setShaderTexture(0, ICON);
                  RenderSystem.setShader(ShaderProgramKeys.field_53880);
                  BufferBuilder buffer = Tessellator.method_1348().method_60827(DrawMode.field_27382, VertexFormats.field_1575);

                  for (int i = 0; i < players.size(); i++) {
                     AbstractClientPlayerEntity player = players.get(i);
                     if (player != this.mc.field_1724 && player.method_5805() && !player.method_31481() && !player.method_7325()) {
                        int col = FriendRepository.isFriend(player.method_5820()) ? -16711936 : this.color.getValue();
                        int baseCol = ColorProvider.setAlpha(col, ColorProvider.alpha(col) / 2);
                        float yaw = this.getRotation(player, tickDelta) - cameraYaw;
                        matrix.method_22903();
                        matrix.method_46416(middleW, middleH, 0.0F);
                        matrix.method_22907(RotationAxis.field_40718.rotationDegrees(yaw));
                        matrix.method_46416(-middleW, -middleH, 0.0F);
                        Matrix4f m = matrix.method_23760().method_23761();
                        buffer.method_22918(m, middleW - size / 2.0F, posY + size, 0.0F).method_22913(0.0F, 1.0F).method_39415(baseCol);
                        buffer.method_22918(m, middleW + size / 2.0F, posY + size, 0.0F).method_22913(1.0F, 1.0F).method_39415(baseCol);
                        buffer.method_22918(m, middleW + size / 2.0F, posY, 0.0F).method_22913(1.0F, 0.0F).method_39415(col);
                        buffer.method_22918(m, middleW - size / 2.0F, posY, 0.0F).method_22913(0.0F, 0.0F).method_39415(col);
                        matrix.method_22909();
                     }
                  }

                  BuiltBuffer builtBuffer = buffer.method_60794();
                  if (builtBuffer != null) {
                     BufferRenderer.method_43433(builtBuffer);
                  }

                  RenderSystem.enableDepthTest();
                  RenderSystem.enableCull();
                  RenderSystem.defaultBlendFunc();
                  RenderSystem.disableBlend();
               }
            }
         }
      }
   }

   private float getRotation(AbstractClientPlayerEntity entity, float tickDelta) {
      Vec3d diff = entity.method_30950(tickDelta).method_1020(this.mc.field_1724.method_30950(tickDelta));
      return (float)(-(Math.atan2(diff.field_1352, diff.field_1350) * (180.0 / Math.PI)));
   }
}
