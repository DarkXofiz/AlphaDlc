package zov.alphadlc.util.commands.defaults;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import zov.alphadlc.util.commands.api.Command;
import zov.alphadlc.util.commands.api.argument.IArgConsumer;
import zov.alphadlc.util.commands.api.exception.CommandException;
import zov.alphadlc.util.player.move.VerticalTeleport;

public class VClipCommand extends Command {
   public VClipCommand() {
      super("vclip");
   }

   @Override
   public void execute(String label, IArgConsumer args) throws CommandException {
      args.requireMin(1);
      String input = args.getString();
      ClientPlayerEntity player = MinecraftClient.method_1551().field_1724;
      ClientWorld world = MinecraftClient.method_1551().field_1687;
      String var8 = input.toLowerCase();
      double yOffset;
      switch (var8) {
         case "up":
            yOffset = this.findOffset(player.method_24515(), true, world);
            break;
         case "down":
            yOffset = this.findOffset(player.method_24515(), false, world);
            break;
         default:
            try {
               yOffset = Double.parseDouble(input);
            } catch (NumberFormatException var11) {
               this.logDirect(
                  Formatting.field_1061 + input + " \u043d\u0435 \u044f\u0432\u043b\u044f\u0435\u0442\u0441\u044f \u0447\u0438\u0441\u043b\u043e\u043c."
               );
               return;
            }
      }

      if (yOffset == 0.0) {
         this.logDirect(
            Formatting.field_1061
               + "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0432\u044b\u043f\u043e\u043b\u043d\u0438\u0442\u044c \u0442\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0430\u0446\u0438\u044e."
         );
      } else {
         VerticalTeleport.teleport(yOffset);
         this.logDirect(
            "\u0422\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0438\u0440\u043e\u0432\u0430\u043d\u043e \u043d\u0430 "
               + (int)yOffset
               + " \u0431\u043b\u043e\u043a\u043e\u0432 \u043f\u043e \u0432\u0435\u0440\u0442\u0438\u043a\u0430\u043b\u0438"
         );
      }
   }

   private double findOffset(BlockPos pos, boolean toUp, ClientWorld world) {
      ClientPlayerEntity player = MinecraftClient.method_1551().field_1724;
      if (toUp) {
         for (int i = 3; i < 255; i++) {
            BlockPos base = pos.method_10069(0, i, 0);
            BlockPos head = base.method_10084();
            if (world.method_8320(base).method_26215() && world.method_8320(head).method_26215()) {
               return (double)base.method_10264() - player.method_23318();
            }
         }
      } else {
         for (int ix = -1; ix > -255; ix--) {
            BlockPos solid = pos.method_10069(0, ix, 0);
            BlockPos air1 = solid.method_10074();
            BlockPos air2 = air1.method_10074();
            boolean isSolid = !world.method_8320(solid).method_26215();
            boolean isAirBelow1 = world.method_8320(air1).method_26215();
            boolean isAirBelow2 = world.method_8320(air2).method_26215();
            if (isSolid && isAirBelow1 && isAirBelow2) {
               return (double)air2.method_10264() - player.method_23318();
            }
         }
      }

      return 0.0;
   }

   @Override
   public String getShortDesc() {
      return "\u0422\u0435\u043b\u0435\u043f\u043e\u0440\u0442 \u043f\u043e \u0432\u0435\u0440\u0442\u0438\u043a\u0430\u043b\u0438";
   }

   @Override
   public List<String> getLongDesc() {
      return List.of(
         "\u0422\u0435\u043b\u0435\u043f\u043e\u0440\u0442\u0438\u0440\u0443\u0435\u0442 \u0438\u0433\u0440\u043e\u043a\u0430 \u0432\u0432\u0435\u0440\u0445 \u0438\u043b\u0438 \u0432\u043d\u0438\u0437",
         "",
         "> vclip <\u0440\u0430\u0441\u0441\u0442\u043e\u044f\u043d\u0438\u0435> \u2014 \u0442\u0435\u043b\u0435\u043f\u043e\u0440\u0442 \u043d\u0430 \u043e\u043f\u0440\u0435\u0434\u0435\u043b\u0435\u043d\u043d\u043e\u0435 \u043a\u043e\u043b\u0438\u0447\u0435\u0441\u0442\u0432\u043e \u0431\u043b\u043e\u043a\u043e\u0432",
         "> vclip up \u2014 \u0432\u0432\u0435\u0440\u0445 \u0434\u043e \u0441\u0432\u043e\u0431\u043e\u0434\u043d\u043e\u0433\u043e \u0431\u043b\u043e\u043a\u0430",
         "> vclip down \u2014 \u0432\u043d\u0438\u0437 \u0434\u043e \u0441\u0432\u043e\u0431\u043e\u0434\u043d\u043e\u0433\u043e \u0431\u043b\u043e\u043a\u0430"
      );
   }

   @Override
   public Stream<String> tabComplete(String label, IArgConsumer args) {
      return args.hasExactlyOne() ? Stream.of("up", "down") : Stream.empty();
   }
}
