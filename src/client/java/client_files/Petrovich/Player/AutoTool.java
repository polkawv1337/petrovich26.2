package client_files.Petrovich.Player;

import client_files.ClientikUtils.Category;
import client_files.Module;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import project.petrovich_26_2.mixin.client.MultiPlayerGameModeAccessor;

public class AutoTool extends Module {

    private int lastSlot = -1;
    private int toolSlot = -1;

    public AutoTool() {
        super("AutoTool", "Автоматически ставит лучший инструмент из хотбара", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        lastSlot = -1;
        toolSlot = -1;
    }

    @Override
    public void onDisable() {
        restore();
        lastSlot = -1;
        toolSlot = -1;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;
        if (mc.player.isCreative() || mc.player.isSpectator()) return;
        if (mc.gui.screen() != null) return;

        if (!isMining()) {
            restore();
            return;
        }

        BlockState state = mc.level.getBlockState(((BlockHitResult) mc.hitResult).getBlockPos());
        Inventory inventory = mc.player.getInventory();
        int current = inventory.getSelectedSlot();
        ItemStack held = inventory.getItem(current);

        int best = findBestSlot(state);
        if (best == -1 || best == current) return;
        if (getDestroySpeed(held, state) >= getDestroySpeed(inventory.getItem(best), state)) return;

        if (toolSlot != current) {
            lastSlot = current;
        }
        toolSlot = best;
        inventory.setSelectedSlot(best);
        ((MultiPlayerGameModeAccessor) mc.gameMode).petrovich$syncCarriedItem();
    }

    private boolean isMining() {
        if (!mc.options.keyAttack.isDown()) return false;
        return mc.hitResult instanceof BlockHitResult;
    }

    private void restore() {
        if (mc.player == null || mc.gameMode == null) return;
        if (lastSlot == -1 || toolSlot == -1) return;

        Inventory inventory = mc.player.getInventory();
        if (inventory.getSelectedSlot() == toolSlot && toolSlot != lastSlot) {
            inventory.setSelectedSlot(lastSlot);
            if (mc.getConnection() != null) {
                mc.getConnection().send(new ServerboundSetCarriedItemPacket(lastSlot));
            }
        }

        lastSlot = -1;
        toolSlot = -1;
    }

    public int getLastSlot() {
        return lastSlot;
    }

    private int findBestSlot(BlockState state) {
        Inventory inventory = mc.player.getInventory();
        int best = -1;
        float bestSpeed = 1.0f;

        if (state.is(Blocks.COBWEB)) {
            for (int slot = 0; slot < 9; slot++) {
                if (inventory.getItem(slot).is(Items.SHEARS)) {
                    return slot;
                }
            }
        }

        for (int slot = 0; slot < 9; slot++) {
            float speed = getDestroySpeed(inventory.getItem(slot), state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                best = slot;
            }
        }
        return best;
    }

    private float getDestroySpeed(ItemStack stack, BlockState state) {
        return stack.isEmpty() ? 1.0f : stack.getDestroySpeed(state);
    }
}
