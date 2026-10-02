package client_files.Petrovich.Render.Legacy;

import client_files.ClientikUtils.render.RRender;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class TargetHud {

    private static final int PANEL_WIDTH = 118;
    private static final int PANEL_HEIGHT = 42;
    private static final float PANEL_RADIUS = 6f;

    private TargetHud() {
    }

    public static void render(GuiGraphicsExtractor g) {
        Minecraft mc = Minecraft.getInstance();
        Entity picked = mc.crosshairPickEntity;
        if (!(picked instanceof LivingEntity) || !picked.isAttackable()) {
            return;
        }
        LivingEntity target = (LivingEntity) picked;

        float x = 140;
        float y = 130;

        LegacyUtil.hud(g, x, y, PANEL_WIDTH, PANEL_HEIGHT, PANEL_RADIUS, 255);

        float headSize = 34f;
        int theme = LegacyUtil.theme();
        int dark = LegacyUtil.themeDark(0.6f);
        int textX = Math.round(x + headSize + 10f);

        if (target instanceof AbstractClientPlayer player) {
            Identifier skin = player.getSkin().body().texturePath();
            g.blit(skin, Math.round(x + 4f), Math.round(y + 4f), Math.round(headSize), Math.round(headSize),
                    8f / 64f, 16f / 64f, 8f / 64f, 16f / 64f);
        } else {
            RRender.rounded(g, Math.round(x + 4f), Math.round(y + 4f), Math.round(headSize), Math.round(headSize),
                    6, LegacyUtil.rgba(32, 32, 32, 255));
            LegacyUtil.text(g, "?", x + 2f, y + 4f, 14f, LegacyUtil.rgba(220, 220, 220, 255));
        }

        String name = target.getDisplayName() != null ? target.getDisplayName().getString() : "?";
        float nameW = LegacyUtil.width(name, 8.5f);
        float maxNameW = PANEL_WIDTH - 10f - headSize - 6f;
        if (nameW > maxNameW) {
            name = trim(g, name, maxNameW);
        }
        LegacyUtil.text(g, name, textX, y + 7.5f, 8.5f, 0xFFFFFFFF);

        float maxHp = target.getMaxHealth();
        float hp = target.getHealth();
        float ratio = Math.max(0f, Math.min(1f, hp / maxHp));

        float barW = PANEL_WIDTH - headSize - 16f;
        float barStartX = x + headSize + 10f;
        float barY = y + 17f;
        float barH = 4f;

        drawGradientBar(g, barStartX, barY, barW, barH, theme, dark, ratio);

        float abs = target.getAbsorptionAmount();
        if (abs > 0) {
            float absRatio = Math.max(0f, Math.min(1f, abs / 6f));
            drawGoldBar(g, barStartX, barY - 4f, barW, barH, absRatio);
        }

        float formatHp = (float) (Math.floor(hp * 10.0) / 10.0);
        String hpText = String.format(Locale.US, "%s", Mth.floor(formatHp));
        float hpTextW = LegacyUtil.width(hpText, 8.5f);
        float hpTextX = barStartX + (barW / 2f) - (hpTextW / 2f);
        LegacyUtil.text(g, hpText, hpTextX, barY - 1.5f, 8.5f, 0xFFFFFFFF);

        float markerX = barStartX + ratio * barW;
        RRender.triangle(g, Math.round(markerX), Math.round(barY + barH + 3f), 0, 5f,
                LegacyUtil.rgba(255, 255, 255, 200));

        renderEquipment(g, target, x, y);
    }

    private static void drawGradientBar(GuiGraphicsExtractor g, float x, float y, float w, float h, int left, int right, float ratio) {
        int segs = 18;
        float segW = w / segs;
        RRender.rounded(g, Math.round(x), Math.round(y), Math.round(w), Math.round(h), 3, LegacyUtil.rgba(0, 0, 0, 120));
        for (int i = 0; i < segs; i++) {
            float segStart = i * segW;
            if (segStart > w * ratio) {
                break;
            }
            int color = LegacyUtil.interpolate(left, right, i / (float) (segs - 1));
            RRender.rounded(g, Math.round(x + segStart), Math.round(y), Math.round(segW + 0.5f), Math.round(h), 3, color);
        }
    }

    private static void drawGoldBar(GuiGraphicsExtractor g, float x, float y, float w, float h, float ratio) {
        RRender.rounded(g, Math.round(x), Math.round(y), Math.round(w), Math.round(h), 3, LegacyUtil.rgba(0, 0, 0, 120));
        if (ratio > 0) {
            RRender.rounded(g, Math.round(x), Math.round(y), Math.round(w * ratio), Math.round(h), 3,
                    LegacyUtil.rgba(255, 210, 68, 255));
        }
    }

    private static String trim(GuiGraphicsExtractor g, String text, float maxWidth) {
        for (int i = text.length() - 1; i > 0; i--) {
            String sub = text.substring(0, i) + "...";
            if (LegacyUtil.width(sub, 8.5f) <= maxWidth) {
                return sub;
            }
        }
        return "...";
    }

    private static void renderEquipment(GuiGraphicsExtractor g, LivingEntity target, float x, float y) {
        if (!(target instanceof Player player)) {
            return;
        }
        int[] slots = {36, 39, 38, 37, 36, 40};
        int prev = -1;
        float size = 9.5f;
        float startX = x + PANEL_WIDTH - 8f - size;
        float cellY = y - size - 5f;

        if (player.getOffhandItem().isEmpty() && player.getMainHandItem().isEmpty()) {
            for (int i = 1; i <= 4; i++) {
                renderCell(g, player.getInventory().getItem(slots[i]), startX, cellY, size);
                startX -= size + 1f;
            }
            return;
        }

        for (int i = 0; i < 6; i++) {
            int slot = slots[i];
            if (slot == prev) {
                continue;
            }
            renderCell(g, player.getInventory().getItem(slot), startX, cellY, size);
            startX -= size + 1f;
            prev = slot;
        }
    }

    private static void renderCell(GuiGraphicsExtractor g, ItemStack stack, float x, float y, float size) {
        g.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, RRender.SLOT,
                Math.round(x), Math.round(y), Math.round(size), Math.round(size));
        g.item(stack, Math.round(x + 0.5f), Math.round(y + 0.5f));
    }
}