package fun.minarty.partygames.game;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.api.model.config.GameConfig;
import fun.minarty.partygames.api.model.game.GameType;
import fun.minarty.partygames.event.GamePlayerEvent;
import fun.minarty.partygames.game.config.MinefieldConfig;
import fun.minarty.partygames.model.game.GamePlayer;
import fun.minarty.partygames.model.game.PartyGame;
import fun.minarty.partygames.state.StateListen;
import fun.minarty.partygames.state.defaults.CustomState;
import fun.minarty.partygames.state.defaults.FullPlayingState;
import fun.minarty.partygames.util.Cuboid;
import fun.minarty.partygames.util.RandomUtil;
import fun.minarty.partygames.util.cooldown.Cooldowner;
import net.minikloon.fsmgasm.State;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

public class MineFieldGame extends PartyGame {

    public MineFieldGame(World world, GameConfig config, GameType type, List<GamePlayer> players, PartyGames plugin) {
        super(world, config, type, players, plugin);
    }

    @Override
    public Set<State> getPreGameStates() {
        return Set.of(new MinefieldPopulateState(this, plugin, ((MinefieldConfig) getConfig()).getMinefieldArea()));
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
        public void onEntityDamage(EntityDamageEvent event){
            if(event.getCause() == EntityDamageEvent.DamageCause.FALL){
                event.setDamage(event.getDamage() * 2.5);
                event.setCancelled(false);
            }
        }

        @StateListen
        public void onMineDetonate(MineDetonateEvent event){
            Player player = event.getPlayer();
            Vector velocity = player.getVelocity();
            velocity.setY(0.5);

            scheduleRepeating(new Runnable() {
                private int f = 0;

                @Override
                public void run() {
                    if(f == 10){
                        return;
                    }

                    player.setVelocity(velocity);
                    f++;
                }
            }, 0, 1);

            player.setVelocity(velocity);
            player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 20 * 5, 0));

            World world = player.getWorld();
            world.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
            world.spawnParticle(Particle.EXPLOSION_LARGE, player.getLocation(), 1);
        }

        @StateListen
        public void onPlayerInteract(PlayerInteractEvent event){
            Player player = event.getPlayer();
            Cooldowner cooldowner = plugin.getCooldowner();
            if(event.getAction() == Action.PHYSICAL && !cooldowner.hasCooldown(player.getUniqueId())){
                MineDetonateEvent detonateEvent = new MineDetonateEvent(game, player);
                Bukkit.getServer().getPluginManager().callEvent(detonateEvent);

                cooldowner.cooldown(player.getUniqueId(), 100, ChronoUnit.MILLIS);
            }
        }
    }

    public static class MineDetonateEvent extends GamePlayerEvent {

        private static final HandlerList handlers = new HandlerList();

        public MineDetonateEvent(@NotNull PartyGame game, @NotNull Player who) {
            super(game, who);
        }

        @Override
        public @NotNull HandlerList getHandlers() {
            return handlers;
        }

        @SuppressWarnings("unused") @NotNull
        public static HandlerList getHandlerList() {
            return handlers;
        }

    }

    public static class MinefieldPopulateState extends CustomState {

        private final Cuboid minefieldArea;

        public MinefieldPopulateState(PartyGame game, PartyGames plugin,
                                      Cuboid minefieldArea) {

            super(game, plugin);
            this.minefieldArea = minefieldArea;
        }

        @NotNull
        @Override
        public Duration getDuration() {
            return Duration.ZERO;
        }

        @Override
        protected void onStart() {
            minefieldArea.getBlocks().stream()
                    .filter(block -> block.getType() == Material.AIR && RandomUtil.chance(0.5))
                    .forEach(block -> block.setType(Material.OAK_PRESSURE_PLATE));
        }

    }

}