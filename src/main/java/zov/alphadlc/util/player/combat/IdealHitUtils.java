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
      if (mc.field_1724.method_6047().method_7960()) {
         cooldown = 0.9F;
      }

      if (mc.field_1724.method_6047().method_7909() instanceof SwordItem) {
         cooldown = 0.52F;
      }

      if (toSprinting) {
         cooldown -= 0.15F;
      }

      return mc.field_1773.field_4012.field_4043 > cooldown;
   }

   public boolean canAIFall() {
      return this.getBlock(0.0, 3.0, 0.0) == Blocks.field_10124
            && this.getBlock(0.0, 2.0, 0.0) == Blocks.field_10124
            && this.getBlock(0.0, 1.0, 0.0) == Blocks.field_10124
         || AlphaDLC.getInstance().getServerManager().getFallDistance() < (this.getBlock(0.0, 2.0, 0.0) != Blocks.field_10124 ? 0.08F : 0.6F)
         || AlphaDLC.getInstance().getServerManager().getFallDistance() > 1.2F;
   }

   public boolean canCritical() {
      double effectiveJumpHeight = (double)mc.field_1724.method_49476();
      Vec3d jumpVec = new Vec3d(0.0, effectiveJumpHeight, 0.0);
      Vec3d allowedMovement = mc.field_1724.method_17835(jumpVec);
      boolean notCrit = mc.field_1724.method_5771()
         || mc.field_1724.method_6101()
         || mc.field_1724.method_5777(FluidTags.field_15517)
         || mc.field_1724.method_6059(StatusEffects.field_5902)
         || mc.field_1724.method_6059(StatusEffects.field_5906)
         || mc.field_1724.method_6059(StatusEffects.field_5919)
         || WorldUtils.isInWeb()
         || mc.field_1724.method_6128()
         || mc.field_1724.method_5765()
         || mc.field_1724.method_31549().field_7479
         || allowedMovement.field_1351 < (double)mc.field_1724.method_49476() - 0.5 && mc.field_1724.method_24828()
         || mc.field_1724.method_18798().field_1351 == -0.005 && mc.field_1724.method_5869()
         || AlphaDLC.getInstance().getModuleStorage().get(FreeCamera.class).isEnabled()
         || mc.field_1724.method_24828()
            && !mc.field_1690.field_1903.method_1434()
            && !AlphaDLC.getInstance().getModuleStorage().get(KillAura.class).onlySpace.getValue();
      return notCrit || mc.field_1724.field_6017 > 0.0F;
   }

   public Block getBlock(double x, double y, double z) {
      return mc.field_1687.method_8320(mc.field_1724.method_24515().method_10069((int)x, (int)y, (int)z)).method_26204();
   }

   public boolean findFall(float fallDistance) {
      Vec3d rotationVec = mc.field_1724.method_5720();
      double tempVelocityX = mc.field_1724.method_18798().field_1352;
      double tempVelocityY = mc.field_1724.method_18798().field_1351;
      double tempVelocityZ = mc.field_1724.method_18798().field_1350;
      float n = MathHelper.method_15362(mc.field_1724.method_36455() * (float) (Math.PI / 180.0));
      n = (float)((double)(n * n) * Math.min(rotationVec.method_1033() / 0.4, 1.0));
      Vec3d vec3d = new Vec3d(tempVelocityX, tempVelocityY, tempVelocityZ).method_1031(0.0, 0.08 * (-1.0 + (double)n * 0.75), 0.0);
      tempVelocityY = vec3d.field_1351 * 0.98F;
      return tempVelocityY < (double)fallDistance;
   }
}
