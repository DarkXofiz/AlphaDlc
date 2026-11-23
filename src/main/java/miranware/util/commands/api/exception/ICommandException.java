package miranware.util.commands.api.exception;

import java.util.List;
import net.minecraft.util.Formatting;
import miranware.util.QuickLogger;
import miranware.util.commands.api.ICommand;
import miranware.util.commands.api.argument.ICommandArgument;

public interface ICommandException extends QuickLogger {
   String getMessage();

   default void handle(ICommand command, List<ICommandArgument> args) {
      this.logDirect(this.getMessage(), Formatting.RED);
   }
}
