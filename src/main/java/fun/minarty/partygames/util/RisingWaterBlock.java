package fun.minarty.partygames.util;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;

public class RisingWaterBlock {

    private int y = 0;
    private final Location location;
    @Getter
    private final int maxY;

    private static final int MIN = 7; // When the water is as low as possible
    private int level = MIN;

    public RisingWaterBlock(Location location, int maxY) {
        this.location = location;
        this.maxY = maxY;
    }

    public void raise(){
        if(y > maxY)
            return;

        Block block = location.clone().add(0, y, 0).getBlock();
        boolean shouldRaise = block.getType() == Material.AIR
                || block.getType() == Material.WATER;

        if(shouldRaise) {
            block.setType(Material.WATER);
            BlockData blockData = block.getBlockData();
            if(blockData instanceof Levelled){
                ((Levelled) blockData).setLevel(level);
                block.setBlockData(blockData);
            }
        }

        if (level == 0) {
            level = MIN;
            y++;
        } else {
            level--;
        }

    }

}