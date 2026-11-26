package miranware.util.commands.api.exception;

import java.util.List;
import miranware.util.QuickLogger;
import miranware.util.commands.api.ICommand;
import miranware.util.commands.api.argument.ICommandArgument;

public class CommandNotFoundException extends CommandException implements QuickLogger {
   public final String command;

   public CommandNotFoundException(String command) {
      super(String.format("Command not found: %s", command));
      this.command = command;
   }

   @Override
   public void handle(ICommand command, List<ICommandArgument> args) {
      this.logDirect(this.getMessage());
   }
}
