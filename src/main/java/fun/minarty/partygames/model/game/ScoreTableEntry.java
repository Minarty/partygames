package fun.minarty.partygames.model.game;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Entry for the score table shown to all players <br>
 * at the end of the game
 */
@Builder
public class ScoreTableEntry {

    @Getter
    private final int place;
    @Getter
    private final GamePlayer player;
    @Getter
    @Builder.Default
    private final int points = -1;
    @Getter
    private final int reward;
    @Getter @Setter
    private int coins;

    public boolean isFirstPlace(){
        return place == 0;
    }

}