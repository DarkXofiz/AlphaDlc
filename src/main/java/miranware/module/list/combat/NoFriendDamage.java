package miranware.module.list.combat;

import com.google.common.eventbus.Subscribe;
import miranware.event.list.EventAttack;
import miranware.module.Module;
import miranware.module.ModuleCategory;
import miranware.module.ModuleInformation;
import miranware.util.friend.Friend;
import miranware.util.friend.FriendRepository;

@ModuleInformation(
   moduleName = "No Friend Damage",
   moduleDesc = "Does not attack friends from the list",
   moduleCategory = ModuleCategory.COMBAT
)
public class NoFriendDamage extends Module {
   @Subscribe
   private void onAttack(EventAttack e) {
      for (Friend friend : FriendRepository.getFriends()) {
         if (e.getEntity() != this.mc.player && e.getEntity().getNameForScoreboard().equals(friend.name())) {
            e.cancelEvent();
         }
      }
   }
}
