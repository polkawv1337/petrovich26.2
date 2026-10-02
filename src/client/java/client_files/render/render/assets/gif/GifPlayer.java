package client_files.render.render.assets.gif;

import client_files.render.render.assets.AnimatedMediaPlayer;
import client_files.render.render.assets.img.TextureAsset;

import java.util.List;

public class GifPlayer implements AnimatedMediaPlayer {
    private final GifAsset asset;
    private boolean playing = false;
    private boolean looping = true;
    private int currentFrameIndex = 0;
    private float currentFrameElapsedMs = 0f;
    private float totalElapsedSeconds = 0f;

    public GifPlayer(GifAsset asset) {
        this.asset = asset;
        if (asset != null) {
            this.looping = asset.isLoop();
        }
    }

    public void tick(float deltaSeconds) {
        if (!playing || asset == null) return;
        List<GifFrame> frames = asset.getFrames();
        if (frames.isEmpty()) return;

        totalElapsedSeconds += deltaSeconds;
        float deltaMs = deltaSeconds * 1000f;
        currentFrameElapsedMs += deltaMs;

        GifFrame current = frames.get(currentFrameIndex);
        while (currentFrameElapsedMs >= current.delayMs()) {
            currentFrameElapsedMs -= current.delayMs();
            currentFrameIndex++;
            if (currentFrameIndex >= frames.size()) {
                if (looping) {
                    currentFrameIndex = 0;
                } else {
                    currentFrameIndex = frames.size() - 1;
                    playing = false;
                    break;
                }
            }
            current = frames.get(currentFrameIndex);
        }
    }

    @Override
    public void play() { playing = true; }

    @Override
    public void pause() { playing = false; }

    @Override
    public void seek(float seconds) {
        this.totalElapsedSeconds = Math.max(0f, seconds);

        if (asset == null || asset.getFrames().isEmpty()) return;
        float targetMs = seconds * 1000f;
        float accum = 0f;
        List<GifFrame> frames = asset.getFrames();
        for (int i = 0; i < frames.size(); i++) {
            accum += frames.get(i).delayMs();
            if (accum >= targetMs) {
                currentFrameIndex = i;
                currentFrameElapsedMs = 0f;
                return;
            }
        }
        currentFrameIndex = frames.size() - 1;
    }

    @Override
    public boolean isPlaying() { return playing; }

    @Override
    public float currentTime() { return totalElapsedSeconds; }

    @Override
    public float duration() { return asset != null ? asset.totalDurationSeconds() : 0f; }

    @Override
    public boolean isLooping() { return looping; }

    @Override
    public void setLooping(boolean loop) { this.looping = loop; }

    @Override
    public TextureAsset currentFrameTexture() {
        if (asset == null || asset.getFrames().isEmpty()) return null;
        return asset.getFrames().get(currentFrameIndex).texture();
    }
}
