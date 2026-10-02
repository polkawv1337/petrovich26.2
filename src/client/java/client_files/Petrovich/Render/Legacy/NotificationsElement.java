package client_files.Petrovich.Render.Legacy;

import client_files.ClientikUtils.Notifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class NotificationsElement {

    private NotificationsElement() {
    }

    public static void render(GuiGraphicsExtractor g) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        float centerX = mc.getWindow().getGuiScaledWidth() / 2f;
        float startY = mc.getWindow().getGuiScaledHeight() / 2f + 20f;
        float offset = 0;

        for (int i = Notifier.count() - 1; i >= 0; i--) {
            float life = Notifier.life(i);
            if (life <= 0.01f) {
                continue;
            }
            String toast = Notifier.text(i).replace("\u00a7", "");
            boolean enabled = toast.contains("ВКЛ");
            String name = parseName(toast);

            float animValue = Math.min(1f, life);
            float clampedAlpha = (float) Math.max(0.0, Math.min(1.0, animValue));
            int alphaInt = (int) (255 * clampedAlpha);

            float height = 22f;
            float y = startY + offset;
            offset += (height + 3f) * clampedAlpha;

            String status = enabled ? "включена" : "выключена";
            String fullText = "Функция " + name;
            String statusText = status;
            String iconCode = "D";

            float textSize = 7f;
            float iconSize = 9f;
            float textWidth = LegacyUtil.width(fullText, textSize);
            float statusWidth = LegacyUtil.width(statusText, textSize);
            float iconWidth = LegacyUtil.width(iconCode, iconSize);
            float spaceW = LegacyUtil.width(" ", textSize);
            float segGap = spaceW * 2f;
            float width = 7f + iconWidth + segGap + textWidth + segGap + statusWidth + 7f;

            float x = centerX - (width / 2f);

            LegacyUtil.round(g, x, y, width, height, 6f, LegacyUtil.rgba(0, 0, 0, (int) (alphaInt * 0.85f)));

            int iconColor = enabled
                    ? LegacyUtil.rgba(0, 255, 0, alphaInt)
                    : LegacyUtil.rgba(255, 0, 0, alphaInt);
            LegacyUtil.text(g, iconCode, x + 7f, y + (height - iconSize) / 2f - 1f, iconSize, iconColor);

            float textY = y + 6.5f;
            float textX = x + 7f + iconWidth + segGap;
            LegacyUtil.text(g, fullText, textX, textY, textSize,
                    LegacyUtil.rgba(255, 255, 255, alphaInt));
        }
    }

    private static String parseName(String toast) {
        int idx = toast.indexOf("  ");
        if (idx > 0) {
            return toast.substring(0, idx);
        }
        idx = toast.indexOf(' ');
        if (idx > 0) {
            return toast.substring(0, idx);
        }
        return toast;
    }
}