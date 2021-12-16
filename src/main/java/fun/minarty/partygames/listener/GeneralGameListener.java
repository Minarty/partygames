package fun.minarty.partygames.listener;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.api.model.game.PlayMode;
import fun.minarty.partygames.api.model.kit.Kit;
import fun.minarty.partygames.event.PlayerKilledEvent;
import fun.minarty.partygames.model.game.GamePlayer;
import fun.minarty.partygames.model.game.PartyGame;
import fun.minarty.partygames.model.game.PvPTag;
import fun.minarty.partygames.util.Cuboid;
import fun.minarty.partygames.util.MiscUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.FluidLevelChangeEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Handles general game events
 */
public class GeneralGameListener implements Listener {

    private static final PotionEffect RESPAWN_ANIMATION_EFFECT = new PotionEffect(PotionEffectType.BLINDNESS, 10, 0);
    private final PartyGames plugin;

    public GeneralGameListener(PartyGames plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDecay(LeavesDecayEvent event){
        event.setCancelled(true);
    }

    @EventHandler
    public void onFluidLevelChange(FluidLevelChangeEvent event){
        event.setCancelled(true);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event){
        event.setFoodLevel(20);
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event){
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event){
        if(event.getCause() == EntityDamageEvent.DamageCause.FALL){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event){
        if(!(event.getEntity() instanceof Player player))
            return;

        Player playerDamager = MiscUtil.getPlayerDamager(event);

        if(playerDamager != null)
            plugin.getPvpTagManager().setTag(player.getUniqueId(), playerDamager.getUniqueId());

    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){

        event.deathMessage(Component.text(""));
        event.getDrops().clear();

        Player player = event.getEntity();
        GamePlayer gamePlayer = plugin.getPlayerManager()
                .getGamePlayerByPlayer(player);

        if(gamePlayer == null)
            return;

        PvPTag tag = plugin.getPvpTagManager().getTagByUniqueId(player.getUniqueId());
        if(tag != null && tag.isValid()) {
            Bukkit.getPluginManager()
                    .callEvent(new PlayerKilledEvent(player, tag.getDamagerPlayer()));
        }

        PartyGame game = gamePlayer.getGame();
        if(game == null)
            return;

        if(game.getConfig().getMode() == PlayMode.PRESENCE){
            if(game.getConfig().isRespawn()){
                plugin.getLogger().warning("Respawn is set for game with PRESENCE mode!");
            }

            plugin.getStatisticManager().updateLongestSurvivingTime(gamePlayer);

            if(game.getActivePlayers().size() <= 3){
                game.getLastActivePlayers().add(gamePlayer);
            }
        }

        if(!game.getConfig().isRespawn()) {
            gamePlayer.setState(GamePlayer.State.SPECTATOR);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event){
        Player player = event.getPlayer();
        GamePlayer gamePlayer = plugin.getPlayerManager().getGamePlayerByPlayer(player);
        if(gamePlayer == null)
            return;

        Location location;

        PartyGame game = gamePlayer.getGame();
        if(game == null) {
            location = plugin.getLobbyLocation();
        } else {
            Cuboid checkpoint = (Cuboid) gamePlayer.getData("checkpoint");

            if (checkpoint != null) {
                location = (checkpoint.getLow());
            } else {
                List<Location> spawns = game.getConfig().getSpawns();
                if(spawns == null || spawns.isEmpty())
                    return;

                location = spawns.get(ThreadLocalRandom.current().nextInt(spawns.size()));
            }
        }

        event.setRespawnLocation(location);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            if(game == null) {
                player.teleport(location);
                return;
            }

            player.setGameMode(gamePlayer.getState() == GamePlayer.State.SPECTATOR
                    ? GameMode.SPECTATOR : game.getConfig().getGameMode());

            player.addPotionEffect(RESPAWN_ANIMATION_EFFECT);
            player.teleport(location);

            Kit kit = game.getConfig().getKit();
            if(kit != null)
                kit.apply(player);
        }, 1);
    }

}
