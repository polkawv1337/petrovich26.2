package client_files.render.render.assets.font;

import client_files.render.render.assets.AssetLocation;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class FontRegistry {
    private static final Map<AssetLocation, FontAsset> REGISTRY = new ConcurrentHashMap<>();

    private FontRegistry() {}

    public static void register(AssetLocation id, FontAsset asset) {
        if (id != null && asset != null) {
            REGISTRY.put(id, asset);
        }
    }

    public static FontAsset get(AssetLocation id) {
        return REGISTRY.get(id);
    }

    public static Map<AssetLocation, FontAsset> getAll() {
        return Collections.unmodifiableMap(REGISTRY);
    }
}
