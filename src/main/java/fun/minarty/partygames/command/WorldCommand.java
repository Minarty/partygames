package fun.minarty.partygames.command;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.api.model.game.GameType;
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
    private String session;

    public WorldCommand(PartyGames plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command,
                             @NotNull String s, @NotNull String[] args) {

        if(args.length == 0)
            return true;

        switch (args[0]) {
            case "tp", "teleport" -> {
                World world = Bukkit.getWorld(args[1]);
                if (world == null) {
                    if (args.length == 3) {
                        world = new WorldCreator(args[1]).createWorld();
                        if (world == null)
                            return true;
                    } else {
                        return true;
                    }
                }

                if (commandSender instanceof Player player)
                    player.teleport(world.getSpawnLocation());

            }
            case "edit" -> {

                GameType gameType = GameType.valueOf(args[1]);
                String worldName = plugin.getMapManager().getWorldName(gameType);
                World loaded = Bukkit.getWorld(worldName);
                if (loaded != null) {
                    Bukkit.dispatchCommand(commandSender, "world tp " + worldName);
                    return true;
                }

                plugin.getMapManager().loadMap(gameType, slimeWorld -> {
                    World world = plugin.getMapManager().loadSlimeWorld(slimeWorld);
                    if (commandSender instanceof Player player)
                        player.teleport(world.getSpawnLocation());

                    session = worldName;
                }, true);

            }
            case "save" -> {
                if (session == null)
                    return true;

                plugin.getMapManager().cleanupMap(session, true);
                session = null;
            }
        }

        return true;
    }

}
