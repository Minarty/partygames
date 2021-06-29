package fun.minarty.partygames.manager.event;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.model.game.PartyGame;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.vehicle.VehicleEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.TimedRegisteredListener;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Game event manager, listens to every event and maps as best as possible
 * to a certain game, so that only players in that game will be affected
 */
public class GameEventManager implements Listener {

    // TODO This class needs documentation

    private final PartyGames plugin;
    private final List<GameEventListener> eventListeners = new ArrayList<>();

    public GameEventManager(PartyGames plugin) {
        this.plugin = plugin;
    }

    public void registerEvent(Class<? extends Event> event, Listener listener, EventPriority priority,
                              EventExecutor executor, Plugin plugin, boolean ignoreCancelled) {

        TimedRegisteredListener customListener = new TimedRegisteredListener(listener, executor, priority,
                plugin, ignoreCancelled);

        getEventListeners(event).register(customListener);
    }

    public <T extends Event> void registerListener(Listener listener, Class<T> eventClass){
        EventExecutor customExecutor = this::onEvent;
        registerEvent(eventClass, listener, EventPriority.HIGHEST,
                customExecutor, plugin, false);
    }

    public GameEventListener getGameListener(World world){

        PartyGame game = plugin.getGameManager().getGame();

        if(game == null)
            return null;

        if (game.getWorld().equals(world)) {
            return eventListeners.stream()
                    .filter(Objects::nonNull)
                    .filter(eventListener -> eventListener.shouldHandle(game))
                    .findAny()
                    .orElse(null);
        }

        return null;
    }

    private void onEvent(Listener listener, Event event) {

        GameEventListener eventListener = null;
        if(event instanceof PlayerEvent){
            Player player = ((PlayerEvent) event).getPlayer();
            if(player.getGameMode() == GameMode.SPECTATOR)
                return;

            eventListener = getGameListener(player.getWorld());
        } else if(event instanceof BlockEvent){
            eventListener = getGameListener(((BlockEvent) event).getBlock().getWorld());
        } else if(event instanceof VehicleEvent){
            eventListener = getGameListener(((VehicleEvent) event).getVehicle().getWorld());
        } else if(event instanceof EntityEvent){
            Entity entity = ((EntityEvent) event).getEntity();
            if(entity instanceof Player){
                if(((Player) entity).getGameMode() == GameMode.SPECTATOR)
                    return;
            }

            eventListener = getGameListener(entity.getWorld());
        }

        if(eventListener != null)
            eventListener.onEvent(listener, event);
    }

    private HandlerList getEventListeners(Class<? extends Event> type) {
        try {
            Method method = getRegistrationClass(type).getDeclaredMethod("getHandlerList");
            method.setAccessible(true);
            return (HandlerList) method.invoke(null);
        } catch (Exception e) {
            throw new IllegalPluginAccessException(e.toString());
        }
    }

    private Class<? extends Event> getRegistrationClass(Class<? extends Event> clazz) {
        try {
            clazz.getDeclaredMethod("getHandlerList");
            return clazz;
        } catch (NoSuchMethodException e) {
            if (clazz.getSuperclass() != null
                    && !clazz.getSuperclass().equals(Event.class)
                    && Event.class.isAssignableFrom(clazz.getSuperclass())) {
                return getRegistrationClass(clazz.getSuperclass().asSubclass(Event.class));

            } else {
                throw new IllegalPluginAccessException("Unable to find handler list for event " + clazz.getName());
            }
        }
    }

    public void registerGameListener(GameEventListener eventListener) {
        eventListeners.add(eventListener);
    }

    public void unregisterGameListener(GameEventListener eventListener) {
        eventListeners.remove(eventListener);
    }

}
