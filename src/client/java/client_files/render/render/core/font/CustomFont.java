package client_files.render.render.core.font;

public final class CustomFont {
    private final GlyphAtlas atlas;
    private final String family;
    private final float baseSize;

    public CustomFont(GlyphAtlas atlas, String family, float baseSize) {
        this.atlas = atlas;
        this.family = family != null ? family : "SansSerif";
        this.baseSize = baseSize > 0 ? baseSize : 16f;
    }

    public GlyphAtlas getAtlas() {
        return atlas;
    }

    public String getFamily() {
        return family;
    }

    public float getBaseSize() {
        return baseSize;
    }

    public float widthOf(String text, float size) {
        if (text == null || text.isEmpty()) return 0f;
        if (FontManager.customFonts()) {
            try {
                String fam = (family != null && !family.equals("Minecraft") && !family.equals("SansSerif"))
                        ? family
                        : FontManager.customFontFamily();
                return client_files.render.render.core.font.msdf.MsdfFontRenderer.get()
                        .getStringWidth(fam, text, size);
            } catch (Throwable ignored) {
                return text.length() * size * 0.5f;
            }
        }
        net.minecraft.client.gui.Font mcFont = net.minecraft.client.Minecraft.getInstance().font;
        if (mcFont != null) {
            return mcFont.width(text);
        }
        float scale = size / baseSize;
        float totalWidth = 0f;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            GlyphAtlas.GlyphInfo glyph = atlas.glyphFor(c);
            totalWidth += glyph.advanceWidth() * scale;
        }
        return totalWidth;
    }

    public float getStringWidth(String text, float size) {
        return widthOf(text, size);
    }

    public float heightOf(float size) {
        return size;
    }
}
