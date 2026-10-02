package client_files.Petrovich.Movement;

import client_files.ClientikUtils.Category;
import client_files.ClientikUtils.SliderSetting;
import client_files.Module;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;

public class Speed extends Module {

    private final SliderSetting speed = addSetting(new SliderSetting("Скорость", 0.28f, 0.1f, 1.0f, 0.01f));
    private final SliderSetting airBoost = addSetting(new SliderSetting("Буст в прыжке", 1.25f, 1.0f, 2.0f, 0.05f));
    private final SliderSetting noPotionScale = addSetting(new SliderSetting("Без зелья скорости", 0.85f, 0.5f, 1.0f, 0.01f));

    public Speed() {
        super("Speed", "Быстрое передвижение по земле и в прыжке", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        LocalPlayer player = mc.player;
        if (player == null || player.isUnderWater() || player.isFallFlying()) return;

        Input input = player.getLastSentInput();
        float forward = (input.forward() ? 1 : 0) - (input.backward() ? 1 : 0);
        float strafe = (input.left() ? 1 : 0) - (input.right() ? 1 : 0);
        if (forward == 0 && strafe == 0) return;

        boolean grounded = player.onGround();
        boolean jumping = input.jump();

        Vec3 motion = player.getDeltaMovement();
        double y = motion.y;
        if (jumping) {
            if (grounded) {
                y = 0.42;
            } else if (y < 0.0) {
                y = Math.max(y * 0.7, -0.25);
            }
        }

        float length = (float) Math.sqrt(forward * forward + strafe * strafe);
        double mul = speed.getValue() / length;

        MobEffectInstance effect = player.getEffect(MobEffects.SPEED);
        if (effect != null) {
            mul *= 1.0 + 0.2 * (effect.getAmplifier() + 1);
        } else {
            mul *= noPotionScale.getValue();
        }

        if (!grounded) {
            mul *= airBoost.getValue();
        }

        float yawRad = player.getYRot() * 0.017453292F;
        double x = (-Math.sin(yawRad) * forward + Math.cos(yawRad) * strafe) * mul;
        double z = (Math.cos(yawRad) * forward + Math.sin(yawRad) * strafe) * mul;

        player.setDeltaMovement(new Vec3(x, y, z));
    }
}
