package fun.minarty.partygames.state.defaults.game;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.model.game.GamePlayer;
import fun.minarty.partygames.model.game.PartyGame;
import fun.minarty.partygames.state.defaults.CustomState;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * State which teleports all players in the game to the spawn locations
 */
public class PlayerTeleportationState extends CustomState {

    public PlayerTeleportationState(PartyGame game, PartyGames plugin) {
        super(game, plugin);
    }

    @NotNull
    @Override
    public Duration getDuration() {
        return Duration.ZERO;
    }

    @Override
    protected void onEnd() {
        List<Location> spawns = game.getConfig().getSpawns();
        if(spawns == null)
            return;

        List<GamePlayer> players = game.getActivePlayers();

        switch (game.getConfig().getSpawnMode()){
            case FIRST:{
                players.forEach(gamePlayer ->
                        gamePlayer.getBukkitPlayer().teleport(spawns.get(0)));

                break;
            }

            case FILL:{
                int max = spawns.size() - 1;

                for (int i = 0; i < players.size(); i++) {
                    Player player = players.get(i).getBukkitPlayer();

                    if(i <= max){
                        player.teleport(spawns.get(i));
                    } else {
                        player.teleport(spawns.get(max));
                    }
                }

                break;
            }

            case RANDOM:{
                Random random = ThreadLocalRandom.current();
                players.forEach(gamePlayer
                        -> gamePlayer.getBukkitPlayer().teleport(spawns.get(random.nextInt(spawns.size()))));

                break;
            }
        }
    }

}
