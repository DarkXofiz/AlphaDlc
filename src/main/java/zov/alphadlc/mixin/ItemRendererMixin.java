package zov.alphadlc.mixin;

import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import zov.alphadlc.module.list.render.ItemReplacer;
import zov.alphadlc.module.list.render.NoRender;
import zov.alphadlc.util.base.Instance;

@Mixin({ItemRenderer.class})
public class ItemRendererMixin {
   @Redirect(
      method = {"renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;III)V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/item/ItemModelManager;update(Lnet/minecraft/client/render/item/ItemRenderState;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;ZLnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V"
      )
   )
   private void useReplacementModel(
      ItemModelManager manager,
      ItemRenderState state,
      ItemStack stack,
      ModelTransformationMode mode,
      boolean leftHanded,
      World world,
      LivingEntity entity,
      int seed
   ) {
      ItemReplacer replacer = Instance.get(ItemReplacer.class);
      ItemStack renderedStack = replacer == null ? stack : replacer.apply(stack);
      NoRender removals = Instance.get(NoRender.class);
      if (removals != null
         && removals.isEnabled()
         && (mode == ModelTransformationMode.field_4321 || mode == ModelTransformationMode.field_4322)
         && stack.method_31574(Items.field_8378)
         && removals.elements.isEnabled("\u0423\u0434\u043e\u0447\u043a\u0430")) {
         renderedStack = ItemStack.field_8037;
      }

      manager.method_65598(state, renderedStack, mode, leftHanded, world, entity, seed);
   }
}
