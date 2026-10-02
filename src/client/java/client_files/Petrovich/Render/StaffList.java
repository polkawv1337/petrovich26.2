package client_files.Petrovich.Render;

import client_files.ClientikUtils.Category;
import client_files.ClientikUtils.HudEditor;
import client_files.ClientikUtils.render.RRender;
import client_files.Module;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.level.GameType;

public class StaffList extends Module {

    public StaffList() {
        super("StaffList", "Список игроков с административным статусом", Category.RENDER);
        setHud(4, 170);
    }

    @Override
    public void onRender(GuiGraphicsExtractor graphics) {
        ClientPacketListener connection = mc.getConnection();
        if (connection == null) {
            return;
        }
        Collection<PlayerInfo> players = connection.getOnlinePlayers();
        if (players == null || players.isEmpty()) {
            return;
        }

        List<PlayerInfo> staff = new ArrayList<>();
        for (PlayerInfo info : players) {
            GameType gameMode = info.getGameMode();
            if (gameMode == GameType.CREATIVE || gameMode == GameType.SPECTATOR) {
                staff.add(info);
            }
        }
        if (staff.isEmpty()) {
            return;
        }
        staff.sort(Comparator.comparingInt(PlayerInfo::getLatency));

        int w = 130;
        int h = 22 + staff.size() * 11;
        int x = HudEditor.xPos(this, w, graphics.guiWidth());
        int y = HudEditor.yPos(this, h, graphics.guiHeight());

        RRender.panel(graphics, x, y, w, h);
        RRender.accentStrip(graphics, x + 8, y + 6, 2, h - 12, RRender.MINT);
        RRender.text(graphics, "STAFF", x + 14, y + 6, 8f, RRender.TEXT_FAINT);

        String count = String.valueOf(staff.size());
        int cw = (int) RRender.textWidth(count, 8f) + 12;
        RRender.rounded(graphics, x + w - 14 - cw, y + 4, cw, 12, 4, 0x1AFFFFFF);
        RRender.text(graphics, count, x + w - 8 - cw, y + 5, 8f, RRender.MINT);

        int rowY = y + 17;
        for (PlayerInfo info : staff) {
            String name = info.getProfile().name();
            RRender.text(graphics, name, x + 14, rowY, 9f, RRender.MINT);

            int latency = info.getLatency();
            String ping = latency + "ms";
            int pingColor = latency <= 60 ? RRender.MINT : (latency <= 120 ? RRender.LEMON : RRender.RED_SOFT);
            RRender.text(graphics, ping,
                    x + w - 12 - RRender.textWidth(ping, 9f), rowY, 9f, pingColor);
            rowY += 11;
        }

        HudEditor.place(this, x, y, w, h);
    }
}