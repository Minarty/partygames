package fun.minarty.partygames.game;

import com.fasterxml.jackson.annotation.JsonProperty;
import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.api.model.config.GameConfig;
import fun.minarty.partygames.api.model.game.GameType;
import fun.minarty.partygames.api.model.profile.GameStatistic;
import fun.minarty.partygames.game.config.TriDodgeConfig;
import fun.minarty.partygames.model.game.GamePlayer;
import fun.minarty.partygames.model.game.PartyGame;
import fun.minarty.partygames.state.EndingScheduledStateSeries;
import fun.minarty.partygames.state.GameState;
import fun.minarty.partygames.util.Cuboid;
import fun.minarty.partygames.util.MiscUtil;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import net.minikloon.fsmgasm.State;
import net.minikloon.fsmgasm.StateSeries;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class TriDodgeGame extends PartyGame {

    private static final int DISPENSE_FORCE = 5;

    private final StateSeries roundSeries;
    private int round = 0;

    public TriDodgeGame(World world, GameConfig config, GameType type, List<GamePlayer> players, PartyGames plugin) {
        super(world, config, type, players, plugin);
        roundSeries = new EndingScheduledStateSeries(plugin);
    }

    @Override
    public Set<State> getPreGameStates() {
        return Collections.emptySet();
    }

    @Override
    public Set<State> getPlayingStates() {
        Set<State> states = new HashSet<>();
        checkRound();
        states.add(roundSeries);
        return states;
    }

    private void checkRound(){
        TriDodgeConfig config = (TriDodgeConfig) getConfig();
        round++;

        RoundConfig roundConfig   = getRoundConfig();
        List<Side> activeSides    = new ArrayList<>();
        List<Side> availableSides = new ArrayList<>(config.getSides());

        for (int i = 0; i < roundConfig.getSides(); i++) {
            Side side = availableSides.get(ThreadLocalRandom.current().nextInt(availableSides.size()));
            activeSides.add(side);
            availableSides.remove(side);
        }

        roundSeries.add(new RoundState(getRoundConfig(), activeSides, this, plugin));
    }

    private RoundConfig getRoundConfig(){
        TriDodgeConfig config = (TriDodgeConfig) getConfig();
        return config.getRoundConfigs().get(round);
    }

    public class RoundState extends GameState {

        private final RoundConfig config;
        private final List<Side> sides;

        private boolean active = false;
        private int tick = 0;

        public RoundState(RoundConfig config,
                          List<Side> sides,
                          PartyGame game,
                          PartyGames plugin) {

            super(game, plugin);
            this.config = config;
            this.sides  = sides;
        }

        @NotNull
        @Override
        public Duration getDuration() {
            return Duration.ofSeconds(config.getInterval());
        }

        @Override
        protected void onEnd() {
            sides.forEach(side -> {
                side.shoot();
                side.setActive(false);
            });

            checkRound();
            // TODO we need generic round
            for (GamePlayer activePlayer : game.getActivePlayers()) {
                plugin.getStatisticManager()
                        .incrementStatistic(activePlayer.getUniqueId(), GameStatistic.ROUNDS_CLEARED, 1);
            }
        }

        @Override
        protected void onStart() {
            // TODO investigate turning off damage for whole game, not state wise?
            enableEvent(EntityDamageEvent.class);
            listen(EntityDamageByEntityEvent.class, event -> {
                if(event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE)
                    event.setDamage(20);
            });

            schedule(() -> getBukkitPlayers().forEach(player -> {
                Title title = Title.title(
                        Component.translatable(game.getLocalePrefix() + "round", NamedTextColor.AQUA),
                        Component.text(round, NamedTextColor.DARK_AQUA),
                        Title.Times.of(Duration.ofMillis(500), Duration.ofMillis(500), Duration.ZERO));

                player.showTitle(title);
            }), 15);

        }

        @Override
        public void onUpdate() {
            tick++;

            long remaining = getDuration().toSeconds() - (tick);
            if(!active && (remaining <= 4 || remaining <= config.getInterval() / 2)){ // Sides have been chosen
                sides.forEach(side -> side.setActive(true));
                active = true;
                getBukkitPlayers().forEach(player ->
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1,1));
            }

            if(remaining < 4 && remaining > 0){ // Time is about to end
                game.getBukkitPlayers().forEach(player -> {
                    Duration second = Duration.ofSeconds(1);
                    if(config.getInterval() >= 4) {
                        player.showTitle(Title.title(Component.text(remaining, NamedTextColor.RED),
                                Component.empty(),
                                Title.Times.of(second, second, second)));
                    }

                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_COW_BELL, 1, 1);
                });
            }
        }

    }

    @Getter
    public static class RoundConfig {

        private final int minRounds;
        private final int interval;
        private final int sides;

        public RoundConfig(@JsonProperty("minRounds") int minRounds,
                           @JsonProperty("interval") int interval,
                           @JsonProperty("sides") int sides){

            this.minRounds = minRounds;
            this.interval  = interval;
            this.sides     = sides;
        }

    }

    public static class Side {

        private final Cuboid indicator;
        private final Cuboid dispenser;

        public Side(@JsonProperty("indicator") Cuboid indicator,
                    @JsonProperty("dispenser") Cuboid dispenser){

            this.indicator = indicator;
            this.dispenser = dispenser;
        }

        public void shoot(){
            dispenser.getBlocks().forEach(block -> {
                Entity dispense = MiscUtil.dispenseEntity(block, EntityType.TRIDENT, DISPENSE_FORCE);
                if(dispense == null)
                    return;

                PartyGames plugin = PartyGames.getInstance();
                plugin.getServer().getScheduler()
                        .scheduleSyncDelayedTask(plugin, dispense::remove, 20); // Remove dispensed tridents a little bit after
            });
        }

        public void setActive(boolean active){
            indicator.fill(active ? Material.LIME_CONCRETE : Material.LIGHT_GRAY_CONCRETE);
        }

    }

}
