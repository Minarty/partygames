package fun.minarty.partygames.state.defaults.game;

import fun.minarty.partygames.api.model.game.PlayMode;
import fun.minarty.partygames.api.model.kit.Kit;
import fun.minarty.partygames.manager.PartyScoreboardManager;
import fun.minarty.partygames.model.game.GamePlayer;
import fun.minarty.partygames.model.game.PartyGame;
import fun.minarty.partygames.state.defaults.FullPlayingState;
import fun.minarty.partygames.PartyGames;

import java.util.List;

/**
 * Default playing state which is used in all games
 */
public class DefaultPlayingState extends FullPlayingState {

    public DefaultPlayingState(PartyGame game, PartyGames plugin) {
        super(game, plugin);
    }

    @Override
    protected void onEnd() {
        game.onEnd();
    }

    @Override
    protected void onStart() {
        game.setTimeStart(System.currentTimeMillis());
        game.getPlayers().forEach(gamePlayer ->
                plugin.getScoreboardManager().setScoreboard(gamePlayer, PartyScoreboardManager.Type.GAME));

        game.getBukkitPlayers().forEach(player -> {
            player.setGameMode(game.getConfig().getGameMode());
            player.setHealth(20);
            player.getInventory().clear();

            Kit kit = game.getConfig().getKit();
            if(kit != null)
                kit.apply(player);
        });
    }

    @Override
    public void onUpdate() {
        plugin.getScoreboardManager().updateGameScoreboard();

        List<GamePlayer> activePlayers = game.getActivePlayers();
        int offset = 0;

        if(game.getConfig().getMode() == PlayMode.FINISH_AREA)
            offset = game.getPlaces().size();

        if(activePlayers.size() + offset < plugin.getMinimumPlayers()){
            // End game if players who are still playing (active and not yet reached goal) is less than the minimum
            game.onEnd();
        }
    }

}