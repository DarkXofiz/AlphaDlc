package miranware.module.list.render;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.ModeSetting;
import miranware.module.settings.SliderSetting;

@ModuleInformation(
   moduleName = "Swing Animations",
   moduleDesc = "Hand swing animation on hit",
   moduleCategory = ModuleCategory.RENDER
)
public class SwingAnimations extends Module {
   private final ModeSetting mode = new ModeSetting(
      "Mode",
      "None",
      "None",
      "Smooth",
      "Self",
      "Self 2",
      "Down",
      "Forward",
      "Touch",
      "BlockHit",
      "Pander",
      "Curt",
      "HMI"
   );
   private final SliderSetting power = new SliderSetting("Strength", 3.0, 0.0, 10.0, 1.0)
      .setVisible(
         () -> !this.mode.getValue().equals("Pander")
               && !this.mode.is("BlockHit")
               && !this.mode.getValue().equals("Forward")
               && !this.mode.getValue().equals("Curt")
               && !this.mode.getValue().equals("None")
               && !this.mode.getValue().equals("HMI")
      );
   public final SliderSetting speed = new SliderSetting("Speed", 7.0, 0.0, 10.0, 1.0);
   public final SliderSetting angle = new SliderSetting("Angle", 90.0, 0.0, 360.0, 5.0)
      .setVisible(() -> this.mode.getValue().equals("Self") || this.mode.getValue().equals("Self 2"));
   public final SliderSetting hmiPosX = new SliderSetting("X Offset", 0.0, -2.0, 2.0, 0.01)
      .setVisible(() -> this.mode.getValue().equals("HMI"));
   public final SliderSetting hmiPosY = new SliderSetting("Y Offset", 0.0, -2.0, 2.0, 0.01)
      .setVisible(() -> this.mode.getValue().equals("HMI"));
   public final SliderSetting hmiPosZ = new SliderSetting("Z Offset", 0.0, -2.0, 2.0, 0.01)
      .setVisible(() -> this.mode.getValue().equals("HMI"));
   public final SliderSetting hmiRotX = new SliderSetting("X Rotation", 0.0, -180.0, 180.0, 1.0)
      .setVisible(() -> this.mode.getValue().equals("HMI"));
   public final SliderSetting hmiRotY = new SliderSetting("Y Rotation", 0.0, -180.0, 180.0, 1.0)
      .setVisible(() -> this.mode.getValue().equals("HMI"));
   public final SliderSetting hmiRotZ = new SliderSetting("Z Rotation", 0.0, -180.0, 180.0, 1.0)
      .setVisible(() -> this.mode.getValue().equals("HMI"));
   public final SliderSetting hmiScale = new SliderSetting("Scale", 1.0, 0.1, 3.0, 0.05)
      .setVisible(() -> this.mode.getValue().equals("HMI"));
   private final HMIRenderer hmiRenderer = new HMIRenderer();

   public boolean isHMI() {
      return this.mode.getValue().equals("HMI");
   }

   public HMIRenderer getHMIRenderer() {
      this.hmiRenderer.setSwingAnimations(this);
      return this.hmiRenderer;
   }

   public void renderSwordAnimation(MatrixStack matrices, float swingProgress, float equipProgress, Arm arm) {
      float anim = (float)Math.sin((double)swingProgress * (Math.PI / 2) * 2.0);
      float sin2 = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
      String var7 = this.mode.getValue();
      switch (var7) {
         case "None": {
            matrices.translate(0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
            float f = -0.4F * MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
            float g = 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) (Math.PI * 2));
            float h = -0.2F * MathHelper.sin(swingProgress * (float) Math.PI);
            matrices.translate(f, g, h);
            int i = arm == Arm.RIGHT ? 1 : -1;
            float f2 = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)i * (45.0F + f2 * -20.0F)));
            float g2 = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)i * g2 * -20.0F));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g2 * -80.0F));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)i * -45.0F));
            break;
         }
         case "Smooth": {
            matrices.translate(0.56F, -0.52F, -0.72F);
            double swingPower = this.power.getValue() * 10.0;
            float f = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)(45.0 + (double)f * (-swingPower / 4.0))));
            float f1 = MathHelper.sin(MathHelper.sqrt(swingProgress * swingProgress) * (float) Math.PI);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)((double)sin2 * -(swingPower / 4.0))));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((float)((double)sin2 * -swingPower)));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-45.0F));
            break;
         }
         case "Self 2":
            matrices.translate(0.56F, -0.52F, -0.72F);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-30.0F));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((float)(-this.angle.getValue() - this.power.getValue() * 10.0 * (double)anim)));
            break;
         case "Forward":
            matrices.translate(0.56F, -0.52F, -0.72F);
            matrices.translate(0.0, 0.0, -0.3 * (double)sin2);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sin2 * -35.0F));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(sin2 * 35.0F));
            break;
         case "Self":
            matrices.translate(0.56F, -0.52F, -0.72F);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-60.0F));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((float)(-this.angle.getValue() - this.power.getValue() * 10.0 * (double)anim)));
            break;
         case "Down":
            matrices.translate(0.56F, -0.52F - (double)anim * this.power.getValue() / 24.0, -0.72F);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-30.0F));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F));
            break;
         case "Touch":
            matrices.translate(0.56F, -0.52F, -0.72F);
            matrices.scale(1.0F, 1.0F, (float)(1.0 + (double)anim * this.power.getValue() / 4.0));
            matrices.translate(0.0F, 0.0F, -0.265F);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-100.0F));
            break;
         case "Curt":
            matrices.translate(0.56F, -0.52F, -0.72F);
            float sqrtSwing = MathHelper.sqrt(swingProgress);
            float sinMain = MathHelper.sin(sqrtSwing * (float) Math.PI);
            float sinExtra = MathHelper.sin(swingProgress * (float) Math.PI);
            matrices.translate(0.4F - sinMain * 0.2F, -0.2F + sinMain * 0.3F, -0.5F - sinExtra * 0.2F);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(91.0F));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-40.0F + sinMain * -100.0F));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-60.0F));
            break;
         case "Pander":
            matrices.translate(0.56F, -0.52F, -0.72F);
            matrices.scale(0.8F, 0.8F, 0.8F);
            float anim2 = 1.0F
               - MathHelper.lerp(
                  this.mc.getRenderTickCounter().getTickDelta(true), this.mc.gameRenderer.firstPersonRenderer.prevEquipProgressMainHand, this.mc.gameRenderer.firstPersonRenderer.equipProgressMainHand
               );
            matrices.translate(0.3 - (double)(anim * 0.15F), (double)(0.2F - anim2 * 0.12F), (double)(-0.15F - anim * 0.13F));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(76.0F - 10.0F * anim));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-16.0F - 8.0F * anim));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-83.0F - 26.0F * anim));
            break;
         case "BlockHit": {
            matrices.translate(0.56F, -0.52F, -0.72F);
            float f = MathHelper.sin((float)((double)(swingProgress * swingProgress) * Math.PI));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45.0F));
            float g = MathHelper.sin((float)((double)MathHelper.sqrt(swingProgress) * Math.PI));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(f * -20.0F));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(g * -20.0F));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * -80.0F));
            matrices.translate(0.4F, 0.2F, 0.2F);
            matrices.translate(-0.5F, 0.08F, 0.0F);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(20.0F));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-80.0F));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(20.0F));
         }
      }
   }
}
