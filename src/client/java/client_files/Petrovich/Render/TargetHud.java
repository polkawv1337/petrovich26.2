package client_files.Petrovich.Render;

import client_files.ClientikUtils.Category;
import client_files.ClientikUtils.HudEditor;
import client_files.ClientikUtils.render.RRender;
import client_files.Module;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class TargetHud extends Module {

    public TargetHud() {
        super("TargetHud", "Информация о цели в прицеле", Category.RENDER);
        setHud(100000, 4);
    }

    @Override
    public void onRender(GuiGraphicsExtractor graphics) {
        LocalPlayer player = mc.player;
        if (player == null) return;
        
        int w = 130;
        int h = 44;
        int x = HudEditor.xPos(this, w, graphics.guiWidth());
        int y = HudEditor.yPos(this, h, graphics.guiHeight());

        RRender.panel(graphics, x, y, w, h);
        RRender.accentStrip(graphics, x + 6, y + 6, 2, h - 12, RRender.LAVENDER);

        Entity picked = mc.crosshairPickEntity;
        boolean hasTarget = picked instanceof LivingEntity target && target.isAlive();
        
        if (hasTarget) {
            // Отображение информации о цели
            LivingEntity target = (LivingEntity) picked;
            String name = target.getDisplayName().getString();
            RRender.text(graphics, name, x + 14, y + 6, 10f, RRender.LAVENDER);

            float hp = target.getHealth();
            float maxHp = target.getMaxHealth();
            float ratio = maxHp <= 0 ? 0 : Mth.clamp(hp / maxHp, 0.0f, 1.0f);

            int barX = x + 14;
            int barY = y + 23;
            int barW = w - 28;
            int barH = 5;
            RRender.rounded(graphics, barX, barY, barW, barH, barH / 2, 0xFF252B3C);
            int barColor = ratio >= 0.5f ? RRender.MINT : (ratio >= 0.25f ? RRender.LEMON : RRender.RED_SOFT);
            RRender.barRounded(graphics, barX, barY, barW, barH, ratio, barColor);

            String hpText = Mth.ceil(hp) + " / " + Mth.ceil(maxHp);
            RRender.text(graphics, hpText,
                    x + w - 14 - RRender.textWidth(hpText, 8f), y + 32, 8f, barColor);
        } else {
            // Отображение статистики игрока когда нет цели
            String playerName = player.getDisplayName().getString();
            RRender.text(graphics, playerName, x + 14, y + 6, 10f, RRender.MINT);

            float hp = player.getHealth();
            float maxHp = player.getMaxHealth();
            float ratio = maxHp <= 0 ? 0 : Mth.clamp(hp / maxHp, 0.0f, 1.0f);

            int barX = x + 14;
            int barY = y + 23;
            int barW = w - 28;
            int barH = 5;
            RRender.rounded(graphics, barX, barY, barW, barH, barH / 2, 0xFF252B3C);
            int barColor = ratio >= 0.5f ? RRender.MINT : (ratio >= 0.25f ? RRender.LEMON : RRender.RED_SOFT);
            RRender.barRounded(graphics, barX, barY, barW, barH, ratio, barColor);

            String hpText = Mth.ceil(hp) + " / " + Mth.ceil(maxHp);
            RRender.text(graphics, "HP: " + hpText,
                    x + w - 14 - RRender.textWidth("HP: " + hpText, 8f), y + 32, 8f, barColor);
        }

        HudEditor.place(this, x, y, w, h);
    }
}