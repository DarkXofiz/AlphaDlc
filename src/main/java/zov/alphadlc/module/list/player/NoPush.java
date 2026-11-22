package zov.alphadlc.module.list.player;

import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.module.settings.ModeListSetting;

@ModuleInformation(
   moduleName = "No Push",
   moduleDesc = "\u0423\u0431\u0438\u0440\u0430\u0435\u0442 \u0442\u043e\u043b\u043a\u0430\u043d\u0438\u0435 \u043e\u0442 \u0438\u0433\u0440\u043e\u043a\u043e\u0432 \u0438 \u0431\u043b\u043e\u043a\u043e\u0432",
   moduleCategory = ModuleCategory.PLAYER
)
public class NoPush extends Module {
   public final ModeListSetting objects = new ModeListSetting(
      "\u041e\u0431\u044c\u0435\u043a\u0442\u044b",
      new BooleanSetting("\u0418\u0433\u0440\u043e\u043a\u0438", true),
      new BooleanSetting("\u0411\u043b\u043e\u043a\u0438", true)
   );
}
