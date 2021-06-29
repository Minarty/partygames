package fun.minarty.partygames.game.config;

import fun.minarty.partygames.model.config.DefaultConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.Location;

@Data
@EqualsAndHashCode(callSuper = true)
public class ElytraConfig extends DefaultConfig {

    private Location ringStart;

}