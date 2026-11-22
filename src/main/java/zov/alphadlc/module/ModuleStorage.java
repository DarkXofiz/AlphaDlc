package zov.alphadlc.module;

import com.google.common.eventbus.Subscribe;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Generated;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.math.MathHelper;
import zov.alphadlc.AlphaDLC;
import zov.alphadlc.event.EventGameUpdate;
import zov.alphadlc.event.list.EventHUD;
import zov.alphadlc.event.list.EventKeyInput;
import zov.alphadlc.event.list.EventTick;
import zov.alphadlc.module.list.combat.AntiBot;
import zov.alphadlc.module.list.combat.AutoArmor;
import zov.alphadlc.module.list.combat.AutoExplosion;
import zov.alphadlc.module.list.combat.AutoPotion;
import zov.alphadlc.module.list.combat.AutoSwap;
import zov.alphadlc.module.list.combat.AutoTotem;
import zov.alphadlc.module.list.combat.AutoTrap;
import zov.alphadlc.module.list.combat.CrystalAura;
import zov.alphadlc.module.list.combat.CrystalSpammer;
import zov.alphadlc.module.list.combat.KillAura;
import zov.alphadlc.module.list.combat.MaceTarget;
import zov.alphadlc.module.list.combat.NoEntityTrace;
import zov.alphadlc.module.list.combat.NoFriendDamage;
import zov.alphadlc.module.list.combat.TargetPearl;
import zov.alphadlc.module.list.combat.TriggerBot;
import zov.alphadlc.module.list.combat.Velocity;
import zov.alphadlc.module.list.misc.AutoDuel;
import zov.alphadlc.module.list.misc.ChestStealer;
import zov.alphadlc.module.list.misc.CrystalOptimizer;
import zov.alphadlc.module.list.misc.NameProtect;
import zov.alphadlc.module.list.misc.ScoreboardHealth;
import zov.alphadlc.module.list.misc.ServerJoiner;
import zov.alphadlc.module.list.misc.UseTracker;
import zov.alphadlc.module.list.movement.AirStuck;
import zov.alphadlc.module.list.movement.DogFly;
import zov.alphadlc.module.list.movement.DragonFly;
import zov.alphadlc.module.list.movement.ElytraBooster;
import zov.alphadlc.module.list.movement.ElytraFlight;
import zov.alphadlc.module.list.movement.ElytraMotion;
import zov.alphadlc.module.list.movement.ElytraTarget;
import zov.alphadlc.module.list.movement.GrimGlide;
import zov.alphadlc.module.list.movement.GuiMove;
import zov.alphadlc.module.list.movement.NoJumpDelay;
import zov.alphadlc.module.list.movement.NoSlow;
import zov.alphadlc.module.list.movement.NoWeb;
import zov.alphadlc.module.list.movement.Speed;
import zov.alphadlc.module.list.movement.Sprint;
import zov.alphadlc.module.list.movement.Strafe;
import zov.alphadlc.module.list.movement.Timer;
import zov.alphadlc.module.list.player.AutoEat;
import zov.alphadlc.module.list.player.AutoLeave;
import zov.alphadlc.module.list.player.AutoTool;
import zov.alphadlc.module.list.player.AutoTpaccept;
import zov.alphadlc.module.list.player.ClickPearl;
import zov.alphadlc.module.list.player.DeathCoords;
import zov.alphadlc.module.list.player.ElytraHelper;
import zov.alphadlc.module.list.player.FakePlayer;
import zov.alphadlc.module.list.player.FastExp;
import zov.alphadlc.module.list.player.FreeCamera;
import zov.alphadlc.module.list.player.LockSlot;
import zov.alphadlc.module.list.player.NoPush;
import zov.alphadlc.module.list.player.RPSpoofer;
import zov.alphadlc.module.list.player.ServerHelper;
import zov.alphadlc.module.list.player.TapeMouse;
import zov.alphadlc.module.list.player.TeleportBack;
import zov.alphadlc.module.list.player.TeleportExploit;
import zov.alphadlc.module.list.render.Ambience;
import zov.alphadlc.module.list.render.Arrows;
import zov.alphadlc.module.list.render.BlockOverlay;
import zov.alphadlc.module.list.render.Cape;
import zov.alphadlc.module.list.render.Chams;
import zov.alphadlc.module.list.render.ClickGui;
import zov.alphadlc.module.list.render.ClientSounds;
import zov.alphadlc.module.list.render.FireFly;
import zov.alphadlc.module.list.render.FreeLook;
import zov.alphadlc.module.list.render.FullBright;
import zov.alphadlc.module.list.render.GlassHands;
import zov.alphadlc.module.list.render.GlowEsp;
import zov.alphadlc.module.list.render.HitBubbles;
import zov.alphadlc.module.list.render.HitIndicator;
import zov.alphadlc.module.list.render.ItemReplacer;
import zov.alphadlc.module.list.render.KillEffect;
import zov.alphadlc.module.list.render.LineGlyphs;
import zov.alphadlc.module.list.render.NameTags;
import zov.alphadlc.module.list.render.NoRender;
import zov.alphadlc.module.list.render.Particles;
import zov.alphadlc.module.list.render.Predictions;
import zov.alphadlc.module.list.render.SeeInvisible;
import zov.alphadlc.module.list.render.ShulkerView;
import zov.alphadlc.module.list.render.SoulESP;
import zov.alphadlc.module.list.render.SwingAnimations;
import zov.alphadlc.module.list.render.TargetESP;
import zov.alphadlc.module.list.render.Tracers;
import zov.alphadlc.module.list.render.Trails;
import zov.alphadlc.module.list.render.UseIndicator;
import zov.alphadlc.module.list.render.ViewModel;
import zov.alphadlc.module.list.render.Wings;
import zov.alphadlc.module.list.render.hud.Interface;
import zov.alphadlc.module.settings.BooleanSetting;
import zov.alphadlc.module.settings.Setting;
import zov.alphadlc.module.settings.ThemeSetting;
import zov.alphadlc.util.IMinecraft;
import zov.alphadlc.util.base.Instance;
import zov.alphadlc.util.player.other.SlownessManager;
import zov.alphadlc.util.rotation.Rotation;
import zov.alphadlc.util.rotation.RotationComponent;

