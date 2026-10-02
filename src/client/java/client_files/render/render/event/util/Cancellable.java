package client_files.render.render.event.util;

public interface Cancellable {
    boolean isCancelled();
    void setCancelled(boolean cancelled);
}
