package fun.minarty.partygames.model.config;

import fun.minarty.partygames.api.model.config.GameConfig;
import fun.minarty.partygames.api.model.game.GameType;
import fun.minarty.partygames.api.model.game.PlayMode;
import fun.minarty.partygames.api.model.game.SpawnMode;
import fun.minarty.partygames.api.model.kit.Kit;
import fun.minarty.partygames.api.model.misc.Door;
import fun.minarty.partygames.util.Cuboid;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.LinkedList;
import java.util.List;

/**
 * Config class containing direct access to all config fields <br>
 * most of them have default values if no entry was found
 */
@SuppressWarnings("FieldMayBeFinal") // Accessible so that we can override them, might work anyway?
@Getter
public class DefaultConfig implements GameConfig {

    private GameType gameType = GameType.ANVIL;
    private Material displayMaterial = Material.DIAMOND;

    private PlayMode mode = PlayMode.PRESENCE;
    private org.bukkit.GameMode gameMode = org.bukkit.GameMode.ADVENTURE;
    private List<Location> spawns = null;
    private SpawnMode spawnMode = SpawnMode.FIRST;
    private boolean respawn = false;
    private Cuboid finishArea = null;
    private int maxPlaces = 3;

    @Getter(AccessLevel.NONE)
    private boolean invincible = true;

    private boolean pvp = false;
    private boolean rounds = false;
    private int pvpGrace = 0;
    private boolean fakeDamage = false;
    private int countdown = 3;
    private int duration = 180;
    private Cuboid startWall = null;
    private Material startWallMaterial = null;
    private List<Door> doors = null;
    private LinkedList<Cuboid> checkpoints = null;
    private Kit kit = null;
    private int maxYLevel = 0;
    private int finishLinePoints = 0;
    private boolean worldModifiable = false;
    private Difficulty difficulty = Difficulty.PEACEFUL;
    private boolean addGamePoints = false;
    private boolean collision = false;
    private String author = "Minarty Design";
    private boolean pvpPoints = false;
    private List<Material> deadlyBlocks = null;
    private boolean playersHidden = false;
    private boolean removeDoorButtonsOnClick = true;

    /* Overrides depending on other config fields */

    @Override
    public boolean isInvincible(){
        return !pvp && invincible;
    }

}