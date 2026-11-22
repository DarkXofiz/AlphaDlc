package zov.alphadlc.util.commands;

import com.google.common.eventbus.Subscribe;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.util.Pair;
import zov.alphadlc.AlphaDLC;
import zov.alphadlc.event.list.ChatEvent;
import zov.alphadlc.event.list.TabCompleteEvent;
import zov.alphadlc.util.commands.api.IBaritoneChatControl;
import zov.alphadlc.util.commands.api.argument.ICommandArgument;
import zov.alphadlc.util.commands.api.exception.CommandNotEnoughArgumentsException;
import zov.alphadlc.util.commands.api.exception.CommandNotFoundException;
import zov.alphadlc.util.commands.api.helpers.TabCompleteHelper;
import zov.alphadlc.util.commands.api.manager.ICommandManager;
import zov.alphadlc.util.commands.argument.ArgConsumer;
import zov.alphadlc.util.commands.argument.CommandArguments;
import zov.alphadlc.util.commands.manager.CommandRepository;

public class CommandDispatcher {
   private final ICommandManager manager = AlphaDLC.getInstance().getCommandRepository();

   public CommandDispatcher() {
      AlphaDLC.getInstance().getEventBus().register(this);
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
            new CommandNotFoundException((String)CommandRepository.expand(commandStr).method_15442()).handle(null, null);
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
