package fun.minarty.partygames.game;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.api.model.config.GameConfig;
import fun.minarty.partygames.api.model.game.GameType;
import fun.minarty.partygames.event.PlayerReachedGoalEvent;
import fun.minarty.partygames.model.game.GamePlayer;
import fun.minarty.partygames.model.game.PartyGame;
import fun.minarty.partygames.state.defaults.CustomState;
import fun.minarty.partygames.state.defaults.FullPlayingState;
import net.minikloon.fsmgasm.State;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class BoatRaceGame extends PartyGame {

    private final Map<Entity, UUID> boats = new HashMap<>();

    public BoatRaceGame(World world,
                        GameConfig config,
                        GameType type,
                        List<GamePlayer> players,
                        PartyGames plugin) {

        super(world, config, type, players, plugin);
        registerListeners();
    }

    @Override
    public Set<State> getPreGameStates() {
        return Set.of(new BoatSpawnState(this, plugin));
    }

    @Override
    public Set<State> getPlayingStates() {
        return Set.of(new PlayingState(this, plugin));
    }

    private void registerListeners(){
        registerListener(PlayerMoveEvent.class, event -> { // Add player to boat if they are moving outside one
            Entity boatByPlayer = getBoatByPlayer(event.getPlayer());

            if(boatByPlayer != null && (!boatByPlayer.getPassengers().contains(event.getPlayer())
                    || !event.getPlayer().isInsideVehicle()))
                boatByPlayer.addPassenger(event.getPlayer());
        });

        registerListener(VehicleMoveEvent.class, event -> {
            Vehicle vehicle = event.getVehicle();

            List<Location> spawns = getConfig().getSpawns();
            if(spawns == null || spawns.isEmpty())
                return;

            if(vehicle.getLocation().getY() <= 70){
                if(boats.containsKey(vehicle)) {
                    UUID uuid = boats.get(vehicle);
                    boats.remove(vehicle);

                    Player player = Bukkit.getPlayer(uuid);
                    if(player != null){
                        player.setVelocity(new Vector(0,0,0));
                        player.setHealth(20);
                        player.teleport(spawns.get(ThreadLocalRandom.current().nextInt(spawns.size())));

                        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin,
                                () -> spawnBoat(player), 10);
                    }

                }
                vehicle.remove();
            }
        });

        registerListener(VehicleDestroyEvent.class,
                event -> event.setCancelled(true));
    }

    private Entity getBoatByPlayer(Player player){
        Optional<Map.Entry<Entity, UUID>> any = boats.entrySet().stream()
                .filter(entry -> entry.getValue().equals(player.getUniqueId())).findAny();

        return any.map(Map.Entry::getKey).orElse(null);
    }

    public void spawnBoat(Player player){
        if(getBoatByPlayer(player) != null)
            return;

        Boat boat = (Boat) player.getWorld().spawnEntity(player.getLocation(), EntityType.BOAT);
        boat.teleport(player.getLocation());
        boat.addPassenger(player);
        boats.put(boat, player.getUniqueId());
    }

    public static class BoatSpawnState extends CustomState {

        public BoatSpawnState(PartyGame game, PartyGames plugin) {
            super(game, plugin);
        }

        @NotNull
        @Override
        public Duration getDuration() {
            return Duration.ZERO;
        }

        @Override
        protected void onStart() {
            List<Location> spawns = game.getConfig().getSpawns();
            if(spawns == null || spawns.isEmpty())
                return;

            float yaw = spawns.get(0).getYaw();
            game.getActivePlayers().forEach(gamePlayer -> {
                Player player = gamePlayer.getBukkitPlayer();
                Location location = player.getLocation().clone();
                location.setYaw(yaw);
                player.teleport(location);

                schedule(() -> ((BoatRaceGame)game).spawnBoat(player), 1);
            });
        }
    }

    public static class PlayingState extends FullPlayingState {

        public PlayingState(PartyGame game, PartyGames plugin) {
            super(game, plugin);
        }

        @Override
        protected void onStart() {
            listen(PlayerReachedGoalEvent.class, event -> { // Remove boats when player enters goal
                Player player = event.getPlayer();

                if(player.isInsideVehicle()) {
                    Entity vehicle = player.getVehicle();
                    if(vehicle == null)
                        return;

                    vehicle.remove();
                }
            });
        }

    }

}
