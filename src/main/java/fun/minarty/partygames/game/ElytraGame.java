package fun.minarty.partygames.game;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.api.model.config.GameConfig;
import fun.minarty.partygames.api.model.game.GameType;
import fun.minarty.partygames.game.config.ElytraConfig;
import fun.minarty.partygames.model.game.GamePlayer;
import fun.minarty.partygames.model.game.PartyGame;
import fun.minarty.partygames.state.StateListen;
import fun.minarty.partygames.state.defaults.CustomState;
import fun.minarty.partygames.state.defaults.FullPlayingState;
import fun.minarty.partygames.util.Cuboid;
import fun.minarty.partygames.util.DefaultCuboid;
import fun.minarty.partygames.util.WorldEditUtil;
import net.minikloon.fsmgasm.State;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ElytraGame extends PartyGame {

    private static final int RING_COUNT = 20;
    private final Map<Cuboid, Integer> rings = new HashMap<>();

    public ElytraGame(World world, GameConfig config, GameType type,
                      List<GamePlayer> players, PartyGames plugin) {

        super(world, config, type, players, plugin);
    }

    @Override
    public Set<State> getPreGameStates() {
        return Set.of(new RingGenerationState(this, plugin, getConfig(ElytraConfig.class).getRingStart()));
    }

    @Override
    public Set<State> getPlayingStates() {
        return Set.of(new PlayingState(this, plugin));
    }

    public static class RingGenerationState extends CustomState {

        private final Location ringStart;
        private final int[] rings = new int[]{4, 2, 0};

        public RingGenerationState(PartyGame game, PartyGames plugin, Location ringStart) {
            super(game, plugin);
            this.ringStart = ringStart;
        }

        @Override
        protected void onStart() {
            generateRings();
        }

        @NotNull
        @Override
        public Duration getDuration() {
            return Duration.ZERO;
        }

        private void generateRings(){
            World world = game.getWorld();

            int fX = ringStart.getBlockX();
            int fY = ringStart.getBlockY();
            int fZ = ringStart.getBlockZ();

            ThreadLocalRandom random = ThreadLocalRandom.current();

            for (int b = 0; b < RING_COUNT; b++) {
                boolean firstRing = b == 0;
                int ring = firstRing ? 0: random.nextInt(rings.length);
                int radius = rings[ring];

                int z = fZ + (firstRing ? 0 : random.nextInt(-40, 40));
                int y = fY + (firstRing ? 0 : random.nextInt(-8,8));

                WorldEditUtil.paste(plugin.getLoadedSchematics().get("elytra" + ring), new Location(world, fX, y, z));

                Location high = world.getBlockAt(fX, y+radius, z-radius).getLocation();
                Location low  = world.getBlockAt(fX - 2, y-radius, z+radius).getLocation();

                ((ElytraGame) game).rings.put(new DefaultCuboid(high, low), ring+1);

                // Have a random distance between the rings
                fX-= random.nextInt(20, 50);
            }
        }
    }

    public static class PlayingState extends FullPlayingState {

        private final Map<UUID, Integer> progress = new HashMap<>();

        public PlayingState(PartyGame game, PartyGames plugin) {
            super(game, plugin);
        }

        @Override
        protected void onStart() {
            game.getPlayers()
                    .forEach(gamePlayer -> progress.put(gamePlayer.getUniqueId(), 0));
        }

        @StateListen
        public void onPlayerDeath(PlayerDeathEvent event){
            progress.put(event.getEntity().getUniqueId(), 0);
        }

        @StateListen
        public void onPlayerMove(PlayerMoveEvent event){
            Player player = event.getPlayer();
            int reached = progress.get(player.getUniqueId());

            for (Map.Entry<Cuboid, Integer> entry : ((ElytraGame) game).rings.entrySet()) {
                Cuboid ring = entry.getKey();
                if(ring.containsLocation(player.getLocation())) {
                    int ringX = Math.abs(ring.getHigh().getBlockX());
                    if (reached >= ringX) // Player has already been here, we should not give them another rocket
                        return;

                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                    player.getInventory().addItem(new ItemStack(Material.FIREWORK_ROCKET));

                    progress.put(player.getUniqueId(), ringX);
                    break;
                }
            }

        }
    }

}