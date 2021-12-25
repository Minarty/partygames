package fun.minarty.partygames.manager;

import cc.pollo.gladeus.scoreboard.Scoreboard;
import cc.pollo.gladeus.scoreboard.ScoreboardEntry;
import cc.pollo.gladeus.scoreboard.ScoreboardManager;
import fun.minarty.api.user.User;
import fun.minarty.grand.misc.TextFormatter;
import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.api.model.game.PlayMode;
import fun.minarty.partygames.model.game.PartyGame;
import fun.minarty.partygames.model.game.GamePlayer;
import fun.minarty.partygames.scoreboard.GameScoreboard;
import fun.minarty.partygames.scoreboard.LobbyScoreboard;
import fun.minarty.partygames.scoreboard.ScoreboardConstants;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;

import static fun.minarty.partygames.scoreboard.ScoreboardConstants.formatTopPlayerEntry;

/**
 * Manager responsible for everything related to the scoreboard
 */
public class PartyScoreboardManager {

    private final PartyGames plugin;
    private final ScoreboardManager scoreboardManager;

    public PartyScoreboardManager(PartyGames plugin) {
        this.plugin = plugin;
        this.scoreboardManager = plugin.getCommon().getScoreboardManager();
    }

    public void clear(Player player){
        scoreboardManager.clearScoreboard(player);
    }

    /**
     * Sets the scoreboard for the player
     * @param player player to set scoreboard for
     * @param type scoreboard type to set
     */
    public void setScoreboard(GamePlayer player, Type type){
        Player bukkitPlayer = player.getBukkitPlayer();
        User user = plugin.getGrand().getApi().getUserByUniqueId(player.getUniqueId());
        TextFormatter textFormatter = plugin.getGrand().getTextFormatter();

        if(user == null || textFormatter == null)
            return;

        Scoreboard scoreboard = null;
        switch (type) {
            case GAME -> scoreboard = new GameScoreboard(player.getGame());
            case LOBBY -> scoreboard = new LobbyScoreboard(user, textFormatter);
        }

        if(scoreboard == null)
            return;

        scoreboardManager.showScoreboard(bukkitPlayer, scoreboard);
    }

    /**
     * General method for updating a scoreboard entry
     * @param player player to update for
     * @param id id of the entry
     * @param value new value
     */
    public void updateEntry(Player player, String id, Component value){
        Scoreboard scoreboard = scoreboardManager.getScoreboard(player);
        if(scoreboard != null){
            ScoreboardEntry entry = scoreboard.getEntryById(id);
            if(entry != null){
                entry.update(ScoreboardConstants.FORMAT
                        .format(Component.translatable("scoreboard." + id), value));
            }
        }
    }

    /**
     * Updates the scoreboard for all players in the game, this runs every second
     */
    public void updateGameScoreboard(){

        PartyGame game = plugin.getGameManager().getGame();
        if(game == null)
            return;

        final Set<Map.Entry<GamePlayer, Integer>> topEntries;

        if(game.getConfig().getMode() == PlayMode.POINTS) { // We only need this leaderboard if the play mode is points
            topEntries = game.getTopPointsPlayersByPoints().entrySet();
        } else {
            topEntries = null;
        }

        long remainingDuration = plugin.getGameManager().getMainState().getRemainingSeconds();
        String remainingTime = String.format("%02d:%02d", remainingDuration / 60, remainingDuration % 60);

        game.getPlayers().forEach(gamePlayer -> {
            Player player = gamePlayer.getBukkitPlayer();
            if(player == null)
                return;

            Scoreboard scoreboard = scoreboardManager.getScoreboard(player);

            if(scoreboard != null){
                ScoreboardEntry time = scoreboard.getEntryById("time");
                if (time != null) {
                    time.update(ScoreboardConstants.FORMAT
                            .format(Component.translatable("scoreboard.time"), Component.text(remainingTime)));
                }

                // Show top players by points
                if(topEntries != null){
                    int i = 0;
                    for (Map.Entry<GamePlayer, Integer> topEntry : topEntries) {
                        // Stop adding entries if we have reached the limit of 5 or active players count
                        if (i == 6 || i == game.getPlayers().size()) {
                            break;
                        }

                        ScoreboardEntry entry = scoreboard.getEntryById("topEntry" + i);
                        if(entry == null)
                            continue;

                        GamePlayer key = topEntry.getKey();

                        if(key != null) {
                            entry.update(formatTopPlayerEntry(i, key.displayName(), topEntry.getValue()));
                        }

                        i++;
                    }
                }

                // Show players left
                if(game.getConfig().getMode() == PlayMode.PRESENCE){
                    ScoreboardEntry playerCountEntry = scoreboard.getEntryById("players");
                    if(playerCountEntry != null)
                        playerCountEntry.update(ScoreboardConstants.FORMAT.format(
                                Component.translatable("scoreboard.players"),
                                Component.text(String.valueOf(game.getActivePlayers().size()))));
                }

            }
        });

    }

    /**
     * Enum holding the different scoreboard types
     */
    public enum Type {
        LOBBY,
        GAME
    }

}

