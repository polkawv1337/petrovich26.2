package client_files.Petrovich.Render;

import client_files.ClientikUtils.Category;
import client_files.ClientikUtils.HudEditor;
import client_files.ClientikUtils.render.RRender;
import client_files.Module;
import client_files.ModuleManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;

public class Watermark extends Module {

    private long lastFrame = System.currentTimeMillis();
    private float fps;
    private Tps tps;
    private Bps bps;
    private Coords coords;

    public Watermark() {
        super("Watermark", "Водяной знак клиента - FPS, TPS, скорость и координаты", Category.RENDER);
        setHud(4, 4);
    }

    @Override
    public boolean isDraggable() {
        return true; // Ватермарка не перемещаемая
    }

    @Override
    public void onRender(GuiGraphicsExtractor graphics) {
        long now = System.currentTimeMillis();
        long diff = now - lastFrame;
        lastFrame = now;
        if (diff > 0) {
            fps = fps * 0.9f + (1000f / diff) * 0.1f;
        }
        if (tps == null) tps = ModuleManager.getInstance().get(Tps.class);
        if (bps == null) bps = ModuleManager.getInstance().get(Bps.class);
        if (coords == null) coords = ModuleManager.getInstance().get(Coords.class);

        LocalPlayer player = mc.player;
        String logo = "\uE001";
        String title = "PETROVICH";
        String version = "26.2";
        String fpsValue = Math.round(fps) + "";
        String tpsValue = tps != null && tps.isEnabled() ? String.format("%.1f", tps.getTps()) : null;
        String bpsValue = bps != null && bps.isEnabled() ? String.format("%.2f", bps.getBps()) : null;
        String coordsValue = coords != null && coords.isEnabled() && player != null
                ? trunc(player.getX()) + " " + trunc(player.getY()) + " " + trunc(player.getZ())
                : null;

        int h = 24;
        float size = 11f;
        float padX = 10f;
        float iconGap = 5f;
        float segGap = 11f;

        float w = padX;
        w += RRender.textWidth("\uE001", size) + 6f;
        w += RRender.textWidth(title, size) + 4f + RRender.textWidth(version, size);
        w += segGap;
        w += RRender.textWidth("\uE004", size) + iconGap + RRender.textWidth("FPS " + fpsValue, size);
        if (tpsValue != null) {
            w += segGap + RRender.textWidth("\uE003", size) + iconGap + RRender.textWidth("TPS " + tpsValue, size);
        }
        if (bpsValue != null) {
            w += segGap + RRender.textWidth("\uE000", size) + iconGap + RRender.textWidth("BPS " + bpsValue, size);
        }
        if (coordsValue != null) {
            w += segGap + RRender.textWidth("\uE002", size) + iconGap + RRender.textWidth("XYZ " + coordsValue, size);
        }
        w += padX;

        int width = Math.round(w);
        int x = HudEditor.xPos(this, width, graphics.guiWidth());
        int y = HudEditor.yPos(this, h, graphics.guiHeight());

        RRender.panel(graphics, x, y, width, h);
        RRender.accentStrip(graphics, x + 8, y + 6, 2, h - 12, RRender.LEMON);

        int fpsCol = fps >= 120 ? RRender.MINT : (fps >= 60 ? RRender.LEMON : RRender.RED_SOFT);
        int lineY = y + Math.round((h - size) / 2f) + 1;

        float cursor = x + padX;
        cursor = draw(title, graphics, cursor, lineY, size, RRender.LEMON) + 6f;
        cursor = draw(version, graphics, cursor, lineY, size, RRender.CYAN) + segGap;
        cursor = draw("FPS " + fpsValue, graphics, cursor, lineY, size, fpsCol) + segGap;
        if (tpsValue != null) {
            cursor = draw("TPS " + tpsValue, graphics, cursor, lineY, size, RRender.MINT) + segGap;
        }
        if (bpsValue != null) {
            cursor = draw("BPS " + bpsValue, graphics, cursor, lineY, size, RRender.LEMON) + segGap;
        }
        if (coordsValue != null) {
            draw("XYZ " + coordsValue, graphics, cursor, lineY, size, RRender.LAVENDER);
        }

        HudEditor.place(this, x, y, width, h);
    }

    private float draw(String text, GuiGraphicsExtractor graphics, float x, float y, float size, int color) {
        RRender.text(graphics, text, x, y, size, color);
        return x + RRender.textWidth(text, size);
    }

    private String trunc(double value) {
        return String.format("%.1f", value);
    }
}