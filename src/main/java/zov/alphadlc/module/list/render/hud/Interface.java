package zov.alphadlc.module.list.render.hud;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.CooldownUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.GameMode;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import zov.alphadlc.AlphaDLC;
import zov.alphadlc.event.list.EventHUD;
import zov.alphadlc.event.list.EventPacket;
import zov.alphadlc.event.list.EventPopTotem;
import zov.alphadlc.event.list.EventTick;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.module.list.combat.KillAura;
import zov.alphadlc.module.list.misc.NameProtect;
import zov.alphadlc.module.list.player.ServerHelper;
import zov.alphadlc.module.list.render.NameTags;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.module.settings.ModeListSetting;
import zov.alphadlc.module.settings.Setting;
import zov.alphadlc.module.settings.SliderSetting;
import zov.alphadlc.util.base.Instance;
import zov.alphadlc.util.draggable.DragManager;
import zov.alphadlc.util.draggable.Draggable;
import zov.alphadlc.util.keyboard.KeyStorage;
import zov.alphadlc.util.math.Counter;
import zov.alphadlc.util.render.builders.Builder;
import zov.alphadlc.util.render.builders.states.QuadColorState;
import zov.alphadlc.util.render.builders.states.QuadRadiusState;
import zov.alphadlc.util.render.builders.states.SizeState;
import zov.alphadlc.util.render.helper.HoverUtil;
import zov.alphadlc.util.render.math.Animation;
import zov.alphadlc.util.render.math.Easing;
import zov.alphadlc.util.render.msdf.Fonts;
import zov.alphadlc.util.render.msdf.MsdfFont;
import zov.alphadlc.util.render.providers.ColorProvider;
import zov.alphadlc.util.render.renderers.DrawUtil;
import zov.alphadlc.util.render.renderers.IRenderer;
import zov.alphadlc.util.render.renderers.impl.BuiltTexture;
import zov.alphadlc.util.render.renderers.impl.HudShine;
import zov.alphadlc.util.render.timer.TimerTextAnimator;
import zov.alphadlc.util.replace.ReplaceUtil;
import zov.alphadlc.util.server.Server;
import zov.alphadlc.util.staff.StaffManager;

@ModuleInformation(
   moduleName = "Interface",
   moduleDesc = "\u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0430 \u044d\u043b\u0435\u043c\u0435\u043d\u0442\u043e\u0432 HUD \u043d\u0430 \u044d\u043a\u0440\u0430\u043d\u0435",
   moduleCategory = ModuleCategory.RENDER
)
public class Interface extends Module {
   private static final Identifier TARGET_HUD_GLOW_TEXTURE = Identifier.method_60655("mre", "images/glow.png");
   private final ModeListSetting elements = new ModeListSetting(
      "\u042d\u043b\u0435\u043c\u0435\u043d\u0442\u044b",
      new BooleanSetting("\u0412\u0430\u0442\u0435\u0440\u043c\u0430\u0440\u043a\u0430", true),
      new BooleanSetting("\u0418\u043d\u0444\u043e", true),
      new BooleanSetting("\u041a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u044b", false),
      new BooleanSetting("\u0410\u043a\u0442\u0438\u0432\u043d\u044b\u0439 \u0442\u0430\u0440\u0433\u0435\u0442", true),
      new BooleanSetting("\u041f\u0440\u0438\u0432\u044f\u0437\u0430\u043d\u043d\u044b\u0435 \u043c\u043e\u0434\u0443\u043b\u0438", true),
      new BooleanSetting("\u0410\u043a\u0442\u0438\u0432\u043d\u044b\u0435 \u043c\u043e\u0434\u0435\u0440\u0430\u0442\u043e\u0440\u044b", true),
      new BooleanSetting("\u0411\u0430\u0444\u044b", true),
      new BooleanSetting("\u041a\u0443\u043b\u0414\u0430\u0443\u043d\u044b", true),
      new BooleanSetting("ServerHelper", true),
      new BooleanSetting("\u041d\u043e\u0442\u0438\u0444\u0438\u043a\u0430\u0446\u0438\u0438", true),
      new BooleanSetting("\u041a\u0430\u0441\u0442\u043e\u043c\u043d\u044b\u0439 \u0445\u043e\u0442\u0431\u0430\u0440", false),
      new BooleanSetting("\u0411\u0440\u043e\u043d\u044f", false),
      new BooleanSetting("\u041f\u043e\u043b\u043e\u0441\u0430 \u0442\u043e\u0442\u0435\u043c\u043e\u0432", false),
      new BooleanSetting("\u0411\u043b\u044e\u0440 \u0444\u043e\u043d\u0430", true)
   );
   private final SliderSetting backgroundIntensity = new SliderSetting(
      "\u0418\u043d\u0442\u0435\u043d\u0441\u0438\u0432\u043d\u043e\u0441\u0442\u044c \u0444\u043e\u043d\u0430", 0.5, 0.05F, 1.0, 0.01F
   );
   private final SliderSetting headerIntensity = new SliderSetting(
      "\u0418\u043d\u0442\u0435\u043d\u0441\u0438\u0432\u043d\u043e\u0441\u0442\u044c \u0437\u0430\u0433\u043e\u043b\u043e\u0432\u043a\u043e\u0432",
      0.5,
      0.05F,
      1.0,
      0.01F
   );
   private final SliderSetting itemIntensity = new SliderSetting(
      "\u0418\u043d\u0442\u0435\u043d\u0441\u0438\u0432\u043d\u043e\u0441\u0442\u044c \u044d\u043b\u0435\u043c\u0435\u043d\u0442\u043e\u0432",
      0.2F,
      0.05F,
      1.0,
      0.01F
   );
   private final SliderSetting lowHpAlertThreshold = new SliderSetting(
      "\u041f\u043e\u0440\u043e\u0433 \u0425\u041f \u043e\u043f\u043e\u0432\u0435\u0449\u0435\u043d\u0438\u044f", 8.0, 1.0, 20.0, 0.5
   );
   private final Draggable watermarkDrag = DragManager.installDrag(this, "Watermark", 4.0F, 4.0F);
   private final Draggable infoDrag = DragManager.installDrag(this, "Info", 4.0F, 24.0F);
   private final Draggable keyBindsDrag = DragManager.installDrag(this, "HotKeys", 100.0F, 50.0F);
   private final Draggable staffListDrag = DragManager.installDrag(this, "StaffList", 200.0F, 50.0F);
   private final Draggable potionsDrag = DragManager.installDrag(this, "Potions", 300.0F, 50.0F);
   private final Draggable cooldownsDrag = DragManager.installDrag(this, "CoolDowns", 300.0F, 130.0F);
   private final Draggable serverHelperDrag = DragManager.installDrag(this, "ServerHelper", 300.0F, 200.0F);
   private final Draggable targetHUDDrag = DragManager.installDrag(this, "TargetHUD", 130.0F, 130.0F);
   private final Draggable hotbarDrag = DragManager.installDrag(this, "CustomHotbar", 150.0F, 220.0F);
   private final Draggable armourDrag = DragManager.installDrag(this, "ArmourBar", 150.0F, 245.0F);
   private final Draggable totemBarDrag = DragManager.installDrag(this, "TotemBar", 150.0F, 270.0F);
   public final NotificationsElement notifications = new NotificationsElement();
   private final Animation animation = new Animation(Easing.EXPO_OUT, 300L);
   private final Animation armorAnim = new Animation(Easing.EXPO_OUT, 300L);
   private final Animation hpAnimation = new Animation(Easing.EXPO_OUT, 600L);
   private final Animation outdatedHpAnimation = new Animation(Easing.EXPO_OUT, 1200L);
   private final Animation absorptionAnimation = new Animation(Easing.EXPO_OUT, 300L);
   private final Animation absorptionTrailAnimation = new Animation(Easing.EXPO_OUT, 1200L);
   private float lastHealthVal = 0.0F;
   private long lastTime = System.currentTimeMillis();
   private Entity lastTarget;
   private float lastHpPercent = -1.0F;
   private final Animation alpha = new Animation(Easing.EXPO_OUT, 200L);
   private final Animation lowHpAlertAnimation = new Animation(Easing.EXPO_OUT, 300L);
   private final List<Interface.BindEntry> keybindEntries = new ArrayList<>();
   private final List<Interface.Staff> staffPlayers = new ArrayList<>();
   private final Pattern namePattern = Pattern.compile("^\\w{3,16}$");
   private final Pattern prefixMatches = Pattern.compile(
      ".*(\ua537|\ua533|\ua529|\ua525|\ua521|\ua517|\ua513|\\bmod\\b|\\badm\\b|\\bhelp\\b|\\bwne\\b|\u043c\u043e\u0434\u0435\u0440|\u0445\u0435\u043b\u043f|\u043f\u043e\u043c\u043e\u0449|\u0430\u0434\u043c\u0438\u043d|\u0432\u043b\u0430\u0434\u0435\u043b|\u043e\u0442\u0440\u0438\u0446|\\btaf\\b|\\bcurat\\b|\u043a\u0443\u0440\u0430\u0442\u043e\u0440|\\bdev\\b|\u0440\u0430\u0437\u0440\u0430\u0431|\\bsupp\\b|\u0441\u0430\u043f\u043f\u043e\u0440\u0442|\\byt\\b|\\[yt\\]|\u044e\u0442\u0443\u0431|\u0441\u0442\u0430\u0436\u0435\u0440|\u0441\u043e\u0442\u0440\u0443\u0434\u043d\u0438\u043a).*"
   );
   private static final float HB_PAD = 3.0F;
   private static final float HB_CELL = 20.0F;
   private static final float HB_ICON = 16.0F;
   private static final float HB_OFFHAND_GAP = 4.0F;
   private final Animation alpha2 = new Animation(Easing.EXPO_OUT, 200L);
   private final Animation alpha3 = new Animation(Easing.EXPO_OUT, 200L);
   private static final float POTION_RING_DIAMETER = 8.0F;
   private static final float POTION_RING_GAP = 3.0F;
   private static final float POTION_RING_THICKNESS = 1.4F;
   private final BooleanSetting potionsBlur = new BooleanSetting("\u0411\u043b\u044e\u0440 \u0431\u0430\u0444\u043e\u0432", true).setVisible(() -> false);
   private final SliderSetting potionsSize = new SliderSetting("\u0420\u0430\u0437\u043c\u0435\u0440 \u0431\u0430\u0444\u043e\u0432", 1.0, 0.5, 2.0, 0.05)
      .setVisible(() -> false);
   private final SliderSetting potionsAlpha = new SliderSetting(
         "\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c \u0431\u0430\u0444\u043e\u0432", 180.0, 0.0, 255.0, 1.0
      )
      .setVisible(() -> false);
   private final BooleanSetting potionsRing = new BooleanSetting("\u041a\u043e\u043b\u044c\u0446\u043e \u0442\u0430\u0439\u043c\u0435\u0440", true)
      .setVisible(() -> false);
   private final BooleanSetting potionsShine = new BooleanSetting("\u0411\u043b\u0438\u043a \u0431\u0430\u0444\u043e\u0432", true).setVisible(() -> false);
   private final SliderSetting potionsShineAlpha = new SliderSetting(
         "\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c \u0431\u043b\u0438\u043a\u0430", 30.0, 0.0, 100.0, 1.0
      )
      .setVisible(() -> false);
   private final SliderSetting potionsShineThickness = new SliderSetting(
         "\u0422\u043e\u043b\u0449\u0438\u043d\u0430 \u0431\u043b\u0438\u043a\u0430", 0.5, 0.2, 2.0, 0.05
      )
      .setVisible(() -> false);
   private final BooleanSetting potionsCorners = new BooleanSetting("\u0423\u0433\u043e\u043b\u043a\u0438 \u0431\u0430\u0444\u043e\u0432", true)
      .setVisible(() -> false);
   private final BooleanSetting keybindsBlur = new BooleanSetting("\u0411\u043b\u044e\u0440 \u043a\u0435\u0439\u0431\u0438\u043d\u0434\u043e\u0432", true)
      .setVisible(() -> false);
   private final SliderSetting keybindsSize = new SliderSetting(
         "\u0420\u0430\u0437\u043c\u0435\u0440 \u043a\u0435\u0439\u0431\u0438\u043d\u0434\u043e\u0432", 1.0, 0.5, 2.0, 0.05
      )
      .setVisible(() -> false);
   private final SliderSetting keybindsAlpha = new SliderSetting(
         "\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c \u043a\u0435\u0439\u0431\u0438\u043d\u0434\u043e\u0432",
         180.0,
         0.0,
         255.0,
         1.0
      )
      .setVisible(() -> false);
   private final BooleanSetting keybindsShine = new BooleanSetting("\u0411\u043b\u0438\u043a \u043a\u0435\u0439\u0431\u0438\u043d\u0434\u043e\u0432", true)
      .setVisible(() -> false);
   private final SliderSetting keybindsShineAlpha = new SliderSetting(
         "\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c \u0431\u043b\u0438\u043a\u0430 \u043a\u0431", 30.0, 0.0, 100.0, 1.0
      )
      .setVisible(() -> false);
   private final SliderSetting keybindsShineThickness = new SliderSetting(
         "\u0422\u043e\u043b\u0449\u0438\u043d\u0430 \u0431\u043b\u0438\u043a\u0430 \u043a\u0431", 0.5, 0.2, 2.0, 0.05
      )
      .setVisible(() -> false);
   private final BooleanSetting keybindsCorners = new BooleanSetting(
         "\u0423\u0433\u043e\u043b\u043a\u0438 \u043a\u0435\u0439\u0431\u0438\u043d\u0434\u043e\u0432", true
      )
      .setVisible(() -> false);
   private final BooleanSetting stafflistBlur = new BooleanSetting("\u0411\u043b\u044e\u0440 \u0441\u0442\u0430\u0444\u0444", true).setVisible(() -> false);
   private final SliderSetting stafflistSize = new SliderSetting("\u0420\u0430\u0437\u043c\u0435\u0440 \u0441\u0442\u0430\u0444\u0444", 1.0, 0.5, 2.0, 0.05)
      .setVisible(() -> false);
   private final SliderSetting stafflistAlpha = new SliderSetting(
         "\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c \u0441\u0442\u0430\u0444\u0444", 180.0, 0.0, 255.0, 1.0
      )
      .setVisible(() -> false);
   private final BooleanSetting stafflistShine = new BooleanSetting("\u0411\u043b\u0438\u043a \u0441\u0442\u0430\u0444\u0444", true).setVisible(() -> false);
   private final SliderSetting stafflistShineAlpha = new SliderSetting(
         "\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c \u0431\u043b\u0438\u043a\u0430 \u0441\u0442\u0430\u0444\u0444",
         30.0,
         0.0,
         100.0,
         1.0
      )
      .setVisible(() -> false);
   private final SliderSetting stafflistShineThickness = new SliderSetting(
         "\u0422\u043e\u043b\u0449\u0438\u043d\u0430 \u0431\u043b\u0438\u043a\u0430 \u0441\u0442\u0430\u0444\u0444", 0.5, 0.2, 2.0, 0.05
      )
      .setVisible(() -> false);
   private final BooleanSetting stafflistCorners = new BooleanSetting(
         "\u0423\u0433\u043e\u043b\u043a\u0438 \u0441\u043f\u0438\u0441\u043a\u0430 \u043c\u043e\u0434\u0435\u0440\u0430\u0442\u043e\u0440\u043e\u0432", true
      )
      .setVisible(() -> false);
   private final BooleanSetting cooldownsBlur = new BooleanSetting("\u0411\u043b\u044e\u0440 \u043a\u0434", true).setVisible(() -> false);
   private final SliderSetting cooldownsSize = new SliderSetting("\u0420\u0430\u0437\u043c\u0435\u0440 \u043a\u0434", 1.0, 0.5, 2.0, 0.05)
      .setVisible(() -> false);
   private final SliderSetting cooldownsAlpha = new SliderSetting(
         "\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c \u043a\u0434", 180.0, 0.0, 255.0, 1.0
      )
      .setVisible(() -> false);
   private final BooleanSetting cooldownsRing = new BooleanSetting(
         "\u041a\u043e\u043b\u044c\u0446\u043e \u0442\u0430\u0439\u043c\u0435\u0440 \u043a\u0434", true
      )
      .setVisible(() -> false);
   private final BooleanSetting cooldownsShine = new BooleanSetting("\u0411\u043b\u0438\u043a \u043a\u0434", true).setVisible(() -> false);
   private final SliderSetting cooldownsShineAlpha = new SliderSetting(
         "\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c \u0431\u043b\u0438\u043a\u0430 \u043a\u0434", 30.0, 0.0, 100.0, 1.0
      )
      .setVisible(() -> false);
   private final SliderSetting cooldownsShineThickness = new SliderSetting(
         "\u0422\u043e\u043b\u0449\u0438\u043d\u0430 \u0431\u043b\u0438\u043a\u0430 \u043a\u0434", 0.5, 0.2, 2.0, 0.05
      )
      .setVisible(() -> false);
   private final BooleanSetting cooldownsCorners = new BooleanSetting(
         "\u0423\u0433\u043e\u043b\u043a\u0438 \u043a\u0443\u043b\u0434\u0430\u0443\u043d\u043e\u0432", true
      )
      .setVisible(() -> false);
   private final Interface.HudPopup potionsPopup = new Interface.HudPopup(
      "Potions",
      this.potionsDrag,
      this.potionsBlur,
      this.potionsSize,
      this.potionsAlpha,
      this.potionsRing,
      this.potionsShine,
      this.potionsShineAlpha,
      this.potionsShineThickness,
      this.potionsCorners
   );
   private final Interface.HudPopup cooldownsPopup = new Interface.HudPopup(
      "CoolDowns",
      this.cooldownsDrag,
      this.cooldownsBlur,
      this.cooldownsSize,
      this.cooldownsAlpha,
      this.cooldownsRing,
      this.cooldownsShine,
      this.cooldownsShineAlpha,
      this.cooldownsShineThickness,
      this.cooldownsCorners
   );
   private final BooleanSetting serverHelperBlur = new BooleanSetting("\u0411\u043b\u044e\u0440 ServerHelper", true).setVisible(() -> false);
   private final SliderSetting serverHelperSize = new SliderSetting("\u0420\u0430\u0437\u043c\u0435\u0440 ServerHelper", 1.0, 0.5, 2.0, 0.05)
      .setVisible(() -> false);
   private final SliderSetting serverHelperAlpha = new SliderSetting(
         "\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c ServerHelper", 180.0, 0.0, 255.0, 1.0
      )
      .setVisible(() -> false);
   private final BooleanSetting serverHelperShine = new BooleanSetting("\u0411\u043b\u0438\u043a ServerHelper", true).setVisible(() -> false);
   private final SliderSetting serverHelperShineAlpha = new SliderSetting(
         "\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c \u0431\u043b\u0438\u043a\u0430 ServerHelper", 30.0, 0.0, 100.0, 1.0
      )
      .setVisible(() -> false);
   private final SliderSetting serverHelperShineThickness = new SliderSetting(
         "\u0422\u043e\u043b\u0449\u0438\u043d\u0430 \u0431\u043b\u0438\u043a\u0430 ServerHelper", 0.5, 0.2, 2.0, 0.05
      )
      .setVisible(() -> false);
   private final BooleanSetting serverHelperCorners = new BooleanSetting("\u0423\u0433\u043e\u043b\u043a\u0438 ServerHelper", true).setVisible(() -> false);
   private final Interface.HudPopup serverHelperPopup = new Interface.HudPopup(
      "ServerHelper",
      this.serverHelperDrag,
      this.serverHelperBlur,
      this.serverHelperSize,
      this.serverHelperAlpha,
      null,
      this.serverHelperShine,
      this.serverHelperShineAlpha,
      this.serverHelperShineThickness,
      this.serverHelperCorners
   );
   private final BooleanSetting wmBlur = new BooleanSetting("\u0411\u043b\u044e\u0440 \u0432\u043c", true).setVisible(() -> false);
   private final SliderSetting wmSize = new SliderSetting("\u0420\u0430\u0437\u043c\u0435\u0440 \u0432\u043c", 1.05, 0.5, 2.0, 0.05).setVisible(() -> false);
   private final SliderSetting wmAlpha = new SliderSetting(
         "\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c \u0432\u043c", 180.0, 0.0, 255.0, 1.0
      )
      .setVisible(() -> false);
   private final BooleanSetting wmShine = new BooleanSetting("\u0411\u043b\u0438\u043a \u0432\u043c", true).setVisible(() -> false);
   private final SliderSetting wmShineAlpha = new SliderSetting(
         "\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c \u0431\u043b\u0438\u043a\u0430 \u0432\u043c", 30.0, 0.0, 100.0, 1.0
      )
      .setVisible(() -> false);
   private final SliderSetting wmShineThickness = new SliderSetting(
         "\u0422\u043e\u043b\u0449\u0438\u043d\u0430 \u0431\u043b\u0438\u043a\u0430 \u0432\u043c", 0.5, 0.2, 2.0, 0.05
      )
      .setVisible(() -> false);
   private final BooleanSetting wmCorners = new BooleanSetting(
         "\u0423\u0433\u043e\u043b\u043a\u0438 \u0432\u0430\u0442\u0435\u0440\u043c\u0430\u0440\u043a\u0438", true
      )
      .setVisible(() -> false);
   private final BooleanSetting wmBps = new BooleanSetting("\u0414\u043e\u0431\u0430\u0432\u0438\u0442\u044c BPS", false).setVisible(() -> false);
   private final BooleanSetting wmTps = new BooleanSetting("\u0414\u043e\u0431\u0430\u0432\u0438\u0442\u044c TPS", false).setVisible(() -> false);
   private final BooleanSetting wmCoords = new BooleanSetting(
         "\u0414\u043e\u0431\u0430\u0432\u0438\u0442\u044c \u043a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u044b", false
      )
      .setVisible(() -> false);
   private final Interface.HudPopup watermarkPopup = new Interface.HudPopup(
      "Watermark", this.watermarkDrag, this.wmBlur, this.wmSize, this.wmAlpha, null, this.wmShine, this.wmShineAlpha, this.wmShineThickness, this.wmCorners
   );
   private final BooleanSetting infoBlur = new BooleanSetting("\u0411\u043b\u044e\u0440 \u0438\u043d\u0444\u043e", true).setVisible(() -> false);
   private final SliderSetting infoSize = new SliderSetting("\u0420\u0430\u0437\u043c\u0435\u0440 \u0438\u043d\u0444\u043e", 1.05, 0.5, 2.0, 0.05)
      .setVisible(() -> false);
   private final SliderSetting infoAlpha = new SliderSetting(
         "\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c \u0438\u043d\u0444\u043e", 180.0, 0.0, 255.0, 1.0
      )
      .setVisible(() -> false);
   private final BooleanSetting infoShine = new BooleanSetting("\u0411\u043b\u0438\u043a \u0438\u043d\u0444\u043e", true).setVisible(() -> false);
   private final SliderSetting infoShineAlpha = new SliderSetting(
         "\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c \u0431\u043b\u0438\u043a\u0430 \u0438\u043d\u0444\u043e",
         30.0,
         0.0,
         100.0,
         1.0
      )
      .setVisible(() -> false);
   private final SliderSetting infoShineThickness = new SliderSetting(
         "\u0422\u043e\u043b\u0449\u0438\u043d\u0430 \u0431\u043b\u0438\u043a\u0430 \u0438\u043d\u0444\u043e", 0.5, 0.2, 2.0, 0.05
      )
      .setVisible(() -> false);
   private final BooleanSetting infoCorners = new BooleanSetting("\u0423\u0433\u043e\u043b\u043a\u0438 \u0438\u043d\u0444\u043e", true).setVisible(() -> false);
   private final Interface.HudPopup infoPopup = new Interface.HudPopup(
      "Info", this.infoDrag, this.infoBlur, this.infoSize, this.infoAlpha, null, this.infoShine, this.infoShineAlpha, this.infoShineThickness, this.infoCorners
   );
   private final BooleanSetting hotbarBlur;
   private final SliderSetting hotbarSize;
   private final SliderSetting hotbarAlpha;
   private final BooleanSetting hotbarShine;
   private final SliderSetting hotbarShineAlpha;
   private final SliderSetting hotbarShineThickness;
   private final BooleanSetting hotbarCorners;
   private final BooleanSetting hotbarCounts;
   private final Interface.HudPopup hotbarPopup;
   private final BooleanSetting armourBlur;
   private final SliderSetting armourSize;
   private final SliderSetting armourAlpha;
   private final BooleanSetting armourShine;
   private final SliderSetting armourShineAlpha;
   private final SliderSetting armourShineThickness;
   private final BooleanSetting armourCorners;
   private final BooleanSetting armourDurability;
   private final Interface.HudPopup armourPopup;
   private final BooleanSetting totemBarBlur;
   private final SliderSetting totemBarSize;
   private final SliderSetting totemBarAlpha;
   private final BooleanSetting totemBarShine;
   private final SliderSetting totemBarShineAlpha;
   private final SliderSetting totemBarShineThickness;
   private final BooleanSetting totemBarCorners;
   private final Interface.HudPopup totemBarPopup;
   private final BooleanSetting thBlur;
   private final SliderSetting thSize;
   private final SliderSetting thAlpha;
   private final BooleanSetting thShine;
   private final SliderSetting thShineAlpha;
   private final SliderSetting thShineThickness;
   private final BooleanSetting thCorners;
   private final BooleanSetting thShowOnHover;
   private final BooleanSetting thShowItems;
   private final Interface.HudPopup targetHudPopup;
   private final Draggable notificationsDrag;
   private final BooleanSetting ntBlur;
   private final SliderSetting ntSize;
   private final SliderSetting ntAlpha;
   private final BooleanSetting ntShine;
   private final SliderSetting ntShineAlpha;
   private final SliderSetting ntShineThickness;
   private final BooleanSetting ntCorners;
   private final BooleanSetting notifModuleStates;
   private final BooleanSetting notifTotem;
   private final Interface.HudPopup notificationsPopup;
   private final Interface.HudPopup keybindsPopup;
   private final Interface.HudPopup stafflistPopup;
   private static final float POPUP_HEADER_H = 15.0F;
   private static final float POPUP_TOGGLE_H = 14.0F;
   private static final float POPUP_SLIDER_H = 19.0F;
   private static final float POPUP_PAD = 4.0F;
   private static final float POPUP_TRACK_INSET = 6.0F;
   private float trailHealthPercent;
   private float lastHealthPercent;
   private float lastHpRaw;
   private final List<Interface.HeadParticle> headParticles;
   private final List<Interface.PotionItem> potionItems;
   private RegistryEntry<StatusEffect> currentRandomEffect;
   private long lastEffectChange;
   private static List<RegistryEntry<StatusEffect>> cachedAllEffects;
   private static final Random RANDOM = new Random();
   private final List<Interface.CooldownItem> cooldownItems;
   private static final Item[] COOLDOWN_EXAMPLE_ITEMS = new Item[]{
      Items.field_8634, Items.field_8449, Items.field_8233, Items.field_49814, Items.field_8367, Items.field_8547, Items.field_8255, Items.field_39057
   };
   private int cooldownExampleIndex;
   private long cooldownExampleChange;
   private final Animation cdAlpha;
   private final Animation exampleNotifToggleAnim;

