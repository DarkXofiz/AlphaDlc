package miranware.module.list.combat;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Generated;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.Last;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import org.joml.Matrix4f;
import miranware.MiranWare;
import miranware.event.EventGameUpdate;
import miranware.event.list.EventChangeSprint;
import miranware.event.list.EventTick;
import miranware.event.list.MoveInputEvent;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.ModuleSettingDefinitions;
import miranware.module.list.movement.ElytraTarget;
import miranware.module.list.player.FreeCamera;
import miranware.module.settings.BooleanSetting;
import miranware.module.settings.ModeListSetting;
import miranware.module.settings.ModeSetting;
import miranware.module.settings.SliderSetting;
import miranware.util.friend.FriendRepository;
import miranware.util.math.BestPoint;
import miranware.util.math.RotationUtil;
import miranware.util.math.StopWatch;
import miranware.util.neuro.rotation.AIRotationRecorder;
import miranware.util.player.combat.PredictUtils;
import miranware.util.player.combat.RaytraceUtil;
import miranware.util.player.simulate.SimulatedPlayer;
import miranware.util.render.math.GCDFixer;
import miranware.util.render.providers.ColorProvider;
import miranware.util.rotation.Rotation;
import miranware.util.rotation.RotationComponent;
import miranware.util.text.ValueUnit;

@ModuleInformation(
   moduleName = "KillAura",
   moduleDesc = "Automatically attacks the nearest enemies",
   moduleCategory = ModuleCategory.COMBAT
)
public class KillAura extends Module {
   public final ModeSetting rotation = ModuleSettingDefinitions.killAuraRotation();
   public final ModeSetting rotationBehavior = new ModeSetting(
      "Rotation Behavior",
      "Smooth",
      "Smooth",
      "Snaps"
   );
   private final ModeListSetting targets = new ModeListSetting(
      "Targets",
      new BooleanSetting("Players", true),
      new BooleanSetting("Naked", true),
      new BooleanSetting("Monsters", true),
      new BooleanSetting("Animals", true)
   );
   public final SliderSetting distance = new SliderSetting(
      "Distance",
      ValueUnit.countable("block", "blocks", "blocks"),
      3.0,
      2.0,
      6.0,
      0.1F
   );
   private final SliderSetting preRotation = new SliderSetting(
      "Pre Distance",
      ValueUnit.countable("block", "blocks", "blocks"),
      1.5,
      0.0,
      3.0,
      0.1F
   );
   public final BooleanSetting raycastCheck = new BooleanSetting(
      "Aim Check", true
   );
   public final BooleanSetting smartAim = new BooleanSetting("Smart Aim", true);
   public final BooleanSetting predictate = new BooleanSetting("Predict", true);
   public final SliderSetting predictValue = new SliderSetting(
      "Predict Value", 3.0, 1.0, 5.0, 0.1F
   );
   public final BooleanSetting elytraSlowdown = new BooleanSetting(
      "Elytra Slowdown", true
   );
   public final ModeSetting slowdownMode = ModuleSettingDefinitions.killAuraSprintReset().setVisible(() -> this.elytraSlowdown.getValue());
   public final SliderSetting slowdownRadius = new SliderSetting(
         "Slowdown Radius",
         ValueUnit.countable("block", "blocks", "blocks"),
         3.0,
         1.0,
         6.0,
         0.1F
      )
      .setVisible(() -> this.elytraSlowdown.getValue() && this.slowdownMode.is("By Radius"));
   public final SliderSetting minSpeed = new SliderSetting("Min. Speed", 0.3F, 0.1F, 0.9F, 0.05F)
      .setVisible(() -> this.elytraSlowdown.getValue() && this.slowdownMode.is("By Radius"));
   public final SliderSetting preHitTicks = new SliderSetting("Ticks Before Hit", 3.0, 1.0, 10.0, 1.0)
      .setVisible(() -> this.elytraSlowdown.getValue() && this.slowdownMode.is("Before Hit"));
   public final BooleanSetting hitAfterOvertake = new BooleanSetting(
      "Hit Only After Overtake", true
   );
   public final ModeSetting moveFix = new ModeSetting(
      "Movement Correction",
      "Focused",
      "None",
      "Focused",
      "Targeted"
   );
   public final BooleanSetting onlySpace = ModuleSettingDefinitions.killAuraOnlySpace();
   public final BooleanSetting clientLook = ModuleSettingDefinitions.killAuraClientLook();
   public final BooleanSetting showPredictPoint = new BooleanSetting(
      "Show Predict Point", true
   );
   public final BooleanSetting elytraTurnaround = new BooleanSetting(
      "Elytra Turn", true
   );
   public final BooleanSetting visualElytraRotation = new BooleanSetting(
      "Elytra Visual Rotation", true
   );
   private ElytraTarget elytraTarget;
   public static final BooleanSetting useResolver = new BooleanSetting("Resolver (Elytra)", true);
   public boolean isResolving = false;
   public Vec3d resolverPoint = null;
   private final StopWatch resolverTimer = new StopWatch();
   public boolean isTurnaroundActive = false;
   private static final float RANDOM_STRENGTH = 0.75F;
   public static boolean isSlowdownActive = false;
   private static final StopWatch stopWatch = new StopWatch();
   private LivingEntity target;
   public static LivingEntity lastTarget;
   public int ticksToAttack;
   private int razvorotikTicks;
   private boolean back;
   public float speedAcceleration;
   public static long lastPhysicalMoveTime;
   public float preddict;
   public float lastYaw;
   public float lastPitch;
   private LivingEntity slothTrackedTarget;
   private float slothCurrentYaw;
   private float slothCurrentPitch;
   private float slothVelocityYaw;
   private float slothVelocityPitch;
   private double slothAimPointX;
   private double slothAimPointY;
   private double slothAimPointZ;
   private float slothNoiseAngle;
   private final float slothNoiseAmplitude = 1.8F;
   private int slothHitPhase;
   private int slothHitTimer;
   private float slothPitchBeforeHit;
   private long slothFirstSeenTime;
   private int slothReactionMs;
   private boolean slothReactionComplete;
   private float slothLastSentYaw;
   private float slothLastSentPitch;
   private float slothSmoothYaw;
   private float slothSmoothPitch;
   private boolean renderListenerRegistered = false;
   private final Last renderListener = context -> {
      if (this.isEnabled() && this.showPredictPoint.getValue()) {
         this.renderPredictPoint(context.matrixStack(), context.camera(), context.tickCounter().getTickDelta(true));
      }
   };

