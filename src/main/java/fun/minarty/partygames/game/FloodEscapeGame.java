package fun.minarty.partygames.game;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.api.model.config.GameConfig;
import fun.minarty.partygames.api.model.game.GameType;
import fun.minarty.partygames.event.DoorOpenEvent;
import fun.minarty.partygames.game.config.FloodEscapeConfig;
import fun.minarty.partygames.model.game.PartyGame;
import fun.minarty.partygames.model.game.GamePlayer;
import fun.minarty.partygames.state.defaults.CustomState;
import fun.minarty.partygames.state.defaults.FullPlayingState;
import fun.minarty.partygames.util.RisingWaterBlock;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minikloon.fsmgasm.State;
import net.minikloon.fsmgasm.StateSeries;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FloodEscapeGame extends PartyGame {

    public FloodEscapeGame(World world,
                           GameConfig config,
                           GameType type,
                           List<GamePlayer> players,
                           PartyGames plugin) {

        super(world, config, type, players, plugin);
        registerListeners();
    }

    private void registerListeners(){
        registerListener(DoorOpenEvent.class, event -> {
            GamePlayer player = getPlayer(event.getPlayer());
            player.addPoints(2);
        });
    }

    @Override
    public Set<State> getPreGameStates() {
        return Collections.emptySet();
    }

    @Override
    public Set<State> getPlayingStates() {
        FloodEscapeConfig config = getConfig(FloodEscapeConfig.class);
        Set<RisingWaterBlock> waterBlocks = config.getWaterArea().getBlocks()
                .stream()
                .map(block -> new RisingWaterBlock(block.getLocation(), 20))
                .collect(Collectors.toSet());

        return Set.of(new StateSeries(new GraceState(this, plugin),
                new WaterState(this, plugin, waterBlocks)));
    }

    public static class GraceState extends CustomState {

        public GraceState(PartyGame game, PartyGames plugin) {
            super(game, plugin);
        }

        @NotNull
        @Override
        public Duration getDuration() {
            return Duration.ofSeconds(15);
        }

        @Override
        public void onUpdate() {
            long seconds = getRemainingDuration().getSeconds();
            if ((seconds % 5 == 0)) {
                game.announceActionbar(Component.text("Water is coming in ", NamedTextColor.AQUA)
                        .append(Component.text(seconds, NamedTextColor.DARK_AQUA))
                                .append(Component.text(" seconds")));
            }
        }

    }

    public static class WaterState extends FullPlayingState {

        private final Set<RisingWaterBlock> waterBlocks;

        public WaterState(PartyGame game, PartyGames plugin, Set<RisingWaterBlock> waterBlocks) {
            super(game, plugin);
            this.waterBlocks = waterBlocks;
        }

        @Override
        public void onUpdate() {
            waterBlocks.forEach(RisingWaterBlock::raise);
        }

    }

}