package miranware.util.commands.defaults;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import miranware.MiranWare;
import miranware.util.commands.api.ICommand;

public final class DefaultCommands {
   public static List<ICommand> createAll() {
      List<ICommand> commands = new ArrayList<>(
         Arrays.asList(
            new CfgCommand(),
            new RotationCommand(),
            new HelpCommand(MiranWare.getInstance()),
            new MacroCommand(MiranWare.getInstance()),
            new BindCommand(MiranWare.getInstance()),
            new FriendCommand(MiranWare.getInstance()),
            new StaffCommand(MiranWare.getInstance()),
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
