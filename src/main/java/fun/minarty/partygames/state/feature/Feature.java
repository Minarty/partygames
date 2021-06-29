package fun.minarty.partygames.state.feature;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.model.game.PartyGame;
import fun.minarty.partygames.state.GameState;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public abstract class Feature extends GameState {

    private final int duration;

    public Feature(PartyGame game, PartyGames plugin) {
        super(game, plugin);
        this.duration = game.getConfig().getDuration();
    }

    public Feature(PartyGame game, PartyGames plugin, int duration) {
        super(game, plugin);
        this.duration = duration;
    }

    @Override
    protected void onEnd() { }

    @Override
    protected void onStart() { }

    @Override
    public void onUpdate() { }

    @NotNull
    @Override
    public Duration getDuration() {
        return Duration.ofSeconds(duration);
    }

}