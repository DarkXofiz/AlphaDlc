package miranware.module.list.player;

import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.BooleanSetting;
import miranware.module.settings.ModeListSetting;

@ModuleInformation(
   moduleName = "No Push",
   moduleDesc = "Removes pushing from players and blocks",
   moduleCategory = ModuleCategory.PLAYER
)
public class NoPush extends Module {
   public final ModeListSetting objects = new ModeListSetting(
      "Objects",
      new BooleanSetting("Players", true),
      new BooleanSetting("Blocks", true)
   );
}
