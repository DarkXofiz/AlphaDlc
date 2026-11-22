package zov.alphadlc.module.list.misc;

import com.google.common.eventbus.Subscribe;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;
import net.minecraft.text.TextColor;
import zov.alphadlc.event.list.EventPacket;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.list.render.hud.Interface;
import zov.alphadlc.util.base.Instance;
import zov.alphadlc.util.friend.FriendRepository;
import zov.alphadlc.util.render.math.Animation;
import zov.alphadlc.util.render.math.Easing;
import zov.alphadlc.util.render.msdf.Fonts;
import zov.alphadlc.util.render.providers.ColorProvider;
import zov.alphadlc.util.render.renderers.DrawUtil;

@ModuleInformation(
   moduleName = "UseTracker",
   moduleDesc = "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0435\u0442 \u043a\u0442\u043e \u043f\u043e\u0434\u043e\u0431\u0440\u0430\u043b/\u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043b \u043f\u0440\u0435\u0434\u043c\u0435\u0442",
   moduleCategory = ModuleCategory.MISC
)
public class UseTracker extends Module {
   private final List<UseTracker.PickupLog> logs = new CopyOnWriteArrayList<>();
   private static final String[] ALLOWED_PICKUP_KEYWORDS = new String[]{
      "\u043d\u0435\u0437\u0435\u0440\u0438\u0442",
      "\u043d\u0430\u0431\u043e\u0440",
      "\u0448\u0430\u0440",
      "\u0442\u0430\u043b\u0438\u0441\u043c\u0430\u043d",
      "\u0437\u0435\u043b\u044c\u0435",
      "\u0430\u0440\u0431\u0430\u043b\u0435\u0442",
      "\u044d\u043b\u0438\u0442\u0440\u044b",
      "\u0444\u0435\u0439\u0435\u0440\u0432\u0435\u0440\u043a",
      "\u044f\u0431\u043b\u043e\u043a\u043e",
      "\u0441\u043e\u043b\u043d\u0435\u0447\u043d",
      "\u0442\u0440\u0435\u0437\u0443\u0431\u0435\u0446"
   };

   public UseTracker() {
      HudRenderCallback.EVENT.register(this::onRenderHUD);
   }

   @Override
   public void onDisable() {
      this.logs.clear();
      super.onDisable();
   }

   private String getProtectedName(PlayerEntity player) {
      String originalName = player.method_5477().getString();
      NameProtect nameProtect = Instance.get(NameProtect.class);
      boolean isNameProtectEnabled = nameProtect != null && nameProtect.isEnabled();
      if (isNameProtectEnabled) {
         boolean isMe = player.equals(this.mc.field_1724);
         boolean isFriend = FriendRepository.isFriend(player.method_5820());
         if (isMe || isFriend) {
            return "Protected";
         }
      }

      return originalName;
   }

   @Subscribe
   public void onPacketReceive(EventPacket event) {
      if (this.mc.field_1687 != null && this.mc.field_1724 != null) {
         if (event.getPacket() instanceof ItemPickupAnimationS2CPacket packet) {
            Entity itemEntity = this.mc.field_1687.method_8469(packet.method_11915());
            Entity collectorEntity = this.mc.field_1687.method_8469(packet.method_11912());
            if (itemEntity instanceof ItemEntity item && collectorEntity instanceof PlayerEntity player) {
               if (player == this.mc.field_1724) {
                  return;
               }

               ItemStack stack = item.method_6983().method_7972();
               String rawName = stack.method_7964().getString();
               String cleanName = rawName.replaceAll("(?i)\u00a7[0-9a-fk-orx]", "");
               String itemName = cleanName.toLowerCase(Locale.ROOT);
               boolean shouldLog = false;

               for (String keyword : ALLOWED_PICKUP_KEYWORDS) {
                  if (itemName.contains(keyword)) {
                     shouldLog = true;
                     break;
                  }
               }

               if (shouldLog) {
                  stack.method_7939(packet.method_11913());
                  String playerName = this.getProtectedName(player);
                  this.logs.add(new UseTracker.PickupLog(playerName, stack, 3000L, "\u041f\u043e\u0434\u043e\u0431\u0440\u0430\u043b:"));
               }
            }
         }

         if (event.getPacket() instanceof EntityStatusS2CPacket statusPacket) {
            if (statusPacket.method_11470() == 9) {
               if (statusPacket.method_11469(this.mc.field_1687) instanceof PlayerEntity player) {
                  if (player == this.mc.field_1724) {
                     return;
                  }

                  ItemStack usedStack = player.method_6047();
                  if (usedStack.method_7960() || !usedStack.method_57826(DataComponentTypes.field_50075) && usedStack.method_7909() != Items.field_8574) {
                     usedStack = player.method_6079();
                  }

                  if (!usedStack.method_7960()) {
                     String playerName = this.getProtectedName(player);
                     this.logs
                        .add(
                           new UseTracker.PickupLog(
                              playerName, usedStack.method_7972(), 3000L, "\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043b:"
                           )
                        );
                  }
               }
            } else if (statusPacket.method_11470() == 35 && statusPacket.method_11469(this.mc.field_1687) instanceof PlayerEntity player) {
               if (player == this.mc.field_1724) {
                  return;
               }

               String playerName = this.getProtectedName(player);
               ItemStack totemStack = Items.field_8288.method_7854();
               this.logs.add(new UseTracker.PickupLog(playerName, totemStack, 3000L, "\u041f\u043e\u0442\u0435\u0440\u044f\u043b:"));
            }
         }
      }
   }

