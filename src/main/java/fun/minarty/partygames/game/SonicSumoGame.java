package fun.minarty.partygames.game;

import fun.minarty.partygames.api.model.config.GameConfig;
import fun.minarty.partygames.api.model.game.GameType;
import fun.minarty.partygames.model.game.PartyGame;
import fun.minarty.partygames.state.StateListen;
import fun.minarty.partygames.state.defaults.FullPlayingState;
import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.event.PlayerKilledEvent;
import fun.minarty.partygames.model.game.GamePlayer;
import net.minikloon.fsmgasm.State;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class SonicSumoGame extends PartyGame {

    public SonicSumoGame(World world, GameConfig config,
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

        private static final int MAX_EFFECT_AMPLIFIER = 4;

        public PlayingState(PartyGame game, PartyGames plugin) {
            super(game, plugin);
        }

        @StateListen
        public void onPlayerKilled(PlayerKilledEvent event){
            Player killer = event.getKiller();
            int amplifier = -1;

            PotionEffect activeEffect = killer.getPotionEffect(PotionEffectType.SPEED);
            if(activeEffect != null) {
                amplifier = activeEffect.getAmplifier();
                if(amplifier >= MAX_EFFECT_AMPLIFIER)
                    return;
            }

            killer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, amplifier + 1));
        }

    }

}
