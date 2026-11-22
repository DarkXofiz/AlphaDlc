package zov.alphadlc.util.neuro.rotation;

import ai.djl.ModelException;
import ai.djl.translate.TranslateException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.awt.Desktop;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import lombok.Generated;
import zov.alphadlc.util.chat.ChatUtil;

public class AIRotationManager {
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final Path AI_DIR = Paths.get(".onetap", "ai");
   private static final Path DATASETS_DIR = AI_DIR.resolve("datasets");
   private static final Path MODELS_DIR = AI_DIR.resolve("models");
   private static AIRotationModel currentModel = null;

   public static void saveDataset(String name) {
      List<TrainingSample> samples = AIRotationRecorder.getSamples();
      if (samples.isEmpty()) {
         ChatUtil.send(
            "\u00a7c\u041d\u0435\u0442 \u0434\u0430\u043d\u043d\u044b\u0445 \u0434\u043b\u044f \u0441\u043e\u0445\u0440\u0430\u043d\u0435\u043d\u0438\u044f! \u0418\u0441\u043f\u043e\u043b\u044c\u0437\u0443\u0439\u0442\u0435 .ai start \u0434\u043b\u044f \u043d\u0430\u0447\u0430\u043b\u0430 \u0437\u0430\u043f\u0438\u0441\u0438"
         );
      } else {
         try {
            Path datasetPath = DATASETS_DIR.resolve(name + ".json");

            try (FileWriter writer = new FileWriter(datasetPath.toFile())) {
               GSON.toJson(samples, writer);
            }

            ChatUtil.send(
               "\u00a7a\u0414\u0430\u0442\u0430\u0441\u0435\u0442 \u00a7e"
                  + name
                  + " \u00a7a\u0441\u043e\u0445\u0440\u0430\u043d\u0435\u043d (\u00a7f"
                  + samples.size()
                  + " \u00a7a\u0441\u044d\u043c\u043f\u043b\u043e\u0432)"
            );
            ChatUtil.send("\u00a77\u041f\u0443\u0442\u044c: \u00a7f" + datasetPath.toAbsolutePath());
         } catch (IOException var8) {
            ChatUtil.send(
               "\u00a7c\u041e\u0448\u0438\u0431\u043a\u0430 \u0441\u043e\u0445\u0440\u0430\u043d\u0435\u043d\u0438\u044f \u0434\u0430\u0442\u0430\u0441\u0435\u0442\u0430: "
                  + var8.getMessage()
            );
            var8.printStackTrace();
         }
      }
   }

   public static void trainModel(String datasetName, String modelName) {
      try {
         Path datasetPath = DATASETS_DIR.resolve(datasetName + ".json");
         if (!Files.exists(datasetPath)) {
            ChatUtil.send(
               "\u00a7c\u0414\u0430\u0442\u0430\u0441\u0435\u0442 \u00a7e" + datasetName + " \u00a7c\u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d!"
            );
            return;
         }

         Type listType = (new TypeToken<List<TrainingSample>>() {
         }).getType();
         FileReader reader = new FileReader(datasetPath.toFile());

         List<TrainingSample> samples;
         try {
            samples = (List<TrainingSample>)GSON.fromJson(reader, listType);
         } catch (Throwable var10) {
            try {
               reader.close();
            } catch (Throwable var9) {
               var10.addSuppressed(var9);
            }

            throw var10;
         }

         reader.close();
         if (samples == null || samples.isEmpty()) {
            ChatUtil.send("\u00a7c\u0414\u0430\u0442\u0430\u0441\u0435\u0442 \u043f\u0443\u0441\u0442!");
            return;
         }

         float[][] features = new float[samples.size()][];
         float[][] labels = new float[samples.size()][];

         for (int i = 0; i < samples.size(); i++) {
            features[i] = samples.get(i).getInput();
            labels[i] = samples.get(i).getOutput();
         }

         AIRotationModel model = new AIRotationModel(modelName);
         model.train(features, labels);
         Path modelPath = MODELS_DIR.resolve(modelName);
         model.save(modelPath);
         model.close();
         ChatUtil.send(
            "\u00a7a\u041c\u043e\u0434\u0435\u043b\u044c \u00a7e"
               + modelName
               + " \u00a7a\u0443\u0441\u043f\u0435\u0448\u043d\u043e \u043e\u0431\u0443\u0447\u0435\u043d\u0430 \u0438 \u0441\u043e\u0445\u0440\u0430\u043d\u0435\u043d\u0430!"
         );
      } catch (ModelException | TranslateException | IOException var11) {
         ChatUtil.send(
            "\u00a7c\u041e\u0448\u0438\u0431\u043a\u0430 \u043e\u0431\u0443\u0447\u0435\u043d\u0438\u044f \u043c\u043e\u0434\u0435\u043b\u0438: "
               + var11.getMessage()
         );
         var11.printStackTrace();
      }
   }

