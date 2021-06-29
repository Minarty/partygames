package fun.minarty.partygames.util.jackson;

import fun.minarty.partygames.api.model.kit.Kit;
import fun.minarty.partygames.manager.KitManager;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class KitDeserializer extends JsonDeserializer<Kit> {

    private final KitManager kitManager;

    public KitDeserializer(KitManager kitManager) {
        this.kitManager = kitManager;
    }

    @Override
    public Kit deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return kitManager.getKitByName(jsonParser.readValueAs(String.class));
    }

}