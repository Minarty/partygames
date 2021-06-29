package fun.minarty.partygames.game.config;

import fun.minarty.partygames.game.AnimalSniperGame;
import fun.minarty.partygames.model.config.DefaultConfig;
import fun.minarty.partygames.util.Cuboid;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class AnimalSniperConfig extends DefaultConfig {

    private Cuboid spawnArea;
    private Set<AnimalSniperGame.TargetAnimalInfo> targets;

}