   private void onRenderHUD(DrawContext context, RenderTickCounter tickCounter) {
      if (!this.logs.isEmpty() && this.isEnabled()) {
         int screenWidth = this.mc.method_22683().method_4486();
         int screenHeight = this.mc.method_22683().method_4502();
         float startY = (float)screenHeight / 2.0F + 20.0F;
         float currentY = startY;
         Interface hudModule = Instance.get(Interface.class);

         for (UseTracker.PickupLog log : this.logs) {
            log.update();
            float animValue = log.animation.getValue();
            if (log.isRemoving && animValue <= 0.01F) {
               this.logs.remove(log);
            } else {
               this.renderLog(context, log, screenWidth, currentY, animValue, hudModule);
               currentY += 16.0F * animValue;
            }
         }
      }
   }

   private void renderLog(DrawContext context, UseTracker.PickupLog log, int screenWidth, float y, float animValue, Interface hud) {
      String actionText = log.playerName + " " + log.actionText;
      String itemName = log.stack.method_7964().getString();
      float fontSize = 6.5F;
      float actionWidth = Fonts.SFMEDIUM.get().getWidth(actionText, fontSize);
      float itemWidth = Fonts.SFMEDIUM.get().getWidth(itemName, fontSize);
      float gap = 4.5F;
      float height = 13.0F;
      float totalWidth = 20.0F + actionWidth + gap + itemWidth + 5.0F;
      float x = ((float)screenWidth - totalWidth) / 2.0F;
      int alphaInt = (int)(255.0F * Math.max(0.0F, Math.min(1.0F, animValue)));
      context.method_51448().method_22903();
      context.method_51448().method_46416(x + totalWidth / 2.0F, y + height / 2.0F, 0.0F);
      context.method_51448().method_22905(animValue, animValue, 1.0F);
      context.method_51448().method_46416(-(x + totalWidth / 2.0F), -(y + height / 2.0F), 0.0F);
      if (hud != null && hud.isEnabled()) {
         hud.drawBackground(x, y, totalWidth, height, 3.0F, alphaInt);
      } else {
         DrawUtil.drawRound(x, y, totalWidth, height, 3.0F, ColorProvider.rgba(25, 25, 25, (float)((int)(150.0F * animValue))));
      }

      context.method_51448().method_22903();
      float iconScale = 0.6F;
      context.method_51448().method_46416(x + 3.5F, y + 1.5F, 0.0F);
      context.method_51448().method_22905(iconScale, iconScale, 1.0F);
      context.method_51427(log.stack, 0, 0);
      context.method_51448().method_22909();
      DrawUtil.drawRound(x + 16.0F, y + 2.0F, 0.5F, height - 4.0F, 0.0F, ColorProvider.rgba(125, 125, 125, (float)alphaInt));
      int actionColor = ColorProvider.rgba(255, 255, 255, (float)alphaInt);
      int itemColorRgb = 16755200;
      TextColor styleColor = log.stack.method_7964().method_10866().method_10973();
      if (styleColor != null) {
         itemColorRgb = styleColor.method_27716();
      }

      int itemColor = ColorProvider.rgba(itemColorRgb >> 16 & 0xFF, itemColorRgb >> 8 & 0xFF, itemColorRgb & 0xFF, (float)alphaInt);
      float textX = x + 20.0F;
      float textY = y + 2.75F;
      DrawUtil.drawText(Fonts.SFMEDIUM.get(), actionText, textX, textY, actionColor, fontSize);
      DrawUtil.drawText(Fonts.SFMEDIUM.get(), itemName, textX + actionWidth + gap, textY, itemColor, fontSize);
      context.method_51448().method_22909();
   }

   private static class PickupLog {
      public final String playerName;
      public final ItemStack stack;
      public final String actionText;
      private final long startTime;
      private final long maxLifeTime;
      public boolean isRemoving = false;
      public final Animation animation;

      public PickupLog(String playerName, ItemStack stack, long maxLifeTime, String actionText) {
         this.playerName = playerName;
         this.stack = stack;
         this.maxLifeTime = maxLifeTime;
         this.actionText = actionText;
         this.startTime = System.currentTimeMillis();
         this.animation = new Animation(Easing.BACK_OUT, 300L);
      }

      public void update() {
         long timeAlive = System.currentTimeMillis() - this.startTime;
         if (timeAlive > this.maxLifeTime && !this.isRemoving) {
            this.isRemoving = true;
         }

         this.animation.run(this.isRemoving ? 0.0F : 1.0F);
      }
   }
}
