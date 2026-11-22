package zov.alphadlc.util.commands.defaults;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;
import zov.alphadlc.util.commands.api.Command;
import zov.alphadlc.util.commands.api.argument.IArgConsumer;
import zov.alphadlc.util.commands.api.exception.CommandException;

public class TpCommand extends Command {
   public TpCommand() {
      super("tp");
   }

   @Override
   public void execute(String label, IArgConsumer args) throws CommandException {
      args.requireMin(1);
      String name = args.getString();
      MinecraftClient mc = MinecraftClient.method_1551();
      if (mc.field_1687 != null && mc.field_1724 != null) {
         PlayerEntity entityPlayer = mc.field_1687
            .method_18456()
            .stream()
            .filter(player -> player.method_5477().getString().equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);
         if (entityPlayer == null) {
            this.logDirect(Formatting.field_1061 + "\u0418\u0433\u0440\u043e\u043a " + name + " \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d.");
         } else {
            double x = entityPlayer.method_23317();
            double y = entityPlayer.method_23318();
            double z = entityPlayer.method_23321();
            mc.field_1724.method_5814(x, y, z);
            this.logDirect(
               "\u0422\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0438\u0440\u043e\u0432\u0430\u043d\u043e \u043a \u0438\u0433\u0440\u043e\u043a\u0443 "
                  + entityPlayer.method_5477().getString()
            );
         }
      } else {
         this.logDirect(
            Formatting.field_1061
               + "\u041d\u0435\u0432\u043e\u0437\u043c\u043e\u0436\u043d\u043e \u0432\u044b\u043f\u043e\u043b\u043d\u0438\u0442\u044c \u043a\u043e\u043c\u0430\u043d\u0434\u0443."
         );
      }
   }

   @Override
   public String getShortDesc() {
      return "\u0422\u0435\u043b\u0435\u043f\u043e\u0440\u0442 \u043a \u0438\u0433\u0440\u043e\u043a\u0443";
   }

   @Override
   public List<String> getLongDesc() {
      return List.of(
         "\u0422\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0438\u0440\u0443\u0435\u0442 \u0432\u0430\u0441 \u043a \u0443\u043a\u0430\u0437\u0430\u043d\u043d\u043e\u043c\u0443 \u0438\u0433\u0440\u043e\u043a\u0443",
         "",
         "> tp <\u0438\u043c\u044f> \u2014 \u0442\u0435\u043b\u0435\u043f\u043e\u0440\u0442 \u043a \u0438\u0433\u0440\u043e\u043a\u0443"
      );
   }

   @Override
   public Stream<String> tabComplete(String label, IArgConsumer args) {
      if (args.hasExactlyOne()) {
         MinecraftClient mc = MinecraftClient.method_1551();
         if (mc.field_1687 != null) {
            return mc.field_1687.method_18456().stream().map(player -> player.method_5477().getString());
         }
      }

      return Stream.empty();
   }
}
