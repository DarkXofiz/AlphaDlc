package zov.alphadlc.module.list.combat;

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
import zov.alphadlc.AlphaDLC;
import zov.alphadlc.event.EventGameUpdate;
import zov.alphadlc.event.list.EventChangeSprint;
import zov.alphadlc.event.list.EventTick;
import zov.alphadlc.event.list.MoveInputEvent;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.ModuleSettingDefinitions;
import zov.alphadlc.module.list.movement.ElytraTarget;
import zov.alphadlc.module.list.player.FreeCamera;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.module.settings.ModeListSetting;
import zov.alphadlc.module.settings.ModeSetting;
import zov.alphadlc.module.settings.SliderSetting;
import zov.alphadlc.util.friend.FriendRepository;
import zov.alphadlc.util.math.BestPoint;
import zov.alphadlc.util.math.RotationUtil;
import zov.alphadlc.util.math.StopWatch;
import zov.alphadlc.util.neuro.rotation.AIRotationRecorder;
import zov.alphadlc.util.player.combat.PredictUtils;
import zov.alphadlc.util.player.combat.RaytraceUtil;
import zov.alphadlc.util.player.simulate.SimulatedPlayer;
import zov.alphadlc.util.render.math.GCDFixer;
import zov.alphadlc.util.render.providers.ColorProvider;
import zov.alphadlc.util.rotation.Rotation;
import zov.alphadlc.util.rotation.RotationComponent;
import zov.alphadlc.util.text.ValueUnit;

