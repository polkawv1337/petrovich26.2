package client_files.Petrovich.Render;

import client_files.ClientikUtils.Category;
import client_files.ClientikUtils.HudEditor;
import client_files.ClientikUtils.render.RRender;
import client_files.Module;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class ArmorHud extends Module {

    private static final int CELL = 16;
    private static final int GAP = 2;
    private static final int SENTINEL = 100000;

    public ArmorHud() {
        super("ArmorHud", "Броня ячейками как в хотбаре, справа от хотбара", Category.RENDER);
        setHud(SENTINEL, SENTINEL);
    }

    @Override
    public boolean isDraggable() {
        return true;
    }

    @Override
    public void onRender(GuiGraphicsExtractor graphics) {
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }

        int gw = graphics.guiWidth();
        int gh = graphics.guiHeight();

        Inventory inventory = player.getInventory();
        ItemStack[] armor = {
                inventory.getItem(39),
                inventory.getItem(38),
                inventory.getItem(37),
                inventory.getItem(36)
        };

        if (getHudX() >= SENTINEL || getHudY() >= SENTINEL) {
            setHud(gw / 2 + 91 + 6, gh - 22);
        }

        int total = 4 * CELL + 3 * GAP;
        int x = HudEditor.xPos(this, total, gw);
        int y = HudEditor.yPos(this, CELL, gh);

        int cur = x;
        for (ItemStack stack : armor) {
            renderCell(graphics, cur, y, stack);
            cur += CELL + GAP;
        }

        HudEditor.place(this, x, y, total, CELL);
    }

    private void renderCell(GuiGraphicsExtractor graphics, int x, int y, ItemStack stack) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, RRender.SLOT, x, y, CELL, CELL);
        if (stack != null && !stack.isEmpty()) {
            graphics.item(stack, x, y);
        }
    }
}