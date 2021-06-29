package fun.minarty.partygames.state.defaults.game;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.manager.StatisticManager;
import fun.minarty.partygames.model.game.PartyGame;
import fun.minarty.partygames.state.defaults.CustomState;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Objects;

/**
 * State used after any game has ended
 */
public class PostGameState extends CustomState {

    public PostGameState(PartyGame game, PartyGames plugin) {
        super(game, plugin);
    }

    @Override
    protected void onEnd() {
        StatisticManager statisticManager = plugin.getStatisticManager();
        game.getActivePlayers().forEach(statisticManager::updateLongestSurvivingTime);
        statisticManager.recordStatistics(game);

        game.getPlayers().stream()
                .filter(Objects::nonNull)
                .forEach(g -> {
                    plugin.getCooldowner().remove(g); // Just in case
                    plugin.sendToLobby(g, false, true);
                });

        plugin.getGameManager().unregisterGame();
        plugin.getGameManager().addPreGameStates();
    }

    @NotNull
    @Override
    public Duration getDuration() {
        return Duration.ZERO;
    }

}