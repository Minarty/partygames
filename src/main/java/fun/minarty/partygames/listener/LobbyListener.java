package fun.minarty.partygames.listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

import java.util.Set;

/**
 * Handles events called when the player is in the lobby
 */
public class LobbyListener implements Listener {

    private static final Set<EntityType> ALLOWED_ENTITY_TYPES = Set.of(EntityType.AXOLOTL);

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event){
        if(isInLobby(event.getEntity()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event){
        Entity entity = event.getEntity();
        if(isInLobby(event.getEntity()) && !ALLOWED_ENTITY_TYPES.contains(entity.getType()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event){
        if(event.getEntity() instanceof Player
                || !(event.getEntity() instanceof LivingEntity)) {
            if (isInLobby(event.getEntity()))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event){
        if(isInLobby(event.getPlayer()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event){
        if(isInLobby(event.getEntity()))
            event.setCancelled(true);
    }

    private boolean isInLobby(Entity entity){

        return entity.getWorld().getName().equals("lobby");
    }

}