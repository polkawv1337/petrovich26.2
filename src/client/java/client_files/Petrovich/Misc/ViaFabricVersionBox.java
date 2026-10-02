package client_files.Petrovich.Misc;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ViaFabricVersionBox {

    private static final String VIA_FABRIC_CLASS = "com.viaversion.fabric.ViaFabric";
    private static final String PROTOCOL_UTILS_CLASS = "com.viaversion.fabric.common.util.ProtocolUtils";

    private ViaFabricVersionBox() {
    }

    public static void register() {
        ScreenEvents.AFTER_INIT.register((mc, screen, width, height) -> {
            if (screen instanceof JoinMultiplayerScreen && !hasVersionBox(screen)) {
                addVersionBox(screen);
            }
        });
    }

    private static boolean hasVersionBox(Screen screen) {
        for (var widget : Screens.getWidgets(screen)) {
            if (widget instanceof EditBox) {
                return true;
            }
        }
        return false;
    }

    private static void addVersionBox(Screen screen) {
        Object config = viaConfig();
        if (config == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        EditBox box = new EditBox(mc.font, 10, 10, 130, 18, Component.literal("Версия протокола"));
        box.setMaxLength(16);
        if (box instanceof EditBoxTrimLeftPad trim) {
            trim.petrovich$setTrimLeftPad(true);
        }
        box.setResponder(s -> {
            String text = s.trim();
            Integer parsed = viaParse(text);
            if (parsed != null) {
                viaApply(config, parsed);
            }
            box.setSuggestion(suggestionFor(text));
        });
        int current = viaClientVersion(config);
        if (current > 0) {
            box.setValue(viaProtocolName(current));
        } else {
            box.setSuggestion(suggestionFor(""));
        }
        Screens.getWidgets(screen).add(box);
    }

    private static String suggestionFor(String text) {
        if (text.isEmpty()) {
            return "1.17.1";
        }
        if ("1.17".equals(text)) {
            return ".1";
        }
        if ("1.21".equals(text)) {
            return ".4";
        }
        return null;
    }

    private static Object viaConfig() {
        try {
            Class<?> clazz = Class.forName(VIA_FABRIC_CLASS);
            Field config = clazz.getField("config");
            return config.get(null);
        } catch (Throwable t) {
            return null;
        }
    }

    private static int viaClientVersion(Object config) {
        try {
            Method method = config.getClass().getMethod("getClientSideVersion");
            return (int) method.invoke(config);
        } catch (Throwable t) {
            return -1;
        }
    }

    private static void viaApply(Object config, int version) {
        try {
            config.getClass().getMethod("setClientSideVersion", int.class).invoke(config, version);
            config.getClass().getMethod("setClientSideEnabled", boolean.class).invoke(config, true);
            config.getClass().getMethod("save").invoke(config);
        } catch (Throwable t) {
            // ignored
        }
    }

    private static String viaProtocolName(int version) {
        try {
            Method method = Class.forName(PROTOCOL_UTILS_CLASS).getMethod("getProtocolName", int.class);
            return (String) method.invoke(null, version);
        } catch (Throwable t) {
            return String.valueOf(version);
        }
    }

    private static Integer viaParse(String text) {
        try {
            Method method = Class.forName(PROTOCOL_UTILS_CLASS).getMethod("parseProtocolId", String.class);
            return (Integer) method.invoke(null, text);
        } catch (Throwable t) {
            return null;
        }
    }
}