package zov.alphadlc.util.commands.defaults;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.client.MinecraftClient;
import zov.alphadlc.util.commands.api.Command;
import zov.alphadlc.util.commands.api.argument.IArgConsumer;
import zov.alphadlc.util.commands.api.exception.CommandException;
import zov.alphadlc.util.rotation.Rotation;
import zov.alphadlc.util.rotation.RotationComponent;

public class RotationCommand extends Command {
   public RotationCommand() {
      super("r");
   }

   @Override
   public void execute(String label, IArgConsumer args) throws CommandException {
      args.requireMin(2);
      String input = args.getString();
      if (args.hasExactlyOne()) {
         String arg = args.getString();
         if (input.contains("yaw")) {
            RotationComponent.getInstance().stopRotation();
            RotationComponent.update(
               new Rotation(Float.parseFloat(arg), MinecraftClient.getInstance().player.getPitch()), 360.0F, 360.0F, 360.0F, 360.0F, 0, 999999, true
            );
         }

         if (input.contains("pitch")) {
            RotationComponent.getInstance().stopRotation();
            RotationComponent.update(
               new Rotation(MinecraftClient.getInstance().player.getYaw(), Float.parseFloat(arg)), 360.0F, 360.0F, 360.0F, 360.0F, 0, 999999, true
            );
         }
      }
   }

   @Override
   public String getShortDesc() {
      return "\u0421\u0442\u0430\u0432\u0438\u0442 \u0440\u043e\u0442\u0430\u0446\u0438\u044e";
   }

   @Override
   public List<String> getLongDesc() {
      return List.of("Test");
   }

   @Override
   public Stream<String> tabComplete(String label, IArgConsumer args) {
      return args.hasExactlyOne() ? Stream.of("yaw", "pitch") : Stream.empty();
   }
}
