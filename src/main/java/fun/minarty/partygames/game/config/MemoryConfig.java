package fun.minarty.partygames.game.config;

import fun.minarty.partygames.model.config.DefaultConfig;
import fun.minarty.partygames.util.Cuboid;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class MemoryConfig extends DefaultConfig {

    private List<Cuboid> platforms;
    private List<Cuboid> indicators;

}