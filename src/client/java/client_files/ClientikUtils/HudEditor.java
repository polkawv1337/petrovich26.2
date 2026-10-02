package client_files.ClientikUtils;

import client_files.Module;
import client_files.ModuleManager;
import client_files.ClientikUtils.render.RRender;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class HudEditor {

    private static final Gson GSON = new GsonBuilder().create();
    private static final Path FILE = Paths.get("hud_positions.json");
    private static final Map<Module, int[]> RECTS = new LinkedHashMap<>();

    private HudEditor() {
    }

    public static int xPos(Module module, int w, int guiW) {
        return Math.max(2, Math.min(guiW - w - 2, Math.round(module.getHudX())));
    }

    public static int yPos(Module module, int h, int guiH) {
        return Math.max(2, Math.min(guiH - h - 2, Math.round(module.getHudY())));
    }

    public static void place(Module module, int x, int y, int w, int h) {
        RECTS.put(module, new int[]{x, y, w, h});
    }

    public static int[] rect(Module module) {
        return RECTS.get(module);
    }

    public static void clear() {
        RECTS.clear();
    }

    public static Module grab(int mouseX, int mouseY) {
        Module best = null;
        for (Map.Entry<Module, int[]> entry : RECTS.entrySet()) {
            int[] r = entry.getValue();
            if (RRender.hovered(mouseX, mouseY, r[0], r[1], r[2], r[3])) {
                best = entry.getKey();
            }
        }
        return best;
    }

    public static void loadOffsets(List<Module> modules) {
        try (Reader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            for (Module module : modules) {
                JsonArray arr = root.getAsJsonArray(module.getName());
                if (arr != null && arr.size() == 2) {
                    module.setHud(arr.get(0).getAsFloat(), arr.get(1).getAsFloat());
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static void save() {
        try {
            JsonObject root = new JsonObject();
            for (Module module : ModuleManager.getInstance().getModules()) {
                if (module.isDraggable()) {
                    JsonArray arr = new JsonArray();
                    arr.add(module.getHudX());
                    arr.add(module.getHudY());
                    root.add(module.getName(), arr);
                }
            }
            Path parent = FILE.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(FILE, GSON.toJson(root).getBytes(StandardCharsets.UTF_8));
        } catch (Exception ignored) {
        }
    }
}