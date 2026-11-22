package zov.alphadlc.util.commands.defaults;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import zov.alphadlc.util.bot.BotSessionManager;
import zov.alphadlc.util.commands.api.Command;
import zov.alphadlc.util.commands.api.argument.IArgConsumer;
import zov.alphadlc.util.commands.api.exception.CommandException;
import zov.alphadlc.util.commands.api.helpers.TabCompleteHelper;

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
               "\u041d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043d\u0430\u044f \u043f\u043e\u0434\u043a\u043e\u043c\u0430\u043d\u0434\u0430. \u0418\u0441\u043f\u043e\u043b\u044c\u0437\u0443\u0439 connect/remove/return/control/say/sayall/list"
            );
      }
   }

   private void connect(IArgConsumer args) throws CommandException {
      args.requireMin(2);
      String name = args.getString();
      String ip = args.getString();
      BotSessionManager.connect(name, ip);
      this.logDirect(
         "\u041f\u043e\u0434\u043a\u043b\u044e\u0447\u0435\u043d\u0438\u0435 \u0432\u044b\u043f\u043e\u043b\u043d\u0435\u043d\u043e: "
            + name
            + " -> "
            + ip
            + " (\u041f\u0440\u0435\u0434\u044b\u0434\u0443\u0449\u0430\u044f \u0441\u0435\u0441\u0441\u0438\u044f \u0437\u0430\u043c\u043e\u0440\u043e\u0436\u0435\u043d\u0430)"
      );
   }

   private void remove(IArgConsumer args) throws CommandException {
      args.requireMin(1);
      String name = args.getString();
      if (BotSessionManager.remove(name)) {
         this.logDirect("\u0411\u043e\u0442 \u043e\u0442\u043a\u043b\u044e\u0447\u0435\u043d \u0438 \u0443\u0434\u0430\u043b\u0435\u043d: " + name);
      } else {
         this.logDirect("\u0411\u043e\u0442 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d: " + name);
      }
   }

   private void control(IArgConsumer args) throws CommandException {
      args.requireMin(1);
      String name = args.getString();
      if (BotSessionManager.control(name)) {
         this.logDirect("\u041f\u0435\u0440\u0435\u043a\u043b\u044e\u0447\u0430\u044e\u0441\u044c \u043d\u0430 \u0431\u043e\u0442\u0430: " + name);
      } else {
         this.logDirect("\u0411\u043e\u0442 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d: " + name);
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
            "\u0421\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u0435 \u043e\u0442 \u043b\u0438\u0446\u0430 "
               + name
               + " \u043e\u0442\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u043e."
         );
      } else {
         this.logDirect("\u0411\u043e\u0442 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d: " + name);
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
         "\u0421\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u0435 \u043e\u0442\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u043e \u043e\u0442 \u0432\u0441\u0435\u0445 \u0431\u043e\u0442\u043e\u0432."
      );
   }

   private void restore() {
      if (BotSessionManager.restore()) {
         this.logDirect(
            "\u0412\u043e\u0437\u0432\u0440\u0430\u0449\u0430\u044e \u043f\u0440\u043e\u0448\u043b\u0443\u044e \u0441\u0435\u0441\u0441\u0438\u044e"
         );
      } else {
         this.logDirect(
            "\u041d\u0435\u0442 \u0441\u043e\u0445\u0440\u0430\u043d\u0435\u043d\u043d\u043e\u0439 \u0441\u0435\u0441\u0441\u0438\u0438 \u0434\u043b\u044f \u0432\u043e\u0437\u0432\u0440\u0430\u0442\u0430"
         );
      }
   }

   private void list() {
      List<BotSessionManager.BotConnection> connections = BotSessionManager.getConnections();
      if (connections.isEmpty()) {
         this.logDirect("\u0421\u043f\u0438\u0441\u043e\u043a \u0431\u043e\u0442\u043e\u0432 \u043f\u0443\u0441\u0442");
      } else {
         this.logDirect("\u041f\u043e\u0434\u043a\u043b\u044e\u0447\u0435\u043d\u043d\u044b\u0435 \u0431\u043e\u0442\u044b:");

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
               return Stream.of("<\u043d\u0438\u043a_\u0431\u043e\u0442\u0430>");
            }

            if (action.equals("sayall")) {
               return Stream.of("<\u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u0435>");
            }
         }

         return args.hasExactly(3) && args.peekString(0).equalsIgnoreCase("connect")
            ? Stream.of("<\u0430\u0439\u043f\u0438_\u0441\u0435\u0440\u0432\u0435\u0440\u0430>")
            : Stream.empty();
      }
   }

   @Override
   public String getShortDesc() {
      return "\u0423\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u0435 \u0431\u043e\u0442-\u0441\u0435\u0441\u0441\u0438\u044f\u043c\u0438";
   }

   @Override
   public List<String> getLongDesc() {
      return List.of(
         "\u0423\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u0435 \u0431\u043e\u0442-\u0441\u0435\u0441\u0441\u0438\u044f\u043c\u0438 \u0438 \u0431\u044b\u0441\u0442\u0440\u044b\u043c \u043f\u0435\u0440\u0435\u043a\u043b\u044e\u0447\u0435\u043d\u0438\u0435\u043c.",
         "",
         "\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435:",
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
