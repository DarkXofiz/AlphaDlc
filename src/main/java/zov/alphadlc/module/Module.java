package zov.alphadlc.module;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.minecraft.client.MinecraftClient;
import zov.alphadlc.AlphaDLC;
import zov.alphadlc.module.list.render.ClientSounds;
import zov.alphadlc.module.list.render.hud.Interface;
import zov.alphadlc.module.settings.ModeSetting;
import zov.alphadlc.module.settings.Setting;
import zov.alphadlc.util.IMinecraft;
import zov.alphadlc.util.QuickLogger;
import zov.alphadlc.util.base.Instance;
import zov.alphadlc.util.render.math.Animation;
import zov.alphadlc.util.render.math.Easing;

public class Module implements IMinecraft, QuickLogger {
   private final String name;
   private final String desc;
   private final ModuleCategory category;
   private int key;
   private boolean enabled;
   private final Animation animation = new Animation(Easing.BACK_OUT, 450L);
   public final MinecraftClient mc = MinecraftClient.method_1551();
   private final List<Setting> settings = new ArrayList<>();
   private boolean settingsCached = false;

   public Module() {
      ModuleInformation information = this.getClass().getAnnotation(ModuleInformation.class);
      this.name = information.moduleName();
      this.desc = information.moduleDesc();
      this.category = information.moduleCategory();
      this.key = information.moduleKeybind();
   }

   public List<Setting> getSettings() {
      if (!this.settingsCached) {
         for (Field field : this.getClass().getDeclaredFields()) {
            try {
               field.setAccessible(true);
               if (field.get(this) instanceof Setting setting) {
                  this.settings.add(setting);
               }
            } catch (IllegalAccessException var7) {
               var7.printStackTrace();
            }
         }

         this.settingsCached = true;
      }

      return this.settings;
   }

   public void setEnabled(boolean enabled) {
      if (this.enabled != enabled) {
         this.enabled = enabled;
         if (enabled) {
            this.onEnable();
         } else {
            this.onDisable();
         }

         Interface iface = Instance.get(Interface.class);
         if (iface != null && iface.isModuleStateNotifEnabled() && !this.isClickGui()) {
            iface.notifications.post(this.name, enabled);
         }
      }

      if (!this.isClickGui()) {
         ClientSounds soundsModule = Instance.get(ClientSounds.class);
         if (soundsModule != null && (soundsModule.isEnabled() || this instanceof ClientSounds)) {
            ClientSounds.play(this.isEnabled());
         }
      }
   }

   protected ModeSetting modeCreate() {
      return new ModeSetting("\u041c\u043e\u0434", "Vanilla", "Vanilla", "Grim", "Polar");
   }

   public void onEnable() {
      AlphaDLC.getInstance().getEventBus().register(this);
   }

   public void onDisable() {
      AlphaDLC.getInstance().getEventBus().unregister(this);
   }

   public void toggle() {
      this.setEnabled(!this.isEnabled());
   }

   private boolean isClickGui() {
      return this.getClass().getSimpleName().equals("ClickGui");
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public String getDesc() {
      return this.desc;
   }

   @Generated
   public ModuleCategory getCategory() {
      return this.category;
   }

   @Generated
   public int getKey() {
      return this.key;
   }

   @Generated
   public boolean isEnabled() {
      return this.enabled;
   }

   @Generated
   public Animation getAnimation() {
      return this.animation;
   }

   @Generated
   public MinecraftClient getMc() {
      return this.mc;
   }

   @Generated
   public boolean isSettingsCached() {
      return this.settingsCached;
   }

   @Generated
   public void setKey(int key) {
      this.key = key;
   }

   @Generated
   public void setSettingsCached(boolean settingsCached) {
      this.settingsCached = settingsCached;
   }
}
