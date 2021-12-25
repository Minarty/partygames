package fun.minarty.partygames.manager;

import fun.minarty.partygames.model.game.GamePlayer;
import fun.minarty.partygames.model.game.PartyGame;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles most things related to GamePlayers
 */
public class PlayerManager {

    @Getter
    private final Map<UUID, GamePlayer> playerCache = new HashMap<>();

    /**
     * Caches a player
     * @param player bukkit player to cache
     * @return the instance of the new GamePlayer
     */
    public GamePlayer cachePlayer(Player player){
        GamePlayer prev = getGamePlayerByPlayer(player);
        if(prev != null)
            return prev;

        GamePlayer gamePlayer = new GamePlayer(player.getUniqueId(), player.getUniqueId());
        playerCache.put(player.getUniqueId(), gamePlayer);
        return gamePlayer;
    }

    /**
     * Clears the player of the game
     * @param player player to clear
     */
    public void clearPlayer(GamePlayer player){
        player.clearData();
        player.setPoints(0);
        player.setGame(null);
        player.setState(GamePlayer.State.STANDBY);
    }

    public GamePlayer getGamePlayerByUniqueId(UUID uniqueId){
        return playerCache.get(uniqueId);
    }

    /**
     * Gets a game player by bukkit player
     * @param player bukkit player
     * @return GamePlayer instance or null if not found
     */
    public GamePlayer getGamePlayerByPlayer(Player player){
        return getGamePlayerByUniqueId(player.getUniqueId());
    }

    /**
     * Gets all players which are ready
     * @return list of ready players
     */
    public List<GamePlayer> getReadyPlayers(){
        return playerCache.values()
                .stream()
                .filter(GamePlayer::isReady)
                .filter(player -> player.getState() == GamePlayer.State.STANDBY)
                .collect(Collectors.toList());
    }


    /**
     * Sets the game for all players in the game
     * @param game game to set for
     */
    public void setGame(PartyGame game) {
        List<GamePlayer> players = game.getPlayers();
        players.forEach(gamePlayer -> {
            gamePlayer.setGame(game);
            gamePlayer.setState(GamePlayer.State.IN_GAME);
        });
    }

}
