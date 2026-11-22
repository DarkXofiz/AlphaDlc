package zov.alphadlc.util.neuro.rotation;

import ai.djl.Model;
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Activation;
import ai.djl.nn.Blocks;
import ai.djl.nn.SequentialBlock;
import ai.djl.nn.core.Linear;
import ai.djl.nn.norm.BatchNorm;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.EasyTrain;
import ai.djl.training.Trainer;
import ai.djl.training.TrainingConfig;
import ai.djl.training.dataset.ArrayDataset;
import ai.djl.training.dataset.ArrayDataset.Builder;
import ai.djl.training.initializer.XavierInitializer;
import ai.djl.training.listener.TrainingListener.Defaults;
import ai.djl.training.loss.Loss;
import ai.djl.training.optimizer.Adam;
import ai.djl.training.tracker.Tracker;
import ai.djl.translate.TranslateException;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import zov.alphadlc.util.chat.ChatUtil;

public class AIRotationModel implements Closeable {
   private static final int NUM_EPOCH = 500;
   private static final int BATCH_SIZE = 32;
   private static final int INPUT_SIZE = 4;
   private static final int OUTPUT_SIZE = 2;
   private final Model model;
   private final Predictor<float[], float[]> predictor;
   private final String name;

   public AIRotationModel(String name) {
      this.name = name;
      this.model = Model.newInstance(name);
      this.model.setBlock(createMlpBlock());
      this.predictor = this.model.newPredictor(new FloatArrayTranslator());
   }

   public float[] predict(float[] input) throws TranslateException {
      return (float[])this.predictor.predict(input);
   }

   public void train(float[][] features, float[][] labels) throws ModelException, IOException, TranslateException {
      if (features.length == labels.length && features.length != 0) {
         ChatUtil.send(
            "\u00a7a\u041d\u0430\u0447\u0438\u043d\u0430\u044e \u043e\u0431\u0443\u0447\u0435\u043d\u0438\u0435 \u043c\u043e\u0434\u0435\u043b\u0438 \u00a7e"
               + this.name
               + "\u00a7a..."
         );
         ChatUtil.send("\u00a77\u0421\u044d\u043c\u043f\u043b\u043e\u0432: \u00a7f" + features.length + " \u00a77| \u042d\u043f\u043e\u0445: \u00a7f500");
         TrainingConfig trainingConfig = new DefaultTrainingConfig(Loss.l2Loss())
            .optInitializer(new XavierInitializer(), "weight")
            .optOptimizer(Adam.builder().optLearningRateTracker(Tracker.fixed(0.001F)).build())
            .addTrainingListeners(Defaults.logging("train"));
         Trainer trainer = this.model.newTrainer(trainingConfig);

         try {
            NDManager manager = NDManager.newBaseManager();

            try {
               ArrayDataset trainingSet = ((Builder)new Builder()
                     .setData(new NDArray[]{manager.create(features)})
                     .optLabels(new NDArray[]{manager.create(labels)})
                     .setSampling(32, true))
                  .build();
               trainer.initialize(new Shape[]{new Shape(new long[]{32L, 4L})});
               EasyTrain.fit(trainer, 500, trainingSet, null);
               ChatUtil.send("\u00a7a\u041e\u0431\u0443\u0447\u0435\u043d\u0438\u0435 \u0437\u0430\u0432\u0435\u0440\u0448\u0435\u043d\u043e!");
            } catch (Throwable var10) {
               if (manager != null) {
                  try {
                     manager.close();
                  } catch (Throwable var9) {
                     var10.addSuppressed(var9);
                  }
               }

               throw var10;
            }

            if (manager != null) {
               manager.close();
            }
         } catch (Throwable var11) {
            if (trainer != null) {
               try {
                  trainer.close();
               } catch (Throwable var8) {
                  var11.addSuppressed(var8);
               }
            }

            throw var11;
         }

         if (trainer != null) {
            trainer.close();
         }
      } else {
         throw new IllegalArgumentException("Features and labels must have the same size and be non-empty");
      }
   }

   public void load(Path path) throws IOException, ModelException {
      this.model.load(path, "model");
      ChatUtil.send("\u00a7a\u041c\u043e\u0434\u0435\u043b\u044c \u00a7e" + this.name + " \u00a7a\u0437\u0430\u0433\u0440\u0443\u0436\u0435\u043d\u0430");
   }

   public void save(Path path) throws IOException {
      this.model.save(path, "model");
      ChatUtil.send("\u00a7a\u041c\u043e\u0434\u0435\u043b\u044c \u00a7e" + this.name + " \u00a7a\u0441\u043e\u0445\u0440\u0430\u043d\u0435\u043d\u0430");
   }

   @Override
   public void close() {
      this.predictor.close();
      this.model.close();
   }

   private static SequentialBlock createMlpBlock() {
      return new SequentialBlock()
         .add(Linear.builder().setUnits(128L).build())
         .add(Blocks.batchFlattenBlock())
         .add(BatchNorm.builder().build())
         .add(Activation.reluBlock())
         .add(Linear.builder().setUnits(64L).build())
         .add(Blocks.batchFlattenBlock())
         .add(BatchNorm.builder().build())
         .add(Activation.reluBlock())
         .add(Linear.builder().setUnits(32L).build())
         .add(Blocks.batchFlattenBlock())
         .add(BatchNorm.builder().build())
         .add(Activation.reluBlock())
         .add(Linear.builder().setUnits(2L).build());
   }
}
