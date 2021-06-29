package fun.minarty.partygames.command;

import fun.minarty.partygames.PartyGames;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WorldCommand implements CommandExecutor {

    private final PartyGames plugin;

    public WorldCommand(PartyGames plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command,
                             @NotNull String s, @NotNull String[] args) {

        if(args.length == 0)
            return true;

        switch (args[0]){
            case "tp":
            case "teleport":{
                World world = Bukkit.getWorld(args[1]);
                if(world == null){
                    if(args.length == 3) {
                        world = new WorldCreator(args[1]).createWorld();
                        if(world == null)
                            return true;
                    } else {
                        return true;
                    }
                }

                if(commandSender instanceof Player) {

                    Player player = (Player) commandSender;
                    player.teleport(world.getSpawnLocation());
                }
                break;
            }

            case "edit":{
                plugin.getMapManager().loadMap(args[1], slimeWorld -> {
                    World world   = plugin.getMapManager().loadSlimeWorld(slimeWorld);
                    if(commandSender instanceof Player) {
                        Player player = (Player) commandSender;
                        player.teleport(world.getSpawnLocation());
                    }
                }, true);

                break;
            }
        }

        return true;
    }

}
