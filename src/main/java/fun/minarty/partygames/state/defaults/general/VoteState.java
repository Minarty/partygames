package fun.minarty.partygames.state.defaults.general;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.api.model.game.GameType;
import fun.minarty.partygames.state.GeneralState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/**
 * State in which players are allowed to vote
 */
public class VoteState extends GeneralState {

    public VoteState(PartyGames plugin) {
        super(plugin);
    }

    @NotNull
    @Override
    public Duration getDuration() {
        return Duration.ofSeconds(10);
    }

    @Override
    protected void onEnd() {
        GameType gameType = plugin.getVoteManager().endVote();

        if (plugin.getPlayersNeeded() > 0) {
            plugin.getGameManager().addPreGameStates();
        } else {
            Bukkit.getOnlinePlayers().forEach(player -> plugin.setHotbarMode(player, false));
            plugin.getGameManager().constructGame(gameType, plugin);
        }
    }

    @Override
    protected void onStart() {
        plugin.getVoteManager().startVote();
        for (Player player : Bukkit.getOnlinePlayers()) {
            plugin.setHotbarMode(player, true);
        }
    }

    @Override
    public void onUpdate() {
        Bukkit.getOnlinePlayers().forEach(player
                -> player.sendActionBar(Component.translatable("vote.actionbar", NamedTextColor.AQUA,
                    Component.translatable("item.minecraft.nether_star", NamedTextColor.DARK_AQUA))));
    }

}
