package client_files.Petrovich.Combat;

import client_files.ClientikUtils.BooleanSetting;
import client_files.ClientikUtils.Category;
import client_files.ClientikUtils.ModeSetting;
import client_files.ClientikUtils.Rotation;
import client_files.ClientikUtils.RotationUtil;
import client_files.ClientikUtils.SliderSetting;
import client_files.Module;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class ElytraTarget extends Module {

    private final SliderSetting range = addSetting(new SliderSetting("Дистанция", 50.0f, 10.0f, 100.0f, 1.0f));
    private final SliderSetting smoothness = addSetting(new SliderSetting("Плавность", 1.5f, 0.1f, 10.0f, 0.1f));
    private final BooleanSetting elytraSlowdown = addSetting(new BooleanSetting("Замедление на элитрах", true));
    private final ModeSetting slowdownMode = addSetting(new ModeSetting("Режим замедления", "По радиусу", "Перед ударом"));
    private final SliderSetting slowdownRadius = addSetting(new SliderSetting("Радиус замедления", 3.0f, 1.0f, 6.0f, 0.1f));
    private final SliderSetting minSpeed = addSetting(new SliderSetting("Мин. скорость", 0.3f, 0.1f, 0.9f, 0.05f));
    private final SliderSetting preHitTicks = addSetting(new SliderSetting("Тики до удара", 3.0f, 1.0f, 10.0f, 1.0f));
    private final BooleanSetting elytraTurnaround = addSetting(new BooleanSetting("Разворот на элитрах", false));
    private final BooleanSetting hitAfterOvertake = addSetting(new BooleanSetting("Бить после перегона", true));
    private final SliderSetting overtakeStrength = addSetting(new SliderSetting("Сила перегона", 5.0f, 0.5f, 10.0f, 0.5f));

    public ElytraTarget() {
        super("ElytraTarget", "Стремится к игроку при полёте на элитрах", Category.COMBAT);
    }

    public float getPredictValue() {
        return overtakeStrength.getValue() * 0.65f;
    }

    public boolean isElytraSlowdown() {
        return elytraSlowdown.getValue();
    }

    public boolean isSlowdownBeforeHit() {
        return "Перед ударом".equals(slowdownMode.getValue());
    }

    public int getPreHitTicks() {
        return (int) preHitTicks.getValue();
    }

    public float getSlowdownRadius() {
        return slowdownRadius.getValue();
    }

    public float getMinSpeed() {
        return minSpeed.getValue();
    }

    public boolean isElytraTurnaround() {
        return elytraTurnaround.getValue();
    }

    public boolean isHitAfterOvertake() {
        return hitAfterOvertake.getValue();
    }

    public float getOvertakeStrength() {
        return overtakeStrength.getValue();
    }

    @Override
    public void onTick() {
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || !player.isFallFlying()) return;

        if (elytraSlowdown.getValue() && Aura.isSlowdownActive) {
            Vec3 motion = player.getDeltaMovement();
            if (motion.horizontalDistanceSqr() > 0.001) {
                player.setDeltaMovement(player.getDeltaMovement().multiply(0.8, 1.0, 0.8));
            }
            return;
        }

        Player target = null;
        double best = range.getValue() * range.getValue();
        for (Player other : mc.level.players()) {
            if (other == player || !other.isAlive()) continue;
            if (!other.isFallFlying()) continue;
            double dist = player.distanceToSqr(other);
            if (dist < best) {
                best = dist;
                target = other;
            }
        }
        if (target == null) return;

        Rotation wanted = RotationUtil.to(player.getEyePosition(), target.getBoundingBox().getCenter());
        float speed = smoothness.getValue() * 0.1f + 0.1f;
        float yawDelta = net.minecraft.util.Mth.wrapDegrees(wanted.getYaw() - player.getYRot());
        float pitchDelta = wanted.getPitch() - player.getXRot();
        player.setYRot(player.getYRot() + yawDelta * speed);
        player.setXRot(net.minecraft.util.Mth.clamp(player.getXRot() + pitchDelta * speed, -90.0f, 90.0f));
        player.yHeadRot = player.getYRot();
    }
}