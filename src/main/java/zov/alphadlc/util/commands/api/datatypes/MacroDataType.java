package zov.alphadlc.util.commands.api.datatypes;

import java.util.List;
import java.util.stream.Stream;
import zov.alphadlc.AlphaDLC;
import zov.alphadlc.util.commands.api.exception.CommandException;
import zov.alphadlc.util.commands.api.helpers.TabCompleteHelper;
import zov.alphadlc.util.keyboard.KeyStorage;
import zov.alphadlc.util.macro.Macro;

public enum MacroDataType implements IDatatypeFor<Macro> {
   INSTANCE;

   @Override
   public Stream<String> tabComplete(IDatatypeContext datatypeContext) throws CommandException {
      Stream<String> macros = this.getMacro().stream().map(macro -> KeyStorage.getKey(macro.key()));
      String context = datatypeContext.getConsumer().getString();
      return new TabCompleteHelper().append(macros).filterPrefix(context).sortAlphabetically().stream();
   }

   public Macro get(IDatatypeContext datatypeContext) throws CommandException {
      String username = datatypeContext.getConsumer().getString();
      return this.getMacro().stream().filter(s -> KeyStorage.getKey(s.key()).equalsIgnoreCase(username)).findFirst().orElse(null);
   }

   private List<? extends Macro> getMacro() {
      return AlphaDLC.getInstance().getMacroRepository().getMacroList();
   }
}
