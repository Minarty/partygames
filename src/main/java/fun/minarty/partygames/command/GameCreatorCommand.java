package fun.minarty.partygames.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.Filters;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.api.model.config.GameConfig;
import fun.minarty.partygames.api.model.game.GameType;
import fun.minarty.partygames.model.config.DefaultConfig;
import fun.minarty.partygames.util.Cuboid;
import fun.minarty.partygames.util.DefaultCuboid;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Commands to set config properties in-game. <br>
 * This is much easier when dealing with locations and areas.
 */
public class GameCreatorCommand implements CommandExecutor {

    // TODO this class needs some refactoring and command validation, description etc

    private GameType currentGameType;
    private final Map<String, Object> currentSession = new HashMap<>();
    private final Map<String, Map<String, Object>> customObjects = new HashMap<>();
    private final Map<UUID, Object> properties = new HashMap<>();

    private static final UUID CONSOLE_UUID = UUID.randomUUID();
    private static final DefaultConfig DEFAULT_CONFIG = new DefaultConfig();
    private static final String CUSTOM_OBJECT_PREFIX = "custom:";

    private final PartyGames plugin;

    public GameCreatorCommand(PartyGames plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if(!sender.isOp())
            return true;

        if(args.length == 0){
            sender.sendMessage("\n" +
                    "    /gamecreator edit <gametype> - Påbörjar en ny ändring av config, laddar in alla gamla värden\n" +
                    "    /gamecreator setobject <namn> <typ> - Sätter objektet för \"namn\" till serialiserade typen \"typ\"\n" +
                    "    /gamecreator addobject <namn> <typ> - Lägger till objektet av serialiserade typen \"typ\" i array \"namn\"\n" +
                    "    /gamecreator setobject <namn> <typ> <customid> - Gör samma sak som setobject fast sparar det i ett custom objekt\n" +
                    "    /gamecreator addobject <namn> <typ> <customid> - Gör samma sak som addobject fast sparar det i ett custom objekt\n" +
                    "    /gamecreator clearobject <namn> - Tar bort ett objekt\n" +
                    "    /gamecreator save - Sparar den nya configen till databasen\n" +
                    "\n" +
                    "    För att spara ett custom objekt körs setobject eller addobject fast custom:<customid> som \"typ\"");
            return true;
        }

        Player player = null;
        if(sender instanceof Player)
            player = (Player) sender;

        boolean customObj = args.length == 4;
        switch (args[0]){
            case "edit":{
                if (currentGameType != null) {
                    sender.sendMessage("Not saved, use /gamecreator discard");
                    return true;
                }

                sender.sendMessage("Loading config..");

                currentGameType = GameType.valueOf(args[1]);
                GameConfig config = plugin.getStoreProvider().getConfigManager().loadConfig(currentGameType);
                if(config == null){
                    sender.sendMessage("Could not load config.");
                    return true;
                }

                currentSession.clear();

                try {
                    currentSession.putAll(getValues(config));
                } catch (InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }

                sender.sendMessage("Loaded " + currentSession.keySet().size() + " keys from config for game type " + currentGameType.name());
                break;
            }

            case "addproperty":
            case "setproperty":{

                String name = args[1];
                String mode = args[2];

                StringBuilder str = new StringBuilder();
                for (int i = 3; i < args.length; i++) {
                    str.append(" ").append(args[i]);
                }

                Object o = parseProperty(mode, str.toString().trim());
                properties.put(player == null ? CONSOLE_UUID : player.getUniqueId(), o);

                Bukkit.dispatchCommand(sender, "gamecreator " +
                        (args[0].equalsIgnoreCase("setproperty") ? "setobject" : "addobject") + " " + name + " property");

                break;
            }

            case "setobject":{
                String name = args[1];
                String type = args[2];

                Object typeObject;
                if(type.startsWith("custom:")){
                    String customId = type.split(CUSTOM_OBJECT_PREFIX)[1];
                    typeObject = customObjects.get(customId);
                } else {
                    typeObject = getTypeObject(player, type);
                }

                if(customObj){
                    putCustomObject(args[3], name, typeObject);
                } else {
                    currentSession.put(name, typeObject);
                }

                sender.sendMessage("Set object " + name);

                break;
            }

            case "addobject":{
                String name = args[1];
                String type = args[2];

                Object typeObject;
                if(type.startsWith("custom:")){
                    String customId = type.split(CUSTOM_OBJECT_PREFIX)[1];
                    typeObject = customObjects.get(customId);
                } else {
                    typeObject = getTypeObject(player, type);
                }

                if(typeObject == null)
                    return true;

                if(customObj){
                    List list = getOrCreateList(name, customObjects.get(args[3]));
                    if(list == null)
                        return true;

                    list.add(typeObject);
                    putCustomObject(args[3], name, list);
                } else {
                    List list = getOrCreateList(name, currentSession);
                    if(list == null)
                        return true;

                    list.add(typeObject);
                    currentSession.put(name, list);
                }

                sender.sendMessage("Added object to list " + name);
                break;
            }

            case "discard":{
                currentGameType = null;
                currentSession.clear();

                sender.sendMessage("Discarded config");
                break;
            }

            case "clearobject":{
                currentSession.remove(args[1]);
                sender.sendMessage("Cleared property " + args[1]);
                break;
            }

            case "save":{
                ObjectMapper mapper = plugin.getStoreProvider().getConfigManager().getMapper();

                Map<String, Object> defaultValues = null;
                try {
                    defaultValues = getValues(DEFAULT_CONFIG);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }

                if(defaultValues == null)
                    return true;

                Iterator<Map.Entry<String, Object>> it = currentSession.entrySet().iterator();
                while (it.hasNext()){ // Remove entries that haven't been changed
                    Map.Entry<String, Object> entry = it.next();
                    String key = entry.getKey();
                    Object value = entry.getValue();

                    if(defaultValues.containsKey(key)){
                        Object val = defaultValues.get(key);
                        if(value == null || (val != null && val.equals(value))){
                            it.remove();
                        }
                    }
                }

                try {
                    plugin.getCommon().getMongoManager().getDatabase()
                            .getCollection("party_gameconfig")
                            .replaceOne(Filters.eq("gameType", currentGameType.name()),
                                    Document.parse(mapper.writeValueAsString(currentSession)));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                sender.sendMessage("Successfully saved!");
                currentGameType = null;

                break;
            }
        }

        return true;
    }

    /**
     * Parses property from user input
     * @param type which type to parse as
     * @param input what the user inputs
     * @return parsed object or return back input
     */
    private Object parseProperty(String type, String input){
        switch (type.toLowerCase(Locale.ROOT)) {
            case "int" -> {
                return Integer.parseInt(input);
            }
            case "boolean" -> {
                return Boolean.parseBoolean(input);
            }
            case "double" -> {
                return Double.parseDouble(input);
            }
            default -> {
                return input;
            }
        }
    }

    /**
     * Saves a custom object for use in the edit session
     * @param customId id for the custom object
     * @param key game config key
     * @param object value to save
     */
    private void putCustomObject(String customId, String key, Object object){
        Map<String, Object> map = customObjects.get(customId);
        if(map == null)
            map = new HashMap<>();

        map.put(key, object);
        customObjects.put(customId, map);
    }

    /**
     * Gets or creates a new list if key doesn't exist in the map
     *
     * @param key key to get from or put to
     * @param map map to perform the operations on
     * @return found list or null if a key with non list mapping exists
     */
    private List<?> getOrCreateList(String key, Map<String, Object> map){
        Object o = map.get(key);
        if(o == null){
            List<?> list = new ArrayList<>();
            map.put(key, list);
            return list;
        }

        if(!(o instanceof List))
            return null;

        return (List<?>) o;
    }

    private Object getTypeObject(CommandSender sender, String type){
        // TODO enum with console property
        switch (type.toLowerCase(Locale.ROOT)) {
            case "loc" -> {
                Player player = getPlayer(sender);
                if (player == null)
                    return null;

                return player.getLocation();
            }
            case "wgmin" -> {
                Player player = getPlayer(sender);
                if (player == null)
                    return null;

                return getLocation(player.getWorld(), getRegionSelection(player).getMinimumPoint());
            }
            case "wgarea" -> {
                Player player = getPlayer(sender);
                if (player == null)
                    return null;

                return getSelection(player);
            }
            case "property" -> {
                Player player = getPlayer(sender);
                return properties.get(player == null ? CONSOLE_UUID : player.getUniqueId());
            }
        }

        return null;
    }

    /**
     * Simple method to cast command sender as player or null if not plaeyr
     * @param sender command sender
     * @return player or null if not a player sender
     */
    private Player getPlayer(CommandSender sender){
        return (sender instanceof Player) ? (Player) sender : null;
    }

    private Cuboid getSelection(Player player){
        Region regionSelection = getRegionSelection(player);
        return new DefaultCuboid(getLocation(player.getWorld(), regionSelection.getMinimumPoint()),
                getLocation(player.getWorld(), regionSelection.getMaximumPoint()));
    }

    private Location getLocation(World world, BlockVector3 vector){
        return new Location(world, vector.getX(), vector.getY(), vector.getZ());
    }

    private Region getRegionSelection(Player player){
        LocalSession session = plugin.getWorldEdit().getSession(player);
        return session.getSelection(new BukkitWorld(player.getWorld()));
    }

    /**
     * Magic reflection method to get all properties from the config class
     * @param config config to get properties from
     * @return K-V property map
     */
    private Map<String, Object> getValues(@NotNull GameConfig config) throws InvocationTargetException, IllegalAccessException {
        Method[] methods = config.getClass().getMethods();
        Map<String, Object> map = new HashMap<>();
        for(Method m : methods) {
            String name = m.getName();
            boolean get = name.startsWith("get");
            boolean is = name.startsWith("is");

            if(name.equals("is"))
                continue;

            if((get || is) && !name.equals("getValues")) {
                Object value = m.invoke(config);
                int keyStart = is ? 2 : 3;

                char[] chars = name.toCharArray();
                chars[keyStart] = Character.toLowerCase(chars[keyStart]);

                // TODO this can probably be improved
                map.put(new String(chars).substring(keyStart), value);
            }
        }

        return map;
    }

}
