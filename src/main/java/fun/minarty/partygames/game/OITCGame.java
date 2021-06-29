package fun.minarty.partygames.game;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.api.model.config.GameConfig;
import fun.minarty.partygames.api.model.game.GameType;
import fun.minarty.partygames.event.PlayerKilledEvent;
import fun.minarty.partygames.model.game.GamePlayer;
import fun.minarty.partygames.model.game.PartyGame;
import fun.minarty.partygames.state.StateListen;
import fun.minarty.partygames.state.defaults.FullPlayingState;
import net.minikloon.fsmgasm.State;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class OITCGame extends PartyGame {

    public OITCGame(World world, GameConfig config,
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

        public PlayingState(PartyGame game, PartyGames plugin) {
            super(game, plugin);
        }

        @StateListen
        public void onPlayerKilled(PlayerKilledEvent event){
            Player killer = event.getKiller();

            killer.playSound(killer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f,1.0f);
            killer.getInventory().addItem(new ItemStack(Material.ARROW, 1));
            killer.setHealth(20);
        }

        @StateListen
        public void onProjectileHit(ProjectileHitEvent event){
            if(event.getEntity() instanceof Arrow){
                Arrow arrow = (Arrow) event.getEntity();
                arrow.remove(); // Don't drop any arrows
            }
        }

        @StateListen
        public void onEntityDamageByEntity(EntityDamageByEntityEvent event){
            Entity entity = event.getEntity();
            Entity damager = event.getDamager();

            if(entity instanceof Player && damager instanceof Arrow){
                Arrow arrow = (Arrow) damager;
                ProjectileSource source = arrow.getShooter();

                if(source != null && source.equals(entity)) { // Prevent players from killing themselves
                    event.setCancelled(true);
                    return;
                }

                event.setDamage(20);
            }
        }

    }

}