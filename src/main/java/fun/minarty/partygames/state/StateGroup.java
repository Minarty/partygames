package fun.minarty.partygames.state;

import net.minikloon.fsmgasm.State;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Comparator;

/**
 * Slightly modified version of FSMgasm's StateGroup
 * Gets duration of the state with the longest duration
 */
public class StateGroup extends net.minikloon.fsmgasm.StateGroup {

    @NotNull
    @Override
    public Duration getDuration() {
        State state = getStates().stream()
                .max(Comparator.comparing(State::getDuration))
                .orElse(null);

        return state != null ? state.getDuration() : Duration.ZERO;
    }

}