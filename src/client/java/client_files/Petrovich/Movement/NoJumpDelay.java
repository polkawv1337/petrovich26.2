package client_files.Petrovich.Movement;

import client_files.ClientikUtils.Category;
import client_files.Module;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

public class NoJumpDelay extends Module {

    public NoJumpDelay() {
        super("NoJumpDelay", "Мгновенный прыжок без задержки", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        LocalPlayer player = mc.player;
        if (player == null || !player.getLastSentInput().jump()) return;
        if (!player.onGround() || player.isPassenger() || player.isFallFlying()) return;
        if (player.isInWater()) return;

        Vec3 vel = player.getDeltaMovement();
        double jumpPower = player.getAttributeValue(Attributes.JUMP_STRENGTH) * 0.42;
        double newY = Math.max(vel.y, jumpPower);
        player.setDeltaMovement(vel.x, newY, vel.z);
    }
}