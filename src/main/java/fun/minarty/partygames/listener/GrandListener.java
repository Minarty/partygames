package fun.minarty.partygames.listener;

import fun.minarty.api.user.User;
import fun.minarty.grand.Grand;
import fun.minarty.grand.event.UserUpdateEvent;
import fun.minarty.partygames.PartyGames;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class GrandListener implements Listener {

    private final PartyGames plugin;
    private final Grand grand;

    public GrandListener(PartyGames plugin, Grand grand){
        this.plugin = plugin;
        this.grand  = grand;
    }

    @EventHandler
    public void onUserUpdate(UserUpdateEvent event){

        User user = event.getUser();
        Player player = Bukkit.getPlayer(user.getId());

        if(player == null)
            return;

        plugin.getScoreboardManager().updateEntry(player, "coins",
                grand.getTextFormatter().formatBalance(user.getBalance()));

        plugin.getScoreboardManager().updateEntry(player, "tickets",
                grand.getTextFormatter().formatTickets(user.getTickets()));

    }

}