   public static void loadModel(String modelName) {
      try {
         Path modelPath = MODELS_DIR.resolve(modelName);
         if (!Files.exists(modelPath)) {
            ChatUtil.send(
               "\u00a7c\u041c\u043e\u0434\u0435\u043b\u044c \u00a7e" + modelName + " \u00a7c\u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u0430!"
            );
            System.out.println("MODEL PATH NOT FOUND: " + modelPath.toAbsolutePath());
            return;
         }

         if (currentModel != null) {
            currentModel.close();
         }

         System.out.println("Loading model from: " + modelPath.toAbsolutePath());
         currentModel = new AIRotationModel(modelName);
         currentModel.load(modelPath);
         ChatUtil.send("\u00a7a\u041c\u043e\u0434\u0435\u043b\u044c \u00a7e" + modelName + " \u00a7a\u0430\u043a\u0442\u0438\u0432\u043d\u0430!");
         System.out.println("MODEL LOADED SUCCESSFULLY: " + modelName);
      } catch (ModelException | IOException var2) {
         ChatUtil.send(
            "\u00a7c\u041e\u0448\u0438\u0431\u043a\u0430 \u0437\u0430\u0433\u0440\u0443\u0437\u043a\u0438 \u043c\u043e\u0434\u0435\u043b\u0438: "
               + var2.getMessage()
         );
         System.out.println("MODEL LOAD ERROR: " + var2.getMessage());
         var2.printStackTrace();
      }
   }

   public static float[] predict(float[] input) {
      if (currentModel == null) {
         System.out.println("AI MODEL IS NULL! Load a model first.");
         return new float[]{0.0F, 0.0F};
      } else {
         try {
            System.out.println("AI Input: [" + input[0] + ", " + input[1] + ", " + input[2] + ", " + input[3] + "]");
            float[] result = currentModel.predict(input);
            System.out.println("AI Output: [" + result[0] + ", " + result[1] + "]");
            return result;
         } catch (Exception var2) {
            System.out.println("AI PREDICTION ERROR: " + var2.getMessage());
            var2.printStackTrace();
            return new float[]{0.0F, 0.0F};
         }
      }
   }

   public static void listFiles() {
      ChatUtil.send("\u00a7e\u00a7l=== AI Rotation Files ===");
      File[] datasets = DATASETS_DIR.toFile().listFiles((dir, namex) -> namex.endsWith(".json"));
      if (datasets != null && datasets.length > 0) {
         ChatUtil.send("\u00a7a\u0414\u0430\u0442\u0430\u0441\u0435\u0442\u044b:");

         for (File dataset : datasets) {
            String name = dataset.getName().replace(".json", "");
            ChatUtil.send("  \u00a77- \u00a7f" + name);
         }
      } else {
         ChatUtil.send("\u00a77\u0414\u0430\u0442\u0430\u0441\u0435\u0442\u044b: \u00a7c\u043d\u0435\u0442");
      }

      File[] models = MODELS_DIR.toFile().listFiles(File::isDirectory);
      if (models != null && models.length > 0) {
         ChatUtil.send("\u00a7a\u041c\u043e\u0434\u0435\u043b\u0438:");

         for (File model : models) {
            String name = model.getName();
            String status = currentModel != null && currentModel.toString().contains(name) ? " \u00a7a(\u0430\u043a\u0442\u0438\u0432\u043d\u0430)" : "";
            ChatUtil.send("  \u00a77- \u00a7f" + name + status);
         }
      } else {
         ChatUtil.send("\u00a77\u041c\u043e\u0434\u0435\u043b\u0438: \u00a7c\u043d\u0435\u0442");
      }
   }

   public static void openDirectory() {
      try {
         Desktop.getDesktop().open(AI_DIR.toFile());
         ChatUtil.send("\u00a7a\u041f\u0430\u043f\u043a\u0430 AI \u043e\u0442\u043a\u0440\u044b\u0442\u0430");
      } catch (IOException var1) {
         ChatUtil.send(
            "\u00a7c\u041e\u0448\u0438\u0431\u043a\u0430 \u043e\u0442\u043a\u0440\u044b\u0442\u0438\u044f \u043f\u0430\u043f\u043a\u0438: " + var1.getMessage()
         );
         ChatUtil.send("\u00a77\u041f\u0443\u0442\u044c: \u00a7f" + AI_DIR.toAbsolutePath());
      }
   }

   @Generated
   public static AIRotationModel getCurrentModel() {
      return currentModel;
   }

   static {
      try {
         Files.createDirectories(DATASETS_DIR);
         Files.createDirectories(MODELS_DIR);
      } catch (IOException var1) {
         var1.printStackTrace();
      }
   }
}
