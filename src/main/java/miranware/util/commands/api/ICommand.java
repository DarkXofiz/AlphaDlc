package miranware.util.commands.api;

import java.util.List;
import java.util.stream.Stream;
import miranware.util.QuickLogger;
import miranware.util.commands.api.argument.IArgConsumer;
import miranware.util.commands.api.exception.CommandException;

public interface ICommand extends QuickLogger {
   void execute(String var1, IArgConsumer var2) throws CommandException;

   Stream<String> tabComplete(String var1, IArgConsumer var2) throws CommandException;

   String getShortDesc();

   List<String> getLongDesc();

   List<String> getNames();

   default boolean hiddenFromHelp() {
      return false;
   }
}
