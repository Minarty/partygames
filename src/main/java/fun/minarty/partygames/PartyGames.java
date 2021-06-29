package fun.minarty.partygames;

import cc.pollo.gladeus.hotbar.HotbarManager;
import cc.pollo.gladeus.hotbar.model.Hotbar;
import cc.pollo.gladeus.hotbar.model.HotbarItem;
import cc.pollo.gladeus.menu.MenuManager;
import cc.pollo.texty.DefaultTexty;
import cc.pollo.texty.Texty;
import cc.pollo.texty.paper.PaperPlatform;
import cc.pollo.texty.source.resourcebundle.ResourceBundleLocalizationSource;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import fun.minarty.gatus.Gatus;
import fun.minarty.grand.Grand;
import fun.minarty.partygames.api.model.profile.Profile;
import fun.minarty.partygames.command.*;
import fun.minarty.partygames.hotbar.LobbyHotbar;
import fun.minarty.partygames.listener.*;
import fun.minarty.partygames.manager.*;
import fun.minarty.partygames.manager.event.GameEventManager;
import fun.minarty.partygames.model.game.GamePlayer;
import fun.minarty.partygames.store.ProfileManager;
import fun.minarty.partygames.store.StoreProvider;
import fun.minarty.partygames.util.LoadedSchematics;
import fun.minarty.partygames.util.cooldown.Cooldowner;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

/**
 * Main class of the plugin, responsible for creating most
 * instances and providing them when needed
 */
@Getter
public final class PartyGames extends JavaPlugin {

    private final PlayerManager playerManager;
    private final GameManager gameManager;
    private final VoteManager voteManager;
    private final MapManager mapManager;
    private final KitManager kitManager;
    private final PvPTagManager pvpTagManager;
    private final LevelManager levelManager;
    private final StatisticManager statisticManager;

    private GameEventManager eventManager;
    private PartyScoreboardManager scoreboardManager;
    private ProfileManager profileManager;

    private MenuManager menuManager;
    private HotbarManager hotbarManager;

    private final StoreProvider storeProvider;
    private Location lobbyLocation;
    private Texty texty;

    private Gatus common;
    private Grand grand;
    private WorldEditPlugin worldEdit;
    private LoadedSchematics loadedSchematics;

    private final Cooldowner cooldowner;

    @Setter
    private int minimumPlayers = 2;

    @Getter
    private static PartyGames instance;

    public PartyGames() {
        instance = this;
        this.playerManager    = new PlayerManager();
        this.levelManager     = new LevelManager(this);
        this.gameManager      = new GameManager(this);
        this.statisticManager = new StatisticManager(this);
        this.storeProvider    = new StoreProvider();
        this.voteManager      = new VoteManager(storeProvider);
        this.mapManager       = new MapManager(getLogger());
        this.kitManager       = new KitManager();
        this.pvpTagManager    = new PvPTagManager();
        this.cooldowner       = Cooldowner.cooldowner();
    }

    @Override
    public void onEnable() {
        eventManager = new GameEventManager(this);

        World lobbyWorld = Bukkit.getWorld("lobby");
        if(lobbyWorld != null){
            lobbyWorld.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
            lobbyWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            lobbyWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            lobbyWorld.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
        }

        getConfig().options().copyDefaults(true);
        saveConfig();

        minimumPlayers = getConfig().getInt("game.min_players");
        kitManager.load(getDataFolder());

        registerCommands();
        registerExternal();
        registerListeners();

        Plugin worldEditPlugin = getServer().getPluginManager().getPlugin("WorldEdit");
        if(worldEditPlugin != null) {
            this.worldEdit = (WorldEditPlugin) worldEditPlugin;
        }

        // TODO move to config
        lobbyLocation = new Location(lobbyWorld,
                20.5, 68, 20.5, -180, -1);

        Bukkit.getOnlinePlayers().forEach(player -> {
            GamePlayer gamePlayer = playerManager.cachePlayer(player);
            scoreboardManager.setScoreboard(gamePlayer, PartyScoreboardManager.Type.LOBBY);
        });

        gameManager.setupStateMachine();
    }

    /**
     * Registers/handles external hooks
     */
    private void registerExternal() {
        common = (Gatus) getServer().getPluginManager().getPlugin("Gatus");

        if (common == null) {
            getLogger().warning("Unable to get Common plugin, functionality will be non-existent.");
            return;
        }

        registerCommonPlaceholders(common);

        grand = (Grand) getServer().getPluginManager().getPlugin("Grand");
        if(grand == null)
            return;

        getServer().getPluginManager()
                .registerEvents(new GrandListener(this, grand), this);
        storeProvider.loadStores(this, common.getMongoManager());

        ResourceBundleLocalizationSource source = ResourceBundleLocalizationSource.builder()
                .withBaseName("PartyGames")
                .withClass(getClass())
                .build();

        source.load(Locale.ENGLISH);
        source.load(Locale.US);
        source.load(new Locale("sv"));

        texty = DefaultTexty.builder()
                .withPlatform(
                        PaperPlatform.builder()
                                .withDefaultLocale(Locale.ENGLISH)
                                .useClientLocale(true)
                                .build())
                .withLocalizationSources(source, source)
                .withMiniMessage(MiniMessage.get())
                .withDefaultPrefix(Component.text("\u25E3", NamedTextColor.AQUA)
                        .append(Component.text("Party", NamedTextColor.DARK_AQUA)
                                .append(Component.text("\u25E2", NamedTextColor.AQUA))))
                .build();

        texty.register();

        loadedSchematics = new LoadedSchematics(this);

        hotbarManager = common.getHotbarManager();
        scoreboardManager = new PartyScoreboardManager(this);
        profileManager = new ProfileManager(this, storeProvider.getProfileStore());
        menuManager = common.getMenuManager();
    }

