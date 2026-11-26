package miranware.module.list.player;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class TpRequestRecognizer {
   private static final Pattern REQUEST = Pattern.compile(
      "^\\s*(?:\u0418\u0433\u0440\u043e\u043a\\s+)?([A-Za-z0-9_]{1,16})\\s+(?:\u043f\u0440\u043e\u0441\u0438\u0442 \u043a \u0432\u0430\u043c \u0442\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0438\u0440\u043e\u0432\u0430\u0442\u044c\u0441\u044f|\u0437\u0430\u043f\u0440\u0430\u0448\u0438\u0432\u0430\u0435\u0442 \u0442\u0435\u043b\u0435\u043f\u043e\u0440\u0442 \u043a \u0432\u0430\u043c|\u0445\u043e\u0447\u0435\u0442 \u0442\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0438\u0440\u043e\u0432\u0430\u0442\u044c\u0441\u044f \u043a \u0432\u0430\u043c|\u043e\u0442\u043f\u0440\u0430\u0432\u0438\u043b \u0432\u0430\u043c \u0437\u0430\u043f\u0440\u043e\u0441 \u043d\u0430 \u0442\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0430\u0446\u0438\u044e|has requested teleport(?: to you)?|has requested to teleport to you|sent you a teleport request)[.!]?\\s*$",
      66
   );

   private TpRequestRecognizer() {
   }

   static Optional<String> requester(String message) {
      if (message == null) {
         return Optional.empty();
      } else {
         Matcher matcher = REQUEST.matcher(message);
         return matcher.matches() ? Optional.of(matcher.group(1)) : Optional.empty();
      }
   }

   static boolean shouldAccept(String message, boolean friendsOnly, Predicate<String> isFriend) {
      Optional<String> requester = requester(message);
      return requester.isEmpty() ? false : !friendsOnly || isFriend.test(requester.get());
   }
}
