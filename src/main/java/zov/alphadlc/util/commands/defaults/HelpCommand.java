package zov.alphadlc.util.commands.defaults;

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
import zov.alphadlc.AlphaDLC;
import zov.alphadlc.util.commands.api.Command;
import zov.alphadlc.util.commands.api.IBaritoneChatControl;
import zov.alphadlc.util.commands.api.ICommand;
import zov.alphadlc.util.commands.api.argument.IArgConsumer;
import zov.alphadlc.util.commands.api.exception.CommandException;
import zov.alphadlc.util.commands.api.exception.CommandNotFoundException;
import zov.alphadlc.util.commands.api.helpers.Paginator;
import zov.alphadlc.util.commands.api.helpers.TabCompleteHelper;
import zov.alphadlc.util.commands.manager.CommandRepository;

public class HelpCommand extends Command {
   AlphaDLC onetap;

   protected HelpCommand(AlphaDLC onetap) {
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
         MutableText returnComponent = Text.method_43470(
            "\u041d\u0430\u0436\u043c\u0438\u0442\u0435 \u0447\u0442\u043e \u0431\u044b \u0432\u0435\u0440\u043d\u0443\u0442\u044c\u0441\u044f \u043e\u0431\u0440\u0430\u0442\u043d\u043e \u0432 \u043c\u0435\u043d\u044e"
         );
         returnComponent.method_10862(
            returnComponent.method_10866().method_10958(new ClickEvent(Action.field_11750, IBaritoneChatControl.FORCE_COMMAND_PREFIX + label))
         );
         this.logDirect(new Text[]{returnComponent});
      } else {
         Paginator.paginate(
            args,
            new Paginator<>(commandRepository.getRegistry().descendingStream().filter(commandx -> !commandx.hiddenFromHelp()).collect(Collectors.toList())),
            () -> this.logDirect(
                  "\u0414\u043e\u0441\u0442\u0443\u043f\u043d\u044b\u0435 \u043a\u043e\u043c\u0430\u043d\u0434\u044b \u0432 \u0447\u0438\u0442\u0435:"
               ),
            commandx -> {
               String names = String.join("/", commandx.getNames());
               String name = commandx.getNames().get(0);
               MutableText shortDescComponent = Text.method_43470(Formatting.field_1063 + " - " + Formatting.field_1080 + commandx.getShortDesc());
               shortDescComponent.method_10862(shortDescComponent.method_10866().method_10977(Formatting.field_1080));
               MutableText namesComponent = Text.method_43470(names);
               namesComponent.method_10862(namesComponent.method_10866().method_10977(Formatting.field_1068));
               MutableText hoverComponent = Text.method_43470("");
               hoverComponent.method_10862(hoverComponent.method_10866().method_10977(Formatting.field_1080));
               hoverComponent.method_10852(namesComponent);
               hoverComponent.method_27693("\n" + commandx.getShortDesc());
               hoverComponent.method_27693(
                  "\n\n\u041d\u0430\u0436\u043c\u0438\u0442\u0435, \u0447\u0442\u043e\u0431\u044b \u043f\u0440\u043e\u0441\u043c\u043e\u0442\u0440\u0435\u0442\u044c \u043f\u043e\u043b\u043d\u0443\u044e \u0441\u043f\u0440\u0430\u0432\u043a\u0443 \u043e \u043a\u043e\u043c\u0430\u043d\u0434\u0435"
               );
               String clickCommand = IBaritoneChatControl.FORCE_COMMAND_PREFIX + String.format("%s %s", label, commandx.getNames().get(0));
               MutableText component = Text.method_43470(name);
               component.method_10862(component.method_10866().method_10977(Formatting.field_1080));
               component.method_10852(shortDescComponent);
               component.method_10862(
                  component.method_10866()
                     .method_10949(new HoverEvent(net.minecraft.text.HoverEvent.Action.field_24342, hoverComponent))
                     .method_10958(new ClickEvent(Action.field_11750, clickCommand))
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
         ? new TabCompleteHelper().addCommands(AlphaDLC.getInstance().getCommandRepository()).filterPrefix(args.getString()).stream()
         : Stream.empty();
   }

   @Override
   public String getShortDesc() {
      return "\u041f\u0440\u043e\u0441\u043c\u043e\u0442\u0440 \u0432\u0441\u0435\u0445 \u0434\u043e\u0441\u0442\u0443\u043f\u043d\u044b\u0445 \u043a\u043e\u043c\u0430\u043d\u0434";
   }

   @Override
   public List<String> getLongDesc() {
      return Arrays.asList(
         "> help - \u041f\u0435\u0440\u0435\u0447\u0438\u0441\u043b\u044f\u0435\u0442 \u0432\u0441\u0435 \u043a\u043e\u043c\u0430\u043d\u0434\u044b \u0438 \u0438\u0445 \u043a\u0440\u0430\u0442\u043a\u0438\u0435 \u043e\u043f\u0438\u0441\u0430\u043d\u0438\u044f.",
         "> help <command> - \u041e\u0442\u043e\u0431\u0440\u0430\u0436\u0435\u043d\u0438\u0435 \u0441\u043f\u0440\u0430\u0432\u043e\u0447\u043d\u043e\u0439 \u0438\u043d\u0444\u043e\u0440\u043c\u0430\u0446\u0438\u0438 \u043f\u043e \u043a\u043e\u043d\u043a\u0440\u0435\u0442\u043d\u043e\u0439 \u043a\u043e\u043c\u0430\u043d\u0434\u0435."
      );
   }
}
