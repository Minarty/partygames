package fun.minarty.partygames.manager;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.api.model.game.GameType;
import fun.minarty.partygames.api.model.profile.GameStatistic;
import fun.minarty.partygames.api.model.profile.Profile;
import fun.minarty.partygames.model.game.GamePlayer;
import fun.minarty.partygames.model.game.PartyGame;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class StatisticManager {

    private final PartyGames plugin;

    public StatisticManager(PartyGames plugin){
        this.plugin = plugin;
    }

    public @Nullable Object getStatistic(@NotNull Profile profile, @NotNull GameStatistic statistic) {
        PartyGame game = getApplicableGameFromStatistic(profile.getId(), statistic);
        if (game == null)
            return null;

        return profile.getStatistic(game.getType(), statistic);
    }

    public Object getStatistic(UUID uniqueId, GameStatistic statistic){
        Profile profile = plugin.getProfileManager().getProfileByUniqueId(uniqueId);
        if(profile == null)
            return null;

        return getStatistic(profile, statistic);
    }

    public void setStatistic(UUID uniqueId, GameStatistic statistic, Object value){
        Profile profile = plugin.getProfileManager().getProfileByUniqueId(uniqueId);
        if(profile == null)
            return;

        setStatistic(profile, statistic, value);
    }

    public void setStatistic(Profile profile, GameStatistic statistic, Object value) {
        PartyGame game = getApplicableGameFromStatistic(profile.getId(), statistic);
        if (game == null)
            return;

        profile.setStatistic(game.getType(), statistic, value);
    }

    public void initializeStatistic(UUID uniqueId, GameStatistic statistic, Object defaultValue){
        Profile profile = plugin.getProfileManager().getProfileByUniqueId(uniqueId);
        if(profile == null)
            return;

        initializeStatistic(profile, statistic, defaultValue);
    }

    public void initializeStatistic(Profile profile, GameStatistic statistic, @NotNull Object defaultValue){
        PartyGame game = getApplicableGameFromStatistic(profile.getId(), statistic);
        if (game == null)
            return;

        GameType type = game.getType();
        Object object = profile.getStatistic(type, statistic);
        if(object == null)
            profile.setStatistic(type, statistic, defaultValue);
    }

    public void incrementStatistic(UUID uniqueId, GameStatistic statistic, int inc) {
        Profile profile = plugin.getProfileManager().getProfileByUniqueId(uniqueId);
        if(profile == null)
            return;

        incrementStatistic(profile, statistic, inc);
    }

    public void incrementStatistic(Profile profile, GameStatistic statistic, int inc) {
        PartyGame game = getApplicableGameFromStatistic(profile.getId(), statistic);
        if (game == null)
            return;

        GameType gameType = game.getType();

        Object object = profile.getStatistic(gameType, statistic);
        if (object == null)
            object = 0;

        if(!(object instanceof Number)){
            plugin.getLogger().warning("Attempted to increment non-number statistic! " +
                            "(" + statistic.name() + ", class: " + object.getClass().getSimpleName() + ")");

            return;
        }

        Number result = 0;
        if (object instanceof Integer) {
            result = ((int) object) + inc;
        } else if(object instanceof Long){
            result = ((long) object) + inc;
        } else if(object instanceof Double){
            result = ((double) object) + inc;
        } else if(object instanceof Float){
            result = ((float) object) + inc;
        }

        profile.setStatistic(gameType, statistic, result);
    }


    public void incrementStatistic(Player player, GameStatistic statistic, int inc) {
        incrementStatistic(player.getUniqueId(), statistic, inc);
    }

    public void incrementStatistic(Player player, GameStatistic statistic) {
        incrementStatistic(player, statistic, 1);
    }

    public void recordStatistics(PartyGame game) {
        for (GamePlayer player : game.getPlayers()) {
            incrementStatistic(player.getUniqueId(), GameStatistic.GAMES_PLAYED, 1);
            Profile profile = plugin.getProfileManager().getProfileByUniqueId(player.getUniqueId());
            if (profile == null)
                continue;

            plugin.getProfileManager().update(profile);
        }
    }

    public void updateLongestSurvivingTime(GamePlayer gamePlayer) {
        PartyGame game = gamePlayer.getGame();
        if (game == null)
            return;

        long previous = getLongStatistic(gamePlayer.getUniqueId(), GameStatistic.LONGEST_SURVIVING_TIME);
        long current = game.getTimeSinceStart();

        if (current > previous) {
            setStatistic(gamePlayer.getUniqueId(), GameStatistic.LONGEST_SURVIVING_TIME, current);
        }
    }

    public long getLongStatistic(UUID playerId, GameStatistic statistic) {
        Object value = getStatistic(playerId, statistic);
        return value != null ? ((Number) value).longValue() : 0L;
    }

    private PartyGame getApplicableGameFromStatistic(UUID uniqueId, GameStatistic statistic) {
        GamePlayer gamePlayer = plugin.getPlayerManager().getGamePlayerByUniqueId(uniqueId);
        if (gamePlayer == null)
            return null;

        PartyGame game = gamePlayer.getGame();
        if (game == null || !statistic.isApplicable(game))
            return null;

        return game;
    }

}