   public Interface() {
      this.infoPopup
         .extraRows
         .add(new Interface.PopupRow(Interface.PopupKind.TOGGLE, "\u0414\u043e\u0431\u0430\u0432\u0438\u0442\u044c BPS", this.wmBps, null));
      this.infoPopup
         .extraRows
         .add(new Interface.PopupRow(Interface.PopupKind.TOGGLE, "\u0414\u043e\u0431\u0430\u0432\u0438\u0442\u044c TPS", this.wmTps, null));
      this.infoPopup
         .extraRows
         .add(
            new Interface.PopupRow(
               Interface.PopupKind.TOGGLE,
               "\u0414\u043e\u0431\u0430\u0432\u0438\u0442\u044c \u043a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u044b",
               this.wmCoords,
               null
            )
         );
      this.hotbarBlur = new BooleanSetting("\u0411\u043b\u044e\u0440 \u0445\u043e\u0442\u0431\u0430\u0440", true).setVisible(() -> false);
      this.hotbarSize = new SliderSetting("\u0420\u0430\u0437\u043c\u0435\u0440 \u0445\u043e\u0442\u0431\u0430\u0440", 1.0, 0.5, 2.0, 0.05)
         .setVisible(() -> false);
      this.hotbarAlpha = new SliderSetting(
            "\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c \u0445\u043e\u0442\u0431\u0430\u0440", 180.0, 0.0, 255.0, 1.0
         )
         .setVisible(() -> false);
      this.hotbarShine = new BooleanSetting("\u0411\u043b\u0438\u043a \u0445\u043e\u0442\u0431\u0430\u0440", true).setVisible(() -> false);
      this.hotbarShineAlpha = new SliderSetting(
            "\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c \u0431\u043b\u0438\u043a\u0430 \u0445\u043e\u0442\u0431\u0430\u0440",
            30.0,
            0.0,
            100.0,
            1.0
         )
         .setVisible(() -> false);
      this.hotbarShineThickness = new SliderSetting(
            "\u0422\u043e\u043b\u0449\u0438\u043d\u0430 \u0431\u043b\u0438\u043a\u0430 \u0445\u043e\u0442\u0431\u0430\u0440", 0.5, 0.2, 2.0, 0.05
         )
         .setVisible(() -> false);
      this.hotbarCorners = new BooleanSetting("\u0423\u0433\u043e\u043b\u043a\u0438 \u0445\u043e\u0442\u0431\u0430\u0440\u0430", true).setVisible(() -> false);
      this.hotbarCounts = new BooleanSetting(
            "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0442\u044c \u043a\u043e\u043b\u0438\u0447\u0435\u0441\u0442\u0432\u043e", true
         )
         .setVisible(() -> false);
      this.hotbarPopup = new Interface.HudPopup(
         "CustomHotbar",
         this.hotbarDrag,
         this.hotbarBlur,
         this.hotbarSize,
         this.hotbarAlpha,
         null,
         this.hotbarShine,
         this.hotbarShineAlpha,
         this.hotbarShineThickness,
         this.hotbarCorners
      );
      this.hotbarPopup
         .extraRows
         .add(
            new Interface.PopupRow(
               Interface.PopupKind.TOGGLE,
               "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0442\u044c \u043a\u043e\u043b\u0438\u0447\u0435\u0441\u0442\u0432\u043e",
               this.hotbarCounts,
               null
            )
         );
      this.armourBlur = new BooleanSetting("\u0411\u043b\u044e\u0440 \u0431\u0440\u043e\u043d\u044f", true).setVisible(() -> false);
      this.armourSize = new SliderSetting("\u0420\u0430\u0437\u043c\u0435\u0440 \u0431\u0440\u043e\u043d\u044f", 1.0, 0.5, 2.0, 0.05).setVisible(() -> false);
      this.armourAlpha = new SliderSetting(
            "\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c \u0431\u0440\u043e\u043d\u044f", 180.0, 0.0, 255.0, 1.0
         )
         .setVisible(() -> false);
      this.armourShine = new BooleanSetting("\u0411\u043b\u0438\u043a \u0431\u0440\u043e\u043d\u044f", true).setVisible(() -> false);
      this.armourShineAlpha = new SliderSetting(
            "\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c \u0431\u043b\u0438\u043a\u0430 \u0431\u0440\u043e\u043d\u044f",
            30.0,
            0.0,
            100.0,
            1.0
         )
         .setVisible(() -> false);
      this.armourShineThickness = new SliderSetting(
            "\u0422\u043e\u043b\u0449\u0438\u043d\u0430 \u0431\u043b\u0438\u043a\u0430 \u0431\u0440\u043e\u043d\u044f", 0.5, 0.2, 2.0, 0.05
         )
         .setVisible(() -> false);
      this.armourCorners = new BooleanSetting("\u0423\u0433\u043e\u043b\u043a\u0438 \u0431\u0440\u043e\u043d\u0438", true).setVisible(() -> false);
      this.armourDurability = new BooleanSetting("\u041f\u043e\u043b\u043e\u0441\u0430 \u043f\u0440\u043e\u0447\u043d\u043e\u0441\u0442\u0438", true)
         .setVisible(() -> false);
      this.armourPopup = new Interface.HudPopup(
         "ArmourBar",
         this.armourDrag,
         this.armourBlur,
         this.armourSize,
         this.armourAlpha,
         null,
         this.armourShine,
         this.armourShineAlpha,
         this.armourShineThickness,
         this.armourCorners
      );
      this.armourPopup
         .extraRows
         .add(
            new Interface.PopupRow(
               Interface.PopupKind.TOGGLE,
               "\u041f\u043e\u043b\u043e\u0441\u0430 \u043f\u0440\u043e\u0447\u043d\u043e\u0441\u0442\u0438",
               this.armourDurability,
               null
            )
         );
      this.totemBarBlur = new BooleanSetting("\u0411\u043b\u044e\u0440 \u0442\u043e\u0442\u0435\u043c-\u0431\u0430\u0440", true).setVisible(() -> false);
      this.totemBarSize = new SliderSetting("\u0420\u0430\u0437\u043c\u0435\u0440 \u0442\u043e\u0442\u0435\u043c-\u0431\u0430\u0440", 1.0, 0.5, 2.0, 0.05)
         .setVisible(() -> false);
      this.totemBarAlpha = new SliderSetting(
            "\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c \u0442\u043e\u0442\u0435\u043c-\u0431\u0430\u0440",
            180.0,
            0.0,
            255.0,
            1.0
         )
         .setVisible(() -> false);
      this.totemBarShine = new BooleanSetting("\u0411\u043b\u0438\u043a \u0442\u043e\u0442\u0435\u043c-\u0431\u0430\u0440", true).setVisible(() -> false);
      this.totemBarShineAlpha = new SliderSetting(
            "\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c \u0431\u043b\u0438\u043a\u0430 \u0442\u043e\u0442\u0435\u043c-\u0431\u0430\u0440",
            30.0,
            0.0,
            100.0,
            1.0
         )
         .setVisible(() -> false);
      this.totemBarShineThickness = new SliderSetting(
            "\u0422\u043e\u043b\u0449\u0438\u043d\u0430 \u0431\u043b\u0438\u043a\u0430 \u0442\u043e\u0442\u0435\u043c-\u0431\u0430\u0440", 0.5, 0.2, 2.0, 0.05
         )
         .setVisible(() -> false);
      this.totemBarCorners = new BooleanSetting(
            "\u0423\u0433\u043e\u043b\u043a\u0438 \u043f\u043e\u043b\u043e\u0441\u044b \u0442\u043e\u0442\u0435\u043c\u043e\u0432", true
         )
         .setVisible(() -> false);
      this.totemBarPopup = new Interface.HudPopup(
         "TotemBar",
         this.totemBarDrag,
         this.totemBarBlur,
         this.totemBarSize,
         this.totemBarAlpha,
         null,
         this.totemBarShine,
         this.totemBarShineAlpha,
         this.totemBarShineThickness,
         this.totemBarCorners
      );
      this.thBlur = new BooleanSetting("\u0411\u043b\u044e\u0440 \u0442\u0445", true).setVisible(() -> false);
      this.thSize = new SliderSetting("\u0420\u0430\u0437\u043c\u0435\u0440 \u0442\u0445", 1.0, 0.5, 2.0, 0.05).setVisible(() -> false);
      this.thAlpha = new SliderSetting("\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c \u0442\u0445", 180.0, 0.0, 255.0, 1.0)
         .setVisible(() -> false);
      this.thShine = new BooleanSetting("\u0411\u043b\u0438\u043a \u0442\u0445", true).setVisible(() -> false);
      this.thShineAlpha = new SliderSetting(
            "\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c \u0431\u043b\u0438\u043a\u0430 \u0442\u0445", 30.0, 0.0, 100.0, 1.0
         )
         .setVisible(() -> false);
      this.thShineThickness = new SliderSetting("\u0422\u043e\u043b\u0449\u0438\u043d\u0430 \u0431\u043b\u0438\u043a\u0430 \u0442\u0445", 0.5, 0.2, 2.0, 0.05)
         .setVisible(() -> false);
      this.thCorners = new BooleanSetting("\u0423\u0433\u043e\u043b\u043a\u0438 \u0442\u0430\u0440\u0433\u0435\u0442 \u0445\u0443\u0434\u0430", true)
         .setVisible(() -> false);
      this.thShowOnHover = new BooleanSetting(
            "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0442\u044c \u043f\u0440\u0438 \u043d\u0430\u0432\u0435\u0434\u0435\u043d\u0438\u0438", true
         )
         .setVisible(() -> false);
      this.thShowItems = new BooleanSetting(
            "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0442\u044c \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u044b", true
         )
         .setVisible(() -> false);
      this.targetHudPopup = new Interface.HudPopup(
         "TargetHUD", this.targetHUDDrag, this.thBlur, this.thSize, this.thAlpha, null, this.thShine, this.thShineAlpha, this.thShineThickness, this.thCorners
      );
      this.targetHudPopup
         .extraRows
         .add(
            new Interface.PopupRow(
               Interface.PopupKind.TOGGLE,
               "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0442\u044c \u043f\u0440\u0438 \u043d\u0430\u0432\u0435\u0434\u0435\u043d\u0438\u0438",
               this.thShowOnHover,
               null
            )
         );
      this.targetHudPopup
         .extraRows
         .add(
            new Interface.PopupRow(
               Interface.PopupKind.TOGGLE,
               "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0442\u044c \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u044b",
               this.thShowItems,
               null
            )
         );
      this.notificationsDrag = DragManager.installDrag(this, "Notifications", 250.0F, 250.0F);
      this.ntBlur = new BooleanSetting("\u0411\u043b\u044e\u0440 notif", true).setVisible(() -> false);
      this.ntSize = new SliderSetting("\u0420\u0430\u0437\u043c\u0435\u0440 notif", 1.0, 0.5, 2.0, 0.05).setVisible(() -> false);
      this.ntAlpha = new SliderSetting("\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c notif", 180.0, 0.0, 255.0, 1.0)
         .setVisible(() -> false);
      this.ntShine = new BooleanSetting("\u0411\u043b\u0438\u043a notif", true).setVisible(() -> false);
      this.ntShineAlpha = new SliderSetting(
            "\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c \u0431\u043b\u0438\u043a\u0430 notif", 30.0, 0.0, 100.0, 1.0
         )
         .setVisible(() -> false);
      this.ntShineThickness = new SliderSetting("\u0422\u043e\u043b\u0449\u0438\u043d\u0430 \u0431\u043b\u0438\u043a\u0430 notif", 0.5, 0.2, 2.0, 0.05)
         .setVisible(() -> false);
      this.ntCorners = new BooleanSetting("\u0423\u0433\u043e\u043b\u043a\u0438 \u0443\u0432\u0435\u0434\u043e\u043c\u043b\u0435\u043d\u0438\u0439", true)
         .setVisible(() -> false);
      this.notifModuleStates = new BooleanSetting("\u0421\u043e\u0441\u0442\u043e\u044f\u043d\u0438\u044f \u043c\u043e\u0434\u0443\u043b\u0435\u0439", true)
         .setVisible(() -> false);
      this.notifTotem = new BooleanSetting("\u0421\u043d\u043e\u0441 \u0442\u043e\u0442\u0435\u043c\u0430", true).setVisible(() -> false);
      this.notificationsPopup = new Interface.HudPopup(
         "Notifications",
         this.notificationsDrag,
         this.ntBlur,
         this.ntSize,
         this.ntAlpha,
         null,
         this.ntShine,
         this.ntShineAlpha,
         this.ntShineThickness,
         this.ntCorners
      );
      this.notificationsPopup
         .extraRows
         .add(
            new Interface.PopupRow(
               Interface.PopupKind.TOGGLE,
               "\u0421\u043e\u0441\u0442\u043e\u044f\u043d\u0438\u044f \u043c\u043e\u0434\u0443\u043b\u0435\u0439",
               this.notifModuleStates,
               null
            )
         );
      this.notificationsPopup
         .extraRows
         .add(new Interface.PopupRow(Interface.PopupKind.TOGGLE, "\u0421\u043d\u043e\u0441 \u0442\u043e\u0442\u0435\u043c\u0430", this.notifTotem, null));
      this.keybindsPopup = new Interface.HudPopup(
         "Keybinds",
         this.keyBindsDrag,
         this.keybindsBlur,
         this.keybindsSize,
         this.keybindsAlpha,
         null,
         this.keybindsShine,
         this.keybindsShineAlpha,
         this.keybindsShineThickness,
         this.keybindsCorners
      );
      this.stafflistPopup = new Interface.HudPopup(
         "StaffList",
         this.staffListDrag,
         this.stafflistBlur,
         this.stafflistSize,
         this.stafflistAlpha,
         null,
         this.stafflistShine,
         this.stafflistShineAlpha,
         this.stafflistShineThickness,
         this.stafflistCorners
      );
      this.trailHealthPercent = 1.0F;
      this.lastHealthPercent = 1.0F;
      this.lastHpRaw = -1.0F;
      this.headParticles = new ArrayList<>();
      this.potionItems = new CopyOnWriteArrayList<>();
      this.lastEffectChange = 0L;
      this.cooldownItems = new CopyOnWriteArrayList<>();
      this.cooldownExampleIndex = 0;
      this.cooldownExampleChange = 0L;
      this.cdAlpha = new Animation(Easing.EXPO_OUT, 200L);
      this.exampleNotifToggleAnim = new Animation(Easing.EXPO_OUT, 200L);
   }

   public float getBackgroundIntensity() {
      return this.backgroundIntensity.getFloatValue();
   }

   public void drawHeaderBackground(float x, float y, float w, float h, float radius, int alpha) {
      float intensity = this.headerIntensity.getFloatValue();
      if (this.elements.isEnabled("\u0411\u043b\u044e\u0440 \u0444\u043e\u043d\u0430")) {
         int color = ColorProvider.setAlpha(ColorProvider.getColorInterfaceBg(), (int)((float)alpha * intensity));
         DrawUtil.drawRoundBlur(x, y, w, h, radius, ColorProvider.rgba(200, 200, 200, (float)alpha), 12.0F);
         DrawUtil.drawRound(x, y, w, h, radius, color);
      } else {
         DrawUtil.drawRound(x, y, w, h, radius, ColorProvider.setAlpha(ColorProvider.getColorInterfaceBg(), (int)((float)alpha * intensity)));
      }
   }

   private void drawItemBackground(float x, float y, float w, float h, float radius, int alpha) {
      float intensity = this.itemIntensity.getFloatValue();
      if (this.elements.isEnabled("\u0411\u043b\u044e\u0440 \u0444\u043e\u043d\u0430")) {
         int color = ColorProvider.setAlpha(ColorProvider.getColorInterfaceBg(), (int)((float)alpha * intensity));
         DrawUtil.drawRoundBlur(x, y, w, h, radius, ColorProvider.rgba(200, 200, 200, (float)alpha), 12.0F);
         DrawUtil.drawRound(x, y, w, h, radius, color);
      } else {
         DrawUtil.drawRound(x, y, w, h, radius, ColorProvider.setAlpha(ColorProvider.getColorInterfaceBg(), (int)((float)alpha * intensity)));
      }
   }

