package fun.minarty.partygames.model.game;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.api.model.config.Game;
import fun.minarty.partygames.api.model.config.GameConfig;
import fun.minarty.partygames.api.model.game.GameType;
import fun.minarty.partygames.api.model.misc.Door;
import fun.minarty.partygames.api.model.profile.GameStatistic;
import fun.minarty.partygames.event.DoorOpenEvent;
import fun.minarty.partygames.event.PlayerReachedGoalEvent;
import fun.minarty.partygames.manager.StatisticManager;
import fun.minarty.partygames.manager.event.GameEventListener;
import fun.minarty.partygames.state.GameState;
import fun.minarty.partygames.state.StateGroup;
import fun.minarty.partygames.state.defaults.game.*;
import fun.minarty.partygames.state.feature.*;
import fun.minarty.partygames.util.Brackets;
import fun.minarty.partygames.util.MiscUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.minikloon.fsmgasm.State;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

/**
 * Main class which all games must extend, does a lot of condition checking
 * to enable features which are enabled in the config
 */
public abstract class PartyGame extends GameEventListener implements Game, Listener {

    protected PartyGames plugin;

    @Getter
    protected final GameConfig config;

    @Getter
    private final List<GamePlayer> players;
    @Getter
    private final List<State> states = new ArrayList<>();
    @Getter
    private final StateGroup playingStateGroup;

    @Setter
    private long timeStart;

    @Getter
    private final LinkedList<GamePlayer> places = new LinkedList<>();
    @Getter
    private final LinkedList<GamePlayer> lastActivePlayers = new LinkedList<>();

    @Getter
    private final World world;
    @Getter @NonNull
    private final GameType type;

    @Getter
    private boolean ended;

    public PartyGame(World world,
                     GameConfig config,
                     @NotNull GameType type,
                     List<GamePlayer> players,
                     PartyGames plugin) {

        super(plugin);

        this.world = world;
        this.config = config;
        this.type = type;
        this.players = players;
        this.plugin = plugin;

        setGame(this);

        plugin.getPlayerManager().setGame(this);
        registerDefaultListeners();

        playingStateGroup = new StateGroup();
    }

    /**
     * Sets up all states which will then be added to the main state series
     * Also adds the PostGameState for when the game has ended
     */
    public void setupStates() {
        states.addAll(getDefaultPreGameStates());
        playingStateGroup.addAll(getDefaultPlayingStates());
        playingStateGroup.addAll(getPlayingStates());

        states.add(playingStateGroup);
        states.add(new PostGameState(this, plugin));
    }

    /**
     * Gets the default pre-game states for this game, depending on config
     *
     * @return list of pre-game states
     */
    private List<State> getDefaultPreGameStates() {
        List<State> states = new ArrayList<>();

        states.add(new PlayerClearState(this, plugin));
        states.add(new PlayerProfileState(this, plugin));
        addConditionalState(states, new PlayerTeleportationState(this, plugin),
                config.getSpawns() != null && config.getSpawns().size() > 0);

        states.addAll(getPreGameStates());

        addConditionalState(states,
                new CountdownFeature(this, plugin, config.getCountdown()), config.getCountdown() > 0);

        return states;
    }

    /**
     * Gets the default playing states for this game, most of them depending on config
     *
     * @return list of playing states
     */
    private List<State> getDefaultPlayingStates() {
        List<State> states = new ArrayList<>();
        states.add(new DefaultPlayingState(this, plugin));

        addConditionalState(states, new FinishAreaFeature(this, plugin), GameConfig.Conditions.FINISH_AREA);
        addConditionalState(states, new StartWallFeature(this, plugin), GameConfig.Conditions.START_WALL);
        addConditionalState(states, new CheckpointFeature(this, plugin), GameConfig.Conditions.CHECKPOINTS);
        addConditionalState(states, new MaxYLevelFeature(this, plugin), config.getMaxYLevel() != 0);
        addConditionalState(states, new PvPPointsFeature(this, plugin), config.isPvpPoints());

        // TODO condition
        addConditionalState(states, new DeadlyBlockFeature(this, plugin),
                config.getDeadlyBlocks() != null && config.getDeadlyBlocks().size() > 0);

        return states;
    }

    /**
     * Adds a {@link GameState} which should only be added <br>
     * if a certain condition is met (often set in config)
     *
     * @param state     state to add
     * @param condition condition to meet
     */
    private void addConditionalState(List<State> list, GameState state, boolean condition) {
        if (condition)
            list.add(state);
    }

    private void addConditionalState(List<State> list, GameState state, GameConfig.Conditions condition) {
        addConditionalState(list, state, condition.getFilter().test(config));
    }

