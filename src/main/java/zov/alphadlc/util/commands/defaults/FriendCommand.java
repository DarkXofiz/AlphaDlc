package zov.alphadlc.util.commands.defaults;

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
import zov.alphadlc.AlphaDLC;
import zov.alphadlc.util.commands.api.Command;
import zov.alphadlc.util.commands.api.IBaritoneChatControl;
import zov.alphadlc.util.commands.api.argument.IArgConsumer;
import zov.alphadlc.util.commands.api.exception.CommandException;
import zov.alphadlc.util.commands.api.helpers.Paginator;
import zov.alphadlc.util.commands.api.helpers.TabCompleteHelper;
import zov.alphadlc.util.friend.Friend;
import zov.alphadlc.util.friend.FriendRepository;

public class FriendCommand extends Command {
   public FriendCommand(AlphaDLC onetap) {
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
               "\u041d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043d\u0430\u044f \u043f\u043e\u0434\u043a\u043e\u043c\u0430\u043d\u0434\u0430. \u0418\u0441\u043f\u043e\u043b\u044c\u0437\u0443\u0439 add/remove/list/clear.",
               Formatting.field_1080
            );
      }
   }

   private void handleAddFriend(IArgConsumer args) throws CommandException {
      args.requireMin(1);
      String name = args.getString();
      if (FriendRepository.isFriend(name)) {
         this.logDirect(
            "\u042d\u0442\u043e\u0442 \u0438\u0433\u0440\u043e\u043a \u0443\u0436\u0435 \u0432 \u0441\u043f\u0438\u0441\u043a\u0435 \u0434\u0440\u0443\u0437\u0435\u0439",
            Formatting.field_1080
         );
      } else {
         FriendRepository.addFriend(name);
         this.logDirect(
            Formatting.field_1080
               + "\u0418\u0433\u0440\u043e\u043a \u0441 \u0438\u043c\u0435\u043d\u0435\u043c "
               + Formatting.field_1068
               + name
               + Formatting.field_1080
               + " \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u0434\u043e\u0431\u0430\u0432\u043b\u0435\u043d \u0432 \u0434\u0440\u0443\u0437\u044c\u044f"
         );
      }
   }

   private void handleRemoveFriend(IArgConsumer args) throws CommandException {
      args.requireMin(1);
      String name = args.getString();
      if (!FriendRepository.isFriend(name)) {
         this.logDirect("\u0422\u0430\u043a\u043e\u0439 \u0434\u0440\u0443\u0433 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d", Formatting.field_1080);
      } else {
         FriendRepository.removeFriend(name);
         this.logDirect(
            "\u0418\u0433\u0440\u043e\u043a \u0441 \u0438\u043c\u0435\u043d\u0435\u043c "
               + Formatting.field_1068
               + name
               + Formatting.field_1080
               + " \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u0443\u0434\u0430\u043b\u0435\u043d \u0438\u0437 \u0434\u0440\u0443\u0437\u0435\u0439"
         );
      }
   }

   private void handleListFriends(IArgConsumer args, String label) throws CommandException {
      args.requireMax(1);
      List<Friend> friends = FriendRepository.getFriends();
      Paginator.paginate(
         args,
         new Paginator<>(friends),
         () -> this.logDirect("\u0421\u043f\u0438\u0441\u043e\u043a \u0434\u0440\u0443\u0437\u0435\u0439:", Formatting.field_1080),
         friend -> {
            Text nameText = Text.method_43470(Formatting.field_1080 + "- " + Formatting.field_1068 + friend.name());
            Text deleteText = Text.method_43470(Formatting.field_1061 + " [\u0423\u0434\u0430\u043b\u0438\u0442\u044c]")
               .method_27694(
                  style -> style.method_10958(new ClickEvent(Action.field_11750, IBaritoneChatControl.FORCE_COMMAND_PREFIX + "friend remove " + friend.name()))
                        .method_10949(new HoverEvent(net.minecraft.text.HoverEvent.Action.field_24342, Text.method_43470("Click to delete friend")))
               );
            return nameText.method_27661().method_10852(deleteText);
         },
         IBaritoneChatControl.FORCE_COMMAND_PREFIX + label
      );
   }

   private void handleClearFriends(IArgConsumer args) throws CommandException {
      args.requireMax(1);
      FriendRepository.clear();
      this.logDirect("\u0421\u043f\u0438\u0441\u043e\u043a \u0434\u0440\u0443\u0437\u0435\u0439 \u043e\u0447\u0438\u0449\u0435\u043d", Formatting.field_1080);
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
               return MinecraftClient.method_1551()
                  .method_1562()
                  .method_2880()
                  .stream()
                  .map(info -> info.method_2966().getName())
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
      return "\u0423\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u0435 \u0434\u0440\u0443\u0437\u044c\u044f\u043c\u0438";
   }

   @Override
   public List<String> getLongDesc() {
      return Arrays.asList(
         "\u042d\u0442\u0430 \u043a\u043e\u043c\u0430\u043d\u0434\u0430 \u043f\u043e\u0437\u0432\u043e\u043b\u044f\u0435\u0442 \u0443\u043f\u0440\u0430\u0432\u043b\u044f\u0442\u044c \u0441\u043f\u0438\u0441\u043a\u043e\u043c \u0434\u0440\u0443\u0437\u0435\u0439.",
         "",
         "\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435:",
         "> friend add <name> - \u0414\u043e\u0431\u0430\u0432\u043b\u044f\u0435\u0442 \u0438\u0433\u0440\u043e\u043a\u0430 \u0432 \u0434\u0440\u0443\u0437\u044c\u044f.",
         "> friend remove <name> - \u0423\u0434\u0430\u043b\u044f\u0435\u0442 \u0438\u0433\u0440\u043e\u043a\u0430 \u0438\u0437 \u0434\u0440\u0443\u0437\u0435\u0439.",
         "> friend list - \u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0435\u0442 \u0441\u043f\u0438\u0441\u043e\u043a \u0432\u0441\u0435\u0445 \u0434\u0440\u0443\u0437\u0435\u0439.",
         "> friend clear - \u0423\u0434\u0430\u043b\u044f\u0435\u0442 \u0432\u0441\u0435\u0445 \u0434\u0440\u0443\u0437\u0435\u0439."
      );
   }
}
