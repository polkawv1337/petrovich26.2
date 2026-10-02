package client_files.render.render.assets.vid;

import client_files.render.render.assets.AnimatedMediaPlayer;
import client_files.render.render.assets.img.TextureAsset;

public class VideoPlayer implements AnimatedMediaPlayer {
    private final VideoAsset asset;
    private final VideoFrameBuffer frameBuffer;
    private boolean playing = false;
    private boolean looping = false;
    private float currentTime = 0f;
    private TextureAsset currentFrame;

    public VideoPlayer(VideoAsset asset) {
        this.asset = asset;
        this.frameBuffer = new VideoFrameBuffer(4);
    }

    public void tick(float deltaSeconds) {
        if (!playing || asset == null) return;
        currentTime += deltaSeconds;
        float duration = asset.getDurationSeconds();
        if (currentTime >= duration) {
            if (looping) {
                currentTime %= duration;
            } else {
                currentTime = duration;
                playing = false;
            }
        }
        TextureAsset next = frameBuffer.pollNextFrame();
        if (next != null) {
            currentFrame = next;
        }
    }

    @Override
    public void play() { playing = true; }

    @Override
    public void pause() { playing = false; }

    @Override
    public void seek(float seconds) {
        currentTime = Math.clamp(seconds, 0f, duration());
    }

    @Override
    public boolean isPlaying() { return playing; }

    @Override
    public float currentTime() { return currentTime; }

    @Override
    public float duration() { return asset != null ? asset.getDurationSeconds() : 0f; }

    @Override
    public boolean isLooping() { return looping; }

    @Override
    public void setLooping(boolean loop) { this.looping = loop; }

    @Override
    public TextureAsset currentFrameTexture() { return currentFrame; }
}
