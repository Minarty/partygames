package fun.minarty.partygames.game;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.api.model.config.GameConfig;
import fun.minarty.partygames.api.model.game.GameType;
import fun.minarty.partygames.model.game.GamePlayer;
import fun.minarty.partygames.model.game.PartyGame;
import fun.minarty.partygames.state.defaults.FullPlayingState;
import net.minikloon.fsmgasm.State;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class HotFloorGame extends PartyGame {

    public HotFloorGame(World world, GameConfig config,
                        GameType type, List<GamePlayer> players, PartyGames plugin) {
        super(world, config, type, players, plugin);
    }

    @Override
    public Set<State> getPreGameStates() {
        return Collections.emptySet();
    }

    @Override
    public Set<State> getPlayingStates() {
        return Set.of(new PlayingState(this, plugin));
    }

    public static class PlayingState extends FullPlayingState {

        public static final Material[] CHARGE_ORDER = new Material[]{Material.BIRCH_PLANKS, Material.OAK_PLANKS,
                Material.SPRUCE_PLANKS, Material.DARK_OAK_PLANKS, Material.CRIMSON_PLANKS};

        public PlayingState(PartyGame game, PartyGames plugin) {
            super(game, plugin);
        }

        @Override
        protected void onStart() {
            scheduleRepeating(() -> game.getActivePlayers().forEach(gamePlayer -> {
                checkPlayer(gamePlayer.getBukkitPlayer());
                Block down = getBlockUnder(gamePlayer.getBukkitPlayer());

                Material next = getNextCharge(down.getType());
                if(next != null)
                    down.setType(next);
            }), 6, 6);
        }

        private void checkPlayer(@NotNull Player player){
            Material finalType = CHARGE_ORDER[CHARGE_ORDER.length - 1];
            if(getBlockUnder(player).getType() == finalType){
                if(player.getHealth() != 0)
                    player.setHealth(0);
            }
        }

        @NotNull
        private Block getBlockUnder(@NotNull Player player) {
            return player.getLocation().getBlock()
                    .getRelative(BlockFace.DOWN);
        }

        @Nullable
        private Material getNextCharge(@NotNull Material on){
            int max = CHARGE_ORDER.length;
            for (int i = 0; i < max; i++) {
                if(on == CHARGE_ORDER[i]){
                    int index = (i < max-1) ? (i + 1) : (max-1); // Make sure we always stay in bounds
                    return CHARGE_ORDER[index];
                }
            }
            return null;
        }

    }


}