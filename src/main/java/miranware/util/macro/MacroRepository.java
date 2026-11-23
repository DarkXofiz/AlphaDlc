package miranware.util.macro;

import com.google.common.eventbus.Subscribe;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.minecraft.util.Formatting;
import miranware.MiranWare;
import miranware.event.list.EventKeyInput;
import miranware.util.IMinecraft;
import miranware.util.QuickLogger;
import miranware.util.keyboard.KeyStorage;

public class MacroRepository implements IMinecraft, QuickLogger {
   private final File file = new File("miranware/macros.json");
   private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
   private final List<Macro> macroList = new ArrayList<>();

   public MacroRepository() {
      MiranWare.getInstance().getEventBus().register(this);
   }

   public boolean isEmpty() {
      return this.macroList.isEmpty();
   }

   public void addMacro(String message, int key) {
      this.macroList.add(new Macro(message, key));
   }

   public boolean hasMacro(String macroName) {
      for (Macro macro : this.macroList) {
         if (KeyStorage.getKey(macro.key()).equalsIgnoreCase(macroName)) {
            return true;
         }
      }

      return false;
   }

   public void deleteMacro(String name) {
      this.macroList.removeIf(macro -> KeyStorage.getKey(macro.key()).equalsIgnoreCase(name));
   }

   public void clearList() {
      this.macroList.clear();
   }

   public void save() {
      try {
         this.file.getParentFile().mkdirs();

         try (Writer writer = new OutputStreamWriter(new FileOutputStream(this.file), StandardCharsets.UTF_8)) {
            this.gson.toJson(this.macroList, writer);
         }
      } catch (IOException var6) {
         this.logDirect(
            "Error while saving macros: "
               + var6.getMessage(),
            Formatting.RED
         );
      }
   }

   public boolean removeByKey(int key) {
      return this.macroList.removeIf(m -> m.key() == key);
   }

   public void load() {
      if (this.file.exists()) {
         try (Reader reader = new InputStreamReader(new FileInputStream(this.file), StandardCharsets.UTF_8)) {
            Type listType = (new TypeToken<ArrayList<Macro>>() {
            }).getType();
            List<Macro> loaded = (List<Macro>)this.gson.fromJson(reader, listType);
            if (loaded != null) {
               this.macroList.clear();
               this.macroList.addAll(loaded);
            }
         } catch (IOException var6) {
            this.logDirect(
               "Error while loading macros: "
                  + var6.getMessage(),
               Formatting.RED
            );
         }
      }
   }

   @Subscribe
   public void onKey(EventKeyInput event) {
      int key = event.getKey();
      if (key != -1) {
         if (mc.player != null && event.getAction() != 0 && mc.currentScreen == null) {
            for (Macro macro : this.macroList) {
               if (macro.key() == key) {
                  String msg = macro.message();
                  if (msg.startsWith(".")) {
                     msg = msg.substring(1);
                     MiranWare.getInstance().getCommandDispatcher().runCommand(msg);
                  } else if (msg.startsWith("/")) {
                     mc.getNetworkHandler().sendChatCommand(msg.replaceFirst("/", ""));
                  } else {
                     mc.getNetworkHandler().sendChatMessage(msg);
                  }
               }
            }
         }
      }
   }

   @Generated
   public File getFile() {
      return this.file;
   }

   @Generated
   public Gson getGson() {
      return this.gson;
   }

   @Generated
   public List<Macro> getMacroList() {
      return this.macroList;
   }
}
