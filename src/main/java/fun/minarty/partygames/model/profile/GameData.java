package fun.minarty.partygames.model.profile;

import cc.pollo.store.api.StoreEntity;
import cc.pollo.store.mongo.MongoStoreEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Persistently saved information about a past game
 */
public class GameData implements MongoStoreEntity<UUID> {

    @Getter @Setter
    private UUID id;

    private List<UUID> players;

    private Map<UUID, String> chatHistory;

}