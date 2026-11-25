package miranware.module.settings;

import java.util.Arrays;
import java.util.function.Supplier;
import lombok.Generated;
import miranware.module.settings.impl.Theme;

public class ThemeSetting extends Setting {
   private final Theme[] themes;
   private Theme current;

   public ThemeSetting(Theme defaultValue, Theme... themes) {
      super("Poop");
      this.setValue(defaultValue);
      this.themes = themes;
   }

   public void setValue(Theme theme) {
      if (this.current == null) {
         this.current = theme;
      }

      if (this.current != theme) {
         theme.startAnimation(this.current.color1, this.current.color2);
         this.current = theme;
      }
   }

   public Theme getValue() {
      return this.current;
   }

   @Override
   public String getValueAsString() {
      return this.getValue().name;
   }

   @Override
   public void setValueFromString(String value) {
      Arrays.stream(this.themes).filter(theme -> theme.name.equals(value)).forEach(this::setValue);
   }

   public ThemeSetting setVisible(Supplier<Boolean> visible) {
      this.visible = visible;
      return this;
   }

   @Generated
   public Theme[] getThemes() {
      return this.themes;
   }

   @Generated
   public Theme getCurrent() {
      return this.current;
   }
}
