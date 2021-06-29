package fun.minarty.partygames.event;

import fun.minarty.partygames.model.game.PartyGame;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event called when a player shoots a shot with the pistol, used in {@link fun.minarty.partygames.game.QuakeGame}
 */
public class QuakeShootEvent extends GamePlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final PartyGame game;

    public QuakeShootEvent(PartyGame game, Player player){
        super(game, player);
        this.game = game;
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
