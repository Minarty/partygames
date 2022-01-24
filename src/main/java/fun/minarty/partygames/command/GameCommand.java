package fun.minarty.partygames.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.manager.GameManager;
import fun.minarty.partygames.model.game.GamePlayer;
import fun.minarty.partygames.state.ScheduledStateSeries;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

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
                ScheduledStateSeries mainState = gameManager.getMainState();
                mainState.setFrozen(!mainState.getFrozen());
                sender.sendMessage("Current game state: " + (mainState.getFrozen() ? "Frozen": "Active"));
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

            case "player":{
                Player player = Bukkit.getPlayer(args[1]);
                if(player == null)
                    return true;

                GamePlayer gamePlayer = plugin.getPlayerManager().getGamePlayerByPlayer(player);
                if(gamePlayer == null)
                    return true;

                if(args.length < 3)
                    return true;

                switch (args[2]){
                    case "debug":{
                        try {
                            sender.sendMessage(new ObjectMapper().writeValueAsString(gamePlayer));
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                    case "setstate":{
                        if(args.length != 4)
                            return true;

                        GamePlayer.State state = GamePlayer.State.valueOf(args[3]);
                        gamePlayer.setState(state);

                        sender.sendMessage("Set state for " + player.getName() + " to " + state.name());
                        break;
                    }
                    case "setready":{
                        if(args.length != 4)
                            return true;

                        boolean value = Boolean.parseBoolean(args[3].toLowerCase(Locale.ROOT));
                        gamePlayer.setReady(value);

                        sender.sendMessage("Set ready for " + player.getName() + " to " + value);
                        break;
                    }
                    case "setpoints":{
                        if(args.length != 4)
                            return true;

                        int points = Integer.parseInt(args[3]);
                        gamePlayer.setPoints(points);
                        sender.sendMessage("Set points for " + player.getName() + " to " + points);
                        break;
                    }
                }

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
