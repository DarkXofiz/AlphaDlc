package zov.alphadlc.module.list.render.hud;

import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import zov.alphadlc.util.IMinecraft;
import zov.alphadlc.util.base.Instance;
import zov.alphadlc.util.render.math.Animation;
import zov.alphadlc.util.render.math.Easing;
import zov.alphadlc.util.render.msdf.Fonts;
import zov.alphadlc.util.render.providers.ColorProvider;
import zov.alphadlc.util.render.renderers.DrawUtil;
import zov.alphadlc.util.render.renderers.IRenderer;

public class NotificationsElement implements IMinecraft {
   private static final CopyOnWriteArrayList<NotificationsElement.Notification> notifications = new CopyOnWriteArrayList<>();
   private static final float TEXT_GAP = 3.0F;

   public void post(String name, boolean enabled) {
      NotificationsElement.Notification n = new NotificationsElement.Notification(name, enabled);
      n.moduleName = name;
      n.statusText = enabled ? "\u0412\u043a\u043b\u044e\u0447\u0435\u043d" : "\u0412\u044b\u043a\u043b\u044e\u0447\u0435\u043d";
      notifications.add(0, n);
   }

   public void postWarning(String text) {
      notifications.add(0, new NotificationsElement.Notification(text, true, true));
   }

   public void postTotem(Text tagText, boolean enchanted) {
      MutableText full = Text.method_43470("\u0418\u0433\u0440\u043e\u043a ")
         .method_10862(Style.field_24360.method_36139(16777215))
         .method_10852(tagText)
         .method_10852(
            Text.method_43470(" \u043f\u043e\u0442\u0435\u0440\u044f\u043b \u0422\u043e\u0442\u0435\u043c")
               .method_10862(Style.field_24360.method_36139(16777215))
         );
      NotificationsElement.Notification n = new NotificationsElement.Notification(full.getString(), true, true);
      n.isTotem = true;
      n.enchanted = enchanted;
      n.totemTagText = full;
      notifications.add(0, n);
   }

