package client_files.Petrovich.Movement;

import client_files.ClientikUtils.Category;
import client_files.ClientikUtils.SliderSetting;
import client_files.Module;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public class DragonFly extends Module {

    private final SliderSetting speed = addSetting(new SliderSetting("Скорость", 0.35f, 0.05f, 1.0f, 0.05f));
    private final SliderSetting vertical = addSetting(new SliderSetting("Вертикальная", 0.3f, 0.05f, 1.0f, 0.05f));

    public DragonFly() {
        super("DragonFly", "Полёт как в креативе (в выживании)", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        LocalPlayer player = mc.player;
        if (player == null || player.isFallFlying()) return;

        float forward = (player.getLastSentInput().forward() ? 1 : 0) - (player.getLastSentInput().backward() ? 1 : 0);
        float strafe = (player.getLastSentInput().left() ? 1 : 0) - (player.getLastSentInput().right() ? 1 : 0);
        float yawRad = player.getYRot() * 0.017453292F;
        double sp = speed.getValue();
        double x = (-Math.sin(yawRad) * forward + Math.cos(yawRad) * strafe) * sp;
        double z = (Math.cos(yawRad) * forward + Math.sin(yawRad) * strafe) * sp;
        double up = (player.getLastSentInput().jump() ? 1 : 0) - (player.getLastSentInput().shift() ? 1 : 0);
        double y = up * vertical.getValue();

        player.setDeltaMovement(new Vec3(x, y, z));
        player.fallDistance = 0.0f;
    }
}