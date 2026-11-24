package miranware.util.commands.api.datatypes;

import java.util.List;
import java.util.stream.Stream;
import miranware.util.commands.api.exception.CommandException;
import miranware.util.commands.api.helpers.TabCompleteHelper;
import miranware.util.friend.Friend;
import miranware.util.friend.FriendRepository;

public enum FriendDataType implements IDatatypeFor<Friend> {
   INSTANCE;

   @Override
   public Stream<String> tabComplete(IDatatypeContext datatypeContext) throws CommandException {
      Stream<String> friends = this.getFriends().stream().map(Friend::name);
      String context = datatypeContext.getConsumer().getString();
      return new TabCompleteHelper().append(friends).filterPrefix(context).sortAlphabetically().stream();
   }

   public Friend get(IDatatypeContext datatypeContext) throws CommandException {
      String username = datatypeContext.getConsumer().getString();
      return this.getFriends().stream().filter(s -> s.name().equalsIgnoreCase(username)).findFirst().orElse(null);
   }

   private List<? extends Friend> getFriends() {
      return FriendRepository.getFriends();
   }
}
