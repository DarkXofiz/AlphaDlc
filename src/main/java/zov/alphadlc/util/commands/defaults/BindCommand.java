package zov.alphadlc.util.commands.defaults;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import zov.alphadlc.AlphaDLC;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleStorage;
import zov.alphadlc.util.commands.api.Command;
import zov.alphadlc.util.commands.api.IBaritoneChatControl;
import zov.alphadlc.util.commands.api.argument.IArgConsumer;
import zov.alphadlc.util.commands.api.argument.ICommandArgument;
import zov.alphadlc.util.commands.api.datatypes.KeyDataType;
import zov.alphadlc.util.commands.api.exception.CommandException;
import zov.alphadlc.util.commands.api.helpers.Paginator;
import zov.alphadlc.util.commands.api.helpers.TabCompleteHelper;
import zov.alphadlc.util.keyboard.KeyStorage;

public class BindCommand extends Command {
   private final ModuleStorage moduleStorage;

   public BindCommand(AlphaDLC onetap) {
      super("bind");
      this.moduleStorage = onetap.getModuleStorage();
   }

   @Override
   public void execute(String label, IArgConsumer args) throws CommandException {
      String action = args.hasAny() ? args.getString().toLowerCase(Locale.US) : "list";
      switch (action) {
         case "add":
            this.handleAddBind(label, args);
            break;
         case "remove":
            this.handleRemoveBind(args);
            break;
         case "list":
            this.handleListBinds(args, label);
            break;
         case "clear":
            this.handleClearBinds(args);
            break;
         default:
            this.logDirect(
               "\u041d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043d\u0430\u044f \u043f\u043e\u0434\u043a\u043e\u043c\u0430\u043d\u0434\u0430. \u0418\u0441\u043f\u043e\u043b\u044c\u0437\u0443\u0439 add/remove/list/clear.",
               Formatting.field_1080
            );
      }
   }

   private void handleAddBind(String label, IArgConsumer args) throws CommandException {
      args.requireMin(2);
      String moduleName = args.getString();
      Module module = this.moduleStorage.get(moduleName);
      if (module == null) {
         this.logDirect(
            Formatting.field_1080
               + "\u041c\u043e\u0434\u0443\u043b\u044c \u0441 \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435\u043c "
               + Formatting.field_1068
               + moduleName
               + Formatting.field_1080
               + " \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d"
         );
      } else {
         int key = args.<Entry<String, Integer>, KeyDataType>getDatatypeFor(KeyDataType.INSTANCE).getValue();
         module.setKey(key);
         this.logDirect(
            Formatting.field_1080
               + "\u041c\u043e\u0434\u0443\u043b\u044c "
               + Formatting.field_1068
               + module.getName()
               + Formatting.field_1080
               + " \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u043f\u0440\u0438\u0432\u044f\u0437\u0430\u043d \u043a \u043a\u043b\u0430\u0432\u0438\u0448\u0435 "
               + Formatting.field_1068
               + KeyStorage.getKey(key).toUpperCase()
         );
      }
   }

   private void handleRemoveBind(IArgConsumer args) throws CommandException {
      args.requireMin(1);
      String moduleName = args.getString();
      Module module = this.moduleStorage.get(moduleName);
      if (module == null) {
         this.logDirect(
            Formatting.field_1080
               + "\u041c\u043e\u0434\u0443\u043b\u044c \u0441 \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435\u043c "
               + Formatting.field_1068
               + moduleName
               + Formatting.field_1080
               + " \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d"
         );
      } else {
         module.setKey(-1);
         this.logDirect(
            Formatting.field_1080
               + "\u041c\u043e\u0434\u0443\u043b\u044c "
               + Formatting.field_1068
               + module.getName()
               + Formatting.field_1080
               + " \u0431\u043e\u043b\u044c\u0448\u0435 \u043d\u0435 \u0438\u043c\u0435\u0435\u0442 \u043f\u0440\u0438\u0432\u044f\u0437\u043a\u0443 \u043a \u043a\u043b\u0430\u0432\u0438\u0448\u0435"
         );
      }
   }

   private void handleListBinds(IArgConsumer args, String label) throws CommandException {
      args.requireMax(1);
      List<Module> boundModules = this.moduleStorage.getModules().stream().filter(module -> module.getKey() != -1).collect(Collectors.toList());
      Paginator.paginate(
         args,
         new Paginator<>(boundModules),
         () -> this.logDirect("\u041f\u0440\u0438\u0432\u044f\u0437\u0430\u043d\u043d\u044b\u0435 \u043c\u043e\u0434\u0443\u043b\u0438:"),
         module -> {
            int key = module.getKey();
            String keyName = KeyStorage.getKey(key).toUpperCase();
            return Text.method_43470(Formatting.field_1080 + module.getName() + ": " + Formatting.field_1068 + keyName);
         },
         IBaritoneChatControl.FORCE_COMMAND_PREFIX + label
      );
   }

