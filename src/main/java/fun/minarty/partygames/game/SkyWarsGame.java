package fun.minarty.partygames.game;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.api.model.config.GameConfig;
import fun.minarty.partygames.api.model.game.GameType;
import fun.minarty.partygames.game.config.SkyWarsConfig;
import fun.minarty.partygames.model.game.PartyGame;
import fun.minarty.partygames.model.game.GamePlayer;
import fun.minarty.partygames.state.GameState;
import fun.minarty.partygames.state.defaults.FullPlayingState;
import fun.minarty.partygames.util.Cuboid;
import fun.minarty.partygames.util.DefaultCuboid;
import net.minikloon.fsmgasm.State;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class SkyWarsGame extends PartyGame {

    public SkyWarsGame(World world, GameConfig config,
                       GameType type, List<GamePlayer> players, PartyGames plugin) {
        super(world, config, type, players, plugin);
    }

    @Override
    public Set<State> getPreGameStates() {
        SkyWarsConfig config = (SkyWarsConfig) getConfig();
        return Set.of(new ChestFillState(this, plugin, config.getIslandChests(), config.getMidChests()));
    }

    @Override
    public Set<State> getPlayingStates() {
        return Set.of(new PlayingState(this, plugin),
                new FallDamageGrace(this, plugin));
    }

    public static class ChestFillState extends GameState {

        // TODO fix horrible chest code

        private final List<Location> islandChests;
        private final List<Location> midChests;

        private static final ItemStack[] IRON_KIT = new ItemStack[]{
            new ItemStack(Material.DIAMOND_SWORD),
            new ItemStack(Material.IRON_HELMET),
            new ItemStack(Material.LEATHER_CHESTPLATE),
            new ItemStack(Material.LEATHER_LEGGINGS),
                new ItemStack(Material.IRON_BOOTS),
                new ItemStack(Material.EGG, 16),
                new ItemStack(Material.OAK_PLANKS, 32),
                new ItemStack(Material.STONE_AXE),
        };

        private static final ItemStack[] DIAMOND_KIT = new ItemStack[]{
                new ItemStack(Material.DIAMOND_CHESTPLATE),
                new ItemStack(Material.DIAMOND_LEGGINGS),
                new ItemStack(Material.IRON_SWORD),
                new ItemStack(Material.GOLDEN_APPLE, 2),
                new ItemStack(Material.SNOWBALL, 16),
                new ItemStack(Material.OAK_PLANKS, 32),
                new ItemStack(Material.IRON_AXE),
        };

        private static final ItemStack[] MID_PEARL_CHEST = new ItemStack[]{
            new ItemStack(Material.ENDER_PEARL, 2),
            new ItemStack(Material.GOLDEN_APPLE, 3)
        };

        private static final ItemStack[] MID_ROD_CHEST = new ItemStack[]{
                new ItemStack(Material.FISHING_ROD),
                new ItemStack(Material.SNOWBALL, 64),
                new ItemStack(Material.GOLDEN_APPLE),
        };

        private static final ItemStack[] MID_BOW_CHEST = new ItemStack[]{
                new ItemStack(Material.ARROW, 32),
                new ItemStack(Material.GOLDEN_APPLE, 4),
                new ItemStack(Material.BOW, 1)
        };

        public ChestFillState(PartyGame game, PartyGames plugin,
                              List<Location> islandChests,
                              List<Location> midChests) {

            super(game, plugin);
            this.islandChests = islandChests;
            this.midChests    = midChests;
        }

        @NotNull
        @Override
        public Duration getDuration() {
            return Duration.ZERO;
        }

        @Override
        protected void onEnd() {
            // TODO clean up this unholy mess
            islandChests.forEach(location -> {
                Block block = location.getBlock();
                if(block.getType() == Material.CHEST) {
                    Chest chest = (Chest) block.getState();
                    boolean iron = ThreadLocalRandom.current().nextInt(100) > 50;
                    if (iron) {
                        for (ItemStack itemStack : IRON_KIT) {
                            chest.getBlockInventory().addItem(itemStack);
                        }
                    } else {
                        for (ItemStack itemStack : DIAMOND_KIT) {
                            chest.getBlockInventory().addItem(itemStack);
                        }
                    }
                }
            });

            midChests.forEach(location -> {
                Block block = location.getBlock();
                if(block.getType() == Material.CHEST) {
                    Chest chest = (Chest) block.getState();
                    int ran = ThreadLocalRandom.current().nextInt(100);
                    if (ran < 100/3) {
                        for (ItemStack itemStack : MID_BOW_CHEST) {
                            chest.getBlockInventory().addItem(itemStack);
                        }
                    } else if(ran < 200 / 3){
                        for (ItemStack itemStack : MID_PEARL_CHEST) {
                            chest.getBlockInventory().addItem(itemStack);
                        }
                    } else {
                        for (ItemStack itemStack : MID_ROD_CHEST) {
                            chest.getBlockInventory().addItem(itemStack);
                        }
                    }
                }
            });
        }

        @Override
        protected void onStart() { }

        @Override
        public void onUpdate() { }

    }

    public static class PlayingState extends FullPlayingState {

        public PlayingState(PartyGame game, PartyGames plugin) {
            super(game, plugin);
        }

        @Override
        protected void onEnd() { }

        @Override
        protected void onStart() {
            // TODO consider moving into config or maybe enum, rule system for protection

            enableEvent(PlayerInteractEvent.class);
            enableEvent(BlockBreakEvent.class);
            enableEvent(BlockPlaceEvent.class);

            enableEvent(PlayerInteractEvent.class);
            enableEvent(PlayerDropItemEvent.class);
            enableEvent(EntityPickupItemEvent.class);
            enableEvent(EntityDamageEvent.class, event
                    -> event.getCause() == EntityDamageEvent.DamageCause.FALL);

            game.getBukkitPlayers().forEach(player -> {
                Cuboid blocks = new DefaultCuboid(player.getLocation().add(1, 3, 1),
                        player.getLocation().subtract(1, 1, 1));

                blocks.fill(Material.AIR);
            });
        }

        @Override
        public void onUpdate() { }

    }

    public static class FallDamageGrace extends GameState {

        public FallDamageGrace(PartyGame game, PartyGames plugin) {
            super(game, plugin);
        }

        @NotNull
        @Override
        public Duration getDuration() {
            return Duration.ofSeconds(5);
        }

        @Override
        protected void onEnd() { }

        @Override
        protected void onStart() {
            listen(EntityDamageEvent.class, event -> {
                if(event.getCause() == EntityDamageEvent.DamageCause.FALL)
                    event.setCancelled(true);
            });
        }

        @Override
        public void onUpdate() { }

    }

}