package client_files.render.render.assets;

import java.util.Objects;

public record AssetLocation(String namespace, String path) {
    public AssetLocation {
        Objects.requireNonNull(namespace, "namespace cannot be null");
        Objects.requireNonNull(path, "path cannot be null");
    }

    public static AssetLocation of(String namespace, String path) {
        return new AssetLocation(namespace, path);
    }

    public static AssetLocation parse(String combined) {
        if (combined == null) throw new IllegalArgumentException("Asset location string cannot be null");
        int colon = combined.indexOf(':');
        if (colon < 0) {
            return new AssetLocation("minecraft", combined);
        }
        return new AssetLocation(combined.substring(0, colon), combined.substring(colon + 1));
    }

    @Override
    public String toString() {
        return namespace + ":" + path;
    }
}
