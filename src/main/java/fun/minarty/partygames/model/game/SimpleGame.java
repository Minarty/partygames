package fun.minarty.partygames.model.game;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.api.model.config.GameConfig;
import fun.minarty.partygames.api.model.game.GameType;
import net.minikloon.fsmgasm.State;
import org.bukkit.World;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Game which don't use any own game states
 */
public class SimpleGame extends PartyGame {

    public SimpleGame(World world, GameConfig config,
                      GameType type, List<GamePlayer> players, PartyGames plugin) {

        super(world, config, type, players, plugin);
    }

    @Override
    public Set<State> getPreGameStates() {
        return Collections.emptySet();
    }

    @Override
    public Set<State> getPlayingStates() {
        return Collections.emptySet();
    }

}
