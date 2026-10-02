package client_files.Petrovich.Render;

import client_files.ClientikUtils.Category;
import client_files.ClientikUtils.HudEditor;
import client_files.ClientikUtils.render.RRender;
import client_files.Module;
import client_files.ModuleManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.lwjgl.glfw.GLFW;

public class HotKeys extends Module {

    public HotKeys() {
        super("HotKeys", "Список забинженных клавиш и колеса", Category.RENDER);
        setHud(100000, 48);
    }

    @Override
    public void onRender(GuiGraphicsExtractor graphics) {
        List<Module> bound = new ArrayList<>();
        for (Module m : ModuleManager.getInstance().getModules()) {
            if (m.isEnabled() && (m.getKey() != -1 || m.getScrollBind() != 0)) {
                bound.add(m);
            }
        }
        if (bound.isEmpty()) {
            return;
        }
        bound.sort(Comparator.comparing(Module::getName));

        int w = 130;
        int h = 24 + bound.size() * 11;
        int x = HudEditor.xPos(this, w, graphics.guiWidth());
        int y = HudEditor.yPos(this, h, graphics.guiHeight());

        RRender.panel(graphics, x, y, w, h);
        RRender.accentStrip(graphics, x + 8, y + 6, 2, h - 12, RRender.accent());
        RRender.text(graphics, "HOTKEYS", x + 14, y + 6, 8f, RRender.TEXT_FAINT);

        int rowY = y + 17;
        for (Module m : bound) {
            RRender.text(graphics, m.getName(), x + 14, rowY, 9f, RRender.TEXT);
            if (m.getScrollBind() != 0) {
                String stroke = m.getScrollBind() == 1 ? "^" : "v";
                RRender.text(graphics, stroke,
                        x + w - 12 - RRender.textWidth(stroke, 9f), rowY, 9f, RRender.accent());
            } else {
                String keyText = keyName(m.getKey());
                RRender.text(graphics, keyText,
                        x + w - 12 - RRender.textWidth(keyText, 9f), rowY, 9f, RRender.accent());
            }
            rowY += 11;
        }

        HudEditor.place(this, x, y, w, h);
    }

    private String keyName(int key) {
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