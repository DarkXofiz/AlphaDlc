package miranware.util.config;

import java.util.ArrayList;
import java.util.List;

final class ConfigKeyAliases {
   private static final List<List<String>> ALIAS_GROUPS = List.of(
      List.of("Dog Fly", "Bib Fly"),
      List.of(
         "Only With Space",
         "Smart Crits"
      ),
      List.of(
         "Slowdown Mode",
         "Reset Sprint",
         "Slowdown"
      ),
      List.of("Swap From", "Swap To")
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
