package miranware.util.commands;

import com.google.common.eventbus.Subscribe;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.util.Pair;
import miranware.MiranWare;
import miranware.event.list.ChatEvent;
import miranware.event.list.TabCompleteEvent;
import miranware.util.commands.api.IBaritoneChatControl;
import miranware.util.commands.api.argument.ICommandArgument;
import miranware.util.commands.api.exception.CommandNotEnoughArgumentsException;
import miranware.util.commands.api.exception.CommandNotFoundException;
import miranware.util.commands.api.helpers.TabCompleteHelper;
import miranware.util.commands.api.manager.ICommandManager;
import miranware.util.commands.argument.ArgConsumer;
import miranware.util.commands.argument.CommandArguments;
import miranware.util.commands.manager.CommandRepository;

public class CommandDispatcher {
   private final ICommandManager manager = MiranWare.getInstance().getCommandRepository();

   public CommandDispatcher() {
      MiranWare.getInstance().getEventBus().register(this);
   }

   @Subscribe
   public void onSendChatMessage(ChatEvent event) {
      String msg = event.getMessage();
      String prefix = ".";
      boolean forceRun = msg.startsWith(IBaritoneChatControl.FORCE_COMMAND_PREFIX);
      if (msg.startsWith(prefix) || forceRun) {
         event.setCancelled(true);
         String commandStr = msg.substring(forceRun ? IBaritoneChatControl.FORCE_COMMAND_PREFIX.length() : prefix.length());
         if (!this.runCommand(commandStr) && !commandStr.trim().isEmpty()) {
            new CommandNotFoundException((String)CommandRepository.expand(commandStr).getLeft()).handle(null, null);
         }
      }
   }

   public boolean runCommand(String msg) {
      if (msg.isEmpty()) {
         return this.runCommand("help");
      } else {
         Pair<String, List<ICommandArgument>> pair = CommandRepository.expand(msg);
         return this.manager.execute(pair);
      }
   }

   @Subscribe
   public void onPreTabComplete(TabCompleteEvent event) {
      String prefix = event.getPrefix();
      String commandPrefix = ".";
      if (prefix.startsWith(commandPrefix)) {
         String msg = prefix.substring(commandPrefix.length());
         List<ICommandArgument> args = CommandArguments.from(msg, true);
         Stream<String> stream = this.tabComplete(msg);
         if (args.size() == 1) {
            stream = stream.map(x -> commandPrefix + x);
         }

         event.completions = stream.toArray(String[]::new);
      }
   }

   public Stream<String> tabComplete(String msg) {
      try {
         List<ICommandArgument> args = CommandArguments.from(msg, true);
         ArgConsumer argc = new ArgConsumer(this.manager, args);
         return argc.hasAtMost(2) && argc.hasExactly(1)
            ? new TabCompleteHelper().addCommands(this.manager).filterPrefix(argc.getString()).stream()
            : this.manager.tabComplete(msg);
      } catch (CommandNotEnoughArgumentsException var4) {
         return Stream.empty();
      }
   }
}