   public ElytraTarget getElytraTarget() {
      if (this.elytraTarget == null) {
         this.elytraTarget = MiranWare.getInstance().getModuleStorage().get(ElytraTarget.class);
      }

      return this.elytraTarget;
   }

   private void findResolverPoint() {
      if (this.mc.player != null && this.mc.world != null) {
         Vec3d eye = this.mc.player.getEyePos();
         float oppositeYaw = this.mc.player.getYaw() + 180.0F;
         float searchPitch = -50.0F;
         int[] yawOffsets = new int[]{0, 30, -30, 45, -45, 60, -60, 90, -90};

         for (int offset : yawOffsets) {
            float testYaw = oppositeYaw + (float)offset;
            float radYaw = (float)Math.toRadians((double)testYaw);
            float radPitch = (float)Math.toRadians((double)searchPitch);
            double x = -Math.sin((double)radYaw) * Math.cos((double)radPitch);
            double y = -Math.sin((double)radPitch);
            double z = Math.cos((double)radYaw) * Math.cos((double)radPitch);
            Vec3d checkVec = new Vec3d(x, y, z).normalize().multiply(8.0);
            Vec3d endPoint = eye.add(checkVec);
            if (this.mc
                  .world
                  .raycast(new RaycastContext(eye, endPoint, ShapeType.COLLIDER, FluidHandling.NONE, this.mc.player))
                  .getType()
               == Type.MISS) {
               this.resolverPoint = endPoint;
               return;
            }
         }

         this.resolverPoint = null;
      }
   }

   @Subscribe
   private void onGameUpdate(EventGameUpdate e) {
      if (this.mc.player != null && this.target != null) {
         MiranWare.getInstance().getModuleStorage().setRandomness(1.0F);
         if (!AIRotationRecorder.isRecording()) {
            if (this.isResolving) {
               if (this.resolverTimer.isReached(300L)) {
                  this.isResolving = false;
               } else if (this.resolverPoint != null) {
                  Rotation rot = new Rotation(RotationUtil.calculate(this.resolverPoint));
                  RotationComponent.update(rot, 360.0F, 360.0F, 360.0F, 360.0F, 0, 1, this.clientLook.getValue());
                  this.lastYaw = rot.getYaw();
                  this.lastPitch = rot.getPitch();
                  return;
               }
            }

            if (this.rotationBehavior.is("Snaps")) {
               boolean isReadyToAttack = this.mc.player.getAttackCooldownProgress(1.0F) >= 0.95F && this.ticksToAttack <= 1;
               if (!isReadyToAttack) {
                  return;
               }
            }

            boolean playerOnElytra = this.mc.player.isGliding();
            if (!playerOnElytra || this.elytraTarget != null && this.elytraTarget.isEnabled()) {
               String var3 = this.rotation.getValue();
               switch (var3) {
                  case "ReallyWorld":
                     this.updateVanillaRotation(this.target);
                     break;
                  case "Smooth":
                     this.updateSmoothRotation(this.target);
               }
            }
         }
      }
   }

   @Subscribe
   private void onChangeSprint(EventChangeSprint e) {
      if (this.canStopSprinting()) {
         e.setSprinting(false);
      }
   }

   @Subscribe
   private void onMoveInput(MoveInputEvent event) {
      if (this.mc.player != null) {
         if (this.moveFix.is("Targeted")) {
            if (this.target != null) {
               if (this.mc.player.isGliding()) {
                  event.forward = 0.0F;
                  event.strafe = 0.0F;
               } else if (event.forward != 0.0F || event.strafe != 0.0F) {
                  float yaw = MathHelper.wrapDegrees(this.mc.gameRenderer.getCamera().getYaw());
                  double dx = this.target.getX() - this.mc.player.getX();
                  double dz = this.target.getZ() - this.mc.player.getZ();
                  double targetAngle = MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
                  float bestForward = 0.0F;
                  float bestStrafe = 0.0F;
                  float smallestDiff = Float.MAX_VALUE;

                  for (float f = -1.0F; f <= 1.0F; f++) {
                     for (float s = -1.0F; s <= 1.0F; s++) {
                        if (f != 0.0F || s != 0.0F) {
                           double predictedAngle = MathHelper.wrapDegrees(Math.toDegrees(RotationComponent.direction(yaw, f, s)));
                           float diff = Math.abs(MathHelper.wrapDegrees((float)(targetAngle - predictedAngle)));
                           if (diff < smallestDiff) {
                              smallestDiff = diff;
                              bestForward = f;
                              bestStrafe = s;
                           }
                        }
                     }
                  }

                  event.forward = bestForward;
                  event.strafe = bestStrafe;
               }
            }
         }
      }
   }

   @Subscribe
   private void onUpdate(EventTick ignored) {
      if (this.mc.player != null && this.mc.world != null) {
         if (this.ticksToAttack > 0) {
            this.ticksToAttack--;
         }

         this.updateTarget();
         if (this.target != null) {
            lastTarget = this.target;
            if (!this.elytraSlowdown.getValue() || !this.mc.player.isGliding()) {
               isSlowdownActive = false;
            } else if (this.slowdownMode.is("Before Hit")) {
               isSlowdownActive = (double)this.ticksToAttack <= this.preHitTicks.getValue();
            } else {
               isSlowdownActive = false;
            }

            Vec3d predict = PredictUtils.predict(this.target, this.predictValue.getValue());
            double distToPredict = this.mc.player.getEyePos().distanceTo(predict);
            if (!this.elytraSlowdown.getValue() || !this.mc.player.isGliding()) {
               isSlowdownActive = false;
            } else if (this.slowdownMode.is("Before Hit")) {
               isSlowdownActive = (double)this.ticksToAttack <= this.preHitTicks.getValue();
            } else {
               isSlowdownActive = distToPredict < 2.7 && this.ticksToAttack <= 2;
            }

            if (this.canStopSprinting()) {
               this.mc.player.setSprinting(false);
            }

            if (this.canAttack()) {
               if (useResolver.getValue() && this.mc.player.isGliding()) {
                  this.mc.player.setVelocity(0.0, 0.0, 0.0);
                  this.findResolverPoint();
                  if (this.resolverPoint != null) {
                     this.isResolving = true;
                     this.resolverTimer.reset();
                  }
               }

               this.mc.getNetworkHandler().sendPacket(new PlayerInputC2SPacket(new PlayerInput(false, false, false, false, false, false, false)));
               this.mc.interactionManager.attackEntity(this.mc.player, this.target);
               this.mc.player.swingHand(Hand.MAIN_HAND);
               this.mc.getNetworkHandler().sendPacket(new PlayerInputC2SPacket(this.mc.player.input.playerInput));
               this.ticksToAttack = 10;
            }
         } else {
            this.speedAcceleration = 0.0F;
         }
      }
   }

