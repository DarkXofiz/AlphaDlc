package zov.alphadlc.util.commands.defaults;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.ClickEvent.Action;
import net.minecraft.util.Formatting;
import zov.alphadlc.AlphaDLC;
import zov.alphadlc.util.commands.api.Command;
import zov.alphadlc.util.commands.api.IBaritoneChatControl;
import zov.alphadlc.util.commands.api.argument.IArgConsumer;
import zov.alphadlc.util.commands.api.exception.CommandException;
import zov.alphadlc.util.commands.api.helpers.Paginator;
import zov.alphadlc.util.commands.api.helpers.TabCompleteHelper;
import zov.alphadlc.util.staff.Staff;
import zov.alphadlc.util.staff.StaffManager;

public class StaffCommand extends Command {
   private final StaffManager staffManager;

   public StaffCommand(AlphaDLC onetap) {
      super("staff");
      this.staffManager = onetap.getStaffManager();
   }

   @Override
   public void execute(String label, IArgConsumer args) throws CommandException {
      String action = args.hasAny() ? args.getString().toLowerCase(Locale.US) : "list";
      switch (action) {
         case "add":
            this.handleAddStaff(args);
            break;
         case "remove":
            this.handleRemoveStaff(args);
            break;
         case "list":
            this.handleListStaff(args, label);
            break;
         case "clear":
            this.handleClearStaff(args);
            break;
         default:
            this.logDirect(
               "\u041d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043d\u0430\u044f \u043f\u043e\u0434\u043a\u043e\u043c\u0430\u043d\u0434\u0430. \u0418\u0441\u043f\u043e\u043b\u044c\u0437\u0443\u0439 add/remove/list/clear.",
               Formatting.GRAY
            );
      }
   }

   private void handleAddStaff(IArgConsumer args) throws CommandException {
      args.requireMin(1);
      String name = args.getString();
      if (StaffManager.isStaff(name)) {
         this.logDirect(
            "\u042d\u0442\u043e\u0442 \u0438\u0433\u0440\u043e\u043a \u0443\u0436\u0435 \u0432 \u0441\u043f\u0438\u0441\u043a\u0435 \u043c\u043e\u0434\u0435\u0440\u0430\u0442\u043e\u0440\u043e\u0432",
            Formatting.GRAY
         );
      } else {
         StaffManager.addStaff(new Staff(name));
         this.logDirect(
            "\u0418\u0433\u0440\u043e\u043a \u0441 \u0438\u043c\u0435\u043d\u0435\u043c "
               + Formatting.WHITE
               + name
               + Formatting.GRAY
               + " \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u0434\u043e\u0431\u0430\u0432\u043b\u0435\u043d \u0432 \u0441\u043f\u0438\u0441\u043e\u043a \u043c\u043e\u0434\u0435\u0440\u0430\u0442\u043e\u0440\u043e\u0432"
         );
      }
   }

   private void handleRemoveStaff(IArgConsumer args) throws CommandException {
      args.requireMin(1);
      String name = args.getString();
      if (!StaffManager.isStaff(name)) {
         this.logDirect(
            "\u0422\u0430\u043a\u043e\u0439 \u0438\u0433\u0440\u043e\u043a \u0432 \u0441\u043f\u0438\u0441\u043a\u0435 \u043c\u043e\u0434\u0435\u0440\u0430\u0442\u043e\u0440\u043e\u0432 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d",
            Formatting.GRAY
         );
      } else {
         StaffManager.removeStaff(name);
         this.logDirect(
            "\u0418\u0433\u0440\u043e\u043a \u0441 \u0438\u043c\u0435\u043d\u0435\u043c "
               + Formatting.WHITE
               + name
               + Formatting.GRAY
               + " \u0443\u0441\u043f\u0435\u0448\u043d\u043e \u0443\u0434\u0430\u043b\u0435\u043d \u0438\u0437 \u0441\u043f\u0438\u0441\u043a\u0430 \u043c\u043e\u0434\u0435\u0440\u0430\u0442\u043e\u0440\u043e\u0432"
         );
      }
   }

