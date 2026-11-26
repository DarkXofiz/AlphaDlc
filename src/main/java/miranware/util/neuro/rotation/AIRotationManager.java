package miranware.util.neuro.rotation;

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
import miranware.util.chat.ChatUtil;

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
            "\u00a7cNo data to save! Use .ai start to begin recording"
         );
      } else {
         try {
            Path datasetPath = DATASETS_DIR.resolve(name + ".json");

            try (FileWriter writer = new FileWriter(datasetPath.toFile())) {
               GSON.toJson(samples, writer);
            }

            ChatUtil.send(
               "\u00a7aDataset \u00a7e"
                  + name
                  + " \u00a7asaved (\u00a7f"
                  + samples.size()
                  + " \u00a7asamples)"
            );
            ChatUtil.send("\u00a77Path: \u00a7f" + datasetPath.toAbsolutePath());
         } catch (IOException var8) {
            ChatUtil.send(
               "\u00a7cError saving dataset: "
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
               "\u00a7cDataset \u00a7e" + datasetName + " \u00a7cnot found!"
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
            ChatUtil.send("\u00a7cThe dataset is empty!");
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
            "\u00a7aModel \u00a7e"
               + modelName
               + " \u00a7atrained and saved successfully!"
         );
      } catch (ModelException | TranslateException | IOException var11) {
         ChatUtil.send(
            "\u00a7cError training model: "
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
               "\u00a7cModel \u00a7e" + modelName + " \u00a7cnot found!"
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
         ChatUtil.send("\u00a7aModel \u00a7e" + modelName + " \u00a7aactive!");
         System.out.println("MODEL LOADED SUCCESSFULLY: " + modelName);
      } catch (ModelException | IOException var2) {
         ChatUtil.send(
            "\u00a7cError loading model: "
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
         ChatUtil.send("\u00a7aDatasets:");

         for (File dataset : datasets) {
            String name = dataset.getName().replace(".json", "");
            ChatUtil.send("  \u00a77- \u00a7f" + name);
         }
      } else {
         ChatUtil.send("\u00a77Datasets: \u00a7cnone");
      }

      File[] models = MODELS_DIR.toFile().listFiles(File::isDirectory);
      if (models != null && models.length > 0) {
         ChatUtil.send("\u00a7aModels:");

         for (File model : models) {
            String name = model.getName();
            String status = currentModel != null && currentModel.toString().contains(name) ? " \u00a7a(active)" : "";
            ChatUtil.send("  \u00a77- \u00a7f" + name + status);
         }
      } else {
         ChatUtil.send("\u00a77Models: \u00a7cnone");
      }
   }

   public static void openDirectory() {
      try {
         Desktop.getDesktop().open(AI_DIR.toFile());
         ChatUtil.send("\u00a7aAI folder opened");
      } catch (IOException var1) {
         ChatUtil.send(
            "\u00a7cError opening folder: " + var1.getMessage()
         );
         ChatUtil.send("\u00a77Path: \u00a7f" + AI_DIR.toAbsolutePath());
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
