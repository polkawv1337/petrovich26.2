package client_files.render.render.core.font.msdf;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.mojang.blaze3d.platform.NativeImage;
import client_files.render.render.core.LinearDynamicTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Загружает заранее испечённые MSDF/MTSDF атласы (JSON + PNG) и превращает их
 * в обычные {@link MsdfFont} движка.
 *
 * Нужно для иконочных шрифтов (petrovich, hud_icons, icons, icons_nurik): их глифы
 * живут в Private Use Area (U+E000..), а {@link MsdfAtlasGenerator} собирает атлас
 * только из Latin+Cyrillic - без этого класса такие символы теряются.
 */
public final class BakedAtlasFonts {
    private static final Logger LOGGER = LoggerFactory.getLogger(BakedAtlasFonts.class);

    /** Запечённые атласы, которые лежат в assets/petrovich_26_2/fonts/. */
    private static final Set<String> BAKED_FAMILIES =
            Set.of("petrovich", "hud_icons", "icons", "icons_nurik");

    /** Резкость coverage-маски в единицах distance field. Ниже - чётче. */
    private static final float SHARPNESS = 2.0f;

    private static final Gson GSON = new Gson();

    private static final Map<String, BakedFont> CACHE = new ConcurrentHashMap<>();
    private static final Map<Character, String> CHAR_FAMILY = new ConcurrentHashMap<>();

    private BakedAtlasFonts() {}

    public record BakedFont(
            String family,
            MsdfFont font,
            Identifier textureId,
            int textureWidth,
            int textureHeight
    ) {}

    public static Set<String> families() {
        return BAKED_FAMILIES;
    }

    public static boolean isBaked(String family) {
        return family != null && BAKED_FAMILIES.contains(MsdfFontCache.normalize(family));
    }

    /** Уже загруженный атлас или null - без обращения к диску. */
    public static BakedFont get(String family) {
        return CACHE.get(MsdfFontCache.normalize(family));
    }

    public static synchronized BakedFont load(String family) {
        String key = MsdfFontCache.normalize(family);
        BakedFont cached = CACHE.get(key);
        if (cached != null) return cached;
        if (!BAKED_FAMILIES.contains(key)) return null;
        try {
            BakedFont loaded = doLoad(key);
            CACHE.put(key, loaded);
            indexChars(loaded);
            LOGGER.info("[Render] Loaded baked atlas font '{}' ({} glyphs, {}x{})",
                    key, loaded.font().glyphCount(), loaded.textureWidth(), loaded.textureHeight());
            return loaded;
        } catch (Exception e) {
            LOGGER.error("[Render] Failed to load baked atlas font '{}'", key, e);
            return null;
        }
    }

