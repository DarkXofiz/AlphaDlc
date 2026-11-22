package zov.alphadlc.util.commands.api.exception;

import java.util.List;
import zov.alphadlc.util.QuickLogger;
import zov.alphadlc.util.commands.api.ICommand;
import zov.alphadlc.util.commands.api.argument.ICommandArgument;

public class CommandNotFoundException extends CommandException implements QuickLogger {
   public final String command;

   public CommandNotFoundException(String command) {
      super(String.format("\u041a\u043e\u043c\u0430\u043d\u0434\u0430 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u0430: %s", command));
      this.command = command;
   }

   @Override
   public void handle(ICommand command, List<ICommandArgument> args) {
      this.logDirect(this.getMessage());
   }
}
