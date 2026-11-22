package zov.alphadlc.module.list.render;

import com.google.common.eventbus.Subscribe;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector4f;
import zov.alphadlc.AlphaDLC;
import zov.alphadlc.event.list.EventHUD;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.list.misc.NameProtect;
import zov.alphadlc.module.list.render.hud.Interface;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.module.settings.ModeListSetting;
import zov.alphadlc.module.settings.SliderSetting;
import zov.alphadlc.util.friend.FriendRepository;
import zov.alphadlc.util.render.builders.Builder;
import zov.alphadlc.util.render.builders.states.QuadColorState;
import zov.alphadlc.util.render.builders.states.QuadRadiusState;
import zov.alphadlc.util.render.builders.states.SizeState;
import zov.alphadlc.util.render.math.ProjectionUtil;
import zov.alphadlc.util.render.msdf.Fonts;
import zov.alphadlc.util.render.msdf.MsdfFont;
import zov.alphadlc.util.render.providers.ColorProvider;
import zov.alphadlc.util.render.renderers.DrawUtil;
import zov.alphadlc.util.render.renderers.IRenderer;
import zov.alphadlc.util.replace.ReplaceUtil;
import zov.alphadlc.util.server.Server;

@ModuleInformation(
   moduleName = "NameTags",
   moduleDesc = "\u0422\u0435\u0433\u0438 \u043d\u0430\u0434 \u0438\u0433\u0440\u043e\u043a\u0430\u043c\u0438",
   moduleCategory = ModuleCategory.RENDER
)
public class NameTags extends Module {
   private final ModeListSetting entityTypes = new ModeListSetting(
      "\u0422\u0438\u043f\u044b",
      new BooleanSetting("\u0418\u0433\u0440\u043e\u043a\u0438", true),
      new BooleanSetting("\u041f\u0440\u0435\u0434\u043c\u0435\u0442\u044b", true)
   );
   private final SliderSetting size = new SliderSetting("\u0420\u0430\u0437\u043c\u0435\u0440", 7.5, 5.0, 15.0, 0.5);
   private final BooleanSetting blur = new BooleanSetting("\u0411\u043b\u044e\u0440", true);
   private final SliderSetting alpha = new SliderSetting("\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c", 100.0, 0.0, 100.0, 1.0);
   private final Map<UUID, Text> normalizedNames = new ConcurrentHashMap<>();
   private int clearCacheTicker = 0;
   private final List<ItemStack> equipmentCache = new ArrayList<>();

   public static Text normalizeSmallCaps(Text text) {
      String[] from = new String[]{
         "\u1d00",
         "\u0299",
         "\u1d04",
         "\u1d05",
         "\u1d07",
         "\ua730",
         "\u0262",
         "\u029c",
         "\u026a",
         "\u1d0a",
         "\u1d0b",
         "\u029f",
         "\u1d0d",
         "\u0274",
         "\u1d0f",
         "\u1d18",
         "\u01eb",
         "\u0280",
         "\ua731",
         "\u1d1b",
         "\u1d1c",
         "\u1d20",
         "\u1d21",
         "x",
         "\u028f",
         "\u1d22",
         "\u25c6",
         "\u2503 "
      };
      String[] to = new String[]{
         "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "\u25cf", ""
      };

      for (int i = 0; i < from.length; i++) {
         text = ReplaceUtil.replace(text, from[i], to[i]);
      }

      return text;
   }

