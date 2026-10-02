package client_files.Petrovich.Render;

import client_files.ClientikUtils.Category;
import client_files.Module;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class Tps extends Module {

    private float tps;
    private long lastNs;

    public Tps() {
        super("Tps", "Показывает TPS сервера", Category.RENDER);
        setHud(4, 50);
    }

    @Override
    public void onTick() {
        long now = System.nanoTime();
        if (lastNs != 0) {
            long delta = now - lastNs;
            float instant = delta > 0 ? 1_000_000_000f / delta : 20.0f;
            tps = tps <= 0 ? instant : tps * 0.8f + instant * 0.2f;
        }
        lastNs = now;
    }

    @Override
    public void onRender(GuiGraphicsExtractor graphics) {
    }

    public float getTps() {
        return tps;
    }
}