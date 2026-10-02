package client_files.render.render.assets.shader;

import client_files.render.render.assets.AssetLocation;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ShaderRegistry {
    private static final Map<AssetLocation, ShaderAsset> REGISTRY = new ConcurrentHashMap<>();

    private ShaderRegistry() {}

    public static void register(AssetLocation id, ShaderAsset shader) {
        if (id != null && shader != null) {
            REGISTRY.put(id, shader);
        }
    }

    public static ShaderAsset get(AssetLocation id) {
        return REGISTRY.get(id);
    }

    public static Map<AssetLocation, ShaderAsset> getAll() {
        return Collections.unmodifiableMap(REGISTRY);
    }
}
