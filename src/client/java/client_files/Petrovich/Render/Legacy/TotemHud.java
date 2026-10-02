package client_files.Petrovich.Render.Legacy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class TotemHud {

    private TotemHud() {
    }

    public static void render(GuiGraphicsExtractor g) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        int totems = countTotems(mc.player.getInventory());
        if (totems <= 0) {
            return;
        }

        float textSize = 8f;
        String count = String.valueOf(totems);
        float countW = LegacyUtil.width(count, textSize);
        float iconW = 13f;
        float pad = 7f;
        float gap = 3f;
        float width = pad + iconW + gap + countW + pad;
        float height = 20f;

        float centerX = mc.getWindow().getGuiScaledWidth() / 2f;
        float y = mc.getWindow().getGuiScaledHeight() - 66f;
        float x = centerX - width / 2f;

        LegacyUtil.round(g, x, y, width, height, 6f, LegacyUtil.rgba(0, 0, 0, 217));
        g.item(new ItemStack(Items.TOTEM_OF_UNDYING), Math.round(x + pad), Math.round(y + (height - 13f) / 2f));
        LegacyUtil.text(g, count, x + pad + iconW + gap, y + (height - textSize) / 2f - 0.5f,
                textSize, 0xFFFFFFFF);
    }

    private static int countTotems(Inventory inventory) {
        int count = 0;
        for (int i = 0; i <= 36; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                count += stack.getCount();
            }
        }
        ItemStack off = inventory.getItem(40);
        if (off.getItem() == Items.TOTEM_OF_UNDYING) {
            count += off.getCount();
        }
        return count;
    }
}