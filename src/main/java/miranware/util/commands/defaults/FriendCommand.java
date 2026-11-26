package miranware.util.commands.defaults;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.ClickEvent.Action;
import net.minecraft.util.Formatting;
import miranware.MiranWare;
import miranware.util.commands.api.Command;
import miranware.util.commands.api.IBaritoneChatControl;
import miranware.util.commands.api.argument.IArgConsumer;
import miranware.util.commands.api.exception.CommandException;
import miranware.util.commands.api.helpers.Paginator;
import miranware.util.commands.api.helpers.TabCompleteHelper;
import miranware.util.friend.Friend;
import miranware.util.friend.FriendRepository;

public class FriendCommand extends Command {
   public FriendCommand(MiranWare onetap) {
      super("friend");
   }

   @Override
   public void execute(String label, IArgConsumer args) throws CommandException {
      String action = args.hasAny() ? args.getString().toLowerCase(Locale.US) : "list";
      switch (action) {
         case "add":
            this.handleAddFriend(args);
            break;
         case "remove":
            this.handleRemoveFriend(args);
            break;
         case "list":
            this.handleListFriends(args, label);
            break;
         case "clear":
            this.handleClearFriends(args);
            break;
         default:
            this.logDirect(
               "Unknown subcommand. Use add/remove/list/clear.",
               Formatting.GRAY
            );
      }
   }

   private void handleAddFriend(IArgConsumer args) throws CommandException {
      args.requireMin(1);
      String name = args.getString();
      if (FriendRepository.isFriend(name)) {
         this.logDirect(
            "This player is already in the friends list",
            Formatting.GRAY
         );
      } else {
         FriendRepository.addFriend(name);
         this.logDirect(
            Formatting.GRAY
               + "Player named "
               + Formatting.WHITE
               + name
               + Formatting.GRAY
               + " was added to friends"
         );
      }
   }

   private void handleRemoveFriend(IArgConsumer args) throws CommandException {
      args.requireMin(1);
      String name = args.getString();
      if (!FriendRepository.isFriend(name)) {
         this.logDirect("Friend not found", Formatting.GRAY);
      } else {
         FriendRepository.removeFriend(name);
         this.logDirect(
            "Player named "
               + Formatting.WHITE
               + name
               + Formatting.GRAY
               + " was removed from friends"
         );
      }
   }

   private void handleListFriends(IArgConsumer args, String label) throws CommandException {
      args.requireMax(1);
      List<Friend> friends = FriendRepository.getFriends();
      Paginator.paginate(
         args,
         new Paginator<>(friends),
         () -> this.logDirect("Friends list:", Formatting.GRAY),
         friend -> {
            Text nameText = Text.literal(Formatting.GRAY + "- " + Formatting.WHITE + friend.name());
            Text deleteText = Text.literal(Formatting.RED + " [Delete]")
               .styled(
                  style -> style.withClickEvent(new ClickEvent(Action.RUN_COMMAND, IBaritoneChatControl.FORCE_COMMAND_PREFIX + "friend remove " + friend.name()))
                        .withHoverEvent(new HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, Text.literal("Click to delete friend")))
               );
            return nameText.copy().append(deleteText);
         },
         IBaritoneChatControl.FORCE_COMMAND_PREFIX + label
      );
   }

   private void handleClearFriends(IArgConsumer args) throws CommandException {
      args.requireMax(1);
      FriendRepository.clear();
      this.logDirect("Friends list cleared", Formatting.GRAY);
   }

   @Override
   public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
      if (args.hasAny() && args.hasExactlyOne()) {
         return new TabCompleteHelper().prepend("add", "remove", "list", "clear").filterPrefix(args.getString()).sortAlphabetically().stream();
      } else {
         if (args.hasAny()) {
            String action = args.peekString(0).toLowerCase(Locale.ROOT);
            if (action.equals("remove") && args.hasExactly(2)) {
               String prefix = args.peekString(1).toLowerCase(Locale.ROOT);
               return FriendRepository.getFriends()
                  .stream()
                  .map(Friend::name)
                  .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix))
                  .sorted()
                  .distinct();
            }

            if (action.equals("add") && args.hasExactly(2)) {
               String prefix = args.peekString(1).toLowerCase(Locale.ROOT);
               return MinecraftClient.getInstance()
                  .getNetworkHandler()
                  .getPlayerList()
                  .stream()
                  .map(info -> info.getProfile().getName())
                  .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix))
                  .sorted()
                  .distinct();
            }
         }

         return Stream.empty();
      }
   }

   @Override
   public String getShortDesc() {
      return "Friend management";
   }

   @Override
   public List<String> getLongDesc() {
      return Arrays.asList(
         "This command lets you manage the friends list.",
         "",
         "Usage:",
         "> friend add <name> - Adds a player to friends.",
         "> friend remove <name> - Removes a player from friends.",
         "> friend list - Shows the list of all friends.",
         "> friend clear - Removes all friends."
      );
   }
}
