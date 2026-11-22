package zov.alphadlc.mixin;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.ChatInputSuggestor.SuggestionWindow;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.CommandSource;
import net.minecraft.text.OrderedText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zov.alphadlc.event.list.TabCompleteEvent;

@Mixin({ChatInputSuggestor.class})
public class ChatInputSuggestorMixin {
   @Shadow
   @Final
   TextFieldWidget field_21599;
   @Shadow
   @Final
   private List<OrderedText> field_21607;
   @Shadow
   private ParseResults<CommandSource> field_21610;
   @Shadow
   private CompletableFuture<Suggestions> field_21611;
   @Shadow
   private SuggestionWindow field_21612;
   @Shadow
   boolean field_21614;

   @Inject(
      method = {"refresh"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void preUpdateSuggestion(CallbackInfo ci) {
      String prefix = this.field_21599.method_1882().substring(0, Math.min(this.field_21599.method_1882().length(), this.field_21599.method_1881()));
      TabCompleteEvent event = new TabCompleteEvent(prefix);
      event.post();
      if (event.isCancelled()) {
         ci.cancel();
      } else {
         if (event.completions != null) {
            ci.cancel();
            this.field_21610 = null;
            if (this.field_21614) {
               return;
            }

            this.field_21599.method_1887(null);
            this.field_21612 = null;
            this.field_21607.clear();
            if (event.completions.length == 0) {
               this.field_21611 = Suggestions.empty();
            } else {
               StringRange range = StringRange.between(prefix.lastIndexOf(" ") + 1, prefix.length());
               List<Suggestion> suggestionList = Stream.of(event.completions).map(s -> new Suggestion(range, s)).collect(Collectors.toList());
               Suggestions suggestions = new Suggestions(range, suggestionList);
               this.field_21611 = new CompletableFuture<>();
               this.field_21611.complete(suggestions);
            }

            ((ChatInputSuggestor)this).method_23920(true);
         }
      }
   }
}
