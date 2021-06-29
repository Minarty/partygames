package fun.minarty.partygames.game;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.api.model.config.GameConfig;
import fun.minarty.partygames.api.model.game.GameType;
import fun.minarty.partygames.game.config.AnvilConfig;
import fun.minarty.partygames.model.game.GamePlayer;
import fun.minarty.partygames.model.game.PartyGame;
import fun.minarty.partygames.state.StateListen;
import fun.minarty.partygames.state.defaults.FullPlayingState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minikloon.fsmgasm.State;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class AnvilGame extends PartyGame {

    public AnvilGame(World world, GameConfig config, GameType type,
                     List<GamePlayer> players, PartyGames plugin) {

        super(world, config, type, players, plugin);
    }

    @Override
    public Set<State> getPreGameStates() {
        return Collections.emptySet();
    }

    @Override
    public Set<State> getPlayingStates() {
        List<Location> anvilSpawnLocations = getConfig(AnvilConfig.class)
                .getAnvilArea().getBlocks()
                .stream()
                .filter(block -> block != null && block.getType() == Material.AIR)
                .map(Block::getLocation)
                .collect(Collectors.toList()); // All air blocks in the spawn areas

        return Set.of(new PlayingState(this, plugin, anvilSpawnLocations));
    }

    public static class PlayingState extends FullPlayingState {

        private final List<Location> spawnLocations;
        private final BlockData anvilData = Bukkit.createBlockData(Material.ANVIL);
        private int perSec = 1;

        public PlayingState(PartyGame game, PartyGames plugin, List<Location> spawnLocations) {
            super(game, plugin);
            this.spawnLocations = spawnLocations;
        }

        @StateListen
        public void onEntityChangeBlock(EntityChangeBlockEvent event){
            Entity entity = event.getEntity();
            if(entity instanceof FallingBlock){
                FallingBlock fallingBlock = (FallingBlock) entity;

                if(fallingBlock.getBlockData().matches(anvilData)){
                    schedule(() -> {
                        // Remove the final anvil block and its drops
                        Block block = entity.getLocation().getBlock();
                        block.getDrops().clear();
                        block.setType(Material.AIR);
                    }, 1);
                }
            }
        }

        @Override
        protected void onStart() {
            scheduleRepeating(() -> game.getActiveBukkitPlayers().forEach(player ->
                player.getNearbyEntities(0.065, 0.01, 0.065).forEach(entity -> {
                    if (((entity instanceof FallingBlock))) {
                        player.damage(player.getHealth());
                    }
                })), 2, 2);
        }

        @Override
        public void onUpdate() {
            List<Location> usedLocations = new ArrayList<>();
            ThreadLocalRandom random = ThreadLocalRandom.current();

            for (int i = 0; i < perSec; i++) {
                usedLocations.add(spawnLocations.get(random.nextInt(spawnLocations.size())));
            }

            for (Location location : usedLocations) {
                schedule(() -> {
                    FallingBlock block = location.getWorld().spawnFallingBlock(location, anvilData);
                    block.setDropItem(false);
                }, random.nextInt(30)); // Random time bound to create cool effect and prevent
            }

            game.getBukkitPlayers().forEach(player ->
                    player.sendActionBar(Component.text(perSec + " anvils/sec", NamedTextColor.AQUA)));

            perSec++;
        }

    }

}
