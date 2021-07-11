package fun.minarty.partygames.listener;

import cc.pollo.store.mongo.MongoStore;
import fun.minarty.gatus.event.PlayerLocaleReadyEvent;
import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.api.model.profile.Profile;
import fun.minarty.partygames.manager.PlayerManager;
import fun.minarty.partygames.model.game.GamePlayer;
import fun.minarty.partygames.model.profile.DefaultProfile;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.UUID;

/**
 * Handles listeners connected to the player joining/leaving
 */
public class ConnectionListener implements Listener {

    private final PartyGames plugin;

    public ConnectionListener(PartyGames plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event){
        MongoStore<UUID, Profile, DefaultProfile> profileStore = plugin.getStoreProvider().getProfileStore();
        Player player   = event.getPlayer();

        Profile profile = profileStore.get(player.getUniqueId());

        if(profile == null){
            plugin.getLogger().info("Creating new party profile for " + player.getUniqueId());
            profileStore.create(new DefaultProfile(player.getUniqueId(), 0, new HashMap<>()));
        }
    }

    @EventHandler
    public void onLocaleReady(PlayerLocaleReadyEvent event){
        Player player = event.getPlayer();
        GamePlayer gamePlayer = plugin.getPlayerManager().getGamePlayerByPlayer(player);
        plugin.applyLocalizedLobby(gamePlayer);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();

        GamePlayer gamePlayer = plugin.getPlayerManager().cachePlayer(player);
        plugin.sendToLobby(gamePlayer, false, false);

        plugin.getScoreboardManager().updateEntry(player, "players",
                Component.text(String.valueOf(Bukkit.getOnlinePlayers().size())));

        plugin.getCommon().getRedisManager()
                .getNameManager().setName(player.getUniqueId(), player.getName());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        plugin.getGameManager().clearGameFromPlayer(player);

        player.getInventory().clear();
        plugin.getHotbarManager().clearHotbar(player);
        plugin.getScoreboardManager().clear(player);
        PlayerManager playerManager = plugin.getPlayerManager();

        GamePlayer gamePlayer = playerManager.getGamePlayerByPlayer(player);
        if(gamePlayer == null)
            return;

        playerManager.clearPlayer(gamePlayer);

        if(gamePlayer.getGame() == null)
            playerManager.clearCache(player);
    }

}