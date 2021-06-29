package fun.minarty.partygames.util.jackson;

import fun.minarty.partygames.api.model.kit.Kit;
import fun.minarty.partygames.model.kit.DefaultKit;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class KitSerializer extends JsonSerializer<Kit> {

    @Override
    public void serialize(Kit kit, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(kit.getName());
    }

}