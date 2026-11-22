package zov.alphadlc.util.math;

import lombok.Generated;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.explosion.ExplosionImpl;
import zov.alphadlc.util.IMinecraft;

public final class CrystalDamageCalculator implements IMinecraft {
   public static float calculateCrystalDamage(Entity crystal, LivingEntity target, double extraResistance) {
      if (mc.field_1724.method_6079().method_7909() != Items.field_8575) {
         extraResistance = 0.0;
      }

      double explosionRadius = 12.0;
      double dx = target.method_23317() - crystal.method_23317();
      double dy = target.method_23320() - crystal.method_23318();
      double dz = target.method_23321() - crystal.method_23321();
      double distance = Math.sqrt(dx * dx + dy * dy + dz * dz) / explosionRadius;
      if (distance > 1.0) {
         return 0.0F;
      } else {
         Vec3d vec = new Vec3d(crystal.method_23317(), crystal.method_23318(), crystal.method_23321());
         float exposure = ExplosionImpl.method_61731(vec, target);
         double impact = (1.0 - distance) * (double)exposure;
         float rawDamage = (float)((impact * impact + impact) * (mc.field_1724.method_23318() <= crystal.method_23318() ? 3.7 : 3.35F) * 7.0 * 2.0);
         float finalDamage = applyBlastReduction(target, (double)rawDamage, extraResistance);

         return switch (target.method_37908().method_8407()) {
            case field_5801 -> 0.0F;
            case field_5805 -> finalDamage * 0.5F;
            case field_5807 -> finalDamage * 1.5F;
            default -> finalDamage;
         };
      }
   }

   private static float applyBlastReduction(LivingEntity entity, double damage, double extraResistance) {
      if (!(entity instanceof PlayerEntity player)) {
         return (float)damage;
      } else {
         double toughness = player.method_45325(EntityAttributes.field_23725);
         double armor = (double)player.method_6096();
         double factor = 2.0 + toughness / 4.0;
         double armorReduction = MathHelper.method_15350(armor - damage / factor, armor * 0.2, 20.0);
         damage *= 1.0 - armorReduction / 25.0;
         float protectionAmount = 0.0F;

         for (ItemStack stack : player.method_5661()) {
            RegistryEntry<Enchantment> protection = (RegistryEntry<Enchantment>)((Registry)MinecraftClient.method_1551()
                  .field_1687
                  .method_30349()
                  .method_46759(RegistryKeys.field_41265)
                  .get())
               .method_10223(Enchantments.field_9111.method_29177())
               .orElseThrow();
            protectionAmount += (float)EnchantmentHelper.method_8225(protection, stack);
         }

         if (protectionAmount > 0.0F) {
            damage *= 1.0 - (double)Math.min(20.0F, protectionAmount) / 25.0;
         }

         if (player.method_6059(StatusEffects.field_5907)) {
            int level = player.method_6112(StatusEffects.field_5907).method_5578();
            damage *= 1.0 - (double)(level + 1) * 0.2;
         }

         damage *= 1.0 - extraResistance;
         return (float)Math.max(damage, 0.0);
      }
   }

   @Generated
   private CrystalDamageCalculator() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
