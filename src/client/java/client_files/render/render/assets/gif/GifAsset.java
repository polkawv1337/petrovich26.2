package client_files.render.render.assets.gif;

import client_files.render.render.assets.AssetLocation;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class GifAsset {
    private final AssetLocation id;
    private final List<GifFrame> frames;
    private final boolean loop;

    public GifAsset(AssetLocation id, List<GifFrame> frames, boolean loop) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.frames = frames != null ? List.copyOf(frames) : Collections.emptyList();
        this.loop = loop;
    }

    public AssetLocation getId() { return id; }
    public List<GifFrame> getFrames() { return frames; }
    public boolean isLoop() { return loop; }

    public float totalDurationSeconds() {
        int totalMs = 0;
        for (GifFrame f : frames) {
            totalMs += f.delayMs();
        }
        return totalMs / 1000.0f;
    }
}
