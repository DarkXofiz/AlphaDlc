package miranware.util.commands.defaults;

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
import miranware.MiranWare;
import miranware.util.commands.api.Command;
import miranware.util.commands.api.IBaritoneChatControl;
import miranware.util.commands.api.argument.IArgConsumer;
import miranware.util.commands.api.exception.CommandException;
import miranware.util.commands.api.helpers.Paginator;
import miranware.util.commands.api.helpers.TabCompleteHelper;
import miranware.util.staff.Staff;
import miranware.util.staff.StaffManager;

public class StaffCommand extends Command {
   private final StaffManager staffManager;

   public StaffCommand(MiranWare onetap) {
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
               "Unknown subcommand. Use add/remove/list/clear.",
               Formatting.GRAY
            );
      }
   }

   private void handleAddStaff(IArgConsumer args) throws CommandException {
      args.requireMin(1);
      String name = args.getString();
      if (StaffManager.isStaff(name)) {
         this.logDirect(
            "This player is already in the moderator list",
            Formatting.GRAY
         );
      } else {
         StaffManager.addStaff(new Staff(name));
         this.logDirect(
            "Player named "
               + Formatting.WHITE
               + name
               + Formatting.GRAY
               + " was added to the moderator list"
         );
      }
   }

   private void handleRemoveStaff(IArgConsumer args) throws CommandException {
      args.requireMin(1);
      String name = args.getString();
      if (!StaffManager.isStaff(name)) {
         this.logDirect(
            "Player not found in the moderator list",
            Formatting.GRAY
         );
      } else {
         StaffManager.removeStaff(name);
         this.logDirect(
            "Player named "
               + Formatting.WHITE
               + name
               + Formatting.GRAY
               + " was removed from the moderator list"
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
         () -> this.logDirect("Moderator list:", Formatting.GRAY),
         staff -> {
            Text nameText = Text.literal(Formatting.GRAY + "- " + Formatting.WHITE + staff.name);
            Text deleteText = Text.literal(Formatting.RED + " [Delete]")
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
         "Moderator list cleared",
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
      return "Moderator list management";
   }

   @Override
   public List<String> getLongDesc() {
      return Arrays.asList(
         "This command lets you manage the moderator list.",
         "",
         "Usage:",
         "> staff add <name> - Adds a player to the moderator list.",
         "> staff remove <name> - Removes a player from the moderator list.",
         "> staff list - Shows the moderator list.",
         "> staff clear - Clears the whole moderator list."
      );
   }
}
