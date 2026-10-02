package client_files.render.render.assets.img;

import client_files.render.render.assets.AssetLocation;

public class DynamicTexture extends TextureAsset {
    private int dynamicWidth;
    private int dynamicHeight;

    public DynamicTexture(AssetLocation id, int width, int height) {
        super(id, 0, width, height);
        this.dynamicWidth = width;
        this.dynamicHeight = height;
    }

    public void upload(int[] argbPixels, int width, int height) {
        this.dynamicWidth = width;
        this.dynamicHeight = height;

    }

    @Override
    public int getWidth() {
        return dynamicWidth;
    }

    @Override
    public int getHeight() {
        return dynamicHeight;
    }
}
