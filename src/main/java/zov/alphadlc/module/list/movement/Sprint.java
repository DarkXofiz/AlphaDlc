package zov.alphadlc.module.list.movement;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.player.PlayerEntity;
import zov.alphadlc.AlphaDLC;
import zov.alphadlc.event.list.EventTick;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.list.player.FreeCamera;

@ModuleInformation(
   moduleName = "Sprint",
   moduleDesc = "\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438\u0439 \u0441\u043f\u0440\u0438\u043d\u0442",
   moduleCategory = ModuleCategory.MOVEMENT
)
public class Sprint extends Module {
   @Subscribe
   public void onUpdate(EventTick event) {
      if (this.mc.field_1724 != null) {
         this.mc.field_1690.field_1867.method_23481(false);
         PlayerEntity fakePlayer = AlphaDLC.getInstance().getModuleStorage().get(FreeCamera.class).fakePlayer;
         this.mc
            .field_1724
            .method_5728(
               fakePlayer == null
                  && (!this.mc.field_1724.method_5799() || this.mc.field_1724.method_5869())
                  && !this.mc.field_1724.method_6128()
                  && this.mc.field_1724.method_20623()
                  && this.mc.field_1724.method_46743()
                  && !this.mc.field_1724.method_6115()
                  && !this.mc.field_1724.method_65525()
                  && (
                     !this.mc.field_1724.method_5765()
                        || this.mc.field_1724.method_5854().method_48155()
                           && this.mc.field_1724.method_5854().method_5787()
                           && !this.mc.field_1724.method_6128()
                           && (!this.mc.field_1724.method_20303() || this.mc.field_1724.method_5869())
                  )
                  && this.mc.field_1724.field_3913.method_20622()
                  && !this.mc.field_1724.field_5976
                  && !this.mc.field_1724.field_34927
            );
      }
   }
}
