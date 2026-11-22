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
      PlayerListEntry list = mc.method_1562().method_2871(entity.method_5667());
      return list != null ? list.method_2959() : 0;
   }

   public static float getHealth(LivingEntity entity, boolean gapple) {
      if (AlphaDLC.getInstance().getModuleStorage().get(ScoreboardHealth.class).isEnabled()) {
         if (entity instanceof PlayerEntity player) {
            if (player.method_7327().method_1189(ScoreboardDisplaySlot.field_45158) == null) {
               return 0.0F;
            } else {
               ScoreboardObjective objective = player.method_7327().method_1189(ScoreboardDisplaySlot.field_45158);
               if (objective == null) {
                  return 0.0F;
               } else {
                  ReadableScoreboardScore score = player.method_7327().method_55430(player, objective);
                  MutableText text = ReadableScoreboardScore.method_55398(score, objective.method_55380(StyledNumberFormat.field_47566));
                  return Float.parseFloat(text.getString().replaceAll("\\D", ""));
               }
            }
         } else {
            return entity.method_6032() + (gapple ? entity.method_6067() : 0.0F);
         }
      } else {
         return entity.method_6032() + (gapple ? entity.method_6067() : 0.0F);
      }
   }

   @Generated
   private Server() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
