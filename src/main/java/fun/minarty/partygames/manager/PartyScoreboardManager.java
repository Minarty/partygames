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
        switch (type){
            case GAME:{
                scoreboard = new GameScoreboard(player.getGame());
                break;
            }

            case LOBBY:{
                scoreboard = new LobbyScoreboard(user, textFormatter);
                break;
            }
        }

        if(scoreboard == null)
            return;

        scoreboardManager.showScoreboard(bukkitPlayer, scoreboard);

        /*
        User user = plugin.getGrand().getApi().getUserByUniqueId(player.getUniqueId());
        TextFormatter textFormatter = plugin.getGrand().getTextFormatter();

        if(user == null || textFormatter == null)
            return;

        Player bukkitPlayer = player.getBukkitPlayer();

        ScoreboardEntryRepository repository = ScoreboardEntryRepository.create();
        repository.empty();

        // If the type isn't lobby, it's a game scoreboard
        if (type != Type.LOBBY) {
            Game game = player.getGame();
            if(game != null) {
                // General entries to always show on games
                createEntry(repository, "game",
                        Component.translatable("games." + game.getType().name().toLowerCase() + ".name"));

                createEntry(repository, "time",
                        Component.text(TextUtil.formatGameDuration(game.getConfig().getDuration())));

                // Show players left for games of the type PRESENCE
                if(game.getConfig().getMode() == GameMode.PRESENCE){
                    repository.empty();
                    createEntry(repository, "players", Component.text(String.valueOf(game.getActivePlayers().size())));
                }
            }
        } else {
            createEntry(repository, "tickets", textFormatter.formatBalance(user.getTickets()));
            createEntry(repository, "coins", textFormatter.formatBalance(user.getBalance()));
        }

        // Show points top players
        if(type == Type.POINTS) {
            Game game = player.getGame();
            if(game != null) {
                repository.empty();
                repository.set("topTitle", new ScoreboardEntry(Component.translatable("scoreboard.top",
                        Style.style(NamedTextColor.AQUA, TextDecoration.BOLD))));

                int entriesToAdd = Math.min(game.getActivePlayers().size(), 5);
                for (int i = 0; i < entriesToAdd; i++) {
                    GamePlayer gamePlayer = game.getActivePlayers().get(i);
                    if(gamePlayer == null)
                        continue;

                    Player p = gamePlayer.getBukkitPlayer();
                    if(p == null)
                        continue;

                    repository.set("topEntry" + i, new ScoreboardEntry(formatTopPlayerEntry(i, p.displayName(), 0)));
                }
            }
        }

        repository.empty();

         */
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

        // TODO fix this mess

        PartyGame game = plugin.getGameManager().getGame();
        if(game == null)
            return;

        final Set<Map.Entry<GamePlayer, Integer>> topEntries;
        if(game.getConfig().getMode() == PlayMode.POINTS) {
            topEntries = game.getTopPointsPlayersByPoints().entrySet();
        } else {
            topEntries = null;
        }

        long remainingDuration = plugin.getGameManager().getMainState().getRemainingSeconds();
        String format = String.format("%02d:%02d", remainingDuration / 60, remainingDuration % 60);

        game.getPlayers().forEach(gamePlayer -> {
            Player player = gamePlayer.getBukkitPlayer();
            Scoreboard scoreboard = scoreboardManager.getScoreboard(player);

            if(scoreboard != null){
                ScoreboardEntry time = scoreboard.getEntryById("time");
                if (time != null) {
                    time.update(ScoreboardConstants.FORMAT
                            .format(Component.translatable("scoreboard.time"), Component.text(format)));
                }

                // Show top players by points
                if(game.getConfig().getMode() == PlayMode.POINTS && topEntries != null){
                    int i = 0;
                    for (Map.Entry<GamePlayer, Integer> topEntry : topEntries) {
                        // Stop showing more if we have reached the limit or the amount of active players
                        if (i == 6 || i == game.getActivePlayers().size()) {
                            break;
                        }

                        ScoreboardEntry entry = scoreboard.getEntryById("topEntry" + i);
                        if(entry == null) {
                            continue;
                        }

                        GamePlayer key = topEntry.getKey();

                        if(key != null && key.getBukkitPlayer() != null) {
                            entry.update(formatTopPlayerEntry(i, key.getBukkitPlayer().displayName(), topEntry.getValue()));
                        }

                        i++;
                    }
                }

                // Show players left
                if(game.getConfig().getMode() == PlayMode.PRESENCE){
                    ScoreboardEntry players = scoreboard.getEntryById("players");
                    if(players != null)
                        players.update(ScoreboardConstants.FORMAT.format(Component.translatable("scoreboard.players"),
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

