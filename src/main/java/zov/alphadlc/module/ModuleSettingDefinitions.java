package zov.alphadlc.module;

import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.module.settings.ModeSetting;
import zov.alphadlc.module.settings.SliderSetting;

public final class ModuleSettingDefinitions {
   private ModuleSettingDefinitions() {
   }

   public static ModeSetting killAuraRotation() {
      return new ModeSetting("\u0420\u043e\u0442\u0430\u0446\u0438\u044f", "ReallyWorld", "ReallyWorld", "Smooth");
   }

   public static BooleanSetting killAuraOnlySpace() {
      return new BooleanSetting("\u0422\u043e\u043b\u044c\u043a\u043e \u0441 \u043f\u0440\u043e\u0431\u0435\u043b\u043e\u043c", false);
   }

   public static BooleanSetting killAuraClientLook() {
      return new BooleanSetting("\u041a\u043b\u0438\u0435\u043d\u0442 \u043b\u0443\u043a", false);
   }

   public static ModeSetting killAuraSprintReset() {
      return new ModeSetting(
         "\u0421\u0431\u0440\u043e\u0441 \u0441\u043f\u0440\u0438\u043d\u0442\u0430",
         "\u041f\u0435\u0440\u0435\u0434 \u0443\u0434\u0430\u0440\u043e\u043c",
         "\u041f\u043e \u0440\u0430\u0434\u0438\u0443\u0441\u0443",
         "\u041f\u0435\u0440\u0435\u0434 \u0443\u0434\u0430\u0440\u043e\u043c"
      );
   }

   public static ModeSetting autoSwapFrom() {
      return new ModeSetting(
         "\u0421\u0432\u0430\u043f\u0430\u0442\u044c \u0441",
         "\u0428\u0430\u0440",
         "\u0413\u0435\u043f\u043b",
         "\u0429\u0438\u0442",
         "\u0422\u0430\u043b\u0438\u0441\u043c\u0430\u043d",
         "\u0428\u0430\u0440"
      );
   }

   public static ModeSetting autoSwapTo() {
      return new ModeSetting(
         "\u0421\u0432\u0430\u043f\u0430\u0442\u044c \u043d\u0430",
         "\u0428\u0430\u0440",
         "\u0413\u0435\u043f\u043b",
         "\u0429\u0438\u0442",
         "\u0422\u0430\u043b\u0438\u0441\u043c\u0430\u043d",
         "\u0428\u0430\u0440"
      );
   }

   public static BooleanSetting triggerBotSmartCriticals() {
      return new BooleanSetting("\u0423\u043c\u043d\u044b\u0435 \u043a\u0440\u0438\u0442\u044b", false);
   }

   public static SliderSetting clientSoundsVolume() {
      return new SliderSetting("\u0413\u0440\u043e\u043c\u043a\u043e\u0441\u0442\u044c", 100.0, 0.0, 100.0, 1.0);
   }

   public static BooleanSetting autoTpOnlyFriends() {
      return new BooleanSetting("\u0422\u043e\u043b\u044c\u043a\u043e \u0434\u0440\u0443\u0437\u044c\u044f", true);
   }
}
