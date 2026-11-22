package zov.alphadlc.module.list.player;

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
import zov.alphadlc.event.list.EventPacket;
import zov.alphadlc.event.list.EventPopTotem;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;

@ModuleInformation(
   moduleName = "Fake Player",
   moduleDesc = "\u0421\u043f\u0430\u0432\u043d\u0438\u0442 \u0444\u0435\u0439\u043a\u043e\u0432\u043e\u0433\u043e \u0438\u0433\u0440\u043e\u043a\u0430",
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
      if (this.mc.field_1724 != null && this.mc.field_1687 != null && this.mc.method_1562() != null) {
         if (this.fakePlayer == null) {
            this.health = 20.0F;
            this.lastHitTime = 0L;
            GameProfile profile = new GameProfile(UUID.randomUUID(), this.mc.field_1724.method_5477().getString());
            this.fakePlayer = new OtherClientPlayerEntity(this.mc.field_1687, profile);
            this.fakePlayer.method_5878(this.mc.field_1724);
            this.fakePlayer.method_23327(this.mc.field_1724.method_23317(), this.mc.field_1724.method_23318(), this.mc.field_1724.method_23321());
            this.fakePlayer.method_36456(this.mc.field_1724.method_36454());
            this.fakePlayer.method_36457(this.mc.field_1724.method_36455());
            this.fakePlayer.method_6033(20.0F);
            this.mc.field_1687.method_53875(this.fakePlayer);
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
         if (this.mc.field_1687 != null) {
            this.mc.field_1687.method_2945(this.fakePlayer.method_5628(), RemovalReason.field_26999);
         }

         this.fakePlayer = null;
      }

      this.health = 20.0F;
   }

   public boolean owns(Entity entity) {
      return entity != null && this.fakePlayer != null && entity == this.fakePlayer;
   }

   public void handleLocalAttack(PlayerEntity attacker) {
      if (attacker != null && this.fakePlayer != null && this.mc.field_1687 != null) {
         long now = System.currentTimeMillis();
         if (now - this.lastHitTime >= 500L) {
            this.lastHitTime = now;
            float cooldown = attacker.method_7261(0.5F);
            float base = (float)attacker.method_45325(EntityAttributes.field_23721);
            float damage = base * (0.2F + cooldown * cooldown * 0.8F);
            boolean charged = cooldown > 0.9F;
            boolean crit = charged
               && attacker.field_6017 > 0.0F
               && !attacker.method_24828()
               && !attacker.method_6101()
               && !attacker.method_5799()
               && !attacker.method_6059(StatusEffects.field_5919)
               && !attacker.method_5765();
            if (crit) {
               damage *= 1.5F;
            }

            boolean enchanted = attacker.method_6047().method_7942();
            boolean sweeping = charged
               && !crit
               && attacker.method_24828()
               && !attacker.method_5624()
               && attacker.method_6047().method_7909() instanceof SwordItem;
            this.playAttackSound(attacker, charged, crit, sweeping, damage);
            this.playHurtReaction(attacker);
            if (crit) {
               attacker.method_7277(this.fakePlayer);
            }

            if (enchanted) {
               attacker.method_7304(this.fakePlayer);
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

            this.fakePlayer.method_6033(Math.max(this.health, 1.0F));
         }
      }
   }

   private void playAttackSound(PlayerEntity attacker, boolean charged, boolean crit, boolean sweeping, float damage) {
      SoundEvent sound;
      if (sweeping) {
         sound = SoundEvents.field_14706;
      } else if (crit) {
         sound = SoundEvents.field_15016;
      } else if (charged) {
         sound = SoundEvents.field_14840;
      } else if (damage <= 0.0F) {
         sound = SoundEvents.field_14914;
      } else {
         sound = SoundEvents.field_14625;
      }

      this.mc
         .field_1687
         .method_43128((PlayerEntity)null, attacker.method_23317(), attacker.method_23318(), attacker.method_23321(), sound, attacker.method_5634(), 1.0F, 1.0F);
   }

   private void playHurtReaction(PlayerEntity attacker) {
      double dx = this.fakePlayer.method_23317() - attacker.method_23317();
      double dz = this.fakePlayer.method_23321() - attacker.method_23321();
      float hitYaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - (double)attacker.method_36454());
      this.fakePlayer.field_6235 = this.fakePlayer.field_6254 = 10;
      this.fakePlayer.method_5879(hitYaw);
      this.mc
         .field_1687
         .method_43128(
            (PlayerEntity)null,
            this.fakePlayer.method_23317(),
            this.fakePlayer.method_23318(),
            this.fakePlayer.method_23321(),
            SoundEvents.field_15115,
            this.fakePlayer.method_5634(),
            1.0F,
            1.0F
         );
   }

   private void spawnSweepParticle(PlayerEntity attacker) {
      double rad = Math.toRadians((double)attacker.method_36454());
      double dx = -Math.sin(rad);
      double dz = Math.cos(rad);
      this.mc
         .field_1687
         .method_8406(
            ParticleTypes.field_11227, this.fakePlayer.method_23317() + dx, this.fakePlayer.method_23323(0.5), this.fakePlayer.method_23321() + dz, dx, 0.0, dz
         );
   }

   private void popTotem() {
      for (int i = 0; i < 40; i++) {
         this.mc
            .field_1687
            .method_8406(
               ParticleTypes.field_11220,
               this.fakePlayer.method_23317(),
               this.fakePlayer.method_23323(0.5),
               this.fakePlayer.method_23321(),
               (this.mc.field_1687.field_9229.method_43058() - 0.5) * 2.0,
               this.mc.field_1687.field_9229.method_43058() * 0.5 + 0.5,
               (this.mc.field_1687.field_9229.method_43058() - 0.5) * 2.0
            );
      }

      this.mc
         .field_1687
         .method_43128(
            (PlayerEntity)null,
            this.fakePlayer.method_23317(),
            this.fakePlayer.method_23318(),
            this.fakePlayer.method_23321(),
            SoundEvents.field_14931,
            this.fakePlayer.method_5634(),
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
