package zov.alphadlc.module.list.render;

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
import zov.alphadlc.event.list.EventEntitySpawn;
import zov.alphadlc.event.list.EventHUD;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.module.settings.SliderSetting;
import zov.alphadlc.util.render.math.ProjectionUtil;
import zov.alphadlc.util.render.providers.ColorProvider;
import zov.alphadlc.util.render.renderers.DrawUtil;

@ModuleInformation(
   moduleName = "UseIndicator",
   moduleDesc = "\u0418\u043a\u043e\u043d\u043a\u0430 \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u0443\u0435\u043c\u043e\u0433\u043e \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u0430 \u0438 \u0432\u0441\u043f\u044b\u0448\u043a\u0438 \u0444\u0435\u0439\u0435\u0440\u0432\u0435\u0440\u043a\u043e\u0432",
   moduleCategory = ModuleCategory.RENDER
)
public class UseIndicator extends Module {
   private static final long FIREWORK_LIFETIME_MS = 1000L;
   private static final int MAX_FIREWORKS = 64;
   private final BooleanSetting players = new BooleanSetting("\u0423 \u0438\u0433\u0440\u043e\u043a\u043e\u0432", true);
   private final BooleanSetting fireworks = new BooleanSetting("\u0424\u0435\u0439\u0435\u0440\u0432\u0435\u0440\u043a\u0438", true);
   private final SliderSetting scale = new SliderSetting("\u041c\u0430\u0441\u0448\u0442\u0430\u0431", 1.0, 0.3, 2.0, 0.05);
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
