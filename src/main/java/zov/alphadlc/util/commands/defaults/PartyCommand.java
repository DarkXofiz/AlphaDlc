package zov.alphadlc.util.commands.defaults;

import com.google.gson.JsonObject;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import net.minecraft.util.Formatting;
import zov.alphadlc.util.IMinecraft;
import zov.alphadlc.util.chat.ChatUtil;
import zov.alphadlc.util.commands.api.Command;
import zov.alphadlc.util.commands.api.argument.IArgConsumer;
import zov.alphadlc.util.commands.api.exception.CommandException;
import zov.alphadlc.util.commands.api.exception.CommandNotEnoughArgumentsException;
import zov.alphadlc.util.commands.api.helpers.TabCompleteHelper;
import zov.alphadlc.util.party.connection.PartyApiClient;

public class PartyCommand extends Command implements IMinecraft {
   public PartyCommand() {
      super("party");
   }

   @Override
   public void execute(String label, IArgConsumer args) throws CommandException {
      String action = args.hasAny() ? args.getString().toLowerCase(Locale.ROOT) : "help";
      switch (action) {
         case "create":
            this.handleCreate(args);
            break;
         case "invite":
            this.handleInvite(args);
            break;
         case "join":
            this.handleJoin(args);
            break;
         case "leave":
            this.handleLeave();
            break;
         case "disband":
            this.handleDisband();
            break;
         case "list":
            this.handleList();
            break;
         case "kick":
            this.handleKick(args);
            break;
         default:
            this.logDirect("\u041d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043d\u0430\u044f \u043f\u043e\u0434\u043a\u043e\u043c\u0430\u043d\u0434\u0430");
      }
   }

   private String nick() {
      return mc.player.getNameForScoreboard();
   }

   private void handleCreate(IArgConsumer args) throws CommandException {
      args.requireMin(1);
      String name = args.getString();
      JsonObject req = new JsonObject();
      req.addProperty("leader", this.nick());
      req.addProperty("name", name);
      PartyApiClient.postAsync(
         "/party/create",
         req,
         json -> ChatUtil.send(
               Formatting.GRAY
                  + "\u0412\u044b \u0441\u043e\u0437\u0434\u0430\u043b\u0438 \u043f\u0430\u0442\u0438 \u0441 \u043d\u0430\u0437\u0432\u0430\u043d\u0438\u0435\u043c "
                  + Formatting.WHITE
                  + name
            )
      );
   }

   private void handleInvite(IArgConsumer args) throws CommandException {
      args.requireMin(1);
      String target = args.getString();
      JsonObject req = new JsonObject();
      req.addProperty("leader", this.nick());
      req.addProperty("target", target);
      PartyApiClient.postAsync(
         "/party/invite",
         req,
         json -> ChatUtil.send(
               Formatting.GRAY
                  + "\u0412\u044b \u043f\u0440\u0438\u0433\u043b\u0430\u0441\u0438\u043b\u0438 \u0438\u0433\u0440\u043e\u043a\u0430 "
                  + Formatting.WHITE
                  + target
            )
      );
   }

   private void handleJoin(IArgConsumer args) throws CommandException {
      args.requireMin(1);
      String party = args.getString();
      JsonObject req = new JsonObject();
      req.addProperty("player", this.nick());
      req.addProperty("party", party);
      PartyApiClient.postAsync(
         "/party/join",
         req,
         json -> ChatUtil.send(
               Formatting.GRAY + "\u0412\u044b \u0432\u043e\u0448\u043b\u0438 \u0432 \u043f\u0430\u0442\u0438 " + Formatting.WHITE + party
            )
      );
   }

   private void handleLeave() {
      JsonObject req = new JsonObject();
      req.addProperty("player", this.nick());
      PartyApiClient.postAsync(
         "/party/leave",
         req,
         json -> ChatUtil.send(Formatting.GRAY + "\u0412\u044b \u043f\u043e\u043a\u0438\u043d\u0443\u043b\u0438 \u043f\u0430\u0442\u0438")
      );
   }

