package miranware.util.math;

import com.google.common.eventbus.Subscribe;
import lombok.Generated;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import miranware.MiranWare;
import miranware.event.list.EventPacket;
import miranware.event.list.EventTick;
import miranware.util.IMinecraft;

public class PingGetter implements IMinecraft {
   private final StopWatch stopWatch = new StopWatch();
   private boolean lagged;
   private int ping;

   public PingGetter() {
      MiranWare.getInstance().getEventBus().register(this);
   }

   @Subscribe
   private void onUpdate(EventTick e) {
      this.ping = (int)this.stopWatch.getTime();
      if (this.stopWatch.getTime() > 1000L) {
         this.lagged = true;
      }
   }

   @Subscribe
   private void onPacket(EventPacket e) {
      if (e.getPacket() instanceof CommonPingS2CPacket) {
         this.stopWatch.reset();
      }
   }

   @Generated
   public StopWatch getStopWatch() {
      return this.stopWatch;
   }

   @Generated
   public boolean isLagged() {
      return this.lagged;
   }

   @Generated
   public int getPing() {
      return this.ping;
   }
}