   public void drawBackground(float x, float y, float w, float h, float radius, int alpha) {
      if (this.elements.isEnabled("\u0411\u043b\u044e\u0440 \u0444\u043e\u043d\u0430")) {
         int color = ColorProvider.setAlpha(ColorProvider.getColorInterfaceBg(), (int)((float)alpha * this.backgroundIntensity.getFloatValue()));
         DrawUtil.drawRoundBlur(x, y, w, h, radius, ColorProvider.rgba(200, 200, 200, (float)alpha), 12.0F);
         DrawUtil.drawRound(x, y, w, h, radius, color);
      } else {
         int color = ColorProvider.setAlpha(ColorProvider.getColorInterfaceBg(), (int)((float)alpha * this.backgroundIntensity.getFloatValue()));
         DrawUtil.drawRound(x, y, w, h, radius, color);
      }
   }

   public void drawBackground(float x, float y, float w, float h, float tl, float tr, float bl, float br, int alpha) {
      Vector4f radii = new Vector4f(tl, tr, bl, br);
      if (this.elements.isEnabled("\u0411\u043b\u044e\u0440 \u0444\u043e\u043d\u0430")) {
         int color = ColorProvider.setAlpha(ColorProvider.getColorInterfaceBg(), (int)((float)alpha * this.backgroundIntensity.getFloatValue()));
         DrawUtil.drawRoundBlur(x, y, w, h, radii, ColorProvider.rgba(200, 200, 200, (float)alpha), 12.0F);
         DrawUtil.drawRound(x, y, w, h, radii, color);
      } else {
         int color = ColorProvider.setAlpha(ColorProvider.getColorInterfaceBg(), (int)((float)alpha * this.backgroundIntensity.getFloatValue()));
         DrawUtil.drawRound(x, y, w, h, radii, color);
      }
   }

   @Subscribe
   public void onEventHUD(EventHUD e) {
      if (this.mc.field_1724 != null && !this.mc.field_1690.field_1842 && !this.mc.method_53526().method_53536()) {
         if (this.elements.isEnabled("\u041d\u043e\u0442\u0438\u0444\u0438\u043a\u0430\u0446\u0438\u0438")) {
            this.notifications.render(e.getDrawContext());
            this.renderNotificationsExample(e.getDrawContext());
         }

         if (this.elements.isEnabled("\u0412\u0430\u0442\u0435\u0440\u043c\u0430\u0440\u043a\u0430")) {
            this.renderWatermark(e.getDrawContext());
         }

         if (this.elements.isEnabled("\u0418\u043d\u0444\u043e")) {
            this.renderInfo(e.getDrawContext());
         }

         if (this.elements.isEnabled("\u041a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u044b")) {
            this.renderCoordsInfo(e.getDrawContext());
         }

         if (this.elements.isEnabled("\u0410\u043a\u0442\u0438\u0432\u043d\u044b\u0439 \u0442\u0430\u0440\u0433\u0435\u0442")) {
            this.renderTargetHUD(e.getDrawContext());
         }

         if (this.elements.isEnabled("\u041f\u0440\u0438\u0432\u044f\u0437\u0430\u043d\u043d\u044b\u0435 \u043c\u043e\u0434\u0443\u043b\u0438")) {
            this.renderKeyBinds(e.getDrawContext());
         }

         if (this.elements.isEnabled("\u0410\u043a\u0442\u0438\u0432\u043d\u044b\u0435 \u043c\u043e\u0434\u0435\u0440\u0430\u0442\u043e\u0440\u044b")) {
            this.renderStaffList(e.getDrawContext());
         }

         if (this.elements.isEnabled("\u0411\u0430\u0444\u044b")) {
            this.renderPotions(e.getDrawContext());
         }

         if (this.elements.isEnabled("\u041a\u0443\u043b\u0414\u0430\u0443\u043d\u044b")) {
            this.renderCoolDowns(e.getDrawContext());
         }

         if (this.elements.isEnabled("ServerHelper")) {
            this.renderServerHelper(e.getDrawContext());
         }

         if (this.elements.isEnabled("\u041a\u0430\u0441\u0442\u043e\u043c\u043d\u044b\u0439 \u0445\u043e\u0442\u0431\u0430\u0440")) {
            this.renderCustomHotbar(e.getDrawContext());
         }

         if (this.elements.isEnabled("\u0411\u0440\u043e\u043d\u044f")) {
            this.renderArmourBar(e.getDrawContext());
         }

         if (this.elements.isEnabled("\u041f\u043e\u043b\u043e\u0441\u0430 \u0442\u043e\u0442\u0435\u043c\u043e\u0432")) {
            this.renderTotemBar(e.getDrawContext());
         }
      }
   }

   @Subscribe
   private void onUpdate(EventTick e) {
      if (this.mc.field_1724 != null && this.mc.field_1687 != null) {
         if (this.elements.isEnabled("\u0410\u043a\u0442\u0438\u0432\u043d\u044b\u0435 \u043c\u043e\u0434\u0435\u0440\u0430\u0442\u043e\u0440\u044b")) {
            this.update();
         }

         if (this.elements.isEnabled("\u0411\u0430\u0444\u044b")) {
            this.updatePotions();
         }

         if (this.elements.isEnabled("\u041a\u0443\u043b\u0414\u0430\u0443\u043d\u044b")) {
            this.updateCooldowns();
         }
      }
   }

   @Subscribe
   private void onPopTotem(EventPopTotem e) {
      if (this.isTotemNotifEnabled()) {
         PlayerEntity player = e.getPlayer();
         String name = player.method_5477().getString();
         boolean enchanted = !player.method_6079().method_58657().method_57543();
         Text tagText = NameTags.processName(player);
         this.notifications.postTotem(tagText, enchanted);
      }
   }

   @Subscribe
   private void onCooldownPacket(EventPacket e) {
      if (this.mc.field_1724 != null && e.getType() == EventPacket.Type.RECEIVE) {
         if (e.getPacket() instanceof CooldownUpdateS2CPacket c) {
            Item item = (Item)Registries.field_41178.method_63535(c.comp_3082());
            if (item == null || item == Items.field_8162) {
               return;
            }

            for (Interface.CooldownItem ci : this.cooldownItems) {
               if (ci.item == item) {
                  ci.active = false;
               }
            }

            if (c.comp_2199() != 0) {
               long durMs = (long)c.comp_2199() * 50L;
               this.cooldownItems.add(new Interface.CooldownItem(item, System.currentTimeMillis() + durMs, durMs));
            }
         } else if (e.getPacket() instanceof PlayerRespawnS2CPacket) {
            this.cooldownItems.clear();
         }
      }
   }

   private void renderKeyBinds(DrawContext context) {
      if (this.mc.field_1724 != null) {
         if (!(this.mc.field_1755 instanceof ChatScreen)) {
            this.keybindsPopup.open = false;
            this.keybindsPopup.draggingSlider = null;
         }

         this.beginScale(this.keybindsPopup, context);
         this.renderKeyBindsNew(context);
         this.endScale(this.keybindsPopup, context);
         this.runPopup(this.keybindsPopup, context);
      }
   }

   private void renderKeyBindsNew(DrawContext context) {
      if (this.mc.field_1724 != null) {
         float posX = this.keyBindsDrag.getX();
         float posY = this.keyBindsDrag.getY();
         float headerHeight = 14.0F;
         float itemHeight = 9.5F;
         float minWidth = 52.0F;
         float padX = 5.0F;
         float padY = 2.0F;
         List<Interface.BindEntry> entries = this.keybindEntries;
         entries.clear();

         for (Module module : AlphaDLC.getInstance().getModuleStorage().getModules()) {
            if (module.getKey() != -1 && (double)module.getAnimation().getValue() > 0.001) {
               entries.add(
                  new Interface.BindEntry(module.getName(), KeyStorage.getKey(module.getKey()), (double)module.getAnimation().getValue(), module.getCategory())
               );
            }

            for (Setting setting : module.getSettings()) {
               if (setting instanceof BooleanSetting) {
                  BooleanSetting bs = (BooleanSetting)setting;
                  if (bs.getKey() != -1 && bs.getValue()) {
                     entries.add(new Interface.BindEntry(bs.getName(), KeyStorage.getKey(bs.getKey()), 1.0, module.getCategory()));
                  }
               }
            }
         }

         boolean isFound = !entries.isEmpty();
         if (!isFound && !(this.mc.field_1755 instanceof ChatScreen)) {
            this.alpha.run(0.0F);
         } else {
            this.alpha.run(1.0F);
         }

         float globalAlpha = this.alpha.getValue();
         if (!(globalAlpha <= 0.05F)) {
            int headerAlpha = (int)Math.min(255.0F, Math.max(0.0F, 255.0F * globalAlpha));
            boolean showExample = this.mc.field_1755 instanceof ChatScreen && !isFound;
            float maxLabelBoxW = minWidth;
            float maxBindBoxW = 0.0F;

            for (Interface.BindEntry entry : entries) {
               float animVal = (float)Math.min(1.0, Math.max(0.0, entry.animValue()));
               if (!(animVal <= 0.001F)) {
                  float lw = 9.0F + Fonts.SFMEDIUM.get().getWidth(entry.label(), 7.0F) + 7.0F;
                  if (lw > maxLabelBoxW) {
                     maxLabelBoxW = lw;
                  }

                  float bw = Fonts.SFMEDIUM.get().getWidth(entry.bind(), 6.75F) + 6.0F;
                  if (bw > maxBindBoxW) {
                     maxBindBoxW = bw;
                  }
               }
            }

            if (showExample) {
               float lwx = 9.0F + Fonts.SFMEDIUM.get().getWidth("Example", 7.0F) + 7.0F;
               if (lwx > maxLabelBoxW) {
                  maxLabelBoxW = lwx;
               }

               float bw = Fonts.SFMEDIUM.get().getWidth("K", 6.75F) + 6.0F;
               if (bw > maxBindBoxW) {
                  maxBindBoxW = bw;
               }
            }

            float contentHeight = 0.0F;
            if (showExample) {
               contentHeight = itemHeight;
            } else {
               for (Interface.BindEntry entryx : entries) {
                  float av = (float)Math.min(1.0, Math.max(0.0, entryx.animValue()));
                  if (!(av <= 0.001F)) {
                     contentHeight += itemHeight * av;
                  }
               }
            }

            float rawWidth = maxLabelBoxW + maxBindBoxW + padX * 2.0F;
            this.keybindsPopup.panelWidth.run(rawWidth);
            float totalRowWidth = this.keybindsPopup.panelWidth.getValue();
            if (totalRowWidth < 20.0F) {
               totalRowWidth = rawWidth;
            }

            float totalHeight = headerHeight + contentHeight + padY * 2.0F;
            this.drawElementBackground(this.keybindsPopup, posX, posY, totalRowWidth, totalHeight, 3.0F, globalAlpha);
            this.drawElementShine(this.keybindsPopup, context, posX, posY, totalRowWidth, totalHeight, 3.0F);
            DrawUtil.drawText(
               Fonts.SFMEDIUM.get(), "Keybinds", posX + padX + 4.0F, posY + padY + 2.5F, ColorProvider.rgba(255, 255, 255, (float)headerAlpha), 8.0F
            );
            DrawUtil.drawText(
               Fonts.ALPHADLC.get(),
               "g",
               posX + totalRowWidth - padX - 11.0F,
               posY + padY + 3.0F,
               ColorProvider.setAlpha(ColorProvider.getColorIcons(), headerAlpha),
               9.0F
            );
            float curY = posY + headerHeight + padY;

            for (Interface.BindEntry entryxx : entries) {
               float animVal = (float)Math.min(1.0, Math.max(0.0, entryxx.animValue()));
               if (!(animVal <= 0.001F)) {
                  int itemAlpha = (int)Math.min(255.0F, Math.max(0.0F, 255.0F * animVal * globalAlpha));
                  if (itemAlpha >= 5) {
                     float rowHeight = itemHeight * animVal;
                     context.method_51448().method_22903();
                     context.method_51448().method_46416(posX + totalRowWidth / 2.0F, curY + rowHeight / 2.0F, 0.0F);
                     context.method_51448().method_22905(animVal, animVal, animVal);
                     context.method_51448().method_46416(-(posX + totalRowWidth / 2.0F), -(curY + rowHeight / 2.0F), 0.0F);

                     String catIcon = switch (entryxx.category()) {
                        case COMBAT -> "a";
                        case MOVEMENT -> "b";
                        case RENDER -> "c";
                        case PLAYER -> "d";
                        case MISC -> "e";
                     };
                     DrawUtil.drawText(
                        Fonts.ICONS_MINCED.get(),
                        catIcon,
                        posX + padX + 1.0F,
                        curY + 1.5F,
                        ColorProvider.setAlpha(ColorProvider.getColorIcons(), itemAlpha),
                        7.0F
                     );
                     DrawUtil.drawText(
                        Fonts.SFMEDIUM.get(), entryxx.label(), posX + padX + 9.0F, curY + 1.35F, ColorProvider.rgba(255, 255, 255, (float)itemAlpha), 7.0F
                     );
                     this.drawKeybindCap(entryxx.bind(), posX + totalRowWidth - padX - maxBindBoxW, curY, maxBindBoxW, itemHeight, itemAlpha);
                     context.method_51448().method_22909();
                     curY += rowHeight;
                  }
               }
            }

            if (showExample) {
               DrawUtil.drawText(
                  Fonts.ICONS_MINCED.get(), "a", posX + padX + 1.0F, curY + 1.5F, ColorProvider.setAlpha(ColorProvider.getColorIcons(), headerAlpha), 7.0F
               );
               DrawUtil.drawText(Fonts.SFMEDIUM.get(), "Example", posX + padX + 9.0F, curY + 1.35F, ColorProvider.rgba(255, 255, 255, (float)headerAlpha), 7.0F);
               this.drawKeybindCap("K", posX + totalRowWidth - padX - maxBindBoxW, curY, maxBindBoxW, itemHeight, headerAlpha);
            }

            this.keyBindsDrag.setWidth(totalRowWidth);
            this.keyBindsDrag.setHeight(totalHeight);
         }
      }
   }

   private void drawKeybindCap(String bind, float colX, float rowY, float colW, float itemHeight, int itemAlpha) {
      float bindW = Fonts.SFMEDIUM.get().getWidth(bind, 6.75F);
      float textX = colX + (colW - bindW) / 2.0F + 0.75F;
      float textY = rowY + itemHeight / 2.0F - 4.0F;
      DrawUtil.drawText(Fonts.SFMEDIUM.get(), bind, textX, textY, ColorProvider.rgba(255, 255, 255, (float)itemAlpha), 6.75F);
   }

   private float hbMainWidth() {
      return 186.0F;
   }

   private float hbOffhandBoxW() {
      return 26.0F;
   }

   private float hbHeight() {
      return 26.0F;
   }

   private float hbBottomY() {
      return (float)this.mc.method_22683().method_4502() - 4.0F;
   }

   private float hotbarMainVisualRight() {
      float size = this.hotbarPopup.size.getFloatValue();
      return (float)this.mc.method_22683().method_4486() / 2.0F + this.hbMainWidth() * size / 2.0F;
   }

   private int durabilityColor(float ratio) {
      ratio = MathHelper.method_15363(ratio, 0.0F, 1.0F);
      int rgb = Color.HSBtoRGB(ratio * 0.33F, 0.85F, 0.95F);
      return ColorProvider.rgba(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF, 255.0F);
   }

   private String trimTextToWidth(String text, float size, float maxWidth) {
      MsdfFont font = Fonts.SFMEDIUM.get();
      if (text != null && !(font.getWidth(text, size) <= maxWidth)) {
         String ellipsis = "..";
         String result = text;

         while (result.length() > 0 && font.getWidth(result + ellipsis, size) > maxWidth) {
            result = result.substring(0, result.length() - 1);
         }

         return result + ellipsis;
      } else {
         return text;
      }
   }

   private void drawOutlinedCount(String text, float x, float y, float size) {
      int outline = ColorProvider.rgba(0, 0, 0, 200.0F);
      DrawUtil.drawText(Fonts.SFMEDIUM.get(), text, x - 0.6F, y, outline, size);
      DrawUtil.drawText(Fonts.SFMEDIUM.get(), text, x + 0.6F, y, outline, size);
      DrawUtil.drawText(Fonts.SFMEDIUM.get(), text, x, y - 0.6F, outline, size);
      DrawUtil.drawText(Fonts.SFMEDIUM.get(), text, x, y + 0.6F, outline, size);
      DrawUtil.drawText(Fonts.SFMEDIUM.get(), text, x, y, -1, size);
   }

   private void renderCustomHotbar(DrawContext context) {
      if (this.mc.field_1724 != null) {
         float size = this.hotbarPopup.size.getFloatValue();
         float offW = this.hbOffhandBoxW();
         float mainW = this.hbMainWidth();
         float height = this.hbHeight();
         float totalWidth = offW + 4.0F + mainW;
         float screenW = (float)this.mc.method_22683().method_4486();
         float ox = screenW / 2.0F - (offW + 4.0F + mainW / 2.0F) * size;
         float oy = this.hbBottomY() - height * size;
         this.hotbarDrag.setX(ox);
         this.hotbarDrag.setY(oy);
         this.hotbarDrag.setWidth(totalWidth);
         this.hotbarDrag.setHeight(height);
         if (!(this.mc.field_1755 instanceof ChatScreen)) {
            this.hotbarPopup.open = false;
            this.hotbarPopup.draggingSlider = null;
         }

         this.beginScale(this.hotbarPopup, context);
         this.renderCustomHotbarContent(context, ox, oy);
         this.endScale(this.hotbarPopup, context);
         this.runPopup(this.hotbarPopup, context);
      }
   }

   private void renderCustomHotbarContent(DrawContext context, float ox, float oy) {
      float pad = 3.0F;
      float cell = 20.0F;
      float iconSize = 16.0F;
      float offW = this.hbOffhandBoxW();
      float mainW = this.hbMainWidth();
      float height = this.hbHeight();
      int selected = this.mc.field_1724.method_31548().field_7545;
      int divColor = ColorProvider.rgba(255, 255, 255, 35.0F);
      this.drawElementBackground(this.hotbarPopup, ox, oy, offW, height, 3.0F, 1.0F);
      this.drawElementShine(this.hotbarPopup, context, ox, oy, offW, height, 3.0F);
      this.drawHotbarCell(context, this.mc.field_1724.method_6079(), ox + pad, oy + pad, cell, iconSize, false);
      float mainX = ox + offW + 4.0F;
      this.drawElementBackground(this.hotbarPopup, mainX, oy, mainW, height, 3.0F, 1.0F);
      this.drawElementShine(this.hotbarPopup, context, mainX, oy, mainW, height, 3.0F);
      String lvl = String.valueOf(this.mc.field_1724.field_7520);
      float lvlW = Fonts.SFMEDIUM.get().getWidth(lvl, 8.0F);
      int xpColor = ColorProvider.rgba(80, 220, 80, 255.0F);
      DrawUtil.drawText(Fonts.SFMEDIUM.get(), lvl, mainX + mainW / 2.0F - lvlW / 2.0F, oy - 9.0F, xpColor, 8.0F);
      float cellY = oy + pad;

      for (int i = 0; i < 9; i++) {
         float cellX = mainX + pad + (float)i * cell;
         if (i > 0) {
            DrawUtil.drawRound(cellX - 0.25F, cellY + 3.0F, 0.5F, cell - 6.0F, 0.25F, divColor);
         }

         this.drawHotbarCell(context, this.mc.field_1724.method_31548().method_5438(i), cellX, cellY, cell, iconSize, i == selected);
      }
   }

   private void drawHotbarCell(DrawContext context, ItemStack stack, float cellX, float cellY, float cell, float iconSize, boolean selected) {
      if (selected) {
         DrawUtil.drawRound(cellX + 0.5F, cellY, cell - 1.0F, cell, 2.5F, ColorProvider.setAlpha(ColorProvider.getColorClient(), 90));
      }

      if (!stack.method_7960()) {
         float iconX = cellX + (cell - iconSize) / 2.0F;
         float iconY = cellY + (cell - iconSize) / 2.0F;
         this.drawCooldownIcon(context, stack, iconX, iconY, iconSize, 255);
         if (this.hotbarCounts.getValue() && stack.method_7947() > 1) {
            String cnt = String.valueOf(stack.method_7947());
            float cw = Fonts.SFMEDIUM.get().getWidth(cnt, 7.5F);
            this.drawOutlinedCount(cnt, cellX + cell - cw - 1.5F, cellY + cell - 8.5F, 7.5F);
         }
      }
   }

   private void renderArmourBar(DrawContext context) {
      if (this.mc.field_1724 != null) {
         float pad = 3.0F;
         float cell = 20.0F;
         float iconSize = 16.0F;
         int count = 4;
         float width = pad * 2.0F + cell * (float)count;
         float height = pad * 2.0F + cell;
         float size = this.armourPopup.size.getFloatValue();
         float screenW = (float)this.mc.method_22683().method_4486();
         float ox = this.hotbarMainVisualRight() + 4.0F;
         float oy = this.hbBottomY() - height * size;
         if (ox + width * size > screenW - 2.0F) {
            ox = screenW - 2.0F - width * size;
         }

         this.armourDrag.setX(ox);
         this.armourDrag.setY(oy);
         this.armourDrag.setWidth(width);
         this.armourDrag.setHeight(height);
         if (!(this.mc.field_1755 instanceof ChatScreen)) {
            this.armourPopup.open = false;
            this.armourPopup.draggingSlider = null;
         }

         this.beginScale(this.armourPopup, context);
         this.renderArmourBarContent(context, ox, oy, width, height, pad, cell, iconSize);
         this.endScale(this.armourPopup, context);
         this.runPopup(this.armourPopup, context);
      }
   }

