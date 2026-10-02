package client_files.render.render.assets.gif;

import client_files.render.render.assets.AssetLoadException;
import client_files.render.render.assets.AssetLocation;
import client_files.render.render.assets.img.TextureAsset;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public final class GifAssetLoader {
    private GifAssetLoader() {}

    public static GifAsset load(AssetLocation id) {
        if (id == null) throw new IllegalArgumentException("AssetLocation cannot be null");

        String resourcePath = "/assets/" + id.namespace() + "/gif/" + id.path();
        if (!resourcePath.endsWith(".gif")) resourcePath += ".gif";

        try (InputStream stream = GifAssetLoader.class.getResourceAsStream(resourcePath)) {
            InputStream effectiveStream = stream;
            if (effectiveStream == null) {
                effectiveStream = GifAssetLoader.class.getClassLoader().getResourceAsStream("assets/" + id.namespace() + "/gif/" + id.path() + (id.path().endsWith(".gif") ? "" : ".gif"));
            }
            if (effectiveStream == null) {
                throw new AssetLoadException(id, "GIF resource not found at " + resourcePath);
            }

            TextureAsset dummy = new TextureAsset(id, 0, 64, 64);
            List<GifFrame> frames = List.of(new GifFrame(dummy, 100, GifFrame.Disposal.NONE));
            return new GifAsset(id, frames, true);
        } catch (AssetLoadException ale) {
            throw ale;
        } catch (Exception e) {
            throw new AssetLoadException(id, "Failed to load GIF resource", e);
        }
    }
}
