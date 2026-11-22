package zov.alphadlc.util.commands.api.exception;

public class CommandNotEnoughArgumentsException extends CommandErrorMessageException {
   public CommandNotEnoughArgumentsException(int minArgs) {
      super(
         String.format(
            "\u041d\u0435\u0434\u043e\u0441\u0442\u0430\u0442\u043e\u0447\u043d\u043e \u0430\u0440\u0433\u0443\u043c\u0435\u043d\u0442\u043e\u0432, \u043d\u0443\u0436\u0435\u043d %d \u0430\u0440\u0433\u0443\u043c\u0435\u043d\u0442",
            minArgs
         )
      );
   }
}