   private void renderArmourBarContent(DrawContext context, float posX, float posY, float width, float height, float pad, float cell, float iconSize) {
      boolean durab = this.armourDurability.getValue();
      EquipmentSlot[] eqSlots = new EquipmentSlot[]{EquipmentSlot.field_6169, EquipmentSlot.field_6174, EquipmentSlot.field_6172, EquipmentSlot.field_6166};
      this.drawElementBackground(this.armourPopup, posX, posY, width, height, 3.0F, 1.0F);
      this.drawElementShine(this.armourPopup, context, posX, posY, width, height, 3.0F);

      for (int i = 0; i < eqSlots.length; i++) {
         float cellX = posX + pad + (float)i * cell;
         float cellY = posY + pad;
         ItemStack stack = this.mc.field_1724.method_6118(eqSlots[i]);
         if (!stack.method_7960()) {
            float iconX = cellX + (cell - iconSize) / 2.0F;
            float iconY = cellY + (cell - iconSize) / 2.0F;
            this.drawCooldownIcon(context, stack, iconX, iconY, iconSize, 255);
            if (durab && stack.method_7963() && stack.method_7936() > 0) {
               float ratio = 1.0F - (float)stack.method_7919() / (float)stack.method_7936();
               ratio = MathHelper.method_15363(ratio, 0.0F, 1.0F);
               float barH = 2.5F;
               float barX = cellX + (cell - iconSize) / 2.0F;
               float barY = cellY + cell - barH - 0.5F;
               DrawUtil.drawRound(barX, barY, iconSize, barH, barH / 2.0F, ColorProvider.rgba(20, 20, 20, 200.0F));
               DrawUtil.drawRound(barX, barY, iconSize * ratio, barH, barH / 2.0F, this.durabilityColor(ratio));
            }
         }
      }
   }

   private void renderTotemBar(DrawContext context) {
      if (this.mc.field_1724 != null) {
         if (!(this.mc.field_1755 instanceof ChatScreen)) {
            this.totemBarPopup.open = false;
            this.totemBarPopup.draggingSlider = null;
         }

         this.beginScale(this.totemBarPopup, context);
         this.renderTotemBarContent(context);
         this.endScale(this.totemBarPopup, context);
         this.runPopup(this.totemBarPopup, context);
      }
   }

   private void renderTotemBarContent(DrawContext context) {
      float posX = this.totemBarDrag.getX();
      float posY = this.totemBarDrag.getY();
      int totemCount = 0;

      for (int i = 0; i < this.mc.field_1724.method_31548().method_5439(); i++) {
         ItemStack s = this.mc.field_1724.method_31548().method_5438(i);
         if (s.method_7909() == Items.field_8288) {
            totemCount += s.method_7947();
         }
      }

      String text = totemCount + "x";
      float pad = 4.0F;
      float iconSize = 16.0F;
      float textSize = 8.0F;
      float gap = 1.5F;
      float textW = Fonts.SFMEDIUM.get().getWidth(text, textSize);
      float contentW = Math.max(iconSize, textW);
      float width = pad * 2.0F + contentW;
      float height = pad * 2.0F + iconSize + gap + textSize;
      this.drawElementBackground(this.totemBarPopup, posX, posY, width, height, 3.0F, 1.0F);
      this.drawElementShine(this.totemBarPopup, context, posX, posY, width, height, 3.0F);
      float iconX = posX + (width - iconSize) / 2.0F;
      float iconY = posY + pad;
      this.drawCooldownIcon(context, new ItemStack(Items.field_8288), iconX, iconY, iconSize, 255);
      DrawUtil.drawText(Fonts.SFMEDIUM.get(), text, posX + (width - textW) / 2.0F, iconY + iconSize + gap, -1, textSize);
      this.totemBarDrag.setWidth(width);
      this.totemBarDrag.setHeight(height);
   }

   private void renderStaffList(DrawContext context) {
      if (this.mc.field_1724 != null) {
         if (!(this.mc.field_1755 instanceof ChatScreen)) {
            this.stafflistPopup.open = false;
            this.stafflistPopup.draggingSlider = null;
         }

         this.beginScale(this.stafflistPopup, context);
         this.renderStaffListNew(context);
         this.endScale(this.stafflistPopup, context);
         this.runPopup(this.stafflistPopup, context);
      }
   }

   private void renderStaffListNew(DrawContext context) {
      float posX = this.staffListDrag.getX();
      float posY = this.staffListDrag.getY();
      float headerHeight = 14.0F;
      float itemHeight = 11.5F;
      float minWidth = 52.0F;
      float padX = 5.0F;
      float padY = 2.0F;
      float statusBoxW = 12.0F;

      for (Interface.Staff staff : this.staffPlayers) {
         staff.animation.run(staff.isOnServer ? 1.0F : 0.0F);
      }

      boolean isFound = false;

      for (Interface.Staff staff : this.staffPlayers) {
         if (staff.animation.getValue() > 0.001F) {
            isFound = true;
         }
      }

      if (!isFound && !(this.mc.field_1755 instanceof ChatScreen)) {
         this.alpha2.run(0.0F);
      } else {
         this.alpha2.run(1.0F);
      }

      float globalAlpha = this.alpha2.getValue();
      if (!(globalAlpha <= 0.05F)) {
         int headerAlpha = (int)Math.min(255.0F, Math.max(0.0F, 255.0F * globalAlpha));
         boolean showExample = this.mc.field_1755 instanceof ChatScreen && !isFound;
         float maxNameBoxW = minWidth;

         for (Interface.Staff staffx : this.staffPlayers) {
            if (staffx.animation.getValue() > 0.001F) {
               float nw = 11.0F + Fonts.SFMEDIUM.get().getWidth(staffx.prefix, 7.0F) + 7.0F;
               if (nw > maxNameBoxW) {
                  maxNameBoxW = nw;
               }
            }
         }

         if (showExample) {
            float nw = 11.0F + Fonts.SFMEDIUM.get().getWidth("Example", 7.0F) + 7.0F;
            if (nw > maxNameBoxW) {
               maxNameBoxW = nw;
            }
         }

         float contentHeight = 0.0F;
         if (showExample) {
            contentHeight = itemHeight;
         } else {
            for (Interface.Staff staffxx : this.staffPlayers) {
               float av = staffxx.animation.getValue();
               if (!(av <= 0.001F)) {
                  contentHeight += itemHeight * av;
               }
            }
         }

         float rawWidth = maxNameBoxW + statusBoxW + padX * 2.0F;
         this.stafflistPopup.panelWidth.run(rawWidth);
         float totalRowWidth = this.stafflistPopup.panelWidth.getValue();
         if (totalRowWidth < 20.0F) {
            totalRowWidth = rawWidth;
         }

         float totalHeight = headerHeight + contentHeight + padY * 2.0F;
         this.drawElementBackground(this.stafflistPopup, posX, posY, totalRowWidth, totalHeight, 3.0F, globalAlpha);
         this.drawElementShine(this.stafflistPopup, context, posX, posY, totalRowWidth, totalHeight, 3.0F);
         float statusCX = posX + totalRowWidth - padX - statusBoxW / 2.0F;
         float headerIconW = Fonts.ALPHADLC.get().getWidth("i", 9.0F);
         DrawUtil.drawText(
            Fonts.SFMEDIUM.get(), "StaffList", posX + padX + 4.0F, posY + padY + 2.5F, ColorProvider.rgba(255, 255, 255, (float)headerAlpha), 8.0F
         );
         DrawUtil.drawText(
            Fonts.ALPHADLC.get(),
            "i",
            statusCX - headerIconW / 2.0F + 0.75F,
            posY + padY + 3.0F,
            ColorProvider.setAlpha(ColorProvider.getColorIcons(), headerAlpha),
            9.0F
         );
         float curY = posY + headerHeight + padY;

         for (Interface.Staff staffxxx : this.staffPlayers) {
            float animVal = staffxxx.animation.getValue();
            if (!(animVal <= 0.001F)) {
               int itemAlpha = (int)Math.min(255.0F, Math.max(0.0F, 255.0F * animVal * globalAlpha));
               if (itemAlpha >= 5) {
                  float rowHeight = itemHeight * animVal;
                  context.method_51448().method_22903();
                  context.method_51448().method_46416(posX + totalRowWidth / 2.0F, curY + rowHeight / 2.0F, 0.0F);
                  context.method_51448().method_22905(animVal, animVal, animVal);
                  context.method_51448().method_46416(-(posX + totalRowWidth / 2.0F), -(curY + rowHeight / 2.0F), 0.0F);
                  PlayerListEntry playerEntry = this.mc.method_1562().method_2874(staffxxx.name);
                  Identifier skinTexture;
                  if (playerEntry != null) {
                     skinTexture = playerEntry.method_52810().comp_1626();
                  } else {
                     skinTexture = DefaultSkinHelper.method_4649();
                  }

                  int textureId = this.mc.method_1531().method_4619(skinTexture).method_4624();
                  float headSize = 8.0F;
                  Builder.texture()
                     .size(new SizeState(headSize, headSize))
                     .radius(new QuadRadiusState(2.0F))
                     .color(new QuadColorState(ColorProvider.setAlpha(-1, itemAlpha)))
                     .texture(0.125F, 0.125F, 0.125F, 0.125F, textureId)
                     .smoothness(1.0F)
                     .build()
                     .render(context.method_51448().method_23760().method_23761(), posX + padX + 1.0F, curY + (itemHeight - headSize) / 2.0F);
                  DrawUtil.drawText(Fonts.SFMEDIUM.get(), staffxxx.prefix, posX + padX + 10.0F, curY + (itemHeight - 7.0F) / 2.0F - 0.35F, 7.0F, itemAlpha);
                  int dotColor = staffxxx.status == Interface.Status.NONE
                     ? ColorProvider.rgba(32, 255, 32, (float)itemAlpha)
                     : ColorProvider.rgba(255, 32, 32, (float)itemAlpha);
                  float dotCX = posX + totalRowWidth - padX - statusBoxW / 2.0F;
                  DrawUtil.drawCircle(dotCX, curY + itemHeight / 2.0F, 3.5F, dotColor);
                  context.method_51448().method_22909();
                  curY += rowHeight;
               }
            }
         }

         if (showExample) {
            int textureId = this.mc.method_1531().method_4619(DefaultSkinHelper.method_4649()).method_4624();
            float headSize = 8.0F;
            Builder.texture()
               .size(new SizeState(headSize, headSize))
               .radius(new QuadRadiusState(2.0F))
               .color(new QuadColorState(ColorProvider.setAlpha(-1, headerAlpha)))
               .texture(0.125F, 0.125F, 0.125F, 0.125F, textureId)
               .smoothness(1.0F)
               .build()
               .render(context.method_51448().method_23760().method_23761(), posX + padX + 1.0F, curY + (itemHeight - headSize) / 2.0F);
            DrawUtil.drawText(
               Fonts.SFMEDIUM.get(),
               "Example",
               posX + padX + 10.0F,
               curY + (itemHeight - 7.0F) / 2.0F - 0.35F,
               ColorProvider.rgba(255, 255, 255, (float)headerAlpha),
               7.0F
            );
            float dotCX = posX + totalRowWidth - padX - statusBoxW / 2.0F;
            DrawUtil.drawCircle(dotCX, curY + itemHeight / 2.0F, 3.5F, ColorProvider.rgba(32, 255, 32, (float)headerAlpha));
         }

         this.staffListDrag.setWidth(totalRowWidth);
         this.staffListDrag.setHeight(totalHeight);
      }
   }

   public boolean isModuleStateNotifEnabled() {
      return this.elements.isEnabled("\u041d\u043e\u0442\u0438\u0444\u0438\u043a\u0430\u0446\u0438\u0438") && this.notifModuleStates.getValue();
   }

   public boolean isTotemNotifEnabled() {
      return this.elements.isEnabled("\u041d\u043e\u0442\u0438\u0444\u0438\u043a\u0430\u0446\u0438\u0438") && this.notifTotem.getValue();
   }

   private List<Interface.PopupRow> buildRows(Interface.HudPopup p) {
      List<Interface.PopupRow> rows = new ArrayList<>();
      rows.add(new Interface.PopupRow(Interface.PopupKind.SLIDER, "\u0420\u0430\u0437\u043c\u0435\u0440", null, p.size));
      rows.add(new Interface.PopupRow(Interface.PopupKind.TOGGLE, "\u0411\u043b\u044e\u0440", p.blur, null));
      rows.add(new Interface.PopupRow(Interface.PopupKind.SLIDER, "\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c", null, p.alpha));
      if (p.ring != null) {
         rows.add(new Interface.PopupRow(Interface.PopupKind.TOGGLE, "\u041a\u043e\u043b\u044c\u0446\u043e \u0442\u0430\u0439\u043c\u0435\u0440", p.ring, null));
      }

      rows.add(new Interface.PopupRow(Interface.PopupKind.TOGGLE, "\u0411\u043b\u0438\u043a", p.shine, null));
      if (p.shine.getValue()) {
         rows.add(
            new Interface.PopupRow(
               Interface.PopupKind.SLIDER,
               "\u041f\u0440\u043e\u0437\u0440\u0430\u0447\u043d\u043e\u0441\u0442\u044c \u0431\u043b\u0438\u043a\u0430",
               null,
               p.shineAlpha
            )
         );
         rows.add(
            new Interface.PopupRow(
               Interface.PopupKind.SLIDER, "\u0422\u043e\u043b\u0449\u0438\u043d\u0430 \u0431\u043b\u0438\u043a\u0430", null, p.shineThickness
            )
         );
      }

      rows.add(new Interface.PopupRow(Interface.PopupKind.TOGGLE, "\u0423\u0433\u043e\u043b\u043a\u0438", p.corners, null));
      rows.addAll(p.extraRows);
      return rows;
   }

   private double potionsMouseX() {
      return this.mc.field_1729.method_1603() / this.mc.method_22683().method_4495();
   }

   private double potionsMouseY() {
      return this.mc.field_1729.method_1604() / this.mc.method_22683().method_4495();
   }

   private void renderPotions(DrawContext context) {
      if (this.mc.field_1724 != null) {
         boolean chatOpen = this.mc.field_1755 instanceof ChatScreen;
         if (!chatOpen) {
            this.potionsPopup.open = false;
            this.potionsPopup.draggingSlider = null;
         }

         this.beginScale(this.potionsPopup, context);
         this.renderPotionsNew(context);
         this.endScale(this.potionsPopup, context);
         this.runPopup(this.potionsPopup, context);
      }
   }

   private void beginScale(Interface.HudPopup p, DrawContext context) {
      float size = p.size.getFloatValue();
      float angle = p.drag.getWobbleAngle();
      boolean needScale = Math.abs(size - 1.0F) > 0.001F;
      boolean needRot = Math.abs(angle) > 0.01F;
      p.transformed = needScale || needRot;
      if (p.transformed) {
         float ox = p.drag.getX();
         float oy = p.drag.getY();
         float cx = ox + p.drag.getWidth() / 2.0F;
         float cy = oy + p.drag.getHeight() / 2.0F;
         if (needScale) {
            IRenderer.DEFAULT_MATRIX.identity().translate(ox, oy, 0.0F).scale(size, size, 1.0F).translate(-ox, -oy, 0.0F);
         }

         context.method_51448().method_22903();
         if (needScale) {
            context.method_51448().method_46416(ox, oy, 0.0F);
            context.method_51448().method_22905(size, size, 1.0F);
            context.method_51448().method_46416(-ox, -oy, 0.0F);
         }

         if (needRot) {
            context.method_51448().method_46416(cx, cy, 0.0F);
            context.method_51448().method_22907(RotationAxis.field_40718.rotationDegrees(angle));
            context.method_51448().method_46416(-cx, -cy, 0.0F);
         }
      }
   }

   private void endScale(Interface.HudPopup p, DrawContext context) {
      if (p.transformed) {
         context.method_51448().method_22909();
         IRenderer.DEFAULT_MATRIX.identity();
         float size = p.size.getFloatValue();
         if (Math.abs(size - 1.0F) > 0.001F) {
            p.drag.setWidth(p.drag.getWidth() * size);
            p.drag.setHeight(p.drag.getHeight() * size);
         }
      }
   }

   private void runPopup(Interface.HudPopup p, DrawContext context) {
      p.anim.run(p.open ? 1.0F : 0.0F);
      if (p.anim.getValue() > 0.01F) {
         this.renderSettingsPopup(p, context);
      }
   }

   private void drawElementBackground(Interface.HudPopup p, float x, float y, float w, float h, float radius, float alphaFactor) {
      float clampFactor = MathHelper.method_15363(alphaFactor, 0.0F, 1.0F);
      int alpha = (int)((float)p.alpha.getIntValue() * clampFactor);
      alpha = Math.min(255, Math.max(0, alpha));
      if (p.blur.getValue()) {
         int blurAlpha = Math.min(255, Math.max(0, (int)(255.0F * clampFactor)));
         DrawUtil.drawRoundBlur(x, y, w, h, radius, ColorProvider.rgba(200, 200, 200, (float)blurAlpha), 12.0F);
         DrawUtil.drawRound(x, y, w, h, radius, ColorProvider.setAlpha(ColorProvider.getColorInterfaceBg(), alpha));
      } else {
         DrawUtil.drawRound(x, y, w, h, radius, ColorProvider.setAlpha(ColorProvider.getColorInterfaceBg(), alpha));
      }

      this.drawElementCorners(p, x, y, w, h, radius, clampFactor);
   }

   private void drawElementCorners(Interface.HudPopup p, float x, float y, float w, float h, float radius, float fadeFactor) {
      if (p.corners.getValue()) {
         int cornerAlpha = Math.min(255, Math.max(0, (int)(255.0F * fadeFactor)));
         int c = ColorProvider.setAlpha(ColorProvider.getColorClient(), cornerAlpha);
         float th = 1.6F;
         float rc = Math.max(radius, 2.0F);
         float outer = rc + th / 2.0F;
         DrawUtil.drawRingArc(x + rc, y + rc, outer, th, 180.0F, 270.0F, c);
         DrawUtil.drawRingArc(x + w - rc, y + rc, outer, th, 270.0F, 360.0F, c);
         DrawUtil.drawRingArc(x + w - rc, y + h - rc, outer, th, 0.0F, 90.0F, c);
         DrawUtil.drawRingArc(x + rc, y + h - rc, outer, th, 90.0F, 180.0F, c);
      }
   }

   private void drawElementShine(Interface.HudPopup p, DrawContext context, float x, float y, float w, float h, float radius) {
      if (p.shine.getValue()) {
         HudShine.render(context.method_51448(), x, y, w, h, radius, p.shineThickness.getFloatValue(), 1.0F, p.shineAlpha.getFloatValue() / 100.0F, 0.0F);
      }
   }

