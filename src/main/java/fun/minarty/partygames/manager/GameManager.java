package fun.minarty.partygames.manager;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.api.model.config.GameConfig;
import fun.minarty.partygames.api.model.game.GameType;
import fun.minarty.partygames.game.*;
import fun.minarty.partygames.game.config.*;
import fun.minarty.partygames.model.config.DefaultConfig;
import fun.minarty.partygames.model.game.GameLoaderInfo;
import fun.minarty.partygames.model.game.GamePlayer;
import fun.minarty.partygames.model.game.PartyGame;
import fun.minarty.partygames.state.ScheduledStateSeries;
import fun.minarty.partygames.state.defaults.general.RequirementCheckState;
import fun.minarty.partygames.state.defaults.general.VoteState;
import fun.minarty.partygames.store.ConfigManager;
import lombok.Getter;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles everything related to game loading, holds the <br>
 * main statemachine state that everything runs under
 */
public class GameManager {

    @Getter
    private PartyGame game;
    private final PartyGames plugin;

    @Getter
    private ScheduledStateSeries mainState;

    public static final Map<GameType, GameLoaderInfo> GAME_TYPES = new HashMap<>();

    public GameManager(PartyGames plugin) {
        this.plugin = plugin;

        // Map game types to their respective game class and config if custom
        addGameType(GameType.TRI_DODGE, TriDodgeGame.class, TriDodgeConfig.class);
        addGameType(GameType.BOAT_RACE, BoatRaceGame.class);
        addGameType(GameType.WIPEOUT, WipeOutGame.class, WipeOutConfig.class);
        addGameType(GameType.ELYTRA, ElytraGame.class, ElytraConfig.class);
        addGameType(GameType.ANVIL, AnvilGame.class, AnvilConfig.class);
        addGameType(GameType.MINE_FIELD, MineFieldGame.class, MinefieldConfig.class);
        addGameType(GameType.QUAKE, QuakeGame.class);
        addGameType(GameType.HOT_FLOOR, HotFloorGame.class);
        addGameType(GameType.MATCH_THE_WOOL, MatchTheWoolGame.class, MatchTheWoolConfig.class);
        addGameType(GameType.FLOOD_ESCAPE, FloodEscapeGame.class, FloodEscapeConfig.class);
        addGameType(GameType.SONIC_SUMO, SonicSumoGame.class);
        addGameType(GameType.MINI_SKYWARS, SkyWarsGame.class, SkyWarsConfig.class);
        addGameType(GameType.ANIMAL_SNIPER, AnimalSniperGame.class, AnimalSniperConfig.class);
        addGameType(GameType.MEMORY, MemoryGame.class, MemoryConfig.class);
        addGameType(GameType.QUICK_CLICK, QuickClickGame.class, QuickClickConfig.class);
        addGameType(GameType.OITC, OITCGame.class);
    }

    private void addGameType(GameType type, Class<? extends PartyGame> gameClass){
        GAME_TYPES.put(type, new GameLoaderInfo(gameClass, DefaultConfig.class));
    }

    private void addGameType(GameType type, Class<? extends PartyGame> gameClass,
                             Class<? extends GameConfig> configClass){

        GAME_TYPES.put(type, new GameLoaderInfo(gameClass, configClass));
    }

    /**
     * Setups the state machine responsible for all states
     * and starts its scheduler etc
     */
    public void setupStateMachine(){
        if(mainState != null)
            mainState.end();

        mainState = new ScheduledStateSeries(plugin, 20);
        addPreGameStates();
        mainState.start();
    }

    /**
     * Adds the default pre-game states, meant to run before starting a game
     * to check for various conditions and perform preparatory actions
     */
    public void addPreGameStates(){
        mainState.add(new RequirementCheckState(plugin));
        mainState.add(new VoteState(plugin));
    }

    /**
     * Gets a game by bukkit player
     * @param player bukkit player to check against
     * @return game with specific player or null if not found
     */
    public PartyGame getGameByPlayer(Player player){

        if(game == null)
            return null;

        if(game.getPlayers().stream()
                .anyMatch(gamePlayer -> {
                    if(gamePlayer == null)
                        return false;

                    Player p = gamePlayer.getBukkitPlayer();
                    return p != null && p.equals(player);
                })){

            return game;
        }

        return null;
    }

    /**
     * Clears the player from game
     * @param player player to clear
     */
    public void clearGameFromPlayer(Player player){
        if(game == null)
            return;

        game.getPlayers().removeIf(gamePlayer -> gamePlayer != null
                && gamePlayer.getBukkitPlayer() != null
                && gamePlayer.getBukkitPlayer().equals(player));
    }

    /**
     * Unregisters a game by unregistering its game listener, unloading
     * the map, unloading the game instance etc
     */
    public void unregisterGame(){
        plugin.getEventManager().unregisterGameListener(game);

        List<GamePlayer> players = game.getPlayers();
        players.forEach(gamePlayer -> plugin.getPlayerManager().clearPlayer(gamePlayer));

        players.forEach(p -> {
            Player bukkitPlayer = p.getBukkitPlayer();
            if(bukkitPlayer == null || !bukkitPlayer.isOnline()){
                plugin.getPlayerManager().clearCache(bukkitPlayer);
            }
        });

        plugin.getMapManager().cleanupMap();
        this.game = null;
    }

    /**
     * Registers a game as the current game and register its
     * game listener
     * @param game game instance to register
     */
    public void registerGame(PartyGame game) {
        this.game = game;
        plugin.getEventManager().registerGameListener(game);
    }

    /**
     * Constructs a new game from the GameType via Reflection
     * @param type type to create for
     * @param plugin plugin instance to inject into the game
     */
    public void constructGame(GameType type, PartyGames plugin) {
        final MapManager mapManager = plugin.getMapManager();
        mapManager.loadMap(type.name(), slimeWorld -> {

            PartyGame game;
            try {
                World world = mapManager.loadSlimeWorld(slimeWorld);

                ConfigManager configManager   = plugin.getStoreProvider().getConfigManager();
                GameConfig config             = configManager.load(type);

                if(config == null){
                    plugin.getLogger().warning("No config found for " + type.name() + "!");
                    return;
                }

                List<GamePlayer> readyPlayers = plugin.getPlayerManager().getReadyPlayers();

                Class<? extends PartyGame> gameClass = GameManager.GAME_TYPES.get(type).getGameClass();
                if(gameClass == null){
                    plugin.getLogger().warning("Unable to find game class for " + type.name() + "!");
                    return;
                }

                game = gameClass
                        .getConstructor(World.class, GameConfig.class, GameType.class, List.class, PartyGames.class)
                        .newInstance(world, config, type, readyPlayers, plugin);

                game.setupStates();
                game.getWorld().setDifficulty(game.getConfig().getDifficulty());
            } catch (InstantiationException | IllegalAccessException
                    | InvocationTargetException | NoSuchMethodException e){
                e.printStackTrace();
                return;
            }

            registerGame(game);
            mainState.addNext(game.getStates());
        }, false);
    }

    public boolean shouldGameEnd(){
        return isGameTimeOut() || game.getActivePlayers().size() < plugin.getMinimumPlayers();
    }

    public boolean isGameTimeOut(){
        return mainState.getRemainingSeconds() < 2;
    }

}