package fun.minarty.partygames.listener;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.api.model.profile.GameStatistic;
import fun.minarty.partygames.event.PlayerKilledEvent;
import fun.minarty.partygames.event.QuakeHitEvent;
import fun.minarty.partygames.event.QuakeShootEvent;
import fun.minarty.partygames.game.MineFieldGame;
import fun.minarty.partygames.manager.StatisticManager;
import fun.minarty.partygames.util.MiscUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.projectiles.ProjectileSource;

/**
 * Listener responsible for recording player stats
 */
public class StatisticListener implements Listener {

    private final StatisticManager statisticManager;

    public StatisticListener(PartyGames plugin) {
        this.statisticManager = plugin.getStatisticManager();
    }

    @EventHandler
    public void onPlayerKilled(PlayerKilledEvent event) {
        statisticManager.incrementStatistic(event.getKiller(), GameStatistic.KILLS);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        Player playerDamager = MiscUtil.getPlayerDamager(event);
        if (playerDamager == null)
            return;

        if (entity instanceof Projectile) {
            statisticManager.incrementStatistic(playerDamager, GameStatistic.PROJECTILE_HITS);
        } else {
            statisticManager.incrementStatistic(playerDamager, GameStatistic.MELEE_HITS);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        if(event.isCancelled())
            return;

        statisticManager.incrementStatistic(event.getPlayer(), GameStatistic.BLOCKS_BROKEN);
    }

    @EventHandler
    public void onQuakeHit(QuakeHitEvent event){
        statisticManager.incrementStatistic(event.getPlayer(), GameStatistic.PISTOL_HITS);
    }

    @EventHandler
    public void onQuakeShoot(QuakeShootEvent event){
        statisticManager.incrementStatistic(event.getPlayer(), GameStatistic.PISTOL_SHOTS);
    }

    @EventHandler
    public void onMineDetonate(MineFieldGame.MineDetonateEvent event){
        statisticManager.incrementStatistic(event.getPlayer(), GameStatistic.MINES_DETONATED);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockPlaceEvent event) {
        if(event.isCancelled())
            return;

        statisticManager.incrementStatistic(event.getPlayer(), GameStatistic.BLOCKS_PLACED);
    }

    // TODO maybe all should check cancelled first?

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        Projectile entity = event.getEntity();
        ProjectileSource shooter = entity.getShooter();
        if (shooter instanceof Player) {
            Player player = (Player) shooter;
            statisticManager.incrementStatistic(player, GameStatistic.PROJECTILE_SHOTS);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        statisticManager.incrementStatistic(event.getEntity(), GameStatistic.DEATHS);
    }

}