@ModuleInformation(
   moduleName = "KillAura",
   moduleDesc = "\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438 \u0430\u0442\u0430\u043a\u0443\u0435\u0442 \u0431\u043b\u0438\u0436\u0430\u0439\u0448\u0438\u0445 \u0432\u0440\u0430\u0433\u043e\u0432",
   moduleCategory = ModuleCategory.COMBAT
)
public class KillAura extends Module {
   public final ModeSetting rotation = ModuleSettingDefinitions.killAuraRotation();
   public final ModeSetting rotationBehavior = new ModeSetting(
      "\u041f\u043e\u0432\u0435\u0434\u0435\u043d\u0438\u0435 \u0440\u043e\u0442\u0430\u0446\u0438\u0438",
      "\u041f\u043b\u0430\u0432\u043d\u0430\u044f",
      "\u041f\u043b\u0430\u0432\u043d\u0430\u044f",
      "\u0421\u043d\u0430\u043f\u044b"
   );
   private final ModeListSetting targets = new ModeListSetting(
      "\u0422\u0430\u0440\u0433\u0435\u0442\u044b",
      new BooleanSetting("\u0418\u0433\u0440\u043e\u043a\u0438", true),
      new BooleanSetting("\u0413\u043e\u043b\u044b\u0435", true),
      new BooleanSetting("\u041c\u043e\u043d\u0441\u0442\u0440\u044b", true),
      new BooleanSetting("\u0416\u0438\u0432\u043e\u0442\u043d\u044b\u0435", true)
   );
   public final SliderSetting distance = new SliderSetting(
      "\u0414\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f",
      ValueUnit.countable("\u0431\u043b\u043e\u043a", "\u0431\u043b\u043e\u043a\u0430", "\u0431\u043b\u043e\u043a\u043e\u0432"),
      3.0,
      2.0,
      6.0,
      0.1F
   );
   private final SliderSetting preRotation = new SliderSetting(
      "\u041f\u0440\u0435 \u0434\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f",
      ValueUnit.countable("\u0431\u043b\u043e\u043a", "\u0431\u043b\u043e\u043a\u0430", "\u0431\u043b\u043e\u043a\u043e\u0432"),
      1.5,
      0.0,
      3.0,
      0.1F
   );
   public final BooleanSetting raycastCheck = new BooleanSetting(
      "\u041f\u0440\u043e\u0432\u0435\u0440\u043a\u0430 \u043d\u0430 \u043d\u0430\u0432\u0435\u0434\u0435\u043d\u0438\u0435", true
   );
   public final BooleanSetting smartAim = new BooleanSetting("\u0423\u043c\u043d\u043e\u0435 \u043d\u0430\u0432\u0435\u0434\u0435\u043d\u0438\u0435", true);
   public final BooleanSetting predictate = new BooleanSetting("\u041f\u0440\u0435\u0434\u0438\u043a\u0442", true);
   public final SliderSetting predictValue = new SliderSetting(
      "\u041f\u0440\u0435\u0434\u0438\u043a\u0442 \u0437\u043d\u0430\u0447\u0435\u043d\u0438\u0435", 3.0, 1.0, 5.0, 0.1F
   );
   public final BooleanSetting elytraSlowdown = new BooleanSetting(
      "\u0417\u0430\u043c\u0435\u0434\u043b\u0435\u043d\u0438\u0435 \u043d\u0430 \u044d\u043b\u0438\u0442\u0440\u0430\u0445", true
   );
   public final ModeSetting slowdownMode = ModuleSettingDefinitions.killAuraSprintReset().setVisible(() -> this.elytraSlowdown.getValue());
   public final SliderSetting slowdownRadius = new SliderSetting(
         "\u0420\u0430\u0434\u0438\u0443\u0441 \u0437\u0430\u043c\u0435\u0434\u043b\u0435\u043d\u0438\u044f",
         ValueUnit.countable("\u0431\u043b\u043e\u043a", "\u0431\u043b\u043e\u043a\u0430", "\u0431\u043b\u043e\u043a\u043e\u0432"),
         3.0,
         1.0,
         6.0,
         0.1F
      )
      .setVisible(() -> this.elytraSlowdown.getValue() && this.slowdownMode.is("\u041f\u043e \u0440\u0430\u0434\u0438\u0443\u0441\u0443"));
   public final SliderSetting minSpeed = new SliderSetting("\u041c\u0438\u043d. \u0441\u043a\u043e\u0440\u043e\u0441\u0442\u044c", 0.3F, 0.1F, 0.9F, 0.05F)
      .setVisible(() -> this.elytraSlowdown.getValue() && this.slowdownMode.is("\u041f\u043e \u0440\u0430\u0434\u0438\u0443\u0441\u0443"));
   public final SliderSetting preHitTicks = new SliderSetting("\u0422\u0438\u043a\u0438 \u0434\u043e \u0443\u0434\u0430\u0440\u0430", 3.0, 1.0, 10.0, 1.0)
      .setVisible(() -> this.elytraSlowdown.getValue() && this.slowdownMode.is("\u041f\u0435\u0440\u0435\u0434 \u0443\u0434\u0430\u0440\u043e\u043c"));
   public final BooleanSetting hitAfterOvertake = new BooleanSetting(
      "\u0411\u0438\u0442\u044c \u0442\u043e\u043a\u043e \u043f\u043e\u0441\u043b\u0435 \u043f\u0435\u0440\u0435\u0433\u043e\u043d\u0430", true
   );
   public final ModeSetting moveFix = new ModeSetting(
      "\u041a\u043e\u0440\u0440\u0435\u043a\u0446\u0438\u044f \u0434\u0432\u0438\u0436\u0435\u043d\u0438\u044f",
      "\u0421\u0444\u043e\u043a\u0443\u0441\u0438\u0440\u043e\u0432\u0430\u043d\u043d\u0430\u044f",
      "\u041d\u0435\u0442",
      "\u0421\u0444\u043e\u043a\u0443\u0441\u0438\u0440\u043e\u0432\u0430\u043d\u043d\u0430\u044f",
      "\u0422\u0430\u0440\u0433\u0435\u0442\u0438\u0440\u043e\u0432\u0430\u043d\u043d\u0430\u044f"
   );
   public final BooleanSetting onlySpace = ModuleSettingDefinitions.killAuraOnlySpace();
   public final BooleanSetting clientLook = ModuleSettingDefinitions.killAuraClientLook();
   public final BooleanSetting showPredictPoint = new BooleanSetting(
      "\u041f\u043e\u043a\u0430\u0437\u0430\u0442\u044c \u043f\u0440\u0435\u0434\u0438\u043a\u0442 \u0442\u043e\u0447\u043a\u0443", true
   );
   public final BooleanSetting elytraTurnaround = new BooleanSetting(
      "\u0420\u0430\u0437\u0432\u043e\u0440\u043e\u0442 \u043d\u0430 \u044d\u043b\u0438\u0442\u0440\u0430\u0445", true
   );
   public final BooleanSetting visualElytraRotation = new BooleanSetting(
      "\u0412\u0438\u0437\u0443\u0430\u043b. \u0440\u043e\u0442\u043a\u0430 \u042d\u043b\u0438\u0442\u0440\u044b", true
   );
   private ElytraTarget elytraTarget;
   public static final BooleanSetting useResolver = new BooleanSetting("\u0420\u0435\u0437\u043e\u043b\u044c\u0432\u0435\u0440 (Elytra)", true);
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
         this.renderPredictPoint(context.matrixStack(), context.camera(), context.tickCounter().method_60637(true));
      }
   };

   public ElytraTarget getElytraTarget() {
      if (this.elytraTarget == null) {
         this.elytraTarget = AlphaDLC.getInstance().getModuleStorage().get(ElytraTarget.class);
      }

      return this.elytraTarget;
   }

   private void findResolverPoint() {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
         Vec3d eye = this.mc.field_1724.method_33571();
         float oppositeYaw = this.mc.field_1724.method_36454() + 180.0F;
         float searchPitch = -50.0F;
         int[] yawOffsets = new int[]{0, 30, -30, 45, -45, 60, -60, 90, -90};

         for (int offset : yawOffsets) {
            float testYaw = oppositeYaw + (float)offset;
            float radYaw = (float)Math.toRadians((double)testYaw);
            float radPitch = (float)Math.toRadians((double)searchPitch);
            double x = -Math.sin((double)radYaw) * Math.cos((double)radPitch);
            double y = -Math.sin((double)radPitch);
            double z = Math.cos((double)radYaw) * Math.cos((double)radPitch);
            Vec3d checkVec = new Vec3d(x, y, z).method_1029().method_1021(8.0);
            Vec3d endPoint = eye.method_1019(checkVec);
            if (this.mc
                  .field_1687
                  .method_17742(new RaycastContext(eye, endPoint, ShapeType.field_17558, FluidHandling.field_1348, this.mc.field_1724))
                  .method_17783()
               == Type.field_1333) {
               this.resolverPoint = endPoint;
               return;
            }
         }

         this.resolverPoint = null;
      }
   }

   @Subscribe
   private void onGameUpdate(EventGameUpdate e) {
      if (this.mc.field_1724 != null && this.target != null) {
         AlphaDLC.getInstance().getModuleStorage().setRandomness(1.0F);
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

            if (this.rotationBehavior.is("\u0421\u043d\u0430\u043f\u044b")) {
               boolean isReadyToAttack = this.mc.field_1724.method_7261(1.0F) >= 0.95F && this.ticksToAttack <= 1;
               if (!isReadyToAttack) {
                  return;
               }
            }

            boolean playerOnElytra = this.mc.field_1724.method_6128();
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
      if (this.mc.field_1724 != null) {
         if (this.moveFix.is("\u0422\u0430\u0440\u0433\u0435\u0442\u0438\u0440\u043e\u0432\u0430\u043d\u043d\u0430\u044f")) {
            if (this.target != null) {
               if (this.mc.field_1724.method_6128()) {
                  event.forward = 0.0F;
                  event.strafe = 0.0F;
               } else if (event.forward != 0.0F || event.strafe != 0.0F) {
                  float yaw = MathHelper.method_15393(this.mc.field_1773.method_19418().method_19330());
                  double dx = this.target.method_23317() - this.mc.field_1724.method_23317();
                  double dz = this.target.method_23321() - this.mc.field_1724.method_23321();
                  double targetAngle = MathHelper.method_15338(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
                  float bestForward = 0.0F;
                  float bestStrafe = 0.0F;
                  float smallestDiff = Float.MAX_VALUE;

                  for (float f = -1.0F; f <= 1.0F; f++) {
                     for (float s = -1.0F; s <= 1.0F; s++) {
                        if (f != 0.0F || s != 0.0F) {
                           double predictedAngle = MathHelper.method_15338(Math.toDegrees(RotationComponent.direction(yaw, f, s)));
                           float diff = Math.abs(MathHelper.method_15393((float)(targetAngle - predictedAngle)));
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
      if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
         if (this.ticksToAttack > 0) {
            this.ticksToAttack--;
         }

         this.updateTarget();
         if (this.target != null) {
            lastTarget = this.target;
            if (!this.elytraSlowdown.getValue() || !this.mc.field_1724.method_6128()) {
               isSlowdownActive = false;
            } else if (this.slowdownMode.is("\u041f\u0435\u0440\u0435\u0434 \u0443\u0434\u0430\u0440\u043e\u043c")) {
               isSlowdownActive = (double)this.ticksToAttack <= this.preHitTicks.getValue();
            } else {
               isSlowdownActive = false;
            }

            Vec3d predict = PredictUtils.predict(this.target, this.predictValue.getValue());
            double distToPredict = this.mc.field_1724.method_33571().method_1022(predict);
            if (!this.elytraSlowdown.getValue() || !this.mc.field_1724.method_6128()) {
               isSlowdownActive = false;
            } else if (this.slowdownMode.is("\u041f\u0435\u0440\u0435\u0434 \u0443\u0434\u0430\u0440\u043e\u043c")) {
               isSlowdownActive = (double)this.ticksToAttack <= this.preHitTicks.getValue();
            } else {
               isSlowdownActive = distToPredict < 2.7 && this.ticksToAttack <= 2;
            }

            if (this.canStopSprinting()) {
               this.mc.field_1724.method_5728(false);
            }

            if (this.canAttack()) {
               if (useResolver.getValue() && this.mc.field_1724.method_6128()) {
                  this.mc.field_1724.method_18800(0.0, 0.0, 0.0);
                  this.findResolverPoint();
                  if (this.resolverPoint != null) {
                     this.isResolving = true;
                     this.resolverTimer.reset();
                  }
               }

               this.mc.method_1562().method_52787(new PlayerInputC2SPacket(new PlayerInput(false, false, false, false, false, false, false)));
               this.mc.field_1761.method_2918(this.mc.field_1724, this.target);
               this.mc.field_1724.method_6104(Hand.field_5808);
               this.mc.method_1562().method_52787(new PlayerInputC2SPacket(this.mc.field_1724.field_3913.field_54155));
               this.ticksToAttack = 10;
            }
         } else {
            this.speedAcceleration = 0.0F;
         }
      }
   }

   private boolean isValidEntity(Entity entity) {
      if (!entity.method_5805()) {
         return false;
      } else {
         PlayerEntity player = (PlayerEntity)(AlphaDLC.getInstance().getModuleStorage().get(FreeCamera.class).fakePlayer != null
            ? AlphaDLC.getInstance().getModuleStorage().get(FreeCamera.class).fakePlayer
            : this.mc.field_1724);
         if (entity == AlphaDLC.getInstance().getModuleStorage().get(FreeCamera.class).fakePlayer) {
            return false;
         } else if (entity instanceof ClientPlayerEntity) {
            return false;
         } else if (entity instanceof ArmorStandEntity) {
            return false;
         } else {
            if (entity instanceof PlayerEntity p && p.method_6096() != 0 && !this.targets.isEnabled("\u0418\u0433\u0440\u043e\u043a\u0438")) {
               return false;
            }

            if (entity instanceof PlayerEntity p && p.method_6096() == 0 && !this.targets.isEnabled("\u0413\u043e\u043b\u044b\u0435")) {
               return false;
            }

            if ((entity instanceof HostileEntity || entity instanceof AmbientEntity) && !this.targets.isEnabled("\u041c\u043e\u043d\u0441\u0442\u0440\u044b")) {
               return false;
            } else if ((entity instanceof PassiveEntity || entity instanceof FishEntity)
               && !this.targets.isEnabled("\u0416\u0438\u0432\u043e\u0442\u043d\u044b\u0435")) {
               return false;
            } else {
               if (entity instanceof PlayerEntity p && !FriendRepository.shouldAttack(p)) {
                  return false;
               }

               return !(
                  player.method_33571().method_1022(BestPoint.getNearestPoint(entity))
                     > (player.method_6128() ? 50.0 : this.distance.getValue() + this.preRotation.getValue())
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
         PlayerEntity player = (PlayerEntity)(AlphaDLC.getInstance().getModuleStorage().get(FreeCamera.class).fakePlayer != null
            ? AlphaDLC.getInstance().getModuleStorage().get(FreeCamera.class).fakePlayer
            : this.mc.field_1724);
         boolean playerElytra = this.mc.field_1724.method_6128();
         boolean targetElytra = this.target.method_6128();
         boolean anyElytra = playerElytra || targetElytra;
         if (this.target.method_6128()) {
            Vec3d predict = PredictUtils.predict(this.target, this.predictValue.getValue());
            double distToPredict = player.method_33571().method_1022(predict);
            this.preddict = this.hitAfterOvertake.getValue() ? 2.7F : 4.0F;
            if (distToPredict <= (double)this.preddict && this.elytraTurnaround.getValue()) {
               this.isTurnaroundActive = true;
            }
         }

         if (!AlphaDLC.getInstance().getIdealHitUtils().cooldownIsReached(false)) {
            return false;
         } else if (this.ticksToAttack > 0) {
            return false;
         } else if (anyElytra) {
            return this.elytraTarget != null && this.elytraTarget.isEnabled() ? this.elytraTarget.canAttack(this.target) : false;
         } else if (!RaytraceUtil.rayTrace(player.method_5720(), this.distance.getValue(), this.target.method_5829()) && this.raycastCheck.getValue()) {
            return false;
         } else {
            return player.method_33571().method_1022(BestPoint.getNearestPoint(this.target)) > this.distance.getValue() - 0.2F
               ? false
               : AlphaDLC.getInstance().getIdealHitUtils().canCritical();
         }
      }
   }

   public boolean canStopSprinting() {
      if (this.target == null) {
         return false;
      } else if (!AlphaDLC.getInstance().getIdealHitUtils().cooldownIsReached(true)) {
         return false;
      } else {
         return this.ticksToAttack > 1 ? false : SimulatedPlayer.simulateLocalPlayer(1).fallDistance != 0.0F;
      }
   }

   private void updateTarget() {
      LivingEntity best = null;
      double bestFovDot = -1.0;
      Vec3d eyePos = this.mc.field_1724.method_33571();
      Vec3d lookVec = this.mc.field_1724.method_5828(1.0F);

      for (Entity entity : this.mc.field_1687.method_18112()) {
         if (entity instanceof LivingEntity && this.isValidEntity(entity)) {
            Vec3d targetVec = BestPoint.getNearestPoint(entity).method_1020(eyePos).method_1029();
            double dot = lookVec.method_1026(targetVec);
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
      if (this.mc.field_1724 != null) {
         this.slothCurrentYaw = this.mc.field_1724.method_36454();
         this.slothCurrentPitch = this.mc.field_1724.method_36455();
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
      double s = (Double)this.mc.field_1690.method_42495().method_41753() * 0.6 + 0.2;
      return (float)(s * s * s * 1.2);
   }

   private void slPickAimPoint(LivingEntity e) {
      Box bb = e.method_5829();
      double w = bb.field_1320 - bb.field_1323;
      double h = bb.field_1325 - bb.field_1322;
      double d = bb.field_1324 - bb.field_1321;
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
      if (this.mc.field_1724 == null) {
         return 0.0F;
      } else {
         Vec3d eyes = this.mc.field_1724.method_33571();
         Vec3d mid = e.method_5829().method_1005();
         Vec3d delta = mid.method_1020(eyes);
         float needYaw = (float)Math.toDegrees(Math.atan2(delta.field_1350, delta.field_1352)) - 90.0F;
         float needPitch = (float)(-Math.toDegrees(Math.atan2(delta.field_1351, delta.method_37267())));
         float dYaw = Math.abs(MathHelper.method_15393(needYaw - this.mc.field_1724.method_36454()));
         float dPitch = Math.abs(needPitch - this.mc.field_1724.method_36455());
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
      return this.mc.field_1724 == null ? false : this.mc.field_1690.field_1894.method_1434();
   }

   private boolean slIsOvertakingTarget(LivingEntity target) {
      if (this.mc.field_1724 != null && target != null) {
         Vec3d playerPos = this.mc.field_1724.method_19538();
         Vec3d targetPos = target.method_19538();
         Vec3d playerVel = new Vec3d(
            this.mc.field_1724.method_23317() - this.mc.field_1724.field_6014,
            this.mc.field_1724.method_23318() - this.mc.field_1724.field_6036,
            this.mc.field_1724.method_23321() - this.mc.field_1724.field_5969
         );
         Vec3d targetVel = new Vec3d(
            target.method_23317() - target.field_6014, target.method_23318() - target.field_6036, target.method_23321() - target.field_5969
         );
         Vec3d toTarget = targetPos.method_1020(playerPos).method_1029();
         double playerSpeedToTarget = playerVel.method_1026(toTarget);
         double targetSpeedToPlayer = targetVel.method_1026(toTarget.method_1021(-1.0));
         double relativeSpeed = playerSpeedToTarget + targetSpeedToPlayer;
         double distance = Math.sqrt(Math.pow(playerPos.field_1352 - targetPos.field_1352, 2.0) + Math.pow(playerPos.field_1350 - targetPos.field_1350, 2.0));
         return relativeSpeed > 0.05 && distance < 4.0;
      } else {
         return false;
      }
   }

   private float[] slGenerateNoise(float dist) {
      this.slothNoiseAngle = this.slothNoiseAngle + 0.042F + (float)(Math.random() * 0.018F);
      float scale = MathHelper.method_15363(dist / 4.5F, 0.25F, 1.0F);
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
      x = MathHelper.method_15363(x, 0.0F, 1.0F);
      return x * x * (3.0F - 2.0F * x);
   }

   private float slAccelCurve(float x) {
      x = MathHelper.method_15363(x, 0.0F, 1.0F);
      return 1.0F - (1.0F - x) * (1.0F - x);
   }

   private float slSpringInterp(float current, float target, float vel, float stiffness, float damping) {
      float diff = target - current;
      float acc = diff * stiffness - vel * damping;
      return vel + acc;
   }

   private float slSmoothLerp(float from, float to, float alpha) {
      alpha = MathHelper.method_15363(alpha, 0.0F, 1.0F);
      float delta = MathHelper.method_15393(to - from);
      return from + delta * alpha;
   }

   private Vec3d slGetTargetPosition(LivingEntity target, boolean bothGliding) {
      return target.method_6128() ? PredictUtils.predict(target, this.predictValue.getValue()) : PredictUtils.predict(target, this.predictValue.getValue());
   }

   private void updateSlothRotation(LivingEntity target) {
      if (this.mc.field_1724 != null && target != null) {
         boolean playerFlying = this.mc.field_1724.method_6128();
         boolean targetFlying = target.method_6128();
         boolean bothGliding = playerFlying && targetFlying;
         if (this.slothTrackedTarget != target) {
            this.slothTrackedTarget = target;
            this.slothCurrentYaw = this.mc.field_1724.method_36454();
            this.slothCurrentPitch = this.mc.field_1724.method_36455();
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

         Vec3d eyePos = this.mc.field_1724.method_33571();
         Vec3d targetCenter = this.slGetTargetPosition(target, bothGliding);
         float distance = (float)eyePos.method_1022(targetCenter);
         float gcd = this.slCalcGcd();
         if (!this.slothReactionComplete) {
            long elapsed = System.currentTimeMillis() - this.slothFirstSeenTime;
            if (elapsed < (long)this.slothReactionMs) {
               float jitterY = ((float)Math.random() - 0.5F) * 0.22F;
               float jitterP = ((float)Math.random() - 0.5F) * 0.14F;
               float outY = this.slothLastSentYaw + jitterY;
               float outP = MathHelper.method_15363(this.slothLastSentPitch + jitterP, -89.0F, 89.0F);
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
               t = MathHelper.method_15363(t, 0.0F, 1.0F);
               float curved = this.slAccelCurve(t);
               this.slothCurrentPitch = MathHelper.method_16439(curved, this.slothPitchBeforeHit, targetPitchUp);
               if (this.slothHitTimer >= upDuration) {
                  this.slothHitPhase = 2;
                  this.slothHitTimer = 0;
               }
            } else if (this.slothHitPhase == 2) {
               float goal = this.slothPitchBeforeHit;
               float t = (float)this.slothHitTimer / (float)downDuration;
               t = MathHelper.method_15363(t, 0.0F, 1.0F);
               float curved = this.slSmoothStep(t);
               this.slothCurrentPitch = MathHelper.method_16439(curved, targetPitchUp, goal);
               if (this.slothHitTimer >= downDuration) {
                  this.slothHitPhase = 0;
                  this.slothHitTimer = 0;
               }
            }

            float outY = this.slothCurrentYaw + noise[0];
            float outP = MathHelper.method_15363(this.slothCurrentPitch + noise[1], -89.0F, 89.0F);
            outY -= (outY - this.slothLastSentYaw) % gcd;
            outP -= (outP - this.slothLastSentPitch) % gcd;
            this.slothLastSentYaw = outY;
            this.slothLastSentPitch = outP;
            RotationComponent.update(new Rotation(outY, outP), 360.0F, 360.0F, 360.0F, 360.0F, 0, 1, this.clientLook.getValue());
         } else {
            if (Math.random() < 0.015) {
               this.slPickAimPoint(target);
            }

            Vec3d aimPos = targetCenter.method_1031(this.slothAimPointX, this.slothAimPointY, this.slothAimPointZ);
            Vec3d direction = aimPos.method_1020(eyePos);
            float wantYaw = (float)MathHelper.method_15338(Math.toDegrees(Math.atan2(direction.field_1350, direction.field_1352)) - 90.0);
            float wantPitch = (float)(-Math.toDegrees(Math.atan2(direction.field_1351, direction.method_37267())));
            float diffYaw = MathHelper.method_15393(wantYaw - this.slothCurrentYaw);
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

            stiffness += MathHelper.method_15363((distance - 1.6F) / 7.5F, 0.0F, 0.045F) * speedMultiplier;
            this.slothVelocityYaw = this.slSpringInterp(this.slothCurrentYaw, this.slothCurrentYaw + diffYaw, this.slothVelocityYaw, stiffness, damping);
            this.slothVelocityPitch = this.slSpringInterp(this.slothCurrentPitch, wantPitch, this.slothVelocityPitch, stiffness * 0.87F, damping);
            float maxVelYaw = 7.1659F * speedMultiplier;
            float maxVelPitch = 5.5416F * speedMultiplier;
            this.slothVelocityYaw = MathHelper.method_15363(this.slothVelocityYaw, -maxVelYaw, maxVelYaw);
            this.slothVelocityPitch = MathHelper.method_15363(this.slothVelocityPitch, -maxVelPitch, maxVelPitch);
            this.slothCurrentYaw = this.slothCurrentYaw + this.slothVelocityYaw;
            this.slothCurrentPitch = this.slothCurrentPitch + this.slothVelocityPitch;
            this.slothCurrentPitch = MathHelper.method_15363(this.slothCurrentPitch, -89.0F, 89.0F);
            float smoothFactor = bothGliding ? 0.35F : 0.85F;
            this.slothSmoothYaw = this.slSmoothLerp(this.slothSmoothYaw, this.slothCurrentYaw, smoothFactor);
            this.slothSmoothPitch = this.slSmoothLerp(this.slothSmoothPitch, this.slothCurrentPitch, smoothFactor * 0.95F);
            float outY = this.slothSmoothYaw + noise[0];
            float outP = this.slothSmoothPitch + noise[1];
            outP = MathHelper.method_15363(outP, -89.0F, 89.0F);
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
         RotationUtil.calculate(target.method_5829().method_1005().method_1031(0.0, (double)((float)Math.abs(Math.sin(time * 19.0)) / 2.0F), 0.0))
      );
      Vec3d predict = PredictUtils.predict(target, this.predictValue.getValue() + 2.5);
      if (target.method_6128() && this.predictate.getValue() && !this.isTurnaroundActive) {
         angle = new Rotation(predict);
      }

      if (!RaytraceUtil.rayTrace(this.mc.field_1724.method_5720(), 999.0, target.method_5829().method_1014(-0.2F))) {
         this.speedAcceleration = this.speedAcceleration + (float)Math.abs(Math.sin(time * 19.0)) / 666.0F;
      } else if (this.speedAcceleration >= 0.02F) {
         this.speedAcceleration -= 0.02F;
      }

      float deltaYaw = MathHelper.method_15393(angle.getYaw() - this.lastYaw);
      float deltaPitch = angle.getPitch() - this.lastPitch;
      float smooth = Math.min(Math.max(this.speedAcceleration, 0.0F), 0.2F);
      float newYaw = this.lastYaw + deltaYaw * smooth;
      float newPitch = this.lastPitch + deltaPitch * (smooth / 3.0F);
      newYaw -= (newYaw - this.lastYaw) % GCDFixer.getGCDValue();
      newPitch -= (newPitch - this.lastPitch) % GCDFixer.getGCDValue();
      Rotation smoothRot = new Rotation(newYaw, newPitch);
      float deltaYaw2 = MathHelper.method_15393(this.mc.field_1773.method_19418().method_19330() - this.lastYaw);
      float deltaPitch2 = this.mc.field_1773.method_19418().method_19329() - this.lastPitch;
      if (this.mc.field_1690.method_31044() == Perspective.field_26666) {
         deltaYaw2 = MathHelper.method_15393(this.mc.field_1773.method_19418().method_19330() - 180.0F - this.lastYaw);
         deltaPitch2 = -this.mc.field_1773.method_19418().method_19329() - this.lastPitch;
      }

      if (this.mc.field_1724.method_6128() && target.method_6128()) {
         RotationComponent.update(smoothRot, 360.0F, 360.0F, 360.0F, 360.0F, 0, 1, this.clientLook.getValue());
      }

      this.lastYaw = smoothRot.getYaw();
      this.lastPitch = smoothRot.getPitch();
   }

   private void updateSmoothRotation(LivingEntity target) {
      if (target != null) {
         Vec3d targetPoint;
         if (target.method_6128() && this.predictate.getValue()) {
            Vec3d predicted = PredictUtils.predict(target, this.predictValue.getValue());
            double boxHeight = target.method_5829().field_1325 - target.method_5829().field_1322;
            targetPoint = new Vec3d(predicted.field_1352, predicted.field_1351 + boxHeight * 0.8, predicted.field_1350);
         } else {
            targetPoint = target.method_33571();
         }

         Rotation angle = new Rotation(RotationUtil.calculate(targetPoint));
         float targetYaw = angle.getYaw();
         float targetPitch = angle.getPitch();
         float deltaYaw = MathHelper.method_15393(targetYaw - this.lastYaw);
         float deltaPitch = targetPitch - this.lastPitch;
         float speed = 1.0F;
         float newYaw = this.lastYaw + deltaYaw * speed;
         float newPitch = this.lastPitch + deltaPitch * speed;
         float gcd = GCDFixer.getGCDValue();
         newYaw -= (newYaw - this.lastYaw) % gcd;
         newPitch -= (newPitch - this.lastPitch) % gcd;
         newPitch = MathHelper.method_15363(newPitch, -90.0F, 90.0F);
         Rotation smoothRot = new Rotation(newYaw, newPitch);
         RotationComponent.update(smoothRot, 360.0F, 360.0F, 360.0F, 360.0F, 0, 1, this.clientLook.getValue());
         this.lastYaw = smoothRot.getYaw();
         this.lastPitch = smoothRot.getPitch();
      }
   }

   private void slothTest(LivingEntity target) {
      if (target != null) {
         Vec3d point = this.resolveMultipoint(target, BestPoint.getPoint2(target), 6.0);
         if (target.method_6128() && this.predictate.getValue() && !this.isTurnaroundActive) {
            point = PredictUtils.predict(target, this.predictValue.getValue());
         }

         boolean isLooking = RaytraceUtil.rayTrace(this.mc.field_1724.method_5720(), 6.0, target.method_5829().method_1009(0.0, -1.0, 0.0));
         Rotation idealRotation = new Rotation(RotationUtil.calculate(point));
         float targetYaw = idealRotation.getYaw();
         float targetPitch = idealRotation.getPitch();
         float randomFactor = (float)Math.random();
         float deltaYaw = MathHelper.method_15393(targetYaw - this.lastYaw);
         float deltaPitch = targetPitch - this.lastPitch;
         float distance = this.mc.field_1724.method_5739(target) / 30.0F;
         if (!isLooking && this.mc.field_1724.method_7261(1.0F) >= 0.7F) {
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
         newPitch = MathHelper.method_15363(newPitch, -90.0F, 90.0F);
         Rotation legitRot = new Rotation(newYaw, newPitch);
         RotationComponent.update(legitRot, 360.0F, 360.0F, 360.0F, 360.0F, 0, 1, this.clientLook.getValue());
         this.lastYaw = legitRot.getYaw();
         this.lastPitch = legitRot.getPitch();
      }
   }

   private void updateLonyGriefRotation(LivingEntity target) {
      Vec3d point = target.method_6128() && this.predictate.getValue() && !this.isTurnaroundActive
         ? PredictUtils.predict(target, this.predictValue.getValue())
         : this.resolveMultipoint(target, BestPoint.getPoint(target), 6.0);
      Rotation angle = new Rotation(RotationUtil.calculate(point));
      float targetYaw = angle.getYaw();
      float targetPitch = angle.getPitch();
      if (!this.back) {
         float pon = this.mc.field_1724.method_6128() ? 1.35F : 1.0F;
         this.speedAcceleration = this.speedAcceleration + (Math.abs(MathHelper.method_15393(targetYaw - this.lastYaw)) > 40.0F ? 0.005F / pon : 0.0038F / pon);
         boolean isLooking = RaytraceUtil.rayTrace(this.mc.field_1724.method_5720(), 6.0, target.method_5829().method_1009(-0.2, -0.3, -0.2));
         if (this.speedAcceleration >= 0.16F / pon || isLooking) {
            this.back = true;
         }
      } else {
         if (this.speedAcceleration >= -0.01F) {
            this.speedAcceleration = this.speedAcceleration - (Math.abs(MathHelper.method_15393(targetYaw - this.lastYaw)) > 60.0F ? 0.06F : 0.01F);
         }

         if (this.speedAcceleration <= -0.01F) {
            this.back = false;
         }
      }

      float deltaYaw = MathHelper.method_15393(targetYaw - this.lastYaw);
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
      Box box = target.method_5829();
      Vec3d vector = this.resolveMultipoint(target, BestPoint.getMultipoint(target, 6.0), 6.0);
      if (target.method_6128() && this.predictate.getValue() && !this.isTurnaroundActive) {
         vector = PredictUtils.predict(target, this.predictValue.getValue());
      }

      Vec2f angle = RotationUtil.calculate(vector);
      float targetYaw = angle.field_1343;
      float targetPitch = angle.field_1342;
      if (!this.back) {
         if (this.speedAcceleration >= 1.0F) {
            this.speedAcceleration = 0.0F;
         } else if (this.mc.field_1724.method_6128()) {
            float diff = Math.abs(MathHelper.method_15393(angle.field_1343 - this.mc.field_1724.method_36454()));
            this.speedAcceleration += diff > 40.0F ? 0.0025F : 0.005F;
         } else {
            this.speedAcceleration += 0.005F;
         }

         Vec3d offset = Vec3d.field_1353;
         if (this.mc.field_1724.method_6128() && target instanceof PlayerEntity && target.method_6128()) {
            offset = PredictUtils.predict(target, this.predictValue.getValue());
         }

         if ((double)this.speedAcceleration >= 0.18
            || RaytraceUtil.rayTrace(this.mc.field_1724.method_5720(), 6.0, box.method_997(offset).method_1009(-0.5, -1.0, -0.5))) {
            this.back = true;
         }
      } else {
         if (this.speedAcceleration >= -0.01F) {
            float diff = Math.abs(MathHelper.method_15393(targetYaw - this.mc.field_1724.method_36454()));
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
      float changeYaw = MathHelper.method_15393(targetYaw - this.mc.field_1724.method_36454()) * smoothVal;
      float changePitch = (targetPitch - this.mc.field_1724.method_36455()) * (smoothVal / 2.0F);
      Rotation smoothRot = new Rotation(
         this.mc.field_1724.method_36454() + changeYaw, MathHelper.method_15363(this.mc.field_1724.method_36455() + changePitch, -90.0F, 90.0F)
      );
      RotationComponent.update(smoothRot, 360.0F, 360.0F, 360.0F, 360.0F, 0, 1, this.clientLook.getValue());
      this.lastYaw = smoothRot.getYaw();
      this.lastPitch = smoothRot.getPitch();
   }

   private Vec3d resolveMultipoint(LivingEntity target, Vec3d point, double range) {
      return this.smartAim.getValue() && target != null ? BestPoint.getNearestVisiblePoint(target, point, range) : point;
   }

   private float applyGCD(float deltaRotation) {
      float sensitivity = (float)((Double)this.mc.field_1690.method_42495().method_41753() * 0.6F + 0.2F);
      float multiplier = sensitivity * sensitivity * sensitivity * 8.0F * 0.15F;
      return (float)Math.round(deltaRotation / multiplier) * multiplier;
   }

   private void updateAssistRotation(LivingEntity target) {
      if (target != null) {
         boolean elytraDuel = this.mc.field_1724.method_6128();
         if (elytraDuel || System.currentTimeMillis() - lastPhysicalMoveTime <= 100L) {
            if (System.currentTimeMillis() - lastPhysicalMoveTime < 100L) {
               this.lastYaw = this.mc.field_1724.method_36454();
               this.lastPitch = this.mc.field_1724.method_36455();
            }

            Vec3d point = this.resolveMultipoint(target, BestPoint.getPoint(target), 6.0);
            if (elytraDuel && target.method_6128() && this.predictate.getValue() && !this.isTurnaroundActive) {
               point = PredictUtils.predict(target, this.predictValue.getValue());
            }

            Vec3d eyePos = this.mc.field_1724.method_33571();
            double deltaX = point.field_1352 - eyePos.field_1352;
            double deltaY = point.field_1351 - eyePos.field_1351;
            double deltaZ = point.field_1350 - eyePos.field_1350;
            double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
            float targetYaw = (float)(Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0);
            float targetPitch = (float)(-Math.toDegrees(Math.atan2(deltaY, distance)));
            float radius = 0.0F;
            Box box = target.method_5829();
            boolean isAimed = RaytraceUtil.rayTrace(this.mc.field_1724.method_5720(), 6.0, box.method_1009((double)radius, (double)radius, (double)radius));
            float speed;
            if (isAimed) {
               speed = 0.0625F;
            } else if (this.mc.field_1724.method_6128()) {
               speed = 8.0F;
            } else if (this.mc.field_1724.method_24828()) {
               speed = 0.5F;
            } else {
               speed = 0.5F;
            }

            float cooldownMultiplier = this.mc.field_1724.method_7261(0.5F) > 0.85F ? 1.2F : 0.4F;
            speed *= cooldownMultiplier;
            if (!RaytraceUtil.rayTrace(
               this.mc.field_1724.method_5720(), 6.0, box.method_1009((double)(radius + 0.1F), (double)(radius + 0.1F), (double)(radius + 0.1F))
            )) {
               this.speedAcceleration += 5.0E-4F * cooldownMultiplier;
            } else if (this.speedAcceleration >= -0.01F) {
               this.speedAcceleration -= 4.0E-4F;
            }

            float smooth = Math.max(this.speedAcceleration, 0.0F);
            speed += smooth + (float)((Math.random() - 0.5) * 0.02);
            float yawDelta = MathHelper.method_15393(targetYaw - this.lastYaw);
            float pitchDelta = targetPitch - this.lastPitch;
            float clampedYawDelta = MathHelper.method_15363(yawDelta, -speed, speed);
            float clampedPitchDelta = MathHelper.method_15363(pitchDelta, -speed, speed);
            float newYaw = this.lastYaw + clampedYawDelta;
            float newPitch = MathHelper.method_15363(this.lastPitch + clampedPitchDelta, -89.9F, 89.9F);
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
      if (this.target != null && this.target.method_6128()) {
         Vec3d predictPos = PredictUtils.predict(this.target, this.predictValue.getValue());
         Vec3d camPos = camera.method_19326();
         double renderX = predictPos.field_1352 - camPos.field_1352;
         double renderY = predictPos.field_1351 - camPos.field_1351;
         double renderZ = predictPos.field_1350 - camPos.field_1350;
         float size = 0.35F;
         int color = ColorProvider.getColorClient();
         matrices.method_22903();
         matrices.method_22904(renderX, renderY, renderZ);
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableDepthTest();
         RenderSystem.disableCull();
         RenderSystem.setShader(ShaderProgramKeys.field_53876);
         Matrix4f matrix = matrices.method_23760().method_23761();
         Tessellator tessellator = Tessellator.method_1348();
         BufferBuilder buffer = tessellator.method_60827(DrawMode.field_29344, VertexFormats.field_1576);
         float r = (float)(color >> 16 & 0xFF) / 255.0F;
         float g = (float)(color >> 8 & 0xFF) / 255.0F;
         float b = (float)(color & 0xFF) / 255.0F;
         float a = 1.0F;
         buffer.method_22918(matrix, -size, -size, -size).method_22915(r, g, b, a);
         buffer.method_22918(matrix, size, -size, -size).method_22915(r, g, b, a);
         buffer.method_22918(matrix, size, -size, -size).method_22915(r, g, b, a);
         buffer.method_22918(matrix, size, -size, size).method_22915(r, g, b, a);
         buffer.method_22918(matrix, size, -size, size).method_22915(r, g, b, a);
         buffer.method_22918(matrix, -size, -size, size).method_22915(r, g, b, a);
         buffer.method_22918(matrix, -size, -size, size).method_22915(r, g, b, a);
         buffer.method_22918(matrix, -size, -size, -size).method_22915(r, g, b, a);
         buffer.method_22918(matrix, -size, size, -size).method_22915(r, g, b, a);
         buffer.method_22918(matrix, size, size, -size).method_22915(r, g, b, a);
         buffer.method_22918(matrix, size, size, -size).method_22915(r, g, b, a);
         buffer.method_22918(matrix, size, size, size).method_22915(r, g, b, a);
         buffer.method_22918(matrix, size, size, size).method_22915(r, g, b, a);
         buffer.method_22918(matrix, -size, size, size).method_22915(r, g, b, a);
         buffer.method_22918(matrix, -size, size, size).method_22915(r, g, b, a);
         buffer.method_22918(matrix, -size, size, -size).method_22915(r, g, b, a);
         buffer.method_22918(matrix, -size, -size, -size).method_22915(r, g, b, a);
         buffer.method_22918(matrix, -size, size, -size).method_22915(r, g, b, a);
         buffer.method_22918(matrix, size, -size, -size).method_22915(r, g, b, a);
         buffer.method_22918(matrix, size, size, -size).method_22915(r, g, b, a);
         buffer.method_22918(matrix, size, -size, size).method_22915(r, g, b, a);
         buffer.method_22918(matrix, size, size, size).method_22915(r, g, b, a);
         buffer.method_22918(matrix, -size, -size, size).method_22915(r, g, b, a);
         buffer.method_22918(matrix, -size, size, size).method_22915(r, g, b, a);
         BufferRenderer.method_43433(buffer.method_60800());
         RenderSystem.enableDepthTest();
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
         matrices.method_22909();
      }
   }

   @Override
   public void onEnable() {
      this.target = null;
      this.razvorotikTicks = 0;
      AlphaDLC.getInstance().getModuleStorage().setSpeedAcceleration(0.0F);
      this.slReset();
      if (this.elytraTarget == null) {
         this.elytraTarget = AlphaDLC.getInstance().getModuleStorage().get(ElytraTarget.class);
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
      AlphaDLC.getInstance().getModuleStorage().setSpeedAcceleration(0.0F);
      AlphaDLC.getInstance().getModuleStorage().setRandomness(1.0F);
      this.slReset();
      super.onDisable();
   }

   @Generated
   public LivingEntity getTarget() {
      return this.target;
   }
}
