package fun.minarty.partygames.game;

import cc.pollo.gladeus.item.StackBuilder;
import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.api.model.config.GameConfig;
import fun.minarty.partygames.api.model.game.GameType;
import fun.minarty.partygames.api.model.profile.GameStatistic;
import fun.minarty.partygames.game.config.WipeOutConfig;
import fun.minarty.partygames.model.game.PartyGame;
import fun.minarty.partygames.model.game.GamePlayer;
import fun.minarty.partygames.state.StateListen;
import fun.minarty.partygames.state.defaults.CustomState;
import fun.minarty.partygames.state.defaults.FullPlayingState;
import fun.minarty.partygames.util.Brackets;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.minikloon.fsmgasm.State;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class WipeOutGame extends PartyGame {

    private static final String POWER_UP_METADATA = "PartyGames_PowerUp";
    private static final String ITEM_SHUFFLE_DATA = "crItemShuffle";
    private static final String ITEM_DATA = "crItem";

    private final List<Entity> boxEntities = new ArrayList<>();

    public WipeOutGame(World world,
                       GameConfig config,
                       GameType type,
                       List<GamePlayer> players,
                       PartyGames plugin) {

        super(world, config, type, players, plugin);
    }

    @Override
    public Set<State> getPreGameStates() {
        return Set.of(new PowerUpSpawnState(this, plugin,
                getConfig(WipeOutConfig.class).getPowerUps(), boxEntities));
    }

    @Override
    public Set<State> getPlayingStates() {
        return Set.of(new PlayingState(this, plugin, boxEntities));
    }

    public static class PowerUpSpawnState extends CustomState {

        private final List<Location> powerUpLocations;
        private final List<Entity> boxEntities;

        public PowerUpSpawnState(PartyGame game, PartyGames plugin,
                                 List<Location> locations, List<Entity> boxEntities) {

            super(game, plugin);
            this.powerUpLocations = locations;
            this.boxEntities = boxEntities;
        }

        @NotNull
        @Override
        public Duration getDuration() {
            return Duration.ZERO;
        }

        @Override
        protected void onEnd() {
            powerUpLocations.forEach(location -> {
                ArmorStand stand = game.getWorld().spawn(location, ArmorStand.class);
                stand.setVisible(false);
                stand.setSmall(true);
                stand.setMetadata(POWER_UP_METADATA, new FixedMetadataValue(plugin, null));
                EntityEquipment equipment = stand.getEquipment();
                if (equipment != null)
                    equipment.setHelmet(new ItemStack(Material.GOLD_BLOCK));

                stand.setHeadPose(new EulerAngle(0, 180, 0));
                boxEntities.add(stand);
            });
        }

    }

    public static class PlayingState extends FullPlayingState {

        private final List<Entity> boxEntities;

        public PlayingState(PartyGame game, PartyGames plugin, List<Entity> boxEntities) {
            super(game, plugin);
            this.boxEntities = boxEntities;
        }

        @StateListen
        public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
            Player player = event.getPlayer();
            ItemStack item = player.getInventory().getItemInMainHand();

            GamePlayer gamePlayer = plugin.getPlayerManager().getGamePlayerByPlayer(player);
            if (gamePlayer == null)
                return;

            if (gamePlayer.hasData(ITEM_SHUFFLE_DATA))
                return;

            Entity entity = event.getRightClicked();
            if (player.equals(entity) || (!(entity instanceof Player)))
                return;

            if (item.getType() != Material.INK_SAC && item.getType() != Material.SUGAR)
                return;

            Player target = (Player) entity;
            applyEffect(player, target, item.getType());
            consumeItem(player);
        }

        @StateListen
        public void onPlayerInteract(PlayerInteractEvent event) {
            event.setCancelled(false);
            Player player = event.getPlayer();

            GamePlayer gamePlayer = plugin.getPlayerManager().getGamePlayerByPlayer(player);
            if (gamePlayer.hasData(ITEM_SHUFFLE_DATA))
                return;

            ItemStack item = event.getItem();
            if (item == null)
                return;

            if (item.getType() != Material.INK_SAC && item.getType() != Material.SUGAR)
                return;

            applyEffect(player, player, item.getType());
            consumeItem(player);
        }

        @StateListen
        public void onBlockPlace(BlockPlaceEvent event) {
            Player player = event.getPlayer();
            GamePlayer gamePlayer = plugin.getPlayerManager().getGamePlayerByPlayer(player);

            if (gamePlayer.hasData(ITEM_SHUFFLE_DATA)) {
                event.setCancelled(true);
                return;
            }

            event.setCancelled(false);

            if (event.getBlock().getType() == Material.COBWEB) {
                gamePlayer.clearData(ITEM_DATA);
                schedule(() -> event.getBlock().setType(Material.AIR), 20L * (PowerUpItem.COBWEB.getDuration()));
            }
        }

        @StateListen
        public void onPlayerDeath(PlayerDeathEvent event) {
            GamePlayer gamePlayer = plugin.getPlayerManager().getGamePlayerByPlayer(event.getEntity());
            if (gamePlayer == null)
                return;

            gamePlayer.clearData(ITEM_DATA);
        }

        @StateListen
        public void onPlayerMove(PlayerMoveEvent event) {
            Player player = event.getPlayer();

            GamePlayer gamePlayer = plugin.getPlayerManager().getGamePlayerByPlayer(player);
            if (gamePlayer == null)
                return;

            if (gamePlayer.getData(ITEM_DATA) != null) // Player already has a power up item, we don't need to continue
                return;

            Entity powerUp = player.getNearbyEntities(0.5, 0.5, 0.5).stream()
                    .filter(entity -> entity.hasMetadata(POWER_UP_METADATA))
                    .findFirst()
                    .orElse(null);

            if (powerUp == null)
                return;

            powerUp.remove();

            plugin.getStatisticManager().incrementStatistic(player, GameStatistic.POWERUPS_PICKED_UP);

            gamePlayer.setData(ITEM_DATA, true);
            gamePlayer.setData(ITEM_SHUFFLE_DATA, true);

            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 1.0f, 1.0f);

            int heldItemSlot = player.getInventory().getHeldItemSlot(); // Store this since player can switch slot during the animation
            new BukkitRunnable() {

                int i = 10;

                @Override
                public void run() {
                    if (i == 0) {
                        cancel();
                        gamePlayer.clearData(ITEM_SHUFFLE_DATA);
                    }

                    if (gamePlayer.hasData(ITEM_DATA)) {
                        player.getInventory().setItem(heldItemSlot,
                                generateStackFromPowerUp(player, PowerUpItem.getRandom()));
                    }

                    i--;
                }
            }.runTaskTimer(plugin, 0, 4);
        }

        @Override
        protected void onStart() {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (game.isEnded()) {
                        cancel();
                        return;
                    }

                    boxEntities.forEach(entity -> {
                        ArmorStand s = (ArmorStand) entity;
                        s.setHeadPose(new EulerAngle(0, s.getHeadPose().getY() + 0.3, 0));
                    });
                }
            }.runTaskTimer(plugin, 0, 1);
        }

        private void consumeItem(Player player) {
            GamePlayer gamePlayer = plugin.getPlayerManager().getGamePlayerByPlayer(player);
            if (gamePlayer == null)
                return;

            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            gamePlayer.clearData(ITEM_DATA);
        }

        private ItemStack generateStackFromPowerUp(Player player, PowerUpItem item) {

            Component actionComponent = Component.translatable("games.wipeout.items.action."
                    + item.getAction().name().toLowerCase(Locale.ROOT));

            Component durationComponent = item.getDuration() > 0 ?
                    Component.space().append(Brackets.brackets(Brackets.Type.SQUARE,
                                    Component.translatable("games.wipeout.items.duration",
                                            Component.text(item.getDuration())), Style.style(NamedTextColor.DARK_AQUA)))
                    : Component.empty();

            Component component = Component.empty()
                    .append(Component.translatable("games.wipeout.items." + item.name().toLowerCase(), NamedTextColor.AQUA))
                    .append(Component.space())
                    .append(Brackets.brackets(Brackets.Type.PARENTHESES, actionComponent, Style.style(NamedTextColor.GRAY)))
                    .append(durationComponent);

            return StackBuilder.localized(plugin.getTexty(), item.getMaterial())
                    .displayName(component)
                    .toStack(player);
        }

        public void applyEffect(Player source, Player target, Material type) {
            boolean buff = false;
            switch (type) {
                case INK_SAC: {
                    // TODO we need a generic buff/debuff item system
                    if(!source.equals(target)){
                        plugin.getStatisticManager().incrementStatistic(source, GameStatistic.DEBUFFS_GIVEN);
                    }
                    target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 10 * 8, 0));
                    break;
                }

                case SUGAR: {
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10 * 8, 0));
                    buff = true;
                    break;
                }
            }

            target.playSound(target.getLocation(), buff ? Sound.BLOCK_NOTE_BLOCK_HARP
                    : Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1.0f, 1.0f);
        }

    }

    public enum PowerUpItem {

        BLINDNESS(Material.INK_SAC, PowerUpAction.INTERACT),
        SPEED(Material.SUGAR, PowerUpAction.INTERACT),
        COBWEB(Material.COBWEB, PowerUpAction.PLACE, 5);

        @Getter
        private final Material material;
        @Getter
        private final PowerUpAction action;
        @Getter
        private final int duration;

        PowerUpItem(Material material, PowerUpAction action) {
            this(material, action, 0);
        }

        PowerUpItem(Material material, PowerUpAction action, int duration) {
            this.material = material;
            this.action = action;
            this.duration = duration;
        }

        public static PowerUpItem getRandom() {
            return values()[ThreadLocalRandom.current().nextInt(values().length)];
        }

        enum PowerUpAction {
            INTERACT,
            PLACE
        }

    }

}