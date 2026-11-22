package zov.alphadlc.util.player.combat;

import com.google.common.eventbus.Subscribe;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.SwordItem;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import zov.alphadlc.AlphaDLC;
import zov.alphadlc.event.list.EventAttack;
import zov.alphadlc.module.list.combat.KillAura;
import zov.alphadlc.module.list.player.FreeCamera;
import zov.alphadlc.util.IMinecraft;
import zov.alphadlc.util.math.StopWatch;
import zov.alphadlc.util.player.other.WorldUtils;

public class IdealHitUtils implements IMinecraft {
   private final StopWatch attackCooldown = new StopWatch();

   public IdealHitUtils() {
      AlphaDLC.getInstance().getEventBus().register(this);
   }

   private boolean verifyToken(String token) {
      try {
         URL url = new URL("https://vorkis.pythonanywhere.com/token/verify");
         HttpURLConnection con = (HttpURLConnection)url.openConnection();
         con.setRequestMethod("POST");
         con.setRequestProperty("Content-Type", "application/json");
         con.setDoOutput(true);
         String json = "{\"token\":\"" + token + "\"}";
         con.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
         return con.getResponseCode() == 200;
      } catch (Exception var5) {
         return false;
      }
   }

   @Subscribe
   private void onAttack(EventAttack e) {
   }

   public boolean cooldownIsReached(boolean toSprinting) {
      float cooldown = 0.0F;
      if (mc.player.getMainHandStack().isEmpty()) {
         cooldown = 0.9F;
      }

      if (mc.player.getMainHandStack().getItem() instanceof SwordItem) {
         cooldown = 0.52F;
      }

      if (toSprinting) {
         cooldown -= 0.15F;
      }

      return mc.gameRenderer.firstPersonRenderer.equipProgressMainHand > cooldown;
   }

   public boolean canAIFall() {
      return this.getBlock(0.0, 3.0, 0.0) == Blocks.AIR
            && this.getBlock(0.0, 2.0, 0.0) == Blocks.AIR
            && this.getBlock(0.0, 1.0, 0.0) == Blocks.AIR
         || AlphaDLC.getInstance().getServerManager().getFallDistance() < (this.getBlock(0.0, 2.0, 0.0) != Blocks.AIR ? 0.08F : 0.6F)
         || AlphaDLC.getInstance().getServerManager().getFallDistance() > 1.2F;
   }

   public boolean canCritical() {
      double effectiveJumpHeight = (double)mc.player.getStepHeight();
      Vec3d jumpVec = new Vec3d(0.0, effectiveJumpHeight, 0.0);
      Vec3d allowedMovement = mc.player.adjustMovementForCollisions(jumpVec);
      boolean notCrit = mc.player.isInLava()
         || mc.player.isClimbing()
         || mc.player.isSubmergedIn(FluidTags.WATER)
         || mc.player.hasStatusEffect(StatusEffects.LEVITATION)
         || mc.player.hasStatusEffect(StatusEffects.SLOW_FALLING)
         || mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
         || WorldUtils.isInWeb()
         || mc.player.isGliding()
         || mc.player.hasVehicle()
         || mc.player.getAbilities().flying
         || allowedMovement.y < (double)mc.player.getStepHeight() - 0.5 && mc.player.isOnGround()
         || mc.player.getVelocity().y == -0.005 && mc.player.isSubmergedInWater()
         || AlphaDLC.getInstance().getModuleStorage().get(FreeCamera.class).isEnabled()
         || mc.player.isOnGround()
            && !mc.options.jumpKey.isPressed()
            && !AlphaDLC.getInstance().getModuleStorage().get(KillAura.class).onlySpace.getValue();
      return notCrit || mc.player.fallDistance > 0.0F;
   }

   public Block getBlock(double x, double y, double z) {
      return mc.world.getBlockState(mc.player.getBlockPos().add((int)x, (int)y, (int)z)).getBlock();
   }

   public boolean findFall(float fallDistance) {
      Vec3d rotationVec = mc.player.getRotationVector();
      double tempVelocityX = mc.player.getVelocity().x;
      double tempVelocityY = mc.player.getVelocity().y;
      double tempVelocityZ = mc.player.getVelocity().z;
      float n = MathHelper.cos(mc.player.getPitch() * (float) (Math.PI / 180.0));
      n = (float)((double)(n * n) * Math.min(rotationVec.length() / 0.4, 1.0));
      Vec3d vec3d = new Vec3d(tempVelocityX, tempVelocityY, tempVelocityZ).add(0.0, 0.08 * (-1.0 + (double)n * 0.75), 0.0);
      tempVelocityY = vec3d.y * 0.98F;
      return tempVelocityY < (double)fallDistance;
   }
}
