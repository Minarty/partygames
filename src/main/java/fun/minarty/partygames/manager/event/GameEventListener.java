package fun.minarty.partygames.manager.event;

import fun.minarty.partygames.PartyGames;
import fun.minarty.partygames.model.game.PartyGame;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Listener that is registered for every Game and listens to most player events
 */
public class GameEventListener implements Listener {

    @Getter @Setter
    private PartyGame game;

    private final Set<ListenerHandler<?>> handlers = ConcurrentHashMap.newKeySet();
    private final GameEventManager eventManager;

    public GameEventListener(PartyGames plugin){
        this.eventManager = plugin.getEventManager();
    }

    public boolean shouldHandle(PartyGame game){
        if(game == null || this.game == null)
            return false;

        return game.equals(this.game);
    }

    /**
     * Registers an event to the game-wide event listener
     * @param eventClass
     * @param consumer
     * @param <T>
     */
    public <T extends Event> void registerListener(Class<T> eventClass, Consumer<T> consumer) {
        registerListener(this, eventClass, consumer);
    }

    public <T extends Event> void registerConditionalListener(Class<T> eventClass, Consumer<T> consumer, boolean condition) {
        if(condition)
            registerListener(eventClass, consumer);
    }

    /**
     * Registers an event to the state-wide event listener
     * @param listener
     * @param eventClass
     * @param consumer
     * @param <T>
     */
    public <T extends Event> void registerListener(Listener listener, Class<T> eventClass, Consumer<T> consumer) {
        eventManager.registerListener(listener, eventClass);
        handlers.add(new ListenerHandler<>(eventClass, consumer, listener));
    }

    public void unregisterListener(Listener listener){
        handlers.removeIf(listenerHandler -> {
            boolean equal = listenerHandler.getListener().equals(listener);
            if(equal)
                HandlerList.unregisterAll(listenerHandler.getListener());

            return equal;
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void onEvent(Listener listener, Event event){
        Set<ListenerHandler<?>> handlers = Collections.synchronizedSet(this.handlers);
        for(ListenerHandler handler : handlers){
            if(handler.getEventClass() == event.getClass() && handler.getListener().equals(listener)){
                handler.getEventConsumer().accept(event);
            }
        }
    }

    public static class ListenerHandler<T extends Event> {

        @Getter
        private final Class<T> eventClass;
        @Getter
        private final Consumer<T> eventConsumer;
        @Getter
        private final Listener listener;

        public ListenerHandler(Class<T> eventClass,
                               Consumer<T> eventConsumer, Listener listener) {

            this.eventClass = eventClass;
            this.eventConsumer = eventConsumer;
            this.listener = listener;
        }

    }

}