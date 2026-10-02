package client_files.Petrovich.Render.Legacy;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class WatermarkElement {

    private static final String PERSON_ICON = "\uE4C2";
    private static final String PING_ICON = "\uF0AC";

    private WatermarkElement() {
    }

    public static void render(GuiGraphicsExtractor g, float x, float y) {
        float padL = 8f;
        float midGap = 6f;
        float midRightGap = 6f;
        float trailing = 6f;
        float textSize = 7f;
        float glyphSize = 7f;
        float height = 16.5f;
        float personSize = 7f;
        float faceIconSize = 8f;

        String brand = "Petrovich Client";
        String name = System.getProperty("petrovich.player", "NesqsuikGuard");
        String fpsText = LegacyUtil.fps() + " Fps";
        String pingText = LegacyUtil.ping() + " Ping";
        String serverName = LegacyUtil.serverName();

        float brandW = LegacyUtil.width(brand, textSize);
        float nameW = LegacyUtil.width(name, textSize);
        float personW = LegacyUtil.width(PERSON_ICON, personSize);
        float faceIconW = LegacyUtil.width("\uE001", faceIconSize);
        float fpsW = LegacyUtil.width(fpsText, textSize);
        float pingIconW = LegacyUtil.width(PING_ICON, glyphSize);
        float pingW = LegacyUtil.width(pingText, textSize);
        float serverIconW = LegacyUtil.width("$", glyphSize);
        float serverNameW = LegacyUtil.width(serverName, textSize);

        float midW = brandW + midGap + personW + 2f + nameW + midGap + faceIconW + 2f + fpsW + midGap + pingIconW + 2f + pingW;
        float rightW = serverIconW + 2f + serverNameW;
        float rightX = x + padL + midW + midRightGap;
        float totalW = (rightX - x) + rightW + trailing;

        LegacyUtil.hud(g, x, y, totalW, height, 6f, 217);

        float textY = y;
        int theme = LegacyUtil.theme();
        int dark = LegacyUtil.themeDark(0.75f);
        int iconColor = LegacyUtil.theme();
        long time = System.currentTimeMillis();

        float currentX = x + padL;
        for (int i = 0; i < brand.length(); i++) {
            String ch = String.valueOf(brand.charAt(i));
            int color = wave(theme, dark, time, i);
            LegacyUtil.text(g, ch, currentX, textY + 4f, textSize, color);
            currentX += LegacyUtil.width(ch, textSize);
        }
        currentX += midGap;

        int personColor = wave(theme, dark, time, 0);
        LegacyUtil.text(g, PERSON_ICON, currentX, textY + (height - personSize) / 2f, personSize, personColor);
        currentX += personW + 2f;

        LegacyUtil.text(g, name, currentX, textY + 4f, textSize, 0xFFFFFFFF);
        currentX += nameW + midGap;

        LegacyUtil.text(g, "\uE001", currentX, textY + (height - faceIconSize) / 2f, faceIconSize, iconColor);
        currentX += faceIconW + 2f;

        LegacyUtil.text(g, fpsText, currentX, textY + 4f, textSize, 0xFFFFFFFF);
        currentX += fpsW + midGap;

        LegacyUtil.text(g, PING_ICON, currentX, textY + 4.25f, glyphSize, iconColor);
        currentX += pingIconW + 2f;

        LegacyUtil.text(g, pingText, currentX, textY + 4f, textSize, 0xFFFFFFFF);

        LegacyUtil.text(g, "$", rightX, textY + 5.25f, glyphSize, iconColor);
        LegacyUtil.text(g, serverName, rightX + serverIconW + 2f, textY + 4f, textSize, 0xFFFFFFFF);
    }

    private static int wave(int from, int to, long time, int i) {
        float p = (float) (Math.sin(time * 0.003d + i * 0.35f) * 0.5d + 0.5d);
        return LegacyUtil.interpolate(from, to, p);
    }
}