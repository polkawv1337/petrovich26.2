package client_files.ClientikUtils;

import client_files.Module;
import client_files.ModuleManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Config {

    private static final Gson GSON = new GsonBuilder().create();
    private static final Path FILE = Paths.get("petrovich_config.json");

    private Config() {
    }

    public static void load(java.util.List<Module> modules) {
        try (Reader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            for (Module module : modules) {
                JsonObject o = root.getAsJsonObject(module.getName());
                if (o == null) {
                    continue;
                }
                if (o.has("enabled")) {
                    module.setEnabled(o.get("enabled").getAsBoolean());
                }
                if (o.has("key")) {
                    module.setKey(o.get("key").getAsInt());
                }
                if (o.has("scroll")) {
                    module.setScrollBind(o.get("scroll").getAsInt());
                }
                JsonObject settings = o.getAsJsonObject("settings");
                if (settings != null) {
                    for (Setting setting : module.getSettings()) {
                        com.google.gson.JsonElement value = settings.get(setting.getName());
                        if (value == null) {
                            continue;
                        }
                        if (setting instanceof BooleanSetting booleanSetting) {
                            booleanSetting.setValue(value.getAsBoolean());
                        } else if (setting instanceof SliderSetting sliderSetting) {
                            sliderSetting.setValue((float) value.getAsDouble());
                        } else if (setting instanceof ModeSetting modeSetting) {
                            modeSetting.setValue(value.getAsString());
                        } else if (setting instanceof ModeListSetting modeListSetting) {
                            java.util.List<String> names = new java.util.ArrayList<>();
                            for (String part : value.getAsString().split(",")) {
                                if (!part.isEmpty()) {
                                    names.add(part);
                                }
                            }
                            modeListSetting.setEnabledNames(names);
                        } else if (setting instanceof BindSetting bindSetting) {
                            bindSetting.setValue(value.getAsInt());
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static void save() {
        if (!ModuleManager.isReady()) {
            return;
        }
        try {
            JsonObject root = new JsonObject();
            for (Module module : ModuleManager.getInstance().getModules()) {
                JsonObject o = new JsonObject();
                o.addProperty("enabled", module.isEnabled());
                o.addProperty("key", module.getKey());
                o.addProperty("scroll", module.getScrollBind());
                JsonObject settings = new JsonObject();
                for (Setting setting : module.getSettings()) {
                    if (setting instanceof BooleanSetting booleanSetting) {
                        settings.addProperty(setting.getName(), booleanSetting.getValue());
                    } else if (setting instanceof SliderSetting sliderSetting) {
                        settings.addProperty(setting.getName(), sliderSetting.getValue());
                    } else if (setting instanceof ModeSetting modeSetting) {
                        settings.addProperty(setting.getName(), modeSetting.getValue());
                    } else if (setting instanceof ModeListSetting modeListSetting) {
                        settings.addProperty(setting.getName(), String.join(",", modeListSetting.getEnabledNames()));
                    } else if (setting instanceof BindSetting bindSetting) {
                        settings.addProperty(setting.getName(), bindSetting.getValue());
                    }
                }
                if (settings.size() > 0) {
                    o.add("settings", settings);
                }
                root.add(module.getName(), o);
            }
            Path parent = FILE.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(FILE, GSON.toJson(root).getBytes(StandardCharsets.UTF_8));
        } catch (Exception ignored) {
        }
    }

    public static void saveAsync() {
        new Thread(Config::save).start();
    }
}