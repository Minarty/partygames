package fun.minarty.partygames.model.game;

import fun.minarty.partygames.api.model.config.GameConfig;
import lombok.Getter;

@Getter
public class GameLoaderInfo {

    private final Class<? extends PartyGame> gameClass;
    private final Class<? extends GameConfig> configClass;

    public GameLoaderInfo(Class<? extends PartyGame> gameClass,
                          Class<? extends GameConfig> configClass) {

        this.gameClass = gameClass;
        this.configClass = configClass;
    }

}
