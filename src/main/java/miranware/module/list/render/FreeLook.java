package miranware.module.list.render;

import com.google.common.eventbus.Subscribe;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import org.lwjgl.glfw.GLFW;
import miranware.event.list.LookEvent;
import miranware.event.list.RotationEvent;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.BindSetting;

@ModuleInformation(
   moduleName = "Third Person",
   moduleDesc = "Rotates the camera without turning the body",
   moduleCategory = ModuleCategory.RENDER
)
public class FreeLook extends Module {
   public final BindSetting key = new BindSetting("Key", 342);
   private boolean active = false;
   private float cameraYaw = 0.0F;
   private float cameraPitch = 0.0F;
   private float playerYaw = 0.0F;
   private float playerPitch = 0.0F;

   @Override
   public void onEnable() {
      super.onEnable();
      this.resetCamera();
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.active = false;
   }

   private void resetCamera() {
      if (this.mc.player != null) {
         this.cameraYaw = this.mc.player.getYaw();
         this.cameraPitch = this.mc.player.getPitch();
         this.playerYaw = this.mc.player.getYaw();
         this.playerPitch = this.mc.player.getPitch();
      }
   }

   private boolean isKeyPressed() {
      long handle = MinecraftClient.getInstance().getWindow().getHandle();
      int keyCode = this.key.getValue();
      return GLFW.glfwGetKey(handle, keyCode) == 1;
   }

   @Subscribe
   public void onLook(LookEvent event) {
      if (this.mc.player != null) {
         boolean wasActive = this.active;
         this.active = this.isKeyPressed();
         if (this.active) {
            this.mc.options.setPerspective(Perspective.THIRD_PERSON_BACK);
            if (!wasActive) {
               this.playerYaw = this.mc.player.getYaw();
               this.playerPitch = this.mc.player.getPitch();
               this.cameraYaw = this.playerYaw;
               this.cameraPitch = this.playerPitch;
            }

            this.cameraYaw = this.cameraYaw + (float)event.getYaw() * 0.15F;
            this.cameraPitch = this.cameraPitch + (float)event.getPitch() * 0.15F;
            this.cameraPitch = Math.max(-90.0F, Math.min(90.0F, this.cameraPitch));
            event.setCancelled(true);
         } else if (wasActive) {
            this.mc.options.setPerspective(Perspective.FIRST_PERSON);
            this.resetCamera();
         }
      }
   }

   @Subscribe
   public void onRotation(RotationEvent event) {
      if (this.mc.player != null) {
         if (this.active) {
            event.setYaw(this.cameraYaw);
            event.setPitch(this.cameraPitch);
         }
      }
   }

   public boolean isActive() {
      return this.active && this.isEnabled();
   }

   public float getCameraYaw() {
      return this.cameraYaw;
   }

   public float getCameraPitch() {
      return this.cameraPitch;
   }
}
