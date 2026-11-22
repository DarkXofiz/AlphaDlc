package zov.alphadlc.util.commands.defaults;

import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.stream.Stream;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import zov.alphadlc.AlphaDLC;
import zov.alphadlc.util.commands.api.Command;
import zov.alphadlc.util.commands.api.IBaritoneChatControl;
import zov.alphadlc.util.commands.api.argument.IArgConsumer;
import zov.alphadlc.util.commands.api.argument.ICommandArgument;
import zov.alphadlc.util.commands.api.datatypes.KeyDataType;
import zov.alphadlc.util.commands.api.exception.CommandException;
import zov.alphadlc.util.commands.api.helpers.Paginator;
import zov.alphadlc.util.commands.api.helpers.TabCompleteHelper;
import zov.alphadlc.util.keyboard.KeyStorage;
import zov.alphadlc.util.macro.MacroRepository;

public class MacroCommand extends Command {
   private final MacroRepository macroRepository;

   public MacroCommand(AlphaDLC onetap) {
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
            this.logDirect("\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u0443\u0439: add / remove / list / clear", Formatting.field_1080);
      }
   }

   private void handleAddMacro(IArgConsumer args) throws CommandException {
      args.requireMin(2);
      int key = args.<Entry<String, Integer>, KeyDataType>getDatatypeFor(KeyDataType.INSTANCE).getValue();
      String command = args.rawRest();
      this.macroRepository.addMacro(command, key);
      this.logDirect(
         Formatting.field_1080
            + "\u041c\u0430\u043a\u0440\u043e\u0441 \u0434\u043e\u0431\u0430\u0432\u043b\u0435\u043d: "
            + Formatting.field_1068
            + KeyStorage.getKey(key).toLowerCase()
            + Formatting.field_1080
            + " \u2192 "
            + Formatting.field_1068
            + command
      );
   }

   private void handleRemoveMacro(IArgConsumer args) throws CommandException {
      args.requireMin(1);
      int key = args.<Entry<String, Integer>, KeyDataType>getDatatypeFor(KeyDataType.INSTANCE).getValue();
      if (this.macroRepository.removeByKey(key)) {
         this.logDirect(
            Formatting.field_1080
               + "\u041c\u0430\u043a\u0440\u043e\u0441 \u043d\u0430 \u043a\u043b\u0430\u0432\u0438\u0448\u0435 "
               + Formatting.field_1068
               + KeyStorage.getKey(key).toLowerCase()
               + Formatting.field_1080
               + " \u0443\u0434\u0430\u043b\u0451\u043d"
         );
      } else {
         this.logDirect(
            "\u041c\u0430\u043a\u0440\u043e\u0441 \u043d\u0430 \u044d\u0442\u043e\u0439 \u043a\u043b\u0430\u0432\u0438\u0448\u0435 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d",
            Formatting.field_1080
         );
      }
   }

   private void handleListMacros(IArgConsumer args, String label) throws CommandException {
      Paginator.paginate(
         args,
         new Paginator<>(this.macroRepository.getMacroList()),
         () -> this.logDirect("\u0421\u043f\u0438\u0441\u043e\u043a \u043c\u0430\u043a\u0440\u043e\u0441\u043e\u0432:"),
         macro -> Text.method_43470(
               Formatting.field_1080
                  + " "
                  + KeyStorage.getKey(macro.key()).toLowerCase()
                  + Formatting.field_1063
                  + " \u2192 "
                  + Formatting.field_1068
                  + macro.message()
            ),
         IBaritoneChatControl.FORCE_COMMAND_PREFIX + label
      );
   }

   private void handleClearMacros(IArgConsumer args) {
      this.macroRepository.clearList();
      this.logDirect("\u0412\u0441\u0435 \u043c\u0430\u043a\u0440\u043e\u0441\u044b \u043e\u0447\u0438\u0449\u0435\u043d\u044b", Formatting.field_1080);
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
      return "\u041c\u0430\u043a\u0440\u043e\u0441\u044b \u043f\u043e \u043a\u043b\u0430\u0432\u0438\u0448\u0430\u043c";
   }

   @Override
   public List<String> getLongDesc() {
      return List.of(
         "\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435:",
         ".macro add <key> <command>",
         ".macro remove <key>",
         ".macro list",
         ".macro clear"
      );
   }
}
