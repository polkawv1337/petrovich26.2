package client_files.render.render.assets;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class AssetCache<T> {
    private final Map<AssetLocation, T> cache = new ConcurrentHashMap<>();

    public T getOrLoad(AssetLocation id, Function<AssetLocation, T> loader) {
        if (id == null) return null;
        return cache.computeIfAbsent(id, loader);
    }

    public void unload(AssetLocation id) {
        if (id == null) return;
        T item = cache.remove(id);
        closeIfPossible(item);
    }

    public void unloadAll() {
        for (T item : cache.values()) {
            closeIfPossible(item);
        }
        cache.clear();
    }

    private void closeIfPossible(Object item) {
        if (item instanceof AutoCloseable closeable) {
            try {
                closeable.close();
            } catch (Exception ignored) {}
        }
    }
}
