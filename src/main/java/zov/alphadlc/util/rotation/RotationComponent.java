package zov.alphadlc.util.rotation;

import com.google.common.eventbus.Subscribe;
import lombok.Generated;
import net.minecraft.util.math.MathHelper;
import zov.alphadlc.event.list.EventTick;
import zov.alphadlc.event.list.MoveInputEvent;
import zov.alphadlc.util.base.Instance;
import zov.alphadlc.util.render.math.GCDFixer;

public class RotationComponent extends Component {
   private RotationComponent.RotationTask currentTask = RotationComponent.RotationTask.IDLE;
   private float currentYawSpeed;
   private float currentPitchSpeed;
   private float currentYawReturnSpeed;
   private float currentPitchReturnSpeed;
   private int currentPriority;
   private int currentTimeout;
   private int idleTicks;
   private Rotation targetRotation;

   public static RotationComponent getInstance() {
      return Instance.getComponent(RotationComponent.class);
   }

   public static double direction(float rotationYaw, float moveForward, float moveStrafing) {
      if (moveForward < 0.0F) {
         rotationYaw += 180.0F;
      }

      float forward = 1.0F;
      if (moveForward < 0.0F) {
         forward = -0.5F;
      }

      if (moveForward > 0.0F) {
         forward = 0.5F;
      }

      if (moveStrafing > 0.0F) {
         rotationYaw -= 90.0F * forward;
      }

      if (moveStrafing < 0.0F) {
         rotationYaw += 90.0F * forward;
      }

      return Math.toRadians((double)rotationYaw);
   }

   public static void fixMovement(MoveInputEvent event, float yaw) {
      float forward = event.getForward();
      float strafe = event.getStrafe();
      if (forward != 0.0F || strafe != 0.0F) {
         double targetAngle = MathHelper.wrapDegrees(Math.toDegrees(direction(yaw, forward, strafe)));
         float bestForward = 0.0F;
         float bestStrafe = 0.0F;
         float smallestDifference = Float.MAX_VALUE;

         for (float testForward = -1.0F; testForward <= 1.0F; testForward++) {
            for (float testStrafe = -1.0F; testStrafe <= 1.0F; testStrafe++) {
               if (testForward != 0.0F || testStrafe != 0.0F) {
                  double testAngle = MathHelper.wrapDegrees(Math.toDegrees(direction(yaw, testForward, testStrafe)));
                  float difference = Math.abs(MathHelper.wrapDegrees((float)(targetAngle - testAngle)));
                  if (difference < smallestDifference) {
                     smallestDifference = difference;
                     bestForward = testForward;
                     bestStrafe = testStrafe;
                  }
               }
            }
         }

         event.forward = bestForward;
         event.strafe = bestStrafe;
      }
   }

   @Subscribe
   public void onEvent(MoveInputEvent event) {
      if (this.isRotating()) {
         fixMovement(event, MathHelper.wrapDegrees(mc.gameRenderer.getCamera().getYaw()));
      }
   }

   private void resetRotation() {
      Rotation targetRotation = new Rotation(FreeLookComponent.getFreeYaw(), FreeLookComponent.getFreePitch());
      if (this.updateRotation(targetRotation, this.currentYawReturnSpeed(), this.currentPitchReturnSpeed())) {
         this.stopRotation();
      }
   }

   @Subscribe
   public void onEvent(EventTick event) {
      if (this.currentTask().equals(RotationComponent.RotationTask.AIM) && this.idleTicks() > this.currentTimeout()) {
         this.currentTask(RotationComponent.RotationTask.RESET);
      }

      if (this.currentTask().equals(RotationComponent.RotationTask.RESET)) {
         this.resetRotation();
      }

      this.idleTicks++;
   }

   public static void update(
      Rotation target, float yawSpeed, float pitchSpeed, float yawReturnSpeed, float pitchReturnSpeed, int timeout, int priority, boolean clientRotation
   ) {
      RotationComponent instance = getInstance();
      if (instance.currentPriority() <= priority) {
         if (instance.currentTask().equals(RotationComponent.RotationTask.IDLE) && !clientRotation) {
            FreeLookComponent.setActive(true);
         }

         instance.currentYawSpeed(yawSpeed);
         instance.currentPitchSpeed(pitchSpeed);
         instance.currentYawReturnSpeed(yawReturnSpeed);
         instance.currentPitchReturnSpeed(pitchReturnSpeed);
         instance.currentTimeout(timeout);
         instance.currentPriority(priority);
         instance.currentTask(RotationComponent.RotationTask.AIM);
         instance.targetRotation(target);
         instance.updateRotation(target, yawSpeed, pitchSpeed);
      }
   }

