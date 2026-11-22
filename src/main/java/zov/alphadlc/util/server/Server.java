package zov.alphadlc.util.server;

import lombok.Generated;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.text.MutableText;
import zov.alphadlc.AlphaDLC;
import zov.alphadlc.module.list.misc.ScoreboardHealth;
import zov.alphadlc.util.IMinecraft;

public final class Server implements IMinecraft {
   public static int getPing(PlayerEntity entity) {
      PlayerListEntry list = mc.getNetworkHandler().getPlayerListEntry(entity.getUuid());
      return list != null ? list.getLatency() : 0;
   }

   public static float getHealth(LivingEntity entity, boolean gapple) {
      if (AlphaDLC.getInstance().getModuleStorage().get(ScoreboardHealth.class).isEnabled()) {
         if (entity instanceof PlayerEntity player) {
            if (player.getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME) == null) {
               return 0.0F;
            } else {
               ScoreboardObjective objective = player.getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME);
               if (objective == null) {
                  return 0.0F;
               } else {
                  ReadableScoreboardScore score = player.getScoreboard().getScore(player, objective);
                  MutableText text = ReadableScoreboardScore.getFormattedScore(score, objective.getNumberFormatOr(StyledNumberFormat.EMPTY));
                  return Float.parseFloat(text.getString().replaceAll("\\D", ""));
               }
            }
         } else {
            return entity.getHealth() + (gapple ? entity.getAbsorptionAmount() : 0.0F);
         }
      } else {
         return entity.getHealth() + (gapple ? entity.getAbsorptionAmount() : 0.0F);
      }
   }

   @Generated
   private Server() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
