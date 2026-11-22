package zov.alphadlc.util.config;

import java.util.ArrayList;
import java.util.List;

final class ConfigKeyAliases {
   private static final List<List<String>> ALIAS_GROUPS = List.of(
      List.of("Dog Fly", "Bib Fly"),
      List.of(
         "\u0422\u043e\u043b\u044c\u043a\u043e \u0441 \u043f\u0440\u043e\u0431\u0435\u043b\u043e\u043c",
         "\u0423\u043c\u043d\u044b\u0435 \u043a\u0440\u0438\u0442\u044b"
      ),
      List.of(
         "\u0420\u0435\u0436\u0438\u043c \u0437\u0430\u043c\u0435\u0434\u043b\u0435\u043d\u0438\u044f",
         "\u0421\u0431\u0440\u043e\u0441 \u0441\u043f\u0440\u0438\u043d\u0442\u0430",
         "\u0417\u0430\u043c\u0435\u0434\u043b\u0435\u043d\u0438\u0435"
      ),
      List.of("\u0421\u0432\u0430\u043f\u0430\u0442\u044c \u0441", "\u0421\u0432\u0430\u043f\u0430\u0442\u044c \u043d\u0430")
   );

   private ConfigKeyAliases() {
   }

   static List<String> candidates(String currentName) {
      for (List<String> group : ALIAS_GROUPS) {
         if (group.contains(currentName)) {
            List<String> candidates = new ArrayList<>(group.size());
            candidates.add(currentName);

            for (String alias : group) {
               if (!alias.equals(currentName)) {
                  candidates.add(alias);
               }
            }

            return List.copyOf(candidates);
         }
      }

      return List.of(currentName);
   }
}
