package zov.alphadlc.module.list.combat;

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
import zov.alphadlc.event.list.EventEntitySpawn;
import zov.alphadlc.event.list.EventObsidianPlace;
import zov.alphadlc.event.list.EventRightClickBlock;
import zov.alphadlc.event.list.EventTick;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.util.friend.FriendRepository;
import zov.alphadlc.util.math.BestPoint;
import zov.alphadlc.util.math.RotationUtil;
import zov.alphadlc.util.player.other.InventoryUtil;
import zov.alphadlc.util.player.other.SlownessManager;
import zov.alphadlc.util.rotation.Rotation;
import zov.alphadlc.util.rotation.RotationComponent;

@ModuleInformation(
   moduleName = "Auto Explosion",
   moduleDesc = "\u0410\u0432\u0442\u043e\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a\u0438 \u0440\u0430\u0437\u043c\u0435\u0449\u0430\u0435\u0442 \u043a\u0440\u0438\u0441\u0442\u0430\u043b\u043b\u044b",
   moduleCategory = ModuleCategory.COMBAT
)
public class AutoExplosion extends Module {
   private final BooleanSetting saveSelf = new BooleanSetting("\u041d\u0435 \u0431\u0430\u0431\u0430\u0445 \u0441\u0435\u0431\u044f", false);
   private final BooleanSetting saveFriend = new BooleanSetting("\u041d\u0435 \u0431\u0430\u0431\u0430\u0445 \u0434\u0440\u0443\u0437\u0435\u0439", false);
   private final BooleanSetting saveResources = new BooleanSetting("\u041d\u0435 \u0431\u0430\u0445\u0430\u0442\u044c \u0440\u0435\u0441\u044b", false);
   private BlockPos obsidianPos;
   private Entity entityToAttack;
   private int prevSlot = -1;
   private int ticksToDisableRightClicks;
   private final Set<BlockPos> myCrystalPlaces = new HashSet<>();

   @Subscribe
   private void onObsidianPlace(EventObsidianPlace e) {
      int crystalSlot = InventoryUtil.searchItemHotbar(Items.field_8301);
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
         if (this.mc.field_1724.method_33571().method_1022(BestPoint.getNearestPoint(this.entityToAttack)) < 3.0) {
            Rotation rotation = new Rotation(RotationUtil.calculate(BestPoint.getNearestPoint(this.entityToAttack)));
            RotationComponent.update(rotation, 360.0F, 360.0F, 360.0F, 360.0F, 0, 55, false);
            SlownessManager.addTimeTask(new SlownessManager.TimeTask(50L, () -> {
               if (this.entityToAttack != null) {
                  this.mc.field_1761.method_2918(this.mc.field_1724, this.entityToAttack);
                  this.mc.field_1724.method_6104(Hand.field_5808);
                  this.entityToAttack = null;
               }
            }, true));
         } else {
            this.entityToAttack = null;
         }
      }

      if (this.obsidianPos != null) {
         Rotation rotation = new Rotation(Vec3d.method_24953(this.obsidianPos));
         RotationComponent.update(rotation, 360.0F, 360.0F, 360.0F, 360.0F, 0, 55, false);
         int slot = InventoryUtil.searchItemHotbar(Items.field_8301);
         SlownessManager.addTimeTask(new SlownessManager.TimeTask(5L, () -> {
            if (slot != -1 && this.mc.field_1765 instanceof BlockHitResult hitResult) {
               if (this.prevSlot == -1) {
                  this.prevSlot = this.mc.field_1724.method_31548().field_7545;
               }

               this.mc.field_1724.method_31548().field_7545 = slot;
               BlockPos placePos = hitResult.method_17777();
               this.myCrystalPlaces.add(placePos);
               this.mc.field_1761.method_2896(this.mc.field_1724, Hand.field_5808, hitResult);
               SlownessManager.addTimeTask(new SlownessManager.TimeTask(5L, () -> {
                  if (this.prevSlot != -1) {
                     this.mc.field_1724.method_31548().field_7545 = this.prevSlot;
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
      if (this.mc.field_1724.method_6047().method_7909() == Items.field_8301) {
         BlockPos placePos = e.getHitResult().method_17777().method_10084();
         this.myCrystalPlaces.add(placePos);
      }
   }

   @Subscribe
   private void onEntitySpawn(EventEntitySpawn e) {
      if (e.getEntity() instanceof EndCrystalEntity crystal) {
         BlockPos var4 = crystal.method_24515();
         if (this.myCrystalPlaces.contains(var4)) {
            this.entityToAttack = crystal;
            this.myCrystalPlaces.remove(var4);
         }
      }
   }

   private boolean condition(BlockPos blockPos) {
      if (this.saveSelf.getValue() && this.mc.field_1724.method_23318() > (double)blockPos.method_10264()) {
         return false;
      } else {
         for (Entity entity : this.mc.field_1687.method_18112()) {
            if (entity != this.mc.field_1724) {
               if (this.saveResources.getValue() && entity instanceof ItemEntity item) {
                  double dx = Math.abs(item.method_23317() - (double)blockPos.method_10263());
                  double dy = Math.abs(item.method_23318() - (double)blockPos.method_10264());
                  double dz = Math.abs(item.method_23321() - (double)blockPos.method_10260());
                  if (dx < 4.0 && dy < 2.0 && dz < 4.0) {
                     return false;
                  }
               }

               if (entity instanceof PlayerEntity player
                  && FriendRepository.isFriend(player.method_5820())
                  && this.saveFriend.getValue()
                  && player.method_23318() > (double)blockPos.method_10264()) {
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
