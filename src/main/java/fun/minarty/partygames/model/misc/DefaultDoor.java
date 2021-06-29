package fun.minarty.partygames.model.misc;

import fun.minarty.partygames.api.model.misc.Door;
import fun.minarty.partygames.util.Cuboid;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class DefaultDoor implements Door {

    private Cuboid area;
    private Location button;

    private final List<UUID> clickedBy = new ArrayList<>();

    @Override
    public boolean shouldOpen(Player player, Block button){
        return this.button.getBlock().equals(button)
                && !clickedBy.contains(player.getUniqueId());
    }

    @Override
    public void open(Player player){
        if(area == null)
            return;

        clickedBy.add(player.getUniqueId());
        area.fill(Material.AIR);
    }

}