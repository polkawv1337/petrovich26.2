package client_files.render.render.assets;

import client_files.render.render.assets.img.TextureAsset;

public interface AnimatedMediaPlayer {
    void play();
    void pause();
    void seek(float seconds);
    boolean isPlaying();
    float currentTime();
    float duration();
    boolean isLooping();
    void setLooping(boolean loop);
    TextureAsset currentFrameTexture();
}
