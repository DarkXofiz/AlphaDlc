package miranware.util.commands.defaults;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import miranware.util.commands.api.Command;
import miranware.util.commands.api.argument.IArgConsumer;
import miranware.util.commands.api.exception.CommandException;
import miranware.util.player.move.VerticalTeleport;

public class VClipCommand extends Command {
   public VClipCommand() {
      super("vclip");
   }

   @Override
   public void execute(String label, IArgConsumer args) throws CommandException {
      args.requireMin(1);
      String input = args.getString();
      ClientPlayerEntity player = MinecraftClient.getInstance().player;
      ClientWorld world = MinecraftClient.getInstance().world;
      String var8 = input.toLowerCase();
      double yOffset;
      switch (var8) {
         case "up":
            yOffset = this.findOffset(player.getBlockPos(), true, world);
            break;
         case "down":
            yOffset = this.findOffset(player.getBlockPos(), false, world);
            break;
         default:
            try {
               yOffset = Double.parseDouble(input);
            } catch (NumberFormatException var11) {
               this.logDirect(
                  Formatting.RED + input + " is not a number."
               );
               return;
            }
      }

      if (yOffset == 0.0) {
         this.logDirect(
            Formatting.RED
               + "Teleport failed."
         );
      } else {
         VerticalTeleport.teleport(yOffset);
         this.logDirect(
            "Teleported "
               + (int)yOffset
               + " blocks vertically"
         );
      }
   }

   private double findOffset(BlockPos pos, boolean toUp, ClientWorld world) {
      ClientPlayerEntity player = MinecraftClient.getInstance().player;
      if (toUp) {
         for (int i = 3; i < 255; i++) {
            BlockPos base = pos.add(0, i, 0);
            BlockPos head = base.up();
            if (world.getBlockState(base).isAir() && world.getBlockState(head).isAir()) {
               return (double)base.getY() - player.getY();
            }
         }
      } else {
         for (int ix = -1; ix > -255; ix--) {
            BlockPos solid = pos.add(0, ix, 0);
            BlockPos air1 = solid.down();
            BlockPos air2 = air1.down();
            boolean isSolid = !world.getBlockState(solid).isAir();
            boolean isAirBelow1 = world.getBlockState(air1).isAir();
            boolean isAirBelow2 = world.getBlockState(air2).isAir();
            if (isSolid && isAirBelow1 && isAirBelow2) {
               return (double)air2.getY() - player.getY();
            }
         }
      }

      return 0.0;
   }

   @Override
   public String getShortDesc() {
      return "Vertical teleport";
   }

   @Override
   public List<String> getLongDesc() {
      return List.of(
         "Teleports the player up or down",
         "",
         "> vclip <distance> \u2014 teleport a set number of blocks",
         "> vclip up \u2014 up to the nearest free block",
         "> vclip down \u2014 down to the nearest free block"
      );
   }

   @Override
   public Stream<String> tabComplete(String label, IArgConsumer args) {
      return args.hasExactlyOne() ? Stream.of("up", "down") : Stream.empty();
   }
}
