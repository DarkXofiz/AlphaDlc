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
      Text name = entity.method_5476();
      MinecraftClient mc = MinecraftClient.method_1551();
      NameProtect nameProtect = AlphaDLC.getInstance().getModuleStorage().get(NameProtect.class);
      if (nameProtect != null && nameProtect.isEnabled() && mc.field_1724 != null) {
         String scoreName = entity.method_5820();
         String myName = mc.field_1724.method_5820();
         String customName = nameProtect.getCustomName();
         if (scoreName.equals(myName) || entity.method_5667().equals(mc.field_1724.method_5667())) {
            name = ReplaceUtil.replace(name, myName, customName);
         } else if (nameProtect.hideFriends.getValue() && FriendRepository.isFriend(scoreName)) {
            name = ReplaceUtil.replace(name, scoreName, customName);
         }
      }

      String s = name.getString();
      if (s.contains("\ua500")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua500", Formatting.field_1080, "\u0418\u0413\u0420\u041e\u041a");
      }

      if (s.contains("\ua504")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua504", Formatting.field_1078, "HERO");
      }

      if (s.contains("\ua508")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua508", Formatting.field_1065, "TITAN");
      }

      if (s.contains("\ua512")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua512", Formatting.field_1060, "AVENGER");
      }

      if (s.contains("\ua516")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua516", Formatting.field_1075, "OVERLORD");
      }

      if (s.contains("\ua520")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua520", Formatting.field_1065, "MAGISTER");
      }

      if (s.contains("\ua524")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua524", Formatting.field_1061, "IMPERATOR");
      }

      if (s.contains("\ua532")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua532", Formatting.field_1064, "BULL");
      }

      if (s.contains("\ua553")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua553", Formatting.field_1063, "GHOST");
      }

      if (s.contains("\ua528")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua528", Formatting.field_1076, "DRAGON");
      }

      if (s.contains("\ua502")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua502", Formatting.field_1065, "GOD");
      }

      if (s.contains("\ua538")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua538", Formatting.field_1065, "GOD");
      }

      if (s.contains("\ua526")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua526", Formatting.field_1078, "D.ML.ADMIN");
      }

      if (s.contains("\ua540")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua540", Formatting.field_1077, "HYDRA");
      }

      if (s.contains("\ua556")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua556", Formatting.field_1080, "BUNNY");
      }

      if (s.contains("\ua552")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua552", Formatting.field_1068, "RABBIT");
      }

      if (s.contains("\ua548")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua548", Formatting.field_1060, "COBRA");
      }

      if (s.contains("\ua536")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua536", Formatting.field_1065, "TIGER");
      }

      if (s.contains("\ua560")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua560", Formatting.field_1054, "D.HELPER");
      }

      if (s.contains("\ua509")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua509", Formatting.field_1054, "HELPER");
      }

      if (s.contains("\ua506")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua506", Formatting.field_1080, "D.MODER");
      }

      if (s.contains("\ua544")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua544", Formatting.field_1079, "VAMPIRE");
      }

      if (s.contains("\ua530")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua530", Formatting.field_1080, "D.ML.ADMIN");
      }

      if (s.contains("\ua510")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua510", Formatting.field_1058, "D.GL.MODER");
      }

      if (s.contains("\ua514")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua514", Formatting.field_1080, "D.GL.MODER");
      }

      if (s.contains("\ua522")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua522", Formatting.field_1080, "D.ST.MODER");
      }

      if (s.contains("\ua561")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua561", Formatting.field_1065, "ST.HELPER");
      }

      if (s.contains("\ua545")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua545", Formatting.field_1064, "MEDIA+");
      }

      if (s.contains("\ua513")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua513", Formatting.field_1078, "ML.MODER");
      }

      if (s.contains("\ua517")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua517", Formatting.field_1078, "MODER");
      }

      if (s.contains("\ua521")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua521", Formatting.field_1064, "MODER+");
      }

      if (s.contains("\ua525")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua525", Formatting.field_1078, "ST.MODER");
      }

      if (s.contains("\ua529")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua529", Formatting.field_1064, "GL.MODER");
      }

      if (s.contains("\ua557")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua557", Formatting.field_1079, "D.ADMIN");
      }

      if (s.contains("\ua518")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua518", Formatting.field_1078, "D.ST.MODER");
      }

      if (s.contains("\ua533")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua533", Formatting.field_1075, "ML.ADMIN");
      }

      if (s.contains("\ua537")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua537", Formatting.field_1061, "ADMIN");
      }

      if (s.contains("\ua501")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua501", Formatting.field_1064, "MEDIA");
      }

      if (s.contains("\ua505")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua505", Formatting.field_1061, "YT");
      }

      if (s.contains("\ua549")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua549", Formatting.field_1065, "PEGAS");
      }

      if (s.contains("\ua546")) {
         name = ReplaceUtil.replaceWithFormatted(name, "\ua546", Formatting.field_1065, "PEGAS");
      }

      return normalizeSmallCaps(name);
   }

   private Text getNormalizedName(PlayerEntity entity) {
      return this.normalizedNames.computeIfAbsent(entity.method_5667(), uuid -> processName(entity));
   }

   @Subscribe
   private void onRender(EventHUD e) {
      if (this.clearCacheTicker++ > 100) {
         this.normalizedNames.clear();
         this.clearCacheTicker = 0;
      }

      if (this.mc.field_1687 != null && this.mc.field_1724 != null) {
         MsdfFont font = Fonts.SFBOLD.get();
         float tickDelta = e.getRenderTickCounter().method_60637(true);
         if (this.entityTypes.isEnabled("\u0418\u0433\u0440\u043e\u043a\u0438")) {
            this.renderPlayerTags(font, tickDelta, e);
         }

         if (this.entityTypes.isEnabled("\u041f\u0440\u0435\u0434\u043c\u0435\u0442\u044b")) {
            this.renderItemTags(font, tickDelta, e);
         }
      }
   }

   private void renderPlayerTags(MsdfFont font, float tickDelta, EventHUD e) {
      List<AbstractClientPlayerEntity> worldPlayers = this.mc.field_1687.method_18456();
      Interface iface = AlphaDLC.getInstance().getModuleStorage().get(Interface.class);
      if (iface != null) {
         for (PlayerEntity entity : worldPlayers) {
            if (entity != this.mc.field_1724 || this.mc.method_1561().field_4686.method_19333()) {
               double x = MathHelper.method_16436((double)tickDelta, entity.field_6038, entity.method_23317());
               double y = MathHelper.method_16436((double)tickDelta, entity.field_5971, entity.method_23318()) + (double)entity.method_17682() + 0.5;
               double z = MathHelper.method_16436((double)tickDelta, entity.field_5989, entity.method_23321());
               Vector2f pos = ProjectionUtil.project(x, y, z);
               if (pos.method_32118() != Float.MAX_VALUE && pos.method_32119() != Float.MAX_VALUE) {
                  float screenYOffset = 5.7F;
                  float posY = pos.method_32119() - 5.7F;
                  Text baseName = this.getNormalizedName(entity);
                  float currentHp = Server.getHealth(entity, false);
                  int hpColor = 16733525;
                  MutableText name = baseName.method_27661();
                  boolean isFriend = FriendRepository.isFriend(entity.method_5820());
                  ItemStack offHandStack = entity.method_6079();
                  if (offHandStack.method_7909() == Items.field_8575 && offHandStack.method_7964().getString().length() <= 18) {
                     name.method_10852(Text.method_43470(" - ").method_10862(Style.field_24360.method_10977(Formatting.field_1080)))
                        .method_10852(offHandStack.method_7964())
                        .method_10852(Text.method_43470("").method_10862(Style.field_24360.method_10977(Formatting.field_1080)));
                  }

                  name.method_10852(Text.method_43470(" - ").method_10862(Style.field_24360.method_10977(Formatting.field_1080)))
                     .method_10852(Text.method_43470(String.format("%.1f", currentHp)).method_10862(Style.field_24360.method_36139(hpColor)))
                     .method_10852(Text.method_43470("HP").method_10862(Style.field_24360.method_36139(hpColor)));
                  float fontSize = (float)this.size.getValue();
                  float textWidth = font.getWidth(name.getString(), fontSize);
                  float paddingX = 3.0F;
                  float headIconSize = 9.5F;
                  float headIconPadding = 1.0F;
                  float friendPrefixWidth = isFriend ? font.getWidth("[F]", fontSize) + 3.0F : 0.0F;
                  float totalWidth = textWidth + paddingX * 2.0F + headIconSize + headIconPadding + friendPrefixWidth;
                  float tagHeight = 12.5F;
                  float tagY = posY - 2.0F;
                  float centerX = pos.method_32118();
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
                     AbstractTexture skinTex = this.mc.method_1531().method_4619(clientPlayer.method_52814().comp_1626());
                     int texId = skinTex.method_4624();
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
                  this.equipmentCache.add(entity.method_6118(EquipmentSlot.field_6169));
                  this.equipmentCache.add(entity.method_6118(EquipmentSlot.field_6174));
                  this.equipmentCache.add(entity.method_6118(EquipmentSlot.field_6172));
                  this.equipmentCache.add(entity.method_6118(EquipmentSlot.field_6166));
                  this.equipmentCache.add(entity.method_6047());
                  this.equipmentCache.add(entity.method_6079());
                  this.equipmentCache.removeIf(ItemStack::method_7960);
                  if (!this.equipmentCache.isEmpty()) {
                     float iconSize = 16.0F;
                     float spacing = 0.0F;
                     float itemsTotalWidth = (float)this.equipmentCache.size() * iconSize + (float)(this.equipmentCache.size() - 1) * spacing;
                     float startX = pos.method_32118() - itemsTotalWidth / 2.0F + 13.5F;
                     float iconY = posY - 10.0F;
                     MatrixStack matrices = e.getDrawContext().method_51448();

                     for (int i = 0; i < this.equipmentCache.size(); i++) {
                        ItemStack stack = this.equipmentCache.get(i);
                        float x2 = startX + (float)i * (iconSize + spacing - 2.0F);
                        float scale = 0.7F;
                        float half = -18.0F;
                        matrices.method_22903();
                        matrices.method_46416(x2 + half, iconY + half, 0.0F);
                        matrices.method_22905(scale, scale, 1.0F);
                        e.getDrawContext().method_51427(stack, (int)(-half), (int)(-half));
                        e.getDrawContext().method_51431(this.mc.field_1772, stack, (int)(-half), (int)(-half));
                        matrices.method_22909();
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
         for (Entity entity : this.mc.field_1687.method_18112()) {
            if (entity instanceof ItemEntity) {
               ItemEntity itemEntity = (ItemEntity)entity;
               if (!itemEntity.method_5767() && !itemEntity.method_5740()) {
                  double x = MathHelper.method_16436((double)tickDelta, entity.field_6038, entity.method_23317());
                  double y = MathHelper.method_16436((double)tickDelta, entity.field_5971, entity.method_23318()) + (double)entity.method_17682() + 0.5;
                  double z = MathHelper.method_16436((double)tickDelta, entity.field_5989, entity.method_23321());
                  Vector2f pos = ProjectionUtil.project(x, y, z);
                  if (pos.method_32118() != Float.MAX_VALUE && pos.method_32119() != Float.MAX_VALUE) {
                     ItemStack stack = itemEntity.method_6983();
                     if (!stack.method_7960()) {
                        int rarityOrdinal = stack.method_7932().ordinal();

                        Formatting rarityColor = switch (rarityOrdinal) {
                           case 0 -> Formatting.field_1068;
                           case 1 -> Formatting.field_1054;
                           case 2 -> Formatting.field_1075;
                           case 3 -> Formatting.field_1076;
                           default -> Formatting.field_1068;
                        };
                        String itemName = stack.method_7964().getString();
                        if (itemName.length() > 20) {
                           itemName = itemName.substring(0, 20);
                        }

                        Text nameText = Text.method_43470(itemName).method_10862(Style.field_24360.method_10977(rarityColor));
                        if (!stack.method_7964().method_10855().isEmpty()) {
                           String siblingName = stack.method_7964().getString();
                           if (siblingName.length() > 20) {
                              siblingName = siblingName.substring(0, 20);
                           }

                           nameText = Text.method_43470(siblingName).method_10862(stack.method_7964().method_10866());
                        }

                        Text countComponent = stack.method_7947() > 1
                           ? Text.method_43470(" - ")
                              .method_10862(Style.field_24360.method_10977(Formatting.field_1080))
                              .method_10852(Text.method_43470(stack.method_7947() + "x").method_10862(Style.field_24360.method_10977(Formatting.field_1061)))
                           : Text.method_43473();
                        Text textComponent = nameText.method_27661().method_10852(countComponent);
                        Text normalized = normalizeSmallCaps(textComponent);
                        float textWidth = sfBold.getWidth(normalized.getString(), (float)this.size.getValue());
                        float totalWidth = textWidth + 4.0F;
                        float bgX = pos.method_32118() - totalWidth / 2.0F;
                        Vector4f radii = new Vector4f(3.0F, 3.0F, 3.0F, 3.0F);
                        if (this.blur.getValue()) {
                           DrawUtil.drawRoundBlur(
                              bgX, pos.method_32119() - 2.0F, totalWidth - 1.0F, 11.0F, radii, ColorProvider.rgba(200, 200, 200, 255.0F), 12.0F
                           );
                        }

                        DrawUtil.drawRound(
                           bgX,
                           pos.method_32119() - 2.0F,
                           totalWidth - 1.0F,
                           11.0F,
                           radii,
                           ColorProvider.setAlpha(ColorProvider.rgba(25, 25, 25, 25.0F), (int)(this.alpha.getValue() * 2.55))
                        );
                        DrawUtil.drawText(sfBold, normalized, bgX + 2.0F, pos.method_32119() - 2.0F, (float)this.size.getValue(), 255);
                     }
                  }
               }
            }
         }
      }
   }
}
