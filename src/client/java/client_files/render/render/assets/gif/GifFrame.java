package client_files.render.render.assets.gif;

import client_files.render.render.assets.img.TextureAsset;

public record GifFrame(TextureAsset texture, int delayMs, Disposal disposal) {
    public enum Disposal {
        NONE,
        BACKGROUND,
        PREVIOUS
    }

    public GifFrame {
        if (delayMs <= 0) delayMs = 100;
        if (disposal == null) disposal = Disposal.NONE;
    }
}
