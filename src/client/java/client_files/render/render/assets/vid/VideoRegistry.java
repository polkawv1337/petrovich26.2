package client_files.render.render.assets.vid;

import client_files.render.render.assets.AssetLocation;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class VideoRegistry {
    private static final Map<AssetLocation, VideoPlayer> REGISTRY = new ConcurrentHashMap<>();

    private VideoRegistry() {}

    public static void register(AssetLocation id, VideoPlayer player) {
        if (id != null && player != null) {
            REGISTRY.put(id, player);
        }
    }

    public static VideoPlayer get(AssetLocation id) {
        return REGISTRY.get(id);
    }

    public static Map<AssetLocation, VideoPlayer> getAll() {
        return Collections.unmodifiableMap(REGISTRY);
    }
}