   public static Text processName(PlayerEntity entity) {
      Text name = entity.getDisplayName();
      MinecraftClient mc = MinecraftClient.getInstance();
      NameProtect nameProtect = AlphaDLC.getInstance().getModuleStorage().get(NameProtect.class);
      if (nameProtect != null && nameProtect.isEnabled() && mc.player != null) {
         String scoreName = entity.getNameForScoreboard();
         String myName = mc.player.getNameForScoreboard();
         String customName = nameProtect.getCustomName();
         if (scoreName.equals(myName) || entity.getUuid().equals(mc.player.getUuid())) {
            name = ReplaceUtil.replace(name, myName, customName);
         } else if (nameProtect.hideFriends.getValue() && FriendRepository.isFriend(scoreName)) {
            name = ReplaceUtil.replace(name, scoreName, customName);
         }
      }

      String s = name.getString();
      if (s.contains("\ua500")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua500", Formatting.GRAY, "\u0418\u0413\u0420\u041e\u041a");
      }

      if (s.contains("\ua504")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua504", Formatting.BLUE, "HERO");
      }

      if (s.contains("\ua508")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua508", Formatting.GOLD, "TITAN");
      }

      if (s.contains("\ua512")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua512", Formatting.GREEN, "AVENGER");
      }

      if (s.contains("\ua516")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua516", Formatting.AQUA, "OVERLORD");
      }

      if (s.contains("\ua520")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua520", Formatting.GOLD, "MAGISTER");
      }

      if (s.contains("\ua524")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua524", Formatting.RED, "IMPERATOR");
      }

      if (s.contains("\ua532")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua532", Formatting.DARK_PURPLE, "BULL");
      }

      if (s.contains("\ua553")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua553", Formatting.DARK_GRAY, "GHOST");
      }

      if (s.contains("\ua528")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua528", Formatting.LIGHT_PURPLE, "DRAGON");
      }

      if (s.contains("\ua502")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua502", Formatting.GOLD, "GOD");
      }

      if (s.contains("\ua538")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua538", Formatting.GOLD, "GOD");
      }

      if (s.contains("\ua526")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua526", Formatting.BLUE, "D.ML.ADMIN");
      }

      if (s.contains("\ua540")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua540", Formatting.DARK_GREEN, "HYDRA");
      }

      if (s.contains("\ua556")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua556", Formatting.GRAY, "BUNNY");
      }

      if (s.contains("\ua552")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua552", Formatting.WHITE, "RABBIT");
      }

      if (s.contains("\ua548")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua548", Formatting.GREEN, "COBRA");
      }

      if (s.contains("\ua536")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua536", Formatting.GOLD, "TIGER");
      }

      if (s.contains("\ua560")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua560", Formatting.YELLOW, "D.HELPER");
      }

      if (s.contains("\ua509")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua509", Formatting.YELLOW, "HELPER");
      }

      if (s.contains("\ua506")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua506", Formatting.GRAY, "D.MODER");
      }

      if (s.contains("\ua544")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua544", Formatting.DARK_RED, "VAMPIRE");
      }

      if (s.contains("\ua530")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua530", Formatting.GRAY, "D.ML.ADMIN");
      }

      if (s.contains("\ua510")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua510", Formatting.DARK_BLUE, "D.GL.MODER");
      }

      if (s.contains("\ua514")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua514", Formatting.GRAY, "D.GL.MODER");
      }

      if (s.contains("\ua522")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua522", Formatting.GRAY, "D.ST.MODER");
      }

      if (s.contains("\ua561")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua561", Formatting.GOLD, "ST.HELPER");
      }

      if (s.contains("\ua545")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua545", Formatting.DARK_PURPLE, "MEDIA+");
      }

      if (s.contains("\ua513")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua513", Formatting.BLUE, "ML.MODER");
      }

      if (s.contains("\ua517")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua517", Formatting.BLUE, "MODER");
      }

      if (s.contains("\ua521")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua521", Formatting.DARK_PURPLE, "MODER+");
      }

      if (s.contains("\ua525")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua525", Formatting.BLUE, "ST.MODER");
      }

      if (s.contains("\ua529")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua529", Formatting.DARK_PURPLE, "GL.MODER");
      }

      if (s.contains("\ua557")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua557", Formatting.DARK_RED, "D.ADMIN");
      }

      if (s.contains("\ua518")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua518", Formatting.BLUE, "D.ST.MODER");
      }

      if (s.contains("\ua533")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua533", Formatting.AQUA, "ML.ADMIN");
      }

      if (s.contains("\ua537")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua537", Formatting.RED, "ADMIN");
      }

      if (s.contains("\ua501")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua501", Formatting.DARK_PURPLE, "MEDIA");
      }

      if (s.contains("\ua505")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua505", Formatting.RED, "YT");
      }

      if (s.contains("\ua549")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua549", Formatting.GOLD, "PEGAS");
      }

      if (s.contains("\ua546")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua546", Formatting.GOLD, "PEGAS");
      }

      return normalizeSmallCaps(name);
   }

