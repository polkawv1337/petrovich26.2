package client_files.render.render.core.font.msdf;

import java.util.Map;
import java.util.function.BiConsumer;

public final class MsdfFont {
    private final String family;
    private final Map<Character, MsdfGlyph> glyphs;
    private final Map<Integer, Map<Integer, Float>> kernings;
    private final float ascender;
    private final float descender;
    private final float lineHeight;
    private final float baseSize;

    public MsdfFont(String family, Map<Character, MsdfGlyph> glyphs,
                    float ascender, float descender, float lineHeight, float baseSize) {
        this(family, glyphs, ascender, descender, lineHeight, baseSize, null);
    }

    public MsdfFont(String family, Map<Character, MsdfGlyph> glyphs,
                    float ascender, float descender, float lineHeight, float baseSize,
                    Map<Integer, Map<Integer, Float>> kernings) {
        this.family = family != null ? family : "Inter";
        this.glyphs = glyphs;
        this.kernings = kernings;
        this.ascender = ascender;
        this.descender = descender;
        this.lineHeight = lineHeight;
        this.baseSize = baseSize > 0 ? baseSize : MsdfAtlasGenerator.BASE_SIZE;
    }

    public String getFamily() {
        return family;
    }

    public float getBaseSize() {
        return baseSize;
    }

    public float getAscender() {
        return ascender;
    }

    public float getDescender() {
        return descender;
    }

    public float getLineHeight() {
        return lineHeight;
    }

    public MsdfGlyph glyphFor(char c) {
        MsdfGlyph g = glyphs.get(c);
        if (g != null) return g;
        MsdfGlyph q = glyphs.get('?');
        if (q != null) return q;
        return new MsdfGlyph(0, 0, 0, 0, baseSize * 0.25f, 0, 0);
    }

    /** Есть ли в атласе пары кернинга - для TTF-атласов они не собираются. */
    public boolean hasKerning() {
        return kernings != null && !kernings.isEmpty();
    }

    public boolean hasGlyph(char c) {
        return glyphs.containsKey(c);
    }

    public int glyphCount() {
        return glyphs.size();
    }

    public void forEachGlyph(BiConsumer<Character, MsdfGlyph> action) {
        for (Map.Entry<Character, MsdfGlyph> e : glyphs.entrySet()) {
            action.accept(e.getKey(), e.getValue());
        }
    }

    public float kerning(int left, int right) {
        if (kernings == null || left < 0) return 0f;
        Map<Integer, Float> row = kernings.get(left);
        if (row == null) return 0f;
        Float v = row.get(right);
        return v != null ? v : 0f;
    }

    public float widthOf(String text, float size) {
        if (text == null || text.isEmpty()) return 0.0f;
        float scale = size / baseSize;
        float total = 0.0f;
        int prev = -1;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '§' && i + 1 < text.length()) {
                i++;
                continue;
            }
            if (kernings != null) {
                total += kerning(prev, c) * scale;
            }
            total += glyphFor(c).advance() * scale;
            prev = c;
        }
        return total;
    }
}