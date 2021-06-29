package fun.minarty.partygames.model.game;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * A tag which is added to a player when he was damaged <br>
 * containing information about who did it
 */
public class PvPTag {

    private final UUID damager;
    private final long time;

    public PvPTag(UUID damager){
        this.damager = damager;
        this.time    = System.currentTimeMillis();
    }

    /**
     * Gets the Bukkit player of the damager
     * @return bukkit player of the damager
     */
    public Player getDamagerPlayer(){
        return Bukkit.getPlayer(damager);
    }

    /**
     * Checks if the tag is valid
     * @return boolean value if tag is valid or not
     */
    public boolean isValid(){
        return getDamagerPlayer() != null &&
                System.currentTimeMillis() - time < 1000 * 4;
    }

}