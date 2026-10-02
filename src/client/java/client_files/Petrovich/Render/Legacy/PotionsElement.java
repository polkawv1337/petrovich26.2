package client_files.Petrovich.Render.Legacy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class PotionsElement {

    private PotionsElement() {
    }

    public static void render(GuiGraphicsExtractor g) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }
        Collection<MobEffectInstance> effects = player.getActiveEffects();
        if (effects.isEmpty()) {
            return;
        }

        List<Row> rows = new ArrayList<>();
        float maxW = 0;
        float textSize = 7f;
        float padY = 6f;
        float rowH = 11f;
        for (MobEffectInstance effect : effects) {
            String name = effect.getEffect().value().getDisplayName().getString();
            int seconds = Math.max(0, (effect.getDuration() + 19) / 20);
            String time = seconds + "s";
            float w = LegacyUtil.width(name, textSize)
                    + 8f + LegacyUtil.width(time, textSize);
            if (w > maxW) maxW = w;
            rows.add(new Row(name, time));
        }

        float panelW = maxW + 12f;
        float panelH = padY + rows.size() * rowH + padY;
        float x = mc.getWindow().getGuiScaledWidth() - 6f - panelW;
        float y = 4f;

        LegacyUtil.round(g, x, y, panelW, panelH, 6f, LegacyUtil.rgba(0, 0, 0, 217));

        long time = System.currentTimeMillis();
        float rowY = y + padY;
        int theme = LegacyUtil.theme();
        int dark = LegacyUtil.themeDark(0.6f);
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            LegacyUtil.round(g, x + 6f, rowY + 3f, 2f, 5f, 1f, wave(theme, dark, time, i * 3));
            LegacyUtil.text(g, row.name, x + 11f, rowY, textSize, 0xFFFFFFFF);
            LegacyUtil.text(g, row.time, x + panelW - 6f - LegacyUtil.width(row.time, textSize),
                    rowY, textSize, wave(theme, dark, time, i * 3 + 1));
            rowY += rowH;
        }
    }

    private static int wave(int from, int to, long time, int i) {
        double ph = time * (3.0 / 1000.0) + i * 0.35f;
        float p = (float) (Math.sin(ph) * 0.5 + 0.5);
        return LegacyUtil.interpolate(from, to, p);
    }

    private static final class Row {
        final String name;
        final String time;

        Row(String name, String time) {
            this.name = name;
            this.time = time;
        }
    }
}