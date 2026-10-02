package client_files.render.render.event;

import client_files.render.render.event.util.ListenerList;
import client_files.render.render.event.util.SubscribeEvent;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class EventManager {
    private static final Map<Class<?>, ListenerList> LISTENERS = new ConcurrentHashMap<>();

    private EventManager() {}

    public static void register(Object listenerOwner) {
        if (listenerOwner == null) return;
        for (Method method : listenerOwner.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(SubscribeEvent.class)) {
                Class<?>[] params = method.getParameterTypes();
                if (params.length == 1 && Event.class.isAssignableFrom(params[0])) {
                    SubscribeEvent sub = method.getAnnotation(SubscribeEvent.class);
                    LISTENERS.computeIfAbsent(params[0], k -> new ListenerList())
                            .add(listenerOwner, method, sub.priority(), sub.receiveCancelled());
                }
            }
        }
    }

    public static void unregister(Object listenerOwner) {
        if (listenerOwner == null) return;
        LISTENERS.values().forEach(list -> list.remove(listenerOwner));
    }

    public static boolean hasListeners(Class<?> eventType) {
        ListenerList list = LISTENERS.get(eventType);
        return list != null && !list.isEmpty();
    }

    public static void call(Object event) {
        if (event == null) return;
        ListenerList list = LISTENERS.get(event.getClass());
        if (list != null) {
            list.invoke(event);
        }
    }
}
