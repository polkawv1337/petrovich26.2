package client_files.render.render.assets.img;

import client_files.render.render.assets.AssetLocation;

import java.util.Objects;

public class TextureAsset {
    public record SubRegion(float u0, float v0, float u1, float v1) {}

    protected final AssetLocation id;
    protected final int glTextureId;
    protected final int width;
    protected final int height;

    public TextureAsset(AssetLocation id, int glTextureId, int width, int height) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.glTextureId = glTextureId;
        this.width = width;
        this.height = height;
    }

    public AssetLocation getId() { return id; }
    public int getGlTextureId() { return glTextureId; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public SubRegion getFullRegion() {
        return new SubRegion(0f, 0f, 1f, 1f);
    }
}
