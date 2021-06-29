package fun.minarty.partygames.state.defaults;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.model.game.PartyGame;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/**
 * State which has the same duration as the duration <br>
 * defined in the config, also overrides state method bodies
 */
public class FullPlayingState extends CustomState {

    public FullPlayingState(PartyGame game, PartyGames plugin) {
        super(game, plugin);
    }

    @NotNull
    @Override
    public Duration getDuration() {
        return Duration.ofSeconds(game.getConfig().getDuration());
    }

}