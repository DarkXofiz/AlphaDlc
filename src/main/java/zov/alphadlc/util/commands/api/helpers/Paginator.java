package zov.alphadlc.util.commands.api.helpers;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.ClickEvent.Action;
import net.minecraft.util.Formatting;
import zov.alphadlc.util.QuickLogger;
import zov.alphadlc.util.commands.api.argument.IArgConsumer;
import zov.alphadlc.util.commands.api.exception.CommandException;
import zov.alphadlc.util.commands.api.exception.CommandInvalidTypeException;

public class Paginator<E> implements QuickLogger {
   public final List<E> entries;
   public int pageSize = 8;
   public int page = 1;

   public Paginator(List<E> entries) {
      this.entries = entries;
   }

   @SafeVarargs
   public Paginator(E... entries) {
      this.entries = Arrays.asList(entries);
   }

   public Paginator<E> setPageSize(int pageSize) {
      this.pageSize = pageSize;
      return this;
   }

   public int getMaxPage() {
      return (this.entries.size() - 1) / this.pageSize + 1;
   }

   public boolean validPage(int page) {
      return page > 0 && page <= this.getMaxPage();
   }

   public void skipPages(int pages) {
      this.page += pages;
   }

   public void display(Function<E, Text> transform, String commandPrefix) {
      int offset = (this.page - 1) * this.pageSize;

      for (int i = offset; i < offset + this.pageSize; i++) {
         if (i < this.entries.size()) {
            this.logDirect(new Text[]{transform.apply(this.entries.get(i))});
         } else {
            this.logDirect("--", Formatting.field_1063);
         }
      }

      boolean hasPrevPage = commandPrefix != null && this.validPage(this.page - 1);
      boolean hasNextPage = commandPrefix != null && this.validPage(this.page + 1);
      MutableText prevPageComponent = Text.method_43470("<<");
      if (hasPrevPage) {
         prevPageComponent.method_10862(
            prevPageComponent.method_10866()
               .method_10958(
                  new ClickEvent(
                     Action.field_11750, commandPrefix.contains("help") ? commandPrefix + " " + (this.page - 1) : commandPrefix + " list " + (this.page - 1)
                  )
               )
               .method_10949(new HoverEvent(net.minecraft.text.HoverEvent.Action.field_24342, Text.method_43470("Click to view previous page")))
         );
      } else {
         prevPageComponent.method_10862(prevPageComponent.method_10866().method_10977(Formatting.field_1063));
      }

      MutableText nextPageComponent = Text.method_43470(">>");
      if (hasNextPage) {
         nextPageComponent.method_10862(
            nextPageComponent.method_10866()
               .method_10958(
                  new ClickEvent(
                     Action.field_11750, commandPrefix.contains("help") ? commandPrefix + " " + (this.page + 1) : commandPrefix + " list " + (this.page + 1)
                  )
               )
               .method_10949(new HoverEvent(net.minecraft.text.HoverEvent.Action.field_24342, Text.method_43470("Click to view next page")))
         );
      } else {
         nextPageComponent.method_10862(nextPageComponent.method_10866().method_10977(Formatting.field_1063));
      }

      MutableText pagerComponent = Text.method_43470("");
      pagerComponent.method_10862(pagerComponent.method_10866().method_10977(Formatting.field_1080));
      pagerComponent.method_10852(prevPageComponent);
      pagerComponent.method_27693(" | ");
      pagerComponent.method_10852(nextPageComponent);
      pagerComponent.method_27693(String.format(" %d/%d", this.page, this.getMaxPage()));
      this.logDirect(new Text[]{pagerComponent});
   }

   public void display(Function<E, Text> transform) {
      this.display(transform, null);
   }

   public static <T> void paginate(IArgConsumer consumer, Paginator<T> pagi, Runnable pre, Function<T, Text> transform, String commandPrefix) throws CommandException {
      int page = 1;
      consumer.requireMax(1);
      if (consumer.hasAny()) {
         page = consumer.getAs(Integer.class);
         if (!pagi.validPage(page)) {
            throw new CommandInvalidTypeException(
               consumer.consumed(),
               "\u0412\u044b \u0443\u043a\u0430\u0437\u0430\u043b\u0438 \u043d\u0435\u0432\u0435\u0440\u043d\u0443\u044e \u0441\u0442\u0440\u0430\u043d\u0438\u0446\u0443, \u043f\u0440\u0435\u0434\u0435\u043b\u044b: 1-"
                  + pagi.getMaxPage()
            );
         }
      }

      pagi.skipPages(page - pagi.page);
      if (pre != null) {
         pre.run();
      }

      pagi.display(transform, commandPrefix);
   }

   public static <T> void paginate(IArgConsumer consumer, List<T> elems, Runnable pre, Function<T, Text> transform, String commandPrefix) throws CommandException {
      paginate(consumer, new Paginator<>(elems), pre, transform, commandPrefix);
   }

   public static <T> void paginate(IArgConsumer consumer, T[] elems, Runnable pre, Function<T, Text> transform, String commandPrefix) throws CommandException {
      paginate(consumer, Arrays.asList(elems), pre, transform, commandPrefix);
   }

   public static <T> void paginate(IArgConsumer consumer, Paginator<T> pagi, Function<T, Text> transform, String commandPrefix) throws CommandException {
      paginate(consumer, pagi, null, transform, commandPrefix);
   }

   public static <T> void paginate(IArgConsumer consumer, List<T> elems, Function<T, Text> transform, String commandPrefix) throws CommandException {
      paginate(consumer, new Paginator<>(elems), null, transform, commandPrefix);
   }

   public static <T> void paginate(IArgConsumer consumer, T[] elems, Function<T, Text> transform, String commandPrefix) throws CommandException {
      paginate(consumer, Arrays.asList(elems), null, transform, commandPrefix);
   }

   public static <T> void paginate(IArgConsumer consumer, Paginator<T> pagi, Runnable pre, Function<T, Text> transform) throws CommandException {
      paginate(consumer, pagi, pre, transform, null);
   }

   public static <T> void paginate(IArgConsumer consumer, List<T> elems, Runnable pre, Function<T, Text> transform) throws CommandException {
      paginate(consumer, new Paginator<>(elems), pre, transform, null);
   }

   public static <T> void paginate(IArgConsumer consumer, T[] elems, Runnable pre, Function<T, Text> transform) throws CommandException {
      paginate(consumer, Arrays.asList(elems), pre, transform, null);
   }

   public static <T> void paginate(IArgConsumer consumer, Paginator<T> pagi, Function<T, Text> transform) throws CommandException {
      paginate(consumer, pagi, null, transform, null);
   }

   public static <T> void paginate(IArgConsumer consumer, List<T> elems, Function<T, Text> transform) throws CommandException {
      paginate(consumer, new Paginator<>(elems), null, transform, null);
   }

   public static <T> void paginate(IArgConsumer consumer, T[] elems, Function<T, Text> transform) throws CommandException {
      paginate(consumer, Arrays.asList(elems), null, transform, null);
   }
}
