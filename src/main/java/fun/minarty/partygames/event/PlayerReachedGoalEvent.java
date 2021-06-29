package fun.minarty.partygames.event;

import fun.minarty.partygames.model.game.PartyGame;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Event called when a player has reached the goal <br>
 * of a game with FINISH_LINE mode
 */
public class PlayerReachedGoalEvent extends PlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final PartyGame game;
    @Getter
    private final int place;

    public PlayerReachedGoalEvent(Player player, PartyGame game, int place) {
        super(player);
        this.game  = game;
        this.place = place;
    }

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}