   private void handleListStaff(IArgConsumer args, String label) throws CommandException {
      args.requireMax(1);
      Set<Staff> staffSet = StaffManager.getStaffList();
      List<Staff> sorted = staffSet.stream().sorted(Comparator.comparing(s -> s.name.toLowerCase())).toList();
      Paginator.paginate(
         args,
         new Paginator<>(sorted),
         () -> this.logDirect("\u0421\u043f\u0438\u0441\u043e\u043a \u043c\u043e\u0434\u0435\u0440\u0430\u0442\u043e\u0440\u043e\u0432:", Formatting.GRAY),
         staff -> {
            Text nameText = Text.literal(Formatting.GRAY + "- " + Formatting.WHITE + staff.name);
            Text deleteText = Text.literal(Formatting.RED + " [\u0423\u0434\u0430\u043b\u0438\u0442\u044c]")
               .styled(
                  style -> style.withClickEvent(new ClickEvent(Action.RUN_COMMAND, IBaritoneChatControl.FORCE_COMMAND_PREFIX + "staff remove " + staff.name))
                        .withHoverEvent(new HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, Text.literal("Click to delete staff")))
               );
            return nameText.copy().append(deleteText);
         },
         IBaritoneChatControl.FORCE_COMMAND_PREFIX + label
      );
   }

   private void handleClearStaff(IArgConsumer args) throws CommandException {
      args.requireMax(1);
      StaffManager.clearStaff();
      this.logDirect(
         "\u0421\u043f\u0438\u0441\u043e\u043a \u043c\u043e\u0434\u0435\u0440\u0430\u0442\u043e\u0440\u043e\u0432 \u043e\u0447\u0438\u0449\u0435\u043d",
         Formatting.GRAY
      );
   }

   @Override
   public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
      if (args.hasAny() && args.hasExactlyOne()) {
         return new TabCompleteHelper().prepend("add", "remove", "list", "clear").filterPrefix(args.getString()).sortAlphabetically().stream();
      } else {
         if (args.hasAny()) {
            String action = args.peekString(0).toLowerCase(Locale.ROOT);
            if (action.equals("remove") && args.hasExactly(2)) {
               String prefix = args.peekString(1).toLowerCase(Locale.ROOT);
               return StaffManager.getStaffList()
                  .stream()
                  .map(staff -> staff.name)
                  .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix))
                  .sorted()
                  .distinct();
            }

            if (action.equals("add") && args.hasExactly(2)) {
               String prefix = args.peekString(1).toLowerCase(Locale.ROOT);
               return MinecraftClient.getInstance()
                  .getNetworkHandler()
                  .getPlayerList()
                  .stream()
                  .map(info -> info.getProfile().getName())
                  .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix))
                  .sorted()
                  .distinct();
            }
         }

         return Stream.empty();
      }
   }

   @Override
   public String getShortDesc() {
      return "\u0423\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u0435 \u0441\u043f\u0438\u0441\u043a\u043e\u043c \u043c\u043e\u0434\u0435\u0440\u0430\u0442\u043e\u0440\u043e\u0432";
   }

   @Override
   public List<String> getLongDesc() {
      return Arrays.asList(
         "\u042d\u0442\u0430 \u043a\u043e\u043c\u0430\u043d\u0434\u0430 \u043f\u043e\u0437\u0432\u043e\u043b\u044f\u0435\u0442 \u0443\u043f\u0440\u0430\u0432\u043b\u044f\u0442\u044c \u0441\u043f\u0438\u0441\u043a\u043e\u043c \u043c\u043e\u0434\u0435\u0440\u0430\u0442\u043e\u0440\u043e\u0432.",
         "",
         "\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435:",
         "> staff add <name> - \u0414\u043e\u0431\u0430\u0432\u043b\u044f\u0435\u0442 \u0438\u0433\u0440\u043e\u043a\u0430 \u0432 \u0441\u043f\u0438\u0441\u043e\u043a \u043c\u043e\u0434\u0435\u0440\u0430\u0442\u043e\u0440\u043e\u0432.",
         "> staff remove <name> - \u0423\u0434\u0430\u043b\u044f\u0435\u0442 \u0438\u0433\u0440\u043e\u043a\u0430 \u0438\u0437 \u0441\u043f\u0438\u0441\u043a\u0430 \u043c\u043e\u0434\u0435\u0440\u0430\u0442\u043e\u0440\u043e\u0432.",
         "> staff list - \u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0435\u0442 \u0441\u043f\u0438\u0441\u043e\u043a \u043c\u043e\u0434\u0435\u0440\u0430\u0442\u043e\u0440\u043e\u0432.",
         "> staff clear - \u043e\u0447\u0438\u0449\u0430\u0435\u0442 \u0432\u0435\u0441\u044c \u0441\u043f\u0438\u0441\u043e\u043a \u043c\u043e\u0434\u0435\u0440\u0430\u0442\u043e\u0440\u043e\u0432."
      );
   }
}
