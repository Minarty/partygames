package fun.minarty.partygames.state.feature;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.event.PlayerKilledEvent;
import fun.minarty.partygames.model.game.PartyGame;
import fun.minarty.partygames.model.game.GamePlayer;

/**
 * Feature which gives players a point when they kill another player
 */
public class PvPPointsFeature extends Feature {

    public PvPPointsFeature(PartyGame game, PartyGames plugin) {
        super(game, plugin);
    }

    @Override
    protected void onStart() {
        listen(PlayerKilledEvent.class, event -> {
            GamePlayer gamePlayer = plugin.getPlayerManager().getGamePlayerByPlayer(event.getKiller());
            if(gamePlayer == null)
                return;

            gamePlayer.addPoints(1);
        });
    }

}