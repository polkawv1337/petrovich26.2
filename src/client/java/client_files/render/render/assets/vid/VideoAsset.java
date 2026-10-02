package client_files.render.render.assets.vid;

import client_files.render.render.assets.AssetLocation;

import java.util.Objects;

public final class VideoAsset {
    private final AssetLocation id;
    private final String filePath;
    private final float durationSeconds;
    private final int fps;
    private final int width;
    private final int height;

    public VideoAsset(AssetLocation id, String filePath, float durationSeconds, int fps, int width, int height) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.filePath = filePath;
        this.durationSeconds = Math.max(0.01f, durationSeconds);
        this.fps = Math.max(1, fps);
        this.width = width;
        this.height = height;
    }

    public AssetLocation getId() { return id; }
    public String getFilePath() { return filePath; }
    public float getDurationSeconds() { return durationSeconds; }
    public int getFps() { return fps; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
