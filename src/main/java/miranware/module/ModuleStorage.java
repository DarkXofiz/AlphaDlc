package miranware.module;

import com.google.common.eventbus.Subscribe;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Generated;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.math.MathHelper;
import miranware.MiranWare;
import miranware.event.EventGameUpdate;
import miranware.event.list.EventHUD;
import miranware.event.list.EventKeyInput;
import miranware.event.list.EventTick;
import miranware.module.list.combat.AntiBot;
import miranware.module.list.combat.AutoArmor;
import miranware.module.list.combat.AutoExplosion;
import miranware.module.list.combat.AutoPotion;
import miranware.module.list.combat.AutoSwap;
import miranware.module.list.combat.AutoTotem;
import miranware.module.list.combat.AutoTrap;
import miranware.module.list.combat.CrystalAura;
import miranware.module.list.combat.CrystalSpammer;
import miranware.module.list.combat.KillAura;
import miranware.module.list.combat.MaceTarget;
import miranware.module.list.combat.NoEntityTrace;
import miranware.module.list.combat.NoFriendDamage;
import miranware.module.list.combat.TargetPearl;
import miranware.module.list.combat.TriggerBot;
import miranware.module.list.combat.Velocity;
import miranware.module.list.misc.AutoDuel;
import miranware.module.list.misc.ChestStealer;
import miranware.module.list.misc.CrystalOptimizer;
import miranware.module.list.misc.NameProtect;
import miranware.module.list.misc.ScoreboardHealth;
import miranware.module.list.misc.ServerJoiner;
import miranware.module.list.misc.UseTracker;
import miranware.module.list.movement.AirStuck;
import miranware.module.list.movement.DogFly;
import miranware.module.list.movement.DragonFly;
import miranware.module.list.movement.ElytraBooster;
import miranware.module.list.movement.ElytraFlight;
import miranware.module.list.movement.ElytraMotion;
import miranware.module.list.movement.ElytraTarget;
import miranware.module.list.movement.GrimGlide;
import miranware.module.list.movement.GuiMove;
import miranware.module.list.movement.NoJumpDelay;
import miranware.module.list.movement.NoSlow;
import miranware.module.list.movement.NoWeb;
import miranware.module.list.movement.Speed;
import miranware.module.list.movement.Sprint;
import miranware.module.list.movement.Strafe;
import miranware.module.list.movement.Timer;
import miranware.module.list.player.AutoEat;
import miranware.module.list.player.AutoLeave;
import miranware.module.list.player.AutoTool;
import miranware.module.list.player.AutoTpaccept;
import miranware.module.list.player.ClickPearl;
import miranware.module.list.player.DeathCoords;
import miranware.module.list.player.ElytraHelper;
import miranware.module.list.player.FakePlayer;
import miranware.module.list.player.FastExp;
import miranware.module.list.player.FreeCamera;
import miranware.module.list.player.LockSlot;
import miranware.module.list.player.NoPush;
import miranware.module.list.player.RPSpoofer;
import miranware.module.list.player.ServerHelper;
import miranware.module.list.player.TapeMouse;
import miranware.module.list.player.TeleportBack;
import miranware.module.list.player.TeleportExploit;
import miranware.module.list.render.Ambience;
import miranware.module.list.render.Arrows;
import miranware.module.list.render.BlockOverlay;
import miranware.module.list.render.Cape;
import miranware.module.list.render.Chams;
import miranware.module.list.render.ClickGui;
import miranware.module.list.render.ClientSounds;
import miranware.module.list.render.FireFly;
import miranware.module.list.render.FreeLook;
import miranware.module.list.render.FullBright;
import miranware.module.list.render.GlassHands;
import miranware.module.list.render.GlowEsp;
import miranware.module.list.render.HitBubbles;
import miranware.module.list.render.HitIndicator;
import miranware.module.list.render.ItemReplacer;
import miranware.module.list.render.KillEffect;
import miranware.module.list.render.LineGlyphs;
import miranware.module.list.render.NameTags;
import miranware.module.list.render.NoRender;
import miranware.module.list.render.Particles;
import miranware.module.list.render.Predictions;
import miranware.module.list.render.SeeInvisible;
import miranware.module.list.render.ShulkerView;
import miranware.module.list.render.SoulESP;
import miranware.module.list.render.SwingAnimations;
import miranware.module.list.render.TargetESP;
import miranware.module.list.render.Tracers;
import miranware.module.list.render.Trails;
import miranware.module.list.render.UseIndicator;
import miranware.module.list.render.ViewModel;
import miranware.module.list.render.Wings;
import miranware.module.list.render.hud.Interface;
import miranware.module.settings.BooleanSetting;
import miranware.module.settings.Setting;
import miranware.module.settings.ThemeSetting;
import miranware.util.IMinecraft;
import miranware.util.base.Instance;
import miranware.util.player.other.SlownessManager;
import miranware.util.rotation.Rotation;
import miranware.util.rotation.RotationComponent;

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
      MiranWare.getInstance().getEventBus().register(this);
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
      if (mc.player != null) {
         if (!SlownessManager.slowTasksIsEmpty()) {
            SlownessManager.updateSlowTasks();
         }

         if (!SlownessManager.timeTasksIsEmpty()) {
            SlownessManager.updateTimeTasks(false);
         }

         KillAura aura = this.get(KillAura.class);
         if (!aura.isEnabled() || aura.getTarget() == null) {
            if (mc.options.getPerspective() == Perspective.THIRD_PERSON_FRONT) {
               aura.lastYaw = mc.gameRenderer.getCamera().getYaw() - 180.0F;
               aura.lastPitch = -mc.gameRenderer.getCamera().getPitch();
            } else {
               aura.lastYaw = mc.gameRenderer.getCamera().getYaw();
               aura.lastPitch = mc.gameRenderer.getCamera().getPitch();
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

      if (mc.player != null) {
      }
   }

   private void updateBackwardsOther() {
      if (mc.player.isGliding()) {
         this.speedAcceleration += 0.06F;
      } else {
         this.speedAcceleration += 0.006F;
      }

      this.get(KillAura.class).speedAcceleration = 0.0F;
      Rotation angle = new Rotation(mc.gameRenderer.getCamera().getYaw(), mc.gameRenderer.getCamera().getPitch());
      float deltaYaw = MathHelper.wrapDegrees(angle.getYaw() - mc.player.getYaw());
      float deltaPitch = angle.getPitch() - mc.player.getPitch();
      if (mc.options.getPerspective() == Perspective.THIRD_PERSON_FRONT) {
         deltaYaw = MathHelper.wrapDegrees(angle.getYaw() - 180.0F - mc.player.getYaw());
         deltaPitch = MathHelper.wrapDegrees(-angle.getPitch() - mc.player.getPitch());
      }

      float smooth = Math.max(this.speedAcceleration, 0.0F);
      float newYaw = mc.player.getYaw() + deltaYaw * Math.min(Math.max(smooth, 0.0F), 1.0F);
      float newPitch = mc.player.getPitch() + deltaPitch * Math.min(Math.max(smooth / 2.0F, 0.0F), 1.0F);
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
