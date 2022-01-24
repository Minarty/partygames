package fun.minarty.partygames.manager;

import com.grinderwolf.swm.api.exceptions.CorruptedWorldException;
import com.grinderwolf.swm.api.exceptions.NewerFormatException;
import com.grinderwolf.swm.api.exceptions.UnknownWorldException;
import com.grinderwolf.swm.api.exceptions.WorldInUseException;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimeProperties;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import com.grinderwolf.swm.plugin.SWMPlugin;
import fun.minarty.partygames.api.model.game.GameType;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manager which handles map/world loading
 */
public class MapManager {

    @Getter
    private String activeWorld;
    private final Logger logger;
    private final SWMPlugin swmPlugin;

    public MapManager(Logger logger){
        this.logger        = logger;
        this.swmPlugin     = SWMPlugin.getInstance();
    }

    /**
     * Loads the map into a world and sets appropriate properties
     * @param type type of the game
     */
    public void loadMap(GameType type, Consumer<SlimeWorld> consumer, boolean edit) {
        logger.info("Loading map for " + type);

        String name = getWorldName(type);
        SlimePropertyMap properties = new SlimePropertyMap();
        properties.setValue(SlimeProperties.SPAWN_X, 0);
        properties.setValue(SlimeProperties.SPAWN_Y, 100);
        properties.setValue(SlimeProperties.SPAWN_Z, 0);
        properties.setValue(SlimeProperties.DIFFICULTY, "peaceful");

        SWMPlugin instance = SWMPlugin.getInstance();
        SlimeWorld world;

        try {
            world = instance.loadWorld(instance.getLoader("file"), name, !edit, properties);
        } catch (UnknownWorldException | IOException | CorruptedWorldException |
                NewerFormatException | WorldInUseException e) {

            e.printStackTrace();
            return;
        }

        // TODO need better solution, both normal games and gamecreator uses this.
        activeWorld = name;

        consumer.accept(world);
    }

    public String getWorldName(GameType type){
        return "MG_" + type.name();
    }

    public synchronized World loadSlimeWorld(SlimeWorld slimeWorld){
        swmPlugin.generateWorld(slimeWorld);
        logger.info("Successfully loaded.");

        World world = Bukkit.getWorld(slimeWorld.getName());
        if(world == null)
            return null;

        world.setTime(7000);
        world.setStorm(false);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);

        return world;
    }

    /**
     * Cleans up the map by unloading the world and
     * deleting the active world directory
     */
    public void cleanupActiveMap(){
        cleanupMap(activeWorld, false);
    }

    public void cleanupMap(String worldName, boolean save){
        World world = Bukkit.getWorld(worldName);
        if(world == null)
            return;

        logger.info("Cleaning up world");

        world.getPlayers().forEach(player -> Bukkit.dispatchCommand(player, "hub"));
        if(!Bukkit.unloadWorld(world, save)){
            logger.log(Level.SEVERE, "Unable to unload game world " + worldName);
            return;
        }

        logger.info("Cleanup complete");
    }

}
