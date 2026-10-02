package client_files.render.render.event;

import net.minecraft.client.Minecraft;

public abstract class Event {
    private final Minecraft client;
    private final long timestampNanos;

    public Event(Minecraft client) {
        this.client = client;
        this.timestampNanos = System.nanoTime();
    }

    public Minecraft getClient() {
        return client;
    }

    public long getTimestampNanos() {
        return timestampNanos;
    }
}
