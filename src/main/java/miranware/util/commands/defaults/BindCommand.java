package miranware.util.commands.defaults;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import miranware.MiranWare;
import miranware.module.Module;
import miranware.module.ModuleStorage;
import miranware.util.commands.api.Command;
import miranware.util.commands.api.IBaritoneChatControl;
import miranware.util.commands.api.argument.IArgConsumer;
import miranware.util.commands.api.argument.ICommandArgument;
import miranware.util.commands.api.datatypes.KeyDataType;
import miranware.util.commands.api.exception.CommandException;
import miranware.util.commands.api.helpers.Paginator;
import miranware.util.commands.api.helpers.TabCompleteHelper;
import miranware.util.keyboard.KeyStorage;

public class BindCommand extends Command {
   private final ModuleStorage moduleStorage;

   public BindCommand(MiranWare onetap) {
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
               "Unknown subcommand. Use add/remove/list/clear.",
               Formatting.GRAY
            );
      }
   }

   private void handleAddBind(String label, IArgConsumer args) throws CommandException {
      args.requireMin(2);
      String moduleName = args.getString();
      Module module = this.moduleStorage.get(moduleName);
      if (module == null) {
         this.logDirect(
            Formatting.GRAY
               + "Module named "
               + Formatting.WHITE
               + moduleName
               + Formatting.GRAY
               + " not found"
         );
      } else {
         int key = args.<Entry<String, Integer>, KeyDataType>getDatatypeFor(KeyDataType.INSTANCE).getValue();
         module.setKey(key);
         this.logDirect(
            Formatting.GRAY
               + "Module "
               + Formatting.WHITE
               + module.getName()
               + Formatting.GRAY
               + " successfully bound to key "
               + Formatting.WHITE
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
            Formatting.GRAY
               + "Module named "
               + Formatting.WHITE
               + moduleName
               + Formatting.GRAY
               + " not found"
         );
      } else {
         module.setKey(-1);
         this.logDirect(
            Formatting.GRAY
               + "Module "
               + Formatting.WHITE
               + module.getName()
               + Formatting.GRAY
               + " is no longer bound to a key"
         );
      }
   }

   private void handleListBinds(IArgConsumer args, String label) throws CommandException {
      args.requireMax(1);
      List<Module> boundModules = this.moduleStorage.getModules().stream().filter(module -> module.getKey() != -1).collect(Collectors.toList());
      Paginator.paginate(
         args,
         new Paginator<>(boundModules),
         () -> this.logDirect("Bound modules:"),
         module -> {
            int key = module.getKey();
            String keyName = KeyStorage.getKey(key).toUpperCase();
            return Text.literal(Formatting.GRAY + module.getName() + ": " + Formatting.WHITE + keyName);
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
         Formatting.GRAY
            + "All modules were unbound and no longer have a key binding"
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
      return "Module key binding management";
   }

   @Override
   public List<String> getLongDesc() {
      return Arrays.asList(
         "Command for setting and removing module key bindings",
         "",
         "Usage:",
         "> bind add <module> <key> \u2014 bind a module to a key",
         "> bind remove <module> \u2014 unbind a module from its key",
         "> bind list \u2014 show all bound modules",
         "> bind clear \u2014 unbind all modules from their keys"
      );
   }
}