   private Text getNormalizedName(PlayerEntity entity) {
      return this.normalizedNames.computeIfAbsent(entity.getUuid(), uuid -> processName(entity));
   }

   @Subscribe
   private void onRender(EventHUD e) {
      if (this.clearCacheTicker++ > 100) {
         this.normalizedNames.clear();
         this.clearCacheTicker = 0;
      }

      if (this.mc.world != null && this.mc.player != null) {
         MsdfFont font = Fonts.SFBOLD.get();
         float tickDelta = e.getRenderTickCounter().getTickDelta(true);
         if (this.entityTypes.isEnabled("\u0418\u0433\u0440\u043e\u043a\u0438")) {
            this.renderPlayerTags(font, tickDelta, e);
         }

         if (this.entityTypes.isEnabled("\u041f\u0440\u0435\u0434\u043c\u0435\u0442\u044b")) {
            this.renderItemTags(font, tickDelta, e);
         }
      }
   }

   private void renderPlayerTags(MsdfFont font, float tickDelta, EventHUD e) {
      List<AbstractClientPlayerEntity> worldPlayers = this.mc.world.getPlayers();
      Interface iface = AlphaDLC.getInstance().getModuleStorage().get(Interface.class);
      if (iface != null) {
         for (PlayerEntity entity : worldPlayers) {
            if (entity != this.mc.player || this.mc.getEntityRenderDispatcher().camera.isThirdPerson()) {
               double x = MathHelper.lerp((double)tickDelta, entity.lastRenderX, entity.getX());
               double y = MathHelper.lerp((double)tickDelta, entity.lastRenderY, entity.getY()) + (double)entity.getHeight() + 0.5;
               double z = MathHelper.lerp((double)tickDelta, entity.lastRenderZ, entity.getZ());
               Vector2f pos = ProjectionUtil.project(x, y, z);
               if (pos.getX() != Float.MAX_VALUE && pos.getY() != Float.MAX_VALUE) {
                  float screenYOffset = 5.7F;
                  float posY = pos.getY() - 5.7F;
                  Text baseName = this.getNormalizedName(entity);
                  float currentHp = Server.getHealth(entity, false);
                  int hpColor = 16733525;
                  MutableText name = baseName.copy();
                  boolean isFriend = FriendRepository.isFriend(entity.getNameForScoreboard());
                  ItemStack offHandStack = entity.getOffHandStack();
                  if (offHandStack.getItem() == Items.PLAYER_HEAD && offHandStack.getName().getString().length() <= 18) {
                     name.append(Text.literal(" - ").setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                        .append(offHandStack.getName())
                        .append(Text.literal("").setStyle(Style.EMPTY.withColor(Formatting.GRAY)));
                  }

                  name.append(Text.literal(" - ").setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                     .append(Text.literal(String.format("%.1f", currentHp)).setStyle(Style.EMPTY.withColor(hpColor)))
                     .append(Text.literal("HP").setStyle(Style.EMPTY.withColor(hpColor)));
                  float fontSize = (float)this.size.getValue();
                  float textWidth = font.getWidth(name.getString(), fontSize);
                  float paddingX = 3.0F;
                  float headIconSize = 9.5F;
                  float headIconPadding = 1.0F;
                  float friendPrefixWidth = isFriend ? font.getWidth("[F]", fontSize) + 3.0F : 0.0F;
                  float totalWidth = textWidth + paddingX * 2.0F + headIconSize + headIconPadding + friendPrefixWidth;
                  float tagHeight = 12.5F;
                  float tagY = posY - 2.0F;
                  float centerX = pos.getX();
                  float bgX = centerX - totalWidth / 2.0F;
                  Vector4f radii = new Vector4f(4.0F, 4.0F, 4.0F, 4.0F);
                  if (isFriend) {
                     if (this.blur.getValue()) {
                        DrawUtil.drawRoundBlur(bgX, tagY, totalWidth, tagHeight + 1.0F, radii, -10633362, 12.0F);
                     }

                     DrawUtil.drawRound(bgX, tagY, totalWidth, tagHeight + 1.0F, radii, ColorProvider.setAlpha(1243507514, (int)(this.alpha.getValue() * 2.55)));
                  } else {
                     if (this.blur.getValue()) {
                        DrawUtil.drawRoundBlur(bgX, tagY, totalWidth, tagHeight + 1.0F, radii, ColorProvider.rgba(200, 200, 200, 255.0F), 12.0F);
                     }

                     DrawUtil.drawRound(
                        bgX,
                        tagY,
                        totalWidth,
                        tagHeight + 1.0F,
                        radii,
                        ColorProvider.setAlpha(ColorProvider.rgba(25, 25, 25, 25.0F), (int)(this.alpha.getValue() * 2.55))
                     );
                  }

                  if (isFriend) {
                     DrawUtil.drawText(font, "[F]", bgX + paddingX, posY - 0.8F, -10633362, fontSize);
                  }

                  float headSize = 9.5F;
                  float headX = bgX + 2.8F + friendPrefixWidth;
                  float headY = tagY + (tagHeight - headSize) / 2.0F;
                  if (entity instanceof AbstractClientPlayerEntity clientPlayer) {
                     AbstractTexture skinTex = this.mc.getTextureManager().getTexture(clientPlayer.getSkinTextures().texture());
                     int texId = skinTex.getGlId();
                     Builder.texture()
                        .size(new SizeState(headSize, headSize))
                        .radius(new QuadRadiusState(1.0F))
                        .color(new QuadColorState(-1))
                        .texture(0.125F, 0.125F, 0.125F, 0.125F, texId)
                        .build()
                        .render(IRenderer.DEFAULT_MATRIX, headX, headY, 0.0F);
                  }

                  DrawUtil.drawText(font, name, bgX + paddingX + 1.0F + headIconSize + headIconPadding + friendPrefixWidth, posY - 0.25F, fontSize, 220);
                  this.equipmentCache.clear();
                  this.equipmentCache.add(entity.getEquippedStack(EquipmentSlot.HEAD));
                  this.equipmentCache.add(entity.getEquippedStack(EquipmentSlot.CHEST));
                  this.equipmentCache.add(entity.getEquippedStack(EquipmentSlot.LEGS));
                  this.equipmentCache.add(entity.getEquippedStack(EquipmentSlot.FEET));
                  this.equipmentCache.add(entity.getMainHandStack());
                  this.equipmentCache.add(entity.getOffHandStack());
                  this.equipmentCache.removeIf(ItemStack::isEmpty);
                  if (!this.equipmentCache.isEmpty()) {
                     float iconSize = 16.0F;
                     float spacing = 0.0F;
                     float itemsTotalWidth = (float)this.equipmentCache.size() * iconSize + (float)(this.equipmentCache.size() - 1) * spacing;
                     float startX = pos.getX() - itemsTotalWidth / 2.0F + 13.5F;
                     float iconY = posY - 10.0F;
                     MatrixStack matrices = e.getDrawContext().getMatrices();

                     for (int i = 0; i < this.equipmentCache.size(); i++) {
                        ItemStack stack = this.equipmentCache.get(i);
                        float x2 = startX + (float)i * (iconSize + spacing - 2.0F);
                        float scale = 0.7F;
                        float half = -18.0F;
                        matrices.push();
                        matrices.translate(x2 + half, iconY + half, 0.0F);
                        matrices.scale(scale, scale, 1.0F);
                        e.getDrawContext().drawItem(stack, (int)(-half), (int)(-half));
                        e.getDrawContext().drawStackOverlay(this.mc.textRenderer, stack, (int)(-half), (int)(-half));
                        matrices.pop();
                     }
                  }
               }
            }
         }
      }
   }

   private void renderItemTags(MsdfFont font, float tickDelta, EventHUD e) {
      MsdfFont sfBold = Fonts.SFBOLD.get();
      Interface iface = AlphaDLC.getInstance().getModuleStorage().get(Interface.class);
      if (iface != null) {
         for (Entity entity : this.mc.world.getEntities()) {
            if (entity instanceof ItemEntity) {
               ItemEntity itemEntity = (ItemEntity)entity;
               if (!itemEntity.isInvisible() && !itemEntity.hasNoGravity()) {
                  double x = MathHelper.lerp((double)tickDelta, entity.lastRenderX, entity.getX());
                  double y = MathHelper.lerp((double)tickDelta, entity.lastRenderY, entity.getY()) + (double)entity.getHeight() + 0.5;
                  double z = MathHelper.lerp((double)tickDelta, entity.lastRenderZ, entity.getZ());
                  Vector2f pos = ProjectionUtil.project(x, y, z);
                  if (pos.getX() != Float.MAX_VALUE && pos.getY() != Float.MAX_VALUE) {
                     ItemStack stack = itemEntity.getStack();
                     if (!stack.isEmpty()) {
                        int rarityOrdinal = stack.getRarity().ordinal();

                        Formatting rarityColor = switch (rarityOrdinal) {
                           case 0 -> Formatting.WHITE;
                           case 1 -> Formatting.YELLOW;
                           case 2 -> Formatting.AQUA;
                           case 3 -> Formatting.LIGHT_PURPLE;
                           default -> Formatting.WHITE;
                        };
                        String itemName = stack.getName().getString();
                        if (itemName.length() > 20) {
                           itemName = itemName.substring(0, 20);
                        }

                        Text nameText = Text.literal(itemName).setStyle(Style.EMPTY.withColor(rarityColor));
                        if (!stack.getName().getSiblings().isEmpty()) {
                           String siblingName = stack.getName().getString();
                           if (siblingName.length() > 20) {
                              siblingName = siblingName.substring(0, 20);
                           }

                           nameText = Text.literal(siblingName).setStyle(stack.getName().getStyle());
                        }

                        Text countComponent = stack.getCount() > 1
                           ? Text.literal(" - ")
                              .setStyle(Style.EMPTY.withColor(Formatting.GRAY))
                              .append(Text.literal(stack.getCount() + "x").setStyle(Style.EMPTY.withColor(Formatting.RED)))
                           : Text.empty();
                        Text textComponent = nameText.copy().append(countComponent);
                        Text normalized = normalizeSmallCaps(textComponent);
                        float textWidth = sfBold.getWidth(normalized.getString(), (float)this.size.getValue());
                        float totalWidth = textWidth + 4.0F;
                        float bgX = pos.getX() - totalWidth / 2.0F;
                        Vector4f radii = new Vector4f(3.0F, 3.0F, 3.0F, 3.0F);
                        if (this.blur.getValue()) {
                           DrawUtil.drawRoundBlur(
                              bgX, pos.getY() - 2.0F, totalWidth - 1.0F, 11.0F, radii, ColorProvider.rgba(200, 200, 200, 255.0F), 12.0F
                           );
                        }

                        DrawUtil.drawRound(
                           bgX,
                           pos.getY() - 2.0F,
                           totalWidth - 1.0F,
                           11.0F,
                           radii,
                           ColorProvider.setAlpha(ColorProvider.rgba(25, 25, 25, 25.0F), (int)(this.alpha.getValue() * 2.55))
                        );
                        DrawUtil.drawText(sfBold, normalized, bgX + 2.0F, pos.getY() - 2.0F, (float)this.size.getValue(), 255);
                     }
                  }
               }
            }
         }
      }
   }
}
