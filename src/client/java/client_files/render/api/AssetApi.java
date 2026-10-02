package client_files.render.api;

import client_files.render.render.assets.AssetLocation;
import client_files.render.render.assets.AssetsLoader;
import client_files.render.render.assets.font.FontAsset;
import client_files.render.render.assets.gif.GifAsset;
import client_files.render.render.assets.img.TextureAsset;
import client_files.render.render.assets.shader.ShaderAsset;
import client_files.render.render.assets.vid.VideoAsset;

public final class AssetApi {
    AssetApi() {}

    public FontAsset font(AssetLocation id) { return AssetsLoader.font(id); }
    public FontAsset font(String location) { return AssetsLoader.font(AssetLocation.parse(location)); }

    public TextureAsset texture(AssetLocation id) { return AssetsLoader.texture(id); }
    public TextureAsset texture(String location) { return AssetsLoader.texture(AssetLocation.parse(location)); }

    public ShaderAsset shader(AssetLocation id) { return AssetsLoader.shader(id); }
    public ShaderAsset shader(String location) { return AssetsLoader.shader(AssetLocation.parse(location)); }

    public VideoAsset video(AssetLocation id) { return AssetsLoader.video(id); }
    public VideoAsset video(String location) { return AssetsLoader.video(AssetLocation.parse(location)); }

    public GifAsset gif(AssetLocation id) { return AssetsLoader.gif(id); }
    public GifAsset gif(String location) { return AssetsLoader.gif(AssetLocation.parse(location)); }
}
