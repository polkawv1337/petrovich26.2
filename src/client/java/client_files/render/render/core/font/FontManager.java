package client_files.render.render.core.font;

import client_files.render.render.assets.AssetLocation;
import client_files.render.render.assets.font.FontAsset;
import client_files.render.render.assets.font.FontAssetType;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class FontManager {
    private static final FontManager INSTANCE = new FontManager();

    private static volatile boolean customFonts = true;
    private static volatile String customFontFamily = "inter";

    private final Map<AssetLocation, CustomFont> fonts = new ConcurrentHashMap<>();
    private CustomFont vanillaFont;

    private FontManager() {}

    public static FontManager get() {
        return INSTANCE;
    }

    /** Использовать собственные MSDF-шрифты вместо ванильного. */
    public static boolean customFonts() {
        return customFonts;
    }

    public static void setCustomFonts(boolean enabled) {
        customFonts = enabled;
    }

    /** Семейство MSDF-шрифта по умолчанию (inter, roboto, poppins, montserrat, opensans, jetbrains_mono, firacode). */
    public static String customFontFamily() {
        return customFontFamily;
    }

    public static void setCustomFontFamily(String family) {
        if (family != null && !family.isEmpty()) {
            customFontFamily = family;
        }
    }

    public void register(AssetLocation id, CustomFont font) {
        if (id != null && font != null) {
            fonts.put(id, font);
        }
    }

    public void register(AssetLocation id, FontAsset asset) {
        if (id == null || asset == null) return;
        boolean isMsdf = asset.getType() == FontAssetType.MSDF;
        GlyphAtlas atlas = new GlyphAtlas(null, Collections.emptyMap(), isMsdf);
        CustomFont font = new CustomFont(atlas, asset.getFamily(), 16f);
        fonts.put(id, font);
    }

    public CustomFont get(AssetLocation id) {
        CustomFont font = fonts.get(id);
        return font != null ? font : vanilla();
    }

    public CustomFont vanilla() {
        if (vanillaFont == null) {
            GlyphAtlas atlas = new GlyphAtlas(null, Collections.emptyMap(), false);
            vanillaFont = new CustomFont(atlas, "Minecraft", 9f);
        }
        return vanillaFont;
    }
}
