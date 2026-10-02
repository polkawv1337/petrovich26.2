package client_files.Petrovich.Render;

import client_files.ClientikUtils.Category;
import client_files.ClientikUtils.HudEditor;
import client_files.ClientikUtils.render.RRender;
import client_files.Module;
import java.util.Collection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;

public class Potions extends Module {

    public Potions() {
        super("Potions", "Список активных эффектов", Category.RENDER);
        setHud(100000, 4);
    }

    @Override
    public void onRender(GuiGraphicsExtractor graphics) {
        LocalPlayer player = mc.player;
        if (player == null) return;

        Collection<MobEffectInstance> effects = player.getActiveEffects();
        if (effects.isEmpty()) {
            return;
        }

        int w = 118;
        int h = 26 + effects.size() * 13;
        int x = HudEditor.xPos(this, w, graphics.guiWidth());
        int y = HudEditor.yPos(this, h, graphics.guiHeight());

        RRender.panel(graphics, x, y, w, h);
        RRender.accentStrip(graphics, x + 6, y + 6, 2, h - 12, RRender.PINK);
        RRender.text(graphics, "EFFECTS", x + 14, y + 6, 8f, RRender.TEXT_FAINT);

        String count = String.valueOf(effects.size());
        int cw = (int) RRender.textWidth(count, 8f) + 12;
        RRender.rounded(graphics, x + w - 14 - cw, y + 4, cw, 12, 4, 0x1AFFFFFF);
        RRender.text(graphics, count, x + w - 8 - cw, y + 5, 8f, RRender.PINK);

        int rowY = y + 18;
        for (MobEffectInstance effect : effects) {
            String name = effect.getEffect().value().getDisplayName().getString();
            int seconds = Math.max(0, effect.getDuration() / 20);
            String time = seconds + "s";
            RRender.text(graphics, name, x + 14, rowY + 1, 9f, RRender.TEXT_DIM);
            RRender.text(graphics, time,
                    x + w - 12 - RRender.textWidth(time, 9f), rowY + 1, 9f, RRender.PINK);
            rowY += 13;
        }

        HudEditor.place(this, x, y, w, h);
    }
}