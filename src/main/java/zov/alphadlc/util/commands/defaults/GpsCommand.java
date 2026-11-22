package zov.alphadlc.util.commands.defaults;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import zov.alphadlc.util.commands.api.Command;
import zov.alphadlc.util.commands.api.argument.IArgConsumer;
import zov.alphadlc.util.commands.api.exception.CommandException;
import zov.alphadlc.util.gps.GpsRenderer;

public class GpsCommand extends Command {
   public GpsCommand() {
      super("gps");
   }

   @Override
   public void execute(String label, IArgConsumer args) throws CommandException {
      GpsRenderer gps = GpsRenderer.get();
      if (args.peekString().equalsIgnoreCase("off")) {
         gps.setEnabled(false);
      } else {
         double z2;
         double x2;
         try {
            x2 = Double.parseDouble(args.getString());
            z2 = Double.parseDouble(args.getString());
         } catch (NumberFormatException var9) {
            return;
         }

         gps.setTarget(x2, z2);
         gps.setEnabled(true);
      }
   }

   @Override
   public String getShortDesc() {
      return "\u0423\u0441\u0442\u0430\u043d\u0430\u0432\u043b\u0438\u0432\u0430\u0435\u0442 \u0441\u0442\u0440\u0435\u043b\u043a\u0443 \u043d\u0430 \u0443\u043a\u0430\u0437\u0430\u043d\u043d\u044b\u0435 \u043a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u044b";
   }

   @Override
   public List<String> getLongDesc() {
      return Arrays.asList(
         "\u0421 \u043f\u043e\u043c\u043e\u0449\u044c\u044e \u044d\u0442\u043e\u0439 \u043a\u043e\u043c\u0430\u043d\u0434\u044b \u043c\u043e\u0436\u043d\u043e \u0443\u0441\u0442\u0430\u043d\u043e\u0432\u0438\u0442\u044c \u0441\u0442\u0440\u0435\u043b\u043a\u0443 \u0443\u043a\u0430\u0437\u044b\u0432\u0430\u044e\u0449\u0443\u044e \u043d\u0430 \u0443\u043a\u0430\u0437\u0430\u043d\u043d\u044b\u0435 \u043a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u044b",
         "",
         "\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435:",
         "> gps <x> <z> - \u0421\u0442\u0430\u0432\u0438\u0442 \u0441\u0442\u0440\u0435\u043b\u043a\u0443 \u043d\u0430 \u0443\u043a\u0430\u0437\u0430\u043d\u043d\u044b\u0435 \u043a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u044b.",
         "> gps off - \u041e\u0442\u043a\u043b\u044e\u0447\u0430\u0435\u0442 \u0441\u0442\u0440\u0435\u043b\u043a\u0443."
      );
   }

   @Override
   public Stream<String> tabComplete(String label, IArgConsumer args) {
      if (!args.hasAny() || args.hasExactlyOne() && args.getArgs().getFirst().getValue().isEmpty()) {
         ClientPlayerEntity p2 = MinecraftClient.method_1551().field_1724;
         return p2 == null ? Stream.of("off") : Stream.of("off", (int)p2.method_23317() + " " + (int)p2.method_23321());
      } else {
         return Stream.empty();
      }
   }
}
