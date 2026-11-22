package zov.alphadlc.module.settings;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ModeSetting extends Setting {
   private String value;
   private final List<String> modes;
   private final Map<String, String> aliases = new HashMap<>();

   public ModeSetting(String name, String defaultValue, String... modes) {
      super(name);
      this.value = defaultValue;
      this.modes = Arrays.asList(modes);
   }

   public ModeSetting addAlias(String from, String to) {
      this.aliases.put(from, to);
      return this;
   }

   private String resolve(String value) {
      return this.aliases.getOrDefault(value, value);
   }

   public String getValue() {
      return this.value;
   }

   public void setValue(String value) {
      String resolved = this.resolve(value);
      if (this.modes.contains(resolved)) {
         this.value = resolved;
      }
   }

   public boolean is(String equalsIgnoreCase) {
      return this.value.equalsIgnoreCase(equalsIgnoreCase);
   }

   public List<String> getModes() {
      return this.modes;
   }

   public int getIndex() {
      return this.modes.indexOf(this.value);
   }

   public void setIndex(int index) {
      if (index >= 0 && index < this.modes.size()) {
         this.value = this.modes.get(index);
      }
   }

   public void cycle() {
      int nextIndex = (this.getIndex() + 1) % this.modes.size();
      this.setValue(this.modes.get(nextIndex));
   }

   @Override
   public String getValueAsString() {
      return this.value;
   }

   @Override
   public void setValueFromString(String value) {
      String resolved = this.resolve(value);
      if (this.modes.contains(resolved)) {
         this.value = resolved;
      }
   }

   public ModeSetting setVisible(Supplier<Boolean> visible) {
      this.visible = visible;
      return this;
   }
}
