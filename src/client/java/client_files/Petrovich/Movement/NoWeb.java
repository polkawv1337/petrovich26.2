package client_files.Petrovich.Movement;

import client_files.ClientikUtils.Category;
import client_files.ClientikUtils.SliderSetting;
import client_files.Module;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class NoWeb extends Module {

    private final SliderSetting speed = addSetting(new SliderSetting("Скорость в паутине", 0.4f, 0.05f, 1.0f, 0.05f));

    public NoWeb() {
        super("NoWeb", "Не застревать в паутине", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        LocalPlayer player = mc.player;
        if (player == null || !isInWeb(player)) return;

        float yawRad = player.getYRot() * 0.017453292F;
        float forward = (player.getLastSentInput().forward() ? 1 : 0) - (player.getLastSentInput().backward() ? 1 : 0);
        float strafe = (player.getLastSentInput().left() ? 1 : 0) - (player.getLastSentInput().right() ? 1 : 0);
        double mul = speed.getValue();
        double x = (-Math.sin(yawRad) * forward + Math.cos(yawRad) * strafe) * mul;
        double z = (Math.cos(yawRad) * forward + Math.sin(yawRad) * strafe) * mul;
        double y = player.getDeltaMovement().y;
        if (player.getLastSentInput().jump()) {
            y = 0.4;
        }
        player.setDeltaMovement(new Vec3(x, y, z));
    }

    private boolean isInWeb(LocalPlayer player) {
        Level level = mc.level;
        if (level == null) return false;
        AABB box = player.getBoundingBox();
        for (int x = Mth.floor(box.minX); x <= Mth.ceil(box.maxX); x++) {
            for (int y = Mth.floor(box.minY); y <= Mth.ceil(box.maxY); y++) {
                for (int z = Mth.floor(box.minZ); z <= Mth.ceil(box.maxZ); z++) {
                    BlockState state = level.getBlockState(new BlockPos(x, y, z));
                    if (state.is(Blocks.COBWEB)) return true;
                }
            }
        }
        return false;
    }
}