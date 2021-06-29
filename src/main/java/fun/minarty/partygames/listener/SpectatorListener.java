package fun.minarty.partygames.listener;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.model.game.GamePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Handles events called by game spectators
 */
public class SpectatorListener implements Listener {

    private final PartyGames plugin;

    public SpectatorListener(PartyGames plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event){
        if(isSpectator(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        if(isSpectator(event.getPlayer()))
            event.setCancelled(true);
    }

    private boolean isSpectator(Entity entity){
        if(entity instanceof Player){
            Player player = (Player) entity;
            GamePlayer gamePlayer = plugin.getPlayerManager().getGamePlayerByPlayer(player);
            if(gamePlayer != null)
                return gamePlayer.getState() == GamePlayer.State.SPECTATOR;
        }

        return false;
    }

}