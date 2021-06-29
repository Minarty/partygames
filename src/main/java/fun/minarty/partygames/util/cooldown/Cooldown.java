package fun.minarty.partygames.util.cooldown;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.temporal.TemporalUnit;

public class Cooldown {

    private final TemporalUnit unit;
    private final Instant timeExpire;

    Cooldown(long time, @NotNull TemporalUnit unit){
        this.unit = unit;
        this.timeExpire = getTimeNow().plus(time, unit);
    }

    public boolean hasExpired(){
        Instant timeNow = getTimeNow();
        return timeNow.isAfter(timeExpire);
    }

    private Instant getTimeNow(){
        return Instant.now().truncatedTo(unit);
    }

}