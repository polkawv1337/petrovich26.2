package client_files.render.render.assets;

import client_files.render.render.assets.font.FontAsset;
import client_files.render.render.assets.font.FontAssetLoader;
import client_files.render.render.assets.gif.GifAsset;
import client_files.render.render.assets.gif.GifAssetLoader;
import client_files.render.render.assets.img.TextureAsset;
import client_files.render.render.assets.img.TextureAssetLoader;
import client_files.render.render.assets.shader.ShaderAsset;
import client_files.render.render.assets.shader.ShaderAssetLoader;
import client_files.render.render.assets.vid.VideoAsset;
import client_files.render.render.assets.vid.VideoAssetLoader;

public final class AssetsLoader {
    private static final AssetCache<FontAsset> FONT_CACHE = new AssetCache<>();
    private static final AssetCache<TextureAsset> TEXTURE_CACHE = new AssetCache<>();
    private static final AssetCache<ShaderAsset> SHADER_CACHE = new AssetCache<>();
    private static final AssetCache<VideoAsset> VIDEO_CACHE = new AssetCache<>();
    private static final AssetCache<GifAsset> GIF_CACHE = new AssetCache<>();

    private AssetsLoader() {}

    public static FontAsset font(AssetLocation id) {
        return FONT_CACHE.getOrLoad(id, FontAssetLoader::load);
    }

    public static TextureAsset texture(AssetLocation id) {
        return TEXTURE_CACHE.getOrLoad(id, TextureAssetLoader::load);
    }

    public static ShaderAsset shader(AssetLocation id) {
        return SHADER_CACHE.getOrLoad(id, ShaderAssetLoader::load);
    }

    public static VideoAsset video(AssetLocation id) {
        return VIDEO_CACHE.getOrLoad(id, VideoAssetLoader::load);
    }

    public static GifAsset gif(AssetLocation id) {
        return GIF_CACHE.getOrLoad(id, GifAssetLoader::load);
    }

    public static void unloadAll() {
        FONT_CACHE.unloadAll();
        TEXTURE_CACHE.unloadAll();
        SHADER_CACHE.unloadAll();
        VIDEO_CACHE.unloadAll();
        GIF_CACHE.unloadAll();
    }
}