   private boolean isValidEntity(Entity entity) {
      if (!entity.isAlive()) {
         return false;
      } else {
         PlayerEntity player = (PlayerEntity)(MiranWare.getInstance().getModuleStorage().get(FreeCamera.class).fakePlayer != null
            ? MiranWare.getInstance().getModuleStorage().get(FreeCamera.class).fakePlayer
            : this.mc.player);
         if (entity == MiranWare.getInstance().getModuleStorage().get(FreeCamera.class).fakePlayer) {
            return false;
         } else if (entity instanceof ClientPlayerEntity) {
            return false;
         } else if (entity instanceof ArmorStandEntity) {
            return false;
         } else {
            if (entity instanceof PlayerEntity p && p.getArmor() != 0 && !this.targets.isEnabled("Players")) {
               return false;
            }

            if (entity instanceof PlayerEntity p && p.getArmor() == 0 && !this.targets.isEnabled("Naked")) {
               return false;
            }

            if ((entity instanceof HostileEntity || entity instanceof AmbientEntity) && !this.targets.isEnabled("Monsters")) {
               return false;
            } else if ((entity instanceof PassiveEntity || entity instanceof FishEntity)
               && !this.targets.isEnabled("Animals")) {
               return false;
            } else {
               if (entity instanceof PlayerEntity p && !FriendRepository.shouldAttack(p)) {
                  return false;
               }

               return !(
                  player.getEyePos().distanceTo(BestPoint.getNearestPoint(entity))
                     > (player.isGliding() ? 50.0 : this.distance.getValue() + this.preRotation.getValue())
               );
            }
         }
      }
   }

   public boolean canAttack() {
      if (this.target == null) {
         return false;
      } else {
         this.isTurnaroundActive = false;
         PlayerEntity player = (PlayerEntity)(MiranWare.getInstance().getModuleStorage().get(FreeCamera.class).fakePlayer != null
            ? MiranWare.getInstance().getModuleStorage().get(FreeCamera.class).fakePlayer
            : this.mc.player);
         boolean playerElytra = this.mc.player.isGliding();
         boolean targetElytra = this.target.isGliding();
         boolean anyElytra = playerElytra || targetElytra;
         if (this.target.isGliding()) {
            Vec3d predict = PredictUtils.predict(this.target, this.predictValue.getValue());
            double distToPredict = player.getEyePos().distanceTo(predict);
            this.preddict = this.hitAfterOvertake.getValue() ? 2.7F : 4.0F;
            if (distToPredict <= (double)this.preddict && this.elytraTurnaround.getValue()) {
               this.isTurnaroundActive = true;
            }
         }

         if (!MiranWare.getInstance().getIdealHitUtils().cooldownIsReached(false)) {
            return false;
         } else if (this.ticksToAttack > 0) {
            return false;
         } else if (anyElytra) {
            return this.elytraTarget != null && this.elytraTarget.isEnabled() ? this.elytraTarget.canAttack(this.target) : false;
         } else if (!RaytraceUtil.rayTrace(player.getRotationVector(), this.distance.getValue(), this.target.getBoundingBox()) && this.raycastCheck.getValue()) {
            return false;
         } else {
            return player.getEyePos().distanceTo(BestPoint.getNearestPoint(this.target)) > this.distance.getValue() - 0.2F
               ? false
               : MiranWare.getInstance().getIdealHitUtils().canCritical();
         }
      }
   }

   public boolean canStopSprinting() {
      if (this.target == null) {
         return false;
      } else if (!MiranWare.getInstance().getIdealHitUtils().cooldownIsReached(true)) {
         return false;
      } else {
         return this.ticksToAttack > 1 ? false : SimulatedPlayer.simulateLocalPlayer(1).fallDistance != 0.0F;
      }
   }

   private void updateTarget() {
      LivingEntity best = null;
      double bestFovDot = -1.0;
      Vec3d eyePos = this.mc.player.getEyePos();
      Vec3d lookVec = this.mc.player.getRotationVec(1.0F);

      for (Entity entity : this.mc.world.getEntities()) {
         if (entity instanceof LivingEntity && this.isValidEntity(entity)) {
            Vec3d targetVec = BestPoint.getNearestPoint(entity).subtract(eyePos).normalize();
            double dot = lookVec.dotProduct(targetVec);
            if (dot > bestFovDot) {
               bestFovDot = dot;
               best = (LivingEntity)entity;
            }
         }
      }

      if (this.target == null || !this.isValidEntity(this.target)) {
         this.target = best;
      }
   }

   private void updateVanillaRotation(LivingEntity target) {
      if (target != null) {
         Vec3d targetPoint = this.resolveMultipoint(target, BestPoint.getNearestPoint(target), this.distance.getValue());
         Rotation rotation = new Rotation(RotationUtil.calculate(targetPoint));
         RotationComponent.update(rotation, 360.0F, 360.0F, 360.0F, 360.0F, 0, 1, this.clientLook.getValue());
      }
   }

   private void slReset() {
      this.slothTrackedTarget = null;
      this.slothVelocityYaw = this.slothVelocityPitch = 0.0F;
      this.slothAimPointX = this.slothAimPointY = this.slothAimPointZ = 0.0;
      this.slothNoiseAngle = 0.0F;
      this.slothHitPhase = this.slothHitTimer = 0;
      this.slothFirstSeenTime = 0L;
      this.slothReactionComplete = false;
      this.slothReactionMs = 0;
      if (this.mc.player != null) {
         this.slothCurrentYaw = this.mc.player.getYaw();
         this.slothCurrentPitch = this.mc.player.getPitch();
         this.slothLastSentYaw = this.slothCurrentYaw;
         this.slothLastSentPitch = this.slothCurrentPitch;
         this.slothSmoothYaw = this.slothCurrentYaw;
         this.slothSmoothPitch = this.slothCurrentPitch;
      } else {
         this.slothCurrentYaw = this.slothCurrentPitch = 0.0F;
         this.slothLastSentYaw = this.slothLastSentPitch = 0.0F;
         this.slothSmoothYaw = this.slothSmoothPitch = 0.0F;
      }
   }

   private float slCalcGcd() {
      double s = (Double)this.mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2;
      return (float)(s * s * s * 1.2);
   }

