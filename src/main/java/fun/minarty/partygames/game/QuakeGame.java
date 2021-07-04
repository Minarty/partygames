package fun.minarty.partygames.game;

import fun.minarty.partygames.api.model.config.GameConfig;
import fun.minarty.partygames.api.model.game.GameType;
import fun.minarty.partygames.event.QuakeHitEvent;
import fun.minarty.partygames.event.QuakeShootEvent;
import fun.minarty.partygames.model.game.GamePlayer;
import fun.minarty.partygames.model.game.PartyGame;
import fun.minarty.partygames.state.StateListen;
import fun.minarty.partygames.state.defaults.FullPlayingState;
import fun.minarty.partygames.PartyGames;
import net.minikloon.fsmgasm.State;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;

public class QuakeGame extends PartyGame {

    public QuakeGame(World world, GameConfig config, GameType type, List<GamePlayer> players, PartyGames plugin) {
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

        private static final int MAX_EXP = 1;
        private static final int MAX_RAY_DISTANCE = 60;

        public PlayingState(PartyGame game, PartyGames plugin) {
            super(game, plugin);
        }

        @Override
        protected void onStart() {
            game.getBukkitPlayers().forEach(player -> player.setExp(1));
        }

        @StateListen
        public void onPlayerInteract(PlayerInteractEvent event){
            Player player = event.getPlayer();
            if(event.getHand() != EquipmentSlot.HAND || player.getExp() != 1)
                return;

            if(event.getAction() != Action.RIGHT_CLICK_BLOCK
                    && event.getAction() != Action.RIGHT_CLICK_AIR){
                return;
            }

            ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
            if(itemInMainHand.getType() != Material.DIAMOND_HOE)
                return;

            player.setExp(0);
            spawnRay(player);

            refillPower(player);
        }

        @StateListen
        public void onPlayerRespawn(PlayerRespawnEvent event){
            schedule(() -> event.getPlayer().setExp(1), 1L);
        }

        @StateListen
        public void onQuakeHit(QuakeHitEvent event){
            Player player = event.getPlayer();
            Player hit = event.getHit();

            hit.setHealth(0);

            GamePlayer gamePlayer = plugin.getPlayerManager().getGamePlayerByPlayer(player);
            gamePlayer.addPoints(1);
        }

        private void refillPower(Player player){
            scheduleRepeating(new Runnable() {
                float i = 0;
                boolean done = false;

                @Override
                public void run() {
                    if(done)
                        return;

                    if(i >= MAX_EXP)
                        done = true;

                    i+= 0.05;
                    player.setExp(i > MAX_EXP ? MAX_EXP : i);
                }
            }, 0, 1);
        }

        private void spawnRay(Player player){
            Vector direction = player.getEyeLocation().getDirection();
            System.out.println("CALLING");
            Bukkit.getPluginManager().callEvent(new QuakeShootEvent(game, player));

            RayTraceResult rayTraceResult = player.getWorld().rayTrace(player.getEyeLocation(),
                    direction, MAX_RAY_DISTANCE, FluidCollisionMode.NEVER, false, 0.25,
                    (p -> p instanceof Player && !p.equals(player)));

            if(rayTraceResult == null)
                return;

            Location location = player.getEyeLocation();
            Entity hitEntity = rayTraceResult.getHitEntity();
            if(hitEntity != null){
                LivingEntity l = (LivingEntity) hitEntity;
                if(l instanceof Player) {
                    Player hitPlayer = (Player) l;
                    if (hitPlayer.getHealth() != 0) {
                        Bukkit.getPluginManager().callEvent(new QuakeHitEvent(game, player, hitPlayer));
                    }
                }
            }

            Block hit = rayTraceResult.getHitBlock();
            for (int i = 0; i < MAX_RAY_DISTANCE; i++) { // Display particle and play sound at each block in the ray
                location = location.add(direction);

                World world = location.getWorld();
                world.playSound(location, Sound.ENTITY_SNOWBALL_THROW, 1.0f, 1.0f);
                world.spawnParticle(Particle.REDSTONE, location,
                        1,0.0,0.0,0.0, new Particle.DustOptions(Color.WHITE, 2));

                if(hit != null && hit.equals(location.getBlock()))
                    break;
            }
        }

    }

}
