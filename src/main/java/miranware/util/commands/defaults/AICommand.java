package miranware.util.commands.defaults;

import java.util.List;
import java.util.stream.Stream;
import miranware.MiranWare;
import miranware.util.chat.ChatUtil;
import miranware.util.commands.api.Command;
import miranware.util.commands.api.argument.IArgConsumer;
import miranware.util.commands.api.exception.CommandException;
import miranware.util.neuro.rotation.AIRotationManager;
import miranware.util.neuro.rotation.AIRotationRecorder;

public class AICommand extends Command {
   private static AIRotationRecorder recorder = null;

   public AICommand() {
      super("ai");
   }

   @Override
   public void execute(String label, IArgConsumer args) throws CommandException {
      if (!args.hasAny()) {
         this.printHelp();
      } else {
         String subcommand = args.getString().toLowerCase();
         switch (subcommand) {
            case "start":
               if (AIRotationRecorder.isRecording()) {
                  ChatUtil.send("\u00a7cRecording is already in progress!");
                  return;
               }

               if (recorder == null) {
                  recorder = new AIRotationRecorder();
                  MiranWare.getInstance().getEventBus().register(recorder);
               }

               AIRotationRecorder.startRecording();
               ChatUtil.send("\u00a7aRecording started!");
               ChatUtil.send(
                  "\u00a77Attack a target, your movements will be recorded"
               );
               ChatUtil.send(
                  "\u00a77Use \u00a7f.ai stop \u00a77to stop"
               );
               break;
            case "stop":
               if (!AIRotationRecorder.isRecording()) {
                  ChatUtil.send("\u00a7cNo recording in progress!");
                  return;
               }

               int samples = AIRotationRecorder.stopRecording();
               ChatUtil.send("\u00a7aRecording stopped!");
               ChatUtil.send("\u00a77Samples recorded: \u00a7f" + samples);
               ChatUtil.send(
                  "\u00a77Use \u00a7f.ai save <name> \u00a77to save"
               );
               break;
            case "save":
               if (!args.hasAny()) {
                  ChatUtil.send("\u00a7cUsage: \u00a7f.ai save <name>");
                  return;
               }

               String name = args.getString();
               AIRotationManager.saveDataset(name);
               break;
            case "load":
               if (!args.hasAny()) {
                  ChatUtil.send("\u00a7cUsage: \u00a7f.ai load <modelname>");
                  return;
               }

               String modelName = args.getString();
               AIRotationManager.loadModel(modelName);
               break;
            case "train":
               if (!args.has(2)) {
                  ChatUtil.send("\u00a7cUsage: \u00a7f.ai train <dataset> <modelname>");
                  return;
               }

               String datasetName = args.getString();
               String trainModelName = args.getString();
               ChatUtil.send("\u00a77Starting training...");
               new Thread(() -> AIRotationManager.trainModel(datasetName, trainModelName)).start();
               break;
            case "list":
               AIRotationManager.listFiles();
               break;
            case "dir":
               AIRotationManager.openDirectory();
               break;
            default:
               ChatUtil.send(
                  "\u00a7cUnknown subcommand: \u00a7f"
                     + subcommand
               );
               this.printHelp();
         }
      }
   }

   private void printHelp() {
      ChatUtil.send("\u00a7e\u00a7l=== AI Rotation Commands ===");
      ChatUtil.send(
         "\u00a7f.ai start \u00a77- Start recording movements"
      );
      ChatUtil.send("\u00a7f.ai stop \u00a77- Stop recording");
      ChatUtil.send("\u00a7f.ai save <name> \u00a77- Save the dataset");
      ChatUtil.send("\u00a7f.ai train <dataset> <model> \u00a77- Train a model");
      ChatUtil.send("\u00a7f.ai load <model> \u00a77- Load a model");
      ChatUtil.send("\u00a7f.ai list \u00a77- List files");
      ChatUtil.send("\u00a7f.ai dir \u00a77- Open the folder");
   }

   @Override
   public String getShortDesc() {
      return "AI rotation management";
   }

   @Override
   public List<String> getLongDesc() {
      return List.of(
         "Command for recording, training and using AI rotation models",
         "",
         "Usage:",
         ".ai start - start recording",
         ".ai stop - stop recording",
         ".ai save <name> - save the dataset",
         ".ai train <dataset> <model> - train a model",
         ".ai load <model> - load a model",
         ".ai list - list files",
         ".ai dir - open the folder"
      );
   }

   @Override
   public Stream<String> tabComplete(String label, IArgConsumer args) {
      return args.hasExactlyOne() ? Stream.of("start", "stop", "save", "load", "train", "list", "dir") : Stream.empty();
   }
}
