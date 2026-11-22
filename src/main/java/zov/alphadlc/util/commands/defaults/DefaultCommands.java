package zov.alphadlc.util.commands.defaults;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import zov.alphadlc.AlphaDLC;
import zov.alphadlc.util.commands.api.ICommand;

public final class DefaultCommands {
   public static List<ICommand> createAll() {
      List<ICommand> commands = new ArrayList<>(
         Arrays.asList(
            new CfgCommand(),
            new RotationCommand(),
            new HelpCommand(AlphaDLC.getInstance()),
            new MacroCommand(AlphaDLC.getInstance()),
            new BindCommand(AlphaDLC.getInstance()),
            new FriendCommand(AlphaDLC.getInstance()),
            new StaffCommand(AlphaDLC.getInstance()),
            new VClipCommand(),
            new TpCommand(),
            new PartyCommand(),
            new GpsCommand(),
            new AICommand(),
            new BotCommand()
         )
      );
      return Collections.unmodifiableList(commands);
   }
}
