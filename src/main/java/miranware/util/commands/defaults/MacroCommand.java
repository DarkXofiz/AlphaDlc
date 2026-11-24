package miranware.util.commands.defaults;

import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.stream.Stream;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import miranware.MiranWare;
import miranware.util.commands.api.Command;
import miranware.util.commands.api.IBaritoneChatControl;
import miranware.util.commands.api.argument.IArgConsumer;
import miranware.util.commands.api.argument.ICommandArgument;
import miranware.util.commands.api.datatypes.KeyDataType;
import miranware.util.commands.api.exception.CommandException;
import miranware.util.commands.api.helpers.Paginator;
import miranware.util.commands.api.helpers.TabCompleteHelper;
import miranware.util.keyboard.KeyStorage;
import miranware.util.macro.MacroRepository;

public class MacroCommand extends Command {
   private final MacroRepository macroRepository;

   public MacroCommand(MiranWare onetap) {
      super("macro");
      this.macroRepository = onetap.getMacroRepository();
   }

   @Override
   public void execute(String label, IArgConsumer args) throws CommandException {
      String action = args.hasAny() ? args.getString().toLowerCase(Locale.US) : "list";
      switch (action) {
         case "add":
            this.handleAddMacro(args);
            break;
         case "remove":
            this.handleRemoveMacro(args);
            break;
         case "list":
            this.handleListMacros(args, label);
            break;
         case "clear":
            this.handleClearMacros(args);
            break;
         default:
            this.logDirect("Use: add / remove / list / clear", Formatting.GRAY);
      }
   }

   private void handleAddMacro(IArgConsumer args) throws CommandException {
      args.requireMin(2);
      int key = args.<Entry<String, Integer>, KeyDataType>getDatatypeFor(KeyDataType.INSTANCE).getValue();
      String command = args.rawRest();
      this.macroRepository.addMacro(command, key);
      this.logDirect(
         Formatting.GRAY
            + "Macro added: "
            + Formatting.WHITE
            + KeyStorage.getKey(key).toLowerCase()
            + Formatting.GRAY
            + " \u2192 "
            + Formatting.WHITE
            + command
      );
   }

   private void handleRemoveMacro(IArgConsumer args) throws CommandException {
      args.requireMin(1);
      int key = args.<Entry<String, Integer>, KeyDataType>getDatatypeFor(KeyDataType.INSTANCE).getValue();
      if (this.macroRepository.removeByKey(key)) {
         this.logDirect(
            Formatting.GRAY
               + "Macro on key "
               + Formatting.WHITE
               + KeyStorage.getKey(key).toLowerCase()
               + Formatting.GRAY
               + " removed"
         );
      } else {
         this.logDirect(
            "No macro on this key",
            Formatting.GRAY
         );
      }
   }

   private void handleListMacros(IArgConsumer args, String label) throws CommandException {
      Paginator.paginate(
         args,
         new Paginator<>(this.macroRepository.getMacroList()),
         () -> this.logDirect("Macro list:"),
         macro -> Text.literal(
               Formatting.GRAY
                  + " "
                  + KeyStorage.getKey(macro.key()).toLowerCase()
                  + Formatting.DARK_GRAY
                  + " \u2192 "
                  + Formatting.WHITE
                  + macro.message()
            ),
         IBaritoneChatControl.FORCE_COMMAND_PREFIX + label
      );
   }

   private void handleClearMacros(IArgConsumer args) {
      this.macroRepository.clearList();
      this.logDirect("All macros cleared", Formatting.GRAY);
   }

   @Override
   public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
      List<ICommandArgument> raw = args.getArgs();
      int size = raw.size();
      if (args.hasExactlyOne()) {
         return new TabCompleteHelper().prepend("add", "remove", "list", "clear").filterPrefix(args.getString()).sortAlphabetically().stream();
      } else {
         if (args.hasAny()) {
            String action = args.getString();
            if ((action.equalsIgnoreCase("add") || action.equalsIgnoreCase("remove")) && size == 2) {
               return args.tabCompleteDatatype(KeyDataType.INSTANCE);
            }
         }

         return Stream.empty();
      }
   }

   @Override
   public String getShortDesc() {
      return "Key macros";
   }

   @Override
   public List<String> getLongDesc() {
      return List.of(
         "Usage:",
         ".macro add <key> <command>",
         ".macro remove <key>",
         ".macro list",
         ".macro clear"
      );
   }
}
