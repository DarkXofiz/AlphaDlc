package miranware.util.commands.api.manager;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.util.Pair;
import miranware.util.commands.api.ICommand;
import miranware.util.commands.api.argument.ICommandArgument;
import miranware.util.commands.api.registry.Registry;

public interface ICommandManager {
   Registry<ICommand> getRegistry();

   ICommand getCommand(String var1);

   boolean execute(String var1);

   boolean execute(Pair<String, List<ICommandArgument>> var1);

   Stream<String> tabComplete(Pair<String, List<ICommandArgument>> var1);

   Stream<String> tabComplete(String var1);
}
