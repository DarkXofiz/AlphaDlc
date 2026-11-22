package zov.alphadlc.module.list.render;

import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import zov.alphadlc.AlphaDLC;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.ModuleSettingDefinitions;
import zov.alphadlc.module.settings.ModeSetting;
import zov.alphadlc.module.settings.SliderSetting;

@ModuleInformation(
   moduleName = "Client Sounds",
   moduleDesc = "\u0421\u043e\u0437\u0434\u0430\u0435\u0442 \u0437\u0432\u0443\u043a \u043f\u0440\u0438 \u0432\u043a\u043b\u044e\u0447\u0435\u043d\u0438\u0438/\u0432\u044b\u043a\u043b\u044e\u0447\u0435\u043d\u0438\u0438 \u0444\u0443\u043d\u043a\u0446\u0438\u0438",
   moduleCategory = ModuleCategory.RENDER
)
public class ClientSounds extends Module {
   private static ClientSounds INSTANCE;
   private final ModeSetting soundMode = new ModeSetting(
      "\u041c\u043e\u0434 \u0437\u0432\u0443\u043a\u0430",
      "\u041f\u0435\u0440\u0432\u044b\u0439",
      "\u041f\u0435\u0440\u0432\u044b\u0439",
      "\u0412\u0442\u043e\u0440\u043e\u0439",
      "\u0422\u0440\u0435\u0442\u0438\u0439"
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
         if (INSTANCE.isEnabled() || INSTANCE == AlphaDLC.getInstance().getModuleStorage().get(ClientSounds.class)) {
            if (lastSound != null) {
               INSTANCE.mc.getSoundManager().stop(lastSound);
            }

            String var2 = INSTANCE.soundMode.getValue();

            SoundEvent soundToPlay = switch (var2) {
               case "\u0412\u0442\u043e\u0440\u043e\u0439" -> enabled ? ENABLE_EVENT1 : DISABLE_EVENT1;
               case "\u0422\u0440\u0435\u0442\u0438\u0439" -> enabled ? ENABLE_EVENT2 : DISABLE_EVENT2;
               default -> enabled ? ENABLE_EVENT : DISABLE_EVENT;
            };
            lastSound = PositionedSoundInstance.master(soundToPlay, 1.0F, INSTANCE.volume.getFloatValue() / 100.0F);
            INSTANCE.mc.getSoundManager().play(lastSound);
         }
      }
   }
}