   public void render(DrawContext context) {
      Interface iface = Instance.get(Interface.class);
      float baseX = iface != null ? iface.getNotificationsCenterX() : (float)MinecraftClient.method_1551().method_22683().method_4486() / 2.0F;
      float baseY = iface != null ? iface.getNotificationsY() : (float)MinecraftClient.method_1551().method_22683().method_4502() / 2.0F + 20.0F;
      float offset = 0.0F;
      float scale = iface != null ? iface.getNotificationsScale() : 1.0F;
      boolean scaled = Math.abs(scale - 1.0F) > 0.001F;
      if (scaled) {
         IRenderer.DEFAULT_MATRIX.identity().translate(baseX, baseY, 0.0F).scale(scale, scale, 1.0F).translate(-baseX, -baseY, 0.0F);
      }

      for (NotificationsElement.Notification n : notifications) {
         if (System.currentTimeMillis() - n.time > n.duration && (double)n.anim.getValue() <= 0.01) {
            notifications.remove(n);
         } else {
            boolean expiring = System.currentTimeMillis() - n.time > n.duration;
            n.anim.run(expiring ? 0.0F : 1.0F);
            double animValue = (double)n.anim.getValue();
            if (!(animValue <= 0.01)) {
               float clampedAlpha = (float)Math.max(0.0, Math.min(1.0, animValue));
               int alphaInt = (int)(255.0F * clampedAlpha);
               float height = 14.5F;
               float iconSize = n.isWarning ? 8.0F : 9.0F;
               String fullText;
               String iconCode;
               int iconColor;
               if (n.isWarning) {
                  fullText = n.customText;
                  iconCode = "G";
                  iconColor = ColorProvider.rgba(255, 255, 255, (float)alphaInt);
               } else {
                  fullText = n.name;
                  iconCode = n.enabled ? "J" : "K";
                  iconColor = ColorProvider.rgba(255, 255, 255, (float)alphaInt);
               }

               float toggleW = 15.0F;
               float toggleH = 8.0F;
               float width;
               if (n.isWarning) {
                  float textWidth = Fonts.SFMEDIUM.get().getWidth(n.customText, 7.0F);
                  float iconWidth = Fonts.ICONS_NURIK.get().getWidth(iconCode, iconSize);
                  width = iconWidth + textWidth + 22.0F;
               } else if (n.isTotem) {
                  float textWidth = Fonts.SFMEDIUM.get().getWidth(n.totemTagText.getString(), 7.0F);
                  width = 10.0F + textWidth + 16.0F;
               } else {
                  String prefix = "\u041c\u043e\u0434\u0443\u043b\u044c";
                  float prefixWidth = Fonts.SFMEDIUM.get().getWidth(prefix, 7.0F);
                  float moduleWidth = Fonts.SFMEDIUM.get().getWidth(n.moduleName, 7.0F);
                  float statusWidth = Fonts.SFMEDIUM.get().getWidth(n.statusText, 7.0F);
                  float totalTextWidth = prefixWidth + 3.0F + moduleWidth + 3.0F + statusWidth;
                  width = totalTextWidth + toggleW + 16.0F;
               }

               float x = baseX - width / 2.0F;
               float y = baseY + offset;
               float cx = x + width / 2.0F;
               context.method_51448().method_22903();
               context.method_51448().method_46416(cx, y + height / 2.0F, 0.0F);
               context.method_51448().method_22905((float)animValue, (float)animValue, 1.0F);
               context.method_51448().method_46416(-cx, -(y + height / 2.0F), 0.0F);
               if (iface != null) {
                  iface.drawNotifBackground(context, x, y, width, height, 2.5F, clampedAlpha);
               }

               if (n.isWarning && !n.isTotem) {
                  float warningTextWidth = Fonts.SFMEDIUM.get().getWidth(fullText, 7.0F);
                  float warningIconWidth = Fonts.ICONS_NURIK.get().getWidth(iconCode, iconSize);
                  float warningTotalWidth = warningIconWidth + 5.0F + warningTextWidth;
                  float warningIconX = x + (width - warningTotalWidth) / 2.0F;
                  DrawUtil.drawText(Fonts.ICONS_NURIK.get(), iconCode, warningIconX, y + 4.0F, iconColor, iconSize);
               }

               float textY = y + (height - 7.0F) / 2.0F - 0.5F;
               if (n.isTotem && n.totemTagText != null) {
                  float textWidth_full = Fonts.SFMEDIUM.get().getWidth(n.totemTagText.getString(), 7.0F);
                  float totalWidth = textWidth_full + 4.0F + 10.0F;
                  float textX = x + (width - totalWidth) / 2.0F;
                  DrawUtil.drawText(Fonts.SFMEDIUM.get(), n.totemTagText, textX, textY, 7.0F, alphaInt);
                  context.method_51448().method_22903();
                  context.method_51448().method_46416(textX + textWidth_full + 4.0F, y + 2.5F, 0.0F);
                  context.method_51448().method_22905(0.6F, 0.6F, 1.0F);
                  context.method_51427(new ItemStack(Items.field_8288), 0, 0);
                  context.method_51448().method_22909();
               } else if (n.isWarning) {
                  float warningTextWidth = Fonts.SFMEDIUM.get().getWidth(fullText, 7.0F);
                  float warningIconWidth = Fonts.ICONS_NURIK.get().getWidth(iconCode, iconSize);
                  float warningTotalWidth = warningIconWidth + 5.0F + warningTextWidth;
                  float warningTextX = x + (width - warningTotalWidth) / 2.0F + warningIconWidth + 5.0F;
                  DrawUtil.drawText(Fonts.SFMEDIUM.get(), fullText, warningTextX, textY, ColorProvider.rgba(255, 255, 255, (float)alphaInt), 7.0F);
               } else {
                  String prefix = "\u041c\u043e\u0434\u0443\u043b\u044c";
                  String moduleName = n.moduleName;
                  String status = n.statusText;
                  float prefixWidth = Fonts.SFMEDIUM.get().getWidth(prefix, 7.0F);
                  float moduleWidth = Fonts.SFMEDIUM.get().getWidth(moduleName, 7.0F);
                  float statusWidth = Fonts.SFMEDIUM.get().getWidth(status, 7.0F);
                  float totalTextWidth = prefixWidth + 3.0F + moduleWidth + 3.0F + statusWidth;
                  int statusColor = n.enabled ? ColorProvider.rgba(0, 255, 100, (float)alphaInt) : ColorProvider.rgba(255, 80, 80, (float)alphaInt);
                  float totalModuleWidth = totalTextWidth + 5.0F + toggleW;
                  float startX = x + (width - totalModuleWidth) / 2.0F;
                  DrawUtil.drawText(Fonts.SFMEDIUM.get(), prefix, startX, textY, ColorProvider.rgba(255, 255, 255, (float)alphaInt), 7.0F);
                  float currentX = startX + prefixWidth + 3.0F;
                  DrawUtil.drawText(Fonts.SFMEDIUM.get(), moduleName, currentX, textY, ColorProvider.rgba(255, 255, 255, (float)alphaInt), 7.0F);
                  currentX += moduleWidth + 3.0F;
                  DrawUtil.drawText(Fonts.SFMEDIUM.get(), status, currentX, textY, statusColor, 7.0F);
                  float tX = startX + totalTextWidth + 5.0F;
                  float tY = y + (height - toggleH) / 2.0F;
                  float anim = n.enabled ? 1.0F : 0.0F;
                  if (iface != null) {
                     iface.drawNewToggle(tX, tY, toggleW, toggleH, n.enabled, anim, alphaInt);
                  }
               }

               context.method_51448().method_22909();
               offset += (height + 3.0F) * clampedAlpha;
            }
         }
      }

      if (scaled) {
         IRenderer.DEFAULT_MATRIX.identity();
      }
   }

   private static class Notification {
      String name;
      boolean enabled;
      long time;
      long duration = 1000L;
      Animation anim = new Animation(Easing.BACK_OUT, 300L);
      boolean isWarning = false;
      String customText;
      boolean isTotem = false;
      boolean enchanted = false;
      Text totemTagText = null;
      String moduleName = "";
      String statusText = "";

      public Notification(String name, boolean enabled) {
         this.name = name;
         this.enabled = enabled;
         this.time = System.currentTimeMillis();
      }

      public Notification(String customText, boolean enabled, boolean isWarning) {
         this.customText = customText;
         this.enabled = enabled;
         this.isWarning = isWarning;
         this.time = System.currentTimeMillis();
         this.duration = 2000L;
      }
   }
}