   private void renderPotionsNew(DrawContext context) {
      if (this.mc.field_1724 != null) {
         float posX = this.potionsDrag.getX();
         float posY = this.potionsDrag.getY();
         float headerHeight = 14.0F;
         float itemHeight = 12.0F;
         float minWidth = 52.0F;
         float padX = 5.0F;
         float padY = 2.0F;
         boolean isFound = false;

         for (Interface.PotionItem item : this.potionItems) {
            item.animation.run(item.active ? 1.0F : 0.0F);
            item.rowAnim.run(item.active ? 1.0F : 0.0F);
            if (item.animation.getValue() > 0.001F) {
               isFound = true;
            }
         }

         if (!isFound && !(this.mc.field_1755 instanceof ChatScreen)) {
            this.alpha3.run(0.0F);
         } else {
            this.alpha3.run(1.0F);
         }

         float globalAlpha = this.alpha3.getValue();
         if (!(globalAlpha <= 0.05F)) {
            int headerAlpha = (int)Math.min(255.0F, Math.max(0.0F, 255.0F * globalAlpha));
            boolean showExample = this.mc.field_1755 instanceof ChatScreen && !isFound;
            if (showExample) {
               long currentTime = System.currentTimeMillis();
               if (currentTime - this.lastEffectChange > 2000L) {
                  if (cachedAllEffects == null) {
                     cachedAllEffects = new ArrayList<>();
                     Registries.field_41174.method_42017().forEach(cachedAllEffects::add);
                  }

                  if (!cachedAllEffects.isEmpty()) {
                     this.currentRandomEffect = cachedAllEffects.get(RANDOM.nextInt(cachedAllEffects.size()));
                     this.lastEffectChange = currentTime;
                  }
               }
            }

            float maxNameBoxW = minWidth;
            float maxTimeBoxW = 0.0F;
            float contentHeight = 0.0F;

            for (Interface.PotionItem itemx : this.potionItems) {
               float animVal = itemx.animation.getValue();
               if (animVal > 0.001F) {
                  contentHeight += itemHeight * itemx.rowAnim.getValue();
                  String lvlStr = itemx.amplifier >= 1 ? "LVL " + (itemx.amplifier + 1) : "";
                  float nameW = Fonts.SFMEDIUM.get().getWidth(itemx.name, 7.0F);
                  float lvlW = lvlStr.isEmpty() ? 0.0F : Fonts.SFMEDIUM.get().getWidth(lvlStr, 6.0F);
                  float nw = nameW + lvlW + 8.0F + 10.0F + 10.0F;
                  if (nw > maxNameBoxW) {
                     maxNameBoxW = nw;
                  }

                  int seconds = itemx.durationTicks / 20;
                  int minutes = seconds / 60;
                  int sec = seconds % 60;
                  String timeStr = String.format("%d:%02d", minutes, sec);
                  float tw = Fonts.SFMEDIUM.get().getWidth(timeStr, 6.75F) + 10.0F + (this.potionsRing.getValue() ? 11.0F : 0.0F);
                  if (tw > maxTimeBoxW) {
                     maxTimeBoxW = tw;
                  }
               }
            }

            if (showExample) {
               float exNameW = Fonts.SFMEDIUM.get().getWidth("Example", 7.0F);
               if (exNameW + 8.0F + 10.0F + 10.0F > maxNameBoxW) {
                  maxNameBoxW = exNameW + 8.0F + 10.0F + 10.0F;
               }

               float exTimeW = Fonts.SFMEDIUM.get().getWidth("**:**", 6.75F) + 10.0F + (this.potionsRing.getValue() ? 11.0F : 0.0F);
               if (exTimeW > maxTimeBoxW) {
                  maxTimeBoxW = exTimeW;
               }

               contentHeight = itemHeight;
            }

            float rawWidth = maxNameBoxW + maxTimeBoxW + padX * 2.0F;
            this.potionsPopup.panelWidth.run(rawWidth);
            float totalRowWidth = this.potionsPopup.panelWidth.getValue();
            if (totalRowWidth < 20.0F) {
               totalRowWidth = rawWidth;
            }

            float totalHeight = headerHeight + contentHeight + padY * 2.0F;
            this.drawElementBackground(this.potionsPopup, posX, posY, totalRowWidth, totalHeight, 3.0F, globalAlpha);
            this.drawElementShine(this.potionsPopup, context, posX, posY, totalRowWidth, totalHeight, 3.0F);
            DrawUtil.drawText(
               Fonts.SFMEDIUM.get(), "Potions", posX + padX + 4.0F, posY + padY + 2.5F, ColorProvider.rgba(255, 255, 255, (float)headerAlpha), 8.0F
            );
            DrawUtil.drawText(
               Fonts.ALPHADLC.get(),
               "f",
               posX + totalRowWidth - padX - 11.0F,
               posY + padY + 3.0F,
               ColorProvider.setAlpha(ColorProvider.getColorIcons(), headerAlpha),
               9.0F
            );
            float curY = posY + headerHeight + padY;

            for (Interface.PotionItem itemxx : this.potionItems) {
               float animVal = itemxx.animation.getValue();
               if (!(animVal <= 0.001F)) {
                  float rowAnimVal = itemxx.rowAnim.getValue();
                  if (!(rowAnimVal <= 0.001F)) {
                     int seconds = itemxx.durationTicks / 20;
                     int minutes = seconds / 60;
                     int sec = seconds % 60;
                     String timeStr = String.format("%d:%02d", minutes, sec);
                     String nameStr = itemxx.name;
                     boolean isHarmful = !((StatusEffect)itemxx.effect.comp_349()).method_5573();
                     String effectId = Registries.field_41174.method_10221((StatusEffect)itemxx.effect.comp_349()).method_12832();
                     boolean isNightVision = effectId.equals("night_vision");
                     int textAlpha = 255;
                     if (itemxx.durationTicks <= 200 && itemxx.durationTicks > 0 && !isNightVision) {
                        double output = 0.5 + 0.5 * Math.cos((Math.PI * 2) * (double)(System.currentTimeMillis() % 700L) / 700.0);
                        textAlpha = (int)(100.0 + 155.0 * output);
                     } else if (itemxx.durationTicks == 0) {
                        textAlpha = 0;
                     }

                     int itemAlpha = (int)Math.min(255.0F, Math.max(0.0F, (float)textAlpha * animVal * globalAlpha));
                     if (itemAlpha >= 5) {
                        float nameWx = Fonts.SFMEDIUM.get().getWidth(nameStr, 7.0F);
                        String lvlStrx = itemxx.amplifier >= 1 ? "LVL " + (itemxx.amplifier + 1) : "";
                        if (lvlStrx.isEmpty()) {
                           float var10000 = 0.0F;
                        } else {
                           Fonts.SFMEDIUM.get().getWidth(lvlStrx, 6.0F);
                        }

                        float timeW = Fonts.SFMEDIUM.get().getWidth(timeStr, 6.75F);
                        float timeBoxW = timeW + 10.0F;
                        float rowHeight = itemHeight * rowAnimVal;
                        context.method_51448().method_22903();
                        context.method_51448().method_46416(posX + totalRowWidth / 2.0F, curY + rowHeight / 2.0F, 0.0F);
                        context.method_51448().method_22905(rowAnimVal, rowAnimVal, rowAnimVal);
                        context.method_51448().method_46416(-(posX + totalRowWidth / 2.0F), -(curY + rowHeight / 2.0F), 0.0F);
                        float potionMid = textMidY(curY + 2.5F, 7.0F);
                        Sprite sprite = this.mc.method_18505().method_18663(itemxx.effect);
                        if (sprite != null) {
                           float iconSize = 8.0F;
                           float iconX = posX + padX + 2.0F;
                           float iconY = potionMid - iconSize / 2.0F;
                           int color = itemAlpha << 24 | 16777215;
                           RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, (float)itemAlpha / 255.0F);
                           context.method_52710(RenderLayer::method_62277, sprite, (int)iconX, (int)iconY, (int)iconSize, (int)iconSize, color);
                           RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                        }

                        float dotX = posX + padX + 11.0F;
                        float dotY = potionMid - 1.5F;
                        DrawUtil.drawRound(dotX, dotY, 3.0F, 3.0F, 1.5F, ColorProvider.rgba(120, 120, 120, (float)itemAlpha));
                        int nameColor = isHarmful ? ColorProvider.rgba(255, 80, 80, (float)itemAlpha) : ColorProvider.rgba(255, 255, 255, (float)itemAlpha);
                        float textX = posX + padX + 18.0F;
                        DrawUtil.drawText(Fonts.SFMEDIUM.get(), nameStr, textX, curY + 2.5F, nameColor, 7.0F);
                        if (!lvlStrx.isEmpty()) {
                           float lvlX = textX + nameWx + 6.0F;
                           DrawUtil.drawText(Fonts.SFMEDIUM.get(), lvlStrx, lvlX, curY + 3.5F, ColorProvider.rgba(160, 160, 160, (float)itemAlpha), 6.0F);
                        }

                        float timerX = posX + totalRowWidth - padX - timeBoxW;
                        if (this.potionsRing.getValue()) {
                           float ringRadius = 4.0F;
                           float ringCX = timerX - 3.0F - ringRadius;
                           float ringCY = curY + itemHeight / 2.0F;
                           float progress = 1.0F;
                           if (itemxx.maxDurationTicks > 0 && itemxx.durationTicks >= 0) {
                              progress = MathHelper.method_15363((float)itemxx.durationTicks / (float)itemxx.maxDurationTicks, 0.0F, 1.0F);
                           }

                           DrawUtil.drawRingArc(ringCX, ringCY, ringRadius, 1.4F, -90.0F, 270.0F, ColorProvider.rgba(80, 80, 80, (float)itemAlpha));
                           if (progress > 0.0F) {
                              DrawUtil.drawRingArc(
                                 ringCX,
                                 ringCY,
                                 ringRadius,
                                 1.4F,
                                 -90.0F,
                                 -90.0F + 360.0F * progress,
                                 ColorProvider.setAlpha(ColorProvider.getColorClient(), itemAlpha)
                              );
                           }
                        }

                        String timerKey = "potions_duration_" + effectId + "_" + itemxx.amplifier;
                        TimerTextAnimator.draw(
                           Fonts.SFMEDIUM.get(),
                           timerKey,
                           timeStr,
                           timerX + (timeBoxW - timeW) / 2.0F + 1.0F,
                           curY + 2.5F,
                           ColorProvider.rgba(255, 255, 255, (float)itemAlpha),
                           6.75F
                        );
                        context.method_51448().method_22909();
                        curY += rowHeight;
                     }
                  }
               }
            }

            if (showExample && this.currentRandomEffect != null) {
               String nameStrx = "Example";
               String timeStrx = "**:**";
               float nameWxx = Fonts.SFMEDIUM.get().getWidth(nameStrx, 7.0F);
               float timeWx = Fonts.SFMEDIUM.get().getWidth(timeStrx, 6.75F);
               float timeBoxWx = timeWx + 10.0F;
               float exMid = textMidY(curY + 2.5F, 7.0F);
               Sprite spritex = this.mc.method_18505().method_18663(this.currentRandomEffect);
               if (spritex != null) {
                  float iconSize = 8.0F;
                  float iconX = posX + padX + 2.0F;
                  float iconY = exMid - iconSize / 2.0F;
                  int color = headerAlpha << 24 | 16777215;
                  RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, (float)headerAlpha / 255.0F);
                  context.method_52710(RenderLayer::method_62277, spritex, (int)iconX, (int)iconY, (int)iconSize, (int)iconSize, color);
                  RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
               }

               float dotXx = posX + padX + 11.0F;
               float dotYx = exMid - 1.5F;
               DrawUtil.drawRound(dotXx, dotYx, 3.0F, 3.0F, 1.5F, ColorProvider.rgba(120, 120, 120, (float)headerAlpha));
               float textXx = posX + padX + 18.0F;
               DrawUtil.drawText(Fonts.SFMEDIUM.get(), nameStrx, textXx, curY + 2.5F, ColorProvider.rgba(255, 255, 255, (float)headerAlpha), 7.0F);
               float timerX = posX + totalRowWidth - padX - timeBoxWx;
               if (this.potionsRing.getValue()) {
                  float ringRadiusx = 4.0F;
                  float ringCXx = timerX - 3.0F - ringRadiusx;
                  float ringCYx = curY + itemHeight / 2.0F;
                  DrawUtil.drawRingArc(ringCXx, ringCYx, ringRadiusx, 1.4F, -90.0F, 270.0F, ColorProvider.rgba(80, 80, 80, (float)headerAlpha));
                  DrawUtil.drawRingArc(ringCXx, ringCYx, ringRadiusx, 1.4F, -90.0F, 180.0F, ColorProvider.setAlpha(ColorProvider.getColorClient(), headerAlpha));
               }

               DrawUtil.drawText(
                  Fonts.SFMEDIUM.get(),
                  timeStrx,
                  timerX + (timeBoxWx - timeWx) / 2.0F + 1.0F,
                  curY + 2.5F,
                  ColorProvider.rgba(255, 255, 255, (float)headerAlpha),
                  6.75F
               );
            }

            this.potionsDrag.setWidth(totalRowWidth);
            this.potionsDrag.setHeight(totalHeight);
         }
      }
   }

   private void renderSettingsPopup(Interface.HudPopup p, DrawContext context) {
      List<Interface.PopupRow> rows = this.buildRows(p);
      float w = 96.0F;

      for (Interface.PopupRow r : rows) {
         float lw = Fonts.SFMEDIUM.get().getWidth(r.label, 6.75F);
         float need = lw + 12.0F + (r.kind == Interface.PopupKind.TOGGLE ? 22.0F : 34.0F);
         if (need > w) {
            w = need;
         }
      }

      float totalH = 19.0F;

      for (Interface.PopupRow rx : rows) {
         totalH += rx.kind == Interface.PopupKind.TOGGLE ? 14.0F : 19.0F;
      }

      float ex = p.drag.getX();
      float ey = p.drag.getY();
      float ew = p.drag.getWidth();
      float screenW = (float)this.mc.method_22683().method_4486();
      float screenH = (float)this.mc.method_22683().method_4502();
      float x = p.title.equals("CustomHotbar") ? ex - w - 4.0F : ex + ew + 4.0F;
      x = MathHelper.method_15363(x, 2.0F, Math.max(2.0F, screenW - w - 2.0F));
      float y = MathHelper.method_15363(ey, 2.0F, Math.max(2.0F, screenH - totalH - 2.0F));
      p.px = x;
      p.py = y;
      p.pw = w;
      p.ph = totalH;
      float anim = p.anim.getValue();
      int a = (int)Math.min(255.0F, Math.max(0.0F, 255.0F * anim));
      this.drawBackground(x, y, w, totalH, 4.0F, a);
      DrawUtil.drawText(
         Fonts.SFMEDIUM.get(), "\u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438", x + 6.0F, y + 4.5F, ColorProvider.rgba(255, 255, 255, (float)a), 7.5F
      );
      DrawUtil.drawRound(x + 5.0F, y + 15.0F - 1.5F, w - 10.0F, 0.5F, 0.0F, ColorProvider.rgba(120, 120, 120, (float)a));
      float cy = y + 15.0F;

      for (Interface.PopupRow rx : rows) {
         rx.x = x;
         rx.y = cy;
         rx.w = w;
         rx.h = rx.kind == Interface.PopupKind.TOGGLE ? 14.0F : 19.0F;
         if (rx.kind == Interface.PopupKind.TOGGLE) {
            this.drawPotionsToggleRow(rx.label, rx.bool, rx.x, rx.y, rx.w, rx.h, a);
         } else {
            this.drawPotionsSliderRow(rx.label, rx.slider, rx.x, rx.y, rx.w, rx.h, a);
         }

         cy += rx.h;
      }

      p.rendered.clear();
      p.rendered.addAll(rows);
      if (p.draggingSlider != null) {
         double val = (this.potionsMouseX() - (double)p.trackX) / (double)p.trackW * (p.draggingSlider.getMax() - p.draggingSlider.getMin())
            + p.draggingSlider.getMin();
         double stepped = (double)Math.round(val / p.draggingSlider.getStep()) * p.draggingSlider.getStep();
         p.draggingSlider.setValue(stepped);
      }
   }

   private void drawPotionsToggleRow(String label, BooleanSetting setting, float x, float y, float w, float h, int a) {
      setting.getAnimation().run(setting.getValue());
      DrawUtil.drawText(Fonts.SFMEDIUM.get(), label, x + 6.0F, y + h / 2.0F - 3.25F, ColorProvider.rgba(255, 255, 255, (float)a), 6.75F);
      float toggleW = 15.0F;
      float toggleH = 8.0F;
      float toggleX = x + w - toggleW - 6.0F;
      float toggleY = y + (h - toggleH) / 2.0F;
      float tAnim = setting.getAnimation().getValue();
      int inactive = ColorProvider.setAlpha(ColorProvider.getColorInactiveIndicator(), a);
      int active = ColorProvider.setAlpha(ColorProvider.getColorIndicator(), a);
      int bg = ColorProvider.interpolateColor(inactive, active, tAnim);
      DrawUtil.drawRound(toggleX, toggleY, toggleW, toggleH, toggleH / 2.0F, bg);
      float knob = toggleH - 1.0F;
      float knobMinX = toggleX + 0.5F;
      float knobMaxX = toggleX + toggleW - knob - 0.5F;
      float knobX = knobMinX + (knobMaxX - knobMinX) * tAnim;
      DrawUtil.drawCircle(knobX + knob / 2.0F, toggleY + 0.5F + knob / 2.0F, knob / 2.0F, ColorProvider.setAlpha(ColorProvider.getColorSliderCircle(), a));
   }

   private String formatSliderValue(SliderSetting s) {
      return s.getStep() < 1.0 ? String.format(Locale.US, "%.2f", s.getValue()) : String.valueOf(s.getIntValue());
   }

   private void drawPotionsSliderRow(String label, SliderSetting setting, float x, float y, float w, float h, int a) {
      DrawUtil.drawText(Fonts.SFMEDIUM.get(), label, x + 6.0F, y + 2.5F, ColorProvider.rgba(255, 255, 255, (float)a), 6.5F);
      String valStr = this.formatSliderValue(setting);
      float valW = Fonts.SFMEDIUM.get().getWidth(valStr, 6.5F);
      DrawUtil.drawText(Fonts.SFMEDIUM.get(), valStr, x + w - 6.0F - valW, y + 2.5F, ColorProvider.setAlpha(ColorProvider.getColorInactiveText(), a), 6.5F);
      float trackX = x + 6.0F;
      float trackW = w - 12.0F;
      float trackY = y + h - 5.5F;
      DrawUtil.drawRound(trackX, trackY, trackW, 3.0F, 1.0F, ColorProvider.setAlpha(ColorProvider.getColorSliderWindow(), a));
      float fill = (float)((double)trackW * (setting.getValue() - setting.getMin()) / (setting.getMax() - setting.getMin()));
      fill = MathHelper.method_15363(fill, 0.0F, trackW);
      DrawUtil.drawRound(trackX, trackY, fill, 3.0F, 1.0F, ColorProvider.setAlpha(ColorProvider.getColorSlider(), a));
      float circleX = trackX + fill;
      DrawUtil.drawRound(circleX - 2.5F, trackY - 1.0F, 5.0F, 5.0F, 1.75F, ColorProvider.setAlpha(ColorProvider.getColorSliderCircle(), a));
   }

   public boolean isPotionsActive() {
      return this.isEnabled() && this.elements.isEnabled("\u0411\u0430\u0444\u044b");
   }

   public boolean isCustomHotbarActive() {
      return this.isEnabled() && this.elements.isEnabled("\u041a\u0430\u0441\u0442\u043e\u043c\u043d\u044b\u0439 \u0445\u043e\u0442\u0431\u0430\u0440");
   }

   public boolean handlePotionsClick(double mouseX, double mouseY, int button) {
      if (!this.isEnabled() || !(this.mc.field_1755 instanceof ChatScreen)) {
         return false;
      } else if (this.elements.isEnabled("\u0411\u0430\u0444\u044b") && this.handleElementClick(this.potionsPopup, mouseX, mouseY, button)) {
         return true;
      } else if (this.elements.isEnabled("\u041a\u0443\u043b\u0414\u0430\u0443\u043d\u044b")
         && this.handleElementClick(this.cooldownsPopup, mouseX, mouseY, button)) {
         return true;
      } else if (this.elements.isEnabled("ServerHelper") && this.handleElementClick(this.serverHelperPopup, mouseX, mouseY, button)) {
         return true;
      } else if (this.elements.isEnabled("\u041f\u0440\u0438\u0432\u044f\u0437\u0430\u043d\u043d\u044b\u0435 \u043c\u043e\u0434\u0443\u043b\u0438")
         && this.handleElementClick(this.keybindsPopup, mouseX, mouseY, button)) {
         return true;
      } else if (this.elements.isEnabled("\u0410\u043a\u0442\u0438\u0432\u043d\u044b\u0435 \u043c\u043e\u0434\u0435\u0440\u0430\u0442\u043e\u0440\u044b")
         && this.handleElementClick(this.stafflistPopup, mouseX, mouseY, button)) {
         return true;
      } else if (this.elements.isEnabled("\u0412\u0430\u0442\u0435\u0440\u043c\u0430\u0440\u043a\u0430")
         && this.handleElementClick(this.watermarkPopup, mouseX, mouseY, button)) {
         return true;
      } else if (this.elements.isEnabled("\u0418\u043d\u0444\u043e") && this.handleElementClick(this.infoPopup, mouseX, mouseY, button)) {
         return true;
      } else if (this.elements.isEnabled("\u0410\u043a\u0442\u0438\u0432\u043d\u044b\u0439 \u0442\u0430\u0440\u0433\u0435\u0442")
         && this.handleElementClick(this.targetHudPopup, mouseX, mouseY, button)) {
         return true;
      } else if (this.elements.isEnabled("\u041d\u043e\u0442\u0438\u0444\u0438\u043a\u0430\u0446\u0438\u0438")
         && this.handleElementClick(this.notificationsPopup, mouseX, mouseY, button)) {
         return true;
      } else if (this.elements.isEnabled("\u041a\u0430\u0441\u0442\u043e\u043c\u043d\u044b\u0439 \u0445\u043e\u0442\u0431\u0430\u0440")
         && this.handleElementClick(this.hotbarPopup, mouseX, mouseY, button)) {
         return true;
      } else {
         return this.elements.isEnabled("\u0411\u0440\u043e\u043d\u044f") && this.handleElementClick(this.armourPopup, mouseX, mouseY, button)
            ? true
            : this.elements.isEnabled("\u041f\u043e\u043b\u043e\u0441\u0430 \u0442\u043e\u0442\u0435\u043c\u043e\u0432")
               && this.handleElementClick(this.totemBarPopup, mouseX, mouseY, button);
      }
   }

   private boolean handleElementClick(Interface.HudPopup p, double mouseX, double mouseY, int button) {
      if (p.open && p.anim.getValue() > 0.5F) {
         if (button == 0) {
            for (Interface.PopupRow r : p.rendered) {
               if (HoverUtil.isHovered(mouseX, mouseY, (double)r.x, (double)r.y, (double)r.w, (double)r.h)) {
                  if (r.kind == Interface.PopupKind.TOGGLE) {
                     r.bool.toggle();
                  } else {
                     p.draggingSlider = r.slider;
                     p.trackX = r.x + 6.0F;
                     p.trackW = r.w - 12.0F;
                     double val = (mouseX - (double)p.trackX) / (double)p.trackW * (r.slider.getMax() - r.slider.getMin()) + r.slider.getMin();
                     double stepped = (double)Math.round(val / r.slider.getStep()) * r.slider.getStep();
                     r.slider.setValue(stepped);
                  }

                  return true;
               }
            }
         }

         if (HoverUtil.isHovered(mouseX, mouseY, (double)p.px, (double)p.py, (double)p.pw, (double)p.ph)) {
            return true;
         }
      }

      if (button == 1 && p.drag.isHovering()) {
         p.open = !p.open;
         return true;
      } else {
         if (p.open && !p.drag.isHovering()) {
            p.open = false;
         }

         return false;
      }
   }

   public void handlePotionsRelease(int button) {
      if (button == 0) {
         this.potionsPopup.draggingSlider = null;
         this.cooldownsPopup.draggingSlider = null;
         this.serverHelperPopup.draggingSlider = null;
         this.keybindsPopup.draggingSlider = null;
         this.stafflistPopup.draggingSlider = null;
         this.watermarkPopup.draggingSlider = null;
         this.infoPopup.draggingSlider = null;
         this.targetHudPopup.draggingSlider = null;
         this.notificationsPopup.draggingSlider = null;
         this.hotbarPopup.draggingSlider = null;
         this.armourPopup.draggingSlider = null;
         this.totemBarPopup.draggingSlider = null;
      }
   }

   public void update() {
      for (Interface.Staff staff : this.staffPlayers) {
         staff.isOnServer = false;
      }

      Iterator var28 = this.mc.method_1562().method_2880().iterator();

      while (true) {
         String name;
         boolean vanish;
         boolean isGM3;
         PlayerListEntry playerListEntry;
         while (true) {
            if (!var28.hasNext()) {
               this.staffPlayers.removeIf(staffx -> !staffx.isOnServer && staffx.animation.getValue() == 0.0F);
               return;
            }

            playerListEntry = (PlayerListEntry)var28.next();
            name = playerListEntry.method_2966().getName().replaceAll("[\\[\\]]", "");
            PlayerListEntry info = MinecraftClient.method_1551().method_1562().method_2874(name);
            vanish = info == null;
            isGM3 = info != null && info.method_2958() == GameMode.field_9219;
            boolean matchesPrefix = this.prefixMatches
               .matcher(playerListEntry.method_2971() != null ? playerListEntry.method_2971().getString().toLowerCase(Locale.ROOT) : "")
               .matches();
            boolean isValidName = this.namePattern.matcher(name).matches();
            boolean notSelf = !name.equals(MinecraftClient.method_1551().field_1724.method_5477().getString());
            if (isValidName && notSelf && matchesPrefix || isValidName && notSelf && vanish || StaffManager.isStaff(name)) {
               if (!StaffManager.isStaff(name)) {
                  break;
               }

               String[] names = new String[]{
                  "auction",
                  "exp_smith",
                  "shop_balls",
                  "shop_grief",
                  "free",
                  "shop_kits",
                  "siege",
                  "rwplus",
                  "bossfight",
                  "guide",
                  "shop_smith",
                  "shop_spawners",
                  "colliseum",
                  "battlepass",
                  "buyer",
                  "huckster",
                  "buff_brewer",
                  "killer",
                  "shop_mage"
               };
               boolean contains = false;
               if (MinecraftClient.method_1551().method_1558() != null
                  && MinecraftClient.method_1551().method_1558().field_3761 != null
                  && (
                     MinecraftClient.method_1551().method_1558().field_3761.contains("mc.rwdonat.pw")
                        || MinecraftClient.method_1551().method_1558().field_3761.contains("mc.cakeworld.pw")
                  )) {
                  for (int i = 0; (long)i < Arrays.stream(names).count(); i++) {
                     if (name.contains(names[i])) {
                        contains = true;
                        break;
                     }
                  }
               }

               if (!contains) {
                  break;
               }
            }
         }

         Optional<Interface.Staff> existingStaff = this.staffPlayers.stream().filter(s -> s.name.equals(name)).findFirst();
         Interface.Status status = vanish ? Interface.Status.VANISHED : (isGM3 ? Interface.Status.VANISHED : Interface.Status.NONE);
         if (existingStaff.isPresent()) {
            Interface.Staff s = existingStaff.get();
            s.isOnServer = true;
            s.status = status;
         } else {
            String[] namesx = new String[]{
               "auction",
               "exp_smith",
               "shop_balls",
               "shop_grief",
               "free",
               "shop_kits",
               "siege",
               "rwplus",
               "bossfight",
               "guide",
               "shop_smith",
               "shop_spawners",
               "colliseum",
               "battlepass",
               "buyer",
               "huckster",
               "buff_brewer",
               "killer",
               "shop_mage"
            };
            boolean containsx = false;
            if (MinecraftClient.method_1551().method_1558() != null
               && MinecraftClient.method_1551().method_1558().field_3761 != null
               && (
                  MinecraftClient.method_1551().method_1558().field_3761.contains("mc.rwdonat.pw")
                     || MinecraftClient.method_1551().method_1558().field_3761.contains("mc.cakeworld.pw")
               )) {
               for (int ix = 0; (long)ix < Arrays.stream(namesx).count(); ix++) {
                  if (name.contains(namesx[ix])) {
                     containsx = true;
                  }
               }
            }

            if (!containsx) {
               Text originalPrefix = playerListEntry.method_2971();
               Text prefix = originalPrefix;
               if (originalPrefix != null) {
                  prefix = ReplaceUtil.replaceSymbols(originalPrefix);
                  String fullString = prefix.getString();
                  int nickIndex = fullString.indexOf(name);
                  if (nickIndex != -1) {
                     int endIndex = nickIndex + name.length();
                     if (endIndex < fullString.length()) {
                        MutableText newText = Text.method_43473();
                        int currentLength = 0;
                        MutableText baseCopy = prefix.method_27661();
                        baseCopy.method_10855().clear();
                        String mainContent = baseCopy.getString();
                        if (!mainContent.isEmpty() && currentLength < endIndex) {
                           int takeLength = Math.min(mainContent.length(), endIndex - currentLength);
                           newText.method_10852(Text.method_43470(mainContent.substring(0, takeLength)).method_10862(prefix.method_10866()));
                           currentLength += takeLength;
                        }

                        for (Text sibling : prefix.method_10855()) {
                           if (currentLength >= endIndex) {
                              break;
                           }

                           MutableText siblingCopy = sibling.method_27661();
                           siblingCopy.method_10855().clear();
                           String siblingContent = siblingCopy.getString();
                           int takeLength = Math.min(siblingContent.length(), endIndex - currentLength);
                           if (takeLength > 0) {
                              newText.method_10852(Text.method_43470(siblingContent.substring(0, takeLength)).method_10862(sibling.method_10866()));
                              currentLength += takeLength;
                           }
                        }

                        prefix = newText;
                     }
                  }
               }

               Interface.Staff staff = new Interface.Staff(
                  prefix == null ? Text.method_30163(playerListEntry.method_2966().getName()) : prefix, name, vanish || isGM3, status
               );
               staff.isOnServer = true;
               this.staffPlayers.add(staff);
            }
         }
      }
   }

   public int getPing(PlayerEntity entity) {
      PlayerListEntry list = this.mc.method_1562().method_2871(entity.method_5667());
      return list != null ? list.method_2959() : 0;
   }

   private void renderWatermark(DrawContext context) {
      if (this.mc.field_1724 != null) {
         boolean chatOpen = this.mc.field_1755 instanceof ChatScreen;
         if (!chatOpen) {
            this.watermarkPopup.open = false;
            this.watermarkPopup.draggingSlider = null;
         }

         this.beginScale(this.watermarkPopup, context);
         this.renderWatermarkNew(context);
         this.endScale(this.watermarkPopup, context);
         this.runPopup(this.watermarkPopup, context);
      }
   }

   private void renderInfo(DrawContext context) {
      if (this.mc.field_1724 != null) {
         boolean chatOpen = this.mc.field_1755 instanceof ChatScreen;
         if (!chatOpen) {
            this.infoPopup.open = false;
            this.infoPopup.draggingSlider = null;
         }

         this.beginScale(this.infoPopup, context);
         this.renderInfoNew(context);
         this.endScale(this.infoPopup, context);
         this.runPopup(this.infoPopup, context);
      }
   }

   private void renderWatermarkNew(DrawContext context) {
      Counter.updateFPS();
      String userText = "User";
      String fpsValue = Counter.getCurrentFPS() + " Fps";
      String pingValue = Server.getPing(this.mc.field_1724) + " Ping";
      float x = this.watermarkDrag.getX();
      float y = this.watermarkDrag.getY();
      float height = 16.5F;
      int iconColor = ColorProvider.getColorIcons();
      int whiteColor = -1;
      int dotColor = ColorProvider.rgba(255, 255, 255, 60.0F);
      String pingIcon = "Q";
      String logoIcon = "4";
      String title = "LEAK BY NEDO t.me/Legit_DLC";
      float logoIconW = Fonts.ICONS2.get().getWidth(logoIcon, 7.0F);
      float titleW = Fonts.SFMEDIUM.get().getWidth(title, 7.0F);
      float userIconW = Fonts.ALPHADLC.get().getWidth("b", 7.0F);
      float fpsIconW = Fonts.ALPHADLC.get().getWidth("n", 7.0F);
      float pingIconW = Fonts.ICONS_NURIK.get().getWidth(pingIcon, 7.0F);
      float userW = Fonts.SFMEDIUM.get().getWidth(userText, 7.0F);
      float fpsW = Fonts.SFMEDIUM.get().getWidth(fpsValue, 7.0F);
      float pingW = Fonts.SFMEDIUM.get().getWidth(pingValue, 7.0F);
      float iconGap = 1.5F;
      float sepGap = 3.5F;
      float dotSize = 3.0F;
      float sep = sepGap + dotSize + sepGap;
      float boxWidth = 5.0F
         + logoIconW
         + iconGap
         + titleW
         + sep
         + userIconW
         + iconGap
         + userW
         + sep
         + fpsIconW
         + iconGap
         + fpsW
         + sep
         + pingIconW
         + iconGap
         + pingW
         + 5.0F;
      this.drawElementBackground(this.watermarkPopup, x, y, boxWidth, height, 3.0F, 1.0F);
      this.drawElementShine(this.watermarkPopup, context, x, y, boxWidth, height, 3.0F);
      float dotY = y + (height - dotSize) / 2.0F;
      float cx = x + 5.0F;
      DrawUtil.drawText(Fonts.ICONS2.get(), logoIcon, cx - 0.5F, y + 3.5F, iconColor, 7.0F);
      cx += logoIconW + iconGap;
      DrawUtil.drawText(Fonts.SFMEDIUM.get(), title, cx, y + 4.0F, whiteColor, 7.0F);
      cx += titleW + sepGap;
      DrawUtil.drawRound(cx, dotY, dotSize, dotSize, dotSize / 2.0F, dotColor);
      cx += dotSize + sepGap;
      DrawUtil.drawText(Fonts.ALPHADLC.get(), "b", cx, y + 4.75F, iconColor, 7.0F);
      cx += userIconW + iconGap;
      DrawUtil.drawText(Fonts.SFMEDIUM.get(), userText, cx, y + 4.0F, whiteColor, 7.0F);
      cx += userW + sepGap;
      DrawUtil.drawRound(cx, dotY, dotSize, dotSize, dotSize / 2.0F, dotColor);
      cx += dotSize + sepGap;
      DrawUtil.drawText(Fonts.ALPHADLC.get(), "n", cx, y + 4.75F, iconColor, 7.0F);
      cx += fpsIconW + iconGap;
      DrawUtil.drawText(Fonts.SFMEDIUM.get(), fpsValue, cx, y + 4.0F, whiteColor, 7.0F);
      cx += fpsW + sepGap;
      DrawUtil.drawRound(cx, dotY, dotSize, dotSize, dotSize / 2.0F, dotColor);
      cx += dotSize + sepGap;
      DrawUtil.drawText(Fonts.ICONS_NURIK.get(), pingIcon, cx, y + 4.75F, iconColor, 7.0F);
      cx += pingIconW + iconGap;
      DrawUtil.drawText(Fonts.SFMEDIUM.get(), pingValue, cx, y + 4.0F, whiteColor, 7.0F);
      this.watermarkDrag.setWidth(boxWidth);
      this.watermarkDrag.setHeight(height);
   }

   private void renderInfoNew(DrawContext context) {
      boolean showCoords = this.wmCoords.getValue();
      boolean showTps = this.wmTps.getValue();
      boolean showBps = this.wmBps.getValue();
      float x = this.infoDrag.getX();
      float y = this.infoDrag.getY();
      float height = 16.5F;
      int iconColor = ColorProvider.getColorIcons();
      int whiteColor = -1;
      int dotColor = ColorProvider.rgba(255, 255, 255, 60.0F);
      MsdfFont msdf = Fonts.SFMEDIUM.get();
      MsdfFont icons = Fonts.ICONS_NURIK.get();
      float iconGap = 1.5F;
      float sepGap = 3.5F;
      float dotSize = 3.0F;
      float sep = sepGap + dotSize + sepGap;
      float leftPad = 5.0F;
      float rightPad = 5.0F;
      if (!showCoords && !showTps && !showBps) {
         if (this.mc.field_1755 instanceof ChatScreen) {
            String ph = "Info";
            float w = leftPad + msdf.getWidth(ph, 7.0F) + rightPad;
            this.drawElementBackground(this.infoPopup, x, y, w, height, 3.0F, 1.0F);
            this.drawElementShine(this.infoPopup, context, x, y, w, height, 3.0F);
            DrawUtil.drawText(msdf, ph, x + leftPad, y + 4.0F, ColorProvider.rgba(255, 255, 255, 120.0F), 7.0F);
            this.infoDrag.setWidth(w);
            this.infoDrag.setHeight(height);
         } else {
            this.infoDrag.setWidth(0.0F);
            this.infoDrag.setHeight(0.0F);
         }
      } else {
         String coordsIcon = "F";
         String tpsIcon = "$";
         String bpsIcon = "@";
         String xPart = "x" + (int)this.mc.field_1724.method_23317();
         String yPart = "y" + (int)this.mc.field_1724.method_23318();
         String zPart = "z" + (int)this.mc.field_1724.method_23321();
         double dX = this.mc.field_1724.method_23317() - this.mc.field_1724.field_6014;
         double dZ = this.mc.field_1724.method_23321() - this.mc.field_1724.field_5969;
         String bpsValue = String.format(Locale.US, "%.1f Bps", Math.hypot(dX, dZ) * 20.0);
         String tpsValue = String.format(Locale.US, "%.1f Tps", AlphaDLC.getInstance().getTpsGetter().getTPS());
         int groups = (showCoords ? 1 : 0) + (showTps ? 1 : 0) + (showBps ? 1 : 0);
         float boxWidth = leftPad + rightPad;
         if (showCoords) {
            boxWidth += icons.getWidth(coordsIcon, 7.0F)
               + iconGap
               + msdf.getWidth(xPart, 7.0F)
               + sep
               + msdf.getWidth(yPart, 7.0F)
               + sep
               + msdf.getWidth(zPart, 7.0F);
         }

         if (showTps) {
            boxWidth += icons.getWidth(tpsIcon, 7.0F) + iconGap + msdf.getWidth(tpsValue, 7.0F);
         }

         if (showBps) {
            boxWidth += icons.getWidth(bpsIcon, 7.0F) + iconGap + msdf.getWidth(bpsValue, 7.0F);
         }

         boxWidth += sep * (float)(groups - 1);
         this.drawElementBackground(this.infoPopup, x, y, boxWidth, height, 3.0F, 1.0F);
         this.drawElementShine(this.infoPopup, context, x, y, boxWidth, height, 3.0F);
         float dotY = y + (height - dotSize) / 2.0F;
         float cx = x + leftPad;
         boolean first = true;
         if (showCoords) {
            DrawUtil.drawText(icons, coordsIcon, cx, y + 4.75F, iconColor, 7.0F);
            cx += icons.getWidth(coordsIcon, 7.0F) + iconGap;
            DrawUtil.drawText(msdf, xPart, cx, y + 4.0F, whiteColor, 7.0F);
            cx += msdf.getWidth(xPart, 7.0F) + sepGap;
            DrawUtil.drawRound(cx, dotY, dotSize, dotSize, dotSize / 2.0F, dotColor);
            cx += dotSize + sepGap;
            DrawUtil.drawText(msdf, yPart, cx, y + 4.0F, whiteColor, 7.0F);
            cx += msdf.getWidth(yPart, 7.0F) + sepGap;
            DrawUtil.drawRound(cx, dotY, dotSize, dotSize, dotSize / 2.0F, dotColor);
            cx += dotSize + sepGap;
            DrawUtil.drawText(msdf, zPart, cx, y + 4.0F, whiteColor, 7.0F);
            cx += msdf.getWidth(zPart, 7.0F);
            first = false;
         }

         if (showTps) {
            if (!first) {
               cx += sepGap;
               DrawUtil.drawRound(cx, dotY, dotSize, dotSize, dotSize / 2.0F, dotColor);
               cx += dotSize + sepGap;
            }

            DrawUtil.drawText(icons, tpsIcon, cx, y + 4.75F, iconColor, 7.0F);
            cx += icons.getWidth(tpsIcon, 7.0F) + iconGap;
            DrawUtil.drawText(msdf, tpsValue, cx, y + 4.0F, whiteColor, 7.0F);
            cx += msdf.getWidth(tpsValue, 7.0F);
            first = false;
         }

         if (showBps) {
            if (!first) {
               cx += sepGap;
               DrawUtil.drawRound(cx, dotY, dotSize, dotSize, dotSize / 2.0F, dotColor);
               cx += dotSize + sepGap;
            }

            DrawUtil.drawText(icons, bpsIcon, cx, y + 4.75F, iconColor, 7.0F);
            cx += icons.getWidth(bpsIcon, 7.0F) + iconGap;
            DrawUtil.drawText(msdf, bpsValue, cx, y + 4.0F, whiteColor, 7.0F);
            cx += msdf.getWidth(bpsValue, 7.0F);
         }

         this.infoDrag.setWidth(boxWidth);
         this.infoDrag.setHeight(height);
      }
   }

   private int colorLerp(int start, int end, float speed, float offset) {
      long t = System.currentTimeMillis();
      double ph = (double)t * ((double)speed / 1000.0) + (double)offset;
      float p = (float)(Math.sin(ph) * 0.5 + 0.5);
      int sr = start >> 16 & 0xFF;
      int sg = start >> 8 & 0xFF;
      int sb = start & 0xFF;
      int er = end >> 16 & 0xFF;
      int eg = end >> 8 & 0xFF;
      int eb = end & 0xFF;
      int r = (int)((float)sr * (1.0F - p) + (float)er * p);
      int g = (int)((float)sg * (1.0F - p) + (float)eg * p);
      int b = (int)((float)sb * (1.0F - p) + (float)eb * p);
      return 0xFF000000 | r << 16 | g << 8 | b;
   }

   private void renderCoordsInfo(DrawContext context) {
   }

   private void renderTargetHUD(DrawContext context) {
      boolean chatOpen = this.mc.field_1755 instanceof ChatScreen;
      if (!chatOpen) {
         this.targetHudPopup.open = false;
         this.targetHudPopup.draggingSlider = null;
      }

      this.beginScale(this.targetHudPopup, context);
      this.renderTargetHUDContent(context);
      this.endScale(this.targetHudPopup, context);
      this.runPopup(this.targetHudPopup, context);
   }

   private void renderTargetHUDContent(DrawContext context) {
      KillAura killAura = Instance.get(KillAura.class);
      boolean chatOpen = this.mc.field_1755 instanceof ChatScreen;
      LivingEntity target = null;
      if (killAura.isEnabled() && killAura.getTarget() != null && killAura.getTarget().method_5805()) {
         target = killAura.getTarget();
      } else if (this.thShowOnHover.getValue() && this.mc.field_1692 instanceof LivingEntity living && living.method_5805()) {
         target = living;
      } else if (chatOpen) {
         target = this.mc.field_1724;
      }

      if (target != null) {
         this.lastTarget = target;
         this.animation.run(1.0F);
         this.armorAnim.run(1.0F);
      } else {
         this.animation.run(0.0F);
         this.armorAnim.run(0.0F);
      }

      if (!(this.animation.getValue() <= 0.05F) && this.lastTarget != null && this.lastTarget instanceof LivingEntity livingEntity) {
         AbstractClientPlayerEntity playerEntity = this.lastTarget instanceof AbstractClientPlayerEntity ? (AbstractClientPlayerEntity)this.lastTarget : null;
         float anim = this.animation.getValue();
         int alphaInt = (int)(255.0F * anim);
         float width = 108.0F;
         float height = 34.0F;
         float x = this.targetHUDDrag.getX();
         float y = this.targetHUDDrag.getY();
         this.drawElementBackground(this.targetHudPopup, x, y, width, height, 4.0F, anim);
         this.drawElementShine(this.targetHudPopup, context, x, y, width, height, 4.0F);
         float headSize = 24.0F;
         float headX = x + 4.0F;
         float headY = y + (height - headSize) / 2.0F;
         float hurtPercent = (float)livingEntity.field_6235 / 10.0F;
         int headColor = ColorProvider.rgba(255, (int)(255.0F * (1.0F - hurtPercent)), (int)(255.0F * (1.0F - hurtPercent)), (float)alphaInt);

         try {
            Identifier faceTex = this.getEntityFaceTexture(livingEntity, playerEntity);
            if (faceTex != null) {
               AbstractTexture tex = this.mc.method_1531().method_4619(faceTex);
               tex.method_4527(false, false);
               int texId = tex.method_4624();
               if (texId > 0) {
                  BuiltTexture headTexture = Builder.texture()
                     .size(new SizeState(headSize, headSize))
                     .radius(new QuadRadiusState(6.0F))
                     .color(new QuadColorState(headColor))
                     .texture(0.125F, 0.125F, 0.125F, 0.125F, texId)
                     .smoothness(1.0F)
                     .build();
                  headTexture.render(context.method_51448().method_23760().method_23761(), headX, headY);
               }
            }
         } catch (Exception var62) {
         }

         float textX = headX + headSize + 5.0F;
         float rightEdge = x + width - 5.0F;
         NameProtect nameProtect = Instance.get(NameProtect.class);
         String name = nameProtect.isEnabled() ? nameProtect.getCustomName(livingEntity.method_5477().getString()) : livingEntity.method_5477().getString();
         String shownName = this.trimTextToWidth(name, 8.5F, rightEdge - textX);
         DrawUtil.drawText(Fonts.SFMEDIUM.get(), shownName, textX, y + 3.0F, ColorProvider.rgba(255, 255, 255, (float)alphaInt), 8.5F);
         float currentHp = Server.getHealth(livingEntity, false);
         if (Float.isNaN(currentHp) || currentHp < 0.0F) {
            currentHp = 0.0F;
         }

         float absorption = livingEntity.method_6067();
         if (Float.isNaN(absorption) || absorption < 0.0F) {
            absorption = 0.0F;
         }

         float barHeight = 4.0F;
         float barY = y + height - 8.0F;
         float barWidth = rightEdge - textX;
         float maxHealth = livingEntity.method_6063();
         float total = maxHealth + absorption;
         if (total <= 0.0F) {
            total = 1.0F;
         }

         float healthFrac = MathHelper.method_15363(currentHp / total, 0.0F, 1.0F);
         float absFrac = MathHelper.method_15363(absorption / total, 0.0F, 1.0F);
         this.hpAnimation.run(healthFrac);
         this.absorptionAnimation.run(absFrac);
         float healthW = barWidth * this.hpAnimation.getValue();
         float absW = barWidth * this.absorptionAnimation.getValue();
         int c1 = ColorProvider.getColorClient();
         int hpColor = ColorProvider.setAlpha(c1, alphaInt);
         int goldColor = ColorProvider.rgba(255, 215, 0, (float)alphaInt);
         int backColor = ColorProvider.rgba(20, 20, 20, (float)((int)(160.0F * anim)));
         DrawUtil.drawRound(textX, barY, barWidth, barHeight, barHeight / 2.0F, backColor);
         if (healthW > 0.5F) {
            DrawUtil.drawRound(textX, barY, healthW, barHeight, barHeight / 2.0F, hpColor);
         }

         if (absW > 0.5F) {
            DrawUtil.drawRound(textX + healthW, barY, absW, barHeight, barHeight / 2.0F, goldColor);
         }

         float fillW = Math.min(barWidth, healthW + absW);
         float arrowX = MathHelper.method_15363(textX + fillW, textX + 3.0F, textX + barWidth - 3.0F);
         float arrowTipY = barY - 1.0F;
         float arrowTopY = barY - 5.0F;
         boolean hasAbs = absorption > 0.05F;
         int arrowColor = hasAbs ? goldColor : ColorProvider.rgba(255, 255, 255, (float)alphaInt);
         this.drawArrowDropDown(arrowX, arrowTopY, arrowTipY, 6.0F, arrowColor);
         float shownHp = currentHp + absorption;
         String hpNum = String.format(Locale.US, "%.1f", shownHp);
         float hpNumW = Fonts.SFMEDIUM.get().getWidth(hpNum, 6.5F);
         float hpNumX = MathHelper.method_15363(arrowX - hpNumW / 2.0F, x + 3.0F, x + width - hpNumW - 3.0F);
         DrawUtil.drawText(Fonts.SFMEDIUM.get(), hpNum, hpNumX, arrowTopY - 8.0F, ColorProvider.rgba(255, 255, 255, (float)alphaInt), 6.5F);
         float armorAlpha = this.armorAnim.getValue();
         if (this.thShowItems.getValue() && armorAlpha > 0.05F) {
            List<ItemStack> armorList = new ArrayList<>();

            for (ItemStack stack : livingEntity.method_5661()) {
               armorList.add(stack);
            }

            Collections.reverse(armorList);
            List<ItemStack> handsList = new ArrayList<>();
            handsList.add(livingEntity.method_6047());
            handsList.add(livingEntity.method_6079());
            float cell = 13.0F;
            float boxPad = 2.0F;
            float boxH = cell + boxPad * 2.0F;
            float armorBoxW = 4.0F * cell + boxPad * 2.0F;
            float handsBoxW = 2.0F * cell + boxPad * 2.0F;
            float boxesY = y + height + 3.0F;
            float handsBoxX = x + width - handsBoxW;
            this.drawElementBackground(this.targetHudPopup, x, boxesY, armorBoxW, boxH, 3.0F, armorAlpha);
            this.drawElementBackground(this.targetHudPopup, handsBoxX, boxesY, handsBoxW, boxH, 3.0F, armorAlpha);
            float itemScale = 0.6875F;
            context.method_51448().method_22903();
            context.method_51448().method_46416(0.0F, 0.0F, 100.0F);
            TextRenderer textRenderer = MinecraftClient.method_1551().field_1772;
            this.drawItemSlots(context, textRenderer, armorList, x + boxPad, boxesY + boxPad, cell, itemScale);
            this.drawItemSlots(context, textRenderer, handsList, handsBoxX + boxPad, boxesY + boxPad, cell, itemScale);
            context.method_51448().method_22909();
         }

         this.targetHUDDrag.setWidth(width);
         this.targetHUDDrag.setHeight(height);
      }
   }

   private Identifier getEntityFaceTexture(LivingEntity entity, AbstractClientPlayerEntity player) {
      try {
         if (player != null) {
            return player.method_52814().comp_1626();
         }

         if (this.mc.method_1561().method_3953(entity) instanceof LivingEntityRenderer renderer) {
            float tickDelta = this.mc.method_61966().method_60637(false);
            LivingEntityRenderState state = (LivingEntityRenderState)renderer.method_62425(entity, tickDelta);
            if (state != null) {
               return renderer.method_3885(state);
            }
         }
      } catch (Exception var7) {
      }

      return null;
   }

   public void drawEntity(float x, float y, float scale, float yawAngle, float pitchAngle, LivingEntity entity) {
      MatrixStack matrices = new MatrixStack();
      matrices.method_22903();
      matrices.method_22904((double)x, (double)y, 50.0);
      matrices.method_22905(-scale, scale, scale);
      matrices.method_22907(RotationAxis.field_40718.rotationDegrees(180.0F));
      matrices.method_22907(RotationAxis.field_40716.rotationDegrees(yawAngle));
      matrices.method_22907(RotationAxis.field_40714.rotationDegrees(pitchAngle));
      float bodyYaw = entity.field_6283;
      float prevBodyYaw = entity.field_6220;
      float headYaw = entity.field_6241;
      float prevHeadYaw = entity.field_6259;
      float yaw = entity.method_36454();
      float prevYaw = entity.field_5982;
      float pitch = entity.method_36455();
      float prevPitch = entity.field_6004;
      entity.field_6283 = 0.0F;
      entity.field_6220 = 0.0F;
      entity.field_6241 = 0.0F;
      entity.field_6259 = 0.0F;
      entity.method_36456(0.0F);
      entity.field_5982 = 0.0F;
      entity.method_36457(0.0F);
      entity.field_6004 = 0.0F;
      DiffuseLighting.method_24210();
      Immediate immediate = this.mc.method_22940().method_23000();
      float tickDelta = this.mc.method_61966().method_60637(true);
      this.mc.method_1561().method_62424(entity, 0.0, 0.0, 0.0, tickDelta, matrices, immediate, 15728880);
      immediate.method_22993();
      DiffuseLighting.method_24211();
      entity.field_6283 = bodyYaw;
      entity.field_6220 = prevBodyYaw;
      entity.field_6241 = headYaw;
      entity.field_6259 = prevHeadYaw;
      entity.method_36456(yaw);
      entity.field_5982 = prevYaw;
      entity.method_36457(pitch);
      entity.field_6004 = prevPitch;
      matrices.method_22909();
   }

   private Color lerpColor(Color a, Color b, float t) {
      return new Color(
         (int)((float)a.getRed() + t * (float)(b.getRed() - a.getRed())),
         (int)((float)a.getGreen() + t * (float)(b.getGreen() - a.getGreen())),
         (int)((float)a.getBlue() + t * (float)(b.getBlue() - a.getBlue()))
      );
   }

   private void updatePotions() {
      Map<String, StatusEffectInstance> currentEffects = this.mc
         .field_1724
         .method_6026()
         .stream()
         .collect(Collectors.toMap(e -> Text.method_43471(e.method_5586()).getString() + ":" + e.method_5578(), e -> (StatusEffectInstance)e, (e1, e2) -> e1));
      this.potionItems.forEach(item -> {
         String key = item.name + ":" + item.amplifier;
         StatusEffectInstance effect = currentEffects.get(key);
         if (effect != null) {
            item.durationTicks = effect.method_5584();
            if (item.durationTicks > item.maxDurationTicks) {
               item.maxDurationTicks = item.durationTicks;
            }

            if (!item.active) {
               item.animation.setValue(1.0F);
            }

            item.active = true;
            currentEffects.remove(key);
         } else {
            item.active = false;
         }
      });
      boolean added = !currentEffects.isEmpty();
      currentEffects.forEach(
         (key, effect) -> this.potionItems
               .add(
                  new Interface.PotionItem(
                     Text.method_43471(effect.method_5586()).getString(), effect.method_5578(), effect.method_5584(), effect.method_5579()
                  )
               )
      );
      boolean removed = this.potionItems.removeIf(item -> !item.active && item.animation.getValue() == 0.0F);
      if (added || removed) {
         this.potionItems.sort(Comparator.comparing(pi -> pi.name));
      }
   }

   private void updateCooldowns() {
      if (this.mc.field_1724 != null) {
         for (Interface.CooldownItem ci : this.cooldownItems) {
            boolean cooling = this.mc.field_1724.method_7357().method_7904(ci.item.method_7854());
            if (!cooling || System.currentTimeMillis() >= ci.endTimeMs) {
               ci.active = false;
            }
         }

         this.cooldownItems.removeIf(cix -> !cix.active && cix.animation.getValue() == 0.0F);
         if (this.cooldownItems.isEmpty() && this.mc.field_1755 instanceof ChatScreen) {
            long now = System.currentTimeMillis();
            if (now - this.cooldownExampleChange >= 1500L) {
               this.cooldownExampleIndex = (this.cooldownExampleIndex + 1) % COOLDOWN_EXAMPLE_ITEMS.length;
               this.cooldownExampleChange = now;
            }
         }
      }
   }

   private void renderCoolDowns(DrawContext context) {
      if (this.mc.field_1724 != null) {
         boolean chatOpen = this.mc.field_1755 instanceof ChatScreen;
         if (!chatOpen) {
            this.cooldownsPopup.open = false;
            this.cooldownsPopup.draggingSlider = null;
         }

         this.beginScale(this.cooldownsPopup, context);
         this.renderCoolDownsNew(context);
         this.endScale(this.cooldownsPopup, context);
         this.runPopup(this.cooldownsPopup, context);
      }
   }

   private void drawCooldownIcon(DrawContext context, ItemStack stack, float x, float y, float size, int alpha) {
      float scale = size / 16.0F;
      context.method_51448().method_22903();
      context.method_51448().method_46416(x, y, 0.0F);
      context.method_51448().method_22905(scale, scale, 1.0F);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, (float)alpha / 255.0F);
      context.method_51427(stack, 0, 0);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      context.method_51448().method_22909();
   }

   private void renderCoolDownsNew(DrawContext context) {
      if (this.mc.field_1724 != null) {
         float posX = this.cooldownsDrag.getX();
         float posY = this.cooldownsDrag.getY();
         float headerHeight = 14.0F;
         float itemHeight = 12.0F;
         float minWidth = 56.0F;
         float padX = 5.0F;
         float padY = 2.0F;
         boolean isFound = false;

         for (Interface.CooldownItem item : this.cooldownItems) {
            item.animation.run(item.active ? 1.0F : 0.0F);
            item.rowAnim.run(item.active ? 1.0F : 0.0F);
            if (item.animation.getValue() > 0.001F) {
               isFound = true;
            }
         }

         boolean showExample = this.mc.field_1755 instanceof ChatScreen && !isFound;
         if (!isFound && !(this.mc.field_1755 instanceof ChatScreen)) {
            this.cdAlpha.run(0.0F);
         } else {
            this.cdAlpha.run(1.0F);
         }

         float globalAlpha = this.cdAlpha.getValue();
         if (!(globalAlpha <= 0.05F)) {
            int headerAlpha = (int)Math.min(255.0F, Math.max(0.0F, 255.0F * globalAlpha));
            float maxNameBoxW = minWidth;
            float maxTimeBoxW = 0.0F;
            float contentHeight = 0.0F;

            for (Interface.CooldownItem itemx : this.cooldownItems) {
               float animVal = itemx.animation.getValue();
               if (animVal > 0.001F) {
                  contentHeight += itemHeight * itemx.rowAnim.getValue();
                  String name = itemx.item.method_63680().getString();
                  float nameW = Fonts.SFMEDIUM.get().getWidth(name, 7.0F);
                  float nw = nameW + 8.0F + 12.0F + 10.0F;
                  if (nw > maxNameBoxW) {
                     maxNameBoxW = nw;
                  }

                  int sec = itemx.remainingSeconds();
                  String timeStr = String.format("%d:%02d", sec / 60, sec % 60);
                  float tw = Fonts.SFMEDIUM.get().getWidth(timeStr, 6.75F) + 10.0F + (this.cooldownsRing.getValue() ? 11.0F : 0.0F);
                  if (tw > maxTimeBoxW) {
                     maxTimeBoxW = tw;
                  }
               }
            }

            if (showExample) {
               float exNameW = Fonts.SFMEDIUM.get().getWidth("Example", 7.0F);
               if (exNameW + 8.0F + 12.0F + 10.0F > maxNameBoxW) {
                  maxNameBoxW = exNameW + 8.0F + 12.0F + 10.0F;
               }

               float exTimeW = Fonts.SFMEDIUM.get().getWidth("**:**", 6.75F) + 10.0F + (this.cooldownsRing.getValue() ? 11.0F : 0.0F);
               if (exTimeW > maxTimeBoxW) {
                  maxTimeBoxW = exTimeW;
               }

               contentHeight = itemHeight;
            }

            float rawWidth = maxNameBoxW + maxTimeBoxW + padX * 2.0F;
            this.cooldownsPopup.panelWidth.run(rawWidth);
            float totalRowWidth = this.cooldownsPopup.panelWidth.getValue();
            if (totalRowWidth < 20.0F) {
               totalRowWidth = rawWidth;
            }

            float totalHeight = headerHeight + contentHeight + padY * 2.0F;
            this.drawElementBackground(this.cooldownsPopup, posX, posY, totalRowWidth, totalHeight, 3.0F, globalAlpha);
            this.drawElementShine(this.cooldownsPopup, context, posX, posY, totalRowWidth, totalHeight, 3.0F);
            DrawUtil.drawText(
               Fonts.SFMEDIUM.get(), "Cooldowns", posX + padX + 4.0F, posY + padY + 2.5F, ColorProvider.rgba(255, 255, 255, (float)headerAlpha), 8.0F
            );
            DrawUtil.drawText(
               Fonts.ALPHADLC.get(),
               "h",
               posX + totalRowWidth - padX - 11.0F,
               posY + padY + 3.0F,
               ColorProvider.setAlpha(ColorProvider.getColorIcons(), headerAlpha),
               9.0F
            );
            float curY = posY + headerHeight + padY;

            for (Interface.CooldownItem itemxx : this.cooldownItems) {
               float animVal = itemxx.animation.getValue();
               if (!(animVal <= 0.001F)) {
                  float rowAnimVal = itemxx.rowAnim.getValue();
                  if (!(rowAnimVal <= 0.001F)) {
                     int sec = itemxx.remainingSeconds();
                     String timeStr = String.format("%d:%02d", sec / 60, sec % 60);
                     String nameStr = itemxx.item.method_63680().getString();
                     int itemAlpha = (int)Math.min(255.0F, Math.max(0.0F, 255.0F * animVal * globalAlpha));
                     if (itemAlpha >= 5) {
                        float timeW = Fonts.SFMEDIUM.get().getWidth(timeStr, 6.75F);
                        float timeBoxW = timeW + 10.0F;
                        float rowHeight = itemHeight * rowAnimVal;
                        context.method_51448().method_22903();
                        context.method_51448().method_46416(posX + totalRowWidth / 2.0F, curY + rowHeight / 2.0F, 0.0F);
                        context.method_51448().method_22905(rowAnimVal, rowAnimVal, rowAnimVal);
                        context.method_51448().method_46416(-(posX + totalRowWidth / 2.0F), -(curY + rowHeight / 2.0F), 0.0F);
                        float cdMid = textMidY(curY + 2.75F, 7.0F);
                        this.drawCooldownIcon(context, itemxx.item.method_7854(), posX + padX + 1.0F, cdMid - 5.0F, 10.0F, itemAlpha);
                        float dotX = posX + padX + 13.0F;
                        float dotY = cdMid - 1.5F;
                        DrawUtil.drawRound(dotX, dotY, 3.0F, 3.0F, 1.5F, ColorProvider.rgba(120, 120, 120, (float)itemAlpha));
                        float textX = posX + padX + 20.0F;
                        DrawUtil.drawText(Fonts.SFMEDIUM.get(), nameStr, textX, curY + 2.75F, ColorProvider.rgba(255, 255, 255, (float)itemAlpha), 7.0F);
                        float timerX = posX + totalRowWidth - padX - timeBoxW;
                        if (this.cooldownsRing.getValue()) {
                           float ringRadius = 4.0F;
                           float ringCX = timerX - 3.0F - ringRadius;
                           float progress = 1.0F;
                           if (itemxx.maxDurationMs > 0L) {
                              long rem = itemxx.endTimeMs - System.currentTimeMillis();
                              progress = MathHelper.method_15363((float)rem / (float)itemxx.maxDurationMs, 0.0F, 1.0F);
                           }

                           DrawUtil.drawRingArc(ringCX, cdMid, ringRadius, 1.4F, -90.0F, 270.0F, ColorProvider.rgba(80, 80, 80, (float)itemAlpha));
                           if (progress > 0.0F) {
                              DrawUtil.drawRingArc(
                                 ringCX,
                                 cdMid,
                                 ringRadius,
                                 1.4F,
                                 -90.0F,
                                 -90.0F + 360.0F * progress,
                                 ColorProvider.setAlpha(ColorProvider.getColorClient(), itemAlpha)
                              );
                           }
                        }

                        String timerKey = "cooldowns_duration_" + itemxx.item.method_7876();
                        TimerTextAnimator.draw(
                           Fonts.SFMEDIUM.get(),
                           timerKey,
                           timeStr,
                           timerX + (timeBoxW - timeW) / 2.0F + 1.0F,
                           curY + 3.5F,
                           ColorProvider.rgba(255, 255, 255, (float)itemAlpha),
                           6.75F
                        );
                        context.method_51448().method_22909();
                        curY += rowHeight;
                     }
                  }
               }
            }

            if (showExample) {
               String nameStr = "Example";
               String timeStr = "**:**";
               ItemStack stack = COOLDOWN_EXAMPLE_ITEMS[this.cooldownExampleIndex].method_7854();
               float timeW = Fonts.SFMEDIUM.get().getWidth(timeStr, 6.75F);
               float timeBoxW = timeW + 10.0F;
               float cdExMid = textMidY(curY + 3.5F, 7.0F);
               this.drawCooldownIcon(context, stack, posX + padX + 1.0F, cdExMid - 5.0F, 10.0F, headerAlpha);
               float dotX = posX + padX + 13.0F;
               float dotY = cdExMid - 1.5F;
               DrawUtil.drawRound(dotX, dotY, 3.0F, 3.0F, 1.5F, ColorProvider.rgba(120, 120, 120, (float)headerAlpha));
               float textX = posX + padX + 20.0F;
               DrawUtil.drawText(Fonts.SFMEDIUM.get(), nameStr, textX, curY + 3.5F, ColorProvider.rgba(255, 255, 255, (float)headerAlpha), 7.0F);
               float timerX = posX + totalRowWidth - padX - timeBoxW;
               if (this.cooldownsRing.getValue()) {
                  float ringRadiusx = 4.0F;
                  float ringCXx = timerX - 3.0F - ringRadiusx;
                  DrawUtil.drawRingArc(ringCXx, cdExMid, ringRadiusx, 1.4F, -90.0F, 270.0F, ColorProvider.rgba(80, 80, 80, (float)headerAlpha));
                  DrawUtil.drawRingArc(ringCXx, cdExMid, ringRadiusx, 1.4F, -90.0F, 180.0F, ColorProvider.setAlpha(ColorProvider.getColorClient(), headerAlpha));
               }

               DrawUtil.drawText(
                  Fonts.SFMEDIUM.get(),
                  timeStr,
                  timerX + (timeBoxW - timeW) / 2.0F + 1.0F,
                  curY + 3.5F,
                  ColorProvider.rgba(255, 255, 255, (float)headerAlpha),
                  6.75F
               );
            }

            this.cooldownsDrag.setWidth(totalRowWidth);
            this.cooldownsDrag.setHeight(totalHeight);
         }
      }
   }

   private static float textMidY(float drawY, float size) {
      return drawY + size * 0.59F;
   }

   private void drawArrowDropDown(float cx, float topY, float tipY, float w, int color) {
      float h = tipY - topY;
      if (h <= 0.0F) {
         h = 4.0F;
      }

      int steps = Math.max(4, (int)(h * 2.0F));

      for (int i = 0; i <= steps; i++) {
         float t = (float)i / (float)steps;
         float rowW = w * (1.0F - t);
         float ry = topY + h * t;
         DrawUtil.drawRound(cx - rowW / 2.0F, ry, rowW, h / (float)steps + 0.6F, 0.0F, color);
      }
   }

   private void drawItemSlots(DrawContext context, TextRenderer textRenderer, List<ItemStack> items, float startX, float startY, float cell, float itemScale) {
      float drawn = 16.0F * itemScale;
      float inset = (cell - drawn) / 2.0F;

      for (int i = 0; i < items.size(); i++) {
         ItemStack stack = items.get(i);
         if (stack != null && !stack.method_7960()) {
            float cx = startX + (float)i * cell + inset;
            float cy = startY + inset;
            context.method_51448().method_22903();
            context.method_51448().method_46416(cx, cy, 0.0F);
            context.method_51448().method_22905(itemScale, itemScale, 1.0F);
            context.method_51427(stack, 0, 0);
            context.method_51431(textRenderer, stack, 0, 0);
            context.method_51448().method_22909();
         }
      }
   }

   public float getNotificationsX() {
      return this.notificationsDrag.getX();
   }

   public float getNotificationsY() {
      return this.notificationsDrag.getY();
   }

   private float notifExampleWidth() {
      float toggleW = 15.0F;
      float pad = 6.0F;
      float gap = 6.0F;
      float textW = Fonts.SFMEDIUM
         .get()
         .getWidth("\u041f\u0440\u0438\u043c\u0435\u0440 \u0443\u0432\u0435\u0434\u043e\u043c\u043b\u0435\u043d\u0438\u044f", 7.0F);
      return pad + textW + gap + toggleW + pad;
   }

   public float getNotificationsCenterX() {
      return this.notificationsDrag.getX() + this.notifExampleWidth() / 2.0F;
   }

   public float getNotificationsScale() {
      return this.notificationsPopup.size.getFloatValue();
   }

   public void drawNotifBackground(DrawContext context, float x, float y, float w, float h, float radius, float alphaFactor) {
      this.drawElementBackground(this.notificationsPopup, x, y, w, h, radius, alphaFactor);
      this.drawElementShine(this.notificationsPopup, context, x, y, w, h, radius);
   }

   public void drawNewToggle(float x, float y, float toggleW, float toggleH, boolean on, float anim, int alpha) {
      int inactive = ColorProvider.setAlpha(ColorProvider.getColorInactiveIndicator(), alpha);
      int active = ColorProvider.setAlpha(ColorProvider.getColorIndicator(), alpha);
      int bg = ColorProvider.interpolateColor(inactive, active, anim);
      DrawUtil.drawRound(x, y, toggleW, toggleH, toggleH / 2.0F, bg);
      float knob = toggleH - 1.0F;
      float knobMinX = x + 0.5F;
      float knobMaxX = x + toggleW - knob - 0.5F;
      float knobX = knobMinX + (knobMaxX - knobMinX) * anim;
      DrawUtil.drawCircle(knobX + knob / 2.0F, y + 0.5F + knob / 2.0F, knob / 2.0F, ColorProvider.setAlpha(ColorProvider.getColorSliderCircle(), alpha));
   }

   private void renderNotificationsExample(DrawContext context) {
      if (this.mc.field_1724 != null) {
         boolean chatOpen = this.mc.field_1755 instanceof ChatScreen;
         if (!chatOpen) {
            this.notificationsPopup.open = false;
            this.notificationsPopup.draggingSlider = null;
         } else {
            this.beginScale(this.notificationsPopup, context);
            float posX = this.notificationsDrag.getX();
            float posY = this.notificationsDrag.getY();
            String text = "\u041f\u0440\u0438\u043c\u0435\u0440 \u0443\u0432\u0435\u0434\u043e\u043c\u043b\u0435\u043d\u0438\u044f";
            float toggleW = 15.0F;
            float toggleH = 8.0F;
            float pad = 6.0F;
            float gap = 6.0F;
            float textW = Fonts.SFMEDIUM.get().getWidth(text, 7.0F);
            float width = pad + textW + gap + toggleW + pad;
            float height = 15.0F;
            this.drawElementBackground(this.notificationsPopup, posX, posY, width, height, 3.0F, 1.0F);
            this.drawElementShine(this.notificationsPopup, context, posX, posY, width, height, 3.0F);
            boolean exampleOn = System.currentTimeMillis() / 3000L % 2L == 0L;
            this.exampleNotifToggleAnim.run(exampleOn ? 1.0F : 0.0F);
            float textX = posX + pad;
            DrawUtil.drawText(Fonts.SFMEDIUM.get(), text, textX, posY + height / 2.0F - 4.1299996F, ColorProvider.rgba(255, 255, 255, 255.0F), 7.0F);
            float tX = textX + textW + gap;
            float tY = posY + (height - toggleH) / 2.0F;
            this.drawNewToggle(tX, tY, toggleW, toggleH, exampleOn, this.exampleNotifToggleAnim.getValue(), 255);
            this.notificationsDrag.setWidth(width);
            this.notificationsDrag.setHeight(height);
            this.endScale(this.notificationsPopup, context);
            this.runPopup(this.notificationsPopup, context);
         }
      }
   }

   private void renderServerHelper(DrawContext context) {
      if (this.mc.field_1724 != null) {
         boolean chatOpen = this.mc.field_1755 instanceof ChatScreen;
         if (!chatOpen) {
            this.serverHelperPopup.open = false;
            this.serverHelperPopup.draggingSlider = null;
         }

         this.beginScale(this.serverHelperPopup, context);
         this.renderServerHelperNew(context);
         this.endScale(this.serverHelperPopup, context);
         this.runPopup(this.serverHelperPopup, context);
      }
   }

   private void renderServerHelperNew(DrawContext context) {
      if (this.mc.field_1724 != null) {
         ServerHelper serverHelper = AlphaDLC.getInstance().getModuleStorage().get(ServerHelper.class);
         if (serverHelper != null) {
            boolean chatOpen = this.mc.field_1755 instanceof ChatScreen;
            boolean showExample = chatOpen && !serverHelper.isEnabled();
            float posX = this.serverHelperDrag.getX();
            float posY = this.serverHelperDrag.getY();
            float itemSize = 32.0F;
            float gap = 4.0F;
            float padding = 2.0F;
            List<Interface.ServerHelperItem> items = new ArrayList<>();
            if (showExample) {
               items.add(new Interface.ServerHelperItem(Items.field_8450, "G", "\u0410\u043d\u0442\u0438\u041f\u043e\u043b\u0435\u0442"));
               items.add(new Interface.ServerHelperItem(Items.field_8436, "H", "\u0413\u0440\u0438\u043d\u0447"));
               items.add(new Interface.ServerHelperItem(Items.field_8575, "J", "Shift"));
               items.add(new Interface.ServerHelperItem(Items.field_8207, "K", "\u0422\u0440\u0430\u043f\u043a\u0430"));
            } else if (serverHelper.isEnabled()) {
               int antiFlyKey = serverHelper.getAntiFlyKey();
               if (antiFlyKey != -1 && this.hasItemInInventory(Items.field_8450)) {
                  items.add(
                     new Interface.ServerHelperItem(
                        Items.field_8450, this.getKeyNameEnglish(antiFlyKey), "\u0410\u043d\u0442\u0438\u041f\u043e\u043b\u0435\u0442"
                     )
                  );
               }

               int grinchKey = serverHelper.getGrinchPotionKey();
               if (grinchKey != -1 && this.hasItemInInventory(Items.field_8436)) {
                  items.add(new Interface.ServerHelperItem(Items.field_8436, this.getKeyNameEnglish(grinchKey), "\u0413\u0440\u0438\u043d\u0447"));
               }

               int autoShiftKey = serverHelper.getAutoShiftKey();
               if (autoShiftKey != -1 && this.hasItemInInventory(Items.field_8575)) {
                  items.add(new Interface.ServerHelperItem(Items.field_8575, this.getKeyNameEnglish(autoShiftKey), "Shift"));
               }

               int horrorKey = serverHelper.getNewYearHorrorKey();
               if (horrorKey != -1 && this.hasItemInInventory(Items.field_8436)) {
                  items.add(new Interface.ServerHelperItem(Items.field_8436, this.getKeyNameEnglish(horrorKey), "\u0423\u0436\u0430\u0441"));
               }

               int essenceKey = serverHelper.getDarkEssenceKey();
               if (essenceKey != -1 && this.hasItemInInventory(Items.field_8436)) {
                  items.add(
                     new Interface.ServerHelperItem(Items.field_8436, this.getKeyNameEnglish(essenceKey), "\u042d\u0441\u0441\u0435\u043d\u0446\u0438\u044f")
                  );
               }

               int snowballKey = serverHelper.getSnowballKey();
               if (snowballKey != -1 && this.hasItemInInventory(Items.field_8436)) {
                  items.add(new Interface.ServerHelperItem(Items.field_8436, this.getKeyNameEnglish(snowballKey), "\u0421\u043d\u0435\u0436\u043e\u043a"));
               }

               int trapKey = serverHelper.getTrapKey();
               if (trapKey != -1 && this.hasItemInInventory(Items.field_8207)) {
                  items.add(new Interface.ServerHelperItem(Items.field_8207, this.getKeyNameEnglish(trapKey), "\u0422\u0440\u0430\u043f\u043a\u0430"));
               }
            }

            if (!items.isEmpty()) {
               int itemsPerRow = items.size();
               float boxSize = itemSize + padding * 2.0F;
               float totalWidth = boxSize * (float)itemsPerRow + gap * (float)(itemsPerRow - 1);
               this.serverHelperPopup.px = posX;
               this.serverHelperPopup.py = posY;
               this.serverHelperPopup.pw = totalWidth;
               this.serverHelperPopup.ph = boxSize;
               int alpha = this.serverHelperPopup.alpha.getIntValue();
               float radius = 4.0F;
               float curX = posX;

               for (Interface.ServerHelperItem item : items) {
                  if (this.serverHelperPopup.blur.getValue()) {
                     DrawUtil.drawRoundBlur(curX, posY, boxSize, boxSize, radius, ColorProvider.rgba(200, 200, 200, 255.0F), 12.0F);
                     DrawUtil.drawRound(curX, posY, boxSize, boxSize, radius, ColorProvider.setAlpha(ColorProvider.getColorInterfaceBg(), alpha));
                  } else {
                     DrawUtil.drawRound(curX, posY, boxSize, boxSize, radius, ColorProvider.setAlpha(ColorProvider.getColorInterfaceBg(), alpha));
                  }

                  if (this.serverHelperPopup.corners.getValue()) {
                     float th = 1.6F;
                     float rc = Math.max(radius, 2.0F);
                     float outer = rc + th / 2.0F;
                     int cornerColor = ColorProvider.setAlpha(ColorProvider.getColorClient(), 255);
                     DrawUtil.drawRingArc(curX + rc, posY + rc, outer, th, 180.0F, 270.0F, cornerColor);
                     DrawUtil.drawRingArc(curX + boxSize - rc, posY + rc, outer, th, 270.0F, 360.0F, cornerColor);
                     DrawUtil.drawRingArc(curX + boxSize - rc, posY + boxSize - rc, outer, th, 0.0F, 90.0F, cornerColor);
                     DrawUtil.drawRingArc(curX + rc, posY + boxSize - rc, outer, th, 90.0F, 180.0F, cornerColor);
                  }

                  if (this.serverHelperPopup.shine.getValue()) {
                     HudShine.render(
                        context.method_51448(),
                        curX,
                        posY,
                        boxSize,
                        boxSize,
                        radius,
                        this.serverHelperPopup.shineThickness.getFloatValue(),
                        1.0F,
                        this.serverHelperPopup.shineAlpha.getFloatValue() / 100.0F,
                        0.0F
                     );
                  }

                  ItemStack stack = item.item.method_7854();
                  float iconSize = 18.0F;
                  float iconX = curX + (boxSize - iconSize) / 2.0F;
                  float iconY = posY + padding + 2.0F;
                  context.method_51448().method_22903();
                  context.method_51448().method_46416(iconX, iconY, 0.0F);
                  float iconScale = iconSize / 16.0F;
                  context.method_51448().method_22905(iconScale, iconScale, 1.0F);
                  RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                  context.method_51427(stack, 0, 0);
                  context.method_51448().method_22909();
                  float keyW = Fonts.SFMEDIUM.get().getWidth(item.keyBind, 8.5F);
                  float keyX = curX + (boxSize - keyW) / 2.0F;
                  float keyY = posY + boxSize - padding - 10.0F;
                  DrawUtil.drawText(Fonts.SFMEDIUM.get(), item.keyBind, keyX, keyY, ColorProvider.rgba(200, 200, 200, 255.0F), 8.5F);
                  curX += boxSize + gap;
               }

               this.serverHelperDrag.setWidth(totalWidth);
               this.serverHelperDrag.setHeight(boxSize);
            }
         }
      }
   }

   private String getKeyNameEnglish(int keyCode) {
      if (keyCode == -1) {
         return "None";
      } else if (keyCode == -100) {
         return "LMB";
      } else if (keyCode == -99) {
         return "RMB";
      } else if (keyCode == -98) {
         return "MMB";
      } else {
         String keyName = GLFW.glfwGetKeyName(keyCode, 0);
         if (keyName != null) {
            return this.convertRussianToEnglish(keyName.toUpperCase());
         } else {
            String key = KeyStorage.getKey(keyCode);
            return this.convertRussianToEnglish(key.toUpperCase());
         }
      }
   }

   private String convertRussianToEnglish(String text) {
      if (text != null && !text.isEmpty()) {
         Map<Character, Character> russianToEnglish = new HashMap<>();
         russianToEnglish.put('\u0419', 'Q');
         russianToEnglish.put('\u0439', 'q');
         russianToEnglish.put('\u0426', 'W');
         russianToEnglish.put('\u0446', 'w');
         russianToEnglish.put('\u0423', 'E');
         russianToEnglish.put('\u0443', 'e');
         russianToEnglish.put('\u041a', 'R');
         russianToEnglish.put('\u043a', 'r');
         russianToEnglish.put('\u0415', 'T');
         russianToEnglish.put('\u0435', 't');
         russianToEnglish.put('\u041d', 'Y');
         russianToEnglish.put('\u043d', 'y');
         russianToEnglish.put('\u0413', 'U');
         russianToEnglish.put('\u0433', 'u');
         russianToEnglish.put('\u0428', 'I');
         russianToEnglish.put('\u0448', 'i');
         russianToEnglish.put('\u0429', 'O');
         russianToEnglish.put('\u0449', 'o');
         russianToEnglish.put('\u0417', 'P');
         russianToEnglish.put('\u0437', 'p');
         russianToEnglish.put('\u0425', '[');
         russianToEnglish.put('\u0445', '[');
         russianToEnglish.put('\u042a', ']');
         russianToEnglish.put('\u044a', ']');
         russianToEnglish.put('\u0424', 'A');
         russianToEnglish.put('\u0444', 'a');
         russianToEnglish.put('\u042b', 'S');
         russianToEnglish.put('\u044b', 's');
         russianToEnglish.put('\u0412', 'D');
         russianToEnglish.put('\u0432', 'd');
         russianToEnglish.put('\u0410', 'F');
         russianToEnglish.put('\u0430', 'f');
         russianToEnglish.put('\u041f', 'G');
         russianToEnglish.put('\u043f', 'g');
         russianToEnglish.put('\u0420', 'H');
         russianToEnglish.put('\u0440', 'h');
         russianToEnglish.put('\u041e', 'J');
         russianToEnglish.put('\u043e', 'j');
         russianToEnglish.put('\u041b', 'K');
         russianToEnglish.put('\u043b', 'k');
         russianToEnglish.put('\u0414', 'L');
         russianToEnglish.put('\u0434', 'l');
         russianToEnglish.put('\u0416', ';');
         russianToEnglish.put('\u0436', ';');
         russianToEnglish.put('\u042d', '\'');
         russianToEnglish.put('\u044d', '\'');
         russianToEnglish.put('\u042f', 'Z');
         russianToEnglish.put('\u044f', 'z');
         russianToEnglish.put('\u0427', 'X');
         russianToEnglish.put('\u0447', 'x');
         russianToEnglish.put('\u0421', 'C');
         russianToEnglish.put('\u0441', 'c');
         russianToEnglish.put('\u041c', 'V');
         russianToEnglish.put('\u043c', 'v');
         russianToEnglish.put('\u0418', 'B');
         russianToEnglish.put('\u0438', 'b');
         russianToEnglish.put('\u0422', 'N');
         russianToEnglish.put('\u0442', 'n');
         russianToEnglish.put('\u042c', 'M');
         russianToEnglish.put('\u044c', 'm');
         russianToEnglish.put('\u0411', ',');
         russianToEnglish.put('\u0431', ',');
         russianToEnglish.put('\u042e', '.');
         russianToEnglish.put('\u044e', '.');
         StringBuilder result = new StringBuilder();

         for (char c : text.toCharArray()) {
            result.append(russianToEnglish.getOrDefault(c, c));
         }

         return result.toString();
      } else {
         return text;
      }
   }

   private boolean hasItemInInventory(Item item) {
      if (this.mc.field_1724 == null) {
         return false;
      } else {
         for (ItemStack stack : this.mc.field_1724.method_31548().field_7547) {
            if (!stack.method_7960() && stack.method_7909() == item) {
               return true;
            }
         }

         for (ItemStack stackx : this.mc.field_1724.method_31548().field_7548) {
            if (!stackx.method_7960() && stackx.method_7909() == item) {
               return true;
            }
         }

         ItemStack offhand = this.mc.field_1724.method_6079();
         return !offhand.method_7960() && offhand.method_7909() == item;
      }
   }

   private static record BindEntry(String label, String bind, double animValue, ModuleCategory category) {
   }

   private static class CooldownItem {
      final Item item;
      long endTimeMs;
      long maxDurationMs;
      boolean active = true;
      final Animation animation = new Animation(Easing.EXPO_OUT, 233L);
      final Animation rowAnim = new Animation(Easing.DECELERATE, 150L);

      CooldownItem(Item item, long endTimeMs, long maxDurationMs) {
         this.item = item;
         this.endTimeMs = endTimeMs;
         this.maxDurationMs = maxDurationMs;
      }

      int remainingSeconds() {
         long rem = this.endTimeMs - System.currentTimeMillis();
         return (int)Math.max(0.0, Math.ceil((double)rem / 1000.0));
      }
   }

   private static class HeadParticle {
      float x;
      float y;
      float vx;
      float vy;
      float size;
      long spawnTime;
      int color;

      HeadParticle(float startX, float startY, int color) {
         this.x = startX;
         this.y = startY;
         double angle = Math.random() * Math.PI * 2.0;
         double speed = Math.random() * 0.4 + 0.1;
         this.vx = (float)(Math.cos(angle) * speed);
         this.vy = (float)(Math.sin(angle) * speed);
         this.size = (float)(Math.random() * 8.0 + 2.0);
         this.spawnTime = System.currentTimeMillis();
         this.color = color;
      }

      void update() {
         this.x = this.x + this.vx;
         this.y = this.y + this.vy;
      }

      float getAlpha() {
         long elapsed = System.currentTimeMillis() - this.spawnTime;
         return elapsed >= 2000L ? 0.0F : 1.0F - (float)elapsed / 2000.0F;
      }
   }

   private static final class HudPopup {
      final String title;
      final Draggable drag;
      final BooleanSetting blur;
      final BooleanSetting shine;
      final BooleanSetting corners;
      final SliderSetting size;
      final SliderSetting alpha;
      final SliderSetting shineAlpha;
      final SliderSetting shineThickness;
      final BooleanSetting ring;
      boolean open = false;
      boolean transformed = false;
      final Animation anim = new Animation(Easing.EXPO_OUT, 250L);
      final Animation panelWidth = new Animation(Easing.EXPO_OUT, 220L);
      final List<Interface.PopupRow> rendered = new ArrayList<>();
      final List<Interface.PopupRow> extraRows = new ArrayList<>();
      float px;
      float py;
      float pw;
      float ph;
      SliderSetting draggingSlider = null;
      float trackX;
      float trackW;

      HudPopup(
         String title,
         Draggable drag,
         BooleanSetting blur,
         SliderSetting size,
         SliderSetting alpha,
         BooleanSetting ring,
         BooleanSetting shine,
         SliderSetting shineAlpha,
         SliderSetting shineThickness,
         BooleanSetting corners
      ) {
         this.title = title;
         this.drag = drag;
         this.blur = blur;
         this.size = size;
         this.alpha = alpha;
         this.ring = ring;
         this.shine = shine;
         this.shineAlpha = shineAlpha;
         this.shineThickness = shineThickness;
         this.corners = corners;
      }
   }

   private static enum PopupKind {
      TOGGLE,
      SLIDER;
   }

   private static final class PopupRow {
      final Interface.PopupKind kind;
      final String label;
      final BooleanSetting bool;
      final SliderSetting slider;
      float x;
      float y;
      float w;
      float h;

      PopupRow(Interface.PopupKind kind, String label, BooleanSetting bool, SliderSetting slider) {
         this.kind = kind;
         this.label = label;
         this.bool = bool;
         this.slider = slider;
      }
   }

   private static class PotionItem {
      String name;
      int amplifier;
      int durationTicks;
      int maxDurationTicks;
      boolean active;
      RegistryEntry<StatusEffect> effect;
      Animation animation = new Animation(Easing.EXPO_OUT, 233L);
      Animation rowAnim = new Animation(Easing.DECELERATE, 150L);

      PotionItem(String name, int amplifier, int durationTicks, RegistryEntry<StatusEffect> effect) {
         this.name = name;
         this.amplifier = amplifier;
         this.durationTicks = durationTicks;
         this.maxDurationTicks = durationTicks;
         this.effect = effect;
         this.active = true;
      }
   }

   private static class ServerHelperItem {
      final Item item;
      final String keyBind;
      final String name;

      ServerHelperItem(Item item, String keyBind, String name) {
         this.item = item;
         this.keyBind = keyBind;
         this.name = name;
      }
   }

   public static class Staff {
      Text prefix;
      public String name;
      boolean isSpec;
      Interface.Status status;
      boolean isOnServer;
      Animation animation;
      long mills;

      public Staff(Text prefix, String name, boolean isSpec, Interface.Status status) {
         this.prefix = prefix;
         this.name = name;
         this.isSpec = isSpec;
         this.status = status;
         this.animation = new Animation(Easing.EXPO_OUT, 233L);
         this.mills = System.currentTimeMillis();
      }
   }

   public static enum Status {
      NONE("", -1),
      VANISHED("SPEC", ColorProvider.rgba(229, 0, 63, 255.0F));

      public final String string;
      public final int color;

      private Status(String string, int color) {
         this.string = string;
         this.color = color;
      }
   }
}
