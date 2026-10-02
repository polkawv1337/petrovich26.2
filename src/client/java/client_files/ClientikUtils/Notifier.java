package client_files.ClientikUtils;

import client_files.ClientikUtils.render.RRender;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class Notifier {

    private static final float LIFE = 2.4f;
    private static final float STEP = 0.06f;
    private static final List<Toast> TOASTS = new ArrayList<>();

    private static class Toast {
        final String text;
        final int color;
        float life;

        Toast(String text, int color, float life) {
            this.text = text;
            this.color = color;
            this.life = life;
        }
    }

    private Notifier() {
    }

    public static void push(String text, int color) {
        TOASTS.add(new Toast(text, color, LIFE));
        if (TOASTS.size() > 5) {
            TOASTS.remove(0);
        }
    }

    public static int count() {
        return TOASTS.size();
    }

    public static String text(int index) {
        return TOASTS.get(index).text;
    }

    public static int color(int index) {
        return TOASTS.get(index).color;
    }

    public static float life(int index) {
        return TOASTS.get(index).life;
    }

    private static int fade(int rgb, float alpha) {
        int a = (int) (Math.max(0f, Math.min(1f, alpha)) * 255f);
        return (a << 24) | (rgb & 0xFFFFFF);
    }

    public static void tick() {
        for (int i = TOASTS.size() - 1; i >= 0; i--) {
            Toast t = TOASTS.get(i);
            t.life -= STEP;
            if (t.life <= 0f) {
                TOASTS.remove(i);
            }
        }
    }

    public static void render(GuiGraphicsExtractor g) {
        if (TOASTS.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        int guiW = mc.getWindow().getGuiScaledWidth();
        int guiH = mc.getWindow().getGuiScaledHeight();
        int centerX = guiW / 2;
        float y = guiH - 26f;
        for (int i = TOASTS.size() - 1; i >= 0; i--) {
            Toast t = TOASTS.get(i);
            float alpha = Math.min(1f, t.life);
            int textW = (int) RRender.textWidth(t.text, 10f);
            RRender.textCenter(g, t.text, centerX, y, 10f, fade(0xFFFFFFFF, alpha));
            RRender.rounded(g, centerX - textW / 2, (int) y + 13, textW, 2, 1, fade(t.color, alpha));
            y -= 15f;
        }
    }
}