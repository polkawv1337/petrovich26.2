package client_files.render.render.assets.vid;

import client_files.render.render.assets.AssetLocation;

public final class VideoAssetLoader {
    private VideoAssetLoader() {}

    public static VideoAsset load(AssetLocation id) {
        if (id == null) throw new IllegalArgumentException("AssetLocation cannot be null");
        String path = "assets/" + id.namespace() + "/videos/" + id.path();
        return new VideoAsset(id, path, 10.0f, 30, 1920, 1080);
    }
}
