package zov.alphadlc.module.list.movement;

import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.OnGroundOnly;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import zov.alphadlc.event.list.EventOnTravelPost;
import zov.alphadlc.event.list.EventPacket;
import zov.alphadlc.event.list.EventTick;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.util.packet.NetworkUtils;

@ModuleInformation(
   moduleName = "Elytra Exploit",
   moduleDesc = "\u042d\u043a\u0441\u043f\u043b\u043e\u0439\u0442 \u0434\u043b\u044f \u044d\u043b\u0438\u0442\u0440",
   moduleCategory = ModuleCategory.MOVEMENT
)
public class GrimGlide extends Module {
   private int flagTicks;
   private boolean isFlag;

   @Subscribe
   public void onPacket(EventPacket e2) {
      if (this.mc.field_1724 != null) {
         if (e2.getPacket() instanceof PlayerPositionLookS2CPacket) {
            this.flagTicks = 2;
            this.isFlag = true;
         }

         if (e2.getPacket() instanceof PlayerMoveC2SPacket) {
            if (this.mc.field_1724.method_6128() && this.flagTicks == 0 && !this.isFlag) {
               NetworkUtils.sendSilentPacket(new OnGroundOnly(true, true));
               e2.cancelEvent();
            }

            this.isFlag = false;
         }
      }
   }

   @Subscribe
   public void onTick(EventTick e2) {
      if (this.flagTicks > 0) {
         this.flagTicks--;
      }
   }

   @Subscribe
   public void onTravelPost(EventOnTravelPost eventOnTravelPost) {
      if (this.mc.field_1724 != null) {
         Vec3d oldVelocity = this.mc.field_1724.method_18798();
         Vec3d vec3d = this.mc.field_1724.method_5720();
         float f2 = this.mc.field_1724.method_36455() * (float) (Math.PI / 180.0);
         double d2 = Math.sqrt(vec3d.field_1352 * vec3d.field_1352 + vec3d.field_1350 * vec3d.field_1350);
         double e2 = oldVelocity.method_37267();
         boolean bl = this.mc.field_1724.method_18798().field_1351 <= 0.0;
         double g2 = bl && this.mc.field_1724.method_6059(StatusEffects.field_5906)
            ? Math.min(this.mc.field_1724.method_56989(), 0.01)
            : this.mc.field_1724.method_56989();
         double h2 = MathHelper.method_33723(Math.cos((double)f2));
         oldVelocity = oldVelocity.method_1031(0.0, g2 * (-1.0 + h2 * 0.75), 0.0);
         if (oldVelocity.field_1351 < 0.0 && d2 > 0.0) {
            double i2 = oldVelocity.field_1351 * -0.1 * h2;
            oldVelocity = oldVelocity.method_1031(vec3d.field_1352 * i2 / d2, i2, vec3d.field_1350 * i2 / d2);
         }

         if (f2 < 0.0F && d2 > 0.0) {
            double i2 = e2 * (double)(-MathHelper.method_15374(f2)) * 0.04F;
            oldVelocity = oldVelocity.method_1031(-vec3d.field_1352 * i2 / d2, i2 * 3.2, -vec3d.field_1350 * i2 / d2);
         }

         if (d2 > 0.0) {
            oldVelocity = oldVelocity.method_1031(
               (vec3d.field_1352 / d2 * e2 - oldVelocity.field_1352) * 0.1, 0.0, (vec3d.field_1350 / d2 * e2 - oldVelocity.field_1350) * 0.1
            );
         }

         double yaw = Math.toRadians((double)this.mc.field_1724.method_36454());
         double xt = -Math.sin(yaw);
         double zt = Math.cos(yaw);
         if (this.flagTicks >= 1) {
            double bst = 0.09F;
            eventOnTravelPost.setOldVelocity(oldVelocity.method_18805(0.99F, 0.98F, 0.99F).method_1031(xt * bst, 0.03F, zt * bst));
         } else {
            eventOnTravelPost.setOldVelocity(oldVelocity.method_18805(0.3F, 0.3F, 0.3F));
         }
      }
   }
}
