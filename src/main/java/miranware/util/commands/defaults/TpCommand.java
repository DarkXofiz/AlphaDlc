package miranware.util.commands.defaults;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;
import miranware.util.commands.api.Command;
import miranware.util.commands.api.argument.IArgConsumer;
import miranware.util.commands.api.exception.CommandException;

public class TpCommand extends Command {
   public TpCommand() {
      super("tp");
   }

   @Override
   public void execute(String label, IArgConsumer args) throws CommandException {
      args.requireMin(1);
      String name = args.getString();
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.world != null && mc.player != null) {
         PlayerEntity entityPlayer = mc.world
            .getPlayers()
            .stream()
            .filter(player -> player.getName().getString().equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);
         if (entityPlayer == null) {
            this.logDirect(Formatting.RED + "Player " + name + " not found.");
         } else {
            double x = entityPlayer.getX();
            double y = entityPlayer.getY();
            double z = entityPlayer.getZ();
            mc.player.setPosition(x, y, z);
            this.logDirect(
               "Teleported to player "
                  + entityPlayer.getName().getString()
            );
         }
      } else {
         this.logDirect(
            Formatting.RED
               + "Unable to run the command."
         );
      }
   }

   @Override
   public String getShortDesc() {
      return "Teleport to player";
   }

   @Override
   public List<String> getLongDesc() {
      return List.of(
         "Teleports you to the specified player",
         "",
         "> tp <name> \u2014 teleport to a player"
      );
   }

   @Override
   public Stream<String> tabComplete(String label, IArgConsumer args) {
      if (args.hasExactlyOne()) {
         MinecraftClient mc = MinecraftClient.getInstance();
         if (mc.world != null) {
            return mc.world.getPlayers().stream().map(player -> player.getName().getString());
         }
      }

      return Stream.empty();
   }
}