   private void slPickAimPoint(LivingEntity e) {
      Box bb = e.getBoundingBox();
      double w = bb.maxX - bb.minX;
      double h = bb.maxY - bb.minY;
      double d = bb.maxZ - bb.minZ;
      this.slothAimPointX = (Math.random() - 0.5) * w * 0.12;
      this.slothAimPointY = (Math.random() - 0.5) * h * 0.11;
      this.slothAimPointZ = (Math.random() - 0.5) * d * 0.12;
   }

   public void slOnAttack() {
      this.slothHitPhase = 1;
      this.slothHitTimer = 0;
      this.slothPitchBeforeHit = this.slothCurrentPitch;
   }

   private float slMeasureAngle(LivingEntity e) {
      if (this.mc.player == null) {
         return 0.0F;
      } else {
         Vec3d eyes = this.mc.player.getEyePos();
         Vec3d mid = e.getBoundingBox().getCenter();
         Vec3d delta = mid.subtract(eyes);
         float needYaw = (float)Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0F;
         float needPitch = (float)(-Math.toDegrees(Math.atan2(delta.y, delta.horizontalLength())));
         float dYaw = Math.abs(MathHelper.wrapDegrees(needYaw - this.mc.player.getYaw()));
         float dPitch = Math.abs(needPitch - this.mc.player.getPitch());
         return dYaw + dPitch;
      }
   }

   private int slComputeReaction(float angle) {
      if (angle > 130.0F) {
         return 0 + (int)(Math.random() * 10.0);
      } else if (angle > 70.0F) {
         return 0 + (int)(Math.random() * 10.0);
      } else {
         return angle > 30.0F ? 0 + (int)(Math.random() * 10.0) : 0 + (int)(Math.random() * 5.0);
      }
   }

   private boolean slIsMovingForward() {
      return this.mc.player == null ? false : this.mc.options.forwardKey.isPressed();
   }

   private boolean slIsOvertakingTarget(LivingEntity target) {
      if (this.mc.player != null && target != null) {
         Vec3d playerPos = this.mc.player.getPos();
         Vec3d targetPos = target.getPos();
         Vec3d playerVel = new Vec3d(
            this.mc.player.getX() - this.mc.player.prevX,
            this.mc.player.getY() - this.mc.player.prevY,
            this.mc.player.getZ() - this.mc.player.prevZ
         );
         Vec3d targetVel = new Vec3d(
            target.getX() - target.prevX, target.getY() - target.prevY, target.getZ() - target.prevZ
         );
         Vec3d toTarget = targetPos.subtract(playerPos).normalize();
         double playerSpeedToTarget = playerVel.dotProduct(toTarget);
         double targetSpeedToPlayer = targetVel.dotProduct(toTarget.multiply(-1.0));
         double relativeSpeed = playerSpeedToTarget + targetSpeedToPlayer;
         double distance = Math.sqrt(Math.pow(playerPos.x - targetPos.x, 2.0) + Math.pow(playerPos.z - targetPos.z, 2.0));
         return relativeSpeed > 0.05 && distance < 4.0;
      } else {
         return false;
      }
   }

   private float[] slGenerateNoise(float dist) {
      this.slothNoiseAngle = this.slothNoiseAngle + 0.042F + (float)(Math.random() * 0.018F);
      float scale = MathHelper.clamp(dist / 4.5F, 0.25F, 1.0F);
      float amp = 1.8F * scale;
      float n1 = (float)Math.sin((double)this.slothNoiseAngle * 0.87) * 0.38F;
      float n2 = (float)Math.sin((double)this.slothNoiseAngle * 1.43 + 0.75) * 0.28F;
      float n3 = (float)Math.cos((double)this.slothNoiseAngle * 1.18 + 0.35) * 0.32F;
      float n4 = (float)Math.cos((double)this.slothNoiseAngle * 1.76 + 1.42) * 0.23F;
      float yawNoise = (n1 + n2) * amp;
      float pitchNoise = (n3 + n4) * amp * 0.52F;
      yawNoise += ((float)Math.random() - 0.5F) * amp * 0.13F;
      pitchNoise += ((float)Math.random() - 0.5F) * amp * 0.09F;
      return new float[]{yawNoise, pitchNoise};
   }

   private float slSmoothStep(float x) {
      x = MathHelper.clamp(x, 0.0F, 1.0F);
      return x * x * (3.0F - 2.0F * x);
   }

   private float slAccelCurve(float x) {
      x = MathHelper.clamp(x, 0.0F, 1.0F);
      return 1.0F - (1.0F - x) * (1.0F - x);
   }

   private float slSpringInterp(float current, float target, float vel, float stiffness, float damping) {
      float diff = target - current;
      float acc = diff * stiffness - vel * damping;
      return vel + acc;
   }

   private float slSmoothLerp(float from, float to, float alpha) {
      alpha = MathHelper.clamp(alpha, 0.0F, 1.0F);
      float delta = MathHelper.wrapDegrees(to - from);
      return from + delta * alpha;
   }

   private Vec3d slGetTargetPosition(LivingEntity target, boolean bothGliding) {
      return target.isGliding() ? PredictUtils.predict(target, this.predictValue.getValue()) : PredictUtils.predict(target, this.predictValue.getValue());
   }

