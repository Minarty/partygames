package fun.minarty.partygames.state.defaults;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.model.game.PartyGame;
import fun.minarty.partygames.state.GameState;

/**
 * State which uses empty method bodies so that you can
 * override the methods that you want to use
 */
public abstract class CustomState extends GameState {

    public CustomState(PartyGame game, PartyGames plugin) {
        super(game, plugin);
    }

    @Override
    protected void onEnd() { }

    @Override
    protected void onStart() { }

    @Override
    public void onUpdate() { }

}