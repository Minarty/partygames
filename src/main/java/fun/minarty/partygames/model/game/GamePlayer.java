package fun.minarty.partygames.model.game;

import fun.minarty.partygames.PartyGames;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Object connected to a specific player, contains <br>
 * game information, status, etc
 */
public class GamePlayer {

    @Getter
    private final UUID uniqueId;
    private final UUID player;
    @Getter @Setter
    private PartyGame game;
    @Getter @Setter
    private State state;
    @Getter @Setter
    private boolean ready;

    @Getter @Setter
    private int points;
    private final Map<String, Object> data = new HashMap<>();

    @Builder
    public GamePlayer(UUID uniqueId, UUID player){
        this.uniqueId = uniqueId;
        this.player = player;
        this.state = State.STANDBY;
        this.ready = true;
    }

    /**
     * Checks if player has data by key
     * @param key key to check for
     * @return boolean value
     */
    public boolean hasData(String key){
        return getData(key) != null;
    }

    /**
     * Sets a value for the key
     * @param key key to set for
     * @param o value to set
     */
    public void setData(String key, Object o){
        data.put(key, o);
    }

    /**
     * Gets data and casts it to the type
     * @param key key to get
     * @param tClass class to map for
     * @return casted
     */
    public <T> T getData(String key, Class<T> tClass){
        try {
            tClass.cast(getData(key));
        } catch (ClassCastException ex){
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * Gets data by key
     * @param key key to get for
     * @return data or null it not found
     */
    public Object getData(String key){
        return data.get(key);
    }

    /**
     * Adds points to the player (GameMode = POINTS)
     * and sends an actionbar messages + plays sound to the bukkit player
     * @param value value to increment with
     */
    public void addPoints(int value){
        setPoints(getPoints() + value);

        PartyGames plugin = PartyGames.getInstance();
        Player bukkitPlayer = getBukkitPlayer();
        if(bukkitPlayer == null)
            return;

        bukkitPlayer.sendActionBar(plugin.getTexty()
                .transform(bukkitPlayer, Component.text("+ ", NamedTextColor.AQUA)
                        .append(Component.text(value))
                        .append(Component.space())
                        .append(Component.translatable("point",
                                Component.text((value > 1 ? "s" : ""))))));
    }

    /**
     * Gets the bukkit player associated with the GamePlayer
     * @return bukkit player
     */
    public Player getBukkitPlayer(){
        return Bukkit.getPlayer(player);
    }

    /**
     * Clears all set data
     */
    public void clearData(){
        data.clear();
    }

    /**
     * Clears data for the specific key
     * @param key key to clear for
     */
    public void clearData(String key) {
        data.remove(key);
    }

    /**
     * Enum for the states which the player may be in
     */
    public enum State {
        /**
         * State when the player is in the lobby or offline
         */
        STANDBY,

        /**
         * State when the player is actively playing
         */
        IN_GAME,

        /**
         * State when the player is spectating the game
         */
        SPECTATOR
    }

}