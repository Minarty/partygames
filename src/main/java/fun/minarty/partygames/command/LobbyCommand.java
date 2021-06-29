package fun.minarty.partygames.command;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.model.game.GamePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Command to go to the lobby
 */
public class LobbyCommand implements CommandExecutor {

    private final PartyGames plugin;

    public LobbyCommand(PartyGames plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {

        if(!(sender instanceof Player))
            return true;

        Player player = (Player) sender;

        GamePlayer gamePlayer = plugin.getPlayerManager().getGamePlayerByPlayer(player);
        if(gamePlayer == null)
            return true;

        gamePlayer.setState(GamePlayer.State.STANDBY);

        plugin.sendToLobby(gamePlayer, false,false);
        return true;
    }

}