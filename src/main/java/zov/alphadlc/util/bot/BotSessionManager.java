package zov.alphadlc.util.bot;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.network.ServerInfo.ServerType;
import net.minecraft.client.session.Session;
import net.minecraft.client.session.Session.AccountType;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.OnGroundOnly;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.common.KeepAliveS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import zov.alphadlc.mixin.IMinecraftClientAccessor;

public final class BotSessionManager {
   private static final List<BotSessionManager.BotConnection> connections = new CopyOnWriteArrayList<>();

   public static List<BotSessionManager.BotConnection> getConnections() {
      return new ArrayList<>(connections);
   }

   public static void connect(String name, String address) {
      MinecraftClient mc = MinecraftClient.method_1551();
      freezeCurrentSession();
      ((IMinecraftClientAccessor)mc).setSession(createSessionWithName(mc.method_1548(), name));
      mc.execute(
         () -> ConnectScreen.method_36877(
               new MultiplayerScreen(new TitleScreen()),
               mc,
               ServerAddress.method_2950(address),
               new ServerInfo(address, address, ServerType.field_45611),
               false,
               null
            )
      );
      connections.removeIf(b -> b.name().equalsIgnoreCase(name));
   }

   public static void pulseBots(boolean rightClick) {
      for (BotSessionManager.BotConnection bot : connections) {
         if (bot.handler() != null && bot.player() != null) {
            if (rightClick) {
               bot.handler().method_52787(new PlayerInteractItemC2SPacket(Hand.field_5808, 0, bot.player().method_36454(), bot.player().method_36455()));
            } else {
               bot.handler().method_52787(new HandSwingC2SPacket(Hand.field_5808));
            }
         }
      }
   }

   public static void sayAll(String message) {
      for (BotSessionManager.BotConnection bot : connections) {
         if (bot.handler() != null) {
            if (message.startsWith("/")) {
               bot.handler().method_45730(message.substring(1));
            } else {
               bot.handler().method_45729(message);
            }
         }
      }
   }

   public static boolean control(String name) {
      MinecraftClient mc = MinecraftClient.method_1551();
      return connections.stream()
         .filter(b -> b.name().equalsIgnoreCase(name))
         .findFirst()
         .map(
            bot -> {
               freezeCurrentSession();
               connections.remove(bot);
               Channel ch = getChannel(bot.connection());
               if (ch != null && ch.pipeline().get("bot_filter") != null) {
                  ch.pipeline().remove("bot_filter");
               }

               mc.field_1687 = bot.world();
               mc.field_1724 = bot.player();
               mc.field_1719 = bot.player();
               mc.field_1761 = bot.interactionManager();

               try {
                  for (Field f : MinecraftClient.class.getDeclaredFields()) {
                     if (f.getType() == ClientPlayNetworkHandler.class) {
                        f.setAccessible(true);
                        f.set(mc, bot.handler());
                        break;
                     }
                  }
               } catch (Exception var7) {
               }

               if (mc.field_1769 != null) {
                  mc.field_1769.method_3244(bot.world());
                  mc.field_1769.method_3279();
               }

               bot.handler()
                  .method_52787(
                     new Full(
                        mc.field_1724.method_23317(),
                        mc.field_1724.method_23318(),
                        mc.field_1724.method_23321(),
                        mc.field_1724.method_36454(),
                        mc.field_1724.method_36455(),
                        mc.field_1724.method_24828(),
                        mc.field_1724.field_5976
                     )
                  );
               ((IMinecraftClientAccessor)mc).setSession(createSessionWithName(mc.method_1548(), bot.name()));
               mc.method_1507(null);
               return true;
            }
         )
         .orElse(false);
   }

   private static void freezeCurrentSession() {
      MinecraftClient mc = MinecraftClient.method_1551();
      if (mc.method_1562() != null && mc.field_1687 != null) {
         ClientPlayNetworkHandler h = mc.method_1562();
         makeNettyBot(h, mc.method_1548().method_1676(), mc.field_1724);
         connections.add(
            new BotSessionManager.BotConnection(
               mc.method_1548().method_1676(),
               mc.method_1558() != null ? mc.method_1558().field_3761 : "",
               h.method_48296(),
               h,
               mc.field_1687,
               mc.field_1724,
               mc.field_1761
            )
         );
         mc.field_1687 = null;
         mc.field_1724 = null;

         try {
            for (Field f : MinecraftClient.class.getDeclaredFields()) {
               if (f.getType() == ClientPlayNetworkHandler.class) {
                  f.setAccessible(true);
                  f.set(mc, null);
                  break;
               }
            }
         } catch (Exception var6) {
         }
      }
   }

