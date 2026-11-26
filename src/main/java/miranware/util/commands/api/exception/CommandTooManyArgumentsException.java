package miranware.util.commands.api.exception;

public class CommandTooManyArgumentsException extends CommandErrorMessageException {
   public CommandTooManyArgumentsException(int maxArgs) {
      super(
         String.format(
            maxArgs == 1
               ? "Too many arguments, maximum %d"
               : (
                  maxArgs > 4
                     ? "Too many arguments, maximum %d"
                     : "Too many arguments, maximum %d"
               ),
            maxArgs
         )
      );
   }
}
