package client_files.render.render.assets.img;

import com.mojang.blaze3d.platform.NativeImage;
import client_files.render.render.assets.AssetLoadException;
import client_files.render.render.assets.AssetLocation;

import java.io.InputStream;

public final class TextureAssetLoader {
    private TextureAssetLoader() {}

    public static TextureAsset load(AssetLocation id) {
        if (id == null) throw new IllegalArgumentException("AssetLocation cannot be null");

        String resourcePath = "/assets/" + id.namespace() + "/textures/" + id.path();
        if (!resourcePath.endsWith(".png")) resourcePath += ".png";

        try (InputStream stream = TextureAssetLoader.class.getResourceAsStream(resourcePath)) {
            InputStream effectiveStream = stream;
            if (effectiveStream == null) {
                effectiveStream = TextureAssetLoader.class.getClassLoader().getResourceAsStream("assets/" + id.namespace() + "/textures/" + id.path() + (id.path().endsWith(".png") ? "" : ".png"));
            }
            if (effectiveStream == null) {
                throw new AssetLoadException(id, "Texture resource not found at " + resourcePath);
            }

            try (NativeImage nativeImage = NativeImage.read(effectiveStream)) {
                int width = nativeImage.getWidth();
                int height = nativeImage.getHeight();
                return new TextureAsset(id, 0, width, height);
            }
        } catch (AssetLoadException ale) {
            throw ale;
        } catch (Exception e) {
            throw new AssetLoadException(id, "Failed to load/decode texture", e);
        }
    }
}