   private void handleClearBinds(IArgConsumer args) throws CommandException {
      args.requireMax(1);

      for (Module m : this.moduleStorage.getModules()) {
         m.setKey(-1);
      }

      this.logDirect(
         Formatting.field_1080
            + "\u0412\u0441\u0435 \u043c\u043e\u0434\u0443\u043b\u0438 \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u043e\u0442\u0432\u044f\u0437\u0430\u043d\u044b \u0438 \u0431\u043e\u043b\u044c\u0448\u0435 \u043d\u0435 \u0438\u043c\u0435\u044e\u0442 \u043f\u0440\u0438\u0432\u044f\u0437\u043a\u0443 \u043a \u043a\u043b\u0430\u0432\u0438\u0448\u0435"
      );
   }

   @Override
   public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
      List<ICommandArgument> raw = args.getArgs();
      int size = raw.size();
      if (size <= 1) {
         String prefix = size == 1 ? args.peekString(0) : "";
         return new TabCompleteHelper().prepend("add", "remove", "list", "clear").filterPrefix(prefix).sortAlphabetically().stream();
      } else {
         String action = args.peekString(0).toLowerCase(Locale.ROOT);
         if (action.equals("add") && size == 2) {
            String modulePrefix = args.peekString(1);
            return this.moduleStorage
               .getModules()
               .stream()
               .map(Module::getName)
               .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(modulePrefix.toLowerCase(Locale.ROOT)))
               .sorted()
               .distinct();
         } else if (action.equals("add") && size == 3) {
            String keyPrefix = args.peekString(2);
            return KeyStorage.keyMap
               .keySet()
               .stream()
               .filter(k -> k.toLowerCase(Locale.ROOT).startsWith(keyPrefix.toLowerCase(Locale.ROOT)))
               .sorted()
               .distinct();
         } else if (action.equals("remove") && size == 2) {
            String modulePrefix = args.peekString(1);
            return this.moduleStorage
               .getModules()
               .stream()
               .filter(m -> m.getKey() != -1)
               .map(Module::getName)
               .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(modulePrefix.toLowerCase(Locale.ROOT)))
               .sorted()
               .distinct();
         } else {
            return Stream.empty();
         }
      }
   }

   @Override
   public String getShortDesc() {
      return "\u0423\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u0435 \u043f\u0440\u0438\u0432\u044f\u0437\u043a\u043e\u0439 \u043c\u043e\u0434\u0443\u043b\u044f \u043a \u043a\u043b\u0430\u0432\u0438\u0448\u0435";
   }

   @Override
   public List<String> getLongDesc() {
      return Arrays.asList(
         "\u041a\u043e\u043c\u0430\u043d\u0434\u0430 \u0434\u043b\u044f \u0443\u0441\u0442\u0430\u043d\u043e\u0432\u043a\u0438 \u0438 \u0443\u0434\u0430\u043b\u0435\u043d\u0438\u044f \u043f\u0440\u0438\u0432\u044f\u0437\u043a\u0438 \u043c\u043e\u0434\u0443\u043b\u044f \u043a \u043a\u043b\u0430\u0432\u0438\u0448\u0435",
         "",
         "\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435:",
         "> bind add <\u043c\u043e\u0434\u0443\u043b\u044c> <\u043a\u043b\u0430\u0432\u0438\u0448\u0430> \u2014 \u043f\u0440\u0438\u0432\u044f\u0437\u0430\u0442\u044c \u043c\u043e\u0434\u0443\u043b\u044c \u043a \u043a\u043b\u0430\u0432\u0438\u0448\u0435",
         "> bind remove <\u043c\u043e\u0434\u0443\u043b\u044c> \u2014 \u043e\u0442\u0432\u044f\u0437\u0430\u0442\u044c \u043c\u043e\u0434\u0443\u043b\u044c \u043e\u0442 \u043a\u043b\u0430\u0432\u0438\u0448\u0438",
         "> bind list \u2014 \u043f\u043e\u043a\u0430\u0437\u0430\u0442\u044c \u0432\u0441\u0435 \u043f\u0440\u0438\u0432\u044f\u0437\u0430\u043d\u043d\u044b\u0435 \u043c\u043e\u0434\u0443\u043b\u0438",
         "> bind clear \u2014 \u043e\u0442\u0432\u044f\u0437\u0430\u0442\u044c \u0432\u0441\u0435 \u043c\u043e\u0434\u0443\u043b\u0438 \u043e\u0442 \u0441\u0432\u043e\u0438\u0445 \u043a\u043b\u0430\u0432\u0438\u0448"
      );
   }
}
