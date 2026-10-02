package client_files.Petrovich.Render.Legacy;

import client_files.ClientikUtils.render.RRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class ArmorHudElement {

    private ArmorHudElement() {
    }

    public static int width() {
        return 18; // + 10 gap + 16*4 + 10 + 18
    }

    public static void render(GuiGraphicsExtractor g) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        Player player = mc.player;
        int[] armorSlots = {39, 38, 37, 36};
        int slotSize = 16;
        int gap = 10;
        int between = 1;

        ItemStack main = player.getInventory().getItem(36);
        ItemStack off = player.getInventory().getItem(40);
        boolean hasMain = !main.isEmpty();
        boolean hasOff = !off.isEmpty();

        int startX = calculateX(mc, hasMain, hasOff);
        int startY = mc.getWindow().getGuiScaledHeight() - 57;

        float count1 = hasMain ? 1 : 0;
        float count2 = count1 + 4;
        float count3 = count2 + (hasOff ? 1 : 0);

        float centerX = startX + count1 * 8.5f;
        float totalW = count3 * 8.5f + count3 * between;
        float adjustedStartX = centerX - totalW / 2f;

        float x = adjustedStartX;

        if (hasMain) {
            renderItem(g, mc, main, x, startY);
            x += slotSize + between;
        }
        for (int slot : armorSlots) {
            renderItem(g, mc, player.getInventory().getItem(slot), x, startY);
            x += slotSize + between;
        }
        if (hasOff) {
            renderItem(g, mc, off, x, startY);
        }
        ItemStack[] items = {main, player.getInventory().getItem(39), player.getInventory().getItem(38),
                player.getInventory().getItem(37), player.getInventory().getItem(36), off};
        int totalDurability = 0;
        int totalMax = 0;
        for (ItemStack item : items) {
            if (item.isEmpty()) {
                continue;
            }
            if (item.isDamageableItem()) {
                totalDurability += item.getMaxDamage() - item.getDamageValue();
                totalMax += item.getMaxDamage();
            }
        }
        if (totalMax > 0) {
            float ratio = (float) totalDurability / (float) totalMax;
            int durColor = ratio > 0.5f ? LegacyUtil.rgba(54, 190, 255, 255)
                    : (ratio > 0.25f ? LegacyUtil.rgba(255, 225, 77, 255)
                    : LegacyUtil.rgba(255, 77, 94, 255));
            String dur = String.format(java.util.Locale.US, "%.0f%%", ratio * 100f);
            float w = LegacyUtil.width(dur, 6.25f);
            LegacyUtil.round(g, adjustedStartX + 1f, startY + slotSize + 3f, totalW - 2f, 8f, 4f,
                    LegacyUtil.rgba(0, 0, 0, 195));
            LegacyUtil.text(g, dur, adjustedStartX + (totalW - w) / 2f,
                    startY + slotSize + 1.7f, 6.25f, durColor);
        }
    }

    private static int calculateX(Minecraft mc, boolean hasMain, boolean hasOff) {
        int slotSize = 16;
        float count = (hasMain ? 1 : 0) + 4 + (hasOff ? 1 : 0);
        float totalW = count * slotSize + (count - 1);
        return Math.round(mc.getWindow().getGuiScaledWidth() / 2f - totalW / 2f);
    }

    private static void renderItem(GuiGraphicsExtractor g, Minecraft mc, ItemStack stack, float x, float y) {
        g.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, RRender.SLOT,
                Math.round(x), Math.round(y), 16, 16);
        g.item(stack, Math.round(x) + 2, Math.round(y) + 2);
        if (stack.getCount() > 1) {
            LegacyUtil.text(g, String.valueOf(stack.getCount()), x + 1.2f, y + 5.5f, 4.5f, 0xFFFFFFFF);
        }
        if (stack.isDamaged()) {
            float ratio = 1f - ((float) stack.getDamageValue() / (float) stack.getMaxDamage());
            float width = 12f * ratio;
            int color = ratio > 0.75f ? 0xFF3CBFFF : (ratio > 0.4f ? 0xFFFFE14D : 0xFFFF4D5E);
            RRender.rounded(g, Math.round(x + 2f), Math.round(y + 13.8f), Math.round(width), 1, 1, color);
        }
    }
}