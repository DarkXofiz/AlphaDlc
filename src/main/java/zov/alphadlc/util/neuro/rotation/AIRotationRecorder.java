package zov.alphadlc.util.neuro.rotation;

import com.google.common.eventbus.Subscribe;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import zov.alphadlc.AlphaDLC;
import zov.alphadlc.event.list.EventTick;
import zov.alphadlc.module.list.combat.KillAura;
import zov.alphadlc.util.IMinecraft;
import zov.alphadlc.util.math.BestPoint;
import zov.alphadlc.util.math.RotationUtil;
import zov.alphadlc.util.rotation.Rotation;

public class AIRotationRecorder implements IMinecraft {
   private static boolean recording = false;
   private static final List<TrainingSample> samples = new ArrayList<>();
   private static Rotation previousRotation = null;
   private static float previousDeltaYaw = 0.0F;
   private static float previousDeltaPitch = 0.0F;

   @Subscribe
   public void onTick(EventTick event) {
      if (recording && mc.field_1724 != null) {
         LivingEntity target = KillAura.lastTarget;
         if (target != null && target.method_5805()) {
            KillAura killAura = AlphaDLC.getInstance().getModuleStorage().get(KillAura.class);
            if (killAura != null && killAura.isEnabled()) {
               Rotation currentRotation = new Rotation(MathHelper.method_15393(mc.field_1724.method_36454()), mc.field_1724.method_36455());
               if (previousRotation == null) {
                  previousRotation = currentRotation;
                  System.out
                     .println("AI RECORDER: Initialized with rotation " + String.format("%.2f, %.2f", currentRotation.getYaw(), currentRotation.getPitch()));
               } else {
                  float actualDeltaYaw = MathHelper.method_15393(currentRotation.getYaw() - previousRotation.getYaw());
                  float actualDeltaPitch = currentRotation.getPitch() - previousRotation.getPitch();
                  double distance = killAura.distance.getValue();
                  Vec3d targetPoint = BestPoint.getMultipoint(target, distance);
                  Rotation targetRotation = new Rotation(RotationUtil.calculate(targetPoint));
                  float targetDeltaYaw = MathHelper.method_15393(targetRotation.getYaw() - currentRotation.getYaw());
                  float targetDeltaPitch = targetRotation.getPitch() - currentRotation.getPitch();
                  if (!isValidData(actualDeltaYaw, actualDeltaPitch, targetDeltaYaw, targetDeltaPitch)) {
                     System.out.println("AI RECORDER: Invalid data, skipping sample");
                     previousRotation = currentRotation;
                     previousDeltaYaw = actualDeltaYaw;
                     previousDeltaPitch = actualDeltaPitch;
                  } else {
                     float[] input = new float[]{previousDeltaYaw, previousDeltaPitch, targetDeltaYaw, targetDeltaPitch};
                     float[] output = new float[]{actualDeltaYaw, actualDeltaPitch};
                     samples.add(new TrainingSample(input, output));
                     if (samples.size() % 20 == 0) {
                        System.out
                           .println(
                              "AI RECORDER: Sample "
                                 + samples.size()
                                 + " | Input: ["
                                 + String.format("%.2f, %.2f, %.2f, %.2f", input[0], input[1], input[2], input[3])
                                 + "] | Output: ["
                                 + String.format("%.2f, %.2f", output[0], output[1])
                                 + "]"
                           );
                     }

                     previousRotation = currentRotation;
                     previousDeltaYaw = actualDeltaYaw;
                     previousDeltaPitch = actualDeltaPitch;
                  }
               }
            }
         }
      }
   }

   private static boolean isValidData(float actualDeltaYaw, float actualDeltaPitch, float targetDeltaYaw, float targetDeltaPitch) {
      if (Float.isFinite(actualDeltaYaw) && Float.isFinite(actualDeltaPitch) && Float.isFinite(targetDeltaYaw) && Float.isFinite(targetDeltaPitch)) {
         return Math.abs(actualDeltaYaw) > 180.0F
               || Math.abs(actualDeltaPitch) > 90.0F
               || Math.abs(targetDeltaYaw) > 180.0F
               || Math.abs(targetDeltaPitch) > 90.0F
            ? false
            : !(Math.abs(actualDeltaYaw) < 0.01F) || !(Math.abs(actualDeltaPitch) < 0.01F);
      } else {
         return false;
      }
   }

   public static void startRecording() {
      recording = true;
      samples.clear();
      previousRotation = null;
      previousDeltaYaw = 0.0F;
      previousDeltaPitch = 0.0F;
      System.out.println("AI RECORDER: Started recording");
   }

   public static int stopRecording() {
      recording = false;
      int count = samples.size();
      previousRotation = null;
      System.out.println("AI RECORDER: Stopped recording, collected " + count + " samples");
      return count;
   }

   public static List<TrainingSample> getSamples() {
      return new ArrayList<>(samples);
   }

   public static void clearSamples() {
      samples.clear();
   }

   @Generated
   public static boolean isRecording() {
      return recording;
   }
}
