package fun.minarty.partygames.state.defaults.general;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.state.GeneralState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/**
 * State ran before all games to make sure all
 * conditions required before a game can start is met
 */
public class RequirementCheckState extends GeneralState {

    public RequirementCheckState(PartyGames plugin) {
        super(plugin);
    }

    @NotNull
    @Override
    public Duration getDuration() {
        return Duration.ZERO;
    }

    @Override
    public boolean isReadyToEnd() {
        return plugin.getPlayersNeeded() <= 0;
    }

    @Override
    protected void onEnd() { }

    @Override
    protected void onStart() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            plugin.setHotbarMode(player, false);
        }
    }

    @Override
    public void onUpdate() {
        int needed = plugin.getPlayersNeeded();
        if(needed > 0) {
            Bukkit.getOnlinePlayers().forEach(player
                    -> player.sendActionBar(Component.translatable("pregame.actionbar",
                    NamedTextColor.AQUA,
                    Component.text(String.valueOf(needed)),
                    Component.text(needed > 1 ? "s" : ""))));
        }
    }

}