package fun.minarty.partygames.event;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Event that is called when the combat tag detects <br>
 * that a player has killed another player
 */
public final class PlayerKilledEvent extends PlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final Player killer;

    public PlayerKilledEvent(Player player, Player killer) {
        super(player);
        this.killer = killer;
    }

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