   private void updateSlothRotation(LivingEntity target) {
      if (this.mc.player != null && target != null) {
         boolean playerFlying = this.mc.player.isGliding();
         boolean targetFlying = target.isGliding();
         boolean bothGliding = playerFlying && targetFlying;
         if (this.slothTrackedTarget != target) {
            this.slothTrackedTarget = target;
            this.slothCurrentYaw = this.mc.player.getYaw();
            this.slothCurrentPitch = this.mc.player.getPitch();
            this.slothLastSentYaw = this.slothCurrentYaw;
            this.slothLastSentPitch = this.slothCurrentPitch;
            this.slothSmoothYaw = this.slothCurrentYaw;
            this.slothSmoothPitch = this.slothCurrentPitch;
            this.slothVelocityYaw = this.slothVelocityPitch = 0.0F;
            this.slPickAimPoint(target);
            this.slothHitPhase = this.slothHitTimer = 0;
            this.slothNoiseAngle = (float)(Math.random() * Math.PI * 2.0);
            float angleDiff = this.slMeasureAngle(target);
            this.slothReactionMs = this.slComputeReaction(angleDiff);
            this.slothFirstSeenTime = System.currentTimeMillis();
            this.slothReactionComplete = false;
         }

         Vec3d eyePos = this.mc.player.getEyePos();
         Vec3d targetCenter = this.slGetTargetPosition(target, bothGliding);
         float distance = (float)eyePos.distanceTo(targetCenter);
         float gcd = this.slCalcGcd();
         if (!this.slothReactionComplete) {
            long elapsed = System.currentTimeMillis() - this.slothFirstSeenTime;
            if (elapsed < (long)this.slothReactionMs) {
               float jitterY = ((float)Math.random() - 0.5F) * 0.22F;
               float jitterP = ((float)Math.random() - 0.5F) * 0.14F;
               float outY = this.slothLastSentYaw + jitterY;
               float outP = MathHelper.clamp(this.slothLastSentPitch + jitterP, -89.0F, 89.0F);
               outY -= (outY - this.slothLastSentYaw) % gcd;
               outP -= (outP - this.slothLastSentPitch) % gcd;
               this.slothLastSentYaw = outY;
               this.slothLastSentPitch = outP;
               RotationComponent.update(new Rotation(outY, outP), 360.0F, 360.0F, 360.0F, 360.0F, 0, 1, this.clientLook.getValue());
               return;
            }

            this.slothReactionComplete = true;
         }

         float[] noise = this.slGenerateNoise(distance);
         if (this.slothHitPhase > 0) {
            this.slothHitTimer++;
            int upDuration = 25;
            int downDuration = 20;
            float targetPitchUp = -89.0F;
            if (this.slothHitPhase == 1) {
               float t = (float)this.slothHitTimer / (float)upDuration;
               t = MathHelper.clamp(t, 0.0F, 1.0F);
               float curved = this.slAccelCurve(t);
               this.slothCurrentPitch = MathHelper.lerp(curved, this.slothPitchBeforeHit, targetPitchUp);
               if (this.slothHitTimer >= upDuration) {
                  this.slothHitPhase = 2;
                  this.slothHitTimer = 0;
               }
            } else if (this.slothHitPhase == 2) {
               float goal = this.slothPitchBeforeHit;
               float t = (float)this.slothHitTimer / (float)downDuration;
               t = MathHelper.clamp(t, 0.0F, 1.0F);
               float curved = this.slSmoothStep(t);
               this.slothCurrentPitch = MathHelper.lerp(curved, targetPitchUp, goal);
               if (this.slothHitTimer >= downDuration) {
                  this.slothHitPhase = 0;
                  this.slothHitTimer = 0;
               }
            }

            float outY = this.slothCurrentYaw + noise[0];
            float outP = MathHelper.clamp(this.slothCurrentPitch + noise[1], -89.0F, 89.0F);
            outY -= (outY - this.slothLastSentYaw) % gcd;
            outP -= (outP - this.slothLastSentPitch) % gcd;
            this.slothLastSentYaw = outY;
            this.slothLastSentPitch = outP;
            RotationComponent.update(new Rotation(outY, outP), 360.0F, 360.0F, 360.0F, 360.0F, 0, 1, this.clientLook.getValue());
         } else {
            if (Math.random() < 0.015) {
               this.slPickAimPoint(target);
            }

            Vec3d aimPos = targetCenter.add(this.slothAimPointX, this.slothAimPointY, this.slothAimPointZ);
            Vec3d direction = aimPos.subtract(eyePos);
            float wantYaw = (float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90.0);
            float wantPitch = (float)(-Math.toDegrees(Math.atan2(direction.y, direction.horizontalLength())));
            float diffYaw = MathHelper.wrapDegrees(wantYaw - this.slothCurrentYaw);
            float diffPitch = wantPitch - this.slothCurrentPitch;
            float speedMultiplier = 1.0F;
            boolean movingForward = this.slIsMovingForward();
            boolean overtaking = this.slIsOvertakingTarget(target);
            if (movingForward || overtaking) {
               speedMultiplier = 0.5F;
            }

            if (bothGliding) {
               speedMultiplier = 0.42995F;
            }

            float stiffness = (0.03686F + (float)Math.random() * 0.00873F) * speedMultiplier;
            float damping = 0.68F + 0.12F * (1.0F - speedMultiplier);
            float totalDiff = (float)Math.sqrt((double)(diffYaw * diffYaw + diffPitch * diffPitch));
            if (totalDiff > 32.0F) {
               stiffness += 0.018F * speedMultiplier;
            } else if (totalDiff < 4.2F) {
               stiffness *= 0.48F;
            }

            stiffness += MathHelper.clamp((distance - 1.6F) / 7.5F, 0.0F, 0.045F) * speedMultiplier;
            this.slothVelocityYaw = this.slSpringInterp(this.slothCurrentYaw, this.slothCurrentYaw + diffYaw, this.slothVelocityYaw, stiffness, damping);
            this.slothVelocityPitch = this.slSpringInterp(this.slothCurrentPitch, wantPitch, this.slothVelocityPitch, stiffness * 0.87F, damping);
            float maxVelYaw = 7.1659F * speedMultiplier;
            float maxVelPitch = 5.5416F * speedMultiplier;
            this.slothVelocityYaw = MathHelper.clamp(this.slothVelocityYaw, -maxVelYaw, maxVelYaw);
            this.slothVelocityPitch = MathHelper.clamp(this.slothVelocityPitch, -maxVelPitch, maxVelPitch);
            this.slothCurrentYaw = this.slothCurrentYaw + this.slothVelocityYaw;
            this.slothCurrentPitch = this.slothCurrentPitch + this.slothVelocityPitch;
            this.slothCurrentPitch = MathHelper.clamp(this.slothCurrentPitch, -89.0F, 89.0F);
            float smoothFactor = bothGliding ? 0.35F : 0.85F;
            this.slothSmoothYaw = this.slSmoothLerp(this.slothSmoothYaw, this.slothCurrentYaw, smoothFactor);
            this.slothSmoothPitch = this.slSmoothLerp(this.slothSmoothPitch, this.slothCurrentPitch, smoothFactor * 0.95F);
            float outY = this.slothSmoothYaw + noise[0];
            float outP = this.slothSmoothPitch + noise[1];
            outP = MathHelper.clamp(outP, -89.0F, 89.0F);
            outY -= (outY - this.slothLastSentYaw) % gcd;
            outP -= (outP - this.slothLastSentPitch) % gcd;
            this.slothLastSentYaw = outY;
            this.slothLastSentPitch = outP;
            RotationComponent.update(new Rotation(outY, outP), 360.0F, 360.0F, 360.0F, 360.0F, 0, 1, this.clientLook.getValue());
         }
      }
   }

