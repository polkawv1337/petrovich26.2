package client_files.Petrovich.Movement;

import client_files.ClientikUtils.Category;
import client_files.Module;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class TridenBoost extends Module {

    public TridenBoost() {
        super("TridenBoost", "Разгон на трезубце в воде", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        LocalPlayer player = mc.player;
        if (player == null) return;
        if (player.getMainHandItem().getItem() != Items.TRIDENT) return;
        if (!player.isInWater()) return;
        boolean moving = player.getLastSentInput().forward() || player.getLastSentInput().backward()
                || player.getLastSentInput().left() || player.getLastSentInput().right() || player.getLastSentInput().jump();
        if (!moving) return;

        float yawRad = player.getYRot() * 0.017453292F;
        player.setDeltaMovement(new Vec3(-Math.sin(yawRad) * 0.8, 0.25, Math.cos(yawRad) * 0.8));
    }
}