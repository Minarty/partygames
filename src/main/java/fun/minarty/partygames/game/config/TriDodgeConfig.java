package fun.minarty.partygames.game.config;

import fun.minarty.partygames.game.TriDodgeGame;
import fun.minarty.partygames.model.config.DefaultConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class TriDodgeConfig extends DefaultConfig {

    private List<TriDodgeGame.Side> sides;
    private Map<Integer, TriDodgeGame.RoundConfig> roundConfigs;

}