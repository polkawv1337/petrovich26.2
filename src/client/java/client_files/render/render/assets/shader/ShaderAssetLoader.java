package client_files.render.render.assets.shader;

import client_files.render.render.assets.AssetLoadException;
import client_files.render.render.assets.AssetLocation;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class ShaderAssetLoader {
    private ShaderAssetLoader() {}

    public static ShaderAsset load(AssetLocation id) {
        if (id == null) throw new IllegalArgumentException("AssetLocation cannot be null");

        String resourcePath = "/assets/" + id.namespace() + "/shaders/" + id.path();
        try (InputStream stream = ShaderAssetLoader.class.getResourceAsStream(resourcePath)) {
            InputStream effectiveStream = stream;
            if (effectiveStream == null) {
                effectiveStream = ShaderAssetLoader.class.getClassLoader().getResourceAsStream("assets/" + id.namespace() + "/shaders/" + id.path());
            }
            if (effectiveStream == null) {
                throw new AssetLoadException(id, "Shader resource not found at " + resourcePath);
            }

            String source = new String(effectiveStream.readAllBytes(), StandardCharsets.UTF_8);
            ShaderSourceType type = inferType(id.path());
            return new ShaderAsset(id, source, type);
        } catch (AssetLoadException ale) {
            throw ale;
        } catch (Exception e) {
            throw new AssetLoadException(id, "Failed to load shader resource", e);
        }
    }

    private static ShaderSourceType inferType(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".vsh") || lower.endsWith(".vert")) {
            return ShaderSourceType.VERTEX;
        } else if (lower.endsWith(".comp")) {
            return ShaderSourceType.COMPUTE;
        }
        return ShaderSourceType.FRAGMENT;
    }
}
