package client_files.Petrovich.Render.Legacy;

import client_files.ClientikUtils.render.RRender;
import client_files.ClientikUtils.render.RRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;

public final class LegacyUtil {

    private LegacyUtil() {
    }

    public static int rgba(int r, int g, int b, int a) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    public static int rgb(int r, int g, int b) {
        return 0xFF000000 | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    public static int setAlpha(int color, int alpha) {
        return ((Math.max(0, Math.min(255, alpha)) & 0xFF) << 24) | (color & 0x00FFFFFF);
    }

    public static int red(int c) {
        return (c >> 16) & 0xFF;
    }

    public static int green(int c) {
        return (c >> 8) & 0xFF;
    }

    public static int blue(int c) {
        return c & 0xFF;
    }

    public static int alpha(int c) {
        return (c >> 24) & 0xFF;
    }

    public static int brighten(int c, float factor) {
        return rgb(Math.min(255, (int) (red(c) * factor)),
                Math.min(255, (int) (green(c) * factor)),
                Math.min(255, (int) (blue(c) * factor)));
    }

    public static int interpolate(int from, int to, float t) {
        t = Math.max(0f, Math.min(1f, t));
        return rgba(
                (int) (red(from) * (1f - t) + red(to) * t),
                (int) (green(from) * (1f - t) + green(to) * t),
                (int) (blue(from) * (1f - t) + blue(to) * t),
                (int) (alpha(from) * (1f - t) + alpha(to) * t));
    }

    public static int theme() {
        return RRender.accent();
    }

    public static int themeDark(float factor) {
        int t = theme();
        return rgba((int) (red(t) * factor), (int) (green(t) * factor), (int) (blue(t) * factor), 255);
    }

    public static void round(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius, int color) {
        RRender.rounded(g, Math.round(x), Math.round(y), Math.round(w), Math.round(h), Math.round(radius), color);
    }

    public static void hud(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius, int alpha) {
        round(g, x, y, w, h, radius, rgba(0, 0, 0, Math.round(alpha * 0.5f)));
    }

    public static void text(GuiGraphicsExtractor g, String s, float x, float y, float size, int color) {
        RRender.text(g, s, x, y + 2f, size, color);
    }

    public static float width(String s, float size) {
        return RRender.textWidth(s, size);
    }

    public static void iconRotated(GuiGraphicsExtractor g, String ch, float x, float y,
                                   float size, int color, float angleDeg) {
        String s = String.valueOf(ch);
        float w = RRender.textWidth(s, size);
        float h = RRender.lineHeight(size);
        float cx = x + w / 2f;
        float cy = y + h / 2f;
        g.pose().pushMatrix();
        g.pose().translate(cx, cy);
        g.pose().rotate((float) Math.toRadians(angleDeg));
        RRender.text(g, s, -w / 2f, -h / 2f + 1f, size, color);
        g.pose().popMatrix();
    }

    public static int fps() {
        return Minecraft.getInstance().getFps();
    }

    public static int ping() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.connection == null) {
            return 0;
        }
        PlayerInfo info = player.connection.getPlayerInfo(player.getUUID());
        return info == null ? 0 : Math.max(0, info.getLatency());
    }

    public static String serverName() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getCurrentServer() != null) {
            return mc.getCurrentServer().name;
        }
        return "Singleplayer";
    }
}