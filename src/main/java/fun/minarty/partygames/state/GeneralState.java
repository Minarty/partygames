package fun.minarty.partygames.state;

import fun.minarty.partygames.PartyGames;
import net.minikloon.fsmgasm.State;

/**
 * State which doesn't have anything to do with a specific game
 */
public abstract class GeneralState extends State {

    protected final PartyGames plugin;

    public GeneralState(PartyGames plugin){
        this.plugin = plugin;
    }

}