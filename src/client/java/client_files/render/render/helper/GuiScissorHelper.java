package client_files.render.render.helper;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class GuiScissorHelper {
    private GuiScissorHelper() {}

    public static boolean push(GuiGraphicsExtractor extractor, float x, float y, float w, float h) {
        if (extractor == null || w <= 1.0f || h <= 1.0f) return false;
        try {
            int minX = Math.round(x);
            int minY = Math.round(y);
            extractor.enableScissor(minX, minY, minX + Math.round(w), minY + Math.round(h));
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static void pop(GuiGraphicsExtractor extractor) {
        if (extractor == null) return;
        try {
            extractor.disableScissor();
        } catch (Throwable ignored) {}
    }
}
