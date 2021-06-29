package fun.minarty.partygames.event;

import fun.minarty.partygames.model.game.PartyGame;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * A generic event for something that involves a player doing something while playing
 */
public abstract class GamePlayerEvent extends PlayerEvent {

    @Getter
    private final PartyGame game;

    public GamePlayerEvent(@NotNull PartyGame game,
                           @NotNull Player who) {

        super(who);
        this.game = game;
    }

}
