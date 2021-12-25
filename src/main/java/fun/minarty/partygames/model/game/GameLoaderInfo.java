package fun.minarty.partygames.model.game;

import fun.minarty.partygames.api.model.config.GameConfig;
import lombok.Getter;

/**
 * Contains info for loading a game, e.g. game class and config class.
 */
@Getter
public class GameLoaderInfo {

    private final Class<? extends PartyGame> gameClass;
    private final Class<? extends GameConfig> configClass;

    /**
     * Constructs {@link GameLoaderInfo}
     * @param gameClass class that contains game logic.
     * @param configClass class that contains the game config.
     */
    public GameLoaderInfo(Class<? extends PartyGame> gameClass,
                          Class<? extends GameConfig> configClass) {

        this.gameClass = gameClass;
        this.configClass = configClass;
    }

}
