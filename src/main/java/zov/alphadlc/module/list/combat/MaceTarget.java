package zov.alphadlc.module.list.combat;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import zov.alphadlc.event.list.EventTick;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.module.settings.SliderSetting;
import zov.alphadlc.util.friend.FriendRepository;
import zov.alphadlc.util.math.StopWatch;

@ModuleInformation(
   moduleName = "MaceTarget",
   moduleDesc = "\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0430\u044f \u0430\u0442\u0430\u043a\u0430 \u0431\u0443\u043b\u0430\u0432\u043e\u0439",
   moduleCategory = ModuleCategory.COMBAT
)
public class MaceTarget extends Module {
   private final SliderSetting distance = new SliderSetting("\u0414\u0438\u0441\u0442\u0430\u043d\u0446\u0438\u044f", 3.0, 2.0, 3.0, 0.5);
   private final SliderSetting minHeight = new SliderSetting(
      "\u041c\u0438\u043d. \u0432\u044b\u0441\u043e\u0442\u0430 \u043f\u0430\u0434\u0435\u043d\u0438\u044f", 3.0, 0.0, 20.0, 0.5
   );
   private final BooleanSetting switchToMace = new BooleanSetting("\u0421\u0432\u0430\u043f \u043d\u0430 \u0431\u0443\u043b\u0430\u0432\u0443", true);
   private final BooleanSetting onlyFalling = new BooleanSetting(
      "\u0422\u043e\u043b\u044c\u043a\u043e \u043f\u0440\u0438 \u043f\u0430\u0434\u0435\u043d\u0438\u0438 \u0432\u043d\u0438\u0437", true
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
