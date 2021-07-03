package fun.minarty.partygames.util;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import fun.minarty.partygames.PartyGames;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Loading schematics into memory
 */
public final class LoadedSchematics {

    private final Map<String, Clipboard> loaded = new HashMap<>();
    private final PartyGames plugin;

    public LoadedSchematics(PartyGames plugin){
        this.plugin = plugin;
    }

    public void loadDefault(){
        loadFromPlugin(plugin, "elytra0", "elytra/0.schem");
        loadFromPlugin(plugin, "elytra1", "elytra/1.schem");
        loadFromPlugin(plugin, "elytra2", "elytra/2.schem");
    }

    public void loadFromPlugin(Plugin plugin, String name, String path){
        load(name, new File(plugin.getDataFolder(), path));
    }

    public void load(String name, File file) {
        plugin.getLogger().info("Loading schematic " + file.getPath());
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null)
            return;

        ClipboardReader reader;
        try {
            reader = format.getReader(new FileInputStream(file));
            loaded.put(name, reader.read());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }


    public Clipboard get(String name) {
        return loaded.get(name);
    }

}