    /**
     * Registers plugin listeners
     */
    private void registerListeners() {
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new ConnectionListener(this), this);
        pluginManager.registerEvents(new GeneralGameListener(this), this);
        pluginManager.registerEvents(new LobbyListener(), this);
        pluginManager.registerEvents(new SpectatorListener(this), this);
        pluginManager.registerEvents(new ProtectionListener(this), this);
        pluginManager.registerEvents(new StatisticListener(this), this);
    }

    /**
     * Registers plugin commands
     */
    private void registerCommands() {
        registerCommand("lobby", new LobbyCommand(this));
        registerCommand("game", new GameCommand(this));
        registerCommand("gamevote", new GameVoteCommand(this));
        registerCommand("world", new WorldCommand(this));
        registerCommand("kit", new KitCommand(this));
        registerCommand("spectate", new SpectateCommand(this));
        registerCommand("gamecreator", new GameCreatorCommand(this));
    }

    /**
     * Registers placeholders to be shared with other plugin
     *
     * @param common instance of the Common plugin
     */
    private void registerCommonPlaceholders(Gatus common) {
        common.getPlaceholderManager().registerPlaceholder("party_level", audience -> {
            if (audience instanceof Player) {
                Profile profile = storeProvider.getProfileStore().get(((Player) audience).getUniqueId());
                if(profile == null)
                    return "0";

                return String.valueOf(levelManager.getLevelForXp(profile.getXp()));
            }

            return "0";
        });
    }

    /**
     * Registers a bukkit command
     *
     * @param name     name of the command
     * @param executor executor to handle the command
     */
    private void registerCommand(String name, CommandExecutor executor) {
        PluginCommand command = getCommand(name);
        if (command == null || executor == null)
            return;

        command.setExecutor(executor);
    }

    public void transformLobbyPlayer(Player player){
        player.getInventory().clear();
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setExp(0);
        player.setFireTicks(0);
        player.setGameMode(GameMode.ADVENTURE);
        player.getActivePotionEffects()
                .forEach(effect -> player.removePotionEffect(effect.getType()));

        player.teleport(lobbyLocation);
    }

    public void applyLocalizedLobby(GamePlayer gamePlayer){
        Player player = gamePlayer.getBukkitPlayer();
        getScoreboardManager().setScoreboard(gamePlayer, PartyScoreboardManager.Type.LOBBY);

        Hotbar hotbar = new LobbyHotbar(player, texty, this);
        hotbar.apply(hotbarManager);

        HotbarItem item = hotbar.getItemAtSlot(3);
        if(item != null){
            item.setState(gamePlayer.isReady() ? 0 : 1);
            item.apply(player);
        }

        HotbarItem voteItem = hotbar.getItemAtSlot(4);
        if(voteItem != null){
            voteItem.setState(voteManager.hasVote() ? 1 : 0);
            voteItem.apply(player);
        }
    }

    /**
     * Loads the lobby loadout for the player, sets
     * appropriate health, inventory etc
     *
     * @param gamePlayer gameplayer to load loadout for
     * @param postGame   whether or not this is called during post-game state
     */
    public void sendToLobby(GamePlayer gamePlayer, boolean logIn, boolean postGame) {
        Player player = gamePlayer.getBukkitPlayer();
        if (!postGame)
            gameManager.clearGameFromPlayer(player);

        transformLobbyPlayer(player);
        if(!logIn)
            applyLocalizedLobby(gamePlayer);
    }

    /**
     * Gets the amount of players needed for start
     *
     * @return amount of players needed for start
     */
    public int getPlayersNeeded() {
        return minimumPlayers - playerManager.getReadyPlayers().size();
    }

    /**
     * Toggles hotbar mode depending on if a vote is active
     *
     * @param player player to toggle hotbar for
     * @param vote   boolean depending on if a game vote is active
     */
    public void setHotbarMode(Player player, boolean vote) {

        Hotbar hotbar = hotbarManager.getHotbar(player);

        if (hotbar == null)
            return;

        HotbarItem item = hotbar.getItemAtSlot(4);

        if (item == null)
            return;

        item.setState(vote ? 1 : 0);
        item.apply(player);

    }

}