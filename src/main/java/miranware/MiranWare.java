package miranware;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import java.io.File;
import lombok.Generated;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import miranware.event.list.EventKeyInput;
import miranware.module.Module;
import miranware.module.ModuleStorage;
import miranware.ui.ThemeEditor;
import miranware.util.commands.CommandDispatcher;
import miranware.util.commands.manager.CommandRepository;
import miranware.util.config.ConfigManager;
import miranware.util.draggable.DragManager;
import miranware.util.friend.FriendRepository;
import miranware.util.macro.MacroRepository;
import miranware.util.math.TPSGetter;
import miranware.util.player.combat.IdealHitUtils;
import miranware.util.player.other.ServerManager;
import miranware.util.rotation.ComponentManager;
import miranware.util.script.ScriptManager;
import miranware.util.staff.StaffManager;

public class MiranWare implements ModInitializer {
   private static MiranWare instance;
   private final EventBus eventBus;
   private final ModuleStorage moduleStorage;
   private final ComponentManager componentManager;
   private final DragManager dragManager;
   private final CommandRepository commandRepository;
   private final MacroRepository macroRepository;
   private final ConfigManager configManager;
   private final CommandDispatcher commandDispatcher;
   private final StaffManager staffManager;
   private final ServerManager serverManager;
   private final TPSGetter tpsGetter;
   private final IdealHitUtils idealHitUtils;
   private final ScriptManager scriptManager;

   public MiranWare() {
      instance = this;
      this.eventBus = new EventBus();
      this.eventBus.register(this);
      this.moduleStorage = new ModuleStorage();
      this.componentManager = new ComponentManager();
      this.dragManager = new DragManager();
      this.macroRepository = new MacroRepository();
      this.configManager = new ConfigManager();
      this.staffManager = new StaffManager();
      this.staffManager.load();
      this.commandRepository = new CommandRepository();
      this.commandDispatcher = new CommandDispatcher();
      this.serverManager = new ServerManager();
      this.tpsGetter = new TPSGetter();
      this.idealHitUtils = new IdealHitUtils();
      this.scriptManager = new ScriptManager();
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
         ConfigManager.save("autocfg");
         this.getDragManager().saveDraggables();
         this.getMacroRepository().save();
         FriendRepository.save();
         this.staffManager.save();
      }));
      File dir = new File("miranware/configs/");
      if (!dir.exists()) {
         dir.mkdirs();
      }
   }

   public static MiranWare getInstance() {
      return instance == null ? new MiranWare() : instance;
   }

   public void onInitialize() {
      this.getModuleStorage().injectRegisterModules();
      this.componentManager.init();
      this.dragManager.load();
      this.macroRepository.load();
      FriendRepository.load();
      ConfigManager.load("autocfg");
      ThemeEditor.applyStartupTheme();
   }

   @Subscribe
   private void onModuleKeyPressed(EventKeyInput event) {
      for (Module module : this.getModuleStorage().getModules()) {
         if (event.getAction() == 1 && MinecraftClient.getInstance().currentScreen == null && module.getKey() == event.getKey()) {
            module.toggle();
         }
      }
   }

   @Generated
   public EventBus getEventBus() {
      return this.eventBus;
   }

   @Generated
   public ModuleStorage getModuleStorage() {
      return this.moduleStorage;
   }

   @Generated
   public ComponentManager getComponentManager() {
      return this.componentManager;
   }

   @Generated
   public DragManager getDragManager() {
      return this.dragManager;
   }

   @Generated
   public CommandRepository getCommandRepository() {
      return this.commandRepository;
   }

   @Generated
   public MacroRepository getMacroRepository() {
      return this.macroRepository;
   }

   @Generated
   public ConfigManager getConfigManager() {
      return this.configManager;
   }

   @Generated
   public CommandDispatcher getCommandDispatcher() {
      return this.commandDispatcher;
   }

   @Generated
   public StaffManager getStaffManager() {
      return this.staffManager;
   }

   @Generated
   public ServerManager getServerManager() {
      return this.serverManager;
   }

   @Generated
   public TPSGetter getTpsGetter() {
      return this.tpsGetter;
   }

   @Generated
   public IdealHitUtils getIdealHitUtils() {
      return this.idealHitUtils;
   }

   @Generated
   public ScriptManager getScriptManager() {
      return this.scriptManager;
   }
}
