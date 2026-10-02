package client_files.ClientikUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Theme {

    public static final String[] NAMES = {"Оригинал", "Неон", "Огонь", "Лёд", "Лес", "White-Green"};

    private static final int[][] PALETTES = {
        {0xFFFFD6C7, 0xFF8FD0F5, 0xFF9BE8B4, 0xFFD6C9FF, 0xFFFFE9A8},
        {0xFFFF5FA2, 0xFF7AEBFF, 0xFF9AFF6B, 0xFFC792EA, 0xFFFFEB3B},
        {0xFFFF8A5C, 0xFFFFA247, 0xFFFF6B6B, 0xFFFFC46B, 0xFFFFE08A},
        {0xFF54C8FF, 0xFF7BE0FF, 0xFFA8E8FF, 0xFF9ADBFF, 0xFFD6F4FF},
        {0xFF98D98C, 0xFF67C678, 0xFFC6E873, 0xFF54B89F, 0xFFD9E89B},
        {0xFFFFFFFF, 0xFF4DFFFF, 0xFF4DFF99, 0xFFA64DFF, 0xFFFFFF4D} // White-Green тема
    };

    private static final int[][] OVERRIDES = new int[PALETTES.length][];
    private static final Gson GSON = new GsonBuilder().create();
    private static final Path FILE = Paths.get("theme.json");

    static {
        for (int i = 0; i < OVERRIDES.length; i++) {
            OVERRIDES[i] = new int[5];
            java.util.Arrays.fill(OVERRIDES[i], -1);
        }
    }

    private static int current;

    private Theme() {
    }

    public static int count() {
        return PALETTES.length;
    }

    public static int getCurrent() {
        return current;
    }

    private static final Category ACCENT_SLOT = Category.COMBAT;

    public static int color(int palette, Category category) {
        int slot = ACCENT_SLOT.ordinal();
        int override = OVERRIDES[Math.max(0, Math.min(OVERRIDES.length - 1, palette))][slot];
        if (override != -1) {
            return override;
        }
        return PALETTES[Math.max(0, Math.min(PALETTES.length - 1, palette))][slot];
    }

    public static void setColor(int palette, Category category, int argb) {
        int p = Math.max(0, Math.min(OVERRIDES.length - 1, palette));
        OVERRIDES[p][ACCENT_SLOT.ordinal()] = argb | 0xFF000000;
        save();
    }

    public static void resetColor(int palette, Category category) {
        OVERRIDES[Math.max(0, Math.min(OVERRIDES.length - 1, palette))][ACCENT_SLOT.ordinal()] = -1;
        save();
    }

    public static boolean hasOverride(int palette, Category category) {
        return OVERRIDES[Math.max(0, Math.min(OVERRIDES.length - 1, palette))][ACCENT_SLOT.ordinal()] != -1;
    }

    public static int accent(Category category) {
        return color(current, ACCENT_SLOT);
    }

    public static void apply(int palette) {
        current = Math.max(0, Math.min(PALETTES.length - 1, palette));
        save();
    }

    public static void load() {
        try (Reader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            if (root.has("current")) {
                int value = root.get("current").getAsInt();
                current = Math.max(0, Math.min(PALETTES.length - 1, value));
            }
            if (root.has("colors") && root.get("colors").isJsonArray()) {
                JsonArray all = root.getAsJsonArray("colors");
                for (int p = 0; p < all.size() && p < OVERRIDES.length; p++) {
                    if (all.get(p).isJsonArray()) {
                        JsonArray row = all.get(p).getAsJsonArray();
                        for (int c = 0; c < row.size() && c < 5; c++) {
                            OVERRIDES[p][c] = row.get(c).getAsInt();
                        }
                    }
                }
            }
        } catch (Exception ignored) {
            current = 5; // White-Green тема по умолчанию
        }
    }

    private static void save() {
        try {
            JsonObject root = new JsonObject();
            root.addProperty("current", current);
            JsonArray colors = new JsonArray();
            for (int[] row : OVERRIDES) {
                JsonArray jsonRow = new JsonArray();
                for (int color : row) {
                    jsonRow.add(color);
                }
                colors.add(jsonRow);
            }
            root.add("colors", colors);
            Path parent = FILE.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(FILE, GSON.toJson(root).getBytes(StandardCharsets.UTF_8));
        } catch (Exception ignored) {
        }
    }
}