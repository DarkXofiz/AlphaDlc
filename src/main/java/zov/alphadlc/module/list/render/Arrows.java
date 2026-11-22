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
   private static final Identifier ICON = Identifier.of("mre", "images/triangle.png");
   private final SliderSetting radius = new SliderSetting("\u0420\u0430\u0434\u0438\u0443\u0441", 50.0, 30.0, 120.0, 1.0);
   private final SliderSetting sizeSetting = new SliderSetting("\u0420\u0430\u0437\u043c\u0435\u0440", 32.0, 16.0, 64.0, 1.0);
   private final ColorSetting color = new ColorSetting("\u0426\u0432\u0435\u0442", -6913281);

   @Subscribe
   private void onHud(EventHUD e) {
      if (this.mc.world != null && this.mc.player != null) {
         if (!this.mc.options.hudHidden) {
            if (this.mc.options.getPerspective().equals(Perspective.FIRST_PERSON)) {
               List<AbstractClientPlayerEntity> players = this.mc.world.getPlayers();
               if (!players.isEmpty()) {
                  DrawContext ctx = e.getDrawContext();
                  MatrixStack matrix = ctx.getMatrices();
                  float tickDelta = this.mc.getRenderTickCounter().getTickDelta(true);
                  float cameraYaw = this.mc.gameRenderer.getCamera().getYaw();
                  float middleW = (float)this.mc.getWindow().getScaledWidth() / 2.0F;
                  float middleH = (float)this.mc.getWindow().getScaledHeight() / 2.0F;
                  float posY = middleH - this.radius.getFloatValue();
                  float size = this.sizeSetting.getFloatValue();
                  RenderSystem.enableBlend();
                  RenderSystem.disableCull();
                  RenderSystem.disableDepthTest();
                  RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA);
                  RenderSystem.setShaderTexture(0, ICON);
                  RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
                  BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

                  for (int i = 0; i < players.size(); i++) {
                     AbstractClientPlayerEntity player = players.get(i);
                     if (player != this.mc.player && player.isAlive() && !player.isRemoved() && !player.isSpectator()) {
                        int col = FriendRepository.isFriend(player.getNameForScoreboard()) ? -16711936 : this.color.getValue();
                        int baseCol = ColorProvider.setAlpha(col, ColorProvider.alpha(col) / 2);
                        float yaw = this.getRotation(player, tickDelta) - cameraYaw;
                        matrix.push();
                        matrix.translate(middleW, middleH, 0.0F);
                        matrix.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(yaw));
                        matrix.translate(-middleW, -middleH, 0.0F);
                        Matrix4f m = matrix.peek().getPositionMatrix();
                        buffer.vertex(m, middleW - size / 2.0F, posY + size, 0.0F).texture(0.0F, 1.0F).color(baseCol);
                        buffer.vertex(m, middleW + size / 2.0F, posY + size, 0.0F).texture(1.0F, 1.0F).color(baseCol);
                        buffer.vertex(m, middleW + size / 2.0F, posY, 0.0F).texture(1.0F, 0.0F).color(col);
                        buffer.vertex(m, middleW - size / 2.0F, posY, 0.0F).texture(0.0F, 0.0F).color(col);
                        matrix.pop();
                     }
                  }

                  BuiltBuffer builtBuffer = buffer.endNullable();
                  if (builtBuffer != null) {
                     BufferRenderer.drawWithGlobalProgram(builtBuffer);
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
      Vec3d diff = entity.getLerpedPos(tickDelta).subtract(this.mc.player.getLerpedPos(tickDelta));
      return (float)(-(Math.atan2(diff.x, diff.z) * (180.0 / Math.PI)));
   }
}
