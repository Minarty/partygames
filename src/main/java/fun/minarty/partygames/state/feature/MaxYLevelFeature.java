package fun.minarty.partygames.state.feature;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.model.game.PartyGame;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Feature which kills players if they go below a certain Y level
 */
public class MaxYLevelFeature extends Feature {

    public MaxYLevelFeature(PartyGame game, PartyGames plugin) {
        super(game, plugin);
    }

    @Override
    protected void onStart() {
        listen(PlayerMoveEvent.class, event -> {
            Player player = event.getPlayer();

            if (player.getLocation().getY()
                    <= game.getConfig().getMaxYLevel() && player.getHealth() != 0)
                player.setHealth(0);
        });
    }

}