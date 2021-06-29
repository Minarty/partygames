package fun.minarty.partygames.state.feature;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.model.game.PartyGame;
import fun.minarty.partygames.model.game.GamePlayer;
import org.bukkit.entity.Player;

import java.util.Objects;

public class DeadlyBlockFeature extends Feature {

    public DeadlyBlockFeature(PartyGame game, PartyGames plugin) {
        super(game, plugin);
    }

    @Override
    protected void onStart() {
        scheduleRepeating(() -> game.getActivePlayers()
                .stream().map(GamePlayer::getBukkitPlayer)
                .filter(Objects::nonNull)
                .filter(this::isPlayerInDeadlyBlock)
                .forEach(player -> player.setHealth(0)), 5, 5);
    }

    private boolean isPlayerInDeadlyBlock(Player player){
        return game.getConfig().getDeadlyBlocks()
                .stream()
                .anyMatch(m -> player.getLocation().getBlock().getType() == m);
    }


}