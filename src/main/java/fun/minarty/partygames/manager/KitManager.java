package fun.minarty.partygames.manager;

import fun.minarty.partygames.api.model.kit.Kit;
import fun.minarty.partygames.model.kit.DefaultKit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.PlayerInventory;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Manager that handles kit caching, loading and saving
 */
public class KitManager {

    private final Set<DefaultKit> kits = new HashSet<>();
    private FileConfiguration config;
    private File file;

    /**
     * Loads kit from config
     * @param dataFolder folder to load file from
     */
    public void load(File dataFolder){
        config = new YamlConfiguration();
        file = new File(dataFolder, "kits.yml");

        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            return;
        }

        ConfigurationSection section = config.getConfigurationSection("kits");
        if(section == null)
            return;

        for(String key : section.getKeys(false)){
            kits.add(DefaultKit.fromConfig(section.getConfigurationSection(key)));
        }
    }

    /**
     * Saves a kit to config and caches it
     * @param name name of the kit
     * @param inventory inventory/loadout to save
     */
    public void save(String name, PlayerInventory inventory){
        ConfigurationSection section = config.createSection("kits." + name);
        DefaultKit kit = DefaultKit.toConfig(inventory, name, section);

        saveConfig();

        kits.add(kit);
    }

    private void saveConfig(){
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets a kit by its name
     * @param name name of the kit
     * @return kit with the name or null if not found
     */
    public fun.minarty.partygames.api.model.kit.Kit getKitByName(String name){
        return kits.stream()
                .filter(kit -> kit.getName().equalsIgnoreCase(name))
                .findAny()
                .orElse(null);
    }

    public void delete(Kit kit) {
        config.set("kits." + kit.getName(), null);
        saveConfig();
        kits.remove((DefaultKit) kit);
    }

}