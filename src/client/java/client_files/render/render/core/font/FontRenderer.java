package client_files.render.render.core.font;

import client_files.render.render.core.batching.BatchManager;
import client_files.render.render.core.batching.BatchedQuad;
import client_files.render.render.core.color.ColorRGBA;
import client_files.render.render.core.context.RenderContext;
import client_files.render.render.core.shader.Shaders;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class FontRenderer {
    private static final FontRenderer INSTANCE = new FontRenderer();
    private final BatchedQuad reusableQuad = new BatchedQuad();

    private FontRenderer() {}

    public static FontRenderer get() {
        return INSTANCE;
    }

    public static void render(CustomFont font, String text, float x, float y, TextStyle style, TextAlign align) {
        float size = (style != null && style.getSize() > 0) ? style.getSize() : 9.0f;
        ColorRGBA col = (style != null && style.getColor() != null) ? style.getColor() : ColorRGBA.WHITE;
        get().drawString(font, text, x, y, size, col, style, align);
    }

    public static void render(CustomFont font, String text, float x, float y, TextStyle style) {
        render(font, text, x, y, style, TextAlign.LEFT);
    }

    public void drawString(CustomFont font, String text, float x, float y, float size, ColorRGBA color, TextStyle style, TextAlign align) {
        if (!RenderContext.is2DActive()) {
            throw new IllegalStateException("Cannot draw text outside of an active 2D RenderContext");
        }
        if (text == null || text.isEmpty()) return;
        if (font == null) font = FontManager.get().vanilla();
        if (color == null) color = ColorRGBA.WHITE;
        if (style == null) style = TextStyle.DEFAULT;
        if (align == null) align = TextAlign.LEFT;

        GuiGraphicsExtractor extractor = RenderContext.current2DExtractor();
        if (extractor != null) {
            boolean useCustom = FontManager.customFonts();
            if (useCustom) {
                String family = (font != null && font != FontManager.get().vanilla() && font.getFamily() != null && !font.getFamily().equals("Minecraft"))
                        ? font.getFamily()
                        : FontManager.customFontFamily();
                float totalWidth = client_files.render.render.core.font.msdf.MsdfFontRenderer.get().getStringWidth(family, text, size);
                float startX = x;
                if (align == TextAlign.CENTER) {
                    startX -= totalWidth * 0.5f;
                } else if (align == TextAlign.RIGHT) {
                    startX -= totalWidth;
                }
                client_files.render.render.core.font.msdf.MsdfFontRenderer.get().drawString(extractor, family, text, startX, y, size, color, style.hasShadow());
                return;
            }

            net.minecraft.client.gui.Font mcFont = net.minecraft.client.Minecraft.getInstance().font;
            if (mcFont != null) {
                float totalWidth = mcFont.width(text);
                float startX = x;
                if (align == TextAlign.CENTER) {
                    startX -= totalWidth * 0.5f;
                } else if (align == TextAlign.RIGHT) {
                    startX -= totalWidth;
                }
                extractor.text(mcFont, text, Math.round(startX), Math.round(y), color.packed(), style.hasShadow());
                return;
            }
        }

        float totalWidth = font.widthOf(text, size);
        float startX = x;
        if (align == TextAlign.CENTER) {
            startX -= totalWidth * 0.5f;
        } else if (align == TextAlign.RIGHT) {
            startX -= totalWidth;
        }

        if (style.hasShadow()) {
            ColorRGBA shadowColor = ColorRGBA.of(0, 0, 0, Math.round(color.a() * 0.6f));
            renderLine(font, text, startX + 1f, y + 1f, size, shadowColor);
        }

        if (style.getOutlineColor() != null) {
            ColorRGBA outline = style.getOutlineColor();
            renderLine(font, text, startX - 1f, y, size, outline);
            renderLine(font, text, startX + 1f, y, size, outline);
            renderLine(font, text, startX, y - 1f, size, outline);
            renderLine(font, text, startX, y + 1f, size, outline);
        }

        renderLine(font, text, startX, y, size, color);

        float scale = size / font.getBaseSize();
        if (style.isUnderline()) {
            float lineY = y + size + 1f;
            drawDecorationLine(startX, lineY, totalWidth, scale, color);
        }
        if (style.isStrikethrough()) {
            float lineY = y + size * 0.5f;
            drawDecorationLine(startX, lineY, totalWidth, scale, color);
        }
    }

    private void renderLine(CustomFont font, String text, float startX, float startY, float size, ColorRGBA color) {
        float currentX = startX;
        float scale = size / font.getBaseSize();
        GlyphAtlas atlas = font.getAtlas();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            GlyphAtlas.GlyphInfo glyph = atlas.glyphFor(c);

            float gx = currentX + glyph.offsetX() * scale;
            float gy = startY + glyph.offsetY() * scale;
            float gw = glyph.width() * scale;
            float gh = glyph.height() * scale;

            if (gw > 0 && gh > 0) {
                reusableQuad.setRect(gx, gy, gw, gh);
                reusableQuad.setUVs(glyph.u0(), glyph.v0(), glyph.u1(), glyph.v0(), glyph.u1(), glyph.v1(), glyph.u0(), glyph.v1());
                reusableQuad.setColor(color);

                BatchManager.get().submit(Shaders.TEXTURE != null ? Shaders.TEXTURE.getPipeline() : null, reusableQuad);
            }

            currentX += glyph.advanceWidth() * scale;
        }
    }

    private void drawDecorationLine(float x, float y, float width, float thickness, ColorRGBA color) {
        reusableQuad.setRect(x, y, width, Math.max(1f, thickness));
        reusableQuad.setColor(color);
        BatchManager.get().submit(Shaders.ROUNDED_RECT != null ? Shaders.ROUNDED_RECT.getPipeline() : null, reusableQuad);
    }
}
