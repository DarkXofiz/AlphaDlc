package miranware.util.commands.api.exception;

public class CommandNotEnoughArgumentsException extends CommandErrorMessageException {
   public CommandNotEnoughArgumentsException(int minArgs) {
      super(
         String.format(
            "Not enough arguments, %d required",
            minArgs
         )
      );
   }
}
