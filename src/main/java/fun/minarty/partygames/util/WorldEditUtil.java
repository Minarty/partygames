package fun.minarty.partygames.util;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Location;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class WorldEditUtil {

    public static void paste(Clipboard clipboard, Location location){
        try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory()
                .getEditSession(new BukkitWorld(location.getWorld()), 20000)) {

            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ()))
                    .build();

            Operations.complete(operation);
        }
    }

    public static void pasteSchematic(File file, Location location){
        Clipboard clipboard;

        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if(format == null)
            return;

        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            clipboard = reader.read();
            try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory()
                    .getEditSession(new BukkitWorld(location.getWorld()), 20000)) {

                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ()))
                        .build();
                Operations.complete(operation);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
