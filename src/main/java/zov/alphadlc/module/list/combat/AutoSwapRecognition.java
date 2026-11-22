package zov.alphadlc.module.list.combat;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

final class AutoSwapRecognition {
   private AutoSwapRecognition() {
   }

   static boolean isSphere(ItemStack stack) {
      if (stack == null) {
         return false;
      } else {
         boolean playerHead = stack.method_31574(Items.field_8575);
         AttributeModifiersComponent attributes = (AttributeModifiersComponent)stack.method_57824(DataComponentTypes.field_49636);
         boolean customSphere = playerHead && attributes != null && !attributes.comp_2393().isEmpty();
         return isSphereCandidate(customSphere, playerHead);
      }
   }

   static boolean isSphereCandidate(boolean customSphere, boolean playerHead) {
      return customSphere || playerHead;
   }
}
