package fun.minarty.partygames.util;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import fun.minarty.partygames.PartyGames;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoadedSchematics {

    private final Map<String, Clipboard> loaded = new HashMap<>();

    public LoadedSchematics(PartyGames plugin){
        load("elytra0", new File(plugin.getDataFolder(), "elytra/0.schem"));
        load("elytra1", new File(plugin.getDataFolder(), "elytra/1.schem"));
        load("elytra2", new File(plugin.getDataFolder(), "elytra/2.schem"));
    }

    public void load(String name, File file) {
        System.out.println("Loading schematic " + file.getPath());
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
