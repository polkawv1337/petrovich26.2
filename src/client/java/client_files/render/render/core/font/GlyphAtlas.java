package client_files.render.render.core.font;

import client_files.render.render.assets.img.TextureAsset;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class GlyphAtlas {
    public record GlyphInfo(
            float u0,
            float v0,
            float u1,
            float v1,
            float advanceWidth,
            float width,
            float height,
            float offsetX,
            float offsetY
    ) {}

    private final TextureAsset backingTexture;
    private final Map<Character, GlyphInfo> glyphs;
    private final boolean isMsdf;
    private final GlyphInfo fallbackGlyph;

    public GlyphAtlas(TextureAsset backingTexture, Map<Character, GlyphInfo> glyphs, boolean isMsdf) {
        this.backingTexture = backingTexture;
        this.glyphs = new HashMap<>(glyphs != null ? glyphs : Collections.emptyMap());
        this.isMsdf = isMsdf;
        this.fallbackGlyph = this.glyphs.getOrDefault('?', new GlyphInfo(0f, 0f, 1f, 1f, 8f, 8f, 10f, 0f, 0f));
    }

    public TextureAsset getBackingTexture() {
        return backingTexture;
    }

    public boolean isMsdf() {
        return isMsdf;
    }

    public GlyphInfo glyphFor(char c) {
        GlyphInfo info = glyphs.get(c);
        return info != null ? info : fallbackGlyph;
    }

    public Map<Character, GlyphInfo> getGlyphs() {
        return Collections.unmodifiableMap(glyphs);
    }
}
