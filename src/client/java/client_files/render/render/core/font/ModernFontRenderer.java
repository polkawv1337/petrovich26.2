package client_files.render.render.core.font;

import com.mojang.blaze3d.platform.NativeImage;
import client_files.render.render.core.LinearDynamicTexture;
import client_files.render.render.core.color.ColorRGBA;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ModernFontRenderer {
    private static final ModernFontRenderer INSTANCE = new ModernFontRenderer();

    public record Glyph(int x, int y, int width, int height, float advance) {}

    public static final class FontData {
        public final String family;
        public final Identifier textureId;
        public final DynamicTexture texture;
        public final Map<Character, Glyph> glyphs;
        public final float baseSize;
        public final float fontHeight;
        public final int atlasWidth;
        public final int atlasHeight;

        public FontData(String family, Identifier textureId, DynamicTexture texture,
                        Map<Character, Glyph> glyphs, float baseSize, float fontHeight,
                        int atlasWidth, int atlasHeight) {
            this.family = family;
            this.textureId = textureId;
            this.texture = texture;
            this.glyphs = glyphs;
            this.baseSize = baseSize;
            this.fontHeight = fontHeight;
            this.atlasWidth = atlasWidth;
            this.atlasHeight = atlasHeight;
        }
    }

    private final Map<String, FontData> fontCache = new ConcurrentHashMap<>();
    private final float baseSize = 32.0f;
    private final int atlasWidth = 1024;
    private final int atlasHeight = 1024;

    private ModernFontRenderer() {}

    public static ModernFontRenderer get() {
        return INSTANCE;
    }

    public static String normalizeFamily(String family) {
        if (family == null || family.isBlank()) return "Inter";
        String f = family.trim().toLowerCase().replace(" ", "_").replace("-", "_");
        return switch (f) {
            case "roboto" -> "Roboto";
            case "poppins" -> "Poppins";
            case "montserrat" -> "Montserrat";
            case "opensans", "open_sans" -> "OpenSans";
            case "jetbrains_mono", "jetbrainsmono" -> "JetBrains Mono";
            case "firacode", "fira_code" -> "FiraCode";
            default -> "Inter";
        };
    }

    private static String familyToResource(String normalized) {
        return switch (normalized) {
            case "Roboto" -> "roboto.ttf";
            case "Poppins" -> "poppins.ttf";
            case "Montserrat" -> "montserrat.ttf";
            case "OpenSans" -> "opensans.ttf";
            case "JetBrains Mono" -> "jetbrains_mono.ttf";
            case "FiraCode" -> "firacode.ttf";
            default -> "inter.ttf";
        };
    }

    public FontData getFont(String family) {
        String norm = normalizeFamily(family);
        FontData existing = fontCache.get(norm);
        if (existing != null) return existing;
        return loadFont(norm);
    }

    private synchronized FontData loadFont(String normFamily) {
        FontData existing = fontCache.get(normFamily);
        if (existing != null) return existing;

        String resName = familyToResource(normFamily);
        Font awtFont = null;

        try (InputStream is = getClass().getResourceAsStream("/assets/Render/font/ttf/" + resName)) {
            if (is != null) {
                awtFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(Font.PLAIN, baseSize);
            }
        } catch (Throwable ignored) {}

        if (awtFont == null) {
            try (InputStream is2 = getClass().getClassLoader().getResourceAsStream("assets/Render/font/ttf/" + resName)) {
                if (is2 != null) {
                    awtFont = Font.createFont(Font.TRUETYPE_FONT, is2).deriveFont(Font.PLAIN, baseSize);
                }
            } catch (Throwable ignored) {}
        }

        if (awtFont == null) {
            awtFont = new Font("SansSerif", Font.PLAIN, (int) baseSize);
        }

        BufferedImage img = new BufferedImage(atlasWidth, atlasHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setFont(awtFont);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        FontMetrics fm = g.getFontMetrics();
        float fontH = fm.getHeight();
        Map<Character, Glyph> glyphs = new HashMap<>();

        int curX = 4;
        int curY = 4;
        int rowHeight = fm.getHeight() + 6;

        StringBuilder sb = new StringBuilder();
        for (char c = 32; c <= 126; c++) sb.append(c);
        for (char c = 1040; c <= 1103; c++) sb.append(c);
        sb.append("ёЁ№°±·—–•“”«»§><v^|%+*-/=()[]{}!?.,:;'\"`~@#$%^&*");

        g.setColor(java.awt.Color.WHITE);

        for (int i = 0; i < sb.length(); i++) {
            char c = sb.charAt(i);
            if (glyphs.containsKey(c)) continue;

            int charW = Math.max(1, fm.charWidth(c));
            int pad = 2;
            int boxW = charW + pad * 2;
            int boxH = fm.getHeight() + pad * 2;

            if (curX + boxW >= atlasWidth - 6) {
                curX = 4;
                curY += rowHeight;
            }
            if (curY + boxH >= atlasHeight - 6) break;

            g.drawString(String.valueOf(c), curX + pad, curY + pad + fm.getAscent());
            glyphs.put(c, new Glyph(curX, curY, boxW, boxH, fm.charWidth(c)));
            curX += boxW + 4;
        }
        g.dispose();

        NativeImage nativeImage = new NativeImage(NativeImage.Format.RGBA, atlasWidth, atlasHeight, false);
        for (int y = 0; y < atlasHeight; y++) {
            for (int x = 0; x < atlasWidth; x++) {
                int argb = img.getRGB(x, y);
                int a = (argb >>> 24) & 0xFF;
                if (a > 0) {

                    int abgr = (a << 24) | 0x00FFFFFF;
                    nativeImage.setPixelABGR(x, y, abgr);
                } else {
                    nativeImage.setPixelABGR(x, y, 0);
                }
            }
        }

        String path = "textures/font_" + normFamily.toLowerCase().replace(" ", "_") + "_atlas.png";
        Identifier texIdentifier = Identifier.fromNamespaceAndPath("petrovich_26_2", path);

        LinearDynamicTexture texture = new LinearDynamicTexture(texIdentifier::toString, nativeImage);
        texture.upload();
        try {
            Minecraft.getInstance().getTextureManager().register(texIdentifier, texture);
        } catch (Throwable ignored) {}

        FontData data = new FontData(normFamily, texIdentifier, texture, glyphs, baseSize, fontH, atlasWidth, atlasHeight);
        fontCache.put(normFamily, data);
        return data;
    }

    public float getStringWidth(String family, String text, float size) {
        if (text == null || text.isEmpty()) return 0.0f;
        FontData fd = getFont(family);
        float scale = size / fd.baseSize;
        float total = 0.0f;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '§' && i + 1 < text.length()) {
                i++;
                continue;
            }
            Glyph g = fd.glyphs.get(c);
            if (g != null) {
                total += g.advance * scale;
            } else {
                total += 8.0f * scale;
            }
        }
        return total;
    }

    public float getStringWidth(String text, float size) {
        String family = "Inter";
        try {
            family = FontManager.customFontFamily();
        } catch (Throwable ignored) {}
        return getStringWidth(family, text, size);
    }

    public void drawString(GuiGraphicsExtractor extractor, String family, String text,
                           float x, float y, float size, ColorRGBA color, boolean shadow) {
        if (text == null || text.isEmpty() || extractor == null) return;
        FontData fd = getFont(family);

        if (shadow) {
            ColorRGBA shadowCol = new ColorRGBA(0, 0, 0, Math.round(color.a() * 0.5f));
            renderTextInternal(extractor, fd, text, x + 0.6f, y + 0.6f, size, shadowCol);
        }

        renderTextInternal(extractor, fd, text, x, y, size, color);
    }

    public void drawString(GuiGraphicsExtractor extractor, String text,
                           float x, float y, float size, ColorRGBA color, boolean shadow) {
        String family = "Inter";
        try {
            family = FontManager.customFontFamily();
        } catch (Throwable ignored) {}
        drawString(extractor, family, text, x, y, size, color, shadow);
    }

    private void renderTextInternal(GuiGraphicsExtractor extractor, FontData fd,
                                    String text, float x, float y, float size, ColorRGBA color) {
        float scale = size / fd.baseSize;
        float curX = x;
        int defaultCol = color.packed();
        int activeCol = defaultCol;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '§' && i + 1 < text.length()) {
                char code = Character.toLowerCase(text.charAt(i + 1));
                activeCol = parseMinecraftColor(code, defaultCol);
                i++;
                continue;
            }

            Glyph g = fd.glyphs.get(c);
            if (g != null) {
                if (c != ' ') {
                    float dw = g.width * scale;
                    float dh = g.height * scale;
                    extractor.blit(
                            RenderPipelines.GUI_TEXTURED,
                            fd.textureId,
                            Math.round(curX),
                            Math.round(y - 2.0f * scale),
                            (float) g.x,
                            (float) g.y,
                            Math.max(1, Math.round(dw)),
                            Math.max(1, Math.round(dh)),
                            g.width,
                            g.height,
                            fd.atlasWidth,
                            fd.atlasHeight,
                            activeCol
                    );
                }
                curX += g.advance * scale;
            } else {
                curX += 8.0f * scale;
            }
        }
    }

    private static int parseMinecraftColor(char code, int defaultCol) {
        return switch (code) {
            case '0' -> 0xFF000000;
            case '1' -> 0xFF0000AA;
            case '2' -> 0xFF00AA00;
            case '3' -> 0xFF00AAAA;
            case '4' -> 0xFFAA0000;
            case '5' -> 0xFFAA00AA;
            case '6' -> 0xFFFFAA00;
            case '7' -> 0xFFAAAAAA;
            case '8' -> 0xFF555555;
            case '9' -> 0xFF5555FF;
            case 'a' -> 0xFF55FF55;
            case 'b' -> 0xFF55FFFF;
            case 'c' -> 0xFFFF5555;
            case 'd' -> 0xFFFF55FF;
            case 'e' -> 0xFFFFFF55;
            case 'f' -> 0xFFFFFFFF;
            case 'r' -> defaultCol;
            default -> defaultCol;
        };
    }
}
