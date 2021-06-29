package fun.minarty.partygames.util.cooldown;

import java.time.temporal.ChronoUnit;

public interface Cooldowner {

    static Cooldowner cooldowner(){
        return new DefaultCooldowner();
    }

    void cooldown(Object object, long time, ChronoUnit unit);

    void remove(Object object);

    Cooldown getCooldown(Object object);

    boolean hasCooldown(Object object);

}