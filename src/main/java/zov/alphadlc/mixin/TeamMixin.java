package zov.alphadlc.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import zov.alphadlc.AlphaDLC;
import zov.alphadlc.module.list.misc.NameProtect;
import zov.alphadlc.util.replace.ReplaceUtil;

@Mixin({Team.class})
public class TeamMixin {
   @ModifyArg(
      method = {"decorateName(Lnet/minecraft/text/Text;)Lnet/minecraft/text/MutableText;"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/text/MutableText;append(Lnet/minecraft/text/Text;)Lnet/minecraft/text/MutableText;",
         ordinal = 0
      ),
      index = 0
   )
   private Text modify(Text original) {
      if (MinecraftClient.method_1551().field_1724 == null) {
         return original;
      } else {
         String me = MinecraftClient.method_1551().field_1724.method_5820();
         String replaced = AlphaDLC.getInstance().getModuleStorage().get(NameProtect.class).getCustomName(me);
         return me.equals(replaced) ? original : ReplaceUtil.replaceLiteral(original, me, replaced);
      }
   }
}