   private void updateLonyJirRotation(LivingEntity target) {
      double time = (double)System.nanoTime() * 1.0E-9;
      Rotation angle = new Rotation(
         RotationUtil.calculate(target.getBoundingBox().getCenter().add(0.0, (double)((float)Math.abs(Math.sin(time * 19.0)) / 2.0F), 0.0))
      );
      Vec3d predict = PredictUtils.predict(target, this.predictValue.getValue() + 2.5);
      if (target.isGliding() && this.predictate.getValue() && !this.isTurnaroundActive) {
         angle = new Rotation(predict);
      }

      if (!RaytraceUtil.rayTrace(this.mc.player.getRotationVector(), 999.0, target.getBoundingBox().expand(-0.2F))) {
         this.speedAcceleration = this.speedAcceleration + (float)Math.abs(Math.sin(time * 19.0)) / 666.0F;
      } else if (this.speedAcceleration >= 0.02F) {
         this.speedAcceleration -= 0.02F;
      }

      float deltaYaw = MathHelper.wrapDegrees(angle.getYaw() - this.lastYaw);
      float deltaPitch = angle.getPitch() - this.lastPitch;
      float smooth = Math.min(Math.max(this.speedAcceleration, 0.0F), 0.2F);
      float newYaw = this.lastYaw + deltaYaw * smooth;
      float newPitch = this.lastPitch + deltaPitch * (smooth / 3.0F);
      newYaw -= (newYaw - this.lastYaw) % GCDFixer.getGCDValue();
      newPitch -= (newPitch - this.lastPitch) % GCDFixer.getGCDValue();
      Rotation smoothRot = new Rotation(newYaw, newPitch);
      float deltaYaw2 = MathHelper.wrapDegrees(this.mc.gameRenderer.getCamera().getYaw() - this.lastYaw);
      float deltaPitch2 = this.mc.gameRenderer.getCamera().getPitch() - this.lastPitch;
      if (this.mc.options.getPerspective() == Perspective.THIRD_PERSON_FRONT) {
         deltaYaw2 = MathHelper.wrapDegrees(this.mc.gameRenderer.getCamera().getYaw() - 180.0F - this.lastYaw);
         deltaPitch2 = -this.mc.gameRenderer.getCamera().getPitch() - this.lastPitch;
      }

      if (this.mc.player.isGliding() && target.isGliding()) {
         RotationComponent.update(smoothRot, 360.0F, 360.0F, 360.0F, 360.0F, 0, 1, this.clientLook.getValue());
      }

      this.lastYaw = smoothRot.getYaw();
      this.lastPitch = smoothRot.getPitch();
   }

   private void updateSmoothRotation(LivingEntity target) {
      if (target != null) {
         Vec3d targetPoint;
         if (target.isGliding() && this.predictate.getValue()) {
            Vec3d predicted = PredictUtils.predict(target, this.predictValue.getValue());
            double boxHeight = target.getBoundingBox().maxY - target.getBoundingBox().minY;
            targetPoint = new Vec3d(predicted.x, predicted.y + boxHeight * 0.8, predicted.z);
         } else {
            targetPoint = target.getEyePos();
         }

         Rotation angle = new Rotation(RotationUtil.calculate(targetPoint));
         float targetYaw = angle.getYaw();
         float targetPitch = angle.getPitch();
         float deltaYaw = MathHelper.wrapDegrees(targetYaw - this.lastYaw);
         float deltaPitch = targetPitch - this.lastPitch;
         float speed = 1.0F;
         float newYaw = this.lastYaw + deltaYaw * speed;
         float newPitch = this.lastPitch + deltaPitch * speed;
         float gcd = GCDFixer.getGCDValue();
         newYaw -= (newYaw - this.lastYaw) % gcd;
         newPitch -= (newPitch - this.lastPitch) % gcd;
         newPitch = MathHelper.clamp(newPitch, -90.0F, 90.0F);
         Rotation smoothRot = new Rotation(newYaw, newPitch);
         RotationComponent.update(smoothRot, 360.0F, 360.0F, 360.0F, 360.0F, 0, 1, this.clientLook.getValue());
         this.lastYaw = smoothRot.getYaw();
         this.lastPitch = smoothRot.getPitch();
      }
   }

   private void slothTest(LivingEntity target) {
      if (target != null) {
         Vec3d point = this.resolveMultipoint(target, BestPoint.getPoint2(target), 6.0);
         if (target.isGliding() && this.predictate.getValue() && !this.isTurnaroundActive) {
            point = PredictUtils.predict(target, this.predictValue.getValue());
         }

         boolean isLooking = RaytraceUtil.rayTrace(this.mc.player.getRotationVector(), 6.0, target.getBoundingBox().expand(0.0, -1.0, 0.0));
         Rotation idealRotation = new Rotation(RotationUtil.calculate(point));
         float targetYaw = idealRotation.getYaw();
         float targetPitch = idealRotation.getPitch();
         float randomFactor = (float)Math.random();
         float deltaYaw = MathHelper.wrapDegrees(targetYaw - this.lastYaw);
         float deltaPitch = targetPitch - this.lastPitch;
         float distance = this.mc.player.distanceTo(target) / 30.0F;
         if (!isLooking && this.mc.player.getAttackCooldownProgress(1.0F) >= 0.7F) {
            distance += 0.02F;
            stopWatch.reset();
         }

         if (!isLooking) {
            distance += 0.005F;
            stopWatch.reset();
         } else {
            distance *= 0.15F + randomFactor * 0.2F;
         }

         float smooth = Math.min(Math.max(distance, 0.0F), 0.12F);
         float newYaw = this.lastYaw + deltaYaw * smooth;
         float newPitch = this.lastPitch + deltaPitch * 0.5F * smooth;
         float gcd = GCDFixer.getGCDValue();
         newYaw -= (newYaw - this.lastYaw) % gcd;
         newPitch -= (newPitch - this.lastPitch) % gcd;
         newPitch = MathHelper.clamp(newPitch, -90.0F, 90.0F);
         Rotation legitRot = new Rotation(newYaw, newPitch);
         RotationComponent.update(legitRot, 360.0F, 360.0F, 360.0F, 360.0F, 0, 1, this.clientLook.getValue());
         this.lastYaw = legitRot.getYaw();
         this.lastPitch = legitRot.getPitch();
      }
   }

