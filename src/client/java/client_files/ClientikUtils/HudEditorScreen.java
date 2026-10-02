package client_files.ClientikUtils;

import client_files.ClientikUtils.render.RRender;
import client_files.Module;
import client_files.ModuleManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class HudEditorScreen extends Screen {

    private Module grabbed;
    private float grabDX;
    private float grabDY;

    public HudEditorScreen() {
        super(Component.literal(""));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        RRender.fill(graphics, 0, 0, width, height, 0x66060A18);

        String hint = "HUD EDITOR - зажми ЛКМ на панели и перетащи. ESC - сохранить и закрыть";
        int hw = (int) RRender.textWidth(hint, 8f);
        RRender.text(graphics, hint, (width - hw) / 2f, 6, 8f, RRender.TEXT_DIM);

        HudEditor.clear();
        for (Module module : ModuleManager.getInstance().getModules()) {
            if (module.isDraggable()) {
                module.onRender(graphics);
            }
        }

        for (Module module : ModuleManager.getInstance().getModules()) {
            if (!module.isDraggable()) {
                continue;
            }
            int[] r = HudEditor.rect(module);
            if (r == null) {
                continue;
            }
            int c = RRender.accent();
            RRender.rounded(graphics, r[0] - 1, r[1] - 1, r[2] + 2, 2, 1, c);
            RRender.rounded(graphics, r[0] - 1, r[1] + r[3] - 1, r[2] + 2, 2, 1, c);
            RRender.rounded(graphics, r[0] - 1, r[1] - 1, 2, r[3] + 2, 1, c);
            RRender.rounded(graphics, r[0] + r[2] - 1, r[1] - 1, 2, r[3] + 2, 1, c);
        }

        if (grabbed != null) {
            String info = grabbed.getName() + "   X " + Math.round(grabbed.getHudX())
                    + "  Y " + Math.round(grabbed.getHudY());
            RRender.text(graphics, info, 8, height - 16, 8f, RRender.TEXT_DIM);
        }

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int mx = (int) event.x();
        int my = (int) event.y();
        grabbed = HudEditor.grab(mx, my);
        if (grabbed != null && !grabbed.isDraggable()) {
            grabbed = null;
        }
        if (grabbed != null) {
            int[] r = HudEditor.rect(grabbed);
            grabDX = mx - r[0];
            grabDY = my - r[1];
            return true;
        }
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (grabbed != null) {
            grabbed.setHud((int) event.x() - grabDX, (int) event.y() - grabDY);
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (grabbed != null) {
            HudEditor.save();
            grabbed = null;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        if (event.key() == 256) {
            HudEditor.save();
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }
}