package fun.minarty.partygames.state.defaults.game;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.api.model.profile.GameStatistic;
import fun.minarty.partygames.manager.StatisticManager;
import fun.minarty.partygames.model.game.PartyGame;
import fun.minarty.partygames.state.defaults.CustomState;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Arrays;

public class PlayerProfileState extends CustomState {

    public PlayerProfileState(PartyGame game, PartyGames plugin) {
        super(game, plugin);
    }

    @Override
    protected void onEnd() {
        StatisticManager statisticManager = plugin.getStatisticManager();

        // Initialize statistics for this game
        game.getPlayers().forEach(gamePlayer ->
                Arrays.stream(GameStatistic.values())
                        .forEach(statistic ->
                                statisticManager.initializeStatistic(gamePlayer.getUniqueId(), statistic, statistic.getDefaultValue())));
    }

    @NotNull
    @Override
    public Duration getDuration() {
        return Duration.ZERO;
    }

}