   private void handleDisband() {
      JsonObject req = new JsonObject();
      req.addProperty("leader", this.nick());
      PartyApiClient.postAsync(
         "/party/disband",
         req,
         json -> ChatUtil.send(Formatting.GRAY + "\u0412\u044b \u0440\u0430\u0441\u043f\u0443\u0441\u0442\u0438\u043b\u0438 \u043f\u0430\u0442\u0438")
      );
   }

   private void handleKick(IArgConsumer args) throws CommandException {
      args.requireMin(1);
      String target = args.getString();
      JsonObject req = new JsonObject();
      req.addProperty("leader", this.nick());
      req.addProperty("target", target);
      PartyApiClient.postAsync(
         "/party/kick",
         req,
         json -> ChatUtil.send(
               Formatting.GRAY
                  + "\u0412\u044b \u043a\u0438\u043a\u043d\u0443\u043b\u0438 \u0438\u0433\u0440\u043e\u043a\u0430 "
                  + Formatting.WHITE
                  + target
            )
      );
   }

   private void handleList() {
      JsonObject req = new JsonObject();
      req.addProperty("player", this.nick());
      PartyApiClient.postAsync("/party/list", req, json -> {
         String[] members = json.get("message").getAsString().split(", ");
         ChatUtil.send(Formatting.GRAY + "\u0418\u0433\u0440\u043e\u043a\u0438 \u0432 \u043f\u0430\u0442\u0438:");

         for (String m : members) {
            ChatUtil.send(" " + Formatting.WHITE + m);
         }
      });
   }

   @Override
   public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandNotEnoughArgumentsException {
      int size = args.getArgs().size();
      if (size <= 1) {
         String prefix = size == 1 ? args.peekString(0) : "";
         return new TabCompleteHelper()
            .prepend("create", "invite", "join", "leave", "disband", "list", "kick")
            .filterPrefix(prefix)
            .sortAlphabetically()
            .stream();
      } else {
         return Stream.empty();
      }
   }

   @Override
   public String getShortDesc() {
      return "\u041f\u043e\u0437\u0432\u043e\u043b\u044f\u0435\u0442 \u0432\u0438\u0434\u0435\u0442\u044c \u0438\u0433\u0440\u043e\u043a\u043e\u0432 \u0438\u0437 \u043f\u0430\u0442\u0438 \u043d\u0430 \u043b\u044e\u0431\u043e\u0439 \u0434\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u0438";
   }

   @Override
   public List<String> getLongDesc() {
      return List.of(
         "\u041a\u043e\u043c\u0430\u043d\u0434\u0430 \u0434\u043b\u044f \u0443\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u044f \u043f\u0430\u0442\u0438 \u0441\u0438\u0441\u0442\u0435\u043c\u043e\u0439",
         "",
         "\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435:",
         "> party create <name> - \u0421\u043e\u0437\u0434\u0430\u0442\u044c \u043f\u0430\u0442\u0438",
         "> party invite <player> - \u041f\u0440\u0438\u0433\u043b\u0430\u0441\u0438\u0442\u044c \u0438\u0433\u0440\u043e\u043a\u0430 (\u043b\u0438\u0434\u0435\u0440)",
         "> party join <name> - \u0412\u043e\u0439\u0442\u0438 \u0432 \u043f\u0430\u0442\u0438",
         "> party leave - \u0412\u044b\u0439\u0442\u0438 \u0438\u0437 \u043f\u0430\u0442\u0438",
         "> party disband - \u0420\u0430\u0441\u043f\u0443\u0441\u0442\u0438\u0442\u044c \u043f\u0430\u0442\u0438 (\u043b\u0438\u0434\u0435\u0440)",
         "> party list - \u041f\u043e\u0441\u043c\u043e\u0442\u0440\u0435\u0442\u044c \u0441\u043f\u0438\u0441\u043e\u043a \u0438\u0433\u0440\u043e\u043a\u043e\u0432 \u0432 \u043f\u0430\u0442\u0438",
         "> party kick <player> - \u041a\u0438\u043a\u043d\u0443\u0442\u044c \u0438\u0433\u0440\u043e\u043a\u0430 (\u043b\u0438\u0434\u0435\u0440)"
      );
   }
}
