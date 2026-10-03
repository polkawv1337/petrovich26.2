package client_files.render.render.core.font.msdf;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MsdfAtlasGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MsdfAtlasGenerator.class);

    public static final float BASE_SIZE = 32.0f;

    private static final int PAD = 4;

    private static final int ATLAS_SIZE = 1024;

    private MsdfAtlasGenerator() {}

    public record GeneratedAtlas(
            BufferedImage image,
            Map<Character, MsdfGlyph> glyphs,
            float ascender,
            float descender,
            float lineHeight
    ) {}

    public static Font loadAwtFont(String family) {
        String file = familyToFile(family);
        Font font = tryLoad(file);
        if (font == null) {
            LOGGER.warn("[Render] Could not find TTF font '{}' for family '{}', falling back to system SansSerif", file, family);
            font = new Font("SansSerif", Font.PLAIN, (int) BASE_SIZE);
        } else {
            font = font.deriveFont(Font.PLAIN, BASE_SIZE);
            LOGGER.info("[Render] Loaded font '{}' for family '{}'", font.getFontName(), family);
        }
        return font;
    }

    public static String familyToFile(String family) {
        return "inter.ttf";
    }

    private static Font tryLoad(String file) {

        try {
            var containerOpt = net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer("petrovich_26_2");
            if (containerOpt.isPresent()) {
                var p = containerOpt.get().findPath("assets/petrovich_26_2/render/font/ttf/" + file);
                if (p.isPresent() && java.nio.file.Files.exists(p.get())) {
                    try (InputStream in = java.nio.file.Files.newInputStream(p.get())) {
                        return Font.createFont(Font.TRUETYPE_FONT, in);
                    }
                }
            }
        } catch (Throwable ignored) {}

        try {
            var id = Identifier.fromNamespaceAndPath("petrovich_26_2", "render/font/ttf/" + file);
            var resOpt = Minecraft.getInstance().getResourceManager().getResource(id);
            if (resOpt.isPresent()) {
                try (InputStream in = resOpt.get().open()) {
                    return Font.createFont(Font.TRUETYPE_FONT, in);
                }
            }
        } catch (Throwable ignored) {}

        try (InputStream is = MsdfAtlasGenerator.class.getResourceAsStream("/assets/petrovich_26_2/render/font/ttf/" + file)) {
            if (is != null) {
                return Font.createFont(Font.TRUETYPE_FONT, is);
            }
        } catch (Throwable ignored) {}

        try (InputStream is = MsdfAtlasGenerator.class.getClassLoader().getResourceAsStream("assets/petrovich_26_2/render/font/ttf/" + file)) {
            if (is != null) {
                return Font.createFont(Font.TRUETYPE_FONT, is);
            }
        } catch (Throwable ignored) {}

        try {
            java.io.File diskFile = new java.io.File("src/client/resources/assets/petrovich_26_2/render/font/ttf/" + file);
            if (diskFile.exists()) {
                try (InputStream in = new java.io.FileInputStream(diskFile)) {
                    return Font.createFont(Font.TRUETYPE_FONT, in);
                }
            }
        } catch (Throwable ignored) {}

        return null;
    }

    public static String charset() {
        StringBuilder sb = new StringBuilder();
        for (char c = 32; c <= 126; c++) sb.append(c);
        for (char c = 1040; c <= 1103; c++) sb.append(c);
        sb.append("ёЁ№°±·—–•“”«»§><v^|%+*-/=()[]{}!?.,:;'\"`~@#$%^&*");
        return sb.toString();
    }

    public static GeneratedAtlas generate(String family) {
        Font font = loadAwtFont(family);

        BufferedImage measureImg = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
        Graphics2D mg = measureImg.createGraphics();
        mg.setFont(font);
        applyHints(mg);
        FontMetrics fm = mg.getFontMetrics();
        float ascender = fm.getAscent();
        float descender = fm.getDescent() + fm.getLeading();
        float lineHeight = fm.getHeight();
        mg.dispose();

        BufferedImage atlas = new BufferedImage(ATLAS_SIZE, ATLAS_SIZE, BufferedImage.TYPE_INT_ARGB);
        Map<Character, MsdfGlyph> glyphs = new LinkedHashMap<>();

        int curX = 4;
        int curY = 4;
        int rowH = 0;

        String chars = charset();

        List<Character> unique = new ArrayList<>();
        for (int i = 0; i < chars.length(); i++) {
            char c = chars.charAt(i);
            if (!glyphs.containsKey(c) && !unique.contains(c)) unique.add(c);
        }

        // Реальные границы чернил: рисуем глиф в запасную клетку и обрезаем по альфе.
        // Раньше размер клетки брался из advance, и широкие буквы (W, М, Ж, @) обрезались.
        Graphics2D inkGraphics = measureImg.createGraphics();
        inkGraphics.setFont(font);
        applyHints(inkGraphics);

        int cell = (int) Math.ceil(Math.max(lineHeight, fm.getAscent() + fm.getDescent()) * 2.5f) + PAD * 2;
        int origin = cell / 2;
        BufferedImage probe = new BufferedImage(cell, cell, BufferedImage.TYPE_INT_ARGB);

        for (char c : unique) {
            float advance = fm.charWidth(c);
            if (c == ' ' || advance <= 0) {
                glyphs.put(c, new MsdfGlyph(0, 0, 0, 0, advance, 0, 0));
                continue;
            }

            Graphics2D pg = probe.createGraphics();
            pg.setComposite(java.awt.AlphaComposite.Clear);
            pg.fillRect(0, 0, cell, cell);
            // обязательно возвращаем SrcOver: с Clear drawString затирает глиф
            pg.setComposite(java.awt.AlphaComposite.SrcOver);
            pg.setFont(font);
            applyHints(pg);
            pg.setColor(java.awt.Color.WHITE);
            pg.drawString(String.valueOf(c), origin, origin);
            pg.dispose();

            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;
            for (int y = 0; y < cell; y++) {
                for (int x = 0; x < cell; x++) {
                    if (((probe.getRGB(x, y) >>> 24) & 0xFF) > 0) {
                        if (x < minX) minX = x;
                        if (x > maxX) maxX = x;
                        if (y < minY) minY = y;
                        if (y > maxY) maxY = y;
                    }
                }
            }

            if (minX > maxX || minY > maxY) {
                glyphs.put(c, new MsdfGlyph(0, 0, 0, 0, advance, 0, 0));
                continue;
            }

            int inkW = maxX - minX + 1;
            int inkH = maxY - minY + 1;
            int bmpW = inkW + PAD * 2;
            int bmpH = inkH + PAD * 2;

            BufferedImage src = new BufferedImage(bmpW, bmpH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = src.createGraphics();
            g.setFont(font);
            applyHints(g);
            g.setColor(java.awt.Color.WHITE);
            g.drawString(String.valueOf(c), origin + PAD - minX, origin + PAD - minY);
            g.dispose();

            int inkX = minX - origin;
            int inkY = minY - origin;
            if (curX + bmpW + 4 >= ATLAS_SIZE) {
                curX = 4;
                curY += rowH + 4;
                rowH = 0;
            }
            if (curY + bmpH + 4 >= ATLAS_SIZE) {
                LOGGER.warn("[Render] Font atlas full for '{}', dropping char '{}'", family, c);
                break;
            }

            for (int y = 0; y < bmpH; y++) {
                for (int x = 0; x < bmpW; x++) {
                    int argb = src.getRGB(x, y);
                    int a = (argb >>> 24) & 0xFF;
                    if (a > 0) {
                        atlas.setRGB(curX + x, curY + y, (a << 24) | 0x00FFFFFF);
                    } else {
                        atlas.setRGB(curX + x, curY + y, 0);
                    }
                }
            }

            glyphs.put(c, new MsdfGlyph(curX, curY, bmpW, bmpH, advance, inkX - PAD, inkY - PAD));
            curX += bmpW + 4;
            rowH = Math.max(rowH, bmpH);
        }

        inkGraphics.dispose();
        return new GeneratedAtlas(atlas, glyphs, ascender, descender, lineHeight);
    }

    private static void applyHints(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }
}
