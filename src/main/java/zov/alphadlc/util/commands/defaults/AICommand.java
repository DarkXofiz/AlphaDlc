package zov.alphadlc.util.commands.defaults;

import java.util.List;
import java.util.stream.Stream;
import zov.alphadlc.AlphaDLC;
import zov.alphadlc.util.chat.ChatUtil;
import zov.alphadlc.util.commands.api.Command;
import zov.alphadlc.util.commands.api.argument.IArgConsumer;
import zov.alphadlc.util.commands.api.exception.CommandException;
import zov.alphadlc.util.neuro.rotation.AIRotationManager;
import zov.alphadlc.util.neuro.rotation.AIRotationRecorder;

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
                  ChatUtil.send("\u00a7c\u0417\u0430\u043f\u0438\u0441\u044c \u0443\u0436\u0435 \u0438\u0434\u0435\u0442!");
                  return;
               }

               if (recorder == null) {
                  recorder = new AIRotationRecorder();
                  AlphaDLC.getInstance().getEventBus().register(recorder);
               }

               AIRotationRecorder.startRecording();
               ChatUtil.send("\u00a7a\u0417\u0430\u043f\u0438\u0441\u044c \u043d\u0430\u0447\u0430\u0442\u0430!");
               ChatUtil.send(
                  "\u00a77\u0410\u0442\u0430\u043a\u0443\u0439\u0442\u0435 \u0446\u0435\u043b\u044c, \u0432\u0430\u0448\u0438 \u0434\u0432\u0438\u0436\u0435\u043d\u0438\u044f \u0431\u0443\u0434\u0443\u0442 \u0437\u0430\u043f\u0438\u0441\u0430\u043d\u044b"
               );
               ChatUtil.send(
                  "\u00a77\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u0443\u0439\u0442\u0435 \u00a7f.ai stop \u00a77\u0434\u043b\u044f \u043e\u0441\u0442\u0430\u043d\u043e\u0432\u043a\u0438"
               );
               break;
            case "stop":
               if (!AIRotationRecorder.isRecording()) {
                  ChatUtil.send("\u00a7c\u0417\u0430\u043f\u0438\u0441\u044c \u043d\u0435 \u0438\u0434\u0435\u0442!");
                  return;
               }

               int samples = AIRotationRecorder.stopRecording();
               ChatUtil.send("\u00a7a\u0417\u0430\u043f\u0438\u0441\u044c \u043e\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d\u0430!");
               ChatUtil.send("\u00a77\u0417\u0430\u043f\u0438\u0441\u0430\u043d\u043e \u0441\u044d\u043c\u043f\u043b\u043e\u0432: \u00a7f" + samples);
               ChatUtil.send(
                  "\u00a77\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u0443\u0439\u0442\u0435 \u00a7f.ai save <name> \u00a77\u0434\u043b\u044f \u0441\u043e\u0445\u0440\u0430\u043d\u0435\u043d\u0438\u044f"
               );
               break;
            case "save":
               if (!args.hasAny()) {
                  ChatUtil.send("\u00a7c\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435: \u00a7f.ai save <name>");
                  return;
               }

               String name = args.getString();
               AIRotationManager.saveDataset(name);
               break;
            case "load":
               if (!args.hasAny()) {
                  ChatUtil.send("\u00a7c\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435: \u00a7f.ai load <modelname>");
                  return;
               }

               String modelName = args.getString();
               AIRotationManager.loadModel(modelName);
               break;
            case "train":
               if (!args.has(2)) {
                  ChatUtil.send("\u00a7c\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435: \u00a7f.ai train <dataset> <modelname>");
                  return;
               }

               String datasetName = args.getString();
               String modelName = args.getString();
               ChatUtil.send("\u00a77\u041d\u0430\u0447\u0438\u043d\u0430\u044e \u043e\u0431\u0443\u0447\u0435\u043d\u0438\u0435...");
               new Thread(() -> AIRotationManager.trainModel(datasetName, modelName)).start();
               break;
            case "list":
               AIRotationManager.listFiles();
               break;
            case "dir":
               AIRotationManager.openDirectory();
               break;
            default:
               ChatUtil.send(
                  "\u00a7c\u041d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043d\u0430\u044f \u043f\u043e\u0434\u043a\u043e\u043c\u0430\u043d\u0434\u0430: \u00a7f"
                     + subcommand
               );
               this.printHelp();
         }
      }
   }

   private void printHelp() {
      ChatUtil.send("\u00a7e\u00a7l=== AI Rotation Commands ===");
      ChatUtil.send(
         "\u00a7f.ai start \u00a77- \u041d\u0430\u0447\u0430\u0442\u044c \u0437\u0430\u043f\u0438\u0441\u044c \u0434\u0432\u0438\u0436\u0435\u043d\u0438\u0439"
      );
      ChatUtil.send("\u00a7f.ai stop \u00a77- \u041e\u0441\u0442\u0430\u043d\u043e\u0432\u0438\u0442\u044c \u0437\u0430\u043f\u0438\u0441\u044c");
      ChatUtil.send("\u00a7f.ai save <name> \u00a77- \u0421\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u0434\u0430\u0442\u0430\u0441\u0435\u0442");
      ChatUtil.send("\u00a7f.ai train <dataset> <model> \u00a77- \u041e\u0431\u0443\u0447\u0438\u0442\u044c \u043c\u043e\u0434\u0435\u043b\u044c");
      ChatUtil.send("\u00a7f.ai load <model> \u00a77- \u0417\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c \u043c\u043e\u0434\u0435\u043b\u044c");
      ChatUtil.send("\u00a7f.ai list \u00a77- \u0421\u043f\u0438\u0441\u043e\u043a \u0444\u0430\u0439\u043b\u043e\u0432");
      ChatUtil.send("\u00a7f.ai dir \u00a77- \u041e\u0442\u043a\u0440\u044b\u0442\u044c \u043f\u0430\u043f\u043a\u0443");
   }

   @Override
   public String getShortDesc() {
      return "\u0423\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u0435 AI \u0440\u043e\u0442\u0430\u0446\u0438\u044f\u043c\u0438";
   }

   @Override
   public List<String> getLongDesc() {
      return List.of(
         "\u041a\u043e\u043c\u0430\u043d\u0434\u0430 \u0434\u043b\u044f \u0437\u0430\u043f\u0438\u0441\u0438, \u043e\u0431\u0443\u0447\u0435\u043d\u0438\u044f \u0438 \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u044f AI \u043c\u043e\u0434\u0435\u043b\u0435\u0439 \u0440\u043e\u0442\u0430\u0446\u0438\u0439",
         "",
         "\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435:",
         ".ai start - \u043d\u0430\u0447\u0430\u0442\u044c \u0437\u0430\u043f\u0438\u0441\u044c",
         ".ai stop - \u043e\u0441\u0442\u0430\u043d\u043e\u0432\u0438\u0442\u044c \u0437\u0430\u043f\u0438\u0441\u044c",
         ".ai save <name> - \u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u0434\u0430\u0442\u0430\u0441\u0435\u0442",
         ".ai train <dataset> <model> - \u043e\u0431\u0443\u0447\u0438\u0442\u044c \u043c\u043e\u0434\u0435\u043b\u044c",
         ".ai load <model> - \u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c \u043c\u043e\u0434\u0435\u043b\u044c",
         ".ai list - \u0441\u043f\u0438\u0441\u043e\u043a \u0444\u0430\u0439\u043b\u043e\u0432",
         ".ai dir - \u043e\u0442\u043a\u0440\u044b\u0442\u044c \u043f\u0430\u043f\u043a\u0443"
      );
   }

   @Override
   public Stream<String> tabComplete(String label, IArgConsumer args) {
      return args.hasExactlyOne() ? Stream.of("start", "stop", "save", "load", "train", "list", "dir") : Stream.empty();
   }
}
