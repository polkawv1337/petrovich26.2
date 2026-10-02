package client_files.render.render.assets.gif;

import client_files.render.render.assets.AssetLocation;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class GifRegistry {
    private static final Map<AssetLocation, GifPlayer> REGISTRY = new ConcurrentHashMap<>();

    private GifRegistry() {}

    public static void register(AssetLocation id, GifPlayer player) {
        if (id != null && player != null) {
            REGISTRY.put(id, player);
        }
    }

    public static GifPlayer get(AssetLocation id) {
        return REGISTRY.get(id);
    }

    public static Map<AssetLocation, GifPlayer> getAll() {
        return Collections.unmodifiableMap(REGISTRY);
    }
}
