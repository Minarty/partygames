package fun.minarty.partygames.listener;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.model.game.PartyGame;
import fun.minarty.partygames.util.MiscUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleCreateEvent;

/**
 * General listener for protecting the worlds
 */
public class ProtectionListener implements Listener {

    private final PartyGames plugin;

    public ProtectionListener(PartyGames plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player player) {
            PartyGame game = plugin.getGameManager().getGameByPlayer(player);
            if(game == null)
                return;

            if (game.getConfig().isInvincible())
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();

        // TODO test if projectiles work fully
        if (entity instanceof Player) {
            Player playerDamager = MiscUtil.getPlayerDamager(event);

            PartyGame game = plugin.getGameManager().getGameByPlayer((Player) entity);
            if (game != null && playerDamager != null && !game.getConfig().isPvp())
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event){
        if(!shouldOverride(event.getPlayer()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDropItem(PlayerDropItemEvent event) {
        if (!shouldOverride(event.getPlayer()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player &&
                !shouldOverride((Player) event.getEntity()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        handleBlockModifyEvent(event);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        handleBlockModifyEvent(event);
    }

    @EventHandler
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        if (!shouldOverride(event.getPlayer()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        if (!shouldOverride(event.getPlayer()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onVehicleSpawn(VehicleCreateEvent event){
        // Disable this for now.
        if(event.getVehicle().getEntitySpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM)
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if(shouldOverride(player))
            return;

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK
                || event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.PHYSICAL) {

            event.setCancelled(true);
        }
    }

    /**
     * Handles an event where a block was modified.
     * @param event event to handle
     */
    private <T extends BlockEvent & Cancellable> void handleBlockModifyEvent(T event) {
        Player player = null;

        // Gather who modified the block
        if (event instanceof BlockBreakEvent) {
            player = ((BlockBreakEvent) event).getPlayer();
        } else if (event instanceof BlockPlaceEvent) {
            player = ((BlockPlaceEvent) event).getPlayer();
        }

        if(player == null)
            return;

        PartyGame game = plugin.getGameManager().getGameByPlayer(player);
        if(game == null)
            return;

        if(!game.getConfig().isWorldModifiable() || !shouldOverride(player)){
            event.setCancelled(true);
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean shouldOverride(Player player) {
        return player.hasPermission("partygames.override.protection");
    }

}