   private static void makeNettyBot(ClientPlayNetworkHandler handler, String name, ClientPlayerEntity botPlayer) {
      Channel channel = getChannel(handler.method_48296());
      if (channel != null) {
         channel.pipeline()
            .addBefore(
               "packet_handler",
               "bot_filter",
               new ChannelDuplexHandler() {
                  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                     if (msg instanceof KeepAliveS2CPacket p) {
                        handler.method_48296().method_10743(new KeepAliveC2SPacket(p.method_11517()));
                        if (botPlayer != null) {
                           handler.method_52787(new OnGroundOnly(botPlayer.method_24828(), botPlayer.field_5976));
                        }

                        ReferenceCountUtil.release(msg);
                     } else if (msg instanceof CommonPingS2CPacket p) {
                        handler.method_48296().method_10743(new CommonPongC2SPacket(p.method_36950()));
                        ReferenceCountUtil.release(msg);
                     } else if (msg instanceof PlayerPositionLookS2CPacket p) {
                        handler.method_52787(new TeleportConfirmC2SPacket(p.comp_3133()));
                        ReferenceCountUtil.release(msg);
                     } else if (msg instanceof HealthUpdateS2CPacket p) {
                        if (botPlayer != null) {
                           botPlayer.method_6033(p.method_11833());
                        }

                        ReferenceCountUtil.release(msg);
                     } else if (msg instanceof DisconnectS2CPacket) {
                        BotSessionManager.connections.removeIf(b -> b.name().equalsIgnoreCase(name));
                        ctx.close();
                        ReferenceCountUtil.release(msg);
                     } else {
                        String pName = msg.getClass().getSimpleName();
                        if (!pName.contains("Sound")
                           && !pName.contains("Particle")
                           && !pName.contains("Screen")
                           && !pName.contains("Chat")
                           && !pName.contains("Title")
                           && !pName.contains("Explosion")
                           && !pName.contains("BossBar")
                           && !pName.contains("Scoreboard")
                           && !pName.contains("OverlayMessage")) {
                           super.channelRead(ctx, msg);
                        } else {
                           ReferenceCountUtil.release(msg);
                        }
                     }
                  }
               }
            );
      }
   }

   public static boolean say(String n, String m) {
      return connections.stream().filter(b -> b.name().equalsIgnoreCase(n)).findFirst().map(b -> {
         if (m.startsWith("/")) {
            b.handler().method_45730(m.substring(1));
         } else {
            b.handler().method_45729(m);
         }

         return true;
      }).orElse(false);
   }

   public static boolean remove(String n) {
      return connections.removeIf(b -> {
         if (b.name().equalsIgnoreCase(n)) {
            b.connection().method_10747(Text.method_43470("Removed"));
            return true;
         } else {
            return false;
         }
      });
   }

   public static boolean restore() {
      return !connections.isEmpty() && control(connections.get(connections.size() - 1).name());
   }

   private static Channel getChannel(ClientConnection c) {
      try {
         for (Field f : ClientConnection.class.getDeclaredFields()) {
            if (Channel.class.isAssignableFrom(f.getType())) {
               f.setAccessible(true);
               return (Channel)f.get(c);
            }
         }
      } catch (Exception var5) {
      }

      return null;
   }

   private static Session createSessionWithName(Session current, String name) {
      try {
         Constructor<Session> c = Session.class
            .getDeclaredConstructor(String.class, UUID.class, String.class, Optional.class, Optional.class, AccountType.class);
         c.setAccessible(true);
         return c.newInstance(name, UUID.randomUUID(), current.method_1674(), Optional.empty(), Optional.empty(), AccountType.field_1988);
      } catch (Exception var3) {
         throw new RuntimeException(var3);
      }
   }

   public static record BotConnection(
      String name,
      String address,
      ClientConnection connection,
      ClientPlayNetworkHandler handler,
      ClientWorld world,
      ClientPlayerEntity player,
      ClientPlayerInteractionManager interactionManager
   ) {
   }
}
