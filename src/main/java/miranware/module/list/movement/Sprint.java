package miranware.module.list.movement;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.player.PlayerEntity;
import miranware.MiranWare;
import miranware.event.list.EventTick;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.list.player.FreeCamera;

@ModuleInformation(
   moduleName = "Sprint",
   moduleDesc = "Automatic sprint",
   moduleCategory = ModuleCategory.MOVEMENT
)
public class Sprint extends Module {
   @Subscribe
   public void onUpdate(EventTick event) {
      if (this.mc.player != null) {
         this.mc.options.sprintKey.setPressed(false);
         PlayerEntity fakePlayer = MiranWare.getInstance().getModuleStorage().get(FreeCamera.class).fakePlayer;
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
