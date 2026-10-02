package client_files.render.render.assets.font;

import client_files.render.render.assets.AssetLoadException;
import client_files.render.render.assets.AssetLocation;

import java.io.InputStream;

public final class FontAssetLoader {
    private FontAssetLoader() {}

    public static FontAsset load(AssetLocation id) {
        if (id == null) throw new IllegalArgumentException("AssetLocation cannot be null");

        String resourcePath = "/assets/" + id.namespace() + "/font/" + id.path();
        try (InputStream stream = FontAssetLoader.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {

                try (InputStream stream2 = FontAssetLoader.class.getClassLoader().getResourceAsStream("assets/" + id.namespace() + "/font/" + id.path())) {
                    if (stream2 == null) {
                        throw new AssetLoadException(id, "Resource not found at " + resourcePath);
                    }
                    byte[] bytes = stream2.readAllBytes();
                    return createFontAsset(id, bytes);
                }
            }
            byte[] bytes = stream.readAllBytes();
            return createFontAsset(id, bytes);
        } catch (AssetLoadException ale) {
            throw ale;
        } catch (Exception e) {
            throw new AssetLoadException(id, "Failed to read font resource", e);
        }
    }

    private static FontAsset createFontAsset(AssetLocation id, byte[] bytes) {
        String lower = id.path().toLowerCase();
        FontAssetType type;
        if (lower.endsWith(".ttf")) {
            type = FontAssetType.TTF;
        } else if (lower.endsWith(".otf")) {
            type = FontAssetType.OTF;
        } else if (lower.endsWith(".json") || lower.contains("msdf")) {
            type = FontAssetType.MSDF;
        } else {
            type = FontAssetType.BITMAP;
        }

        String family = id.path();
        int slash = family.lastIndexOf('/');
        if (slash >= 0) family = family.substring(slash + 1);
        int dot = family.lastIndexOf('.');
        if (dot >= 0) family = family.substring(0, dot);

        return new FontAsset(id, bytes, type, family);
    }
}
