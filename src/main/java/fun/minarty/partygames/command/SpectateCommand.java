package fun.minarty.partygames.command;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.manager.PartyScoreboardManager;
import fun.minarty.partygames.model.game.PartyGame;
import fun.minarty.partygames.model.game.GamePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Command for players to spectate the game
 */
public class SpectateCommand implements CommandExecutor {

    private final PartyGames plugin;

    public SpectateCommand(PartyGames plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        Player player  = (Player) sender;
        PartyGame game = plugin.getGameManager().getGame();

        if(game == null) {
            player.sendMessage(Component.translatable("spectate.no_game", NamedTextColor.RED));
            return true;
        }

        GamePlayer gamePlayer = plugin.getPlayerManager().getGamePlayerByPlayer(player);
        if(gamePlayer.getState() == GamePlayer.State.SPECTATOR || game.getPlayers().contains(gamePlayer))
            return true;

        game.getPlayers().add(gamePlayer);
        plugin.getScoreboardManager()
                .setScoreboard(gamePlayer, PartyScoreboardManager.Type.GAME);

        gamePlayer.setState(GamePlayer.State.SPECTATOR);
        player.setGameMode(GameMode.SPECTATOR);

        Player randomPlayer = game.getActiveBukkitPlayers().get(0);
        Location location;
        if(randomPlayer != null && randomPlayer.isOnline()){
            location = randomPlayer.getLocation();
        } else {
            List<Location> spawns = game.getConfig().getSpawns();
            location = (spawns != null && !spawns.isEmpty()) ? spawns.get(0) : null;
        }

        if(location == null){
            player.sendMessage(Component.text("Unable to find a suitable location! Please wait for the game to end.",
                    NamedTextColor.RED));
            return true;
        }

        player.teleport(location);
        return true;
    }

}
