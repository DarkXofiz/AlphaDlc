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
      if (this.mc.player != null) {
         this.mc.options.sprintKey.setPressed(false);
         PlayerEntity fakePlayer = AlphaDLC.getInstance().getModuleStorage().get(FreeCamera.class).fakePlayer;
         this.mc
            .player
            .setSprinting(
               fakePlayer == null
                  && (!this.mc.player.isTouchingWater() || this.mc.player.isSubmergedInWater())
                  && !this.mc.player.isGliding()
                  && this.mc.player.isWalking()
                  && this.mc.player.canSprint()
                  && !this.mc.player.isUsingItem()
                  && !this.mc.player.isBlind()
                  && (
                     !this.mc.player.hasVehicle()
                        || this.mc.player.getVehicle().canSprintAsVehicle()
                           && this.mc.player.getVehicle().isLogicalSideForUpdatingMovement()
                           && !this.mc.player.isGliding()
                           && (!this.mc.player.shouldSlowDown() || this.mc.player.isSubmergedInWater())
                  )
                  && this.mc.player.input.hasForwardMovement()
                  && !this.mc.player.horizontalCollision
                  && !this.mc.player.collidedSoftly
            );
      }
   }
}
