package fun.minarty.partygames.state;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.model.game.PartyGame;
import net.minikloon.fsmgasm.State;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Main state for any state in a party game
 * Handles listener and scheduler registering/unregistering
 */
public abstract class GameState extends State implements Listener {

    protected final PartyGame game;
    protected final PartyGames plugin;

    protected final Set<Listener> listeners = new HashSet<>();
    protected final Set<BukkitTask> tasks = new HashSet<>();

    public GameState(PartyGame game, PartyGames plugin) {
        this.plugin = plugin;
        this.game   = game;
    }

    @Override
    public final void start() {

        Method[] declaredMethods = getClass().getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            StateListen stateListen = declaredMethod.getAnnotation(StateListen.class);
            if(stateListen != null){
                Class<?>[] parameterTypes = declaredMethod.getParameterTypes();
                Class<? extends Event> parameterType = (Class<? extends Event>) parameterTypes[0];

                listen(parameterType, ev -> {
                    try {
                        declaredMethod.invoke(this, ev);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                });
            }
        }

        super.start();
    }

    @Override
    public final void end() {
        super.end();
        if(!super.getEnded())
            return;

        listeners.forEach(HandlerList::unregisterAll);
        tasks.forEach(BukkitTask::cancel);
        listeners.clear();
        tasks.clear();

        game.unregisterListener(this);
    }

    /**
     * Listens to an event for the duration of the GameState
     * @param eventClass class of the event
     * @param consumer consumer for called events
     */
    public <T extends Event> void listen(Class<T> eventClass, Consumer<T> consumer){
        game.registerListener(this, eventClass, consumer);
    }

    /**
     * Enables a event that was disabled due to protection or similar
     * @param eventClass class of the event
     */
    public <T extends Event & Cancellable> void enableEvent(Class<T> eventClass){
        Consumer<T> enablingConsumer = t -> t.setCancelled(false);
        game.registerListener(this, eventClass, enablingConsumer);
    }

    /**
     * Enables a general event that was disabled due to protection or similar if
     * the predicate condition is met on the incoming event
     * @param eventClass class of the event
     * @param enableCondition condition to check for
     */
    public <T extends Event & Cancellable> void enableEvent(Class<T> eventClass, Predicate<T> enableCondition){
        Consumer<T> enablingConsumer = t -> {
            if(enableCondition.test(t))
                t.setCancelled(false);
        };

        game.registerListener(this, eventClass, enablingConsumer);
    }

    /**
     * Schedules a runnable
     * @param runnable runnable to schedule
     * @param delay delay
     */
    protected void schedule(Runnable runnable, long delay) {
        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, runnable, delay);
        tasks.add(task);
    }

    /**
     * Schedules a repeating timer for the duration of the GameState
     * @param runnable runnable to schedule
     * @param delay initial delay
     * @param interval interval to run after
     */
    protected void scheduleRepeating(Runnable runnable, long delay, long interval) {
        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, runnable, delay, interval);
        tasks.add(task);
    }

}