    private static BakedFont doLoad(String key) throws IOException {
        Minecraft mc = Minecraft.getInstance();
        Identifier jsonId = Identifier.fromNamespaceAndPath("petrovich_26_2", "fonts/" + key + ".json");
        AtlasJson data;
        try (BufferedReader reader = mc.getResourceManager().openAsReader(jsonId)) {
            data = GSON.fromJson(reader, AtlasJson.class);
        }
        if (data == null || data.atlas == null || data.glyphs == null) {
            throw new IOException("Malformed atlas json for '" + key + "'");
        }

        boolean mtsdf = "mtsdf".equalsIgnoreCase(data.atlas.type);
        NativeImage source;
        try (InputStream in = mc.getResourceManager()
                .open(Identifier.fromNamespaceAndPath("petrovich_26_2", "fonts/" + key + ".png"))) {
            source = NativeImage.read(in);
        }

        NativeImage coverage = toCoverage(source, mtsdf);
        Identifier texId = Identifier.fromNamespaceAndPath("petrovich_26_2", "textures/baked/font_" + key + ".png");
        LinearDynamicTexture texture = new LinearDynamicTexture(texId::toString, coverage);
        texture.upload();
        try {
            mc.getTextureManager().register(texId, texture);
        } catch (Throwable t) {
            LOGGER.warn("[Render] Could not register texture {}: {}", texId, t.getMessage());
        }

        float atlasWidth = data.atlas.width > 0 ? data.atlas.width : source.getWidth();
        float atlasHeight = data.atlas.height > 0 ? data.atlas.height : source.getHeight();
        // yOrigin=bottom: атлас растёт снизу вверх, V-координаты переворачиваем.
        boolean flipV = !"top".equalsIgnoreCase(data.atlas.yOrigin);

        Map<Character, MsdfGlyph> glyphs = new LinkedHashMap<>();
        Map<Integer, Map<Integer, Float>> kernings = new HashMap<>();

        for (Glyph g : data.glyphs) {
            float planeLeft = 0f;
            float planeTop = 0f;
            if (g.planeBounds != null) {
                planeLeft = g.planeBounds.left;
                planeTop = g.planeBounds.top;
            }
            int u = 0;
            int v = 0;
            int w = 0;
            int h = 0;
            if (g.atlasBounds != null && atlasWidth > 0 && atlasHeight > 0) {
                w = Math.round(g.atlasBounds.right - g.atlasBounds.left);
                h = Math.round(g.atlasBounds.bottom - g.atlasBounds.top);
                u = Math.round(g.atlasBounds.left);
                v = flipV
                        ? Math.round(atlasHeight - g.atlasBounds.bottom)
                        : Math.round(g.atlasBounds.top);
            }
            glyphs.put((char) g.unicode, new MsdfGlyph(u, v, w, h, g.advance, planeLeft, -planeTop));
        }

        if (data.kerning != null) {
            for (Kerning k : data.kerning) {
                kernings.computeIfAbsent(k.left, $ -> new HashMap<>()).put(k.right, k.advance);
            }
        }

        float ascender = data.metrics != null && data.metrics.ascender != 0f ? data.metrics.ascender : 0.9f;
        float descender = data.metrics != null && data.metrics.descender != 0f ? data.metrics.descender : -0.1f;
        float lineHeight = data.metrics != null && data.metrics.lineHeight > 0f ? data.metrics.lineHeight : 1f;

        // emSize == 1 в запечённых атласах: plane-координаты уже в долях em.
        MsdfFont font = new MsdfFont(key, glyphs, ascender, descender, lineHeight, 1f, kernings);

        return new BakedFont(key, font, texId, coverage.getWidth(), coverage.getHeight());
    }

    private static void indexChars(BakedFont font) {
        font.font().forEachGlyph((ch, glyph) -> {
            if (glyph.w() > 0 && glyph.h() > 0) {
                CHAR_FAMILY.putIfAbsent(ch, font.family());
            }
        });
    }

    /**
     * Семейство испечённого иконочного шрифта для символа Private Use Area,
     * либо null - если символ не иконка и должен браться из основного шрифта.
     */
    public static String familyForChar(char c) {
        return (c >= 0xE000 && c <= 0xF8FF) ? CHAR_FAMILY.get(c) : null;
    }

    /** Distance field -> резкая coverage-маска в альфе, RGB белый (для tint в blit). */
    private static NativeImage toCoverage(NativeImage source, boolean mtsdf) {
        int w = source.getWidth();
        int h = source.getHeight();
        NativeImage out = new NativeImage(w, h, true);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                out.setPixelABGR(x, y, coverage(source.getPixel(x, y), mtsdf) | 0x00FFFFFF);
            }
        }
        return out;
    }

    private static int coverage(int argb, boolean mtsdf) {
        int a = (argb >>> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        int field = mtsdf ? a : Math.max(a, Math.max(r, Math.max(g, b)));
        float d = field / 255.0f;
        float c = Math.max(0.0f, Math.min(1.0f, (d - 0.5f) * SHARPNESS + 0.5f));
        c = c * c * (3.0f - 2.0f * c);
        int v = Math.round(c * 255.0f);
        return (v << 24) | (v << 16) | (v << 8) | v;
    }

    // ------------------------------------------------------------------ json

    static final class AtlasJson {
        AtlasData atlas;
        MetricsData metrics;
        List<Glyph> glyphs;
        @SerializedName("kerning")
        List<Kerning> kerning;
    }

    static final class AtlasData {
        String type;
        float width;
        float height;
        String yOrigin;
    }

    static final class MetricsData {
        float emSize;
        float lineHeight;
        float ascender;
        float descender;
    }

    static final class Glyph {
        int unicode;
        float advance;
        Bounds planeBounds;
        Bounds atlasBounds;
    }

    static final class Bounds {
        float left;
        float top;
        float right;
        float bottom;
    }

    static final class Kerning {
        @SerializedName("unicode1")
        int left;
        @SerializedName("unicode2")
        int right;
        float advance;
    }
}