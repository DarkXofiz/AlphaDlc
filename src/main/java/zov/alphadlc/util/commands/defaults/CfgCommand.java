package zov.alphadlc.util.commands.defaults;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.ClickEvent.Action;
import net.minecraft.util.Formatting;
import zov.alphadlc.util.commands.api.Command;
import zov.alphadlc.util.commands.api.IBaritoneChatControl;
import zov.alphadlc.util.commands.api.argument.IArgConsumer;
import zov.alphadlc.util.commands.api.exception.CommandException;
import zov.alphadlc.util.commands.api.exception.CommandNotEnoughArgumentsException;
import zov.alphadlc.util.commands.api.helpers.Paginator;
import zov.alphadlc.util.commands.api.helpers.TabCompleteHelper;
import zov.alphadlc.util.config.ConfigManager;

public class CfgCommand extends Command {
   public CfgCommand() {
      super("cfg");
   }

   @Override
   public void execute(String label, IArgConsumer args) throws CommandException {
      String action = args.hasAny() ? args.getString().toLowerCase(Locale.US) : "list";
      switch (action) {
         case "save":
            this.handleSave(args);
            break;
         case "load":
            this.handleLoad(args);
            break;
         case "list":
            this.handleList(args, label);
            break;
         case "clear":
            for (String name : ConfigManager.getConfigs()) {
               Path file = Paths.get("alphadlc/configs").resolve(name + ".json");
               if (Files.exists(file)) {
                  try {
                     Files.delete(file);
                  } catch (IOException var12) {
                     this.logDirect(
                        Formatting.GRAY
                           + "\u041e\u0448\u0438\u0431\u043a\u0430 \u043f\u0440\u0438 \u0443\u0434\u0430\u043b\u0435\u043d\u0438\u0438 \u0444\u0430\u0439\u043b\u0430."
                     );
                     var12.printStackTrace();
                  }
               }
            }

            this.logDirect(
               "\u0421\u043f\u0438\u0441\u043e\u043a \u043a\u043e\u043d\u0444\u0438\u0433\u043e\u0432 \u043e\u0447\u0438\u0449\u0435\u043d",
               Formatting.GRAY
            );
            break;
         case "dir":
            try {
               File dir = new File("alphadlc/configs/");
               if (!dir.exists()) {
                  this.logDirect(Formatting.GRAY + "\u0417\u0430\u0447 \u043f\u0430\u043f\u043a\u0443 \u0443\u0434\u0430\u043b\u0438\u043b?");
                  dir.mkdirs();
               } else {
                  this.logDirect(
                     Formatting.GRAY
                        + "\u041e\u0442\u043a\u0440\u044b\u0432\u0430\u044e \u043f\u0430\u043f\u043a\u0443 \u0441 \u043a\u043e\u043d\u0444\u0438\u0433\u0430\u043c\u0438..."
                  );
               }

               Runtime.getRuntime().exec("explorer " + dir.getAbsolutePath());
            } catch (IOException var11) {
               this.logDirect(
                  Formatting.GRAY
                     + "\u041e\u0448\u0438\u0431\u043a\u0430 \u043f\u0440\u0438 \u043e\u0442\u043a\u0440\u044b\u0442\u0438\u0438 \u043f\u0430\u043f\u043a\u0438: "
                     + Formatting.WHITE
                     + var11.getMessage()
               );
            }
            break;
         case "remove":
            this.handleRemove(args);
            break;
         default:
            this.logDirect(
               "\u041d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043d\u0430\u044f \u043f\u043e\u0434\u043a\u043e\u043c\u0430\u043d\u0434\u0430. \u0418\u0441\u043f\u043e\u043b\u044c\u0437\u0443\u0439 load/save/remove/list/dir.",
               Formatting.GRAY
            );
      }
   }

   private void handleSave(IArgConsumer args) throws CommandException {
      args.requireExactly(1);
      String name = args.getString();
      ConfigManager.save(name);
      this.logDirect(
         Formatting.GRAY
            + "\u041a\u043e\u043d\u0444\u0438\u0433 \u0441 \u0438\u043c\u0435\u043d\u0435\u043c "
            + Formatting.WHITE
            + name
            + Formatting.GRAY
            + " \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u0441\u043e\u0445\u0440\u0430\u043d\u0451\u043d"
      );
   }

   private void handleLoad(IArgConsumer args) throws CommandException {
      args.requireExactly(1);
      String name = args.getString();
      if (!ConfigManager.getConfigs().contains(name)) {
         this.logDirect(
            Formatting.GRAY
               + "\u041a\u043e\u043d\u0444\u0438\u0433 \u0441 \u0442\u0430\u043a\u0438\u043c \u0438\u043c\u0435\u043d\u0435\u043c \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d"
         );
      } else {
         ConfigManager.load(name);
         this.logDirect(
            Formatting.GRAY
               + "\u041a\u043e\u043d\u0444\u0438\u0433 \u0441 \u0438\u043c\u0435\u043d\u0435\u043c "
               + Formatting.WHITE
               + name
               + Formatting.GRAY
               + " \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u0437\u0430\u0433\u0440\u0443\u0436\u0435\u043d"
         );
      }
   }

