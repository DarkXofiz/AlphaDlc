package zov.alphadlc.util.base;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;
import lombok.Generated;
import zov.alphadlc.AlphaDLC;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.util.rotation.Component;

public final class Instance {
   private static final ConcurrentMap<Class<? extends Module>, Module> instances = new ConcurrentHashMap<>();
   private static final ConcurrentMap<Class<? extends Component>, Component> componentInstances = new ConcurrentHashMap<>();

   public static <T extends Module> T get(Class<T> clazz) {
      return clazz.cast(instances.computeIfAbsent(clazz, instance -> AlphaDLC.getInstance().getModuleStorage().get((Class<? extends Module>)instance)));
   }

   public static <T extends Component> T getComponent(Class<T> clazz) {
      return clazz.cast(
         componentInstances.computeIfAbsent(clazz, instance -> AlphaDLC.getInstance().getComponentManager().get((Class<? extends Component>)instance))
      );
   }

   public static <T extends Module> Supplier<T> getSupplier(Class<T> clazz) {
      return () -> clazz.cast(instances.computeIfAbsent(clazz, instance -> AlphaDLC.getInstance().getModuleStorage().get((Class<? extends Module>)instance)));
   }

   public static <T extends Module> T get(String module) {
      return AlphaDLC.getInstance().getModuleStorage().get(module);
   }

   public static List<Module> get(ModuleCategory category) {
      return AlphaDLC.getInstance().getModuleStorage().get(category);
   }

   @Generated
   private Instance() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
