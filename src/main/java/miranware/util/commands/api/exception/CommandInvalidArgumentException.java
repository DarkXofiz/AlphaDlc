package miranware.util.commands.api.exception;

import miranware.util.commands.api.argument.ICommandArgument;

public abstract class CommandInvalidArgumentException extends CommandErrorMessageException {
   public final ICommandArgument arg;

   protected CommandInvalidArgumentException(ICommandArgument arg, String message) {
      super(formatMessage(arg, message));
      this.arg = arg;
   }

   protected CommandInvalidArgumentException(ICommandArgument arg, String message, Throwable cause) {
      super(formatMessage(arg, message), cause);
      this.arg = arg;
   }

   private static String formatMessage(ICommandArgument arg, String message) {
      return String.format(
         "Error in argument %s: %s",
         arg.getIndex() == -1 ? "<unknown>" : Integer.toString(arg.getIndex() + 1),
         message
      );
   }
}
