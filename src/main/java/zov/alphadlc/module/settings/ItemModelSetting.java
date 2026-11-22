package zov.alphadlc.module.settings;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public class ItemModelSetting extends ModeSetting {
   private final Map<String, ItemStack> previewStacks = new LinkedHashMap<>();

   public ItemModelSetting(String name, String defaultValue, String... models) {
      super(name, defaultValue, models);

      for (String model : models) {
         ItemStack preview = new ItemStack(Items.field_8600);
         preview.method_57379(DataComponentTypes.field_54199, this.getModelId(model));
         this.previewStacks.put(model, preview);
      }
   }

   public Identifier getModelId(String model) {
      return Identifier.method_60655("mre", "item_replacer/sword/" + model);
   }

   public ItemStack getPreviewStack(String model) {
      return this.previewStacks.get(model);
   }
}
