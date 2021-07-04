package fun.minarty.partygames.command;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.api.model.game.GameType;
import fun.minarty.partygames.menu.VoteMenu;
import fun.minarty.partygames.model.game.GamePlayer;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.Component.translatable;

/**
 * Command to vote for the next game
 */
public class GameVoteCommand implements CommandExecutor {

    private final PartyGames plugin;

    public GameVoteCommand(PartyGames plugin) {
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

        if (!plugin.getVoteManager().hasVote()) {
            player.sendMessage(translatable("vote.not_active", NamedTextColor.RED));
            return true;
        }

        GamePlayer gamePlayer = plugin.getPlayerManager().getGamePlayerByPlayer(player);
        if(gamePlayer == null || !gamePlayer.isReady()){
            player.sendMessage(translatable("generic.not_ready", NamedTextColor.RED));
            return true;
        }

        if(args.length != 0 && args[0].equals("force")
                && sender.hasPermission("partygames.force_vote")){

            GameType type = GameType.valueOf(args[1]);

            plugin.getVoteManager().getVote().setForced(type);

            sender.sendMessage(ChatColor.AQUA + "Forcefully set next game to " + ChatColor.DARK_AQUA + type.name());
            return true;
        }

        VoteMenu voteMenu = new VoteMenu(plugin, player);

        voteMenu.open(plugin.getMenuManager());
        return true;
    }

}