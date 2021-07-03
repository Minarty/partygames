package fun.minarty.partygames.store;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.api.model.config.GameConfig;
import fun.minarty.partygames.api.model.game.GameType;
import fun.minarty.partygames.api.model.kit.Kit;
import fun.minarty.partygames.api.model.misc.Door;
import fun.minarty.partygames.manager.GameManager;
import fun.minarty.partygames.model.misc.DefaultDoor;
import fun.minarty.partygames.util.Cuboid;
import fun.minarty.partygames.util.DefaultCuboid;
import fun.minarty.partygames.util.jackson.KitDeserializer;
import fun.minarty.partygames.util.jackson.KitSerializer;
import fun.minarty.partygames.util.jackson.LocationDeserializer;
import fun.minarty.partygames.util.jackson.LocationSerializer;
import lombok.Getter;
import org.bson.Document;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the external game config loading, also sets up <br>
 * the Jackson ObjectMapper to serialize/deserialize data
 */
public class ConfigManager {

    @Getter
    private GameConfig loadedConfig;
    private final MongoCollection<Document> collection;
    @Getter
    private final ObjectMapper mapper = new ObjectMapper();

    @Getter
    private final List<String> modifiedFields = new ArrayList<>();

    private static final String[] REQUIRED_FIELDS = new String[]{"gameType"};

    private final PartyGames plugin;

    public ConfigManager(PartyGames plugin, MongoDatabase database){
        this.plugin = plugin;
        this.collection = database.getCollection("party_gameconfig");

        SimpleModule module = new SimpleModule();
        module.addDeserializer(Location.class, new LocationDeserializer());
        module.addSerializer(Location.class, new LocationSerializer());
        module.addSerializer(Kit.class, new KitSerializer());
        module.addDeserializer(Kit.class, new KitDeserializer(plugin.getKitManager()));
        deserializeAs(module, Cuboid.class, DefaultCuboid.class);
        deserializeAs(module, Door.class, DefaultDoor.class);

        mapper.registerModule(module);

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public GameConfig loadConfig(GameType gameType){
        plugin.getLogger().info("Loading game config for " + gameType.name());
        loadedConfig = findConfig(gameType);
        return loadedConfig;
    }

    /**
     * Loads a game config from MongoDB
     * @param gameType type of the game
     * @return found config or null if not found
     */
    private @Nullable GameConfig findConfig(GameType gameType){
        GameConfig gameConfig = null;
        for (Document document : collection.find(Filters.eq("gameType", gameType.name()))) {
            plugin.getLogger().info("Found config");

            if(!validateRequiredFields(gameType, document))
                continue;

            try {
                gameConfig = mapper.readValue(document.toJson(), GameManager.GAME_TYPES.get(gameType).getConfigClass());
                if(gameConfig == null)
                    plugin.getLogger().warning("An error occurred parsing " + gameType.name());

                break;
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return null;
            }
        }

        return gameConfig;
    }

    /**
     * Validates that a document has all the required fields, otherwise <br>
     * log warning for each missing field.
     *
     * @param type type of the game
     * @param document document to validate
     * @return whether the document has all the required fields
     */
    private boolean validateRequiredFields(GameType type, Document document){
        for (String requiredField : REQUIRED_FIELDS) {
            if(!document.containsKey(requiredField)){
                plugin.getLogger().warning(type.name() + " is missing required field " + requiredField + "!");
                return false;
            }
        }

        return true;
    }

    private <T> void deserializeAs(SimpleModule module, Class<T> base, Class<? extends T> implClass){
        module.addDeserializer(base, new DeserializeAs<>(base, implClass));
    }

    /**
     * Deserializes a base class as the implementation class specified
     * @param <T> type of the base class
     */
    private static final class DeserializeAs<T> extends StdDeserializer<T> {

        private final Class<? extends T> implClass;

        protected DeserializeAs(Class<T> base, Class<? extends T> implClass) {
            super(base);
            this.implClass = implClass;
        }

        @Override
        public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.readValueAs(implClass);
        }
    }

}