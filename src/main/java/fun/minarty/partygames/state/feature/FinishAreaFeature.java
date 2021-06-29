package fun.minarty.partygames.state.feature;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.model.game.PartyGame;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Feature connected to the FINISH_LINE GameMode, calls
 * necessary methods when they reach the finish line area
 */
public class FinishAreaFeature extends Feature {

    public FinishAreaFeature(PartyGame game, PartyGames plugin) {
        super(game, plugin);
    }

    @Override
    protected void onStart() {
        listen(PlayerMoveEvent.class, event -> {
            Player player = event.getPlayer();

            if(game.getConfig().getFinishArea()
                    .containsLocation(player.getLocation())){

                game.onPlayerReachedGoal(player);
            }
        });
    }

}