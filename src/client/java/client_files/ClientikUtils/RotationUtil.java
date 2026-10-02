package client_files.ClientikUtils;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class RotationUtil {

    private RotationUtil() {
    }

    public static Rotation to(Vec3 eye, Vec3 target) {
        Vec3 dir = target.subtract(eye);
        float yaw = (float) Mth.wrapDegrees(Math.toDegrees(Math.atan2(dir.z(), dir.x())) - 90.0);
        float pitch = (float) -Math.toDegrees(Math.atan2(dir.y(), dir.horizontalDistance()));
        return new Rotation(yaw, pitch);
    }

    public static Rotation to(Entity from, Entity target) {
        return to(from.getEyePosition(), target.getBoundingBox().getCenter());
    }

    public static Vec3 toVector(float yaw, float pitch) {
        float f = pitch * 0.017453292F;
        float g = -yaw * 0.017453292F;
        float h = Mth.cos(g);
        float i = Mth.sin(g);
        float j = Mth.cos(f);
        float k = Mth.sin(f);
        return new Vec3(i * j, -k, h * j);
    }
}
