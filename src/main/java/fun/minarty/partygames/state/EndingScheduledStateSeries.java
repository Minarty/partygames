package fun.minarty.partygames.state;

import fun.minarty.partygames.PartyGames;
import net.minikloon.fsmgasm.StateSeries;

/**
 * ScheduledStateSeries which end if there are no states left
 */
public class EndingScheduledStateSeries extends StateSeries {

    private boolean lastCheck = false;

    private final PartyGames plugin;

    public EndingScheduledStateSeries(PartyGames plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isReadyToEnd() {
        boolean allEnded = (getCurrent() == getStates().size() - 1
                && getStates().get(getCurrent()).isReadyToEnd());

        boolean ready = allEnded && lastCheck;
        lastCheck = allEnded;

        return plugin.getGameManager().shouldGameEnd() || ready;
    }

}