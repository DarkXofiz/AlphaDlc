package miranware.util.friend;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

final class CaseInsensitiveNameIndex<T> {
   private final Function<T, String> nameExtractor;
   private final Map<String, T> valuesByName = new LinkedHashMap<>();

   CaseInsensitiveNameIndex(Function<T, String> nameExtractor) {
      this.nameExtractor = nameExtractor;
   }

   synchronized boolean add(T value) {
      String key = this.keyOf(value);
      if (key != null && !this.valuesByName.containsKey(key)) {
         this.valuesByName.put(key, value);
         return true;
      } else {
         return false;
      }
   }

   synchronized T remove(String name) {
      return name == null ? null : this.valuesByName.remove(normalize(name));
   }

   synchronized boolean contains(String name) {
      return name != null && this.valuesByName.containsKey(normalize(name));
   }

   synchronized T get(String name) {
      return name == null ? null : this.valuesByName.get(normalize(name));
   }

   synchronized void replaceAll(Collection<? extends T> values) {
      this.valuesByName.clear();

      for (T value : values) {
         this.add(value);
      }
   }

   synchronized void clear() {
      this.valuesByName.clear();
   }

   synchronized List<T> values() {
      return List.copyOf(this.valuesByName.values());
   }

   private String keyOf(T value) {
      if (value == null) {
         return null;
      } else {
         String name = this.nameExtractor.apply(value);
         return name == null ? null : normalize(name);
      }
   }

   private static String normalize(String name) {
      return name.toLowerCase(Locale.ROOT);
   }
}
