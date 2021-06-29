package fun.minarty.partygames.state.defaults.game;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.model.game.PartyGame;
import fun.minarty.partygames.state.defaults.CustomState;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/**
 * Clears all players from lobby items and such
 */
public class PlayerClearState extends CustomState {

    public PlayerClearState(PartyGame game, PartyGames plugin) {
        super(game, plugin);
    }

    @NotNull
    @Override
    public Duration getDuration() {
        return Duration.ZERO;
    }

    @Override
    protected void onEnd() {
        game.getBukkitPlayers().forEach(player -> {
            player.getInventory().clear();
            plugin.getHotbarManager().clearHotbar(player);
        });
    }

}