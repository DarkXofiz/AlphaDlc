package miranware.module;

import miranware.module.settings.BooleanSetting;
import miranware.module.settings.ModeSetting;
import miranware.module.settings.SliderSetting;

public final class ModuleSettingDefinitions {
   private ModuleSettingDefinitions() {
   }

   public static ModeSetting killAuraRotation() {
      return new ModeSetting("Rotation", "ReallyWorld", "ReallyWorld", "Smooth");
   }

   public static BooleanSetting killAuraOnlySpace() {
      return new BooleanSetting("Only With Space", false);
   }

   public static BooleanSetting killAuraClientLook() {
      return new BooleanSetting("Client Bow", false);
   }

   public static ModeSetting killAuraSprintReset() {
      return new ModeSetting(
         "Reset Sprint",
         "Before Hit",
         "By Radius",
         "Before Hit"
      );
   }

   public static ModeSetting autoSwapFrom() {
      return new ModeSetting(
         "Swap From",
         "Sphere",
         "Gapple",
         "Shield",
         "Talisman",
         "Sphere"
      );
   }

   public static ModeSetting autoSwapTo() {
      return new ModeSetting(
         "Swap To",
         "Sphere",
         "Gapple",
         "Shield",
         "Talisman",
         "Sphere"
      );
   }

   public static BooleanSetting triggerBotSmartCriticals() {
      return new BooleanSetting("Smart Crits", false);
   }

   public static SliderSetting clientSoundsVolume() {
      return new SliderSetting("Volume", 100.0, 0.0, 100.0, 1.0);
   }

   public static BooleanSetting autoTpOnlyFriends() {
      return new BooleanSetting("Friends Only", true);
   }
}