public class ModuleStorage implements IMinecraft {
   private final List<Module> modules = new ArrayList<>();
   private float speedAcceleration;
   private float randomness;

   public void injectRegisterModules() {
      this.modules
         .addAll(
            List.of(
               new FullBright(),
               new ClickGui(),
               new Sprint(),
               new TriggerBot(),
               new NoRender(),
               new KillAura(),
               new NameTags(),
               new TargetESP(),
               new NoPush(),
               new SoulESP(),
               new NoJumpDelay(),
               new TeleportBack(),
               new ElytraHelper(),
               new AutoTotem(),
               new ClickPearl(),
               new UseTracker(),
               new CrystalAura(),
               new ClientSounds(),
               new NoFriendDamage(),
               new ElytraBooster(),
               new FreeCamera(),
               new SwingAnimations(),
               new Predictions(),
               new DogFly(),
               new AutoTpaccept(),
               new RPSpoofer(),
               new FireFly(),
               new AntiBot(),
               new AutoExplosion(),
               new DeathCoords(),
               new CrystalOptimizer(),
               new GuiMove(),
               new Tracers(),
               new ElytraMotion(),
               new Velocity(),
               new ElytraFlight(),
               new ViewModel(),
               new GlassHands(),
               new ItemReplacer(),
               new KillEffect(),
               new AutoArmor(),
               new Speed(),
               new AutoTool(),
               new TapeMouse(),
               new Ambience(),
               new BlockOverlay(),
               new FreeLook(),
               new Chams(),
               new GlowEsp(),
               new Trails(),
               new FastExp(),
               new NameProtect(),
               new CrystalSpammer(),
               new AirStuck(),
               new AutoSwap(),
               new NoSlow(),
               new NoWeb(),
               new FakePlayer(),
               new Interface(),
               new AutoEat(),
               new AutoLeave(),
               new Wings(),
               new ScoreboardHealth(),
               new AutoPotion(),
               new Cape(),
               new AutoDuel(),
               new DragonFly(),
               new Strafe(),
               new ChestStealer(),
               new SeeInvisible(),
               new ShulkerView(),
               new ServerJoiner(),
               new UseIndicator(),
               new ServerHelper(),
               new Particles(),
               new Arrows(),
               new TeleportExploit(),
               new AutoTrap(),
               new TargetPearl(),
               new LineGlyphs(),
               new HitIndicator(),
               new GrimGlide(),
               new Timer(),
               new MaceTarget(),
               new NoEntityTrace(),
               new LockSlot(),
               new ElytraTarget(),
               new HitBubbles()
            )
         );
      AlphaDLC.getInstance().getEventBus().register(this);
   }

   public <T extends Module> T get(String name) {
      return this.modules.stream().filter(module -> module.getName().equalsIgnoreCase(name)).map(module -> (T)module).findFirst().orElse(null);
   }

