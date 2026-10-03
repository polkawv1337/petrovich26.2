package client_files.render.render.core.font.msdf;

import client_files.render.render.core.color.ColorRGBA;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MsdfFontRenderer {
    private static final Logger LOGGER = LoggerFactory.getLogger(MsdfFontRenderer.class);

    private static final MsdfFontRenderer INSTANCE = new MsdfFontRenderer();

    /** Семейство + текстура + размеры атласа, к которым привязан конкретный глиф. */
    private record Face(MsdfFont font, Identifier texture, int texWidth, int texHeight) {}

    private MsdfFontRenderer() {}

    public static MsdfFontRenderer get() {
        return INSTANCE;
    }

    public float getStringWidth(String family, String text, float size) {
        if (text == null || text.isEmpty()) return 0.0f;
        Face face = face(family);
        if (face == null) return text.length() * size * 0.5f;

        float scale = size / face.font().getBaseSize();
        float total = 0f;
        int prev = -1;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '§' && i + 1 < text.length()) {
                i++;
                continue;
            }
            Face f = faceFor(c, face);
            if (f.font().hasKerning()) {
                total += f.font().kerning(prev, c) * scale;
            }
            total += f.font().glyphFor(c).advance() * scale;
            prev = c;
        }
        return total;
    }

    public void drawString(GuiGraphicsExtractor extractor, String family, String text,
                           float x, float y, float size, ColorRGBA color, boolean shadow) {
        if (text == null || text.isEmpty() || extractor == null) return;
        if (shadow) {
            ColorRGBA shadowCol = new ColorRGBA(0, 0, 0, Math.round(color.a() * 0.6f));
            float off = Math.max(1.0f, size * 0.08f);
            renderInternal(extractor, family, text, x + off, y + off, size, shadowCol);
        }
        renderInternal(extractor, family, text, x, y, size, color);
    }

    private void renderInternal(GuiGraphicsExtractor extractor, String family, String text,
                                float x, float y, float size, ColorRGBA color) {
        Face base = face(family);
        if (base == null) return;

        float unit = guiPixelUnit();
        float penX = x;
        int defaultCol = color.packed();
        int activeCol = defaultCol;
        int prev = -1;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '§' && i + 1 < text.length()) {
                activeCol = parseMinecraftColor(Character.toLowerCase(text.charAt(i + 1)), defaultCol);
                i++;
                prev = -1;
                continue;
            }

            Face f = faceFor(c, base);
            MsdfFont font = f.font();
            float scale = size / font.getBaseSize();

            if (font.hasKerning()) {
                penX += font.kerning(prev, c) * scale;
            }

            MsdfGlyph g = font.glyphFor(c);
            if (g.w() > 0 && g.h() > 0 && c != ' ' && f.texture() != null) {
                float dx = penX + g.offsetX() * scale;
                float dy = y + font.getAscender() * scale + g.offsetY() * scale;

                float x0 = snap(dx, unit);
                float y0 = snap(dy, unit);
                float x1 = snap(dx + g.w() * scale, unit);
                float y1 = snap(dy + g.h() * scale, unit);
                float dw = Math.max(x1 - x0, unit);
                float dh = Math.max(y1 - y0, unit);

                var pose = extractor.pose();
                pose.pushMatrix();
                pose.translate(x0, y0);
                pose.scale(dw / g.w(), dh / g.h());
                extractor.blit(
                        RenderPipelines.GUI_TEXTURED,
                        f.texture(),
                        0,
                        0,
                        (float) g.u(),
                        (float) g.v(),
                        g.w(),
                        g.h(),
                        g.w(),
                        g.h(),
                        f.texWidth(),
                        f.texHeight(),
                        activeCol
                );
                pose.popMatrix();
            }
            penX += g.advance() * scale;
            prev = c;
        }
    }

    private static float snap(float value, float unit) {
        return Math.round(value / unit) * unit;
    }

    private static float guiPixelUnit() {
        try {
            int scale = Minecraft.getInstance().getWindow().getGuiScale();
            if (scale > 0) return 1.0f / scale;
        } catch (Throwable ignored) {
        }
        return 1.0f;
    }

    private Face face(String family) {
        if (family == null) return null;
        try {
            MsdfFont font = MsdfFontCache.get().font(family);
            if (font == null) return null;
            Identifier tex = MsdfFontCache.get().textureId(family);
            if (tex == null) return null;
            return new Face(font, tex,
                    MsdfFontCache.get().atlasWidth(family),
                    MsdfFontCache.get().atlasHeight(family));
        } catch (Throwable t) {
            LOGGER.warn("[Render] Could not resolve font '{}': {}", family, t.getMessage());
            return null;
        }
    }

    /**
     * Иконочные символы Private Use Area отсутствуют в основном шрифте, поэтому
     * для них подтягивается испечённый атлас ({@link BakedAtlasFonts}).
     */
    private Face faceFor(char c, Face base) {
        if (base.font().hasGlyph(c)) return base;
        String iconFamily = BakedAtlasFonts.familyForChar(c);
        if (iconFamily == null) return base;
        Face icon = face(iconFamily);
        return icon != null ? icon : base;
    }

    private static int parseMinecraftColor(char code, int defaultCol) {
        int a = defaultCol & 0xFF000000;
        int rgb = switch (code) {
            case '0' -> 0x000000;
            case '1' -> 0x0000AA;
            case '2' -> 0x00AA00;
            case '3' -> 0x00AAAA;
            case '4' -> 0xAA0000;
            case '5' -> 0xAA00AA;
            case '6' -> 0xFFAA00;
            case '7' -> 0xAAAAAA;
            case '8' -> 0x555555;
            case '9' -> 0x5555FF;
            case 'a' -> 0x55FF55;
            case 'b' -> 0x55FFFF;
            case 'c' -> 0xFF5555;
            case 'd' -> 0xFF55FF;
            case 'e' -> 0xFFFF55;
            case 'f' -> 0xFFFFFF;
            case 'r' -> defaultCol & 0x00FFFFFF;
            default -> defaultCol & 0x00FFFFFF;
        };
        return a | rgb;
    }
}