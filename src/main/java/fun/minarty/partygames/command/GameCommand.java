package fun.minarty.partygames.command;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.manager.GameManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Admin command to handle various game related actions <br>
 * such as skipping game states etc
 */
public class GameCommand implements CommandExecutor {

    private final PartyGames plugin;

    public GameCommand(PartyGames plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if(args.length == 0)
            return true;

        if(!sender.hasPermission("partygames.game")) {
            sender.sendMessage(Component.text("You don't have permission to use this command!", NamedTextColor.RED));
            return true;
        }

        GameManager gameManager = plugin.getGameManager();

        switch (args[0].toLowerCase()){
            case "reloadconfig":{
                plugin.reloadConfig();
                sender.sendMessage("Reloaded config.");
                break;
            }

            case "freeze":{
                sender.sendMessage("Froze current game state.");
                gameManager.getMainState().setFrozen(true);
                break;
            }

            case "skip":{
                sender.sendMessage("Skipped current game state.");
                gameManager.getMainState().skip();
                break;
            }

            case "setminplayers":{
                plugin.setMinimumPlayers(Integer.parseInt(args[1]));
                sender.sendMessage("Set minimum players to " + args[1]);
                break;
            }

            case "reset":
            case "fix":{
                sender.sendMessage("Resetting state machine...");
                gameManager.setupStateMachine();
                break;
            }
        }

        return true;
    }

}