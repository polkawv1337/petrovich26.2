package client_files.Petrovich.Render.Legacy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.level.GameType;

public final class StaffListElement {

    private StaffListElement() {
    }

    public static void render(GuiGraphicsExtractor g) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null || mc.player == null) {
            return;
        }
        Collection<PlayerInfo> players = mc.getConnection().getOnlinePlayers();
        if (players == null || players.isEmpty()) {
            return;
        }

        List<PlayerInfo> staff = new ArrayList<>();
        for (PlayerInfo info : players) {
            GameType gm = info.getGameMode();
            if (gm == GameType.CREATIVE || gm == GameType.SPECTATOR) {
                staff.add(info);
            }
        }
        if (staff.isEmpty()) {
            return;
        }
        staff.sort(Comparator.comparingInt(PlayerInfo::getLatency));

        float textSize = 7f;
        float rowH = 11f;
        float padY = 6f;
        float maxNameW = 0;
        float maxPingW = 0;
        for (PlayerInfo info : staff) {
            String name = info.getProfile().name();
            String ping = Math.max(0, info.getLatency()) + "ms";
            float nw = LegacyUtil.width(name, textSize);
            float pw = LegacyUtil.width(ping, textSize);
            if (nw > maxNameW) maxNameW = nw;
            if (pw > maxPingW) maxPingW = pw;
        }

        float panelW = maxNameW + 10f + maxPingW + 6f + 12f;
        float panelH = padY + staff.size() * rowH + padY;
        float x = 6f;
        float y = Math.max(70f, 4f + (staff.size() * rowH) + 40f);

        LegacyUtil.round(g, x, y, panelW, panelH, 6f, LegacyUtil.rgba(0, 0, 0, 217));

        long time = System.currentTimeMillis();
        int theme = LegacyUtil.theme();
        int dark = LegacyUtil.themeDark(0.75f);
        float rowY = y + padY;
        for (int i = 0; i < staff.size(); i++) {
            PlayerInfo info = staff.get(i);
            String name = info.getProfile().name();
            int ping = Math.max(0, info.getLatency());
            String pingText = ping + "ms";
            int pingColor = ping <= 60 ? 0xFF54FF8A : (ping <= 120 ? 0xFFFFE14D : 0xFFFF4D5E);

            int color = wave(theme, dark, time, i * 2);
            LegacyUtil.text(g, name, x + 6f, rowY - 1f, textSize, color);
            LegacyUtil.text(g, pingText,
                    x + panelW - 6f - maxPingW, rowY - 1f, textSize, pingColor);
            rowY += rowH;
        }
    }

    private static int wave(int from, int to, long time, int i) {
        double ph = time * (3.0 / 1000.0) + i * 0.35f;
        float p = (float) (Math.sin(ph) * 0.5 + 0.5);
        return LegacyUtil.interpolate(from, to, p);
    }
}