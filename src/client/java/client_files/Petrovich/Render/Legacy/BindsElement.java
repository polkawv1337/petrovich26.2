package client_files.Petrovich.Render.Legacy;

import client_files.Module;
import client_files.ModuleManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.lwjgl.glfw.GLFW;

public final class BindsElement {

    private BindsElement() {
    }

    public static void render(GuiGraphicsExtractor g, float x, float y) {
        List<Module> bound = new ArrayList<>();
        for (Module m : ModuleManager.getInstance().getModules()) {
            if (m.isEnabled() && m.getKey() != -1) {
                bound.add(m);
            }
        }
        if (bound.isEmpty()) {
            return;
        }
        bound.sort(java.util.Comparator.comparing(Module::getName));

        float headerHeight = 15f;
        float itemHeight = 12f;
        float gap = 1.5f;

        String headerStr = "KeyBinds";
        float headerTextW = LegacyUtil.width(headerStr, 8f);

        char kbIcon = (char) 0xF11C;
        float kbIconSize = 6.5f;
        float kbIconW = LegacyUtil.width(String.valueOf(kbIcon), kbIconSize);
        float kbGap = 2f;

        float maxLabelW = 0;
        float maxBindW = 0;
        for (Module m : bound) {
            float labelW = LegacyUtil.width(m.getName(), 6.75f);
            if (labelW > maxLabelW) maxLabelW = labelW;
            float bindW = LegacyUtil.width(keyName(m.getKey()), 6.75f);
            if (bindW > maxBindW) maxBindW = bindW;
        }

        float rectW = Math.max(18f, maxBindW + 4f);
        float headerNeed = 6f + headerTextW + 10f + kbIconW + kbGap + 4f;
        float itemsNeed = (6f + maxLabelW + 7f) + 5f + rectW + 5f;
        float panelW = Math.max(headerNeed, itemsNeed);

        float contentH = 0;
        for (Module ignored : bound) {
            contentH += itemHeight + gap;
        }
        float totalH = headerHeight + contentH;

        LegacyUtil.round(g, x, y, panelW, totalH, 6f, LegacyUtil.rgba(0, 0, 0, 217));

        float headerY = y + (headerHeight - 7f) / 2f;
        int theme = LegacyUtil.theme();
        int dark = LegacyUtil.themeDark(0.75f);
        long time = System.currentTimeMillis();

        float hx = x + 6f;
        for (int i = 0; i < headerStr.length(); i++) {
            double ph = time * (3.0 / 1000.0) + i * 0.35f;
            float p = (float) (Math.sin(ph) * 0.5 + 0.5);
            int color = LegacyUtil.interpolate(theme, dark, p);
            String ch = String.valueOf(headerStr.charAt(i));
            LegacyUtil.text(g, ch, hx, headerY, 8f, color);
            hx += LegacyUtil.width(ch, 8f);
        }
        LegacyUtil.text(g, String.valueOf(kbIcon),
                x + panelW - 5f - kbIconW, headerY + 0.4f, kbIconSize, LegacyUtil.theme());
        LegacyUtil.round(g, x + 6f, y + headerHeight, panelW - 12f, 1f, 0f,
                LegacyUtil.rgba(255, 255, 255, 18));

        float curY = y + headerHeight + gap;
        for (Module m : bound) {
            float textY = curY + (itemHeight - 7f) / 2f - 0.1f;
            LegacyUtil.text(g, m.getName(), x + 6f, textY, 6.75f, 0xFFFFFFFF);
            String bindStr = keyName(m.getKey());
            float bindW = LegacyUtil.width(bindStr, 6.75f);
            LegacyUtil.text(g, bindStr, x + panelW - 5f - bindW, textY - 0.8f,
                    6.75f, LegacyUtil.rgba(170, 170, 170, 255));
            curY += itemHeight + gap;
        }
    }

    private static String keyName(int key) {
        if (key >= 290 && key <= 301) {
            return "F" + (key - 289);
        }
        String name = GLFW.glfwGetKeyName(key, 0);
        if (name != null && !name.isEmpty()) {
            return name.toUpperCase();
        }
        return "B" + key;
    }
}