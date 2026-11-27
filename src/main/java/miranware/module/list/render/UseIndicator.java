package miranware.module.list.render;

import com.google.common.eventbus.Subscribe;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.consume.UseAction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import miranware.event.list.EventEntitySpawn;
import miranware.event.list.EventHUD;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.module.settings.BooleanSetting;
import miranware.module.settings.SliderSetting;
import miranware.util.render.math.ProjectionUtil;
import miranware.util.render.providers.ColorProvider;
import miranware.util.render.renderers.DrawUtil;

@ModuleInformation(
   moduleName = "UseIndicator",
   moduleDesc = "Icon of the item in use and firework flashes",
   moduleCategory = ModuleCategory.RENDER
)
public class UseIndicator extends Module {
   private static final long FIREWORK_LIFETIME_MS = 1000L;
   private static final int MAX_FIREWORKS = 64;
   private final BooleanSetting players = new BooleanSetting("On Players", true);
   private final BooleanSetting fireworks = new BooleanSetting("Fireworks", true);
   private final SliderSetting scale = new SliderSetting("Scale", 1.0, 0.3, 2.0, 0.05);
   private final List<UseIndicator.Spawn> fireworkSpawns = new ArrayList<>();

   @Subscribe
   private void onEntitySpawn(EventEntitySpawn e) {
      if (this.fireworks.getValue()) {
         if (e.getEntity() instanceof FireworkRocketEntity rocket) {
            ItemStack var7 = new ItemStack(Items.FIREWORK_ROCKET);
            synchronized (this.fireworkSpawns) {
               this.fireworkSpawns.add(new UseIndicator.Spawn(rocket.getPos(), var7, System.currentTimeMillis() + 1000L));

               while (this.fireworkSpawns.size() > 64) {
                  this.fireworkSpawns.remove(0);
               }
            }
         }
      }
   }

   @Subscribe
   private void onHud(EventHUD e) {
      if (this.mc.world != null && this.mc.player != null) {
         DrawContext ctx = e.getDrawContext();
         float tickDelta = this.mc.getRenderTickCounter().getTickDelta(true);
         float sc = this.scale.getFloatValue();
         if (this.players.getValue()) {
            for (PlayerEntity player : this.mc.world.getPlayers()) {
               if (this.shouldRender(player)) {
                  ItemStack stack = this.resolveConsumable(player);
                  if (!stack.isEmpty()) {
                     Vec3d pos = player.getLerpedPos(tickDelta).add(0.0, (double)player.getHeight() / 2.0, 0.0);
                     Vector2f screen = ProjectionUtil.project(pos, tickDelta);
                     if (screen.getX() != Float.MAX_VALUE) {
                        this.drawIcon(ctx, stack, screen.getX(), screen.getY(), sc);
                     }
                  }
               }
            }
         }

         if (this.fireworks.getValue()) {
            long now = System.currentTimeMillis();
            synchronized (this.fireworkSpawns) {
               Iterator<UseIndicator.Spawn> it = this.fireworkSpawns.iterator();

               while (it.hasNext()) {
                  UseIndicator.Spawn spawn = it.next();
                  if (now >= spawn.expireAt()) {
                     it.remove();
                  } else {
                     Vector2f screen = ProjectionUtil.project(spawn.pos(), tickDelta);
                     if (screen.getX() != Float.MAX_VALUE) {
                        this.drawIcon(ctx, spawn.stack(), screen.getX(), screen.getY(), sc);
                     }
                  }
               }
            }
         }
      }
   }

   private boolean shouldRender(LivingEntity entity) {
      if (!entity.isAlive() || !entity.isUsingItem()) {
         return false;
      } else if (entity == this.mc.player && this.mc.options.getPerspective().isFirstPerson()) {
         return false;
      } else {
         return entity.isInvisible() && entity != this.mc.player ? false : !this.resolveConsumable(entity).isEmpty();
      }
   }

   private ItemStack resolveConsumable(LivingEntity entity) {
      if (!entity.isUsingItem()) {
         return ItemStack.EMPTY;
      } else {
         ItemStack active = entity.getActiveItem();
         if (this.isConsumable(active)) {
            return active;
         } else {
            Hand hand = entity.getActiveHand();
            if (hand != null) {
               ItemStack handStack = entity.getStackInHand(hand);
               if (this.isConsumable(handStack)) {
                  return handStack;
               }
            }

            return ItemStack.EMPTY;
         }
      }
   }

   private boolean isConsumable(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         UseAction action = stack.getUseAction();
         return action == UseAction.EAT || action == UseAction.DRINK;
      } else {
         return false;
      }
   }

   private void drawIcon(DrawContext ctx, ItemStack stack, float x, float y, float sc) {
      float radius = 9.0F * sc;
      DrawUtil.drawRound(x - radius, y - radius, radius * 2.0F, radius * 2.0F, radius, ColorProvider.rgba(16, 16, 20, 205.0F));
      float itemSize = 16.0F * sc;
      ctx.getMatrices().push();
      ctx.getMatrices().translate(x - itemSize / 2.0F, y - itemSize / 2.0F, 300.0F);
      ctx.getMatrices().scale(sc, sc, 1.0F);
      ctx.drawItem(stack, 0, 0);
      ctx.getMatrices().pop();
   }

   @Override
   public void onDisable() {
      super.onDisable();
      synchronized (this.fireworkSpawns) {
         this.fireworkSpawns.clear();
      }
   }

   private static record Spawn(Vec3d pos, ItemStack stack, long expireAt) {
   }
}
