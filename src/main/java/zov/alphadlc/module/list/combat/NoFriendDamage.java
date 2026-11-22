package zov.alphadlc.module.list.combat;

import com.google.common.eventbus.Subscribe;
import zov.alphadlc.event.list.EventAttack;
import zov.alphadlc.module.Module;
import zov.alphadlc.module.ModuleCategory;
import zov.alphadlc.module.ModuleInformation;
import zov.alphadlc.util.friend.Friend;
import zov.alphadlc.util.friend.FriendRepository;

@ModuleInformation(
   moduleName = "No Friend Damage",
   moduleDesc = "\u041d\u0435 \u0430\u0442\u0430\u043a\u0443\u0435\u0442 \u0434\u0440\u0443\u0437\u0435\u0439 \u0438\u0437 \u0441\u043f\u0438\u0441\u043a\u0430",
   moduleCategory = ModuleCategory.COMBAT
)
public class NoFriendDamage extends Module {
   @Subscribe
   private void onAttack(EventAttack e) {
      for (Friend friend : FriendRepository.getFriends()) {
         if (e.getEntity() != this.mc.field_1724 && e.getEntity().method_5820().equals(friend.name())) {
            e.cancelEvent();
         }
      }
   }
}
