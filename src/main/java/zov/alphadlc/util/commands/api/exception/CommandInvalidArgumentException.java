package zov.alphadlc.util.commands.api.exception;

import zov.alphadlc.util.commands.api.argument.ICommandArgument;

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
         "\u041e\u0448\u0438\u0431\u043a\u0430 \u0432 %s \u0430\u0440\u0433\u0443\u043c\u0435\u043d\u0442\u0435: %s",
         arg.getIndex() == -1 ? "<unknown>" : Integer.toString(arg.getIndex() + 1),
         message
      );
   }
}
