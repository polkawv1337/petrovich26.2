package client_files.render.render.core.font.msdf;

import com.mojang.blaze3d.platform.NativeImage;
import client_files.render.render.core.LinearDynamicTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class MsdfFontCache {
    private static final MsdfFontCache INSTANCE = new MsdfFontCache();

    public static final String[] FAMILIES = {
            "Inter", "Roboto", "Poppins", "Montserrat", "OpenSans", "JetBrains Mono", "FiraCode"
    };

    private final Map<String, MsdfFont> fonts = new ConcurrentHashMap<>();
    private final Map<String, Identifier> textures = new ConcurrentHashMap<>();
    private final Map<String, Integer> atlasSizes = new ConcurrentHashMap<>();
    private final Map<String, Integer> atlasWidths = new ConcurrentHashMap<>();
    private final Map<String, Integer> atlasHeights = new ConcurrentHashMap<>();

    private MsdfFontCache() {}

    public static MsdfFontCache get() {
        return INSTANCE;
    }

    /** Атлас шрифта: сначала испечённый, иначе генерируется из TTF. */
    public synchronized MsdfFont font(String family) {
        BakedAtlasFonts.BakedFont baked = BakedAtlasFonts.get(family);
        if (baked != null) return baked.font();

        String key = normalize(family);
        MsdfFont existing = fonts.get(key);
        if (existing != null) return existing;

        BakedAtlasFonts.BakedFont loaded = BakedAtlasFonts.load(key);
        if (loaded != null) return loaded.font();

        return load(key);
    }

    public Identifier textureId(String family) {
        String key = normalize(family);
        BakedAtlasFonts.BakedFont baked = BakedAtlasFonts.get(key);
        if (baked != null) return baked.textureId();
        return textures.get(key);
    }

    /** Ширина текстуры атласа в пикселях. */
    public int atlasWidth(String family) {
        String key = normalize(family);
        BakedAtlasFonts.BakedFont baked = BakedAtlasFonts.get(key);
        if (baked != null) return baked.textureWidth();
        return atlasWidths.getOrDefault(key, 1024);
    }

    /** Высота текстуры атласа в пикселях. */
    public int atlasHeight(String family) {
        String key = normalize(family);
        BakedAtlasFonts.BakedFont baked = BakedAtlasFonts.get(key);
        if (baked != null) return baked.textureHeight();
        return atlasHeights.getOrDefault(key, 1024);
    }

    /** legacy-аксессор: размер, считающийся квадратным. */
    public int atlasSize(String family) {
        return Math.max(atlasWidth(family), atlasHeight(family));
    }

    private MsdfFont load(String key) {
        MsdfAtlasGenerator.GeneratedAtlas gen = MsdfAtlasGenerator.generate(displayName(key));
        BufferedImage img = gen.image();
        int size = img.getWidth();

        NativeImage nativeImage = new NativeImage(NativeImage.Format.RGBA, size, size, false);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int argb = img.getRGB(x, y);
                int a = (argb >>> 24) & 0xFF;
                nativeImage.setPixelABGR(x, y, a == 0 ? 0 : (a << 24) | 0x00FFFFFF);
            }
        }

        String path = "textures/msdf_" + key.toLowerCase() + ".png";
        Identifier id = Identifier.fromNamespaceAndPath("petrovich_26_2", path);
        try {
            LinearDynamicTexture tex = new LinearDynamicTexture(id::toString, nativeImage);
            tex.upload();
            try {
                Minecraft.getInstance().getTextureManager().register(id, tex);
            } catch (Throwable ignored) {}
        } catch (Throwable t) {
            t.printStackTrace();
        }

        MsdfFont font = new MsdfFont(displayName(key), gen.glyphs(),
                gen.ascender(), gen.descender(), gen.lineHeight(), MsdfAtlasGenerator.BASE_SIZE);
        fonts.put(key, font);
        textures.put(key, id);
        atlasSizes.put(key, size);
        atlasWidths.put(key, size);
        atlasHeights.put(key, size);
        return font;
    }

    public static String normalize(String family) {
        if (family == null) return "inter";
        return family.trim().toLowerCase().replace(" ", "_");
    }

    public static String displayName(String normalized) {
        return switch (normalize(normalized)) {
            case "roboto" -> "Roboto";
            case "poppins" -> "Poppins";
            case "montserrat" -> "Montserrat";
            case "opensans", "open_sans" -> "OpenSans";
            case "jetbrains_mono" -> "JetBrains Mono";
            case "firacode", "fira_code" -> "FiraCode";
            case "petrovich" -> "Petrovich";
            case "hud_icons" -> "HudIcons";
            case "icons" -> "Icons";
            case "icons_nurik" -> "IconsNurik";
            default -> "Inter";
        };
    }
}