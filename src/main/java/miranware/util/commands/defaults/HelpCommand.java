package miranware.util.commands.defaults;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.ClickEvent.Action;
import net.minecraft.util.Formatting;
import miranware.MiranWare;
import miranware.util.commands.api.Command;
import miranware.util.commands.api.IBaritoneChatControl;
import miranware.util.commands.api.ICommand;
import miranware.util.commands.api.argument.IArgConsumer;
import miranware.util.commands.api.exception.CommandException;
import miranware.util.commands.api.exception.CommandNotFoundException;
import miranware.util.commands.api.helpers.Paginator;
import miranware.util.commands.api.helpers.TabCompleteHelper;
import miranware.util.commands.manager.CommandRepository;

public class HelpCommand extends Command {
   MiranWare onetap;

   protected HelpCommand(MiranWare onetap) {
      super("help");
      this.onetap = onetap;
   }

   @Override
   public void execute(String label, IArgConsumer args) throws CommandException {
      args.requireMax(1);
      CommandRepository commandRepository = this.onetap.getCommandRepository();
      if (args.hasAny() && !args.is(Integer.class)) {
         String commandName = args.getString().toLowerCase();
         ICommand command = commandRepository.getCommand(commandName);
         if (command == null) {
            throw new CommandNotFoundException(commandName);
         }

         this.logDirect("");
         command.getLongDesc().forEach(this::logDirect);
         this.logDirect("");
         MutableText returnComponent = Text.literal(
            "Click to go back to the menu"
         );
         returnComponent.setStyle(
            returnComponent.getStyle().withClickEvent(new ClickEvent(Action.RUN_COMMAND, IBaritoneChatControl.FORCE_COMMAND_PREFIX + label))
         );
         this.logDirect(new Text[]{returnComponent});
      } else {
         Paginator.paginate(
            args,
            new Paginator<>(commandRepository.getRegistry().descendingStream().filter(commandx -> !commandx.hiddenFromHelp()).collect(Collectors.toList())),
            () -> this.logDirect(
                  "Available client commands:"
               ),
            commandx -> {
               String names = String.join("/", commandx.getNames());
               String name = commandx.getNames().get(0);
               MutableText shortDescComponent = Text.literal(Formatting.DARK_GRAY + " - " + Formatting.GRAY + commandx.getShortDesc());
               shortDescComponent.setStyle(shortDescComponent.getStyle().withColor(Formatting.GRAY));
               MutableText namesComponent = Text.literal(names);
               namesComponent.setStyle(namesComponent.getStyle().withColor(Formatting.WHITE));
               MutableText hoverComponent = Text.literal("");
               hoverComponent.setStyle(hoverComponent.getStyle().withColor(Formatting.GRAY));
               hoverComponent.append(namesComponent);
               hoverComponent.append("\n" + commandx.getShortDesc());
               hoverComponent.append(
                  "\n\nClick to view the full help for this command"
               );
               String clickCommand = IBaritoneChatControl.FORCE_COMMAND_PREFIX + String.format("%s %s", label, commandx.getNames().get(0));
               MutableText component = Text.literal(name);
               component.setStyle(component.getStyle().withColor(Formatting.GRAY));
               component.append(shortDescComponent);
               component.setStyle(
                  component.getStyle()
                     .withHoverEvent(new HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, hoverComponent))
                     .withClickEvent(new ClickEvent(Action.RUN_COMMAND, clickCommand))
               );
               return component;
            },
            IBaritoneChatControl.FORCE_COMMAND_PREFIX + label
         );
      }
   }

   @Override
   public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
      return args.hasExactlyOne()
         ? new TabCompleteHelper().addCommands(MiranWare.getInstance().getCommandRepository()).filterPrefix(args.getString()).stream()
         : Stream.empty();
   }

   @Override
   public String getShortDesc() {
      return "View all available commands";
   }

   @Override
   public List<String> getLongDesc() {
      return Arrays.asList(
         "> help - Lists all commands with short descriptions.",
         "> help <command> - Shows help for a specific command."
      );
   }
}
