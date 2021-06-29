package fun.minarty.partygames.event;

import fun.minarty.partygames.model.game.PartyGame;
import fun.minarty.partygames.api.model.misc.Door;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Event called when a {@link Door} is opened
 */
public class DoorOpenEvent extends PlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final PartyGame game;
    @Getter
    private final Door door;

    public DoorOpenEvent(Player player, PartyGame game, Door door){
        super(player);
        this.game = game;
        this.door = door;
    }

    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

}