   private void handleList(IArgConsumer args, String label) throws CommandException {
      args.requireMax(1);
      List<String> configs = ConfigManager.getConfigs();
      this.logDirect("\u0421\u043f\u0438\u0441\u043e\u043a \u043a\u043e\u043d\u0444\u0438\u0433\u043e\u0432:", Formatting.GRAY);
      Paginator.paginate(
         args,
         new Paginator<>(configs),
         name -> {
            Text nameText = Text.literal(Formatting.GRAY + "- " + Formatting.WHITE + name + " ");
            Text loadText = Text.literal(Formatting.GREEN + "[\u0417\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c]")
               .styled(
                  style -> style.withClickEvent(new ClickEvent(Action.RUN_COMMAND, IBaritoneChatControl.FORCE_COMMAND_PREFIX + "cfg load " + name))
                        .withHoverEvent(new HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, Text.literal("Click to load config")))
               );
            Text deleteText = Text.literal(Formatting.RED + " [\u0423\u0434\u0430\u043b\u0438\u0442\u044c]")
               .styled(
                  style -> style.withClickEvent(new ClickEvent(Action.RUN_COMMAND, IBaritoneChatControl.FORCE_COMMAND_PREFIX + "cfg remove " + name))
                        .withHoverEvent(new HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, Text.literal("Click to delete config")))
               );
            return nameText.copy().append(loadText).append(deleteText);
         },
         IBaritoneChatControl.FORCE_COMMAND_PREFIX + label
      );
   }

   private void handleRemove(IArgConsumer args) throws CommandException {
      args.requireExactly(1);
      String name = args.getString();
      Path file = Paths.get("alphadlc/configs").resolve(name + ".json");
      if (Files.exists(file)) {
         try {
            Files.delete(file);
            this.logDirect(
               Formatting.GRAY
                  + "\u041a\u043e\u043d\u0444\u0438\u0433 "
                  + Formatting.WHITE
                  + name
                  + Formatting.GRAY
                  + " \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u0443\u0434\u0430\u043b\u0451\u043d"
            );
         } catch (IOException var5) {
            this.logDirect(
               Formatting.GRAY
                  + "\u041e\u0448\u0438\u0431\u043a\u0430 \u043f\u0440\u0438 \u0443\u0434\u0430\u043b\u0435\u043d\u0438\u0438 \u0444\u0430\u0439\u043b\u0430."
            );
            var5.printStackTrace();
         }
      } else {
         this.logDirect(Formatting.GRAY + "\u041a\u043e\u043d\u0444\u0438\u0433 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d");
      }
   }

   @Override
   public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
      if (args.hasAny() && args.hasExactlyOne()) {
         return new TabCompleteHelper().sortAlphabetically().prepend("load", "save", "remove", "list", "clear", "dir").filterPrefix(args.getString()).stream();
      } else {
         if (args.hasAny()) {
            String arg = args.getString();
            if (args.hasExactlyOne() && (arg.equalsIgnoreCase("load") || arg.equalsIgnoreCase("remove"))) {
               return ConfigManager.getConfigs().stream().filter(cfg -> {
                  try {
                     return cfg.startsWith(args.peekString());
                  } catch (CommandNotEnoughArgumentsException var3x) {
                     throw new RuntimeException(var3x);
                  }
               }).sorted();
            }
         }

         return Stream.empty();
      }
   }

   @Override
   public String getShortDesc() {
      return "\u0423\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u0435 \u043a\u043e\u043d\u0444\u0438\u0433\u0430\u043c\u0438";
   }

   @Override
   public List<String> getLongDesc() {
      return Arrays.asList(
         "\u041a\u043e\u043c\u0430\u043d\u0434\u0430 \u0434\u043b\u044f \u0443\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u044f \u043a\u043e\u043d\u0444\u0438\u0433\u0443\u0440\u0430\u0446\u0438\u044f\u043c\u0438 \u043a\u043b\u0438\u0435\u043d\u0442\u0430.",
         "",
         "\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435:",
         "> cfg save <name> - \u0421\u043e\u0445\u0440\u0430\u043d\u044f\u0435\u0442 \u0442\u0435\u043a\u0443\u0449\u0443\u044e \u043a\u043e\u043d\u0444\u0438\u0433\u0443\u0440\u0430\u0446\u0438\u044e.",
         "> cfg load <name> - \u0417\u0430\u0433\u0440\u0443\u0436\u0430\u0435\u0442 \u043a\u043e\u043d\u0444\u0438\u0433\u0443\u0440\u0430\u0446\u0438\u044e.",
         "> cfg list - \u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0435\u0442 \u0432\u0441\u0435 \u0434\u043e\u0441\u0442\u0443\u043f\u043d\u044b\u0435 \u043a\u043e\u043d\u0444\u0438\u0433\u0438.",
         "> cfg remove <name> - \u0423\u0434\u0430\u043b\u044f\u0435\u0442 \u043a\u043e\u043d\u0444\u0438\u0433 \u043f\u043e \u0438\u043c\u0435\u043d\u0438."
      );
   }
}
