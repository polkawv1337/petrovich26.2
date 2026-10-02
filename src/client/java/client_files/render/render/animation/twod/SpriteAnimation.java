package client_files.render.render.animation.twod;

import client_files.render.render.assets.img.TextureAsset;

public class SpriteAnimation {
    private final TextureAsset texture;
    private final int frameWidth;
    private final int frameHeight;
    private final int frameCount;
    private final int fps;
    private final boolean looping;

    public SpriteAnimation(TextureAsset texture, int frameWidth, int frameHeight, int frameCount, int fps, boolean looping) {
        this.texture = texture;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.frameCount = frameCount;
        this.fps = Math.max(1, fps);
        this.looping = looping;
    }

    public TextureAsset.SubRegion currentFrame(float elapsedSeconds) {
        if (texture == null || frameCount <= 0) {
            return new TextureAsset.SubRegion(0f, 0f, 1f, 1f);
        }

        int frame = (int) (elapsedSeconds * fps);
        if (looping) {
            frame = frame % frameCount;
        } else {
            frame = Math.min(frame, frameCount - 1);
        }

        int texWidth = texture.getWidth() > 0 ? texture.getWidth() : frameWidth * frameCount;
        int texHeight = texture.getHeight() > 0 ? texture.getHeight() : frameHeight;

        int cols = Math.max(1, texWidth / frameWidth);
        int col = frame % cols;
        int row = frame / cols;

        float u0 = (float) (col * frameWidth) / texWidth;
        float v0 = (float) (row * frameHeight) / texHeight;
        float u1 = (float) ((col + 1) * frameWidth) / texWidth;
        float v1 = (float) ((row + 1) * frameHeight) / texHeight;

        return new TextureAsset.SubRegion(u0, v0, u1, v1);
    }

    public TextureAsset getTexture() { return texture; }
    public int getFrameWidth() { return frameWidth; }
    public int getFrameHeight() { return frameHeight; }
    public int getFrameCount() { return frameCount; }
    public int getFps() { return fps; }
}
