package fun.minarty.partygames.game;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.api.model.config.GameConfig;
import fun.minarty.partygames.api.model.game.GameType;
import fun.minarty.partygames.game.config.AnimalSniperConfig;
import fun.minarty.partygames.model.game.PartyGame;
import fun.minarty.partygames.model.game.GamePlayer;
import fun.minarty.partygames.state.StateListen;
import fun.minarty.partygames.state.defaults.FullPlayingState;
import fun.minarty.partygames.util.Cuboid;
import fun.minarty.partygames.util.RandomUtil;
import fun.minarty.partygames.util.cooldown.Cooldowner;
import lombok.Data;
import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minikloon.fsmgasm.State;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class AnimalSniperGame extends PartyGame {

    public AnimalSniperGame(World world, GameConfig config,
                            GameType type, List<GamePlayer> players, PartyGames plugin) {

        super(world, config, type, players, plugin);
    }

    @Override
    public Set<State> getPreGameStates() {
        return Collections.emptySet();
    }

    @Override
    public Set<State> getPlayingStates() {
        AnimalSniperConfig config = getConfig(AnimalSniperConfig.class);
        return Set.of(new PlayingState(this, plugin, config.getSpawnArea(), config.getTargets()));
    }

    @Data
    public static class TargetAnimalInfo {

        @Getter
        private EntityType type;
        @Getter
        private int points;
        @Getter
        private double chance;

    }

    public static class PlayingState extends FullPlayingState {
        
        private boolean skip = false;
        private final Map<TargetAnimalInfo, Double> weights = new HashMap<>();

        private final List<Location> spawnLocations;

        public PlayingState(PartyGame game, PartyGames plugin,
                            Cuboid spawnArea, Set<TargetAnimalInfo> targets) {

            super(game, plugin);
            this.spawnLocations = spawnArea.getBlocks()
                    .stream()
                    .map(Block::getLocation)
                    .collect(Collectors.toList());

            targets.forEach(info -> weights.put(info, info.getChance()));
        }

        @Override
        public void onUpdate() {
            if(!skip) {
                for (int i = 0; i < game.getActivePlayers().size(); i++) {
                    spawnAnimal();
                }
            }

            skip = !skip;
        }

        @StateListen
        public void onCreatureSpawn(CreatureSpawnEvent event){
            if(event.getEntity() instanceof Monster &&
                    event.getEntity().getType() != EntityType.SILVERFISH) {
                event.setCancelled(true);
            }
        }

        @StateListen
        public void onEntityDamage(EntityDamageEvent event){
            if(event.getCause() == EntityDamageEvent.DamageCause.FALL)
                event.setCancelled(true);
        }

        @StateListen
        public void onEntityDeath(EntityDeathEvent event){
            LivingEntity entity = event.getEntity();

            event.setDroppedExp(0);
            event.getDrops().clear();

            int points = getPointsForEntity(entity);
            Player killer = entity.getKiller();

            if(killer != null && points != 0){
                Cooldowner cooldowner = plugin.getCooldowner();
                if(!cooldowner.hasCooldown(killer.getUniqueId())){
                    cooldowner.cooldown(killer.getUniqueId(), 500, ChronoUnit.MILLIS);
                    game.getPlayer(killer).addPoints(points);
                }
            }
        }

        private int getPointsForEntity(LivingEntity entity) {
            return weights.keySet()
                    .stream()
                    .filter(info -> info.getType() == entity.getType())
                    .findFirst()
                    .map(TargetAnimalInfo::getPoints)
                    .orElse(0);
        }

        private void spawnAnimal(){
            Location spawnLocation = spawnLocations.get(ThreadLocalRandom.current()
                    .nextInt(spawnLocations.size()));

            TargetAnimalInfo animalInfo = RandomUtil.getWeightedRandom(weights, ThreadLocalRandom.current());
            Entity entity = spawnLocation.getWorld().spawnEntity(spawnLocation,
                    animalInfo.getType());

            scheduleRepeating(() -> {
                entity.setVelocity(entity.getVelocity().divide(new Vector(2, 2, 2)));
                if (entity.isOnGround() && !entity.isDead())
                    spawnLocation.getWorld().createExplosion(entity.getLocation(), 1, false, false);
            }, 1, 1);
        }

    }

}
