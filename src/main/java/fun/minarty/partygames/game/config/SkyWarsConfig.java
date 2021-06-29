package fun.minarty.partygames.game.config;

import fun.minarty.partygames.model.config.DefaultConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.Location;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class SkyWarsConfig extends DefaultConfig {

    private List<Location> islandChests;
    private List<Location> midChests;
    
}
