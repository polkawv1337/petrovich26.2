package client_files.Petrovich.Render;

import client_files.ClientikUtils.Category;
import client_files.Module;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public class Bps extends Module {

    private Vec3 lastPos;
    private long lastMs;
    private double bps;

    public Bps() {
        super("Bps", "Скорость движения (блоков в секунду)", Category.RENDER);
        setHud(4, 90);
    }

    @Override
    public void onTick() {
        LocalPlayer player = mc.player;
        if (player == null) return;
        long now = System.currentTimeMillis();
        if (lastPos != null && lastMs != 0) {
            double dist = lastPos.distanceTo(player.position());
            double secs = (now - lastMs) / 1000.0;
            if (secs > 0) {
                bps = bps * 0.8 + (dist / secs) * 0.2;
            }
        }
        lastPos = player.position();
        lastMs = now;
    }

    @Override
    public void onRender(GuiGraphicsExtractor graphics) {
    }

    public double getBps() {
        return bps;
    }
}