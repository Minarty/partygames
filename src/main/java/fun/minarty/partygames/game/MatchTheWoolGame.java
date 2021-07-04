package fun.minarty.partygames.game;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.api.model.config.GameConfig;
import fun.minarty.partygames.api.model.game.GameType;
import fun.minarty.partygames.api.model.profile.GameStatistic;
import fun.minarty.partygames.game.config.MatchTheWoolConfig;
import fun.minarty.partygames.model.game.GamePlayer;
import fun.minarty.partygames.model.game.PartyGame;
import fun.minarty.partygames.state.StateListen;
import fun.minarty.partygames.state.defaults.FullPlayingState;
import net.minikloon.fsmgasm.State;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class MatchTheWoolGame extends PartyGame {

    public MatchTheWoolGame(World world, GameConfig config,
                            GameType type, List<GamePlayer> players, PartyGames plugin) {
        super(world, config, type, players, plugin);
    }

    @Override
    public Set<State> getPreGameStates() {
        return Collections.emptySet();
    }

    @Override
    public Set<State> getPlayingStates() {
        List<Block> woolBlocks = ((MatchTheWoolConfig) getConfig()).getWoolArea().getBlocks()
                .stream()
                .filter(block
                        -> block.getType() == Material.WHITE_WOOL)
                .collect(Collectors.toList());

        return Set.of(new PlayingState(this, plugin, woolBlocks));
    }

    public static class PlayingState extends FullPlayingState {

        private final List<Block> woolBlocks;

        public PlayingState(PartyGame game, PartyGames plugin, List<Block> woolBlocks) {
            super(game, plugin);
            this.woolBlocks = woolBlocks;
        }

        @StateListen
        public void onBlockPlace(BlockPlaceEvent event){
            Player player = event.getPlayer();
            Block blockAgainst = event.getBlockAgainst();
            event.setCancelled(true);

            if(blockAgainst.getType() == event.getBlock().getType() && !plugin.getCooldowner().hasCooldown(player.getUniqueId())){
                plugin.getCooldowner().cooldown(player.getUniqueId(), 100, ChronoUnit.MILLIS);
                event.getBlock().setType(Material.AIR);
                blockAgainst.setType(Material.BEDROCK);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 1.0f, 1.0f);

                plugin.getStatisticManager().incrementStatistic(player, GameStatistic.WOOL_PLACED);

                GamePlayer gamePlayer = game.getPlayer(player);
                if(gamePlayer == null)
                    return;

                gamePlayer.addPoints(1);
                giveRandomWool(player);
            }
        }

        @Override
        protected void onStart() {
            updateWool();
            game.getBukkitPlayers().forEach(this::giveRandomWool);

            scheduleRepeating(this::updateWool, 20 * 20,20 * 20);
            enableEvent(PlayerInteractEvent.class);
        }

        private void giveRandomWool(Player player){
            PlayerInventory inventory = player.getInventory();
            inventory.clear();

            Material randomWool = getRandomWool();
            for (int i = 0; i < 9; i++) {
                inventory.setItem(i, new ItemStack(randomWool));
            }
        }

        private Material getRandomWool(){
            DyeColor randomDyeColor = DyeColor.values()
                    [ThreadLocalRandom.current().nextInt(DyeColor.values().length)];

            return Material.valueOf(randomDyeColor.name() + "_WOOL");
        }

        private void updateWool(){
            woolBlocks.forEach(block -> block.setType(getRandomWool()));
            game.getBukkitPlayers().forEach(player ->
                    player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1.0f, 1.0f));
        }

    }

}