package client_files.Petrovich.Render.Legacy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;

public final class SpeedPill {

    private SpeedPill() {
    }

    public static void render(GuiGraphicsExtractor g) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }
        double dx = player.getX() - player.xo;
        double dy = player.getY() - player.yo;
        double dz = player.getZ() - player.zo;
        double speed = Math.sqrt(dx * dx + dy * dy + dz * dz) * 20.0;
        String text = String.format(java.util.Locale.US, "%.2f", speed);
        float fontSize = 11f;
        float textWidth = LegacyUtil.width(text, fontSize);
        float x = mc.getWindow().getGuiScaledWidth() / 2f - textWidth / 2f;
        float y = mc.getWindow().getGuiScaledHeight() / 2f + 12f;
        LegacyUtil.text(g, text, x, y, fontSize, 0xFFFFFFFF);
    }
}