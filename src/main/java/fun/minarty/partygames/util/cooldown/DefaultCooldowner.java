package fun.minarty.partygames.util.cooldown;

import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class DefaultCooldowner implements Cooldowner {

    private final Map<Object, Cooldown> cooldowns = new HashMap<>();

    @Override
    public void cooldown(Object object, long time, ChronoUnit unit) {
        cooldowns.put(object, new Cooldown(time, unit));
    }

    @Override
    public void remove(Object object) {
        cooldowns.remove(object);
    }

    @Override
    public Cooldown getCooldown(Object object) {
        return cooldowns.get(object);
    }

    @Override
    public boolean hasCooldown(Object object){
        removeExpired();
        Cooldown cooldown = cooldowns.get(object);
        return cooldown != null && !cooldown.hasExpired();
    }

    private void removeExpired(){
        cooldowns.values().removeIf(Cooldown::hasExpired);
    }

}