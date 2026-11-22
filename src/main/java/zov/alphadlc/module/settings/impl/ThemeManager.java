package zov.alphadlc.module.settings.impl;

public class ThemeManager {
   private static ThemeManager instance;
   private final Theme defaultTheme = new Theme("Default", -12816651, -15921904);

   private ThemeManager() {
   }

   public static ThemeManager getInstance() {
      if (instance == null) {
         instance = new ThemeManager();
      }

      return instance;
   }

   public Theme getCurrentTheme() {
      return this.defaultTheme;
   }
}
