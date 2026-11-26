package miranware.module.list.render;

import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import miranware.MiranWare;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.ModuleSettingDefinitions;
import miranware.module.settings.ModeSetting;
import miranware.module.settings.SliderSetting;

@ModuleInformation(
   moduleName = "Client Sounds",
   moduleDesc = "Plays a sound when a module is toggled",
   moduleCategory = ModuleCategory.RENDER
)
public class ClientSounds extends Module {
   private static ClientSounds INSTANCE;
   private final ModeSetting soundMode = new ModeSetting(
      "Sound Mode",
      "First",
      "First",
      "Second",
      "Third"
   );
   private final SliderSetting volume = ModuleSettingDefinitions.clientSoundsVolume();
   private static final Identifier ENABLE_ID = Identifier.of("mre", "module_enable");
   private static final Identifier DISABLE_ID = Identifier.of("mre", "module_disable");
   private static final Identifier ENABLE_ID1 = Identifier.of("mre", "module_enable1");
   private static final Identifier DISABLE_ID1 = Identifier.of("mre", "module_disable1");
   private static final Identifier ENABLE_ID2 = Identifier.of("mre", "module_enable2");
   private static final Identifier DISABLE_ID2 = Identifier.of("mre", "module_disable2");
   private static final SoundEvent ENABLE_EVENT = SoundEvent.of(ENABLE_ID);
   private static final SoundEvent DISABLE_EVENT = SoundEvent.of(DISABLE_ID);
   private static final SoundEvent ENABLE_EVENT1 = SoundEvent.of(ENABLE_ID1);
   private static final SoundEvent DISABLE_EVENT1 = SoundEvent.of(DISABLE_ID1);
   private static final SoundEvent ENABLE_EVENT2 = SoundEvent.of(ENABLE_ID2);
   private static final SoundEvent DISABLE_EVENT2 = SoundEvent.of(DISABLE_ID2);
   private static SoundInstance lastSound;

   public ClientSounds() {
      INSTANCE = this;
   }

   public static void play(boolean enabled) {
      if (INSTANCE != null && INSTANCE.mc.getSoundManager() != null) {
         if (INSTANCE.isEnabled() || INSTANCE == MiranWare.getInstance().getModuleStorage().get(ClientSounds.class)) {
            if (lastSound != null) {
               INSTANCE.mc.getSoundManager().stop(lastSound);
            }

            String var2 = INSTANCE.soundMode.getValue();

            SoundEvent soundToPlay = switch (var2) {
               case "Second" -> enabled ? ENABLE_EVENT1 : DISABLE_EVENT1;
               case "Third" -> enabled ? ENABLE_EVENT2 : DISABLE_EVENT2;
               default -> enabled ? ENABLE_EVENT : DISABLE_EVENT;
            };
            lastSound = PositionedSoundInstance.master(soundToPlay, 1.0F, INSTANCE.volume.getFloatValue() / 100.0F);
            INSTANCE.mc.getSoundManager().play(lastSound);
         }
      }
   }
}
