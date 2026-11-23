package miranware.util.commands.defaults;

import com.google.gson.JsonObject;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import net.minecraft.util.Formatting;
import miranware.util.IMinecraft;
import miranware.util.chat.ChatUtil;
import miranware.util.commands.api.Command;
import miranware.util.commands.api.argument.IArgConsumer;
import miranware.util.commands.api.exception.CommandException;
import miranware.util.commands.api.exception.CommandNotEnoughArgumentsException;
import miranware.util.commands.api.helpers.TabCompleteHelper;
import miranware.util.party.connection.PartyApiClient;

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
            this.logDirect("Unknown subcommand");
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
                  + "You created a party named "
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
                  + "You invited player "
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
               Formatting.GRAY + "You joined the party " + Formatting.WHITE + party
            )
      );
   }

   private void handleLeave() {
      JsonObject req = new JsonObject();
      req.addProperty("player", this.nick());
      PartyApiClient.postAsync(
         "/party/leave",
         req,
         json -> ChatUtil.send(Formatting.GRAY + "You left the party")
      );
   }

   private void handleDisband() {
      JsonObject req = new JsonObject();
      req.addProperty("leader", this.nick());
      PartyApiClient.postAsync(
         "/party/disband",
         req,
         json -> ChatUtil.send(Formatting.GRAY + "You disbanded the party")
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
                  + "You kicked player "
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
         ChatUtil.send(Formatting.GRAY + "Party players:");

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
      return "Lets you see party players at any distance";
   }

   @Override
   public List<String> getLongDesc() {
      return List.of(
         "Command for managing the party system",
         "",
         "Usage:",
         "> party create <name> - Create a party",
         "> party invite <player> - Invite a player (leader)",
         "> party join <name> - Join a party",
         "> party leave - Leave the party",
         "> party disband - Disband the party (leader)",
         "> party list - View the list of party players",
         "> party kick <player> - Kick a player (leader)"
      );
   }
}
