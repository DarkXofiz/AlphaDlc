package miranware.module.list.combat;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import miranware.event.list.EventTick;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.BooleanSetting;
import miranware.module.settings.SliderSetting;
import miranware.util.friend.FriendRepository;
import miranware.util.math.StopWatch;

@ModuleInformation(
   moduleName = "MaceTarget",
   moduleDesc = "Automatic mace attack",
   moduleCategory = ModuleCategory.COMBAT
)
public class MaceTarget extends Module {
   private final SliderSetting distance = new SliderSetting("Distance", 3.0, 2.0, 3.0, 0.5);
   private final SliderSetting minHeight = new SliderSetting(
      "Min. Fall Height", 3.0, 0.0, 20.0, 0.5
   );
   private final BooleanSetting switchToMace = new BooleanSetting("Swap To Mace", true);
   private final BooleanSetting onlyFalling = new BooleanSetting(
      "Only While Falling Down", true
   );
   private final StopWatch cooldown = new StopWatch();
   private int savedSlot = -1;

   @Override
   public void onDisable() {
      this.restoreSlot();
      super.onDisable();
   }

   @Subscribe
   public void onTick(EventTick event) {
      if (this.mc.player != null && this.mc.world != null) {
         if (this.mc.player.isOnGround()) {
            this.restoreSlot();
         } else if (this.onlyFalling.getValue() && this.mc.player.getVelocity().y >= 0.0) {
            this.restoreSlot();
         } else if ((double)this.mc.player.fallDistance < this.minHeight.getValue()) {
            this.restoreSlot();
         } else {
            LivingEntity target = this.findTarget();
            if (target == null) {
               this.restoreSlot();
            } else {
               if (this.switchToMace.getValue()) {
                  int maceSlot = this.findMaceSlot();
                  if (maceSlot == -1) {
                     return;
                  }

                  if (this.mc.player.getInventory().selectedSlot != maceSlot) {
                     this.savedSlot = this.mc.player.getInventory().selectedSlot;
                     this.mc.player.getInventory().selectedSlot = maceSlot;
                     this.mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(maceSlot));
                  }
               } else if (this.mc.player.getMainHandStack().getItem() != Items.MACE) {
                  return;
               }

               if (this.cooldown.isReached(450L)) {
                  this.mc.interactionManager.attackEntity(this.mc.player, target);
                  this.mc.player.swingHand(Hand.MAIN_HAND);
                  this.restoreSlot();
                  this.cooldown.reset();
               }
            }
         }
      }
   }

   private LivingEntity findTarget() {
      LivingEntity best = null;
      double bestDist = Double.MAX_VALUE;

      for (Entity entity : this.mc.world.getEntities()) {
         if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)entity;
            double dist;
            PlayerEntity p2;
            if (entity != this.mc.player
               && entity.isAlive()
               && !(entity instanceof ArmorStandEntity)
               && (!(entity instanceof PlayerEntity) || FriendRepository.shouldAttack(p2 = (PlayerEntity)entity))
               && !((dist = (double)this.mc.player.distanceTo(entity)) > this.distance.getValue())
               && dist < bestDist) {
               bestDist = dist;
               best = living;
            }
         }
      }

      return best;
   }

   private int findMaceSlot() {
      for (int i2 = 0; i2 < 9; i2++) {
         if (this.mc.player.getInventory().getStack(i2).getItem() == Items.MACE) {
            return i2;
         }
      }

      return -1;
   }

   private void restoreSlot() {
      if (this.savedSlot != -1 && this.mc.player != null) {
         this.mc.player.getInventory().selectedSlot = this.savedSlot;
         this.mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(this.savedSlot));
         this.savedSlot = -1;
      }
   }
}
