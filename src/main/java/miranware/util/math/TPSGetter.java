package miranware.util.math;

import com.google.common.eventbus.Subscribe;
import java.util.LinkedList;
import java.util.Queue;
import lombok.Generated;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.util.math.MathHelper;
import miranware.MiranWare;
import miranware.event.list.EventPacket;

public class TPSGetter {
   private final Queue<Float> tpsHistory = new LinkedList<>();
   private float TPS = 20.0F;
   private float averageTPS = 20.0F;
   private float adjustTicks = 0.0F;
   private long timestamp;

   public TPSGetter() {
      MiranWare.getInstance().getEventBus().register(this);
   }

   @Subscribe
   private void onPacket(EventPacket e) {
      if (e.getPacket() != null && e.getPacket() instanceof WorldTimeUpdateS2CPacket) {
         this.updateTPS();
      }
   }

   private void updateTPS() {
      long delay = System.nanoTime() - this.timestamp;
      float maxTPS = 20.0F;
      float rawTPS = maxTPS * (1.0E9F / (float)delay);
      float boundedTPS = MathHelper.clamp(rawTPS, 0.0F, maxTPS);
      this.TPS = (float)this.round((double)boundedTPS);
      this.adjustTicks = boundedTPS - maxTPS;
      this.timestamp = System.nanoTime();
      this.updateAverageTPS();
   }

   private void updateAverageTPS() {
      if (this.tpsHistory.size() >= 10) {
         this.tpsHistory.poll();
      }

      this.tpsHistory.add(this.TPS);
      float sum = 0.0F;

      for (float tps : this.tpsHistory) {
         sum += tps;
      }

      this.averageTPS = (float)this.round((double)(sum / (float)this.tpsHistory.size()));
   }

   public double round(double input) {
      return (double)Math.round(input * 100.0) / 100.0;
   }

   @Generated
   public Queue<Float> getTpsHistory() {
      return this.tpsHistory;
   }

   @Generated
   public float getTPS() {
      return this.TPS;
   }

   @Generated
   public float getAverageTPS() {
      return this.averageTPS;
   }

   @Generated
   public float getAdjustTicks() {
      return this.adjustTicks;
   }

   @Generated
   public long getTimestamp() {
      return this.timestamp;
   }
}
