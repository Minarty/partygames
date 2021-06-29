package fun.minarty.partygames.model.profile;

import com.fasterxml.jackson.annotation.JsonProperty;
import fun.minarty.partygames.api.model.game.GameType;
import fun.minarty.partygames.api.model.profile.GameStatistic;
import fun.minarty.partygames.api.model.profile.Profile;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.mongojack.Id;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Base class for {@link Profile}
 */
public class DefaultProfile implements Profile {

    @Getter
    private final UUID id;
    @Getter @Setter
    private int xp;
    @Getter
    private final Map<GameType, Map<GameStatistic, Object>> statistics;

    public DefaultProfile(@Id UUID uniqueId, @JsonProperty("xp") int xp,
                          @JsonProperty("statistics") Map<GameType, Map<GameStatistic, Object>> statistics){

        this.id = uniqueId;
        this.xp = xp;
        this.statistics = statistics;
    }

    public Object getStatistic(GameType type, GameStatistic statistic){
        return getStatisticsForGame(type).get(statistic);
    }

    public void incrementStatistic(GameType type, GameStatistic statistic, int inc){
        Object o = getStatistic(type, statistic);
        if(o instanceof Integer){
            setStatistic(type, statistic, ((int) o) + inc);
        }
    }

    public void setStatistic(GameType type, GameStatistic statistic, Object value){
        getStatisticsForGame(type).put(statistic, value);
    }

    private @NonNull Map<GameStatistic, Object> getStatisticsForGame(GameType type){
        if(statistics.containsKey(type)){
            return statistics.get(type);
        } else {
            Map<GameStatistic, Object> map = new HashMap<>();
            statistics.put(type, map);
            return map;
        }
    }

}