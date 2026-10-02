package client_files.render.render.assets.img;

import client_files.render.render.assets.AssetLocation;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TextureRegistry {
    private static final Map<AssetLocation, TextureAsset> REGISTRY = new ConcurrentHashMap<>();

    private TextureRegistry() {}

    public static void register(AssetLocation id, TextureAsset texture) {
        if (id != null && texture != null) {
            REGISTRY.put(id, texture);
        }
    }

    public static TextureAsset get(AssetLocation id) {
        return REGISTRY.get(id);
    }

    public static Map<AssetLocation, TextureAsset> getAll() {
        return Collections.unmodifiableMap(REGISTRY);
    }
}
