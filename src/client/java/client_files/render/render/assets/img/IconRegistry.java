package client_files.render.render.assets.img;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class IconRegistry {
    private static final Map<String, TextureAsset> ICONS = new ConcurrentHashMap<>();

    private IconRegistry() {}

    public static void register(String key, TextureAsset icon) {
        if (key != null && icon != null) {
            ICONS.put(key, icon);
        }
    }

    public static TextureAsset get(String key) {
        return ICONS.get(key);
    }

    public static Map<String, TextureAsset> getAll() {
        return Collections.unmodifiableMap(ICONS);
    }
}
