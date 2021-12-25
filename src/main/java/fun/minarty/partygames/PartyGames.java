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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Locale;

/**
 * Main class of the plugin, entry point for many instances <br>
 * and provides common methods
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

        getConfig().options().copyDefaults(true);
        saveConfig();

        setupLobby();

        minimumPlayers = getConfig().getInt("game.min_players");
        kitManager.load(getDataFolder());

        registerCommands();
        registerExternal();
        registerListeners();

        loadedSchematics = new LoadedSchematics(this);
        loadedSchematics.loadDefault();

        // In case of reload
        Bukkit.getOnlinePlayers().forEach(player -> {
            GamePlayer gamePlayer = playerManager.cachePlayer(player);
            if(player.getName().equals("Essl"))
                gamePlayer.setReady(false);

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

        Plugin worldEditPlugin = getServer().getPluginManager().getPlugin("WorldEdit");
        if(worldEditPlugin != null) {
            this.worldEdit = (WorldEditPlugin) worldEditPlugin;
        }

        grand = (Grand) getServer().getPluginManager().getPlugin("Grand");
        if(grand == null)
            return;

        setupLocalization();

        getServer().getPluginManager()
                .registerEvents(new GrandListener(this, grand), this);

        storeProvider.loadStores(this, common.getMongoManager());

        hotbarManager = common.getHotbarManager();
        scoreboardManager = new PartyScoreboardManager(this);
        profileManager = new ProfileManager(this, storeProvider.getProfileStore());
        menuManager = common.getMenuManager();
    }

    /**
     * Sets important rules in the lobby world and loads the lobby location
     */
    private void setupLobby(){
        World lobbyWorld = Bukkit.getWorld("lobby");
        if(lobbyWorld != null){
            lobbyWorld.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
            lobbyWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            lobbyWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            lobbyWorld.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
        }

        ConfigurationSection lobbySection = getConfig().getConfigurationSection("lobby");
        if(lobbySection == null)
            return;

        double x = lobbySection.getDouble("x");
        double y = lobbySection.getDouble("y");
        double z = lobbySection.getDouble("z");
        float yaw = (float) lobbySection.getDouble("yaw");
        float pitch = (float) lobbySection.getDouble("pitch");

        lobbyLocation = new Location(lobbyWorld,
                x, y, z, yaw, pitch);
    }

    /**
     * Sets up the localization with Texty
     */
    private void setupLocalization(){
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

        Method[] methods = StatisticListener.class.getMethods();
        for (Method method : methods) {
            StatisticListener.StatisticHandler annotation = method.getAnnotation(StatisticListener.StatisticHandler.class);
            if(annotation == null)
                continue;

            Parameter[] parameters = method.getParameters();
            if(parameters.length < 1)
                continue;

            Parameter firstParam = parameters[0];
            Class<?> type = firstParam.getType();

            if(!Event.class.isAssignableFrom(type))
                continue;

            Class<? extends Event> eventClass = type.asSubclass(Event.class);
            pluginManager.registerEvent(eventClass, new StatisticListener(this), EventPriority.MONITOR, (listener, event) -> {
                if(!(event instanceof Cancellable) || !((Cancellable) event).isCancelled()){
                    try {
                        if (!eventClass.isAssignableFrom(event.getClass())) {
                            return;
                        }

                        method.invoke(listener, event);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }, this);
        }
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
            if (audience instanceof Player player) {
                Profile profile = storeProvider.getProfileStore().get(player.getUniqueId());
                if(profile == null)
                    return "0";

                int xp = levelManager.getLevelForXp(profile.getXp());
                return String.valueOf(xp);
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

    /**
     * Sets all the vanilla values for the player and teleports them to the lobby
     * @param player player to transform
     */
    private void transformLobbyPlayer(Player player){
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

    /**
     * Applies the lobby load-out that depends on localized text
     * @param gamePlayer game player to apply to
     */
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
     * Loads the lobby loadout for the player, sets <br>
     * appropriate health, inventory etc
     *
     * @param gamePlayer gameplayer to load loadout for
     * @param logIn whether this player has just logged in
     * @param postGame   whether this is called during post-game state
     */
    public void sendToLobby(GamePlayer gamePlayer, boolean logIn, boolean postGame) {
        Player player = gamePlayer.getBukkitPlayer();
        if (!postGame) // If this is post game,
            gameManager.clearGameFromPlayer(player);

        gamePlayer.setState(GamePlayer.State.STANDBY);

        transformLobbyPlayer(player);
        if(!logIn) // If it's login this will be applied when the client locale is ready, which it isn't directly.
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