   private void updateLonyGriefRotation(LivingEntity target) {
      Vec3d point = target.isGliding() && this.predictate.getValue() && !this.isTurnaroundActive
         ? PredictUtils.predict(target, this.predictValue.getValue())
         : this.resolveMultipoint(target, BestPoint.getPoint(target), 6.0);
      Rotation angle = new Rotation(RotationUtil.calculate(point));
      float targetYaw = angle.getYaw();
      float targetPitch = angle.getPitch();
      if (!this.back) {
         float pon = this.mc.player.isGliding() ? 1.35F : 1.0F;
         this.speedAcceleration = this.speedAcceleration + (Math.abs(MathHelper.wrapDegrees(targetYaw - this.lastYaw)) > 40.0F ? 0.005F / pon : 0.0038F / pon);
         boolean isLooking = RaytraceUtil.rayTrace(this.mc.player.getRotationVector(), 6.0, target.getBoundingBox().expand(-0.2, -0.3, -0.2));
         if (this.speedAcceleration >= 0.16F / pon || isLooking) {
            this.back = true;
         }
      } else {
         if (this.speedAcceleration >= -0.01F) {
            this.speedAcceleration = this.speedAcceleration - (Math.abs(MathHelper.wrapDegrees(targetYaw - this.lastYaw)) > 60.0F ? 0.06F : 0.01F);
         }

         if (this.speedAcceleration <= -0.01F) {
            this.back = false;
         }
      }

      float deltaYaw = MathHelper.wrapDegrees(targetYaw - this.lastYaw);
      float deltaPitch = targetPitch - this.lastPitch;
      float smooth = Math.max(this.speedAcceleration, 0.0F);
      float newYaw = this.lastYaw + deltaYaw * Math.min(Math.max(smooth, 0.0F), 1.0F);
      float newPitch = this.lastPitch + deltaPitch * Math.min(Math.max(smooth / 2.0F, 0.0F), 1.0F);
      float gcdValue = GCDFixer.getGCDValue();
      newYaw -= (newYaw - this.lastYaw) % gcdValue;
      newPitch -= (newPitch - this.lastPitch) % gcdValue;
      Rotation smoothRot = new Rotation(newYaw, newPitch);
      RotationComponent.update(smoothRot, 360.0F, 360.0F, 360.0F, 360.0F, 0, 1, this.clientLook.getValue());
      this.lastYaw = smoothRot.getYaw();
      this.lastPitch = smoothRot.getPitch();
   }

   private void updateWellmineRotation(LivingEntity target) {
      Box box = target.getBoundingBox();
      Vec3d vector = this.resolveMultipoint(target, BestPoint.getMultipoint(target, 6.0), 6.0);
      if (target.isGliding() && this.predictate.getValue() && !this.isTurnaroundActive) {
         vector = PredictUtils.predict(target, this.predictValue.getValue());
      }

      Vec2f angle = RotationUtil.calculate(vector);
      float targetYaw = angle.x;
      float targetPitch = angle.y;
      if (!this.back) {
         if (this.speedAcceleration >= 1.0F) {
            this.speedAcceleration = 0.0F;
         } else if (this.mc.player.isGliding()) {
            float diff = Math.abs(MathHelper.wrapDegrees(angle.x - this.mc.player.getYaw()));
            this.speedAcceleration += diff > 40.0F ? 0.0025F : 0.005F;
         } else {
            this.speedAcceleration += 0.005F;
         }

         Vec3d offset = Vec3d.ZERO;
         if (this.mc.player.isGliding() && target instanceof PlayerEntity && target.isGliding()) {
            offset = PredictUtils.predict(target, this.predictValue.getValue());
         }

         if ((double)this.speedAcceleration >= 0.18
            || RaytraceUtil.rayTrace(this.mc.player.getRotationVector(), 6.0, box.offset(offset).expand(-0.5, -1.0, -0.5))) {
            this.back = true;
         }
      } else {
         if (this.speedAcceleration >= -0.01F) {
            float diff = Math.abs(MathHelper.wrapDegrees(targetYaw - this.mc.player.getYaw()));
            this.speedAcceleration -= diff > 40.0F ? 0.04F : 0.01F;
         }

         if (this.speedAcceleration <= -0.01F) {
            this.back = false;
         }
      }

      float randomYaw = (float)ThreadLocalRandom.current().nextDouble(-0.75, 0.75);
      float randomPitch = (float)ThreadLocalRandom.current().nextDouble(-0.75, 0.75);
      targetYaw += randomYaw;
      targetPitch += randomPitch;
      float smoothVal = Math.min(Math.max(this.speedAcceleration, -1.0F), 1.0F);
      float changeYaw = MathHelper.wrapDegrees(targetYaw - this.mc.player.getYaw()) * smoothVal;
      float changePitch = (targetPitch - this.mc.player.getPitch()) * (smoothVal / 2.0F);
      Rotation smoothRot = new Rotation(
         this.mc.player.getYaw() + changeYaw, MathHelper.clamp(this.mc.player.getPitch() + changePitch, -90.0F, 90.0F)
      );
      RotationComponent.update(smoothRot, 360.0F, 360.0F, 360.0F, 360.0F, 0, 1, this.clientLook.getValue());
      this.lastYaw = smoothRot.getYaw();
      this.lastPitch = smoothRot.getPitch();
   }

   private Vec3d resolveMultipoint(LivingEntity target, Vec3d point, double range) {
      return this.smartAim.getValue() && target != null ? BestPoint.getNearestVisiblePoint(target, point, range) : point;
   }

   private float applyGCD(float deltaRotation) {
      float sensitivity = (float)((Double)this.mc.options.getMouseSensitivity().getValue() * 0.6F + 0.2F);
      float multiplier = sensitivity * sensitivity * sensitivity * 8.0F * 0.15F;
      return (float)Math.round(deltaRotation / multiplier) * multiplier;
   }

