package fun.minarty.partygames.state;

import lombok.Getter;
import net.minikloon.fsmgasm.StateSeries;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * A StateSeries hooked up with Bukkit scheduler to perform updates <br>
 * This state never ends, to allow for other states to push new states
 */
public class ScheduledStateSeries extends StateSeries {

    private final Plugin plugin;
    private final long interval;
    protected BukkitTask scheduledTask;

    @Getter
    private int tick;
    private final long tickInterval;

    private int current;

    public ScheduledStateSeries(Plugin plugin, long interval) {
        this.plugin = plugin;
        this.interval = interval;
        this.tickInterval = interval / 20;
    }

    @Override
    public final void onStart() {
        super.onStart();
        current = super.getCurrent();
        scheduledTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if(current != super.getCurrent()) {
                current = super.getCurrent();
                tick = 0;
            }

            update();
            tick += tickInterval;
        }, 0L, interval);
    }

    /**
     * Gets remaining seconds of the current state in this series
     * @return remaining seconds
     */
    public long getRemainingSeconds(){
        return super.getStates()
                .get(super.getCurrent()).getDuration().toSeconds() - tick;
    }

    @Override
    public final void onEnd() {
        super.onEnd();
        if(scheduledTask != null)
            scheduledTask.cancel();
    }

    @Override
    public boolean isReadyToEnd() {
        return false;
    }

}