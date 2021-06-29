package fun.minarty.partygames.game;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.api.model.config.GameConfig;
import fun.minarty.partygames.api.model.game.GameType;
import fun.minarty.partygames.api.model.profile.GameStatistic;
import fun.minarty.partygames.game.config.MemoryConfig;
import fun.minarty.partygames.model.game.PartyGame;
import fun.minarty.partygames.model.game.GamePlayer;
import fun.minarty.partygames.state.EndingScheduledStateSeries;
import fun.minarty.partygames.state.GameState;
import fun.minarty.partygames.util.Cuboid;
import net.minikloon.fsmgasm.State;
import net.minikloon.fsmgasm.StateSeries;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class MemoryGame extends PartyGame {

    private final StateSeries roundSeries = new EndingScheduledStateSeries(plugin);
    private List<Cuboid> platforms;
    private List<Cuboid> indicators;

    private static final Map<Material, ChatColor> COLOURS = Map.of(
            Material.RED_CONCRETE, ChatColor.DARK_RED,
            Material.BLUE_CONCRETE, ChatColor.DARK_BLUE,
            Material.LIME_CONCRETE, ChatColor.GREEN,
            Material.LIGHT_BLUE_CONCRETE, ChatColor.BLUE,
            Material.YELLOW_CONCRETE, ChatColor.YELLOW,
            Material.PINK_CONCRETE, ChatColor.LIGHT_PURPLE,
            Material.CYAN_CONCRETE, ChatColor.DARK_AQUA,
            Material.BLACK_CONCRETE, ChatColor.BLACK);

    private static final List<Material> MATERIALS = new ArrayList<>(COLOURS.keySet());

    public MemoryGame(World world, GameConfig config,
                      GameType type, List<GamePlayer> players, PartyGames plugin) {

        super(world, config, type, players, plugin);
    }

    @Override
    public Set<State> getPreGameStates() {
        return new HashSet<>();
    }

    @Override
    public Set<State> getPlayingStates() {
        MemoryConfig memoryConfig = (MemoryConfig) getConfig();
        platforms  = memoryConfig.getPlatforms();
        indicators = memoryConfig.getIndicators();

        Set<State> states = new HashSet<>();
        checkRound();
        states.add(roundSeries);
        return states;
    }

    private void checkRound(){
        roundSeries.add(new RoundState(this, plugin));
    }

    public class RoundState extends GameState {

        private int tick = 0;
        private Material selectedType;
        private final Map<Cuboid, Material> memory = new HashMap<>();
        private final List<Material> colours = new ArrayList<>();

        public RoundState(PartyGame game, PartyGames plugin) {
            super(game, plugin);
            for (int i = 0; i < 4; i++) {
                addRandomColour();
            }
        }

        /**
         * Recursive method which adds a random colour that hasn't been added before
         */
        private void addRandomColour(){
            int i = ThreadLocalRandom.current().nextInt(MATERIALS.size());
            Material material = MATERIALS.get(i);

            if(colours.contains(material)) {
                addRandomColour();
            } else {
                colours.add(material);
            }
        }

        @NotNull
        @Override
        public Duration getDuration() {
            return Duration.ofSeconds(25);
        }

        @Override
        protected void onEnd() {
            checkRound();
            // TODO we REALLY need generic round
            for (GamePlayer activePlayer : game.getActivePlayers()) {
                plugin.getStatisticManager().incrementStatistic(activePlayer.getUniqueId(), GameStatistic.ROUNDS_CLEARED, 1);
            }
        }

        @Override
        protected void onStart() {
            updateColours(true);
            showPlayers();
        }

        @Override
        public void onUpdate() {
            tick++;

            long remaining = getDuration().toSeconds() - (tick);
            // TODO round config?
            if(remaining == 10){ // When we should hide colours and let players go to them
                selectedType = memory.get(platforms.get(ThreadLocalRandom.current().nextInt(platforms.size()))); // Get one of the current colours
                indicators.forEach(area -> area.fill(selectedType));
                game.hidePlayers();
                updateColours(false);
            } else if(remaining == 5){
                game.showPlayers();
                for (Cuboid platform : platforms) {
                    platform.fill(memory.get(platform));
                    platform.getBlocks().stream()
                            .filter(block -> block.getType() != selectedType)
                            .forEach(block -> block.setType(Material.AIR));
                }
            }
        }

        private Material getRandomColour(){
            // TODO fix
            return colours.get(ThreadLocalRandom.current().nextInt(colours.size()));
        }

        private void updateColours(boolean reset){
            if(reset)
                indicators.forEach(indicator -> indicator.fill(Material.WHITE_CONCRETE));

            platforms.forEach(c -> {
                Material randomColour = getRandomColour();
                c.fill(reset ? randomColour : Material.WHITE_CONCRETE);
                if(reset)
                    memory.put(c, randomColour);
            });
        }

    }

}