package miranware.module.list.movement;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import miranware.event.list.EventPlayerUpdate;
import miranware.event.list.MoveInputEvent;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.list.combat.KillAura;
import miranware.module.settings.SliderSetting;
import miranware.util.base.Instance;
import miranware.util.math.StopWatch;
import miranware.util.packet.NetworkUtils;
import miranware.util.player.other.InventoryUtil;
import miranware.util.render.math.GCDFixer;
import miranware.util.rotation.Rotation;
import miranware.util.rotation.RotationComponent;

@ModuleInformation(
   moduleName = "ElytraFlight",
   moduleDesc = "Fast and precise elytra movement",
   moduleCategory = ModuleCategory.MOVEMENT
)
public class ElytraFlight extends Module {
   private final SliderSetting fireworkDelay = new SliderSetting(
      "Firework Delay", 750.0, 100.0, 2000.0, 50.0
   );
   private final StopWatch stopWatch = new StopWatch();
   private Vec2f rotation = new Vec2f(0.0F, 0.0F);
   private float rotationYawOffset = -2.1474836E9F;
   private float rotationPitchOffset = -2.1474836E9F;
   private boolean hasInput = false;

   @Subscribe
   private void onStrafe(MoveInputEvent e) {
      if (this.mc.player != null && this.mc.player.isGliding()) {
         if (this.hasInput) {
            if (this.rotationYawOffset != -2.1474836E9F && this.rotationPitchOffset != -2.1474836E9F) {
               RotationComponent.fixMovement(e, this.rotationYawOffset);
            }
         }
      }
   }

   @Subscribe
   private void onUpdate(EventPlayerUpdate e) {
      if (this.mc.player != null) {
         if (!this.mc.player.isGliding() && this.mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
            if (this.mc.player.isOnGround()) {
               this.mc.player.jump();
            } else {
               NetworkUtils.sendPacket(new ClientCommandC2SPacket(this.mc.player, Mode.START_FALL_FLYING));
               this.mc.player.startGliding();
               InventoryUtil.swapAndUseHvH(Items.FIREWORK_ROCKET);
               this.stopWatch.reset();
            }
         } else if (this.mc.player.isGliding()) {
            KillAura killAura = Instance.get(KillAura.class);
            boolean killauraActive = killAura != null && killAura.isEnabled() && killAura.getTarget() != null;
            if (this.stopWatch.isReached((long)this.fireworkDelay.getValue())) {
               InventoryUtil.swapAndUseHvH(Items.FIREWORK_ROCKET);
               this.stopWatch.reset();
            }

            boolean forward = this.mc.options.forwardKey.isPressed();
            boolean back = this.mc.options.backKey.isPressed();
            boolean left = this.mc.options.leftKey.isPressed();
            boolean right = this.mc.options.rightKey.isPressed();
            boolean jump = this.mc.options.jumpKey.isPressed();
            boolean sneak = this.mc.options.sneakKey.isPressed();
            boolean isMoving = forward || back || left || right;
            float cameraYaw = this.mc.gameRenderer.getCamera().getYaw();
            float pitch = 0.0F;
            float yaw;
            if (isMoving) {
               float yawOffset = 0.0F;
               if (forward) {
                  if (left) {
                     yawOffset = -45.0F;
                  } else if (right) {
                     yawOffset = 45.0F;
                  }
               } else if (back) {
                  yawOffset = 180.0F;
                  if (left) {
                     yawOffset = -135.0F;
                  } else if (right) {
                     yawOffset = 135.0F;
                  }
               } else if (left) {
                  yawOffset = -90.0F;
               } else if (right) {
                  yawOffset = 90.0F;
               }

               yaw = cameraYaw + yawOffset;
            } else {
               yaw = cameraYaw;
            }

            if (jump) {
               pitch = isMoving ? -25.0F : -90.0F;
            } else if (sneak) {
               pitch = isMoving ? 25.0F : 90.0F;
            }

            boolean hasAnyInput = isMoving || jump || sneak;
            this.hasInput = hasAnyInput;
            if (!killauraActive && isMoving) {
               float motion = pitch == -25.0F || pitch == 25.0F ? 0.5F : (pitch != 90.0F && pitch != -90.0F ? 1.0F : 0.25F);
               double rad = Math.toRadians((double)(yaw + 90.0F));
               double motionX = Math.cos(rad) * (double)motion;
               double motionZ = Math.sin(rad) * (double)motion;
               double vy = 0.0;
               if (pitch == -25.0F) {
                  vy = 0.5;
               } else if (pitch == -90.0F) {
                  vy = 1.0;
               } else if (pitch == 25.0F) {
                  vy = -0.5;
               } else if (pitch == 90.0F) {
                  vy = -1.0;
               }

               this.mc.player.setVelocity(motionX, vy, motionZ);
            } else if (!killauraActive && !isMoving && (jump || sneak)) {
               double vy = jump ? 1.0 : -1.0;
               this.mc.player.setVelocity(0.0, vy, 0.0);
            } else if (!killauraActive && !hasAnyInput) {
               this.mc.player.setVelocity(0.0, 0.0, 0.0);
            }

            if (hasAnyInput && !killauraActive) {
               yaw -= (yaw - this.rotation.x) % GCDFixer.getGCDValue();
               pitch -= (pitch - this.rotation.y) % GCDFixer.getGCDValue();
               this.rotation = new Vec2f(yaw, MathHelper.clamp(pitch, -90.0F, 90.0F));
               this.rotationYawOffset = this.rotation.x;
               this.rotationPitchOffset = this.rotation.y;
               RotationComponent.update(new Rotation(this.rotation.x, this.rotation.y), 360.0F, 360.0F, 360.0F, 360.0F, 0, 3, false);
               this.mc.player.setYaw(this.rotation.x);
               this.mc.player.setPitch(this.rotation.y);
               this.mc.player.headYaw = this.rotation.x;
               this.mc.player.bodyYaw = this.rotation.x;
            }
         }
      }
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.stopWatch.reset();
      this.rotation = new Vec2f(this.mc.player != null ? this.mc.player.getYaw() : 0.0F, 0.0F);
      this.rotationYawOffset = -2.1474836E9F;
      this.rotationPitchOffset = -2.1474836E9F;
      this.hasInput = false;
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.rotationYawOffset = -2.1474836E9F;
      this.rotationPitchOffset = -2.1474836E9F;
      this.hasInput = false;
   }
}
