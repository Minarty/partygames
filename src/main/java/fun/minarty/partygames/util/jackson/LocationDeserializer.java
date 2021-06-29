package fun.minarty.partygames.util.jackson;

import fun.minarty.partygames.PartyGames;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.IOException;

public class LocationDeserializer extends JsonDeserializer<Location> {

    @Override
    public Location deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode root = jsonParser.getCodec().readTree(jsonParser);

        double x = root.get("x").asDouble();
        double y = root.get("y").asDouble();
        double z = root.get("z").asDouble();
        float yaw = root.get("yaw").floatValue();
        float pitch = root.get("pitch").floatValue();

        return new Location(Bukkit.getWorld(PartyGames.getInstance().getMapManager().getActiveWorld()),
                x, y, z, yaw, pitch);
    }

}
