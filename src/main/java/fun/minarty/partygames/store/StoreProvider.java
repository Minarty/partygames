package fun.minarty.partygames.store;

import cc.pollo.store.mongo.MongoStore;
import com.mongodb.client.MongoDatabase;
import fun.minarty.gatus.manager.mongo.MongoManager;
import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.api.model.profile.Profile;
import fun.minarty.partygames.model.profile.DefaultProfile;
import lombok.Getter;

import java.util.UUID;

/**
 * Provides different stores
 */
public class StoreProvider {

    @Getter
    private MongoStore<UUID, Profile, DefaultProfile> profileStore;


    @Getter
    private ConfigManager configManager;

    public void loadStores(PartyGames plugin, MongoManager mongoManager){
        profileStore  = new PlayerProfileStore(mongoManager.getDatabase());
        configManager = new ConfigManager(plugin, mongoManager.getDatabase());
    }

    public static class PlayerProfileStore extends MongoStore<UUID, Profile, DefaultProfile> {
        public PlayerProfileStore(MongoDatabase database) {
            super(database, "party_profiles", DefaultProfile.class);
        }
    }

}