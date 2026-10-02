package client_files.render.render.event.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ListenerList {
    private static final Logger LOGGER = LoggerFactory.getLogger(ListenerList.class);

    private record ListenerEntry(
            Object target,
            Method method,
            EventPriority priority,
            boolean receiveCancelled
    ) {}

    private final List<ListenerEntry> entries = new ArrayList<>();

    public void add(Object target, Method method, EventPriority priority, boolean receiveCancelled) {
        method.setAccessible(true);
        entries.add(new ListenerEntry(target, method, priority, receiveCancelled));
        entries.sort(Comparator.comparingInt(e -> e.priority().ordinal()));
    }

    public void remove(Object target) {
        entries.removeIf(e -> e.target() == target);
    }

    public void invoke(Object event) {
        boolean isCancellable = event instanceof Cancellable;
        for (int i = 0; i < entries.size(); i++) {
            ListenerEntry entry = entries.get(i);
            if (isCancellable && ((Cancellable) event).isCancelled() && !entry.receiveCancelled()) {
                continue;
            }
            try {
                entry.method().invoke(entry.target(), event);
            } catch (IllegalAccessException | InvocationTargetException e) {
                LOGGER.error("Failed to invoke event listener for {}", event.getClass().getSimpleName(), e);
            }
        }
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }
}