   private void updateAssistRotation(LivingEntity target) {
      if (target != null) {
         boolean elytraDuel = this.mc.player.isGliding();
         if (elytraDuel || System.currentTimeMillis() - lastPhysicalMoveTime <= 100L) {
            if (System.currentTimeMillis() - lastPhysicalMoveTime < 100L) {
               this.lastYaw = this.mc.player.getYaw();
               this.lastPitch = this.mc.player.getPitch();
            }

            Vec3d point = this.resolveMultipoint(target, BestPoint.getPoint(target), 6.0);
            if (elytraDuel && target.isGliding() && this.predictate.getValue() && !this.isTurnaroundActive) {
               point = PredictUtils.predict(target, this.predictValue.getValue());
            }

            Vec3d eyePos = this.mc.player.getEyePos();
            double deltaX = point.x - eyePos.x;
            double deltaY = point.y - eyePos.y;
            double deltaZ = point.z - eyePos.z;
            double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
            float targetYaw = (float)(Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0);
            float targetPitch = (float)(-Math.toDegrees(Math.atan2(deltaY, distance)));
            float radius = 0.0F;
            Box box = target.getBoundingBox();
            boolean isAimed = RaytraceUtil.rayTrace(this.mc.player.getRotationVector(), 6.0, box.expand((double)radius, (double)radius, (double)radius));
            float speed;
            if (isAimed) {
               speed = 0.0625F;
            } else if (this.mc.player.isGliding()) {
               speed = 8.0F;
            } else if (this.mc.player.isOnGround()) {
               speed = 0.5F;
            } else {
               speed = 0.5F;
            }

            float cooldownMultiplier = this.mc.player.getAttackCooldownProgress(0.5F) > 0.85F ? 1.2F : 0.4F;
            speed *= cooldownMultiplier;
            if (!RaytraceUtil.rayTrace(
               this.mc.player.getRotationVector(), 6.0, box.expand((double)(radius + 0.1F), (double)(radius + 0.1F), (double)(radius + 0.1F))
            )) {
               this.speedAcceleration += 5.0E-4F * cooldownMultiplier;
            } else if (this.speedAcceleration >= -0.01F) {
               this.speedAcceleration -= 4.0E-4F;
            }

            float smooth = Math.max(this.speedAcceleration, 0.0F);
            speed += smooth + (float)((Math.random() - 0.5) * 0.02);
            float yawDelta = MathHelper.wrapDegrees(targetYaw - this.lastYaw);
            float pitchDelta = targetPitch - this.lastPitch;
            float clampedYawDelta = MathHelper.clamp(yawDelta, -speed, speed);
            float clampedPitchDelta = MathHelper.clamp(pitchDelta, -speed, speed);
            float newYaw = this.lastYaw + clampedYawDelta;
            float newPitch = MathHelper.clamp(this.lastPitch + clampedPitchDelta, -89.9F, 89.9F);
            float gcd = GCDFixer.getGCDValue();
            if (gcd > 0.0F) {
               newYaw = this.lastYaw + (float)Math.round((newYaw - this.lastYaw) / gcd) * gcd;
               newPitch = this.lastPitch + (float)Math.round((newPitch - this.lastPitch) / gcd) * gcd;
            }

            Rotation smoothRot = new Rotation(newYaw, newPitch);
            RotationComponent.update(smoothRot, 360.0F, 360.0F, 360.0F, 360.0F, 0, 1, this.clientLook.getValue());
            this.lastYaw = smoothRot.getYaw();
            this.lastPitch = smoothRot.getPitch();
         }
      }
   }

   private void renderPredictPoint(MatrixStack matrices, Camera camera, float tickDelta) {
      if (this.target != null && this.target.isGliding()) {
         Vec3d predictPos = PredictUtils.predict(this.target, this.predictValue.getValue());
         Vec3d camPos = camera.getPos();
         double renderX = predictPos.x - camPos.x;
         double renderY = predictPos.y - camPos.y;
         double renderZ = predictPos.z - camPos.z;
         float size = 0.35F;
         int color = ColorProvider.getColorClient();
         matrices.push();
         matrices.translate(renderX, renderY, renderZ);
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableDepthTest();
         RenderSystem.disableCull();
         RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
         Matrix4f matrix = matrices.peek().getPositionMatrix();
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder buffer = tessellator.begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
         float r = (float)(color >> 16 & 0xFF) / 255.0F;
         float g = (float)(color >> 8 & 0xFF) / 255.0F;
         float b = (float)(color & 0xFF) / 255.0F;
         float a = 1.0F;
         buffer.vertex(matrix, -size, -size, -size).color(r, g, b, a);
         buffer.vertex(matrix, size, -size, -size).color(r, g, b, a);
         buffer.vertex(matrix, size, -size, -size).color(r, g, b, a);
         buffer.vertex(matrix, size, -size, size).color(r, g, b, a);
         buffer.vertex(matrix, size, -size, size).color(r, g, b, a);
         buffer.vertex(matrix, -size, -size, size).color(r, g, b, a);
         buffer.vertex(matrix, -size, -size, size).color(r, g, b, a);
         buffer.vertex(matrix, -size, -size, -size).color(r, g, b, a);
         buffer.vertex(matrix, -size, size, -size).color(r, g, b, a);
         buffer.vertex(matrix, size, size, -size).color(r, g, b, a);
         buffer.vertex(matrix, size, size, -size).color(r, g, b, a);
         buffer.vertex(matrix, size, size, size).color(r, g, b, a);
         buffer.vertex(matrix, size, size, size).color(r, g, b, a);
         buffer.vertex(matrix, -size, size, size).color(r, g, b, a);
         buffer.vertex(matrix, -size, size, size).color(r, g, b, a);
         buffer.vertex(matrix, -size, size, -size).color(r, g, b, a);
         buffer.vertex(matrix, -size, -size, -size).color(r, g, b, a);
         buffer.vertex(matrix, -size, size, -size).color(r, g, b, a);
         buffer.vertex(matrix, size, -size, -size).color(r, g, b, a);
         buffer.vertex(matrix, size, size, -size).color(r, g, b, a);
         buffer.vertex(matrix, size, -size, size).color(r, g, b, a);
         buffer.vertex(matrix, size, size, size).color(r, g, b, a);
         buffer.vertex(matrix, -size, -size, size).color(r, g, b, a);
         buffer.vertex(matrix, -size, size, size).color(r, g, b, a);
         BufferRenderer.drawWithGlobalProgram(buffer.end());
         RenderSystem.enableDepthTest();
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
         matrices.pop();
      }
   }

   @Override
   public void onEnable() {
      this.target = null;
      this.razvorotikTicks = 0;
      MiranWare.getInstance().getModuleStorage().setSpeedAcceleration(0.0F);
      this.slReset();
      if (this.elytraTarget == null) {
         this.elytraTarget = MiranWare.getInstance().getModuleStorage().get(ElytraTarget.class);
      }

      if (!this.renderListenerRegistered) {
         WorldRenderEvents.LAST.register(this.renderListener);
         this.renderListenerRegistered = true;
      }

      super.onEnable();
   }

   @Override
   public void onDisable() {
      this.target = null;
      this.ticksToAttack = 0;
      this.speedAcceleration = 0.0F;
      this.razvorotikTicks = 0;
      this.isResolving = false;
      this.resolverPoint = null;
      MiranWare.getInstance().getModuleStorage().setSpeedAcceleration(0.0F);
      MiranWare.getInstance().getModuleStorage().setRandomness(1.0F);
      this.slReset();
      super.onDisable();
   }

   @Generated
   public LivingEntity getTarget() {
      return this.target;
   }
}
