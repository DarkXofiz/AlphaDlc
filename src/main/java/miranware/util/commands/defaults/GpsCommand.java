package miranware.util.commands.defaults;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import miranware.util.commands.api.Command;
import miranware.util.commands.api.argument.IArgConsumer;
import miranware.util.commands.api.exception.CommandException;
import miranware.util.gps.GpsRenderer;

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
      return "Sets an arrow to the specified coordinates";
   }

   @Override
   public List<String> getLongDesc() {
      return Arrays.asList(
         "Use this command to set an arrow pointing to the specified coordinates",
         "",
         "Usage:",
         "> gps <x> <z> - Sets an arrow to the specified coordinates.",
         "> gps off - Disables the arrow."
      );
   }

   @Override
   public Stream<String> tabComplete(String label, IArgConsumer args) {
      if (!args.hasAny() || args.hasExactlyOne() && args.getArgs().getFirst().getValue().isEmpty()) {
         ClientPlayerEntity p2 = MinecraftClient.getInstance().player;
         return p2 == null ? Stream.of("off") : Stream.of("off", (int)p2.getX() + " " + (int)p2.getZ());
      } else {
         return Stream.empty();
      }
   }
}
