package miranware.module.list.player;

import com.google.common.eventbus.Subscribe;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import miranware.event.list.EventPacket;
import miranware.event.list.EventPopTotem;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;

@ModuleInformation(
   moduleName = "Fake Player",
   moduleDesc = "Spawns a fake player",
   moduleCategory = ModuleCategory.MISC
)
public class FakePlayer extends Module {
   private OtherClientPlayerEntity fakePlayer;
   private final float maxHealth = 20.0F;
   private float health = 20.0F;
   private static final long INVULN_MS = 500L;
   private long lastHitTime;

   @Override
   public void onEnable() {
      super.onEnable();
      this.spawnFake();
   }

   private void spawnFake() {
      if (this.mc.player != null && this.mc.world != null && this.mc.getNetworkHandler() != null) {
         if (this.fakePlayer == null) {
            this.health = 20.0F;
            this.lastHitTime = 0L;
            GameProfile profile = new GameProfile(UUID.randomUUID(), this.mc.player.getName().getString());
            this.fakePlayer = new OtherClientPlayerEntity(this.mc.world, profile);
            this.fakePlayer.copyFrom(this.mc.player);
            this.fakePlayer.setPos(this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ());
            this.fakePlayer.setYaw(this.mc.player.getYaw());
            this.fakePlayer.setPitch(this.mc.player.getPitch());
            this.fakePlayer.setHealth(20.0F);
            this.mc.world.addEntity(this.fakePlayer);
         }
      }
   }

   @Override
   public void onDisable() {
      this.removeFake();
      super.onDisable();
   }

   private void removeFake() {
      if (this.fakePlayer != null) {
         if (this.mc.world != null) {
            this.mc.world.removeEntity(this.fakePlayer.getId(), RemovalReason.DISCARDED);
         }

         this.fakePlayer = null;
      }

      this.health = 20.0F;
   }

   public boolean owns(Entity entity) {
      return entity != null && this.fakePlayer != null && entity == this.fakePlayer;
   }

   public void handleLocalAttack(PlayerEntity attacker) {
      if (attacker != null && this.fakePlayer != null && this.mc.world != null) {
         long now = System.currentTimeMillis();
         if (now - this.lastHitTime >= 500L) {
            this.lastHitTime = now;
            float cooldown = attacker.getAttackCooldownProgress(0.5F);
            float base = (float)attacker.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
            float damage = base * (0.2F + cooldown * cooldown * 0.8F);
            boolean charged = cooldown > 0.9F;
            boolean crit = charged
               && attacker.fallDistance > 0.0F
               && !attacker.isOnGround()
               && !attacker.isClimbing()
               && !attacker.isTouchingWater()
               && !attacker.hasStatusEffect(StatusEffects.BLINDNESS)
               && !attacker.hasVehicle();
            if (crit) {
               damage *= 1.5F;
            }

            boolean enchanted = attacker.getMainHandStack().hasEnchantments();
            boolean sweeping = charged
               && !crit
               && attacker.isOnGround()
               && !attacker.isSprinting()
               && attacker.getMainHandStack().getItem() instanceof SwordItem;
            this.playAttackSound(attacker, charged, crit, sweeping, damage);
            this.playHurtReaction(attacker);
            if (crit) {
               attacker.addCritParticles(this.fakePlayer);
            }

            if (enchanted) {
               attacker.addEnchantedHitParticles(this.fakePlayer);
            }

            if (sweeping) {
               this.spawnSweepParticle(attacker);
            }

            if (this.health - damage <= 0.0F) {
               this.popTotem();
               this.health = 20.0F;
            } else {
               this.health -= damage;
            }

            this.fakePlayer.setHealth(Math.max(this.health, 1.0F));
         }
      }
   }

   private void playAttackSound(PlayerEntity attacker, boolean charged, boolean crit, boolean sweeping, float damage) {
      SoundEvent sound;
      if (sweeping) {
         sound = SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP;
      } else if (crit) {
         sound = SoundEvents.ENTITY_PLAYER_ATTACK_CRIT;
      } else if (charged) {
         sound = SoundEvents.ENTITY_PLAYER_ATTACK_STRONG;
      } else if (damage <= 0.0F) {
         sound = SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE;
      } else {
         sound = SoundEvents.ENTITY_PLAYER_ATTACK_WEAK;
      }

      this.mc
         .world
         .playSound((PlayerEntity)null, attacker.getX(), attacker.getY(), attacker.getZ(), sound, attacker.getSoundCategory(), 1.0F, 1.0F);
   }

   private void playHurtReaction(PlayerEntity attacker) {
      double dx = this.fakePlayer.getX() - attacker.getX();
      double dz = this.fakePlayer.getZ() - attacker.getZ();
      float hitYaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - (double)attacker.getYaw());
      this.fakePlayer.hurtTime = this.fakePlayer.maxHurtTime = 10;
      this.fakePlayer.animateDamage(hitYaw);
      this.mc
         .world
         .playSound(
            (PlayerEntity)null,
            this.fakePlayer.getX(),
            this.fakePlayer.getY(),
            this.fakePlayer.getZ(),
            SoundEvents.ENTITY_PLAYER_HURT,
            this.fakePlayer.getSoundCategory(),
            1.0F,
            1.0F
         );
   }

   private void spawnSweepParticle(PlayerEntity attacker) {
      double rad = Math.toRadians((double)attacker.getYaw());
      double dx = -Math.sin(rad);
      double dz = Math.cos(rad);
      this.mc
         .world
         .addParticle(
            ParticleTypes.SWEEP_ATTACK, this.fakePlayer.getX() + dx, this.fakePlayer.getBodyY(0.5), this.fakePlayer.getZ() + dz, dx, 0.0, dz
         );
   }

   private void popTotem() {
      for (int i = 0; i < 40; i++) {
         this.mc
            .world
            .addParticle(
               ParticleTypes.TOTEM_OF_UNDYING,
               this.fakePlayer.getX(),
               this.fakePlayer.getBodyY(0.5),
               this.fakePlayer.getZ(),
               (this.mc.world.random.nextDouble() - 0.5) * 2.0,
               this.mc.world.random.nextDouble() * 0.5 + 0.5,
               (this.mc.world.random.nextDouble() - 0.5) * 2.0
            );
      }

      this.mc
         .world
         .playSound(
            (PlayerEntity)null,
            this.fakePlayer.getX(),
            this.fakePlayer.getY(),
            this.fakePlayer.getZ(),
            SoundEvents.ITEM_TOTEM_USE,
            this.fakePlayer.getSoundCategory(),
            1.0F,
            1.0F
         );
      new EventPopTotem(this.fakePlayer).post();
   }

   @Subscribe
   private void onPacket(EventPacket e) {
      if (e.getPacket() instanceof DisconnectS2CPacket || e.getPacket() instanceof GameJoinS2CPacket || e.getPacket() instanceof PlayerRespawnS2CPacket) {
         this.setEnabled(false);
      }
   }
}
