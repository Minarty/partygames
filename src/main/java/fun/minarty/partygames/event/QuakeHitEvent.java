package fun.minarty.partygames.event;

import fun.minarty.partygames.model.game.PartyGame;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event called when a pistol shot hits a player, used in {@link fun.minarty.partygames.game.QuakeGame}
 */
public class QuakeHitEvent extends GamePlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final Player hit;
    @Getter
    private final PartyGame game;

    public QuakeHitEvent(PartyGame game,
                         Player player, Player hit){

        super(game, player);
        this.game = game;
        this.hit  = hit;
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
