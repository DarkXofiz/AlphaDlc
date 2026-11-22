package zov.alphadlc.util.rotation;

import java.util.HashMap;
import zov.alphadlc.AlphaDLC;

public final class ComponentManager extends HashMap<Class<? extends Component>, Component> {
   public void init() {
      this.add(new RotationComponent(), new FreeLookComponent());
      this.values().forEach(component -> AlphaDLC.getInstance().getEventBus().register(component));
   }

   public void add(Component... components) {
      for (Component component : components) {
         this.put((Class<? extends Component>)component.getClass(), component);
      }
   }

   public <T extends Component> T get(Class<T> clazz) {
      return this.values().stream().filter(component -> component.getClass() == clazz).map(clazz::cast).findFirst().orElse(null);
   }
}
