package client_files.Petrovich.Misc;

import client_files.ClientikUtils.Category;
import client_files.Module;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.BlockHitResult;

public class UseTracker extends Module {

    private static int leftClicks;
    private static int rightClicks;
    private static int blocksBroken;

    private boolean prevLeft;
    private boolean prevRight;

    public UseTracker() {
        super("UseTracker", "Считает клики ЛКМ/ПКМ и сломанные блоки", Category.MISC);
    }

    @Override
    public void onTick() {
        LocalPlayer player = mc.player;
        if (player == null) return;
        boolean left = mc.options.keyAttack.isDown();
        boolean right = mc.options.keyUse.isDown();
        if (left && !prevLeft) {
            leftClicks++;
            if (mc.hitResult instanceof BlockHitResult) blocksBroken++;
        }
        if (right && !prevRight) {
            rightClicks++;
        }
        prevLeft = left;
        prevRight = right;
    }

    public static int getLeftClicks() {
        return leftClicks;
    }

    public static int getRightClicks() {
        return rightClicks;
    }

    public static int getBlocksBroken() {
        return blocksBroken;
    }
}