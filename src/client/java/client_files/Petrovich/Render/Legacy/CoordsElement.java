package client_files.Petrovich.Render.Legacy;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;

public final class CoordsElement {

    private CoordsElement() {
    }

    public static void render(GuiGraphicsExtractor g, float x, float y) {
        LocalPlayer player = net.minecraft.client.Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        int cxInt = (int) player.getX();
        int cyInt = (int) player.getY();
        int czInt = (int) player.getZ();
        String coordX = String.valueOf(cxInt);
        String coordY = String.valueOf(cyInt);
        String coordZ = String.valueOf(czInt);

        double deltaX = player.getX() - player.xo;
        double deltaY = player.getY() - player.yo;
        double deltaZ = player.getZ() - player.zo;
        double speed = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) * 20;
        String speedStr = String.format(java.util.Locale.US, "%.1f", speed);

        int themeColor = LegacyUtil.theme();
        int themeDark = LegacyUtil.themeDark(0.75f);
        long time = System.currentTimeMillis();

        float padding = 5f;
        float gap = 4f;
        float textSize = 7f;
        float iconSize = 6f;
        float height = 16.5f;

        float coordsIconW = LegacyUtil.width("\uF70C", iconSize);
        float speedIconW = LegacyUtil.width("@", iconSize);

        String[] labels = {"x", ", y", ", z"};
        float labelsW = 0;
        for (String label : labels) {
            labelsW += LegacyUtil.width(label, textSize);
        }

        float coordsValW = charWidthSum(coordX, textSize) + charWidthSum(coordY, textSize) + charWidthSum(coordZ, textSize);
        float speedValW = charWidthSum(speedStr, textSize);
        float speedLabelW = LegacyUtil.width("Bps  ", textSize);

        float totalW = padding + coordsIconW + 2f + labelsW + coordsValW + gap
                + speedIconW + 2f + speedValW + 2f + speedLabelW + padding;

        LegacyUtil.round(g, x, y, totalW, height, 6f, LegacyUtil.rgba(0, 0, 0, 217));

        int labelColor = 0xFFFFFFFF;
        int iconColor = LegacyUtil.theme();
        float cx = x + padding;
        float textY = y + 4f;

        LegacyUtil.text(g, "\uF70C", cx, y + 4f, iconSize, iconColor);
        cx += coordsIconW + 2f;

        String[] valStrs = {coordX, coordY, coordZ};
        for (int idx = 0; idx < 3; idx++) {
            String label = labels[idx];
            LegacyUtil.text(g, label, cx, textY, textSize, labelColor);
            cx += LegacyUtil.width(label, textSize);

            String val = valStrs[idx];
            for (int ci = 0; ci < val.length(); ci++) {
                String ch = String.valueOf(val.charAt(ci));
                int color = wave(themeColor, themeDark, time, idx * 10 + ci);
                LegacyUtil.text(g, ch, cx, textY, textSize, color);
                cx += LegacyUtil.width(ch, textSize);
            }
        }

        cx += gap;

        LegacyUtil.text(g, "@", cx, y + 5f, iconSize, iconColor);
        cx += speedIconW + 2f;

        for (int i = 0; i < speedStr.length(); i++) {
            String ch = String.valueOf(speedStr.charAt(i));
            int color = wave(themeColor, themeDark, time, 30 + i);
            LegacyUtil.text(g, ch, cx, textY, textSize, color);
            cx += LegacyUtil.width(ch, textSize);
        }
        cx += 2f;
        LegacyUtil.text(g, "Bps", cx, textY, textSize, labelColor);
    }

    private static float charWidthSum(String text, float size) {
        float w = 0;
        for (int i = 0; i < text.length(); i++) {
            w += LegacyUtil.width(String.valueOf(text.charAt(i)), size);
        }
        return w;
    }

    private static int wave(int from, int to, long time, int i) {
        double ph = time * (3.0 / 1000.0) + i * 0.35f;
        float p = (float) (Math.sin(ph) * 0.5 + 0.5);
        return LegacyUtil.interpolate(from, to, p);
    }
}