package zov.alphadlc.module.list.movement;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import zov.alphadlc.event.list.EventPlayerUpdate;
import zov.alphadlc.event.list.MoveInputEvent;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.list.combat.KillAura;
import zov.alphadlc.module.settings.SliderSetting;
import zov.alphadlc.util.base.Instance;
import zov.alphadlc.util.math.StopWatch;
import zov.alphadlc.util.packet.NetworkUtils;
import zov.alphadlc.util.player.other.InventoryUtil;
import zov.alphadlc.util.render.math.GCDFixer;
import zov.alphadlc.util.rotation.Rotation;
import zov.alphadlc.util.rotation.RotationComponent;

@ModuleInformation(
   moduleName = "ElytraFlight",
   moduleDesc = "\u0411\u044b\u0441\u0442\u0440\u043e\u0435 \u0438 \u0447\u0435\u0442\u043a\u043e\u0435 \u043f\u0435\u0440\u0435\u0434\u0432\u0438\u0436\u0435\u043d\u0438\u0435 \u043d\u0430 \u044d\u043b\u0438\u0442\u0440\u0430\u0445",
   moduleCategory = ModuleCategory.MOVEMENT
)
public class ElytraFlight extends Module {
   private final SliderSetting fireworkDelay = new SliderSetting(
      "\u0417\u0430\u0434\u0435\u0440\u0436\u043a\u0430 \u0444\u0435\u0435\u0440\u0430", 750.0, 100.0, 2000.0, 50.0
   );
   private final StopWatch stopWatch = new StopWatch();
   private Vec2f rotation = new Vec2f(0.0F, 0.0F);
   private float rotationYawOffset = -2.1474836E9F;
   private float rotationPitchOffset = -2.1474836E9F;
   private boolean hasInput = false;

   @Subscribe
   private void onStrafe(MoveInputEvent e) {
      if (this.mc.field_1724 != null && this.mc.field_1724.method_6128()) {
         if (this.hasInput) {
            if (this.rotationYawOffset != -2.1474836E9F && this.rotationPitchOffset != -2.1474836E9F) {
               RotationComponent.fixMovement(e, this.rotationYawOffset);
            }
         }
      }
   }

   @Subscribe
   private void onUpdate(EventPlayerUpdate e) {
      if (this.mc.field_1724 != null) {
         if (!this.mc.field_1724.method_6128() && this.mc.field_1724.method_6118(EquipmentSlot.field_6174).method_7909() == Items.field_8833) {
            if (this.mc.field_1724.method_24828()) {
               this.mc.field_1724.method_6043();
            } else {
               NetworkUtils.sendPacket(new ClientCommandC2SPacket(this.mc.field_1724, Mode.field_12982));
               this.mc.field_1724.method_23669();
               InventoryUtil.swapAndUseHvH(Items.field_8639);
               this.stopWatch.reset();
            }
         } else if (this.mc.field_1724.method_6128()) {
            KillAura killAura = Instance.get(KillAura.class);
            boolean killauraActive = killAura != null && killAura.isEnabled() && killAura.getTarget() != null;
            if (this.stopWatch.isReached((long)this.fireworkDelay.getValue())) {
               InventoryUtil.swapAndUseHvH(Items.field_8639);
               this.stopWatch.reset();
            }

            boolean forward = this.mc.field_1690.field_1894.method_1434();
            boolean back = this.mc.field_1690.field_1881.method_1434();
            boolean left = this.mc.field_1690.field_1913.method_1434();
            boolean right = this.mc.field_1690.field_1849.method_1434();
            boolean jump = this.mc.field_1690.field_1903.method_1434();
            boolean sneak = this.mc.field_1690.field_1832.method_1434();
            boolean isMoving = forward || back || left || right;
            float cameraYaw = this.mc.field_1773.method_19418().method_19330();
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

               this.mc.field_1724.method_18800(motionX, vy, motionZ);
            } else if (!killauraActive && !isMoving && (jump || sneak)) {
               double vy = jump ? 1.0 : -1.0;
               this.mc.field_1724.method_18800(0.0, vy, 0.0);
            } else if (!killauraActive && !hasAnyInput) {
               this.mc.field_1724.method_18800(0.0, 0.0, 0.0);
            }

            if (hasAnyInput && !killauraActive) {
               yaw -= (yaw - this.rotation.field_1343) % GCDFixer.getGCDValue();
               pitch -= (pitch - this.rotation.field_1342) % GCDFixer.getGCDValue();
               this.rotation = new Vec2f(yaw, MathHelper.method_15363(pitch, -90.0F, 90.0F));
               this.rotationYawOffset = this.rotation.field_1343;
               this.rotationPitchOffset = this.rotation.field_1342;
               RotationComponent.update(new Rotation(this.rotation.field_1343, this.rotation.field_1342), 360.0F, 360.0F, 360.0F, 360.0F, 0, 3, false);
               this.mc.field_1724.method_36456(this.rotation.field_1343);
               this.mc.field_1724.method_36457(this.rotation.field_1342);
               this.mc.field_1724.field_6241 = this.rotation.field_1343;
               this.mc.field_1724.field_6283 = this.rotation.field_1343;
            }
         }
      }
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.stopWatch.reset();
      this.rotation = new Vec2f(this.mc.field_1724 != null ? this.mc.field_1724.method_36454() : 0.0F, 0.0F);
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
