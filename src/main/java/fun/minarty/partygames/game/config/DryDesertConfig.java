package fun.minarty.partygames.game.config;

import fun.minarty.partygames.model.config.DefaultConfig;
import fun.minarty.partygames.util.Cuboid;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DryDesertConfig extends DefaultConfig {

    private Cuboid dropArea;

}