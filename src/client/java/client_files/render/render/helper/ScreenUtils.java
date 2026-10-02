package client_files.render.render.helper;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;

public final class ScreenUtils {
    private ScreenUtils() {}

    public static Window getWindow() {
        return Minecraft.getInstance().getWindow();
    }

    public static int getWidth() {
        Window window = getWindow();
        return window != null ? window.getGuiScaledWidth() : 1920;
    }

    public static int getHeight() {
        Window window = getWindow();
        return window != null ? window.getGuiScaledHeight() : 1080;
    }

    public static double getGuiScale() {
        Window window = getWindow();
        return window != null ? window.getGuiScale() : 2.0;
    }
}
