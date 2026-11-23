package miranware.util.commands.defaults;

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
import miranware.util.commands.api.Command;
import miranware.util.commands.api.IBaritoneChatControl;
import miranware.util.commands.api.argument.IArgConsumer;
import miranware.util.commands.api.exception.CommandException;
import miranware.util.commands.api.exception.CommandNotEnoughArgumentsException;
import miranware.util.commands.api.helpers.Paginator;
import miranware.util.commands.api.helpers.TabCompleteHelper;
import miranware.util.config.ConfigManager;

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
               Path file = Paths.get("miranware/configs").resolve(name + ".json");
               if (Files.exists(file)) {
                  try {
                     Files.delete(file);
                  } catch (IOException var12) {
                     this.logDirect(
                        Formatting.GRAY
                           + "Error while deleting the file."
                     );
                     var12.printStackTrace();
                  }
               }
            }

            this.logDirect(
               "Config list cleared",
               Formatting.GRAY
            );
            break;
         case "dir":
            try {
               File dir = new File("miranware/configs/");
               if (!dir.exists()) {
                  this.logDirect(Formatting.GRAY + "Why did you delete the folder?");
                  dir.mkdirs();
               } else {
                  this.logDirect(
                     Formatting.GRAY
                        + "Opening the configs folder..."
                  );
               }

               Runtime.getRuntime().exec("explorer " + dir.getAbsolutePath());
            } catch (IOException var11) {
               this.logDirect(
                  Formatting.GRAY
                     + "Error while opening the folder: "
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
               "Unknown subcommand. Use load/save/remove/list/dir.",
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
            + "Config named "
            + Formatting.WHITE
            + name
            + Formatting.GRAY
            + " saved successfully"
      );
   }

   private void handleLoad(IArgConsumer args) throws CommandException {
      args.requireExactly(1);
      String name = args.getString();
      if (!ConfigManager.getConfigs().contains(name)) {
         this.logDirect(
            Formatting.GRAY
               + "No config with that name was found"
         );
      } else {
         ConfigManager.load(name);
         this.logDirect(
            Formatting.GRAY
               + "Config named "
               + Formatting.WHITE
               + name
               + Formatting.GRAY
               + " loaded successfully"
         );
      }
   }

   private void handleList(IArgConsumer args, String label) throws CommandException {
      args.requireMax(1);
      List<String> configs = ConfigManager.getConfigs();
      this.logDirect("Config list:", Formatting.GRAY);
      Paginator.paginate(
         args,
         new Paginator<>(configs),
         name -> {
            Text nameText = Text.literal(Formatting.GRAY + "- " + Formatting.WHITE + name + " ");
            Text loadText = Text.literal(Formatting.GREEN + "[Load]")
               .styled(
                  style -> style.withClickEvent(new ClickEvent(Action.RUN_COMMAND, IBaritoneChatControl.FORCE_COMMAND_PREFIX + "cfg load " + name))
                        .withHoverEvent(new HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, Text.literal("Click to load config")))
               );
            Text deleteText = Text.literal(Formatting.RED + " [Delete]")
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
      Path file = Paths.get("miranware/configs").resolve(name + ".json");
      if (Files.exists(file)) {
         try {
            Files.delete(file);
            this.logDirect(
               Formatting.GRAY
                  + "Config "
                  + Formatting.WHITE
                  + name
                  + Formatting.GRAY
                  + " deleted successfully"
            );
         } catch (IOException var5) {
            this.logDirect(
               Formatting.GRAY
                  + "Error while deleting the file."
            );
            var5.printStackTrace();
         }
      } else {
         this.logDirect(Formatting.GRAY + "Config not found");
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
      return "Config management";
   }

   @Override
   public List<String> getLongDesc() {
      return Arrays.asList(
         "Command for managing client configs.",
         "",
         "Usage:",
         "> cfg save <name> - Saves the current config.",
         "> cfg load <name> - Loads a config.",
         "> cfg list - Shows all available configs.",
         "> cfg remove <name> - Deletes a config by name."
      );
   }
}