   public <T extends Module> T get(Class<T> clazz) {
      return this.modules.stream().filter(module -> clazz.isAssignableFrom(module.getClass())).map(clazz::cast).findFirst().orElse(null);
   }

   public List<Module> get(ModuleCategory category) {
      return this.modules.stream().filter(module -> module.getCategory() == category).collect(Collectors.toList());
   }

   @Subscribe
   private void onGameUpdate(EventGameUpdate e) {
      if (mc.field_1724 != null) {
         if (!SlownessManager.slowTasksIsEmpty()) {
            SlownessManager.updateSlowTasks();
         }

         if (!SlownessManager.timeTasksIsEmpty()) {
            SlownessManager.updateTimeTasks(false);
         }

         KillAura aura = this.get(KillAura.class);
         if (!aura.isEnabled() || aura.getTarget() == null) {
            if (mc.field_1690.method_31044() == Perspective.field_26666) {
               aura.lastYaw = mc.field_1773.method_19418().method_19330() - 180.0F;
               aura.lastPitch = -mc.field_1773.method_19418().method_19329();
            } else {
               aura.lastYaw = mc.field_1773.method_19418().method_19330();
               aura.lastPitch = mc.field_1773.method_19418().method_19329();
            }

            if (aura.rotation.is("Vanilla") || Instance.get(FreeLook.class).isActive()) {
               return;
            }

            this.updateBackwardsOther();
         }
      }
   }

   @Subscribe
   private void onRender(EventHUD ignored) {
      for (Module module : this.getModules()) {
         module.getAnimation().run(module.isEnabled());

         for (Setting setting : module.getSettings()) {
            if (setting instanceof BooleanSetting b) {
               b.getAnimation().run(b.getValue());
            }

            if (setting instanceof ThemeSetting t) {
               t.getValue().animation.run(1.0F);
            }
         }
      }
   }

   @Subscribe
   private void onKey(EventKeyInput e) {
      if (e.getAction() != 0) {
         for (Module module : this.getModules()) {
            module.getSettings().stream().filter(setting -> {
               if (setting instanceof BooleanSetting b && e.getKey() == b.getKey()) {
                  return true;
               }

               return false;
            }).forEach(setting -> ((BooleanSetting)setting).toggle());
         }
      }
   }

   @Subscribe
   private void onUpdate(EventTick ignored) {
      if (!SlownessManager.timeTasksIsEmpty()) {
         SlownessManager.updateTimeTasks(true);
      }

      if (mc.field_1724 != null) {
      }
   }

   private void updateBackwardsOther() {
      if (mc.field_1724.method_6128()) {
         this.speedAcceleration += 0.06F;
      } else {
         this.speedAcceleration += 0.006F;
      }

      this.get(KillAura.class).speedAcceleration = 0.0F;
      Rotation angle = new Rotation(mc.field_1773.method_19418().method_19330(), mc.field_1773.method_19418().method_19329());
      float deltaYaw = MathHelper.method_15393(angle.getYaw() - mc.field_1724.method_36454());
      float deltaPitch = angle.getPitch() - mc.field_1724.method_36455();
      if (mc.field_1690.method_31044() == Perspective.field_26666) {
         deltaYaw = MathHelper.method_15393(angle.getYaw() - 180.0F - mc.field_1724.method_36454());
         deltaPitch = MathHelper.method_15393(-angle.getPitch() - mc.field_1724.method_36455());
      }

      float smooth = Math.max(this.speedAcceleration, 0.0F);
      float newYaw = mc.field_1724.method_36454() + deltaYaw * Math.min(Math.max(smooth, 0.0F), 1.0F);
      float newPitch = mc.field_1724.method_36455() + deltaPitch * Math.min(Math.max(smooth / 2.0F, 0.0F), 1.0F);
      Rotation smoothRot = new Rotation(newYaw, newPitch);
      RotationComponent.update(smoothRot, 360.0F, 360.0F, 360.0F, 360.0F, 0, 2, false);
   }

   @Generated
   public List<Module> getModules() {
      return this.modules;
   }

   @Generated
   public float getSpeedAcceleration() {
      return this.speedAcceleration;
   }

   @Generated
   public float getRandomness() {
      return this.randomness;
   }

   @Generated
   public void setSpeedAcceleration(float speedAcceleration) {
      this.speedAcceleration = speedAcceleration;
   }

   @Generated
   public void setRandomness(float randomness) {
      this.randomness = randomness;
   }
}
