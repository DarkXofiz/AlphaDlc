package miranware.util.commands.defaults;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import miranware.util.bot.BotSessionManager;
import miranware.util.commands.api.Command;
import miranware.util.commands.api.argument.IArgConsumer;
import miranware.util.commands.api.exception.CommandException;
import miranware.util.commands.api.helpers.TabCompleteHelper;

public class BotCommand extends Command {
   public BotCommand() {
      super("bot");
   }

   @Override
   public void execute(String label, IArgConsumer args) throws CommandException {
      String action = args.hasAny() ? args.getString().toLowerCase(Locale.ROOT) : "list";
      switch (action) {
         case "connect":
            this.connect(args);
            break;
         case "remove":
            this.remove(args);
            break;
         case "return":
            this.restore();
            break;
         case "control":
            this.control(args);
            break;
         case "say":
            this.say(args);
            break;
         case "sayall":
            this.sayAll(args);
            break;
         case "list":
            this.list();
            break;
         default:
            this.logDirect(
               "Unknown subcommand. Use connect/remove/return/control/say/sayall/list"
            );
      }
   }

   private void connect(IArgConsumer args) throws CommandException {
      args.requireMin(2);
      String name = args.getString();
      String ip = args.getString();
      BotSessionManager.connect(name, ip);
      this.logDirect(
         "Connected: "
            + name
            + " -> "
            + ip
            + " (Previous session frozen)"
      );
   }

   private void remove(IArgConsumer args) throws CommandException {
      args.requireMin(1);
      String name = args.getString();
      if (BotSessionManager.remove(name)) {
         this.logDirect("Bot disconnected and removed: " + name);
      } else {
         this.logDirect("Bot not found: " + name);
      }
   }

   private void control(IArgConsumer args) throws CommandException {
      args.requireMin(1);
      String name = args.getString();
      if (BotSessionManager.control(name)) {
         this.logDirect("Switching to bot: " + name);
      } else {
         this.logDirect("Bot not found: " + name);
      }
   }

   private void say(IArgConsumer args) throws CommandException {
      args.requireMin(2);
      String name = args.getString();
      StringBuilder message = new StringBuilder();

      while (args.hasAny()) {
         message.append(args.getString()).append(" ");
      }

      if (BotSessionManager.say(name, message.toString().trim())) {
         this.logDirect(
            "Message sent as "
               + name
               + " sent."
         );
      } else {
         this.logDirect("Bot not found: " + name);
      }
   }

   private void sayAll(IArgConsumer args) throws CommandException {
      args.requireMin(1);
      StringBuilder message = new StringBuilder();

      while (args.hasAny()) {
         message.append(args.getString()).append(" ");
      }

      BotSessionManager.sayAll(message.toString().trim());
      this.logDirect(
         "Message sent from all bots."
      );
   }

   private void restore() {
      if (BotSessionManager.restore()) {
         this.logDirect(
            "Returning to the previous session"
         );
      } else {
         this.logDirect(
            "No saved session to return to"
         );
      }
   }

   private void list() {
      List<BotSessionManager.BotConnection> connections = BotSessionManager.getConnections();
      if (connections.isEmpty()) {
         this.logDirect("The bot list is empty");
      } else {
         this.logDirect("Connected bots:");

         for (BotSessionManager.BotConnection connection : connections) {
            this.logDirect("- " + connection.name() + " @ " + connection.address());
         }
      }
   }

   @Override
   public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
      if (args.hasExactlyOne()) {
         return new TabCompleteHelper().prepend("connect", "remove", "return", "control", "say", "sayall", "list").filterPrefix(args.getString()).stream();
      } else {
         if (args.hasExactly(2)) {
            String action = args.peekString(0).toLowerCase(Locale.ROOT);
            if (List.of("remove", "control", "say").contains(action)) {
               String searchName = args.peekString(1).toLowerCase(Locale.ROOT);
               return BotSessionManager.getConnections()
                  .stream()
                  .map(BotSessionManager.BotConnection::name)
                  .filter(n -> n.toLowerCase().startsWith(searchName));
            }

            if (action.equals("connect")) {
               return Stream.of("<bot_name>");
            }

            if (action.equals("sayall")) {
               return Stream.of("<message>");
            }
         }

         return args.hasExactly(3) && args.peekString(0).equalsIgnoreCase("connect")
            ? Stream.of("<server_ip>")
            : Stream.empty();
      }
   }

   @Override
   public String getShortDesc() {
      return "Bot session management";
   }

   @Override
   public List<String> getLongDesc() {
      return List.of(
         "Bot session management and quick switching.",
         "",
         "Usage:",
         "> bot connect <name> <ip>",
         "> bot control <name>",
         "> bot say <name> <message>",
         "> bot sayall <message>",
         "> bot remove <name>",
         "> bot return",
         "> bot list"
      );
   }
}
