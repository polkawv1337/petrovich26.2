package client_files.ClientikUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class Rotation {
    private static final Minecraft mc = Minecraft.getInstance();

    public float yaw;
    public float pitch;

    public Rotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Rotation(Vec3 eye, Vec3 target) {
        Vec3 dir = target.subtract(eye);
        this.yaw = (float) Mth.wrapDegrees(Math.toDegrees(Math.atan2(dir.z(), dir.x())) - 90.0);
        this.pitch = (float) -Math.toDegrees(Math.atan2(dir.y(), dir.horizontalDistance()));
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getDelta(Rotation target) {
        float yawDelta = Mth.wrapDegrees(target.getYaw() - this.yaw);
        float pitchDelta = target.getPitch() - this.pitch;
        return (float) Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta));
    }

    public double getDeltaDouble(Rotation target) {
        double yawDelta = Mth.wrapDegrees(target.getYaw() - yaw);
        double pitchDelta = Mth.wrapDegrees(target.getPitch() - pitch);
        return Math.hypot(yawDelta, pitchDelta);
    }

    public static float cameraYaw() {
        float yaw = mc.gameRenderer.mainCamera().yRot();
        if (!mc.options.getCameraType().isFirstPerson()) yaw += 180;
        return Mth.wrapDegrees(yaw);
    }

    public static float cameraPitch() {
        boolean thirdPerson = !mc.options.getCameraType().isFirstPerson();
        return (thirdPerson ? -1 : 1) * mc.gameRenderer.mainCamera().xRot();
    }

    public Vec3 toVector() {
        return RotationUtil.toVector(yaw, pitch);
    }
}
