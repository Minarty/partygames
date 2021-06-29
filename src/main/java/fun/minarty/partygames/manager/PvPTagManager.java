package fun.minarty.partygames.manager;

import fun.minarty.partygames.model.game.PvPTag;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages pvp tags
 */
public class PvPTagManager {

    private final Map<UUID, PvPTag> pvpTag = new HashMap<>();

    /**
     * Sets a new tag on the victim
     * @param victimUuid uuid of the victim player
     * @param damagerUuid uuid of the damager
     */
    public void setTag(UUID victimUuid, UUID damagerUuid){
        pvpTag.put(victimUuid, new PvPTag(damagerUuid));
    }

    /**
     * Gets a tag by its unique id
     * @param uniqueId unique id of the tag
     * @return associated tag with id or null of not found
     */
    public PvPTag getTagByUniqueId(UUID uniqueId){
        return pvpTag.get(uniqueId);
    }

}