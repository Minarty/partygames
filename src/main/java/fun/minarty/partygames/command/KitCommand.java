package fun.minarty.partygames.command;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.api.model.kit.Kit;
import fun.minarty.partygames.manager.KitManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Admin command to create kits from inventory and apply them
 */
public class KitCommand implements CommandExecutor {

    private final PartyGames plugin;

    public KitCommand(PartyGames plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String s, @NotNull String[] args) {

        if(!(sender instanceof Player))
            return true;

        if(!sender.hasPermission("partygames.kit"))
            return true;

        if(args.length == 0)
            return true;

        Player player = (Player)sender;
        KitManager kitManager = plugin.getKitManager();
        switch (args[0].toLowerCase()){
            case "save":{
                if(args.length != 2)
                    return true;

                String name = args[1];

                if(kitManager.getKitByName(name) != null){
                    player.sendMessage("There already exists a kit with that name!");
                    return true;
                }

                kitManager.save(args[1], player.getInventory());
                player.sendMessage("Saved kit " + args[1]);

                break;
            }

            case "load":{
                if(args.length != 2)
                    return true;

                Kit kit = kitManager.getKitByName(args[1]);
                if(kit == null) {
                    sender.sendMessage("Unknown kit.");
                    return true;
                }

                kit.apply(player);
                player.sendMessage("Applied " + args[1]);
                break;
            }

            case "delete":{

                String name = args[1];

                Kit kit = kitManager.getKitByName(name);
                if(kit == null) {
                    sender.sendMessage("Unknown kit.");
                    return true;
                }

                kitManager.delete(kit);
                sender.sendMessage("Successfully deleted kit" + name);
                break;
            }
        }

        return true;
    }

}