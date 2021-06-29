package fun.minarty.partygames.game;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.api.model.config.GameConfig;
import fun.minarty.partygames.api.model.game.GameType;
import fun.minarty.partygames.game.config.QuickClickConfig;
import fun.minarty.partygames.model.game.PartyGame;
import fun.minarty.partygames.model.game.GamePlayer;
import fun.minarty.partygames.state.StateListen;
import fun.minarty.partygames.state.defaults.CustomState;
import fun.minarty.partygames.state.defaults.FullPlayingState;
import fun.minarty.partygames.util.Cuboid;
import lombok.Data;
import net.minikloon.fsmgasm.State;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.FaceAttachable;
import org.bukkit.block.data.type.Switch;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class QuickClickGame extends PartyGame {

    private final Map<UUID, List<Block>> buttons = new HashMap<>();

    public QuickClickGame(World world, GameConfig config, GameType type,
                          List<GamePlayer> players, PartyGames plugin) {

        super(world, config, type, players, plugin);
    }

    @Override
    public Set<State> getPreGameStates() {
        QuickClickConfig config = (QuickClickConfig) getConfig();
        List<ButtonRoom> rooms = config.getRooms();

        return Set.of(new PlayerRoomReadyState(this, plugin, rooms, buttons));
    }

    @Override
    public Set<State> getPlayingStates() {
        return Set.of(new PlayingState(this, plugin, buttons));
    }

    @Data
    public static class ButtonRoom {

        private Location spawn;
        private Cuboid buttonArea;

    }

    public static class PlayerRoomReadyState extends CustomState {

        private final Map<UUID, List<Block>> buttons;
        private final List<ButtonRoom> rooms;

        public PlayerRoomReadyState(PartyGame game, PartyGames plugin,
                                    List<ButtonRoom> rooms,
                                    Map<UUID, List<Block>> buttons) {

            super(game, plugin);
            this.buttons = buttons;
            this.rooms = rooms;
        }

        @NotNull
        @Override
        public Duration getDuration() {
            return Duration.ZERO;
        }

        @Override
        protected void onEnd() {
            List<Player> bukkitPlayers = game.getBukkitPlayers();
            for (int i = 0; i < bukkitPlayers.size(); i++) {
                if (i >= rooms.size())
                    i = 0;

                Player player = bukkitPlayers.get(i);
                ButtonRoom buttonRoom = rooms.get(i);
                buttons.put(player.getUniqueId(), buttonRoom.getButtonArea().getBlocks()
                        .stream()
                        .filter(block -> block.getType() == Material.OAK_BUTTON)
                        .collect(Collectors.toList()));

                player.teleport(buttonRoom.getSpawn());
            }
        }

    }

    public static class PlayingState extends FullPlayingState {

        private final Map<UUID, List<Block>> buttons;
        private final Map<UUID, Location> activeButton = new HashMap<>();

        public PlayingState(PartyGame game, PartyGames plugin,
                            Map<UUID, List<Block>> buttons) {

            super(game, plugin);
            this.buttons = buttons;
        }

        @Override
        protected void onStart() {
            game.getBukkitPlayers().forEach(this::setRandomButton);
        }

        @StateListen
        public void onPlayerInteract(PlayerInteractEvent event) {
            Player player = event.getPlayer();
            Block clickedBlock = event.getClickedBlock();

            if (clickedBlock == null)
                return;

            if (clickedBlock.getType() != Material.OAK_BUTTON)
                return;

            if (!activeButton.containsKey(player.getUniqueId()))
                return;

            GamePlayer gamePlayer = plugin.getPlayerManager().getGamePlayerByPlayer(player);
            if (gamePlayer == null)
                return;

            Location location = activeButton.get(player.getUniqueId());
            if(location == null)
                return;

            if(location.equals(clickedBlock.getLocation()))
                gamePlayer.addPoints(1);

            resetActive(player);
            schedule(() -> setRandomButton(player), 20);
        }

        private void resetActive(Player player) {
            Location location = activeButton.get(player.getUniqueId());
            if (location == null)
                return;

            toggleBlock(getBlockBehindButton(location.getBlock()), false);
            activeButton.remove(player.getUniqueId());
        }

        private void setRandomButton(Player player) {
            List<Block> buttons = this.buttons.get(player.getUniqueId());
            Block block = buttons.get(ThreadLocalRandom.current().nextInt(buttons.size()));
            player.playSound(block.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 1.0f, 1.0f);

            toggleBlock(getBlockBehindButton(block), true);
            activeButton.put(player.getUniqueId(), block.getLocation());
        }

        private Block getBlockBehindButton(Block block) {
            Switch button = (Switch) block.getBlockData();
            FaceAttachable faceAttachable = (FaceAttachable) block.getBlockData();

            BlockFace face = button.getFacing();
            switch (faceAttachable.getAttachedFace()) {
                case FLOOR:
                    face = BlockFace.UP;
                    break;
                case CEILING:
                    face = BlockFace.DOWN;
                    break;
            }

            return block.getRelative(face.getOppositeFace());
        }

        private void toggleBlock(Block block, boolean active) {
            block.setType(active ? Material.GOLD_BLOCK : Material.CHISELED_STONE_BRICKS);
        }

    }

}