package miranware.module.list.combat;

import com.google.common.eventbus.Subscribe;
import java.util.HashSet;
import java.util.Set;
import lombok.Generated;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import miranware.event.list.EventEntitySpawn;
import miranware.event.list.EventObsidianPlace;
import miranware.event.list.EventRightClickBlock;
import miranware.event.list.EventTick;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.BooleanSetting;
import miranware.util.friend.FriendRepository;
import miranware.util.math.BestPoint;
import miranware.util.math.RotationUtil;
import miranware.util.player.other.InventoryUtil;
import miranware.util.player.other.SlownessManager;
import miranware.util.rotation.Rotation;
import miranware.util.rotation.RotationComponent;

@ModuleInformation(
   moduleName = "Auto Explosion",
   moduleDesc = "Automatically places crystals",
   moduleCategory = ModuleCategory.COMBAT
)
public class AutoExplosion extends Module {
   private final BooleanSetting saveSelf = new BooleanSetting("Don't Blast Self", false);
   private final BooleanSetting saveFriend = new BooleanSetting("Don't Blast Friends", false);
   private final BooleanSetting saveResources = new BooleanSetting("Don't Blast Resources", false);
   private BlockPos obsidianPos;
   private Entity entityToAttack;
   private int prevSlot = -1;
   private int ticksToDisableRightClicks;
   private final Set<BlockPos> myCrystalPlaces = new HashSet<>();

   @Subscribe
   private void onObsidianPlace(EventObsidianPlace e) {
      int crystalSlot = InventoryUtil.searchItemHotbar(Items.END_CRYSTAL);
      if (crystalSlot != -1) {
         if (this.obsidianPos == null && this.condition(e.getBlockPos())) {
            this.obsidianPos = e.getBlockPos();
            this.ticksToDisableRightClicks = 5;
         }
      }
   }

   @Subscribe
   private void onTick(EventTick e) {
      if (this.ticksToDisableRightClicks > 0) {
         this.ticksToDisableRightClicks--;
      }

      if (this.entityToAttack != null) {
         if (this.mc.player.getEyePos().distanceTo(BestPoint.getNearestPoint(this.entityToAttack)) < 3.0) {
            Rotation rotation = new Rotation(RotationUtil.calculate(BestPoint.getNearestPoint(this.entityToAttack)));
            RotationComponent.update(rotation, 360.0F, 360.0F, 360.0F, 360.0F, 0, 55, false);
            SlownessManager.addTimeTask(new SlownessManager.TimeTask(50L, () -> {
               if (this.entityToAttack != null) {
                  this.mc.interactionManager.attackEntity(this.mc.player, this.entityToAttack);
                  this.mc.player.swingHand(Hand.MAIN_HAND);
                  this.entityToAttack = null;
               }
            }, true));
         } else {
            this.entityToAttack = null;
         }
      }

      if (this.obsidianPos != null) {
         Rotation rotation = new Rotation(Vec3d.ofCenter(this.obsidianPos));
         RotationComponent.update(rotation, 360.0F, 360.0F, 360.0F, 360.0F, 0, 55, false);
         int slot = InventoryUtil.searchItemHotbar(Items.END_CRYSTAL);
         SlownessManager.addTimeTask(new SlownessManager.TimeTask(5L, () -> {
            if (slot != -1 && this.mc.crosshairTarget instanceof BlockHitResult hitResult) {
               if (this.prevSlot == -1) {
                  this.prevSlot = this.mc.player.getInventory().selectedSlot;
               }

               this.mc.player.getInventory().selectedSlot = slot;
               BlockPos placePos = hitResult.getBlockPos();
               this.myCrystalPlaces.add(placePos);
               this.mc.interactionManager.interactBlock(this.mc.player, Hand.MAIN_HAND, hitResult);
               SlownessManager.addTimeTask(new SlownessManager.TimeTask(5L, () -> {
                  if (this.prevSlot != -1) {
                     this.mc.player.getInventory().selectedSlot = this.prevSlot;
                  }

                  this.prevSlot = -1;
               }, true));
               this.obsidianPos = null;
            }
         }, true));
      }
   }

   @Subscribe
   private void onRightClick(EventRightClickBlock e) {
      if (this.mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL) {
         BlockPos placePos = e.getHitResult().getBlockPos().up();
         this.myCrystalPlaces.add(placePos);
      }
   }

   @Subscribe
   private void onEntitySpawn(EventEntitySpawn e) {
      if (e.getEntity() instanceof EndCrystalEntity crystal) {
         BlockPos var4 = crystal.getBlockPos();
         if (this.myCrystalPlaces.contains(var4)) {
            this.entityToAttack = crystal;
            this.myCrystalPlaces.remove(var4);
         }
      }
   }

   private boolean condition(BlockPos blockPos) {
      if (this.saveSelf.getValue() && this.mc.player.getY() > (double)blockPos.getY()) {
         return false;
      } else {
         for (Entity entity : this.mc.world.getEntities()) {
            if (entity != this.mc.player) {
               if (this.saveResources.getValue() && entity instanceof ItemEntity item) {
                  double dx = Math.abs(item.getX() - (double)blockPos.getX());
                  double dy = Math.abs(item.getY() - (double)blockPos.getY());
                  double dz = Math.abs(item.getZ() - (double)blockPos.getZ());
                  if (dx < 4.0 && dy < 2.0 && dz < 4.0) {
                     return false;
                  }
               }

               if (entity instanceof PlayerEntity player
                  && FriendRepository.isFriend(player.getNameForScoreboard())
                  && this.saveFriend.getValue()
                  && player.getY() > (double)blockPos.getY()) {
                  return false;
               }
            }
         }

         return true;
      }
   }

   @Override
   public void onDisable() {
      this.obsidianPos = null;
      this.entityToAttack = null;
      this.ticksToDisableRightClicks = 0;
      super.onDisable();
   }

   @Generated
   public int getTicksToDisableRightClicks() {
      return this.ticksToDisableRightClicks;
   }
}
