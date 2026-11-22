package zov.alphadlc.module.list.render;

import com.google.common.eventbus.Subscribe;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import org.lwjgl.glfw.GLFW;
import zov.alphadlc.event.list.LookEvent;
import zov.alphadlc.event.list.RotationEvent;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.BindSetting;

@ModuleInformation(
   moduleName = "Third Person",
   moduleDesc = "\u0412\u0440\u0430\u0449\u0435\u043d\u0438\u0435 \u043a\u0430\u043c\u0435\u0440\u044b \u0431\u0435\u0437 \u043f\u043e\u0432\u043e\u0440\u043e\u0442\u0430 \u0442\u0435\u043b\u0430",
   moduleCategory = ModuleCategory.RENDER
)
public class FreeLook extends Module {
   public final BindSetting key = new BindSetting("\u041a\u043b\u0430\u0432\u0438\u0448\u0430", 342);
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
      if (this.mc.field_1724 != null) {
         this.cameraYaw = this.mc.field_1724.method_36454();
         this.cameraPitch = this.mc.field_1724.method_36455();
         this.playerYaw = this.mc.field_1724.method_36454();
         this.playerPitch = this.mc.field_1724.method_36455();
      }
   }

   private boolean isKeyPressed() {
      long handle = MinecraftClient.method_1551().method_22683().method_4490();
      int keyCode = this.key.getValue();
      return GLFW.glfwGetKey(handle, keyCode) == 1;
   }

   @Subscribe
   public void onLook(LookEvent event) {
      if (this.mc.field_1724 != null) {
         boolean wasActive = this.active;
         this.active = this.isKeyPressed();
         if (this.active) {
            this.mc.field_1690.method_31043(Perspective.field_26665);
            if (!wasActive) {
               this.playerYaw = this.mc.field_1724.method_36454();
               this.playerPitch = this.mc.field_1724.method_36455();
               this.cameraYaw = this.playerYaw;
               this.cameraPitch = this.playerPitch;
            }

            this.cameraYaw = this.cameraYaw + (float)event.getYaw() * 0.15F;
            this.cameraPitch = this.cameraPitch + (float)event.getPitch() * 0.15F;
            this.cameraPitch = Math.max(-90.0F, Math.min(90.0F, this.cameraPitch));
            event.setCancelled(true);
         } else if (wasActive) {
            this.mc.field_1690.method_31043(Perspective.field_26664);
            this.resetCamera();
         }
      }
   }

   @Subscribe
   public void onRotation(RotationEvent event) {
      if (this.mc.field_1724 != null) {
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