   public static void update(Rotation targetRotation, float turnSpeed, float returnSpeed, int timeout, int priority) {
      update(targetRotation, turnSpeed, turnSpeed, returnSpeed, returnSpeed, timeout, priority, false);
   }

   public static void update(Rotation targetRotation, float yawSpeed, float pitchSpeed, float returnSpeed, int timeout, int priority) {
      update(targetRotation, yawSpeed, pitchSpeed, returnSpeed, returnSpeed, timeout, priority, false);
   }

   private boolean updateRotation(Rotation targetRotation, float yawSpeed, float pitchSpeed) {
      if (mc.player == null) {
         return false;
      } else {
         Rotation currentRotation = new Rotation(mc.player);
         float yawDelta = MathHelper.wrapDegrees(targetRotation.getYaw() - currentRotation.getYaw());
         float pitchDelta = targetRotation.getPitch() - currentRotation.getPitch();
         float clampedYaw = Math.min(Math.abs(yawDelta), yawSpeed);
         float clampedPitch = Math.min(Math.abs(pitchDelta), pitchSpeed);
         float yaw = mc.player.getYaw();
         yaw += GCDFixer.getFixRotate(MathHelper.clamp(yawDelta, -clampedYaw, clampedYaw));
         mc.player.setYaw(yaw);
         mc.player
            .setPitch(
               MathHelper.clamp(
                  mc.player.getPitch() + GCDFixer.getFixRotate(MathHelper.clamp(pitchDelta, -clampedPitch, clampedPitch)), -90.0F, 90.0F
               )
            );
         this.idleTicks(0);
         return new Rotation(mc.player).getDelta(targetRotation) < 1.0F;
      }
   }

   public void stopRotation() {
      this.currentTask(RotationComponent.RotationTask.IDLE);
      this.currentPriority(0);
      FreeLookComponent.setActive(false);
   }

   public boolean isRotating() {
      return !this.currentTask.equals(RotationComponent.RotationTask.IDLE);
   }

   @Generated
   public RotationComponent.RotationTask currentTask() {
      return this.currentTask;
   }

   @Generated
   public float currentYawSpeed() {
      return this.currentYawSpeed;
   }

   @Generated
   public float currentPitchSpeed() {
      return this.currentPitchSpeed;
   }

   @Generated
   public float currentYawReturnSpeed() {
      return this.currentYawReturnSpeed;
   }

   @Generated
   public float currentPitchReturnSpeed() {
      return this.currentPitchReturnSpeed;
   }

   @Generated
   public int currentPriority() {
      return this.currentPriority;
   }

   @Generated
   public int currentTimeout() {
      return this.currentTimeout;
   }

   @Generated
   public int idleTicks() {
      return this.idleTicks;
   }

   @Generated
   public Rotation targetRotation() {
      return this.targetRotation;
   }

   @Generated
   public RotationComponent currentTask(RotationComponent.RotationTask currentTask) {
      this.currentTask = currentTask;
      return this;
   }

   @Generated
   public RotationComponent currentYawSpeed(float currentYawSpeed) {
      this.currentYawSpeed = currentYawSpeed;
      return this;
   }

   @Generated
   public RotationComponent currentPitchSpeed(float currentPitchSpeed) {
      this.currentPitchSpeed = currentPitchSpeed;
      return this;
   }

   @Generated
   public RotationComponent currentYawReturnSpeed(float currentYawReturnSpeed) {
      this.currentYawReturnSpeed = currentYawReturnSpeed;
      return this;
   }

   @Generated
   public RotationComponent currentPitchReturnSpeed(float currentPitchReturnSpeed) {
      this.currentPitchReturnSpeed = currentPitchReturnSpeed;
      return this;
   }

   @Generated
   public RotationComponent currentPriority(int currentPriority) {
      this.currentPriority = currentPriority;
      return this;
   }

   @Generated
   public RotationComponent currentTimeout(int currentTimeout) {
      this.currentTimeout = currentTimeout;
      return this;
   }

   @Generated
   public RotationComponent idleTicks(int idleTicks) {
      this.idleTicks = idleTicks;
      return this;
   }

   @Generated
   public RotationComponent targetRotation(Rotation targetRotation) {
      this.targetRotation = targetRotation;
      return this;
   }

   public static enum RotationTask {
      AIM,
      RESET,
      IDLE;
   }
}