    /**
     * Registers the default listeners, depending on config
     */
    public void registerDefaultListeners() {
        registerConditionalListener(PlayerInteractEvent.class, event -> {
            if(event.getHand() != EquipmentSlot.HAND)
                return;

            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock == null)
                return;

            if (clickedBlock.getType() != Material.STONE_BUTTON
                    || event.getAction() != Action.RIGHT_CLICK_BLOCK) {

                return;
            }

            Player player = event.getPlayer();

            Door door = config.getDoors().stream()
                    .filter(d -> d.shouldOpen(player, clickedBlock))
                    .findAny()
                    .orElse(null);

            if (door == null)
                return;

            door.open(player);
            Bukkit.getPluginManager()
                    .callEvent(new DoorOpenEvent(player, this, door));

            plugin.getStatisticManager().incrementStatistic(player, GameStatistic.DOORS_OPENED);

        }, GameConfig.Conditions.DOORS.getFilter().test(config));
    }

    /**
     * Method for when the game has ended, does rewarding and output
     */
    public void onEnd() {
        if (ended)
            return;

        ended = true;

        ScoreTableEntry[] scores = getScoreTable();

        announce(Component.empty(), false);

        if(scores[0] == null)
            announce(Component.text("    ", NamedTextColor.AQUA)
             .append(Component.translatable("game.end.no_winner")), false);

        for (int i = 0; i < scores.length; i++) {
            ScoreTableEntry entry = scores[i];
            if (entry != null) {
                // TODO More game specific rewards
                entry.setCoins((int) (entry.getReward() * 8.5));
                announceScoreEntry(entry, i);
            }
        }

        announce(Component.empty(), false);

        Arrays.stream(scores)
                .filter(Objects::nonNull)
                .forEach(s -> plugin.getProfileManager().applyScore(this, s));

        playingStateGroup.end();
    }

    /**
     * Announce a score-table entry
     *
     * @param entry entry from the table
     * @param i     place of the entry
     */
    public void announceScoreEntry(ScoreTableEntry entry, int i) {
        Player player = entry.getPlayer().getBukkitPlayer();
        if (player == null)
            return;

        getPlayers().forEach(gamePlayer -> {
            Player target = gamePlayer.getBukkitPlayer();

            Component winEntry = text("         ")
                    .append(translatable("game.end.win_section.prefix." + i, NamedTextColor.DARK_AQUA))
                    .append(text(" - ", NamedTextColor.GRAY))
                    .append(text(player.getName(), NamedTextColor.AQUA))
                    .append(entry.getPoints() != -1 ?
                            text(" ", NamedTextColor.GRAY)
                                    .append(text("(" + entry.getPoints()))
                                    .append(text(" ")
                                            .append(translatable("points")))
                                    .append(text(")"))
                            : text(""))
                    .append(text(" +", NamedTextColor.GREEN))
                    .append(text(entry.getReward(), NamedTextColor.YELLOW))
                    .append(text("XP", NamedTextColor.GOLD))
                    .append(text(",", NamedTextColor.GRAY))
                    .append(text(" +", NamedTextColor.GREEN))
                    .append(text(entry.getCoins(), NamedTextColor.AQUA))
                    .append(text("\u26C3", NamedTextColor.GOLD)); // TODO use grand currency

            target.sendMessage(winEntry);
        });
    }

    /**
     * Gets the score-table depending on game mode
     *
     * @return Score-table (array of entries)
     */
    public ScoreTableEntry[] getScoreTable() {
        ScoreTableEntry[] scores = new ScoreTableEntry[3];
        switch (config.getMode()) {
            case PRESENCE: {
                lastActivePlayers.addAll(getActivePlayers());
                for (int i = 2; i > -1; i--) {
                    if (lastActivePlayers.size() - 1 >= i) {
                        GamePlayer gamePlayer = lastActivePlayers.get(i);
                        if(gamePlayer == null)
                            continue;

                        int place = lastActivePlayers.size() - 1 - i;
                        scores[place] = ScoreTableEntry.builder()
                                .place(place)
                                .player(gamePlayer)
                                .reward(3 - place)
                                .build();
                    }
                }

                break;
            }

            case FINISH_AREA: {
                for (int i = 0; i < 3 && i < places.size(); i++) {
                    GamePlayer gamePlayer = places.get(i);
                    if(gamePlayer == null)
                        continue;

                    scores[i] = ScoreTableEntry.builder()
                            .place(i)
                            .player(gamePlayer)
                            .reward(3 - i)
                            .build();
                }
                break;
            }

            case POINTS: {
                int i = 0;
                for (Map.Entry<GamePlayer, Integer> entry : getTopPointsPlayersByPoints().entrySet()) {
                    if (i == 3)
                        break;

                    scores[i] = ScoreTableEntry.builder()
                            .place(i)
                            .player(entry.getKey())
                            .reward(3 - i)
                            .points(entry.getValue())
                            .build();

                    i++;
                }
                break;
            }
        }

        return scores;
    }

    /**
     * Handles when a player has reached the finish line
     *
     * @param player bukkit player that has reached the goal
     */
    public void onPlayerReachedGoal(Player player) {
        GamePlayer gamePlayer = plugin.getPlayerManager().getGamePlayerByPlayer(player);

        if (config.getFinishLinePoints() > 0)
            gamePlayer.addPoints(config.getFinishLinePoints());

        List<GamePlayer> activePlayers = getActivePlayers();
        int max = Math.min(activePlayers.size() + places.size(), 3);

        if (!places.contains(gamePlayer)) {
            places.add(gamePlayer);
        } else {
            return;
        }

        StatisticManager statisticManager = plugin.getStatisticManager();
        long previous = statisticManager.getLongStatistic(player.getUniqueId(), GameStatistic.FASTEST_FINISH_TIME);
        long current = getTimeSinceStart();

        if(previous == 0 || current < previous){
            statisticManager.setStatistic(player.getUniqueId(), GameStatistic.FASTEST_FINISH_TIME, current);
        }

        statisticManager.incrementStatistic(player.getUniqueId(), GameStatistic.FINISHES, 1);
        int place = places.size();

        announce(translatable("finish_line.goal", NamedTextColor.GRAY,
                text(player.getName(), NamedTextColor.DARK_AQUA))
                .append(Component.space())
                .append(Brackets.brackets(Brackets.Type.PARENTHESES,
                        text(String.valueOf(place), NamedTextColor.DARK_AQUA)
                                .append(Component.text(" / ", NamedTextColor.GRAY))
                                .append(text(String.valueOf(max), NamedTextColor.DARK_AQUA)), Style.style(NamedTextColor.GRAY))), true);

        Bukkit.getPluginManager().callEvent(new PlayerReachedGoalEvent(player, this, place));

        gamePlayer.setState(GamePlayer.State.SPECTATOR);
        player.setGameMode(org.bukkit.GameMode.SPECTATOR);

        if (place == config.getMaxPlaces() || place == max)
            onEnd();

    }

    /**
     * Gets the GamePlayer for the bukkit player in the game
     *
     * @param player bukkit player
     * @return GamePlayer in the game
     */
    public GamePlayer getPlayer(Player player) {
        return getPlayers().stream()
                .filter(p -> p.getBukkitPlayer().equals(player))
                .findAny()
                .orElse(null);
    }

    public Map<GamePlayer, Integer> getPointsMap() {
        List<GamePlayer> players = getPlayers();
        Map<GamePlayer, Integer> map = new HashMap<>();
        players.forEach(gamePlayer -> map.put(gamePlayer, gamePlayer.getPoints()));

        return map;
    }

    /**
     * Gets the top players sorted by points (mode POINTS)
     *
     * @return LinkedHashMap with the player and the value of their points
     */
    public LinkedHashMap<GamePlayer, Integer> getTopPointsPlayersByPoints() {
        return MiscUtil.sortByValue(getPointsMap());
    }

    /**
     * Gets the alive, playing players
     *
     * @return list of active game-players
     */
    public List<GamePlayer> getActivePlayers() {
        return getPlayers().stream()
                .filter(player
                        -> player.getState() == GamePlayer.State.IN_GAME)
                .collect(Collectors.toList());
    }

    /**
     * Gets all players as bukkit players
     *
     * @return list of bukkit players
     */
    public List<Player> getBukkitPlayers() {
        return getPlayers().stream()
                .map(GamePlayer::getBukkitPlayer)
                .collect(Collectors.toList());
    }

    public List<Player> getActiveBukkitPlayers(){
        return getActivePlayers()
                .stream()
                .map(GamePlayer::getBukkitPlayer)
                .collect(Collectors.toList());
    }

    public void showPlayers() {
        getBukkitPlayers().forEach(target ->
                getBukkitPlayers().forEach(player -> player.showPlayer(plugin, target)));
    }

    public void hidePlayers() {
        getBukkitPlayers().forEach(target ->
                getBukkitPlayers().forEach(player -> player.hidePlayer(plugin, target)));
    }

    /**
     * Announce a message to all players in the game
     *
     * @param prefix whether or not to show prefix
     */
    public void announce(Component component, boolean prefix) {
        for (GamePlayer player : getPlayers()) {
            Player p = player.getBukkitPlayer();
            if (p != null) {
                if (prefix) {
                    plugin.getTexty().getChat().sendWithDefaultPrefix(p, component);
                } else {
                    plugin.getTexty().getChat().send(p, component);
                }
            }
        }
    }

    public void announceActionbar(Component component){
        for (GamePlayer player : getPlayers()) {
            Player p = player.getBukkitPlayer();
            if (p != null) {
                p.sendActionBar(component);
            }
        }
    }

    /**
     * Gets the default prefix for locale keys regarding the current game type
     *
     * @return locale prefix
     */
    public String getLocalePrefix() {
        return "games." + type.name().toLowerCase() + ".";
    }

    public abstract Set<State> getPreGameStates();

    public abstract Set<State> getPlayingStates();

    public long getTimeSinceStart(){
        return System.currentTimeMillis() - timeStart;
    }

}