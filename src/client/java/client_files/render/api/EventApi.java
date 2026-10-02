package client_files.render.api;

import client_files.render.render.event.EventManager;

public final class EventApi {
    EventApi() {}

    public void subscribe(Object listenerOwner) {
        EventManager.register(listenerOwner);
    }

    public void unsubscribe(Object listenerOwner) {
        EventManager.unregister(listenerOwner);
    }
}
